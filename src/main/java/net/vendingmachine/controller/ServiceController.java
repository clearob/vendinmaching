package net.vendingmachine.controller;


import net.vendingmachine.domain.Coin;
import net.vendingmachine.domain.Product;
import net.vendingmachine.domain.Role;
import net.vendingmachine.domain.User;
import net.vendingmachine.product.ProductService;
import net.vendingmachine.user.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("api")
public class ServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private SessionRegistry sessionRegistry;

    private final UserService userService;
    private final ProductService productService;

    public ServiceController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }



    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName(Authentication authentication) {
        LOGGER.info("user is:"+authentication.getName());
        return authentication.getName();
    }




    @GetMapping(value = "/login/{u}/{p}",  produces="application/json")
    @ResponseBody
    public ResponseEntity<String> login(final Model model,Authentication authentication,HttpServletRequest request,@PathVariable String u,@PathVariable String p) {
        JSONObject resp = new JSONObject();
        LOGGER.info("login....");
       List<String> users= userService.getUsersFromSessionRegistry();

        if (users.contains(u)) {
            resp.put("warning","There is already an active session using your account");
            return  ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://localhost:8080/api/logout/all")).build();

        }else {

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(u, p);
            token.setDetails(new WebAuthenticationDetails(request));
            User user = new User();
            user.setUsername(u);
            user.setPassword(p);
            user.setRole(Role.ADMIN);
            user.setDeposit(50);
            userService.create(user);

            Principal principal = new Principal() {
                @Override
                public String getName() {
                    return u;
                }
            };


            sessionRegistry.registerNewSession(request.getSession().getId(), principal);
            return new ResponseEntity<String>(resp.toString(), HttpStatus.CREATED);
        }
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();



        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }


    @RequestMapping(value="/logout/all",method = RequestMethod.GET)
    public String logout(HttpServletRequest request){
        LOGGER.info("logout...");
        HttpSession httpSession = request.getSession();
        httpSession.invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/api/login";
    }








    public List<String> getUsersFromSessionRegistry() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty())
                .map(Object::toString)
                .collect(Collectors.toList());
    }




    @RequestMapping(value = "/allusers", method = RequestMethod.GET,produces="application/json")
    @ResponseBody
    public List<User> getAllUsers() {
        LOGGER.info("list of users");
        return userService.getAllUsers();
    }


    @PostMapping(value = "/user", consumes="application/json")
    public HttpStatus create(@RequestBody
                                     User entity) {
        ResponseEntity<User> response = new ResponseEntity<>(entity, HttpStatus.BAD_REQUEST);
        LOGGER.info("add user");
        User record = null;
        try {
            record = userService.create(entity);
            response = new ResponseEntity<>(record, HttpStatus.CREATED);
        }catch(Exception ex){
            response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
        }
        return response.getStatusCode();
    }

    @GetMapping(value = "/buy/{productId}/{amount}", produces="application/json")
    @ResponseBody
    public  ResponseEntity<String> buy(@PathVariable long productId,@PathVariable int amount,
                                       Authentication authentication) {
        LOGGER.info("buy product {}. amount {}",productId,amount);
        JSONObject resp = new JSONObject();
        if(hasRole("BUYER")) {
            Optional<Product> optProduct = productService.findByProduct(productId);
            User user = userService.findUserByName(authentication.getName());
            if (optProduct.isPresent()) {
                long available = optProduct.get().getAmountAvailable();
                String productName = optProduct.get().getProductName();
                long cost = optProduct.get().getCost();
                long requiredDeposit = cost * amount;
                if (available >= amount){
                    if( user.getDeposit()>=requiredDeposit){
                        resp.put("you are  buying product ", optProduct.get().getProductName());
                        resp.put("total expense ", requiredDeposit);
                        long change = user.getDeposit()-requiredDeposit;
                        String calcChange = calculateChange(change);
                        resp.put("change ",calcChange);
                        //decrease product's amount available
                        optProduct.get().setAmountAvailable(available-amount);
                        productService.update(optProduct.get());
                        user.setDeposit(change);
                        userService.updateDeposit(user);
                    }else
                        resp.put("warning", "your account deposit is insufficient ...");
                }else
                    resp.put("warning", "for chosen product avaliability is insufficient");
            }else
                resp.put("warning", "productid not existing");
        }
        else
            resp.put("warning", "your account is not authorized to buy");
        return new ResponseEntity<String>(resp.toString(), HttpStatus.CREATED);
    }



    @PutMapping(value = "/deposit", consumes="application/json")
    public HttpStatus deposit(@RequestBody
                                      User entity,
                              Authentication authentication) {
        ResponseEntity<User> response = new ResponseEntity<User>(entity,HttpStatus.NOT_ACCEPTABLE);

        if(hasRole("BUYER")  && entity.getUsername().equalsIgnoreCase(authentication.getName())) {
            LOGGER.info("deposit");
            User record = null;
            if(Coin.accpetedAmount(entity.getDeposit())) {
                long sum = userService.getUserById(entity.getId()).getDeposit() + entity.getDeposit();
                entity.setDeposit(sum);
                userService.updateDeposit(entity);
                response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
            }
        }
        return response.getStatusCode();
    }


    @PutMapping(value = "/reset", consumes="application/json")
    public HttpStatus resetDeposit(@RequestBody
                                           User entity,
                                   Authentication authentication) {
        ResponseEntity<User> response = new ResponseEntity<User>(entity,HttpStatus.NOT_ACCEPTABLE);

        if(hasRole("BUYER")  && entity.getUsername().equalsIgnoreCase(authentication.getName())) {
            LOGGER.info("reset deposit");
            User record = null;

            entity.setDeposit(0);
            userService.updateDeposit(entity);
            response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
        }
        return response.getStatusCode();
    }


    @RequestMapping(value = "/products", method = RequestMethod.GET,produces="application/json")
    public List<Product> getProducts() {
        LOGGER.info("list of products");
        return productService.getAllProducts();
    }

    //@PreAuthorize("hasRole(SELLER)")
    @PostMapping(value = "/createproduct", consumes="application/json")
    public HttpStatus create(@RequestBody
                                     Product entity) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.METHOD_NOT_ALLOWED);;
        if(hasRole("SELLER")) {
            LOGGER.info("add product");
            Product record = null;
            try {
                productService.create(entity);
                response = new ResponseEntity<>(record, HttpStatus.CREATED);
            }catch (Exception ex){
                response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
            }
        }
        return response.getStatusCode();
    }

    @PutMapping(value = "/updateproduct", consumes="application/json")
    public HttpStatus updateProduct(@RequestBody
                                            Product entity,
                                    Authentication authentication) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.NOT_ACCEPTABLE);

        if(hasRole("SELLER") && entity.getSellerId().equalsIgnoreCase(authentication.getName())) {
            LOGGER.info("update product");
            Product record = null;
            if(productService.findByProduct(entity.getId()).isPresent()) {
                if(entity.getCost()%5==0) {
                    productService.update(entity);
                    response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
                }else
                    response = new ResponseEntity<>(record, HttpStatus.BAD_REQUEST);
            }else
                response = new ResponseEntity<>(record, HttpStatus.NOT_FOUND);
        }
        return response.getStatusCode();
    }

    @DeleteMapping(value = "/deleteproduct", consumes="application/json")
    public HttpStatus deleteProduct(@RequestBody
                                            Product entity,
                                    Authentication authentication) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.NOT_ACCEPTABLE);

        if(hasRole("SELLER") && entity.getSellerId().equalsIgnoreCase(authentication.getName())) {
            LOGGER.info("delete product");
            Product record = null;

            if(productService.findByProduct(entity.getId()).isPresent()) {
                productService.delete(entity);
                response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
            }else
                response = new ResponseEntity<>(record, HttpStatus.NOT_FOUND);
        }
        return response.getStatusCode();
    }


    protected boolean hasRole(String role) {

        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null)
            return false;

        Authentication authentication = context.getAuthentication();
        if (authentication == null)
            return false;

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (role.equals(auth.getAuthority()))
                return true;
        }

        return false;
    }


    private String calculateChange(long change){

        List<Long> moneyback = new ArrayList<Long>();
        long div;
        long mod = change;

        Stream<Coin> coins = Arrays.stream(Coin.values()).sorted(Comparator.reverseOrder()) ;
        for (Coin coin : coins.toList()) {
            div = mod/coin.getAmount();
            for(int j=0 ;j<div;j++)
                moneyback.add(coin.getAmount());
            mod = change%coin.getAmount();
        }
        return Arrays.toString(moneyback.toArray());

    }


}

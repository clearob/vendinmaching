package net.vendingmachine.controller;


import net.vendingmachine.config.JwtUtils;
import net.vendingmachine.domain.Coin;
import net.vendingmachine.domain.Product;
import net.vendingmachine.domain.User;
import net.vendingmachine.payload.JwtResponse;
import net.vendingmachine.payload.LoginRequest;
import net.vendingmachine.payload.ToBuy;
import net.vendingmachine.product.ProductService;
import net.vendingmachine.user.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;



@RestController
@RequestMapping("api")
public class ServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;

    List<String> loggedUser = new ArrayList<>();


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








    @RequestMapping(value="/logout/all",method = RequestMethod.GET)
    public String logout(HttpServletRequest request){
        LOGGER.info("logout...");
        HttpSession httpSession = request.getSession();

        List<SessionInformation> sessions = userService.getActiveSessions();
        sessions.stream().forEach(elem -> {
            elem.expireNow();
            sessionRegistry.removeSessionInformation(elem.getSessionId());
            sessionRegistry.getAllPrincipals().remove(elem.getPrincipal());

        });
        sessionRegistry.removeSessionInformation(httpSession.getId());
        httpSession.invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
        sessionRegistry.getAllPrincipals().clear();




        return "redirect:/api/login";
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


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,HttpServletRequest request) {
        JSONObject resp = new JSONObject();

        System.out.println(loginRequest.getUsername());
        System.out.println(loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));


        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user =  userService.findUserByName(authentication.getName());
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(),false);

        if(sessions.size()>0){
            HttpSession httpSession = request.getSession();
            httpSession.invalidate();

            loggedUser.remove(loginRequest.getUsername());
            sessionRegistry.removeSessionInformation(httpSession.getId());
            sessionRegistry.getAllPrincipals().clear();

            SecurityContextHolder.getContext().setAuthentication(null);
            resp.put("warning","There is already an active session using your account");
            return  ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://localhost:8080/api/logout/all")).build();
        }
        else {
            loggedUser.add(loginRequest.getUsername());
            sessionRegistry.registerNewSession(request.getSession().getId(), authentication.getPrincipal());
            return ResponseEntity.ok(new JwtResponse(jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getRole()));
        }
    }


    @PostMapping(value = "/buy", produces="application/json")
    @ResponseBody
    public  ResponseEntity<String> buy(@RequestBody
                                               ToBuy entity,
                                       @RequestHeader("Authorization") String authString,
                                       Authentication authentication) {
        LOGGER.info("buy product {}. amount {}",entity.getIdProduct(),entity.getAmount());
        JSONObject resp = new JSONObject();
        if(hasRole("BUYER")
                && checkUserExist(authentication.getName())
                && jwtUtils.validateJwtToken(authString)) {
            Optional<Product> optProduct = productService.findByProduct(entity.getIdProduct());
            User user = userService.findUserByName(authentication.getName());
            if (optProduct.isPresent()) {
                long available = optProduct.get().getAmountAvailable();
                String productName = optProduct.get().getProductName();
                long cost = optProduct.get().getCost();
                long requiredDeposit = cost * entity.getAmount();
                if (available >= entity.getAmount()){
                    if( user.getDeposit()>=requiredDeposit){
                        resp.put("you are  buying product ", optProduct.get().getProductName());
                        resp.put("total expense ", requiredDeposit);
                        long change = user.getDeposit()-requiredDeposit;
                        String calcChange = calculateChange(change);
                        resp.put("change ",calcChange);
                        //decrease product's amount available
                        optProduct.get().setAmountAvailable(available-entity.getAmount());
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
                              @RequestHeader("Authorization") String authString,
                              Authentication authentication) {
        ResponseEntity<User> response = new ResponseEntity<User>(entity,HttpStatus.NOT_ACCEPTABLE);
        User record = null;

        if(hasRole("BUYER")
                && checkUserExist(authentication.getName())
                && jwtUtils.validateJwtToken(authString)) {
            LOGGER.info("deposit");
            User user = userService.findUserByName(jwtUtils.getUserNameFromJwtToken(authString));
            entity.setId(user.getId());
            if(Coin.accpetedAmount(entity.getDeposit())) {
                long sum = userService.getUserById(entity.getId()).getDeposit() + entity.getDeposit();
                entity.setUsername(user.getUsername());
                entity.setPassword(user.getPassword());
                entity.setRole(user.getRole());
                entity.setDeposit(sum);
                userService.updateDeposit(entity);
                response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
            }
        }else
            response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
        return response.getStatusCode();
    }


    @PutMapping(value = "/reset", consumes="application/json")
    public HttpStatus resetDeposit(@RequestBody
                                           User entity,
                                   @RequestHeader("Authorization") String authString,
                                   Authentication authentication) {
        ResponseEntity<User> response = new ResponseEntity<User>(entity,HttpStatus.NOT_ACCEPTABLE);


        User record = null;
        if(hasRole("BUYER")
                && checkUserExist(authentication.getName())
                && jwtUtils.validateJwtToken(authString) ) {
            LOGGER.info("reset deposit");
            User user = userService.findUserByName(jwtUtils.getUserNameFromJwtToken(authString));
            entity.setId(user.getId());
            entity.setUsername(user.getUsername());
            entity.setPassword(user.getPassword());
            entity.setRole(user.getRole());
            entity.setDeposit(0);
            userService.updateDeposit(entity);
            response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
        }else
            response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
        return response.getStatusCode();
    }


    @RequestMapping(value = "/products", method = RequestMethod.GET,produces="application/json")
    public List<Product> getProducts() {
        LOGGER.info("list of products");
        return productService.getAllProducts();
    }


    @PostMapping(value = "/createproduct", consumes="application/json")
    public HttpStatus create(@RequestBody
                                     Product entity,
                             @RequestHeader("Authorization") String authString,
                             Authentication authentication) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.METHOD_NOT_ALLOWED);;
        Product record = null;
        if(hasRole("SELLER")
                && checkUserExist(authentication.getName())
                && jwtUtils.validateJwtToken(authString)) {
            LOGGER.info("add product");
            User user = userService.findUserByName(jwtUtils.getUserNameFromJwtToken(authString));
            try {
                entity.setSellerId(user.getUsername());
                productService.create(entity);
                response = new ResponseEntity<>(record, HttpStatus.CREATED);
            }catch (Exception ex){
                response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
            }
        }else
            response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
        return response.getStatusCode();
    }

    @PutMapping(value = "/updateproduct", consumes="application/json")
    public HttpStatus updateProduct(@RequestBody
                                            Product entity,
                                    @RequestHeader("Authorization") String authString,
                                    Authentication authentication) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.NOT_ACCEPTABLE);
        Product record = null;


        LOGGER.info("update product");
        if (productService.findByProduct(entity.getId()).isPresent()) {
            record = productService.findByProduct(entity.getId()).get();
            if(hasRole("SELLER")  && jwtUtils.validateJwtToken(authString)
                    && checkUserExist(authentication.getName())
                    && record.getSellerId().equalsIgnoreCase(authentication.getName())) {
                if (entity.getCost() % 5 == 0) {
                    record.setCost(entity.getCost());
                    record.setAmountAvailable(entity.getAmountAvailable());
                    productService.update(record);
                    response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
                } else
                    response = new ResponseEntity<>(record, HttpStatus.BAD_REQUEST);
            } else
                response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
        }else
            response = new ResponseEntity<>(record, HttpStatus.NOT_FOUND);


        return response.getStatusCode();
    }

    private boolean checkUserExist(String name){
        List<Object> users  = sessionRegistry.getAllPrincipals();
        boolean res = false;
       if(loggedUser.contains(name))
           res=true;
        return res;
    }


    @DeleteMapping(value = "/deleteproduct", consumes="application/json")
    public HttpStatus deleteProduct(@RequestBody
                                            Product entity,
                                    @RequestHeader("Authorization") String authString,
                                    Authentication authentication) {
        LOGGER.info("delete product");
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.NOT_ACCEPTABLE);
        Product record = null;
        if (productService.findByProduct(entity.getId()).isPresent()) {
            record = productService.findByProduct(entity.getId()).get();
            if(hasRole("SELLER")
                    && jwtUtils.validateJwtToken(authString)
                    && checkUserExist(authentication.getName())
                    && record.getSellerId().equalsIgnoreCase(authentication.getName())) {
                productService.delete(entity);
                response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
            }else
                response = new ResponseEntity<>(record, HttpStatus.NOT_ACCEPTABLE);
        }else
            response = new ResponseEntity<>(record, HttpStatus.NOT_FOUND);

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


    protected String calculateChange(long change){

        List<Long> moneyback = new ArrayList<Long>();
        long div;
        long mod = change;

        Stream<Coin> coins = Arrays.stream(Coin.values()).sorted(Comparator.reverseOrder()) ;
        for (Coin coin : coins.toList()) {
            div = mod/coin.getAmount();
            for(int j=0 ;j<div;j++)
                moneyback.add(coin.getAmount());
            change = mod;
            mod = change%coin.getAmount();
        }
        return Arrays.toString(moneyback.toArray());

    }



}

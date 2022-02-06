package net.vendingmachine.controller;


import net.vendingmachine.domain.Deposit;
import net.vendingmachine.domain.Product;
import net.vendingmachine.domain.User;
import net.vendingmachine.product.ProductService;
import net.vendingmachine.user.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.util.EnumUtils;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("api")
public class ServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

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


    @RequestMapping(value = "/allusers", method = RequestMethod.GET,produces="application/json")
    @ResponseBody
    public List<User> getAllUsers() {
        LOGGER.info("list of users");
        return userService.getAllUsers();
    }


    @PostMapping(value = "/user", consumes="application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public HttpStatus create(@RequestBody
                                     User entity) {
        ResponseEntity<User> response = new ResponseEntity<>(entity, HttpStatus.BAD_REQUEST);
        LOGGER.info("add user");
        User record = null;
        record = userService.create(entity);
        response = new ResponseEntity<>(record, HttpStatus.CREATED);
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
                        resp.put("change ", user.getDeposit()-requiredDeposit);
                        //decrease product's amount available
                        optProduct.get().setAmountAvailable(available-amount);
                        productService.update(optProduct.get());
                    }else
                        resp.put("warning", "your account deposit is insufficient ...");
                }else
                    resp.put("warning", "for chosen product avaliability is insufficient");
            }


        }
        else
            resp.put("warning", "your account is not authorized to buy");
        return new ResponseEntity<String>(resp.toString(), HttpStatus.CREATED);
    }



    @PutMapping(value = "/deposit", consumes="application/json")
    //@ResponseStatus(HttpStatus.CREATED)
    public HttpStatus deposit(@RequestBody
                                      User entity,
                              Authentication authentication) {
        ResponseEntity<User> response = new ResponseEntity<User>(entity,HttpStatus.NOT_ACCEPTABLE);

        if(hasRole("BUYER")  && entity.getUsername().equalsIgnoreCase(authentication.getName())) {
            LOGGER.info("deposit");
            User record = null;
            if(Deposit.findByAmount(entity.getDeposit())) {
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
    @ResponseBody
    public List<Product> getProducts() {
        LOGGER.info("list of products");
        return productService.getAllProducts();
    }

    //@PreAuthorize("hasRole(SELLER)")
    @PostMapping(value = "/createproduct", consumes="application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public HttpStatus create(@RequestBody
                                     Product entity) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.METHOD_NOT_ALLOWED);;
        if(hasRole("SELLER")) {
            LOGGER.info("add product");
            Product record = null;
            productService.create(entity);
            response = new ResponseEntity<>(record, HttpStatus.CREATED);
        }
        return response.getStatusCode();
    }

    @PutMapping(value = "/updateproduct", consumes="application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public HttpStatus updateProduct(@RequestBody
                                            Product entity,
                                    Authentication authentication) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.NOT_ACCEPTABLE);

        if(hasRole("SELLER") && entity.getSellerId().equalsIgnoreCase(authentication.getName())) {
            LOGGER.info("update product");
            Product record = null;
            productService.update(entity);
            response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
        }
        return response.getStatusCode();
    }

    @DeleteMapping(value = "/deleteproduct", consumes="application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public HttpStatus deleteProduct(@RequestBody
                                            Product entity,
                                    Authentication authentication) {
        ResponseEntity<Product> response = new ResponseEntity<Product>(entity,HttpStatus.NOT_ACCEPTABLE);

        if(hasRole("SELLER") && entity.getSellerId().equalsIgnoreCase(authentication.getName())) {
            LOGGER.info("delete product");
            Product record = null;
            productService.delete(entity);
            response = new ResponseEntity<>(record, HttpStatus.ACCEPTED);
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

}

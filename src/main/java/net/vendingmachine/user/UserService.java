package net.vendingmachine.user;


import net.vendingmachine.domain.Product;
import net.vendingmachine.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface UserService extends UserDetailsService {

    User getUserById(long id);

    //Optional<User> getUserByUsername(String username);

    //Collection<User> getAllUsers();

    User create(User user);

    List<User> getAllUsers();


    void updateDeposit(User entity);

    User findUserByName(String name);
}

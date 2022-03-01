package net.vendingmachine.user;


import net.vendingmachine.domain.Product;
import net.vendingmachine.domain.User;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface UserService extends UserDetailsService {

    User getUserById(long id);



    User create(User user);

    List<User> getAllUsers();


    void updateDeposit(User entity);

    User findUserByName(String name);

    List<String> getUsersFromSessionRegistry();

     <U> Optional<? extends U> findByToken(String s);

    public List<SessionInformation> getActiveSessions();
    public User getUser( SessionInformation session );
    public void logoutSession(String sessionId);




}

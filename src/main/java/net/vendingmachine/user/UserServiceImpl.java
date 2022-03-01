package net.vendingmachine.user;


import net.vendingmachine.domain.Product;
import net.vendingmachine.domain.Role;
import net.vendingmachine.domain.User;
import net.vendingmachine.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionRegistry sessionRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);


    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }





    @Override
    public UserDetails  loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =userRepository.findOneByUsername(username);

        if(user == null){
            throw new UsernameNotFoundException(username);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));



        Set<GrantedAuthority> grantedAuthorities = new HashSet<>(); // use list if you wish

        grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                grantedAuthorities
        );

    }

    @Override
    public User getUserById(long id) {
        return userRepository.getById(id);
    }

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateDeposit(User entity) {
        userRepository.save(entity);
    }

    @Override
    public User findUserByName(String name) {
        return userRepository.findOneByUsername(name);
    }


    @Override
    public List<String> getUsersFromSessionRegistry() {
        List<String> users = new ArrayList<>();
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();


        return allPrincipals.stream()
                .filter((u) -> !sessionRegistry.getAllSessions(u, false)
                        .isEmpty())
                .map(o -> { if(o instanceof  Principal) return ((Principal) o).getName();
                    else
                  return ((org.springframework.security.core.userdetails.User)o).getUsername();
                }).collect(Collectors.toList());

        /*
        return allPrincipals.stream().map(elem ->

            ((org.springframework.security.core.userdetails.User)elem).getUsername()
        ).collect(Collectors.toList());

         */
       // return allPrincipals.stream().map(elem -> (String)elem).collect(Collectors.toList());
    }



    public List<SessionInformation> getActiveSessions()
    {
        List<SessionInformation> activeSessions = new ArrayList<>();
        for ( Object principal : sessionRegistry.getAllPrincipals() )
        {
            activeSessions.addAll( sessionRegistry.getAllSessions( principal, false ) );
        }
        return activeSessions;
    }

    public User getUser( SessionInformation session )
    {
        Object principalObj = session.getPrincipal();
        if ( principalObj instanceof User )
        {
            User user = (User) principalObj;
            return user;
        }
        return null;
    }

    public void logoutSession(String sessionId) {
        SessionInformation session = sessionRegistry.getSessionInformation(sessionId);
        if (session != null) {
            session.expireNow();
        }
    }


    @Override
    public <U> Optional<? extends U> findByToken(String s) {
        return Optional.empty();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

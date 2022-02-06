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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);


    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }





    /*
    @Override
    public Optional<User> getUserById(long id) {
        LOGGER.debug("Getting user={}", id);
        return Optional.ofNullable(userRepository.findById(id).get());
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        LOGGER.debug("Getting user by username ={}",username);
        return userRepository.findOneByUsername(username);
    }

    @Override
    public Collection<User> getAllUsers() {
        LOGGER.debug("Getting all users");
        return userRepository.findAll(Sort.by("email"));
    }

    @Override

*/
    @Override
    public UserDetails  loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =userRepository.findOneByUsername(username);

        if(user == null){
            throw new UsernameNotFoundException(username);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));



        Set<GrantedAuthority> grantedAuthorities = new HashSet<>(); // use list if you wish
        /*
        for (Role role : user.getRole()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }

         */
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


}

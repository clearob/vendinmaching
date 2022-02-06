package net.vendingmachine.repository;

import net.vendingmachine.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    User findOneByUsername(String username);

    //Collection<User> getAllUsers();

}

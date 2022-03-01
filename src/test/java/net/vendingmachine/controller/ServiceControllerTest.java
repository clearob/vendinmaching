package net.vendingmachine.controller;

import net.vendingmachine.VendingMachineApplication;
import net.vendingmachine.product.ProductService;
import net.vendingmachine.product.ProducteServiceImpl;
import net.vendingmachine.repository.ProductRepository;
import net.vendingmachine.repository.UserRepository;
import net.vendingmachine.user.UserService;
import net.vendingmachine.user.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceController.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = {"net.vendingmachine"})
class ServiceControllerTest {

    @Autowired
    private ServiceController serviceController;

    @Test
    void calculateChange() {
        long change = 10;
        String res = serviceController.calculateChange(change);
        assertTrue(res.equalsIgnoreCase("[20, 10]"));

    }
}
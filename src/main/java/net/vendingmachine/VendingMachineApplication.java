package net.vendingmachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.servlet.annotation.WebServlet;


@SpringBootApplication
//@ComponentScan(basePackages = {"com.xxx.xxx.dao"})
@ComponentScan(basePackages = {"net.vendingmachine"})//to scan repository files
//@EntityScan("net.vendingmachine.domain" )

@EnableJpaRepositories("net.vendingmachine.repository")

public class VendingMachineApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(VendingMachineApplication.class, args);
    }
/*
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(VendingMachineApplication.class);
    }
*/


}

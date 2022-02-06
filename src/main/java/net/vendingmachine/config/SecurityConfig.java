package net.vendingmachine.config;

import net.vendingmachine.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
//@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    //@Override
    protected void configure_old(HttpSecurity http) throws Exception {
       /*
        http.authorizeRequests()
                .antMatchers("/", "/public/**","/h2/**").permitAll()
                .antMatchers("/user/**").permitAll()
                .antMatchers("/users/**").permitAll()
                .anyRequest().permitAll() //.fullyAuthenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .failureUrl("/login?error")
                .usernameParameter("email")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .deleteCookies("remember-me")
                .logoutSuccessUrl("/")
                .permitAll()
                .and()
                .rememberMe();

        */

        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/h2/**").permitAll()
                .antMatchers("/login/**").fullyAuthenticated();


        http.csrf().disable();
        http.headers().frameOptions().disable();

    }


   // @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        String[] publicUrls=  {"/api/user", "/api/allusers", "/api/products"};

        http
                .csrf().disable()
                .authorizeRequests()
                 .antMatchers(publicUrls).permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncodeer());

    }



    @Bean
    public PasswordEncoder passwordEncodeer() {
        return new BCryptPasswordEncoder();
    }


    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(userDetailsService);

    }

}
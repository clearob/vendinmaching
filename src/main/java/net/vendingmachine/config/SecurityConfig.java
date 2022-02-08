package net.vendingmachine.config;

import net.vendingmachine.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.security.AuthProvider;
import java.security.Provider;


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
//@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;


    protected void configure(HttpSecurity http) throws Exception
    {
        String[] publicUrls=  {"/api/login/**","/api/user", "/api/allusers", "/api/products"};

        http
                .csrf().disable()
                .authorizeRequests()
                 .antMatchers(publicUrls).permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
        /*
        http.logout()
            .logoutUrl("/api/logout/all")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID");

        http
                .sessionManagement()
                .maximumSessions(1).sessionRegistry(sessionRegistry());
        http.sessionManagement().maximumSessions(1).expiredUrl("/api/login?expired=true");
*/
        http.sessionManagement().maximumSessions(1);
    }


    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
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
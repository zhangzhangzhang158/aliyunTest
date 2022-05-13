package org.aming.web.qq.security;

import org.aming.web.qq.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 * @author daming
 * @version 2017/10/1.
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String salt = "web_qq";

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    @Primary
    public PasswordEncoder getPasswordEncoder(){
        return new StandardPasswordEncoder(salt);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsService).passwordEncoder(getPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.authorizeRequests()
				.antMatchers("/swagger*").permitAll()
				.antMatchers("/v2/api-docs").permitAll()
                .antMatchers("/**/addUser").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .failureUrl("/login?error")
                    .permitAll()
                .and()
                .logout().permitAll();
        http.csrf().disable();
    }
}

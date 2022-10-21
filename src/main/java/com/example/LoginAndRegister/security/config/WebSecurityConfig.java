package com.example.LoginAndRegister.security.config;

import com.example.LoginAndRegister.appuser.AppUserService;
import com.example.LoginAndRegister.security.CustomAuthenticationProvider;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig /*extends WebSecurityConfigurerAdapter*/ {

    private final AppUserService appUserService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CustomAuthenticationProvider customAuthenticationProvider;

/*    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("api/registration/**")
                .permitAll()
                .anyRequest()
                .authenticated().and()
                .formLogin();
    }*/

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                //.antMatchers("/api/registration/**","/registration/**","/styles/**")
                .antMatchers("/api/registration/**","/api/login/**","/registration/**","/styles/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login")//custom login page
                .usernameParameter("email")  //put this if input id and name for username in login form is different from username
                .passwordParameter("password") //put this if input id and name for password in login form is different from password
                .defaultSuccessUrl("/home", true)
                .permitAll()
                .and()
                .logout();

        return http.build();
    }

/*    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }*/

/*    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(appUserService);
        return provider;
    }*/

/*    @Bean
    public AuthenticationManager authManager(HttpSecurity http)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(appUserService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }*/

    @Bean
    public AuthenticationManager authManager(HttpSecurity http)
            throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
        return authenticationManagerBuilder.build();

    }
}

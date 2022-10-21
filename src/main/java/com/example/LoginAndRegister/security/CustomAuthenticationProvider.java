package com.example.LoginAndRegister.security;

import com.example.LoginAndRegister.appuser.AppUser;
import com.example.LoginAndRegister.appuser.AppUserRepository;
import com.example.LoginAndRegister.email.EmailForm;
import com.example.LoginAndRegister.email.EmailSender;
import com.example.LoginAndRegister.registration.token.ConfirmationToken;
import com.example.LoginAndRegister.registration.token.ConfirmationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        AppUser appUser = appUserRepository.findByEmail(email).orElseThrow(()->
                new UsernameNotFoundException("User not found"));

        if(passwordEncoder.bCryptPasswordEncoder().matches(password, appUser.getPassword())){
            //if user exist but not activated account
            if(appUser.isEnabled()) {
                return new UsernamePasswordAuthenticationToken(email, password, new ArrayList<>());
            }else {
                String token = UUID.randomUUID().toString();
                ConfirmationToken confirmationToken = new ConfirmationToken(
                        token,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(15),
                        appUser
                );
                confirmationTokenService.saveConfirmationToken(confirmationToken);
                String link = "http://localhost:8080/api/registration/confirm?token=" + token;
                emailSender.send(appUser.getEmail(), EmailForm.buildEmail(appUser.getFirstName(), link));
                throw new BadCredentialsException("Check your email and activate account");
        }
        }else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}

package com.example.LoginAndRegister.appuser;

import com.example.LoginAndRegister.registration.token.ConfirmationToken;
import com.example.LoginAndRegister.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "User with email %s not exist";

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(
                ()-> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email))
        );
    }

    public String signUpUser(AppUser appUser) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken;/* = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );*/

        Optional<AppUser> userOptional = appUserRepository.findByEmail(appUser.getEmail());
        boolean userExist = userOptional.isPresent();
        if(userExist){
            boolean isActivated = userOptional.get().getEnabled();
            if(!isActivated)
            {
                AppUser user = userOptional.orElseThrow(
                        ()-> new IllegalStateException("Something went wrong. Please try again later")
                );

                confirmationToken = new ConfirmationToken(
                        token,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(15),
                        user
                );
                confirmationTokenService.saveConfirmationToken(confirmationToken);
                //return ("Email already taken. Please activate account");
                throw new IllegalStateException("Email already taken. Please activate account");
            }
            throw new IllegalStateException("Email already taken");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());

        confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        appUser.setPassword(encodedPassword);
        appUserRepository.save(appUser);

        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    public int enableAppUser(String email){
        return appUserRepository.enableAppUser(email);
    }

 /*   public AppUser signInUser(String email, String password)
    {
         appUser = loadUserByUsername(email).getAuthorities();
    }*/
}

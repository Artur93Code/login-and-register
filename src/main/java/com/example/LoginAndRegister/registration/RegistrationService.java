package com.example.LoginAndRegister.registration;

import com.example.LoginAndRegister.appuser.AppUser;
import com.example.LoginAndRegister.appuser.AppUserRole;
import com.example.LoginAndRegister.appuser.AppUserService;
import com.example.LoginAndRegister.email.EmailForm;
import com.example.LoginAndRegister.email.EmailSender;
import com.example.LoginAndRegister.registration.token.ConfirmationToken;
import com.example.LoginAndRegister.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final EmailSender emailSender;

    private final ConfirmationTokenService confirmationTokenService;
    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());

        if(!isValidEmail){
            throw new IllegalStateException("Email not valid");
        }
        String token = appUserService.signUpUser(new AppUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                AppUserRole.USER
        ));
        String link = "http://localhost:8080/api/registration/confirm?token="+token;
        emailSender.send(request.getEmail(), EmailForm.buildEmail(request.getFirstName(), link));
        return token;
    }

    @Transactional
    public String conirmToken(String token)
    {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token).orElseThrow(
                ()-> new IllegalStateException("Token not found")
        );

        if(confirmationToken.getConfirmedAt() != null){
            throw new IllegalStateException("Email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if(expiredAt.isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());

        return "Confirmed";
    }
}

package com.example.LoginAndRegister.appuser;

import com.example.LoginAndRegister.registration.RegistrationRequest;
import com.example.LoginAndRegister.registration.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
//@RequestMapping(path = "registration")
@AllArgsConstructor
public class AppUserController {

    private final RegistrationService registrationService;
    private final AppUserService appUserService;

    @GetMapping(path = "/registration")
    public String showRegisterPage() {return "register";}

    @PostMapping(path = "/registration")
    public String register(@RequestParam Map<String, String> request, RedirectAttributes redirectAttributes) {
        RegistrationRequest registrationRequest = new RegistrationRequest(
                request.get("firstName"), request.get("lastName"),request.get("email"),request.get("password"));
        String endpoint;
        try {
            registrationService.register(registrationRequest);
            redirectAttributes.addFlashAttribute("success", "Sign up Complete!");
            endpoint="redirect:login";
        }
        catch (Exception ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            endpoint="redirect:registration";
        }
        return endpoint;
    }

    @GetMapping(path = "/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }
        model.addAttribute("error", errorMessage);
        return "login";
    }

/*    @PostMapping(path = "/home")
    public String HomePage(Authentication authentication){
        authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getName();
        return "home";
    }*/

    @GetMapping(path = "/home")
    public String showHomePage(Authentication authentication, Model model){
        authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getPrincipal().toString();
        UserDetails currentUser = appUserService.loadUserByUsername(email);
        model.addAttribute("user", currentUser);
        return "home";
    }


}

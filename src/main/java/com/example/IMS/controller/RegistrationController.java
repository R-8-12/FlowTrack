package com.example.IMS.controller;

import com.example.IMS.config.GoogleOAuth2SuccessHandler;
import com.example.IMS.dto.RetailerRegistrationDto;
import com.example.IMS.dto.VendorRegistrationDto;
import com.example.IMS.dto.InvestorRegistrationDto;
import com.example.IMS.dto.UserRegistrationDto;
import com.example.IMS.model.User;
import com.example.IMS.service.EmailService;
import com.example.IMS.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private static final String GOOGLE_PLACEHOLDER_PASSWORD = "oauth_google_account";

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/retailer/google-start")
    public String startRetailerGoogleSignup(HttpServletRequest request) {
        return startGoogleSignupForRole("ROLE_RETAILER", request);
    }

    @GetMapping("/vendor/google-start")
    public String startVendorGoogleSignup(HttpServletRequest request) {
        return startGoogleSignupForRole("ROLE_VENDOR", request);
    }

    @GetMapping("/investor/google-start")
    public String startInvestorGoogleSignup(HttpServletRequest request) {
        return startGoogleSignupForRole("ROLE_INVESTOR", request);
    }

    @GetMapping("/retailer/google")
    public String selectRetailerRoleViaGoogle(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return continueGoogleSignup("ROLE_RETAILER", "/register/retailer?googleSignup=true", request, redirectAttributes);
    }

    @GetMapping("/vendor/google")
    public String selectVendorRoleViaGoogle(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return continueGoogleSignup("ROLE_VENDOR", "/register/vendor?googleSignup=true", request, redirectAttributes);
    }

    @GetMapping("/investor/google")
    public String selectInvestorRoleViaGoogle(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return continueGoogleSignup("ROLE_INVESTOR", "/register/investor?googleSignup=true", request, redirectAttributes);
    }
    
    // Retailer Registration
    @GetMapping("/retailer")
    public String showRetailerRegistrationForm(
            @RequestParam(name = "googleSignup", defaultValue = "false") boolean googleSignup,
            Model model,
            RedirectAttributes redirectAttributes) {

        RetailerRegistrationDto dto = new RetailerRegistrationDto();
        if (googleSignup) {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                return "redirect:/login";
            }
            if (hasAnyAssignedRole(currentUser)) {
                return "redirect:" + resolveDashboardByRole(currentUser);
            }
            hydrateRetailerDtoFromUser(dto, currentUser);
        }

        model.addAttribute("googleSignup", googleSignup);
        model.addAttribute("retailerDto", dto);
        return "auth/register-retailer";
    }
    
    @PostMapping("/retailer")
    public String registerRetailer(
            @Valid @ModelAttribute("retailerDto") RetailerRegistrationDto dto,
            BindingResult result,
            @RequestParam(name = "googleSignup", defaultValue = "false") boolean googleSignup,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (googleSignup) {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                return "redirect:/login";
            }
            if (hasAnyAssignedRole(currentUser)) {
                return "redirect:" + resolveDashboardByRole(currentUser);
            }
            hydrateRetailerDtoFromUser(dto, currentUser);
        }
        
        if (result.hasErrors()) {
            model.addAttribute("googleSignup", googleSignup);
            return "auth/register-retailer";
        }
        
        if (!googleSignup && !dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.retailerDto", "Passwords do not match");
            model.addAttribute("googleSignup", false);
            return "auth/register-retailer";
        }
        
        try {
            if (googleSignup) {
                User currentUser = getAuthenticatedUser();
                if (currentUser == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                    return "redirect:/login";
                }
                if (hasAnyAssignedRole(currentUser)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Your account is already configured.");
                    return "redirect:" + resolveDashboardByRole(currentUser);
                }

                User updatedUser = userService.assignRoleToExistingUser(
                        currentUser.getId(), "ROLE_RETAILER", dto.getFirstName(), dto.getLastName());

                userService.saveRegistrationHints(
                        updatedUser.getId(),
                        dto.getBusinessName(),
                        dto.getBusinessType(),
                        dto.getGstNumber(),
                        dto.getPhoneNumber(),
                        dto.getBusinessAddress());

                refreshAuthentication(updatedUser, request);
                request.getSession(true).removeAttribute(GoogleOAuth2SuccessHandler.PENDING_ROLE_SESSION_KEY);
                redirectAttributes.addFlashAttribute("successMessage", "Google sign-up completed successfully.");
                return "redirect:/retailer/dashboard";
            }

            UserRegistrationDto userDto = new UserRegistrationDto();
            userDto.setUsername(dto.getUsername());
            userDto.setEmail(dto.getEmail());
            userDto.setPassword(dto.getPassword());
            userDto.setFirstName(dto.getFirstName());
            userDto.setLastName(dto.getLastName());

            User savedUser = userService.registerUserWithRole(userDto, "ROLE_RETAILER");

            // Persist business hints so business-profile/create can be pre-populated
            userService.saveRegistrationHints(
                savedUser.getId(),
                dto.getBusinessName(),
                dto.getBusinessType(),
                dto.getGstNumber(),
                dto.getPhoneNumber(),
                dto.getBusinessAddress()
            );

            // E-01: Welcome email
            try {
                emailService.sendWelcomeEmail(dto.getEmail(),
                        dto.getFirstName() + " " + dto.getLastName(), "Retailer");
            } catch (Exception mailEx) {
                logger.warn("Welcome email failed for retailer {}: {}", dto.getEmail(), mailEx.getMessage());
            }

            logger.info("Retailer registered: {}", dto.getUsername());

            redirectAttributes.addFlashAttribute("successMessage",
                "Registration submitted successfully! Please log in and complete your business profile.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue(googleSignup ? "businessName" : "email", "error.retailerDto", e.getMessage());
            model.addAttribute("googleSignup", googleSignup);
            return "auth/register-retailer";
        }
    }
    
    // Vendor Registration
    @GetMapping("/vendor")
    public String showVendorRegistrationForm(
            @RequestParam(name = "googleSignup", defaultValue = "false") boolean googleSignup,
            Model model,
            RedirectAttributes redirectAttributes) {

        VendorRegistrationDto dto = new VendorRegistrationDto();
        if (googleSignup) {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                return "redirect:/login";
            }
            if (hasAnyAssignedRole(currentUser)) {
                return "redirect:" + resolveDashboardByRole(currentUser);
            }
            hydrateVendorDtoFromUser(dto, currentUser);
        }

        model.addAttribute("googleSignup", googleSignup);
        model.addAttribute("vendorDto", dto);
        return "auth/register-vendor";
    }
    
    @PostMapping("/vendor")
    public String registerVendor(
            @Valid @ModelAttribute("vendorDto") VendorRegistrationDto dto,
            BindingResult result,
            @RequestParam(name = "googleSignup", defaultValue = "false") boolean googleSignup,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (googleSignup) {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                return "redirect:/login";
            }
            if (hasAnyAssignedRole(currentUser)) {
                return "redirect:" + resolveDashboardByRole(currentUser);
            }
            hydrateVendorDtoFromUser(dto, currentUser);
        }
        
        if (result.hasErrors()) {
            model.addAttribute("googleSignup", googleSignup);
            return "auth/register-vendor";
        }
        
        if (!googleSignup && !dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.vendorDto", "Passwords do not match");
            model.addAttribute("googleSignup", false);
            return "auth/register-vendor";
        }
        
        try {
            if (googleSignup) {
                User currentUser = getAuthenticatedUser();
                if (currentUser == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                    return "redirect:/login";
                }
                if (hasAnyAssignedRole(currentUser)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Your account is already configured.");
                    return "redirect:" + resolveDashboardByRole(currentUser);
                }

                User updatedUser = userService.assignRoleToExistingUser(
                        currentUser.getId(), "ROLE_VENDOR", dto.getFirstName(), dto.getLastName());

                userService.saveRegistrationHints(
                        updatedUser.getId(),
                        dto.getCompanyName(),
                        dto.getBusinessType(),
                        dto.getGstNumber(),
                        dto.getPhoneNumber(),
                        dto.getCompanyAddress());

                refreshAuthentication(updatedUser, request);
                request.getSession(true).removeAttribute(GoogleOAuth2SuccessHandler.PENDING_ROLE_SESSION_KEY);
                redirectAttributes.addFlashAttribute("successMessage", "Google sign-up completed successfully.");
                return "redirect:/vendor/dashboard";
            }

            UserRegistrationDto userDto = new UserRegistrationDto();
            userDto.setUsername(dto.getUsername());
            userDto.setEmail(dto.getEmail());
            userDto.setPassword(dto.getPassword());
            userDto.setFirstName(dto.getFirstName());
            userDto.setLastName(dto.getLastName());

            User savedUser = userService.registerUserWithRole(userDto, "ROLE_VENDOR");

            // Persist business hints so business-profile/create can be pre-populated
            userService.saveRegistrationHints(
                savedUser.getId(),
                dto.getCompanyName(),
                dto.getBusinessType(),
                dto.getGstNumber(),
                dto.getPhoneNumber(),
                dto.getCompanyAddress()
            );

            // E-02: Welcome email
            try {
                emailService.sendWelcomeEmail(dto.getEmail(),
                        dto.getFirstName() + " " + dto.getLastName(), "Vendor");
            } catch (Exception mailEx) {
                logger.warn("Welcome email failed for vendor {}: {}", dto.getEmail(), mailEx.getMessage());
            }

            logger.info("Vendor registered: {}", dto.getUsername());

            redirectAttributes.addFlashAttribute("successMessage",
                "Registration submitted successfully! Please log in and complete your business profile.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue(googleSignup ? "companyName" : "email", "error.vendorDto", e.getMessage());
            model.addAttribute("googleSignup", googleSignup);
            return "auth/register-vendor";
        }
    }
    
    // Investor Registration
    @GetMapping("/investor")
    public String showInvestorRegistrationForm(
            @RequestParam(name = "googleSignup", defaultValue = "false") boolean googleSignup,
            Model model,
            RedirectAttributes redirectAttributes) {

        InvestorRegistrationDto dto = new InvestorRegistrationDto();
        if (googleSignup) {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                return "redirect:/login";
            }
            if (hasAnyAssignedRole(currentUser)) {
                return "redirect:" + resolveDashboardByRole(currentUser);
            }
            hydrateInvestorDtoFromUser(dto, currentUser);
        }

        model.addAttribute("googleSignup", googleSignup);
        model.addAttribute("investorDto", dto);
        return "auth/register-investor";
    }
    
    @PostMapping("/investor")
    public String registerInvestor(
            @Valid @ModelAttribute("investorDto") InvestorRegistrationDto dto,
            BindingResult result,
            @RequestParam(name = "googleSignup", defaultValue = "false") boolean googleSignup,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (googleSignup) {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                return "redirect:/login";
            }
            if (hasAnyAssignedRole(currentUser)) {
                return "redirect:" + resolveDashboardByRole(currentUser);
            }
            hydrateInvestorDtoFromUser(dto, currentUser);
        }
        
        if (result.hasErrors()) {
            model.addAttribute("googleSignup", googleSignup);
            return "auth/register-investor";
        }
        
        if (!googleSignup && !dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.investorDto", "Passwords do not match");
            model.addAttribute("googleSignup", false);
            return "auth/register-investor";
        }
        
        try {
            if (googleSignup) {
                User currentUser = getAuthenticatedUser();
                if (currentUser == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to continue role setup.");
                    return "redirect:/login";
                }
                if (hasAnyAssignedRole(currentUser)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Your account is already configured.");
                    return "redirect:" + resolveDashboardByRole(currentUser);
                }

                User updatedUser = userService.assignRoleToExistingUser(
                        currentUser.getId(), "ROLE_INVESTOR", dto.getFirstName(), dto.getLastName());

                userService.saveRegistrationHints(
                        updatedUser.getId(),
                        dto.getInvestorName(),
                        dto.getInvestorType(),
                        dto.getPanNumber(),
                        dto.getPhoneNumber(),
                        dto.getAddress());

                refreshAuthentication(updatedUser, request);
                request.getSession(true).removeAttribute(GoogleOAuth2SuccessHandler.PENDING_ROLE_SESSION_KEY);
                redirectAttributes.addFlashAttribute("successMessage", "Google sign-up completed successfully.");
                return "redirect:/investor/dashboard";
            }

            UserRegistrationDto userDto = new UserRegistrationDto();
            userDto.setUsername(dto.getUsername());
            userDto.setEmail(dto.getEmail());
            userDto.setPassword(dto.getPassword());
            userDto.setFirstName(dto.getFirstName());
            userDto.setLastName(dto.getLastName());

            userService.registerUserWithRole(userDto, "ROLE_INVESTOR");

            // E-03: Welcome email
            try {
                emailService.sendWelcomeEmail(dto.getEmail(),
                        dto.getFirstName() + " " + dto.getLastName(), "Investor");
            } catch (Exception mailEx) {
                logger.warn("Welcome email failed for investor {}: {}", dto.getEmail(), mailEx.getMessage());
            }

            logger.info("Investor registered: {}", dto.getUsername());

            redirectAttributes.addFlashAttribute("successMessage",
                "Registration submitted successfully! Please log in and complete your business profile.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue(googleSignup ? "investorName" : "email", "error.investorDto", e.getMessage());
            model.addAttribute("googleSignup", googleSignup);
            return "auth/register-investor";
        }
    }

    private String startGoogleSignupForRole(String roleName, HttpServletRequest request) {
        request.getSession(true).setAttribute(GoogleOAuth2SuccessHandler.PENDING_ROLE_SESSION_KEY, roleName);
        return "redirect:/oauth2/authorization/google";
    }

    private String continueGoogleSignup(String roleName,
                                        String roleFormPath,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();

        if (currentUser == null) {
            request.getSession(true).setAttribute(GoogleOAuth2SuccessHandler.PENDING_ROLE_SESSION_KEY, roleName);
            return "redirect:/oauth2/authorization/google";
        }

        if (hasAnyAssignedRole(currentUser)) {
            return "redirect:" + resolveDashboardByRole(currentUser);
        }

        request.getSession(true).setAttribute(GoogleOAuth2SuccessHandler.PENDING_ROLE_SESSION_KEY, roleName);
        redirectAttributes.addFlashAttribute("successMessage", "Please complete required details to finish registration.");
        return "redirect:" + roleFormPath;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return null;
        }
        return (User) principal;
    }

    private boolean hasAnyAssignedRole(User user) {
        return user != null && user.getRoles() != null && !user.getRoles().isEmpty();
    }

    private void hydrateRetailerDtoFromUser(RetailerRegistrationDto dto, User user) {
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            dto.setFirstName(user.getFirstName());
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            dto.setLastName(user.getLastName());
        }
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(GOOGLE_PLACEHOLDER_PASSWORD);
        dto.setConfirmPassword(GOOGLE_PLACEHOLDER_PASSWORD);
    }

    private void hydrateVendorDtoFromUser(VendorRegistrationDto dto, User user) {
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            dto.setFirstName(user.getFirstName());
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            dto.setLastName(user.getLastName());
        }
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(GOOGLE_PLACEHOLDER_PASSWORD);
        dto.setConfirmPassword(GOOGLE_PLACEHOLDER_PASSWORD);
    }

    private void hydrateInvestorDtoFromUser(InvestorRegistrationDto dto, User user) {
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            dto.setFirstName(user.getFirstName());
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            dto.setLastName(user.getLastName());
        }
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(GOOGLE_PLACEHOLDER_PASSWORD);
        dto.setConfirmPassword(GOOGLE_PLACEHOLDER_PASSWORD);
    }

    private void refreshAuthentication(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
    }

    private String resolveDashboardByRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "/get-started";
        }
        return user.getRoles().stream()
                .map(role -> role.getName())
                .filter(name -> name != null)
                .findFirst()
                .map(roleName -> {
                    switch (roleName) {
                        case "ROLE_PLATFORM_ADMIN": return "/admin/dashboard";
                        case "ROLE_RETAILER": return "/retailer/dashboard";
                        case "ROLE_VENDOR": return "/vendor/dashboard";
                        case "ROLE_INVESTOR": return "/investor/dashboard";
                        default: return "/get-started";
                    }
                })
                .orElse("/get-started");
    }
}

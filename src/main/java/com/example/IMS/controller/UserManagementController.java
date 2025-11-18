package com.example.IMS.controller;

import com.example.IMS.dto.UserRegistrationDto;
import com.example.IMS.model.User;
import com.example.IMS.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserManagementController {

    @Autowired
    private IUserService userService;

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "admin/user-form";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            return "admin/user-form";
        }

        try {
            // Admin can assign specific roles
            String roleName = userDto.getRole() != null ? userDto.getRole() : "ROLE_USER";
            userService.registerUserWithRole(userDto, roleName);
            return "redirect:/admin/users?success=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/user-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findByUsername(String.valueOf(id));
        if (user == null) {
            return "redirect:/admin/users?error=notfound";
        }
        
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        
        model.addAttribute("user", dto);
        model.addAttribute("userId", id);
        return "admin/user-form";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") UserRegistrationDto userDto,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            return "admin/user-form";
        }

        try {
            userService.updateUser(id, userDto);
            return "redirect:/admin/users?updated=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userId", id);
            return "admin/user-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return "redirect:/admin/users?deleted=true";
        } catch (Exception e) {
            return "redirect:/admin/users?error=deletefailed";
        }
    }
}

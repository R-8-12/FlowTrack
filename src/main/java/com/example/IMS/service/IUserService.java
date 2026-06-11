package com.example.IMS.service;

import com.example.IMS.dto.UserRegistrationDto;
import com.example.IMS.dto.RetailerRegistrationDto;
import com.example.IMS.dto.VendorRegistrationDto;
import com.example.IMS.model.User;
import java.util.List;

public interface IUserService {
    User registerUser(UserRegistrationDto registrationDto);
    User registerUserWithRole(UserRegistrationDto registrationDto, String roleName);
    void createRetailerProfile(User user, RetailerRegistrationDto registrationDto);
    void createVendorProfile(User user, VendorRegistrationDto registrationDto);
    User assignRoleToExistingUser(Long userId, String roleName, String firstName, String lastName);
    void saveRegistrationHints(Long userId, String businessName, String businessType,
                               String gstHint, String phone, String address);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> getAllUsers();
    User updateUser(Long id, UserRegistrationDto userDto);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

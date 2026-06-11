package com.example.IMS.service;

import com.example.IMS.dto.RetailerRegistrationDto;
import com.example.IMS.dto.VendorRegistrationDto;
import com.example.IMS.dto.UserRegistrationDto;
import com.example.IMS.model.Role;
import com.example.IMS.model.RetailerProfile;
import com.example.IMS.model.User;
import com.example.IMS.model.VendorProfile;
import com.example.IMS.repository.IRoleRepository;
import com.example.IMS.repository.IUserRepository;
import com.example.IMS.repository.IRetailerProfileRepository;
import com.example.IMS.repository.IVendorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IRetailerProfileRepository retailerProfileRepository;

    @Autowired
    private IVendorProfileRepository vendorProfileRepository;

    /**
     * @deprecated This method references the non-existent ROLE_USER from the legacy IMS system.
     * Use {@link #registerUserWithRole(UserRegistrationDto, String)} instead to explicitly specify a role.
     * FlowTrack uses: ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR.
     */
    @Deprecated
    @Override
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        throw new UnsupportedOperationException("Use registerUserWithRole() instead");
    }
    
    @Override
    @Transactional
    public User registerUserWithRole(UserRegistrationDto registrationDto, String roleName) {
        // This method is for admin use only - to create users with specific roles
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEnabled(true);

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role(roleName);
                    return roleRepository.save(newRole);
                });
        
        user.addRole(role);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void createRetailerProfile(User user, RetailerRegistrationDto registrationDto) {
        if (user == null) {
            throw new RuntimeException("User not found for retailer profile creation");
        }

        RetailerProfile profile = retailerProfileRepository.findByUserId(user.getId())
                .orElseGet(RetailerProfile::new);
        profile.setUser(user);
        profile.setBusinessName(registrationDto.getBusinessName());
        profile.setBusinessType(registrationDto.getBusinessType());
        profile.setTrademark(registrationDto.getTrademark());
        profile.setBusinessRegistrationNumber(registrationDto.getBusinessRegistrationNumber());
        profile.setGstNumber(registrationDto.getGstNumber());
        profile.setBusinessAddress(registrationDto.getBusinessAddress());
        profile.setPhoneNumber(registrationDto.getPhoneNumber());
        profile.setBusinessDescription(registrationDto.getBusinessDescription());
        profile.setProofOfIdentityUrl(registrationDto.getProofOfIdentityUrl());
        profile.setBusinessLicenseUrl(registrationDto.getBusinessLicenseUrl());
        retailerProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void createVendorProfile(User user, VendorRegistrationDto registrationDto) {
        if (user == null) {
            throw new RuntimeException("User not found for vendor profile creation");
        }

        VendorProfile profile = vendorProfileRepository.findByUserId(user.getId())
                .orElseGet(VendorProfile::new);
        profile.setUser(user);
        profile.setCompanyName(registrationDto.getCompanyName());
        profile.setBusinessType(registrationDto.getBusinessType());
        profile.setTradeLicenseNumber(registrationDto.getTradeLicenseNumber());
        profile.setGstNumber(registrationDto.getGstNumber());
        profile.setPanNumber(registrationDto.getPanNumber());
        profile.setCompanyAddress(registrationDto.getCompanyAddress());
        profile.setPhoneNumber(registrationDto.getPhoneNumber());
        profile.setProductCategories(registrationDto.getProductCategories());
        profile.setCompanyDescription(registrationDto.getCompanyDescription());
        profile.setBankAccountNumber(registrationDto.getBankAccountNumber());
        profile.setBankName(registrationDto.getBankName());
        profile.setBankIfscCode(registrationDto.getBankIfscCode());
        profile.setTradeLicenseUrl(registrationDto.getTradeLicenseUrl());
        profile.setGstCertificateUrl(registrationDto.getGstCertificateUrl());
        profile.setCompanyRegistrationUrl(registrationDto.getCompanyRegistrationUrl());
        vendorProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public User assignRoleToExistingUser(Long userId, String roleName, String firstName, String lastName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        if (firstName != null && !firstName.trim().isEmpty()) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName.trim());
        }

        user.addRole(role);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void saveRegistrationHints(Long userId, String businessName, String businessType,
                                       String gstHint, String phone, String address) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRegistrationBusinessName(businessName);
            user.setRegistrationBusinessType(businessType);
            user.setRegistrationGstHint(gstHint);
            user.setRegistrationPhone(phone);
            user.setRegistrationAddress(address);
            userRepository.save(user);
        });
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserRegistrationDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}

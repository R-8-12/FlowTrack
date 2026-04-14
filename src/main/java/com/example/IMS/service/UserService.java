package com.example.IMS.service;

import com.example.IMS.dto.UserRegistrationDto;
import com.example.IMS.model.Role;
import com.example.IMS.model.User;
import com.example.IMS.repository.IRoleRepository;
import com.example.IMS.repository.IUserRepository;
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

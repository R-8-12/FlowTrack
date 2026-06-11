package com.example.IMS.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.IMS.dto.RetailerRegistrationDto;
import com.example.IMS.dto.UserRegistrationDto;
import com.example.IMS.dto.VendorRegistrationDto;
import com.example.IMS.model.RetailerProfile;
import com.example.IMS.model.Role;
import com.example.IMS.model.User;
import com.example.IMS.model.VendorProfile;
import com.example.IMS.repository.IRetailerProfileRepository;
import com.example.IMS.repository.IRoleRepository;
import com.example.IMS.repository.IUserRepository;
import com.example.IMS.repository.IVendorProfileRepository;

/**
 * Unit tests for the registration pipeline.
 *
 * Verifies that registering a retailer or vendor:
 * 1. Creates a row in the users table (via IUserRepository)
 * 2. Assigns the correct role
 * 3. Creates a row in retailer_profiles / vendor_profiles
 * 4. Stores registration hints on the user record
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Registration Pipeline — UserService")
class RegistrationServiceTest {

    @Mock private IUserRepository userRepository;
    @Mock private IRoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IRetailerProfileRepository retailerProfileRepository;
    @Mock private IVendorProfileRepository vendorProfileRepository;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto baseUserDto;
    private RetailerRegistrationDto retailerDto;
    private VendorRegistrationDto vendorDto;

    @BeforeEach
    void setUp() {
        baseUserDto = new UserRegistrationDto();
        baseUserDto.setUsername("testuser");
        baseUserDto.setEmail("test@example.com");
        baseUserDto.setPassword("password123");
        baseUserDto.setFirstName("Test");
        baseUserDto.setLastName("User");

        retailerDto = new RetailerRegistrationDto();
        retailerDto.setUsername("retailer1");
        retailerDto.setEmail("retailer@example.com");
        retailerDto.setPassword("password123");
        retailerDto.setConfirmPassword("password123");
        retailerDto.setFirstName("Raj");
        retailerDto.setLastName("Sharma");
        retailerDto.setBusinessName("Sharma Traders");
        retailerDto.setBusinessType("Retail Store");
        retailerDto.setBusinessAddress("123 Market St, Delhi");
        retailerDto.setPhoneNumber("9876543210");

        vendorDto = new VendorRegistrationDto();
        vendorDto.setUsername("vendor1");
        vendorDto.setEmail("vendor@example.com");
        vendorDto.setPassword("password123");
        vendorDto.setConfirmPassword("password123");
        vendorDto.setFirstName("Priya");
        vendorDto.setLastName("Patel");
        vendorDto.setCompanyName("Patel Wholesale Co.");
        vendorDto.setBusinessType("Wholesaler");
        vendorDto.setCompanyAddress("456 Industrial Area, Mumbai");
        vendorDto.setPhoneNumber("9123456780");
    }

    // ── registerUserWithRole ──────────────────────────────────────────────────

    @Test
    @DisplayName("registerUserWithRole: saves user to users table with encoded password")
    void registerRetailer_savesUserToUsersTable() {
        Role role = new Role("ROLE_RETAILER");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("retailer1");

        when(userRepository.existsByUsername("retailer1")).thenReturn(false);
        when(userRepository.existsByEmail("retailer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pw");
        when(roleRepository.findByName("ROLE_RETAILER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("retailer1");
        dto.setEmail("retailer@example.com");
        dto.setPassword("password123");
        dto.setFirstName("Raj");
        dto.setLastName("Sharma");

        User result = userService.registerUserWithRole(dto, "ROLE_RETAILER");

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u ->
            "retailer1".equals(u.getUsername()) &&
            "hashed_pw".equals(u.getPassword()) &&
            u.getRoles().stream().anyMatch(r -> "ROLE_RETAILER".equals(r.getName()))
        ));
    }

    @Test
    @DisplayName("registerUserWithRole: saves user to users table with ROLE_VENDOR")
    void registerVendor_savesUserToUsersTable() {
        Role role = new Role("ROLE_VENDOR");
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("vendor1");

        when(userRepository.existsByUsername("vendor1")).thenReturn(false);
        when(userRepository.existsByEmail("vendor@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pw");
        when(roleRepository.findByName("ROLE_VENDOR")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("vendor1");
        dto.setEmail("vendor@example.com");
        dto.setPassword("password123");
        dto.setFirstName("Priya");
        dto.setLastName("Patel");

        User result = userService.registerUserWithRole(dto, "ROLE_VENDOR");

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u ->
            u.getRoles().stream().anyMatch(r -> "ROLE_VENDOR".equals(r.getName()))
        ));
    }

    @Test
    @DisplayName("registerUserWithRole: throws if username already exists")
    void registerUser_throwsOnDuplicateUsername() {
        when(userRepository.existsByUsername("retailer1")).thenReturn(true);

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("retailer1");
        dto.setEmail("new@example.com");
        dto.setPassword("password123");

        assertThatThrownBy(() -> userService.registerUserWithRole(dto, "ROLE_RETAILER"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("registerUserWithRole: throws if email already exists")
    void registerUser_throwsOnDuplicateEmail() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("retailer@example.com")).thenReturn(true);

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("retailer@example.com");
        dto.setPassword("password123");

        assertThatThrownBy(() -> userService.registerUserWithRole(dto, "ROLE_RETAILER"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Email already exists");
    }

    // ── createRetailerProfile ─────────────────────────────────────────────────

    @Test
    @DisplayName("createRetailerProfile: saves row to retailer_profiles with correct fields")
    void createRetailerProfile_savesRowToRetailerProfilesTable() {
        User user = new User();
        user.setId(1L);

        when(retailerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(retailerProfileRepository.save(any(RetailerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createRetailerProfile(user, retailerDto);

        verify(retailerProfileRepository).save(argThat(p ->
            "Sharma Traders".equals(p.getBusinessName()) &&
            "Retail Store".equals(p.getBusinessType()) &&
            "9876543210".equals(p.getPhoneNumber()) &&
            p.getUser() == user
        ));
    }

    @Test
    @DisplayName("createRetailerProfile: upserts — updates existing row if one already exists")
    void createRetailerProfile_updatesExistingRow() {
        User user = new User();
        user.setId(1L);

        RetailerProfile existing = new RetailerProfile();
        existing.setUser(user);
        existing.setBusinessName("Old Name");

        when(retailerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(retailerProfileRepository.save(any(RetailerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createRetailerProfile(user, retailerDto);

        verify(retailerProfileRepository).save(argThat(p ->
            "Sharma Traders".equals(p.getBusinessName())
        ));
        // Should not create a new record
        verify(retailerProfileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createRetailerProfile: throws if user is null")
    void createRetailerProfile_throwsIfUserNull() {
        assertThatThrownBy(() -> userService.createRetailerProfile(null, retailerDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");
    }

    // ── createVendorProfile ───────────────────────────────────────────────────

    @Test
    @DisplayName("createVendorProfile: saves row to vendor_profiles with correct fields")
    void createVendorProfile_savesRowToVendorProfilesTable() {
        User user = new User();
        user.setId(2L);

        when(vendorProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(vendorProfileRepository.save(any(VendorProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createVendorProfile(user, vendorDto);

        verify(vendorProfileRepository).save(argThat(p ->
            "Patel Wholesale Co.".equals(p.getCompanyName()) &&
            "Wholesaler".equals(p.getBusinessType()) &&
            "9123456780".equals(p.getPhoneNumber()) &&
            p.getUser() == user
        ));
    }

    @Test
    @DisplayName("createVendorProfile: upserts — updates existing row if one already exists")
    void createVendorProfile_updatesExistingRow() {
        User user = new User();
        user.setId(2L);

        VendorProfile existing = new VendorProfile();
        existing.setUser(user);
        existing.setCompanyName("Old Company");

        when(vendorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(existing));
        when(vendorProfileRepository.save(any(VendorProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createVendorProfile(user, vendorDto);

        verify(vendorProfileRepository).save(argThat(p ->
            "Patel Wholesale Co.".equals(p.getCompanyName())
        ));
        verify(vendorProfileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createVendorProfile: throws if user is null")
    void createVendorProfile_throwsIfUserNull() {
        assertThatThrownBy(() -> userService.createVendorProfile(null, vendorDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");
    }

    // ── saveRegistrationHints ─────────────────────────────────────────────────

    @Test
    @DisplayName("saveRegistrationHints: persists hint fields on user record")
    void saveRegistrationHints_persistsHintsOnUser() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.saveRegistrationHints(1L, "Sharma Traders", "Retail Store",
                "29ABCDE1234F1Z5", "9876543210", "123 Market St, Delhi");

        verify(userRepository).save(argThat(u ->
            "Sharma Traders".equals(u.getRegistrationBusinessName()) &&
            "Retail Store".equals(u.getRegistrationBusinessType()) &&
            "29ABCDE1234F1Z5".equals(u.getRegistrationGstHint())
        ));
    }
}

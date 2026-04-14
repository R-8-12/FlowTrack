# Role System Cleanup Bugfix Design

## Overview

FlowTrack evolved from an IMS (Inventory Management System) to a SaaS platform with four defined roles: ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, and ROLE_INVESTOR. However, legacy code in UserManagementController and UserService still references obsolete roles (ROLE_ADMIN, ROLE_USER) that were never migrated. This causes two critical bugs:

1. UserManagementController is completely inaccessible - @PreAuthorize checks for non-existent 'ROLE_ADMIN', causing all /admin/users endpoints to return 403 Forbidden
2. UserService.registerUser() throws "Default role ROLE_USER not found" exception because ROLE_USER is never created in DataInitializer

The fix updates these two files to use the correct FlowTrack role system while preserving all existing functionality in RegistrationController, DataInitializer, and SecurityConfig.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when legacy role names (ROLE_ADMIN, ROLE_USER) are referenced in code but don't exist in the database
- **Property (P)**: The desired behavior - code should reference only the 4 valid FlowTrack roles (ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR)
- **Preservation**: Existing registration flows, role creation in DataInitializer, and SecurityConfig authorization rules that must remain unchanged
- **UserManagementController**: The controller in `src/main/java/com/example/IMS/controller/UserManagementController.java` that provides admin user management UI
- **UserService**: The service in `src/main/java/com/example/IMS/service/UserService.java` that handles user registration and role assignment
- **DataInitializer**: The component in `src/main/java/com/example/IMS/config/DataInitializer.java` that creates the 4 FlowTrack roles on startup
- **RegistrationController**: The controller that handles user registration flows (/register/retailer, /register/vendor, /register/investor) - works correctly, must not be changed

## Bug Details

### Bug Condition

The bug manifests when code references legacy role names that don't exist in the database. UserManagementController uses @PreAuthorize("hasAuthority('ROLE_ADMIN')") but ROLE_ADMIN is never created in DataInitializer, causing Spring Security to deny all access. UserService.registerUser() attempts to find "ROLE_USER" which also doesn't exist, causing a RuntimeException.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type CodeReference
  OUTPUT: boolean
  
  RETURN input.referencedRole IN ['ROLE_ADMIN', 'ROLE_USER', 'ROLE_MANAGER', 'ROLE_STAFF']
         AND input.referencedRole NOT IN databaseRoles
         AND (input.isSecurityAnnotation OR input.isRoleQuery)
END FUNCTION
```

### Examples

- **UserManagementController Access**: ROLE_PLATFORM_ADMIN user navigates to /admin/users → Expected: Access granted | Actual: 403 Forbidden (checks for non-existent ROLE_ADMIN)
- **UserService.registerUser() Call**: Code calls userService.registerUser(dto) → Expected: User created with appropriate role | Actual: RuntimeException "Default role ROLE_USER not found"
- **UserManagementController.addUser() with no role**: Admin creates user without specifying role → Expected: Require explicit role selection | Actual: Defaults to non-existent "ROLE_USER", causing registration to fail
- **Edge Case - Direct Role Query**: Code queries roleRepository.findByName("ROLE_ADMIN") → Expected: Returns empty Optional | Actual: Returns empty Optional (correct behavior, but indicates missing role)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- RegistrationController flows (/register/retailer, /register/vendor, /register/investor) must continue to assign correct roles (ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR)
- DataInitializer must continue to create only the 4 FlowTrack roles (ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR)
- SecurityConfig authorization rules must continue to use correct FlowTrack role names for all other endpoints (retailer/**, vendor/**, investor/**, platform/**)
- Platform admin account creation in DataInitializer must continue to assign ROLE_PLATFORM_ADMIN correctly

**Scope:**
All code that does NOT involve UserManagementController or UserService.registerUser() should be completely unaffected by this fix. This includes:
- All registration flows in RegistrationController
- All role creation logic in DataInitializer
- All authorization rules in SecurityConfig (except /admin/users path)
- All other service methods in UserService (registerUserWithRole, assignRoleToExistingUser, etc.)

## Hypothesized Root Cause

Based on the bug description and code analysis, the root causes are:

1. **Incomplete Migration**: When FlowTrack evolved from IMS to SaaS, DataInitializer was updated to create the 4 new roles, but UserManagementController and UserService.registerUser() were not updated to reference the new role names

2. **Hardcoded Legacy Role References**: UserManagementController has @PreAuthorize("hasAuthority('ROLE_ADMIN')") hardcoded at the class level, and UserService.registerUser() has "ROLE_USER" hardcoded in the role lookup

3. **Dead Code Path**: UserService.registerUser() appears to be unused (RegistrationController uses registerUserWithRole instead), but it's still callable and will throw exceptions if invoked

4. **Default Role Assumption**: UserManagementController.addUser() defaults to "ROLE_USER" when no role is specified, assuming this role exists in the database

## Correctness Properties

Property 1: Bug Condition - Access Control with Correct Roles

_For any_ HTTP request to /admin/users endpoints where the authenticated user has ROLE_PLATFORM_ADMIN authority, the fixed UserManagementController SHALL grant access by checking for 'ROLE_PLATFORM_ADMIN' instead of non-existent 'ROLE_ADMIN', allowing the user to manage users through the admin panel.

**Validates: Requirements 2.1**

Property 2: Bug Condition - User Registration without Default Role

_For any_ call to UserService.registerUser() method, the fixed implementation SHALL NOT attempt to auto-assign a default role, and instead SHALL either require explicit role specification or be deprecated in favor of registerUserWithRole(), preventing RuntimeException "Default role ROLE_USER not found".

**Validates: Requirements 2.2**

Property 3: Bug Condition - Explicit Role Selection in Admin Panel

_For any_ user creation through UserManagementController.addUser() where no role is specified, the fixed implementation SHALL require explicit role selection from the 4 valid FlowTrack roles (ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR) instead of defaulting to non-existent "ROLE_USER".

**Validates: Requirements 2.3**

Property 4: Preservation - Registration Controller Flows

_For any_ user registration through RegistrationController endpoints (/register/retailer, /register/vendor, /register/investor), the fixed code SHALL produce exactly the same behavior as the original code, continuing to assign the correct role (ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR) as currently implemented.

**Validates: Requirements 3.1**

Property 5: Preservation - DataInitializer Role Creation

_For any_ application startup where DataInitializer runs, the fixed code SHALL produce exactly the same behavior as the original code, continuing to create only the 4 FlowTrack roles (ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR) and no legacy roles.

**Validates: Requirements 3.2**

Property 6: Preservation - SecurityConfig Authorization Rules

_For any_ HTTP request to endpoints other than /admin/users, the fixed code SHALL produce exactly the same authorization behavior as the original code, continuing to use the correct FlowTrack role names for all other endpoints (retailer/**, vendor/**, investor/**, platform/**).

**Validates: Requirements 3.3**

Property 7: Preservation - Platform Admin Account Creation

_For any_ application startup where DataInitializer creates or refreshes the platform admin account, the fixed code SHALL produce exactly the same behavior as the original code, continuing to assign ROLE_PLATFORM_ADMIN correctly.

**Validates: Requirements 3.4**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `src/main/java/com/example/IMS/controller/UserManagementController.java`

**Function**: Class-level @PreAuthorize annotation and addUser() method

**Specific Changes**:
1. **Update @PreAuthorize Annotation**: Change from `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` to `@PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")`
   - This fixes the 403 Forbidden issue by checking for the correct role that actually exists in the database

2. **Update Default Role in addUser()**: Change from `String roleName = userDto.getRole() != null ? userDto.getRole() : "ROLE_USER";` to require explicit role selection
   - Option A: Throw exception if role is null: `if (userDto.getRole() == null) throw new RuntimeException("Role must be specified");`
   - Option B: Default to ROLE_RETAILER (most common user type): `String roleName = userDto.getRole() != null ? userDto.getRole() : "ROLE_RETAILER";`
   - Recommendation: Option A (explicit role selection) for clarity and security

**File**: `src/main/java/com/example/IMS/service/UserService.java`

**Function**: registerUser() method

**Specific Changes**:
3. **Deprecate or Remove registerUser()**: Since RegistrationController uses registerUserWithRole() and registerUser() references non-existent ROLE_USER
   - Option A: Mark as @Deprecated and throw UnsupportedOperationException with message directing to use registerUserWithRole()
   - Option B: Remove the method entirely if no code paths use it
   - Recommendation: Option A (deprecate) to avoid breaking any unknown callers

4. **Alternative - Update to Use Valid Role**: If registerUser() must remain functional, change from `roleRepository.findByName("ROLE_USER")` to `roleRepository.findByName("ROLE_RETAILER")` (most common user type)
   - Update comment from "SECURITY: Self-registration always gets ROLE_USER" to "SECURITY: Self-registration always gets ROLE_RETAILER"
   - This maintains backward compatibility while fixing the bug

5. **Update Method Documentation**: Add clear documentation explaining which method to use for different scenarios:
   - registerUserWithRole() - for admin-initiated user creation with specific role
   - registerUser() - deprecated or for self-registration with default ROLE_RETAILER

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write integration tests that simulate ROLE_PLATFORM_ADMIN user accessing /admin/users endpoints and unit tests that call UserService.registerUser(). Run these tests on the UNFIXED code to observe failures and understand the root cause.

**Test Cases**:
1. **UserManagementController Access Test**: Authenticate as ROLE_PLATFORM_ADMIN user and attempt to access /admin/users → will fail with 403 Forbidden on unfixed code
2. **UserService.registerUser() Test**: Call userService.registerUser(validDto) → will fail with RuntimeException "Default role ROLE_USER not found" on unfixed code
3. **UserManagementController.addUser() with null role**: Call addUser with userDto.getRole() == null → will fail with RuntimeException on unfixed code
4. **Edge Case - Multiple Role Check**: Verify that user with both ROLE_PLATFORM_ADMIN and another role can access /admin/users → may fail on unfixed code depending on Spring Security evaluation order

**Expected Counterexamples**:
- HTTP 403 Forbidden responses when ROLE_PLATFORM_ADMIN users access /admin/users
- RuntimeException with message "Default role ROLE_USER not found" when registerUser() is called
- Possible causes: hardcoded legacy role names, incomplete migration from IMS to FlowTrack role system

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL request WHERE request.path.startsWith("/admin/users") 
                  AND request.user.hasAuthority("ROLE_PLATFORM_ADMIN") DO
  response := UserManagementController_fixed.handleRequest(request)
  ASSERT response.status == 200 OR response.status == 302 (redirect)
  ASSERT response.status != 403
END FOR

FOR ALL userDto WHERE userDto.isValid() DO
  IF registerUser_fixed is deprecated THEN
    ASSERT throws UnsupportedOperationException
  ELSE
    result := UserService_fixed.registerUser(userDto)
    ASSERT result.user.roles.contains(validFlowTrackRole)
    ASSERT NOT throws RuntimeException("Default role ROLE_USER not found")
  END IF
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL request WHERE request.path NOT IN ["/admin/users/**"] DO
  ASSERT SecurityConfig_original.authorize(request) == SecurityConfig_fixed.authorize(request)
END FOR

FOR ALL registrationRequest WHERE registrationRequest.path IN ["/register/retailer", "/register/vendor", "/register/investor"] DO
  originalUser := RegistrationController_original.register(registrationRequest)
  fixedUser := RegistrationController_fixed.register(registrationRequest)
  ASSERT originalUser.roles == fixedUser.roles
  ASSERT originalUser.username == fixedUser.username
END FOR

FOR ALL startup WHERE DataInitializer runs DO
  originalRoles := DataInitializer_original.createRoles()
  fixedRoles := DataInitializer_fixed.createRoles()
  ASSERT originalRoles == fixedRoles
  ASSERT originalRoles == ["ROLE_PLATFORM_ADMIN", "ROLE_RETAILER", "ROLE_VENDOR", "ROLE_INVESTOR"]
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for registration flows, DataInitializer, and SecurityConfig, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Registration Flow Preservation**: Observe that /register/retailer correctly assigns ROLE_RETAILER on unfixed code, then write test to verify this continues after fix
2. **DataInitializer Preservation**: Observe that DataInitializer creates exactly 4 roles on unfixed code, then write test to verify this continues after fix
3. **SecurityConfig Preservation**: Observe that /retailer/**, /vendor/**, /investor/** endpoints authorize correctly on unfixed code, then write test to verify this continues after fix
4. **Platform Admin Creation Preservation**: Observe that platform admin account gets ROLE_PLATFORM_ADMIN on unfixed code, then write test to verify this continues after fix

### Unit Tests

- Test UserManagementController with ROLE_PLATFORM_ADMIN user accessing each endpoint (/admin/users, /admin/users/add, /admin/users/edit/{id}, /admin/users/delete/{id})
- Test UserService.registerUser() behavior (either throws UnsupportedOperationException if deprecated, or creates user with valid role)
- Test UserManagementController.addUser() with null role (should require explicit role selection)
- Test that registerUserWithRole() continues to work with all 4 FlowTrack roles

### Property-Based Tests

- Generate random UserRegistrationDto objects and verify registerUserWithRole() works correctly with all 4 FlowTrack roles
- Generate random HTTP requests to /admin/users endpoints with ROLE_PLATFORM_ADMIN user and verify access is granted
- Generate random registration requests to /register/* endpoints and verify correct role assignment is preserved
- Test that DataInitializer creates exactly 4 roles across multiple application restarts

### Integration Tests

- Test full user management flow: ROLE_PLATFORM_ADMIN logs in → navigates to /admin/users → creates new user with specific role → verifies user is created with correct role
- Test registration flow: User registers via /register/retailer → verifies ROLE_RETAILER is assigned → logs in → verifies access to /retailer/** endpoints
- Test that platform admin account works correctly after application restart (DataInitializer refresh scenario)
- Test that SecurityConfig authorization rules work correctly for all 4 role types across their respective endpoints

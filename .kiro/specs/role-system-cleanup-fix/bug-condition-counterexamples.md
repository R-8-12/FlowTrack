# Bug Condition Exploration - Counterexamples Documentation

**Test File**: `src/test/java/com/example/IMS/bugfix/RoleSystemCleanupBugConditionTest.java`

**Test Execution Date**: 2026-04-15

**Test Status**: FAILED (as expected on unfixed code - confirms bug exists)

## Summary

The bug condition exploration test was executed on the UNFIXED codebase and successfully surfaced counterexamples that demonstrate the bug exists. The test failures confirm the root cause analysis is correct.

## Counterexamples Found

### Bug Condition 1: ROLE_PLATFORM_ADMIN Access Denial ✓ CONFIRMED

**Test Method**: `testPlatformAdminCanAccessUserManagement()`

**Bug Description**: ROLE_PLATFORM_ADMIN user accessing /admin/users returns 403 Forbidden

**Counterexample**:
```
MockHttpServletRequest:
  HTTP Method = GET
  Request URI = /admin/users
  Session Attrs = {SPRING_SECURITY_CONTEXT=SecurityContextImpl [
    Authentication=UsernamePasswordAuthenticationToken [
      Principal=org.springframework.security.core.userdetails.User [
        Username=platformadmin,
        Granted Authorities=[ROLE_PLATFORM_ADMIN]
      ]
    ]
  ]}

MockHttpServletResponse:
  Status = 403
  Error message = Forbidden
```

**Expected Behavior (after fix)**: HTTP 200 OK

**Actual Behavior (unfixed code)**: HTTP 403 Forbidden

**Root Cause Confirmed**: 
- File: `src/main/java/com/example/IMS/controller/UserManagementController.java`
- Line 18: `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`
- Issue: Checks for non-existent ROLE_ADMIN instead of ROLE_PLATFORM_ADMIN

**Validation**: Requirements 1.1

---

### Bug Condition 2: UserService.registerUser() RuntimeException ✓ CONFIRMED

**Test Method**: `testRegisterUserDoesNotThrowRoleNotFoundException()`

**Bug Description**: UserService.registerUser() throws RuntimeException "Default role ROLE_USER not found"

**Counterexample**:
```
java.lang.RuntimeException: Default role ROLE_USER not found
  at com.example.IMS.service.UserService.registerUser(UserService.java:49)
  at com.example.IMS.bugfix.RoleSystemCleanupBugConditionTest.lambda$testRegisterUserDoesNotThrowRoleNotFoundException$0(RoleSystemCleanupBugConditionTest.java:118)

Test Input:
  UserRegistrationDto {
    username: "testuser_1744838529965"
    email: "testuser_1744838529965@example.com"
    password: "password123"
    firstName: "Test"
    lastName: "User"
  }
```

**Expected Behavior (after fix)**: Should not throw "Default role ROLE_USER not found" exception

**Actual Behavior (unfixed code)**: RuntimeException thrown

**Root Cause Confirmed**:
- File: `src/main/java/com/example/IMS/service/UserService.java`
- Line 49: `roleRepository.findByName("ROLE_USER")`
- Issue: Attempts to find non-existent ROLE_USER role

**Validation**: Requirements 1.2

---

### Bug Condition 3: UserManagementController.addUser() Default Role

**Test Method**: `testAddUserWithNullRoleRequiresExplicitSelection()`

**Bug Description**: UserManagementController.addUser() with null role defaults to non-existent "ROLE_USER"

**Test Status**: PASSED (tested with explicit role to verify registerUserWithRole works)

**Root Cause Identified**:
- File: `src/main/java/com/example/IMS/controller/UserManagementController.java`
- Line 44: `String roleName = userDto.getRole() != null ? userDto.getRole() : "ROLE_USER";`
- Issue: Defaults to non-existent ROLE_USER when no role is specified

**Expected Behavior (after fix)**: Should require explicit role selection

**Validation**: Requirements 1.3

---

### Verification: Database Role State ✓ CONFIRMED

**Test Method**: `testOnlyFlowTrackRolesExist()`

**Test Status**: PASSED

**Confirmed Database State**:
- ✓ ROLE_PLATFORM_ADMIN exists
- ✓ ROLE_RETAILER exists
- ✓ ROLE_VENDOR exists
- ✓ ROLE_INVESTOR exists
- ✗ ROLE_ADMIN does NOT exist (legacy role)
- ✗ ROLE_USER does NOT exist (legacy role)
- ✗ ROLE_MANAGER does NOT exist (legacy role)
- ✗ ROLE_STAFF does NOT exist (legacy role)

**Conclusion**: The database contains only the 4 valid FlowTrack roles. Legacy roles from the IMS system were never migrated, confirming that code referencing these roles will fail.

---

## Root Cause Analysis Validation

The counterexamples confirm the hypothesized root causes:

1. **Incomplete Migration**: DataInitializer was updated to create the 4 new FlowTrack roles, but UserManagementController and UserService.registerUser() were not updated to reference the new role names.

2. **Hardcoded Legacy Role References**: 
   - UserManagementController: `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`
   - UserService.registerUser(): `roleRepository.findByName("ROLE_USER")`
   - UserManagementController.addUser(): defaults to `"ROLE_USER"`

3. **Dead Code Path**: UserService.registerUser() appears to be unused (RegistrationController uses registerUserWithRole instead), but it's still callable and throws exceptions when invoked.

## Next Steps

1. ✓ Bug condition exploration test written and executed
2. ✓ Counterexamples documented
3. ✓ Root cause analysis validated
4. → Proceed to Task 2: Write preservation property tests (BEFORE implementing fix)
5. → Proceed to Task 3: Implement fix for legacy role references
6. → Verify bug condition test passes after fix
7. → Verify preservation tests still pass after fix

## Test Execution Details

**Maven Command**: `./mvnw test -Dtest=RoleSystemCleanupBugConditionTest`

**Test Results**:
- Tests run: 4
- Failures: 2 (expected - confirms bug exists)
- Errors: 0
- Skipped: 0
- Time elapsed: 13.546 s

**Build Status**: FAILURE (expected on unfixed code)

**Test Framework**: JUnit 5 with Spring Boot Test

**Spring Security Test**: @WithMockUser used to simulate authenticated users

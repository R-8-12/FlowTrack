# Bugfix Requirements Document

## Introduction

FlowTrack has evolved from an IMS (Inventory Management System) to a SaaS platform with a defined role system: ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, and ROLE_INVESTOR. However, legacy code still references obsolete roles (ROLE_ADMIN, ROLE_USER, ROLE_MANAGER, ROLE_STAFF) that were never migrated from the original IMS system. This causes two critical bugs:

1. UserManagementController is completely broken - uses @PreAuthorize("hasAuthority('ROLE_ADMIN')") but ROLE_ADMIN doesn't exist, causing all /admin/users endpoints to return 403 Forbidden
2. UserService.registerUser() will throw "Default role ROLE_USER not found" exception because ROLE_USER is never created in DataInitializer

The registration flow (RegistrationController) works correctly by assigning roles during registration, but the legacy UserService.registerUser() method and UserManagementController are broken due to hardcoded references to non-existent roles.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a ROLE_PLATFORM_ADMIN user attempts to access /admin/users endpoints THEN the system returns 403 Forbidden because @PreAuthorize checks for non-existent 'ROLE_ADMIN'

1.2 WHEN UserService.registerUser() is called THEN the system throws RuntimeException "Default role ROLE_USER not found" because ROLE_USER is never created in DataInitializer

1.3 WHEN UserManagementController.addUser() is called with no role specified THEN the system defaults to "ROLE_USER" which doesn't exist, causing registration to fail

### Expected Behavior (Correct)

2.1 WHEN a ROLE_PLATFORM_ADMIN user attempts to access /admin/users endpoints THEN the system SHALL grant access by checking for 'ROLE_PLATFORM_ADMIN' authority

2.2 WHEN UserService.registerUser() is called THEN the system SHALL NOT auto-assign any role (users choose roles during registration via RegistrationController)

2.3 WHEN UserManagementController.addUser() is called with no role specified THEN the system SHALL require explicit role selection from the 4 valid FlowTrack roles

### Unchanged Behavior (Regression Prevention)

3.1 WHEN users register via RegistrationController (/register/retailer, /register/vendor, /register/investor) THEN the system SHALL CONTINUE TO assign the correct role (ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR) as currently implemented

3.2 WHEN DataInitializer runs THEN the system SHALL CONTINUE TO create only the 4 FlowTrack roles (ROLE_PLATFORM_ADMIN, ROLE_RETAILER, ROLE_VENDOR, ROLE_INVESTOR) and no legacy roles

3.3 WHEN SecurityConfig evaluates authorization rules THEN the system SHALL CONTINUE TO use the correct FlowTrack role names for all other endpoints (retailer/**, vendor/**, investor/**, platform/**)

3.4 WHEN the platform admin account is created/refreshed in DataInitializer THEN the system SHALL CONTINUE TO assign ROLE_PLATFORM_ADMIN correctly

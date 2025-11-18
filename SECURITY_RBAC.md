# Role-Based Access Control (RBAC) Documentation 🔐

## Overview
The Inventory Management System implements a comprehensive RBAC system with 4 roles and multiple layers of security.

---

## 🎭 Roles & Permissions

### 1. **ROLE_USER** (Default for self-registration)
**Permissions:**
- ✅ View inventory items
- ✅ Issue items
- ✅ Return items
- ✅ View fines
- ✅ Use chatbot
- ❌ Cannot add/edit/delete items
- ❌ Cannot manage vendors
- ❌ Cannot access admin panel

### 2. **ROLE_STAFF**
**Permissions:**
- ✅ All USER permissions
- ✅ Process item issuance
- ✅ Process item returns
- ✅ Calculate fines
- ❌ Cannot add/edit/delete items
- ❌ Cannot manage vendors
- ❌ Cannot access admin panel

### 3. **ROLE_MANAGER**
**Permissions:**
- ✅ All STAFF permissions
- ✅ Add/Edit/Delete inventory items
- ✅ Manage vendors
- ✅ View all reports
- ❌ Cannot access admin panel
- ❌ Cannot manage users

### 4. **ROLE_ADMIN** (Highest privilege)
**Permissions:**
- ✅ All MANAGER permissions
- ✅ Full user management (create, edit, delete users)
- ✅ Assign roles to users
- ✅ Access admin panel
- ✅ System configuration

---

## 🔒 Security Implementation

### 1. **URL-Based Access Control**
Configured in `SecurityConfig.java`:

```java
.antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
.antMatchers("/ItemCreate", "/ItemEdit/**", "/ItemDelete/**")
    .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
.antMatchers("/vendors/**")
    .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
.anyRequest().authenticated()
```

### 2. **Method-Level Security**
Using `@PreAuthorize` annotation:

```java
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserManagementController {
    // Only ADMIN can access these methods
}
```

### 3. **Self-Registration Security**
**CRITICAL SECURITY FEATURE:**

When users register via `/register`:
- ✅ **Automatically assigned ROLE_USER** (hardcoded)
- ❌ **Cannot choose their own role**
- ❌ **Cannot make themselves ADMIN**

```java
// In UserService.registerUser()
Role role = roleRepository.findByName("ROLE_USER")
    .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
```

### 4. **Admin-Created Users**
Only admins can create users with specific roles via `/admin/users/add`:

```java
// In UserManagementController
userService.registerUserWithRole(userDto, roleName);
```

---

## 🛡️ Access Control Matrix

| Feature | USER | STAFF | MANAGER | ADMIN |
|---------|------|-------|---------|-------|
| **Authentication** |
| Login/Logout | ✅ | ✅ | ✅ | ✅ |
| Self-Register | ✅ | ✅ | ✅ | ✅ |
| **Inventory** |
| View Items | ✅ | ✅ | ✅ | ✅ |
| Add Items | ❌ | ❌ | ✅ | ✅ |
| Edit Items | ❌ | ❌ | ✅ | ✅ |
| Delete Items | ❌ | ❌ | ✅ | ✅ |
| **Vendors** |
| View Vendors | ❌ | ❌ | ✅ | ✅ |
| Add Vendors | ❌ | ❌ | ✅ | ✅ |
| Delete Vendors | ❌ | ❌ | ✅ | ✅ |
| **Item Operations** |
| Issue Items | ✅ | ✅ | ✅ | ✅ |
| Return Items | ✅ | ✅ | ✅ | ✅ |
| Calculate Fines | ✅ | ✅ | ✅ | ✅ |
| Item Repair | ✅ | ✅ | ✅ | ✅ |
| **User Management** |
| View Users | ❌ | ❌ | ❌ | ✅ |
| Add Users | ❌ | ❌ | ❌ | ✅ |
| Edit Users | ❌ | ❌ | ❌ | ✅ |
| Delete Users | ❌ | ❌ | ❌ | ✅ |
| Assign Roles | ❌ | ❌ | ❌ | ✅ |
| **Chatbot** |
| Use Chatbot | ✅ | ✅ | ✅ | ✅ |

---

## 🚨 Security Features

### 1. **Password Security**
- ✅ BCrypt encryption (10 rounds)
- ✅ Passwords never stored in plain text
- ✅ Minimum 6 characters required
- ✅ Password confirmation on registration

### 2. **Session Management**
- ✅ Session-based authentication
- ✅ Remember-me functionality
- ✅ Automatic logout on session expiry
- ✅ Secure logout endpoint

### 3. **Input Validation**
- ✅ Username uniqueness check
- ✅ Email uniqueness check
- ✅ Email format validation
- ✅ Required field validation

### 4. **Authorization Checks**
- ✅ URL-level authorization
- ✅ Method-level authorization
- ✅ Role-based access control
- ✅ 403 Forbidden for unauthorized access

---

## 📋 User Registration Flow

### Public Registration (`/register`)
```
User fills form
    ↓
Submit registration
    ↓
UserService.registerUser()
    ↓
FORCED: ROLE_USER assigned
    ↓
Password encrypted (BCrypt)
    ↓
User saved to database
    ↓
Redirect to login
```

### Admin Creating User (`/admin/users/add`)
```
Admin logs in
    ↓
Access admin panel
    ↓
Fill user form + SELECT ROLE
    ↓
UserService.registerUserWithRole(dto, roleName)
    ↓
Specified role assigned
    ↓
Password encrypted (BCrypt)
    ↓
User saved to database
    ↓
Redirect to user list
```

---

## 🔑 Default Credentials

**System Administrator:**
- Username: `admin`
- Password: `admin123`
- Role: `ROLE_ADMIN`

**⚠️ IMPORTANT:** Change the default admin password after first login!

---

## 🛠️ How to Test RBAC

### Test 1: Self-Registration Security
1. Go to `/register`
2. Register as "testuser"
3. Login with testuser
4. Try to access `/admin/users` → Should get **403 Forbidden**
5. Try to access `/ItemCreate` → Should get **403 Forbidden**
6. ✅ Can access `/ItemView` → Should work

### Test 2: Admin Creating Manager
1. Login as admin
2. Go to `/admin/users/add`
3. Create user with ROLE_MANAGER
4. Logout and login as new manager
5. Try to access `/ItemCreate` → Should work
6. Try to access `/admin/users` → Should get **403 Forbidden**

### Test 3: Role Escalation Prevention
1. Register as regular user
2. Check database: `SELECT * FROM user_roles WHERE user_id = X`
3. Should only have `role_id` for ROLE_USER
4. ✅ Cannot escalate to ADMIN via registration

---

## 🔐 Security Best Practices Implemented

1. ✅ **Principle of Least Privilege** - Users get minimum required permissions
2. ✅ **Defense in Depth** - Multiple security layers (URL + Method + Service)
3. ✅ **Secure by Default** - Self-registration gets lowest privilege (USER)
4. ✅ **Password Hashing** - BCrypt with salt
5. ✅ **Input Validation** - Server-side validation
6. ✅ **Session Security** - Secure session management
7. ✅ **Authorization Checks** - Every endpoint protected

---

## 📝 Security Configuration Files

1. **SecurityConfig.java** - Main security configuration
2. **UserDetailsServiceImpl.java** - User authentication
3. **UserService.java** - User registration logic
4. **AuthController.java** - Login/Register endpoints
5. **UserManagementController.java** - Admin user management

---

## ⚠️ Security Warnings

### DO NOT:
- ❌ Allow users to choose their own roles during registration
- ❌ Store passwords in plain text
- ❌ Disable CSRF protection in production
- ❌ Use default admin password in production
- ❌ Expose admin endpoints without authentication

### DO:
- ✅ Change default admin password immediately
- ✅ Use HTTPS in production
- ✅ Enable CSRF protection in production
- ✅ Regularly audit user roles
- ✅ Monitor failed login attempts

---

## 🎯 Summary

**Your RBAC implementation is SECURE because:**

1. ✅ Self-registration **always** assigns ROLE_USER (hardcoded)
2. ✅ Only admins can assign other roles
3. ✅ URL-based access control prevents unauthorized access
4. ✅ Method-level security adds extra protection
5. ✅ Passwords are encrypted with BCrypt
6. ✅ Session management is secure
7. ✅ Input validation prevents injection attacks

**Users CANNOT escalate their privileges through self-registration!** 🔒

# Testing Checklist ✅

## Pre-Testing Setup
- [ ] MySQL database "IMS" created
- [ ] MySQL service is running
- [ ] Gemini API key is set (environment variable or application.properties)
- [ ] Maven dependencies downloaded (`mvn clean install`)

---

## 1. Application Startup Tests

### Start the Application
```bash
mvn spring-boot:run
```

### Expected Console Output:
- [ ] ✅ Role created: ROLE_ADMIN
- [ ] ✅ Role created: ROLE_MANAGER
- [ ] ✅ Role created: ROLE_STAFF
- [ ] ✅ Role created: ROLE_USER
- [ ] ✅ Default admin user created - Username: admin, Password: admin123
- [ ] Application started on port 8086
- [ ] No error messages in console

---

## 2. Authentication Tests

### Test Login Page
- [ ] Navigate to http://localhost:8086
- [ ] Should redirect to http://localhost:8086/login
- [ ] Login page displays correctly
- [ ] No console errors in browser

### Test Admin Login
- [ ] Username: `admin`
- [ ] Password: `admin123`
- [ ] Click "Login"
- [ ] Should redirect to homepage (/)
- [ ] No error messages

### Test Invalid Login
- [ ] Try wrong username/password
- [ ] Should show error: "Invalid username or password"
- [ ] Should stay on login page

### Test Registration
- [ ] Click "Register here" link
- [ ] Fill in registration form:
  - Username: `testuser`
  - Email: `test@example.com`
  - Password: `test123`
  - Confirm Password: `test123`
- [ ] Click "Register"
- [ ] Should redirect to login with success message
- [ ] Login with new credentials
- [ ] Should work successfully

### Test Logout
- [ ] Click user icon in navbar
- [ ] Click "Logout"
- [ ] Should redirect to login page
- [ ] Should show logout success message

---

## 3. Role-Based Access Control Tests

### Test Admin Access
Login as admin (admin/admin123):
- [ ] Can access homepage (/)
- [ ] Can access User Management (/admin/users)
- [ ] Can access Item Management (/ItemView)
- [ ] Can access Item Create (/ItemCreate)
- [ ] Can access Vendors (/vendors)

### Test Regular User Access
Login as testuser (test@example.com/test123):
- [ ] Can access homepage (/)
- [ ] CANNOT access User Management (/admin/users) - should get 403 error
- [ ] Can view items (/ItemView)
- [ ] CANNOT create items (/ItemCreate) - should get 403 error

---

## 4. User Management Tests (Admin Only)

Login as admin:

### View Users
- [ ] Navigate to User Management
- [ ] Should see list of users
- [ ] Should see admin and testuser
- [ ] Should see roles displayed as badges

### Add New User
- [ ] Click "Add New User"
- [ ] Fill in form:
  - Username: `manager1`
  - Email: `manager@ims.com`
  - Password: `manager123`
  - Confirm Password: `manager123`
  - Role: ROLE_MANAGER
- [ ] Click "Save User"
- [ ] Should redirect to user list
- [ ] Should show success message
- [ ] New user should appear in list

### Edit User
- [ ] Click edit button on manager1
- [ ] Change first name to "John"
- [ ] Change last name to "Manager"
- [ ] Click "Save User"
- [ ] Should show updated name in list

### Delete User
- [ ] Click delete button on testuser
- [ ] Confirm deletion
- [ ] User should be removed from list
- [ ] Should show success message

---

## 5. Chatbot Tests

### Test Chatbot Widget
- [ ] Chatbot button visible in bottom-right corner
- [ ] Click chatbot button
- [ ] Chat window opens
- [ ] Welcome message displays
- [ ] Close button works

### Test Chatbot Queries

#### Query 1: Get All Items
- [ ] Type: "Show me all inventory items"
- [ ] Press Enter or click send
- [ ] Should show typing indicator
- [ ] Should receive response with item data
- [ ] Response should be formatted properly

#### Query 2: Low Stock Items
- [ ] Type: "What items are low in stock?"
- [ ] Should receive list of low stock items
- [ ] Should show count and threshold

#### Query 3: Get Vendors
- [ ] Type: "List all vendors"
- [ ] Should receive vendor information
- [ ] Should show count

#### Query 4: Get Borrowers
- [ ] Type: "Show me all borrowers"
- [ ] Should receive borrower list
- [ ] Should show count

#### Query 5: Get Loans
- [ ] Type: "What are the active loans?"
- [ ] Should receive loan information
- [ ] Should show count

#### Query 6: Specific Item
- [ ] Type: "Show me item with ID 1"
- [ ] Should receive specific item details
- [ ] Or error if item doesn't exist

### Test Chatbot Error Handling
- [ ] Type gibberish: "asdfghjkl"
- [ ] Should receive polite error or clarification request
- [ ] Chatbot should not crash

---

## 6. Inventory Management Tests

### View Items
- [ ] Navigate to Item Management > Manage Inventory Items
- [ ] Should see list of items (if any exist)
- [ ] DataTable should work (search, pagination)

### Create Item (Admin/Manager only)
- [ ] Navigate to Item Management > Add a new Item
- [ ] Fill in item details
- [ ] Submit form
- [ ] Should create successfully
- [ ] Should appear in item list

### Edit Item (Admin/Manager only)
- [ ] Click edit on an item
- [ ] Modify details
- [ ] Save changes
- [ ] Should update successfully

### Delete Item (Admin/Manager only)
- [ ] Click delete on an item
- [ ] Confirm deletion
- [ ] Should remove from list

---

## 7. UI/UX Tests

### Responsive Design
- [ ] Resize browser window
- [ ] Sidebar should collapse on mobile
- [ ] Chatbot should remain accessible
- [ ] Forms should be readable

### Navigation
- [ ] All sidebar links work
- [ ] Breadcrumbs display correctly
- [ ] Navbar dropdown works
- [ ] Footer displays correctly

### Styling
- [ ] Login page gradient displays
- [ ] Register page gradient displays
- [ ] Chatbot widget styled correctly
- [ ] Tables formatted properly
- [ ] Buttons have hover effects

---

## 8. Database Tests

### Check Database Tables
Connect to MySQL and verify:
```sql
USE IMS;
SHOW TABLES;
```

Expected tables:
- [ ] users
- [ ] roles
- [ ] user_roles
- [ ] Inventory_item
- [ ] Borrower
- [ ] Vendor
- [ ] Loan
- [ ] (other existing tables)

### Check Default Data
```sql
SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM user_roles;
```

- [ ] 4 roles exist (ADMIN, MANAGER, STAFF, USER)
- [ ] Admin user exists
- [ ] Admin has ROLE_ADMIN

---

## 9. Security Tests

### Password Encryption
```sql
SELECT username, password FROM users;
```
- [ ] Passwords should be BCrypt hashed (start with $2a$ or $2b$)
- [ ] Passwords should NOT be plain text

### Session Management
- [ ] Login creates session
- [ ] Logout destroys session
- [ ] Cannot access protected pages after logout
- [ ] Remember-me checkbox works

### CSRF Protection
- [ ] Forms should work without CSRF issues
- [ ] API endpoints accessible

---

## 10. Error Handling Tests

### Invalid URLs
- [ ] Navigate to /nonexistent
- [ ] Should show 404 or redirect appropriately

### Database Connection Error
- [ ] Stop MySQL service
- [ ] Try to start application
- [ ] Should show connection error
- [ ] Restart MySQL and application should work

### Invalid API Key (Chatbot)
- [ ] Set invalid Gemini API key
- [ ] Try chatbot query
- [ ] Should show error message
- [ ] Should not crash application

---

## 11. Performance Tests

### Page Load Times
- [ ] Homepage loads in < 2 seconds
- [ ] Login page loads in < 1 second
- [ ] User list loads in < 3 seconds

### Chatbot Response Time
- [ ] Simple query responds in < 5 seconds
- [ ] Complex query responds in < 10 seconds

---

## 12. Browser Compatibility

Test in multiple browsers:
- [ ] Chrome/Edge (Chromium)
- [ ] Firefox
- [ ] Safari (if available)

All features should work in all browsers.

---

## Common Issues & Solutions

### Issue: Port 8086 already in use
**Solution**: Change port in application.properties or kill process using port

### Issue: Database connection refused
**Solution**: Start MySQL service, verify credentials

### Issue: Chatbot not responding
**Solution**: Check Gemini API key, check internet connection

### Issue: 403 Forbidden errors
**Solution**: Check user roles, verify SecurityConfig

### Issue: White label error page
**Solution**: Check controller mappings, verify template paths

---

## Success Criteria

All tests should pass with:
- ✅ No console errors
- ✅ No 500 server errors
- ✅ All features working as expected
- ✅ Proper role-based access control
- ✅ Chatbot responding correctly
- ✅ Database properly populated

---

## Final Verification

- [ ] Application starts without errors
- [ ] Can login as admin
- [ ] Can create new users
- [ ] Can manage inventory (if admin/manager)
- [ ] Chatbot responds to queries
- [ ] Can logout successfully
- [ ] All roles work as expected

**If all checkboxes are checked, your system is ready for use! 🎉**

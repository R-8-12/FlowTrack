# Inventory Management System - Setup Guide

## 🎯 Features Implemented

### 1. ✅ Role-Based Access Control (RBAC)
- **4 Roles**: ADMIN, MANAGER, STAFF, USER
- **Admin**: Full system access, user management
- **Manager**: Add/edit/delete inventory items, vendors
- **Staff**: View and issue items
- **User**: Basic access to view inventory

### 2. ✅ Authentication & Authorization
- Spring Security integration
- BCrypt password encryption
- Login/Logout functionality
- Session management
- Registration system

### 3. ✅ Chatbot with Gemini AI
- Floating chatbot widget on all pages
- Natural language queries about inventory
- Real-time stock information
- Low stock alerts
- Vendor and borrower information

### 4. ✅ User Management
- Admin panel for user CRUD operations
- Role assignment
- User activation/deactivation
- Password management

### 5. ✅ Enhanced Database Functions
- Loan repository created
- Advanced chatbot queries (low stock, item search)
- Complete inventory tracking

---

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- MySQL 8.0+
- Maven 3.6+
- Gemini API Key (get from https://makersuite.google.com/app/apikey)

### Step 1: Database Setup
```sql
CREATE DATABASE IMS;
```

### Step 2: Configure Application
Edit `src/main/resources/application.properties`:

```properties
# Update MySQL credentials if needed
spring.datasource.username=root
spring.datasource.password=root

# Set your Gemini API key as environment variable
# OR replace ${GEMINI_API_KEY} with your actual key
```

### Step 3: Set Gemini API Key

**Option A - Environment Variable (Recommended):**
```bash
# Windows CMD
set GEMINI_API_KEY=your_api_key_here

# Windows PowerShell
$env:GEMINI_API_KEY="your_api_key_here"

# Linux/Mac
export GEMINI_API_KEY=your_api_key_here
```

**Option B - Direct in application.properties:**
```properties
gemini.api.key=your_actual_api_key_here
```

### Step 4: Build and Run
```bash
# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run
```

### Step 5: Access the Application
- **URL**: http://localhost:8086
- **Default Admin Credentials**:
  - Username: `admin`
  - Password: `admin123`

---

## 👥 Default Users

The system automatically creates:
- **Admin User**: admin / admin123
- **Roles**: ROLE_ADMIN, ROLE_MANAGER, ROLE_STAFF, ROLE_USER

---

## 🔐 Security Configuration

### Access Control Rules:
- `/login`, `/register` - Public access
- `/admin/**` - ADMIN only
- `/ItemCreate`, `/ItemEdit/**`, `/ItemDelete/**` - ADMIN & MANAGER
- `/vendors/**` - ADMIN & MANAGER
- All other pages - Authenticated users
- `/api/chatbot/**` - Public (for chatbot functionality)

---

## 🤖 Chatbot Features

The AI chatbot can answer questions like:
- "Show me all inventory items"
- "What items are low in stock?"
- "List all vendors"
- "Show me all borrowers"
- "What loans are currently active?"
- "Show me items with less than 5 in stock"

### Chatbot Functions:
1. `getAllInventoryItems()` - Get all items
2. `getAllVendors()` - Get all vendors
3. `getAllBorrowers()` - Get all borrowers
4. `getAllLoans()` - Get all loans
5. `getItemById(itemId)` - Get specific item
6. `getLowStockItems(threshold)` - Get low stock items

---

## 📁 Project Structure

```
src/main/java/com/example/IMS/
├── config/
│   ├── SecurityConfig.java          # Spring Security configuration
│   └── DataInitializer.java         # Initial data setup
├── model/
│   ├── User.java                    # User entity
│   ├── Role.java                    # Role entity
│   ├── Item.java                    # Inventory item
│   ├── Borrower.java
│   ├── Vendor.java
│   └── Loan.java
├── repository/
│   ├── IUserRepository.java
│   ├── IRoleRepository.java
│   ├── IItemRepository.java
│   ├── ILoanRepository.java         # NEW
│   └── ...
├── service/
│   ├── UserService.java             # User management
│   ├── UserDetailsServiceImpl.java  # Spring Security
│   └── ...
├── controller/
│   ├── AuthController.java          # Login/Register
│   ├── UserManagementController.java # User CRUD
│   ├── ChatbotController.java       # Chatbot API
│   └── ...
└── chatbot/
    ├── config/ChatbotToolsConfig.java
    ├── service/GeminiChatService.java
    └── service/ChatbotDatabaseService.java

src/main/resources/
├── templates/
│   ├── auth/
│   │   ├── login.html               # NEW
│   │   └── register.html            # NEW
│   ├── admin/
│   │   ├── user-list.html           # NEW
│   │   └── user-form.html           # NEW
│   └── ...
└── static/
    ├── css/
    │   └── chatbot.css              # NEW
    └── js/
        └── chatbot.js               # NEW
```

---

## 🎨 UI Features

### Login Page
- Modern gradient design
- Remember me functionality
- Link to registration

### Registration Page
- User self-registration
- Password confirmation
- Email validation

### Chatbot Widget
- Floating button (bottom-right)
- Collapsible chat window
- Typing indicators
- Message history

### User Management (Admin Only)
- List all users with roles
- Add/Edit/Delete users
- Role assignment
- Status management

---

## 🔧 Troubleshooting

### Issue: Login fails
- Check database is running
- Verify default admin user was created (check console logs)
- Try registering a new user

### Issue: Chatbot not responding
- Verify Gemini API key is set correctly
- Check console for API errors
- Ensure internet connection is active

### Issue: Access Denied errors
- Check user roles in database
- Verify SecurityConfig.java permissions
- Clear browser cache and re-login

### Issue: Database connection failed
- Verify MySQL is running
- Check credentials in application.properties
- Ensure IMS database exists

---

## 📝 Creating New Users

### Via Registration Page:
1. Go to http://localhost:8086/register
2. Fill in the form (default role: USER)
3. Login with new credentials

### Via Admin Panel:
1. Login as admin
2. Navigate to User Management
3. Click "Add New User"
4. Select appropriate role
5. Save

---

## 🔄 Next Steps / Future Enhancements

- [ ] Email verification for registration
- [ ] Password reset functionality
- [ ] User profile management
- [ ] Activity logging
- [ ] Advanced reporting
- [ ] Export functionality (PDF/Excel)
- [ ] Real-time notifications
- [ ] Mobile responsive improvements

---

## 📞 Support

For issues or questions:
1. Check console logs for errors
2. Verify all dependencies are installed
3. Ensure database is properly configured
4. Check Gemini API key is valid

---

## 🎉 Success Indicators

When everything is working correctly, you should see:
```
✅ Role created: ROLE_ADMIN
✅ Role created: ROLE_MANAGER
✅ Role created: ROLE_STAFF
✅ Role created: ROLE_USER
✅ Default admin user created - Username: admin, Password: admin123
```

Application should be accessible at: http://localhost:8086

---

**Happy Inventory Managing! 🚀**

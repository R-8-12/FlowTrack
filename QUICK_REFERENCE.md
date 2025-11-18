# Quick Reference Card 📋

## 🚀 Start Application
```bash
# Windows
start.bat

# Or manually
mvn spring-boot:run
```

## 🌐 Access URLs
- **Application**: http://localhost:8086
- **Login**: http://localhost:8086/login
- **Register**: http://localhost:8086/register
- **Admin Panel**: http://localhost:8086/admin/users

## 🔑 Default Credentials
```
Username: admin
Password: admin123
Role: ADMIN
```

## 👥 User Roles
| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access, user management |
| **MANAGER** | Add/edit/delete items, vendors |
| **STAFF** | View inventory, issue/return items |
| **USER** | Basic inventory viewing |

## 🤖 Chatbot Commands
```
"Show me all inventory items"
"What items are low in stock?"
"List all vendors"
"Show me all borrowers"
"What are the active loans?"
"Show me item with ID 1"
"Items with less than 5 in stock"
```

## 📍 Main Navigation
- **Homepage**: `/`
- **Items**: `/ItemView`
- **Add Item**: `/ItemCreate` (Admin/Manager)
- **Issue Item**: `/ItemIssuanceCreate`
- **Return Item**: `/ItemReturnCreate`
- **Vendors**: `/vendors` (Admin/Manager)
- **User Management**: `/admin/users` (Admin only)

## 🔧 Configuration Files
- **Database**: `src/main/resources/application.properties`
- **Security**: `src/main/java/com/example/IMS/config/SecurityConfig.java`
- **Chatbot**: `src/main/java/com/example/IMS/chatbot/config/ChatbotToolsConfig.java`

## 🗄️ Database Setup
```sql
CREATE DATABASE IMS;
```

## 🔐 Environment Variables
```bash
# Windows CMD
set GEMINI_API_KEY=your_api_key_here

# Windows PowerShell
$env:GEMINI_API_KEY="your_api_key_here"
```

## 📊 Key Endpoints
| Endpoint | Method | Access | Purpose |
|----------|--------|--------|---------|
| `/login` | GET/POST | Public | Login page |
| `/register` | GET/POST | Public | Registration |
| `/logout` | GET | Auth | Logout |
| `/admin/users` | GET | Admin | User list |
| `/admin/users/add` | GET/POST | Admin | Add user |
| `/api/chatbot/chat` | POST | Public | Chat API |

## 🎨 UI Components
- **Chatbot**: Bottom-right floating button
- **Navbar**: Top navigation with user menu
- **Sidebar**: Left navigation menu
- **DataTables**: Searchable, sortable tables

## 🐛 Troubleshooting
| Issue | Solution |
|-------|----------|
| Port in use | Change port in application.properties |
| DB connection failed | Start MySQL, check credentials |
| Chatbot not working | Verify Gemini API key |
| 403 errors | Check user role permissions |
| Login fails | Verify user exists in database |

## 📝 Common Tasks

### Create New User (Admin)
1. Login as admin
2. Go to User Management
3. Click "Add New User"
4. Fill form and select role
5. Save

### Add Inventory Item (Manager)
1. Login as manager/admin
2. Go to Item Management
3. Click "Add a new Item"
4. Fill item details
5. Submit

### Use Chatbot
1. Click chatbot button (bottom-right)
2. Type your question
3. Press Enter
4. View response

## 🔄 Development Commands
```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run application
mvn spring-boot:run

# Check for updates
mvn versions:display-dependency-updates
```

## 📚 Documentation Files
- `README_SETUP.md` - Complete setup guide
- `API_ENDPOINTS.md` - API documentation
- `TESTING_CHECKLIST.md` - Testing guide
- `IMPLEMENTATION_SUMMARY.md` - What was built
- `QUICK_REFERENCE.md` - This file

## 🎯 Success Indicators
✅ Application starts on port 8086
✅ Can login with admin/admin123
✅ Chatbot responds to queries
✅ User management accessible
✅ No console errors

## 📞 Need Help?
1. Check console logs for errors
2. Review TESTING_CHECKLIST.md
3. Verify database connection
4. Check Gemini API key
5. Review SecurityConfig.java for permissions

---

**Keep this card handy for quick reference! 🚀**

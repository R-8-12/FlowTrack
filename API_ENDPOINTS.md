# API Endpoints Reference

## 🔐 Authentication Endpoints

### Login
- **URL**: `/login`
- **Method**: GET (form), POST (submit)
- **Access**: Public
- **Parameters**: 
  - `username` (string)
  - `password` (string)
  - `remember-me` (optional)

### Register
- **URL**: `/register`
- **Method**: GET (form), POST (submit)
- **Access**: Public
- **Body**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "confirmPassword": "string",
  "firstName": "string",
  "lastName": "string"
}
```

### Logout
- **URL**: `/logout`
- **Method**: GET
- **Access**: Authenticated users

---

## 👥 User Management Endpoints (Admin Only)

### List All Users
- **URL**: `/admin/users`
- **Method**: GET
- **Access**: ROLE_ADMIN
- **Returns**: List of all users with roles

### Add User Form
- **URL**: `/admin/users/add`
- **Method**: GET
- **Access**: ROLE_ADMIN

### Create User
- **URL**: `/admin/users/add`
- **Method**: POST
- **Access**: ROLE_ADMIN
- **Body**: UserRegistrationDto

### Edit User Form
- **URL**: `/admin/users/edit/{id}`
- **Method**: GET
- **Access**: ROLE_ADMIN

### Update User
- **URL**: `/admin/users/edit/{id}`
- **Method**: POST
- **Access**: ROLE_ADMIN
- **Body**: UserRegistrationDto

### Delete User
- **URL**: `/admin/users/delete/{id}`
- **Method**: GET
- **Access**: ROLE_ADMIN

---

## 🤖 Chatbot API Endpoints

### Chat with Bot
- **URL**: `/api/chatbot/chat`
- **Method**: POST
- **Access**: Public
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "message": "Show me all inventory items"
}
```
- **Response**:
```json
{
  "response": "Here are all inventory items..."
}
```

### Test Chatbot
- **URL**: `/api/chatbot/test`
- **Method**: GET
- **Access**: Public
- **Response**: "✅ Chatbot API is working!"

---

## 📦 Inventory Endpoints

### View All Items
- **URL**: `/ItemView`
- **Method**: GET
- **Access**: Authenticated users

### Create Item Form
- **URL**: `/ItemCreate`
- **Method**: GET
- **Access**: ROLE_ADMIN, ROLE_MANAGER

### Edit Item
- **URL**: `/ItemEdit/{id}`
- **Method**: GET, POST
- **Access**: ROLE_ADMIN, ROLE_MANAGER

### Delete Item
- **URL**: `/ItemDelete/{id}`
- **Method**: GET
- **Access**: ROLE_ADMIN, ROLE_MANAGER

---

## 📋 Item Issuance Endpoints

### View Issued Items
- **URL**: `/ItemIssuanceView`
- **Method**: GET
- **Access**: Authenticated users

### Issue Item Form
- **URL**: `/ItemIssuanceCreate`
- **Method**: GET, POST
- **Access**: Authenticated users

---

## 🔄 Item Return Endpoints

### Return Item Form
- **URL**: `/ItemReturnCreate`
- **Method**: GET, POST
- **Access**: Authenticated users

---

## 💰 Fine Management Endpoints

### Calculate Fine
- **URL**: `/FineCreate`
- **Method**: GET, POST
- **Access**: Authenticated users

### View Fine Records
- **URL**: `/FineView`
- **Method**: GET
- **Access**: Authenticated users

---

## 🏢 Vendor Endpoints

### List Vendors
- **URL**: `/vendors`
- **Method**: GET
- **Access**: ROLE_ADMIN, ROLE_MANAGER

### Vendor Form
- **URL**: `/vendors/add`, `/vendors/edit/{id}`
- **Method**: GET, POST
- **Access**: ROLE_ADMIN, ROLE_MANAGER

---

## 🔧 Chatbot Database Functions

These are internal functions called by the Gemini AI:

1. **getAllInventoryItems()**
   - Returns all inventory items with count

2. **getAllVendors()**
   - Returns all vendors with count

3. **getAllBorrowers()**
   - Returns all borrowers with count

4. **getAllLoans()**
   - Returns all active loans with count

5. **getItemById(itemId)**
   - Parameters: `itemId` (number)
   - Returns specific item details

6. **getLowStockItems(threshold)**
   - Parameters: `threshold` (number, default: 10)
   - Returns items below threshold quantity

---

## 🎯 Role-Based Access Summary

| Endpoint Pattern | ADMIN | MANAGER | STAFF | USER |
|-----------------|-------|---------|-------|------|
| `/login`, `/register` | ✅ | ✅ | ✅ | ✅ |
| `/admin/**` | ✅ | ❌ | ❌ | ❌ |
| `/ItemCreate`, `/ItemEdit/**`, `/ItemDelete/**` | ✅ | ✅ | ❌ | ❌ |
| `/vendors/**` | ✅ | ✅ | ❌ | ❌ |
| `/ItemView` | ✅ | ✅ | ✅ | ✅ |
| `/ItemIssuance**` | ✅ | ✅ | ✅ | ✅ |
| `/ItemReturn**` | ✅ | ✅ | ✅ | ✅ |
| `/Fine**` | ✅ | ✅ | ✅ | ✅ |
| `/api/chatbot/**` | ✅ | ✅ | ✅ | ✅ |

---

## 📝 Example Chatbot Queries

```javascript
// Get all items
POST /api/chatbot/chat
{
  "message": "Show me all inventory items"
}

// Check low stock
POST /api/chatbot/chat
{
  "message": "What items have less than 5 in stock?"
}

// Get vendors
POST /api/chatbot/chat
{
  "message": "List all vendors"
}

// Get specific item
POST /api/chatbot/chat
{
  "message": "Show me details of item with ID 1"
}

// Get borrowers
POST /api/chatbot/chat
{
  "message": "Who are the current borrowers?"
}

// Get loans
POST /api/chatbot/chat
{
  "message": "Show me all active loans"
}
```

---

## 🔒 Security Notes

- All passwords are encrypted using BCrypt
- CSRF protection is disabled for API endpoints
- Session-based authentication
- Remember-me functionality available
- Logout invalidates session

---

## 📊 Response Formats

### Success Response
```json
{
  "response": "Data or message here"
}
```

### Error Response (Chatbot)
```json
{
  "response": "Error: Description of error"
}
```

### Redirect Responses
- Success operations typically redirect with query params:
  - `?success=true`
  - `?updated=true`
  - `?deleted=true`
  - `?error=true`

---

**For testing, use tools like Postman or curl to test API endpoints.**

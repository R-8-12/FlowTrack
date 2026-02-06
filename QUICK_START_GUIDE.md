# 🎯 FlowTrack - Quick Reference Card

## 📖 **What is FlowTrack?**

**In One Sentence:**  
A web-based lending system that tracks physical items (laptops, tools, equipment) borrowed by people, calculates fines for late returns, and manages vendors/repairs.

**Think of it like:**  
📚 A library system, but for equipment instead of books

---

## 🏢 **Who Uses It?**

### **1. Borrowers (Students/Employees)** 👥
- Borrow items
- View their loans
- Pay fines
- Request extensions

### **2. Staff** 👨‍💼
- Process item checkouts
- Process returns
- Calculate fines
- Help borrowers

### **3. Managers** 👩‍💼
- Add/edit/delete items
- Manage vendors
- View reports
- Track repairs

### **4. Admins** 👩‍💻
- Manage users
- Assign roles
- System configuration
- Full access

---

## 🎯 **Real-World Use Cases**

| Organization Type | Example Items | Typical Users |
|-------------------|---------------|---------------|
| **University** | Laptops, projectors, lab equipment | Students, professors |
| **Corporate Office** | Monitors, keyboards, furniture | Employees |
| **Tool Library** | Drills, saws, ladders | Community members |
| **Sports Club** | Balls, rackets, gear | Members |
| **Photography Studio** | Cameras, lenses, lighting | Clients |

---

## 🔄 **How It Works (Simple Flow)**

```
1. SETUP
   ├── Admin adds users → Staff, Managers, Borrowers
   ├── Manager adds vendors → Suppliers of items
   └── Manager adds items → What can be borrowed

2. BORROWING
   ├── Borrower requests item
   ├── Staff checks availability
   ├── Staff issues item (creates loan)
   ├── System records: Who, What, When, Due Date
   └── Item quantity decreases

3. RETURNING
   ├── Borrower returns item
   ├── Staff processes return
   ├── System calculates fine if late
   ├── Fine recorded for payment
   └── Item quantity increases

4. REPAIRS (if damaged)
   ├── Manager logs damage
   ├── Sends to vendor for repair
   ├── Tracks repair cost
   └── Returns to inventory when fixed
```

---

## 📊 **Current Features (What Works Now)**

| Module | Features | Status |
|--------|----------|--------|
| **Items** | Add, edit, delete, view, search | ✅ Complete |
| **Borrowing** | Issue, return, track loans | ✅ Complete |
| **Fines** | Auto-calculate late fees | ✅ Complete |
| **Vendors** | Manage suppliers | ✅ Complete |
| **Repairs** | Log repairs, track costs | ✅ Complete |
| **Users** | RBAC, 4 roles, authentication | ✅ Complete |
| **Reports** | Dashboard, charts, analytics | ✅ Complete |
| **Chatbot** | AI-powered queries (Gemini) | ✅ Complete |
| **Security** | Login, password hashing, sessions | ✅ Complete |

---

## 🚀 **Your Mission: Phase 2 Features**

### **What's Missing (Your Work):**

| Feature | Why Needed | Priority |
|---------|------------|----------|
| **💳 Payment Gateway** | Can't collect fines online | 🔴 HIGH |
| **🔔 Notifications** | No reminders, users forget | 🔴 HIGH |
| **📱 Mobile UI** | Desktop-only right now | 🔴 HIGH |
| **🐳 DevOps** | Manual deployment, no CI/CD | 🔴 HIGH |
| **📅 Reservations** | Can't book items in advance | 🟡 MEDIUM |
| **🔍 Advanced Search** | Basic search only | 🟡 MEDIUM |
| **📦 Item Kits** | Can't bundle related items | 🟡 MEDIUM |
| **🎨 Dark Mode** | Visual preference | 🟢 LOW |

---

## 💰 **The Payment Gateway Story**

### **Current State:**
```
User returns item late → Fine calculated → Shows "$50 fine" → User has to:
1. Visit office in person
2. Pay with cash/check
3. Staff manually records payment
4. Update system manually
❌ Time-consuming, error-prone, inconvenient
```

### **After Your Work:**
```
User returns item late → Fine calculated → Email with "Pay Now" button →
1. Click button → Payment page
2. Enter card details
3. Pay online (Stripe/Razorpay)
4. Instant confirmation + receipt
5. System auto-updates
✅ Fast, accurate, convenient!
```

---

## 🔔 **The Notification Story**

### **Current State:**
```
Item due tomorrow → Nothing happens → User forgets → Returns late →
Gets fined → Gets angry → Blames system
❌ No reminders!
```

### **After Your Work:**
```
Item due tomorrow → Email reminder → "Laptop due tomorrow!" →
User returns on time → No fine → Happy user
✅ Proactive communication!
```

**Notifications You'll Build:**
- 📧 Loan due reminders
- 📧 Fine charged alerts
- 📧 Payment confirmations
- 📧 Repair completion
- 📧 Low stock alerts (for managers)
- 🔔 In-app notifications

---

## 🏗️ **Tech Stack (What You're Working With)**

| Layer | Technology | Why? |
|-------|-----------|------|
| **Backend** | Spring Boot 2.7 + Java 11 | Industry standard, enterprise-ready |
| **Database** | MySQL 8.0 | Reliable, open-source RDBMS |
| **Frontend** | Thymeleaf + Bootstrap | Server-side rendering, simple |
| **Security** | Spring Security | Built-in auth/authorization |
| **AI** | Google Gemini API | Natural language chatbot |
| **Build** | Maven | Dependency management |

---

## 📁 **Project Structure (Quick Map)**

```
FlowTrack/
├── src/main/java/.../IMS/
│   ├── controller/          ← URLs, web requests
│   │   ├── ItemController.java
│   │   ├── FineController.java
│   │   └── ...
│   │
│   ├── service/             ← Business logic
│   │   ├── ItemService.java
│   │   ├── LoanService.java
│   │   └── ...
│   │
│   ├── repository/          ← Database queries
│   │   ├── IItemRepository.java
│   │   └── ...
│   │
│   ├── model/               ← Database tables
│   │   ├── Item.java
│   │   ├── Loan.java
│   │   ├── Borrower.java
│   │   └── ...
│   │
│   └── dto/                 ← Form data objects
│
├── src/main/resources/
│   ├── templates/           ← HTML pages
│   ├── static/              ← CSS, JS, images
│   └── application.properties  ← Config
│
└── Documentation/
    ├── PROJECT_OVERVIEW.md       ⭐ Start here!
    ├── FUNCTIONAL_REQUIREMENTS.md ⭐ Your roadmap
    ├── BEGINNER_GUIDE.md
    └── ARCHITECTURE.md
```

---

## 🗺️ **Your Implementation Roadmap**

### **Week 1-2: Payment Gateway** 💳
1. Choose provider (Stripe/Razorpay)
2. Create Payment entity & repository
3. Build payment form UI
4. Integrate API
5. Test with sandbox
6. Generate receipts

**Files to create:**
- `PaymentController.java`
- `PaymentService.java`
- `Payment.java` (model)
- `IPaymentRepository.java`
- `payment-form.html`

### **Week 3-4: Notifications** 🔔
1. Set up email service (SendGrid/SMTP)
2. Create notification templates
3. Build scheduled job for reminders
4. Implement in-app notifications
5. Add notification preferences

**Files to create:**
- `NotificationService.java`
- `EmailService.java`
- `NotificationScheduler.java`
- `Notification.java` (model)
- Email templates (HTML)

### **Week 5-6: Mobile UI** 📱
1. Add responsive CSS
2. Test on mobile devices
3. Optimize forms for touch
4. Simplify navigation
5. Performance optimization

**Files to modify:**
- All `.html` templates
- `styles.css`
- Add mobile.css

### **Week 7-8: DevOps** 🚀
1. Write Dockerfile
2. Create docker-compose.yml
3. Set up GitHub Actions
4. Configure monitoring
5. Set up auto-backups

**Files to create:**
- `Dockerfile`
- `docker-compose.yml`
- `.github/workflows/ci-cd.yml`
- `deploy.sh`

---

## 📚 **Must-Read Documents (In Order)**

### **For Understanding the Project:**
1. ⭐ **[PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md)** (30 min read)
   - What it is, who it's for, why it exists
   - Business context and use cases
   - Your role in the project

2. ⭐ **[FUNCTIONAL_REQUIREMENTS.md](./FUNCTIONAL_REQUIREMENTS.md)** (45 min read)
   - Detailed specs for each feature
   - User stories, acceptance criteria
   - Implementation priorities

3. **[ARCHITECTURE.md](./ARCHITECTURE.md)** (20 min read)
   - System architecture diagrams
   - Layer explanations
   - Flow diagrams

### **For Learning Spring Boot:**
1. **[BEGINNER_GUIDE.md](./BEGINNER_GUIDE.md)** (15 min read)
2. **[SPRING_BOOT_VISUAL_GUIDE.md](./SPRING_BOOT_VISUAL_GUIDE.md)** (20 min read)
3. **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** (reference)

### **For Implementation:**
1. **[API_ENDPOINTS.md](./API_ENDPOINTS.md)** - Existing APIs
2. **[SECURITY_RBAC.md](./SECURITY_RBAC.md)** - Roles & permissions
3. **[SAMPLE_DATA.md](./SAMPLE_DATA.md)** - Test data

---

## 🎓 **Key Concepts to Understand**

### **1. The 4-Layer Architecture**
```
Controller ← Handles URLs, HTTP requests
    ↓
Service ← Business logic, calculations
    ↓
Repository ← Database queries
    ↓
Database ← Stores data
```

### **2. The Loan Lifecycle**
```
1. Available Item (quantity = 10)
   ↓
2. Issued to Borrower (quantity = 9, Loan created)
   ↓
3. Due Date Arrives
   ↓
4. Returned on Time (quantity = 10, Loan closed, no fine)
   OR
   Returned Late (quantity = 10, Loan closed, fine charged)
```

### **3. Fine Calculation**
```
Days Late = Return Date - Due Date
Fine = Days Late × Item Fine Rate

Example:
- Item: Laptop (fine rate = ₹20/day)
- Issued: Jan 1
- Due: Jan 7
- Returned: Jan 10
- Days Late = 3 days
- Fine = 3 × ₹20 = ₹60
```

### **4. The 4 Roles**
```
USER       → Can borrow, view, return
STAFF      → + Process loans/returns
MANAGER    → + Add/edit items, vendors
ADMIN      → + Manage users, full access
```

---

## 🔧 **Quick Commands**

```powershell
# Run application
mvn spring-boot:run

# Build
mvn clean install

# Run tests
mvn test

# Build Docker image (after you create Dockerfile)
docker build -t flowtrack .

# Run with Docker Compose (after you create it)
docker-compose up

# Check database
mysql -u root -p ims_db
```

---

## 💡 **Pro Tips for Your Work**

### **Before Writing Code:**
1. ✅ Read PROJECT_OVERVIEW.md thoroughly
2. ✅ Read FUNCTIONAL_REQUIREMENTS.md for your feature
3. ✅ Look at existing similar code (how ItemController works)
4. ✅ Plan your database changes first
5. ✅ Write user stories in your own words

### **While Writing Code:**
1. ✅ Follow existing code patterns
2. ✅ Write small commits with clear messages
3. ✅ Test locally before pushing
4. ✅ Comment complex logic
5. ✅ Use the same naming conventions

### **Testing Your Work:**
1. ✅ Test happy path (everything works)
2. ✅ Test error cases (invalid data, network errors)
3. ✅ Test edge cases (midnight, leap year, etc.)
4. ✅ Test on mobile and desktop
5. ✅ Ask someone else to test

---

## 🆘 **When You Get Stuck**

### **Technical Issues:**
- Check [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
- Search Stack Overflow
- Read Spring Boot docs
- Ask in Spring community

### **Business Logic Questions:**
- Re-read [PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md)
- Check [FUNCTIONAL_REQUIREMENTS.md](./FUNCTIONAL_REQUIREMENTS.md)
- Look at similar existing features
- Ask product owner/mentor

### **"I don't know where to start":**
1. Read this entire document again
2. Set up the project locally
3. Explore the UI (register, borrow item, return)
4. Read ItemController.java and ItemService.java
5. Make a tiny change (button color) to get comfortable
6. Start with Payment Gateway (clearest requirements)

---

## 📈 **Success Metrics for Your Work**

### **Payment Gateway:**
- [ ] Borrowers can pay fines online
- [ ] ≥95% payment success rate
- [ ] Automatic receipt generation
- [ ] Payment history visible

### **Notifications:**
- [ ] Users receive reminders 1 day before due date
- [ ] Email delivery rate >98%
- [ ] Notification preferences work
- [ ] In-app notifications update in real-time

### **Mobile UI:**
- [ ] All pages responsive on mobile
- [ ] Forms usable on touchscreens
- [ ] Load time <3s on 4G
- [ ] Works on iOS and Android browsers

### **DevOps:**
- [ ] Application containerized
- [ ] CI/CD pipeline working
- [ ] Zero-downtime deployments
- [ ] Monitoring dashboard active
- [ ] Automated daily backups

---

## 🎯 **Your First Task (Start Today!)**

### **Get Familiar with the System:**

1. **Run the application** (15 minutes)
   ```powershell
   cd d:\Coding_Playground\FlowTrack
   mvn spring-boot:run
   ```

2. **Explore as each role** (30 minutes)
   - Register as USER
   - Login as ADMIN (check sample data for credentials)
   - Browse items
   - Issue an item
   - Return it late
   - See the fine

3. **Read the code** (1 hour)
   - Open `ItemController.java`
   - Follow the flow to `ItemService.java`
   - Check `Item.java` model
   - Look at `item_list.html` template

4. **Make a small change** (15 minutes)
   - Change button color in a template
   - Rebuild and see the change
   - Commit to git: `git commit -m "test: Changed button color"`

5. **Read PROJECT_OVERVIEW.md** (30 minutes)
   - Understand the why behind the project
   - Identify your role in Phase 2

**Total Time: 2.5 hours to get up to speed!**

---

## ✅ **You're Ready!**

You now know:
- ✅ **What FlowTrack is** - Equipment lending system
- ✅ **Who uses it** - Universities, offices, rental businesses
- ✅ **Why it exists** - Track items, prevent loss, automate fines
- ✅ **What's built** - Core features working
- ✅ **What you'll build** - Payment, notifications, mobile, DevOps
- ✅ **How to start** - Read docs, explore code, make small change

**Your next steps:**
1. Complete "Your First Task" above
2. Deep-dive into [PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md)
3. Study [FUNCTIONAL_REQUIREMENTS.md](./FUNCTIONAL_REQUIREMENTS.md) for Payment Gateway
4. Start implementing!

---

**Welcome to the FlowTrack team! Let's build something amazing! 🚀**

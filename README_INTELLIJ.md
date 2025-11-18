# 🚀 Running Inventory Management System in IntelliJ IDEA

## 📍 Your Project Location
```
D:\new inventory management\Major-Project-Invento-main - Copy
```

## 📚 Available Guides

I've created **3 guides** to help you set up the project in IntelliJ IDEA:

### 1. ⚡ QUICK_START_INTELLIJ.txt
**Best for:** Quick reference, experienced users
**Time:** 5 minutes
- Simple step-by-step checklist
- No detailed explanations
- Perfect for quick setup

### 2. 📖 INTELLIJ_SETUP_GUIDE.md
**Best for:** Detailed instructions, troubleshooting
**Time:** 10-15 minutes
- Complete setup instructions
- Troubleshooting section
- Project structure overview
- Configuration details

### 3. 📸 INTELLIJ_VISUAL_GUIDE.md
**Best for:** First-time users, visual learners
**Time:** 15-20 minutes
- Step-by-step with visual descriptions
- Screenshots descriptions
- What you'll see at each step
- Common issues with solutions

---

## 🎯 Quick Start (5 Steps)

### 1️⃣ Open Project
```
IntelliJ IDEA → File → Open → Select project folder → OK
```

### 2️⃣ Configure JDK
```
File → Project Structure → Project → SDK → Select JDK 24
```

### 3️⃣ Create Run Configuration
```
Run → Edit Configurations → + → Application
Main class: com.example.IMS.ImsApApplication
Module: IMS-AP
```

### 4️⃣ Verify MySQL
```
Windows + R → services.msc → Find MySQL80 → Ensure Running
```

### 5️⃣ Run Application
```
Click Run button (▶️) → Wait for startup → Open http://localhost:8087
```

---

## 🔑 Login Credentials

```
Username: admin
Password: admin123
```

---

## ✅ What's Already Configured

Your project is ready with:
- ✅ All prices converted to Rupees (₹)
- ✅ Fine Records page fixed
- ✅ Dashboard with historical tracking
- ✅ Chatbot integration
- ✅ MySQL database configuration
- ✅ Template caching disabled for development
- ✅ All dependencies in pom.xml

---

## 🗂️ Project Files in Your Folder

```
Major-Project-Invento-main - Copy/
│
├── 📄 README_INTELLIJ.md              ← You are here
├── 📄 QUICK_START_INTELLIJ.txt        ← Quick 5-minute guide
├── 📄 INTELLIJ_SETUP_GUIDE.md         ← Detailed setup guide
├── 📄 INTELLIJ_VISUAL_GUIDE.md        ← Visual step-by-step guide
│
├── 📄 START_HERE.txt                  ← Project overview
├── 📄 MYSQL_SETUP_GUIDE.md            ← Database setup
├── 📄 CHATBOT_QUICK_START.md          ← Chatbot configuration
│
├── 📁 src/                            ← Source code
│   ├── main/
│   │   ├── java/                      ← Java files
│   │   └── resources/                 ← Config & templates
│   └── test/                          ← Test files
│
├── 📄 pom.xml                         ← Maven configuration
└── 📄 mvnw.cmd                        ← Maven wrapper
```

---

## 🆘 Quick Troubleshooting

### Problem: Maven not importing
```
Solution: Right-click pom.xml → Maven → Reload Project
```

### Problem: Cannot resolve symbols
```
Solution: File → Invalidate Caches → Invalidate and Restart
```

### Problem: Port 8087 in use
```
Solution: Stop running process or change port in application.properties
```

### Problem: MySQL connection error
```
Solution: Check MySQL service is running (services.msc)
```

---

## 📞 Need Help?

1. **First time?** → Read `INTELLIJ_VISUAL_GUIDE.md`
2. **Quick setup?** → Read `QUICK_START_INTELLIJ.txt`
3. **Detailed info?** → Read `INTELLIJ_SETUP_GUIDE.md`
4. **Database issues?** → Read `MYSQL_SETUP_GUIDE.md`

---

## 🎯 System Requirements

- ✅ IntelliJ IDEA (Community or Ultimate)
- ✅ JDK 11+ (You have JDK 24 ✓)
- ✅ MySQL Server (Port 3306)
- ✅ Maven (Bundled with IntelliJ)
- ✅ 4GB RAM minimum
- ✅ Internet connection (for first-time Maven download)

---

## 🌟 Features

Your application includes:
- 📦 Item Management (with ₹ prices)
- 👥 Vendor Management
- 📋 Item Issuance & Return
- 💰 Fine Calculation & Records
- 📊 Dashboard with Charts
- 🤖 AI Chatbot
- 📈 Historical Tracking
- 🔐 User Authentication

---

## 🚀 After Setup

Once running, you can:
1. Manage inventory items
2. Track item issuance and returns
3. Calculate and manage fines
4. View analytics on dashboard
5. Chat with AI assistant
6. Generate reports

---

## 💡 Development Tips

**Hot Reload:**
- Enable auto-restart on code changes
- See INTELLIJ_SETUP_GUIDE.md for instructions

**Debugging:**
- Use Debug button (🐛) instead of Run
- Set breakpoints by clicking line numbers

**Database View:**
- IntelliJ Ultimate: Built-in database tools
- IntelliJ Community: Use MySQL Workbench

---

## 📊 Application URLs

After starting:
- **Main App:** http://localhost:8087
- **Login Page:** http://localhost:8087/login
- **Dashboard:** http://localhost:8087/ (after login)

---

## ✨ Recent Updates

- ✅ All currency converted to Rupees (₹)
- ✅ Fine Records page error fixed
- ✅ Template caching disabled
- ✅ Dashboard tracking enabled
- ✅ IntelliJ setup guides created

---

**Ready to start? Open `QUICK_START_INTELLIJ.txt` for the fastest setup!**

**Good luck! 🎉**

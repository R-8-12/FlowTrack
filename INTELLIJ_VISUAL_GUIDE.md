# 📸 IntelliJ IDEA Visual Setup Guide

## Complete Step-by-Step with Visual Instructions

---

## 🎯 STEP 1: Opening the Project

### 1.1 Launch IntelliJ IDEA
- Double-click the IntelliJ IDEA icon on your desktop
- Or search for "IntelliJ IDEA" in Windows Start menu

### 1.2 Open Project Dialog
```
Click: File → Open
```
**What you'll see:**
- A file browser window will open

### 1.3 Navigate to Project
```
Path: D:\new inventory management\Major-Project-Invento-main - Copy
```
**Actions:**
1. In the file browser, navigate to D: drive
2. Open "new inventory management" folder
3. Select "Major-Project-Invento-main - Copy" folder
4. Click "OK" button

### 1.4 Trust Project
**What you'll see:**
- A dialog asking "Trust and Open Project?"
**Action:**
- Click "Trust Project"

### 1.5 Wait for Maven Import
**What you'll see:**
- Bottom-right corner: Progress bar "Importing Maven projects..."
- Status bar: "Indexing..."
**Action:**
- ⏳ Wait 2-5 minutes until both complete
- ✅ When done, you'll see "Ready" in the status bar

---

## 🎯 STEP 2: Configure Project SDK

### 2.1 Open Project Structure
```
Click: File → Project Structure
OR Press: Ctrl + Alt + Shift + S
```

### 2.2 Configure Project Settings
**What you'll see:**
- A dialog with left sidebar showing: Project, Modules, Libraries, etc.

**In the "Project" section:**
1. **Name:** Should show "IMS-AP"
2. **SDK:** Click the dropdown
   - If you see "24 (java version 24.0.1)": Select it ✅
   - If not listed: Continue to 2.3

3. **Language level:** Select "11 - Local variable syntax for lambda parameters"

### 2.3 Add SDK (If Not Listed)
**If SDK dropdown is empty:**
1. Click "Add SDK" → "Download JDK..."
2. OR Click "Add SDK" → "JDK..."
3. Browse to: `C:\Program Files\Java\jdk-24`
4. Click "OK"

### 2.4 Verify Modules
**In the left sidebar:**
1. Click "Modules"
2. You should see: "IMS-AP" with a blue square icon
3. Under "IMS-AP", you should see: "src" folder structure

### 2.5 Apply Changes
```
Click: Apply → OK
```

---

## 🎯 STEP 3: Create Run Configuration

### 3.1 Open Run Configurations
```
Click: Run → Edit Configurations...
OR Click: Dropdown next to Run button (top-right) → Edit Configurations...
```

### 3.2 Add New Configuration
**What you'll see:**
- A dialog with left panel showing existing configurations

**Actions:**
1. Click the "+" button (top-left corner)
2. Select **"Application"** (first option in the list)
   - **Note:** IntelliJ Community Edition doesn't have "Spring Boot" option
   - IntelliJ Ultimate users can select "Spring Boot" if available

### 3.3 Configure Application
**Fill in these fields:**

**Name:**
```
IMS Application
```

**Main class:**
1. Click the "..." button (browse button)
2. In the search dialog, type: `ImsApApplication`
3. Select: `com.example.IMS.ImsApApplication`
4. Click "OK"

**OR manually type:**
```
com.example.IMS.ImsApApplication
```

**Use classpath of module:**
```
Select: IMS-AP
```

**JRE:**
```
Select: 24 (java version "24.0.1")
```

**Other fields:**
- VM options: (leave empty)
- Program arguments: (leave empty)
- Working directory: (auto-filled, don't change)

### 3.4 Save Configuration
```
Click: Apply → OK
```

**What you'll see:**
- Top-right corner now shows: "IMS Application" in the dropdown
- Green Run button (▶️) is now active

---

## 🎯 STEP 4: Verify MySQL Database

### 4.1 Check MySQL Service
**Open Services:**
1. Press `Windows + R`
2. Type: `services.msc`
3. Press Enter

**Find MySQL:**
1. Scroll down to find "MySQL80"
2. Check "Status" column: Should say "Running"
3. If not running:
   - Right-click "MySQL80"
   - Click "Start"

### 4.2 Verify Database Configuration
**In IntelliJ:**
1. Navigate to: `src/main/resources/application.properties`
2. Verify these lines exist:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ims?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
server.port=8087
```

---

## 🎯 STEP 5: Run the Application

### 5.1 Start Application
**Method 1: Using Run Button**
```
Click: Green Run button (▶️) in top-right toolbar
```

**Method 2: Using Keyboard**
```
Press: Shift + F10
```

**Method 3: From Main Class**
1. Navigate to: `src/main/java/com/example/IMS/ImsApApplication.java`
2. Right-click on the file
3. Select: "Run 'ImsApApplication'"

### 5.2 Watch Console Output
**What you'll see in the Run console (bottom panel):**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.1)

... (many log lines) ...

2025-11-17 20:21:50.xxx  INFO --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8087 (http)
2025-11-17 20:21:50.xxx  INFO --- [  restartedMain] com.example.IMS.ImsApApplication         : Started ImsApApplication in X.XXX seconds
```

**✅ Success indicators:**
- "Tomcat started on port(s): 8087"
- "Started ImsApApplication in X seconds"
- No red error messages

### 5.3 Access Application
**Open your web browser:**
```
URL: http://localhost:8087
```

**What you'll see:**
- Login page with username and password fields
- "Inventory Management System" title

**Login credentials:**
```
Username: admin
Password: admin123
```

---

## 🎯 STEP 6: Verify Everything Works

### 6.1 After Login
**You should see:**
- Dashboard with charts
- Left sidebar with modules:
  - Item Management
  - Item Issuance
  - Item Return
  - Fine (with Fine Records submenu)
  - Vendor Management

### 6.2 Test Fine Records
1. Click "Fine" in left sidebar
2. Click "Fine Records"
3. Should display without errors
4. Table shows: Item ID, Borrower ID, Loan Duration, etc.
5. All currency values show ₹ symbol

### 6.3 Test Other Features
- Click "Item Management" → "View Items" (should show items with ₹ prices)
- Click "Vendor Management" → "View Vendors"
- Check dashboard charts are displaying

---

## 🔧 Common Issues and Solutions

### Issue 1: "Cannot resolve symbol" errors in code

**Visual indicator:**
- Red underlines in Java files
- Red text in code

**Solution:**
```
File → Invalidate Caches... → Invalidate and Restart
```
Wait for IntelliJ to restart and re-index (2-3 minutes)

---

### Issue 2: Maven dependencies not downloading

**Visual indicator:**
- Red underlines in pom.xml
- "Cannot resolve" errors

**Solution:**
1. Right-click on `pom.xml`
2. Select: `Maven → Reload Project`
3. OR: Click Maven icon (M) in right sidebar → Click refresh icon (🔄)

---

### Issue 3: Run button is grayed out

**Visual indicator:**
- Run button (▶️) is gray/disabled

**Solution:**
1. Verify Run Configuration exists (Step 3)
2. Check that "IMS Application" is selected in dropdown
3. Ensure Maven import completed successfully

---

### Issue 4: Application fails to start - Port already in use

**Console shows:**
```
Error: Port 8087 is already in use
```

**Solution Option 1: Stop existing process**
1. In IntelliJ, click Stop button (⏹️) in Run panel
2. OR: Press `Ctrl + F2`

**Solution Option 2: Change port**
1. Open: `src/main/resources/application.properties`
2. Change line:
```properties
server.port=8088
```
3. Re-run application
4. Access at: `http://localhost:8088`

---

### Issue 5: MySQL connection error

**Console shows:**
```
Error: Communications link failure
OR
Error: Access denied for user 'root'
```

**Solution:**
1. Verify MySQL is running (Step 4.1)
2. Check credentials in `application.properties`
3. Test MySQL connection:
   - Open Command Prompt
   - Type: `mysql -u root -p`
   - Enter password: `root`
   - Should connect successfully

---

## 📊 IntelliJ Interface Overview

### Top Toolbar (from left to right):
- **Project dropdown:** Shows current project
- **Run Configuration dropdown:** Shows "IMS Application"
- **Run button (▶️):** Starts application
- **Debug button (🐛):** Starts in debug mode
- **Stop button (⏹️):** Stops running application

### Left Sidebar:
- **Project:** File explorer
- **Structure:** Class structure
- **Maven:** Maven projects and dependencies

### Bottom Panel:
- **Run:** Console output when running
- **Terminal:** Command line interface
- **Problems:** Shows errors and warnings
- **Services:** Shows running services

### Right Sidebar:
- **Maven:** Maven lifecycle and plugins
- **Database:** Database connections

---

## ✅ Success Checklist

Before considering setup complete, verify:

- [ ] IntelliJ opened project successfully
- [ ] Maven dependencies downloaded (no red errors in pom.xml)
- [ ] JDK 24 configured in Project Structure
- [ ] Run Configuration "IMS Application" created
- [ ] MySQL service is running
- [ ] Application starts without errors
- [ ] Console shows "Started ImsApApplication"
- [ ] Can access http://localhost:8087
- [ ] Can login with admin/admin123
- [ ] Dashboard displays correctly
- [ ] Fine Records page works without errors
- [ ] All prices show ₹ symbol

---

## 🎉 You're All Set!

Your Inventory Management System is now running in IntelliJ IDEA!

**Next Steps:**
- Explore the application features
- Test item management
- Try the chatbot feature
- Check the dashboard analytics

**Development Tips:**
- Use `Ctrl + Shift + F10` to run current file
- Use `Shift + F9` to debug
- Use `Ctrl + F2` to stop application
- Enable auto-reload for faster development (see INTELLIJ_SETUP_GUIDE.md)

---

**Need more help? Check:**
- INTELLIJ_SETUP_GUIDE.md (detailed guide)
- QUICK_START_INTELLIJ.txt (quick reference)
- START_HERE.txt (project overview)

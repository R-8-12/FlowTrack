# IntelliJ IDEA Setup Guide for Inventory Management System

## 📋 Prerequisites

Before starting, ensure you have:
- ✅ IntelliJ IDEA (Community or Ultimate Edition)
- ✅ JDK 11 or higher installed (You have JDK 24)
- ✅ MySQL Server running (Port 3306)
- ✅ Maven (bundled with IntelliJ)

---

## 🚀 Step-by-Step Setup in IntelliJ IDEA

### Step 1: Open Project in IntelliJ

1. **Launch IntelliJ IDEA**

2. **Open the Project:**
   - Click on `File` → `Open`
   - Navigate to: `D:\new inventory management\Major-Project-Invento-main - Copy`
   - Select the folder and click `OK`
   - IntelliJ will detect it as a Maven project

3. **Wait for Maven Import:**
   - IntelliJ will automatically start importing Maven dependencies
   - You'll see a progress bar at the bottom: "Importing Maven projects..."
   - This may take 2-5 minutes depending on your internet speed
   - Wait until it completes

---

### Step 2: Configure JDK

1. **Open Project Structure:**
   - Click `File` → `Project Structure` (or press `Ctrl+Alt+Shift+S`)

2. **Set Project SDK:**
   - In the left panel, click `Project`
   - Under `SDK`, select your JDK 24
   - If not listed, click `Add SDK` → `JDK` → Browse to `C:\Program Files\Java\jdk-24`
   - Set `Language level` to `11` or higher
   - Click `Apply`

3. **Verify Modules:**
   - In the left panel, click `Modules`
   - Ensure `IMS-AP` module is listed
   - Click `OK`

---

### Step 3: Configure MySQL Database

1. **Ensure MySQL is Running:**
   - Open Services (Windows + R, type `services.msc`)
   - Find `MySQL80` service
   - Ensure it's running (Status: Running)

2. **Verify Database Configuration:**
   - The project is already configured to use:
     - Database: `ims`
     - Username: `root`
     - Password: `root`
     - Port: `3306`

3. **Check application.properties:**
   - Open: `src/main/resources/application.properties`
   - Verify these settings:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ims?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=root
   ```

---

### Step 4: Configure Run Configuration

1. **Open Run/Debug Configurations:**
   - Click `Run` → `Edit Configurations...`
   - Or click the dropdown next to the Run button (top-right) → `Edit Configurations...`

2. **Add Application Configuration:**
   - Click the `+` button (top-left)
   - Select `Application` (first option)
   - **Note:** If you have IntelliJ Ultimate and see "Spring Boot", you can use that instead

3. **Configure the Application:**
   - **Name:** `IMS Application`
   - **Main class:** Click `...` button and select `com.example.IMS.ImsApApplication`
     - Or type: `com.example.IMS.ImsApApplication`
   - **Use classpath of module:** Select `IMS-AP`
   - **JRE:** Select your JDK 24 (java 24)
   - Click `Apply` then `OK`

---

### Step 5: Run the Application

1. **Start the Application:**
   - Click the green `Run` button (▶️) in the top-right toolbar
   - Or press `Shift + F10`
   - Or right-click on `ImsApApplication.java` → `Run 'ImsApApplication'`

2. **Wait for Startup:**
   - Watch the `Run` console at the bottom
   - Wait for the message: `Started ImsApApplication in X seconds`
   - You should see: `Tomcat started on port(s): 8087`

3. **Access the Application:**
   - Open your browser
   - Go to: `http://localhost:8087`
   - You should see the login page

---

## 🔧 Troubleshooting

### Issue 1: Maven Dependencies Not Downloading

**Solution:**
1. Right-click on `pom.xml`
2. Select `Maven` → `Reload Project`
3. Or click the Maven icon (M) in the right sidebar → Click refresh icon

### Issue 2: "Cannot resolve symbol" errors

**Solution:**
1. Click `File` → `Invalidate Caches...`
2. Select `Invalidate and Restart`
3. Wait for IntelliJ to restart and re-index

### Issue 3: Port 8087 Already in Use

**Solution:**
1. Stop any running Spring Boot process
2. Or change port in `application.properties`:
   ```properties
   server.port=8088
   ```

### Issue 4: MySQL Connection Error

**Solution:**
1. Verify MySQL is running
2. Check username/password in `application.properties`
3. Ensure database `ims` exists:
   ```sql
   CREATE DATABASE IF NOT EXISTS ims;
   ```

### Issue 5: JDK Not Found

**Solution:**
1. Go to `File` → `Project Structure` → `SDKs`
2. Click `+` → `Add JDK`
3. Browse to: `C:\Program Files\Java\jdk-24`
4. Click `OK`

---

## 📁 Project Structure

```
Major-Project-Invento-main - Copy/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/IMS/
│   │   │       ├── controller/      # REST Controllers
│   │   │       ├── model/           # Entity Classes
│   │   │       ├── repository/      # JPA Repositories
│   │   │       ├── service/         # Business Logic
│   │   │       └── ImsApApplication.java  # Main Class
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf HTML Templates
│   │       ├── static/              # CSS, JS, Images
│   │       └── application.properties
│   └── test/                        # Test Classes
├── pom.xml                          # Maven Configuration
└── mvnw.cmd                         # Maven Wrapper
```

---

## 🎯 Default Login Credentials

After the application starts, use these credentials:

- **Username:** `admin`
- **Password:** `admin123`

---

## ✅ Verification Checklist

Before running, ensure:
- [ ] IntelliJ has finished importing Maven dependencies
- [ ] JDK 24 is configured in Project Structure
- [ ] MySQL service is running
- [ ] Database `ims` exists
- [ ] Run configuration is created
- [ ] No errors in the `pom.xml` file

---

## 🔄 Hot Reload (Optional)

To enable automatic restart on code changes:

1. Add Spring Boot DevTools (already included in pom.xml)
2. Enable automatic build:
   - `File` → `Settings` → `Build, Execution, Deployment` → `Compiler`
   - Check `Build project automatically`
3. Enable registry setting:
   - Press `Ctrl+Shift+A`
   - Type `Registry`
   - Enable `compiler.automake.allow.when.app.running`

---

## 📞 Need Help?

If you encounter issues:
1. Check the console output in IntelliJ
2. Verify MySQL is running
3. Check application.properties settings
4. Ensure all Maven dependencies are downloaded

---

## 🎉 Success!

Once you see "Started ImsApApplication" in the console:
- Application is running at: `http://localhost:8087`
- All features including Fine Records are working
- Currency is displayed in Rupees (₹)
- Dashboard tracking is enabled

---

**Happy Coding! 🚀**

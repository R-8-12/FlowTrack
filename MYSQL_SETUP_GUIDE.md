# MySQL Database Setup Guide

## Prerequisites
- MySQL Server 8.0 or higher installed
- MySQL running on localhost:3306

## Step 1: Install MySQL (if not installed)

### Windows:
1. Download MySQL Installer from: https://dev.mysql.com/downloads/installer/
2. Run the installer
3. Choose "Developer Default" setup
4. Set root password as: `root` (or update application.properties with your password)
5. Complete the installation

### Verify MySQL is Running:
```cmd
mysql --version
```

## Step 2: Create Database

### Option A: Using MySQL Command Line
```cmd
mysql -u root -p
```
Enter password: `root`

Then run:
```sql
CREATE DATABASE ims_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
EXIT;
```

### Option B: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to Local instance
3. Click "Create a new schema" button
4. Name: `ims_db`
5. Charset: `utf8mb4`
6. Collation: `utf8mb4_unicode_ci`
7. Click Apply

## Step 3: Configure Application

The application is already configured in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ims_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

### If your MySQL credentials are different:
Edit `src/main/resources/application.properties`:
- Change `username` if not `root`
- Change `password` if not `root`
- Change `3306` if MySQL runs on different port

## Step 4: Run the Application

```cmd
cd "Major-Project-Invento-main - Copy"
mvnw.cmd spring-boot:run
```

The application will:
1. Connect to MySQL
2. Automatically create all tables
3. Insert sample data (roles, users, items, vendors)
4. Start on http://localhost:8087

## Step 5: Verify Database

### Check tables were created:
```sql
USE ims_db;
SHOW TABLES;
```

You should see:
- borrower
- dashboard_snapshot
- inventory_item
- item_repair
- item_type
- loan
- manager_type
- roles
- user_roles
- users
- vendor

### Check sample data:
```sql
SELECT * FROM users;
SELECT * FROM inventory_item;
SELECT * FROM vendor;
```

## Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"
- Check MySQL password in application.properties
- Reset MySQL root password if needed

### Error: "Unknown database 'ims_db'"
- Create the database using Step 2

### Error: "Communications link failure"
- Verify MySQL service is running
- Check port 3306 is not blocked
- Verify localhost connection

### Error: "Public Key Retrieval is not allowed"
- Already handled in connection URL with `allowPublicKeyRetrieval=true`

## Data Persistence

✅ **All data is now permanently stored in MySQL**
✅ **Survives application restarts**
✅ **Can be backed up using MySQL tools**
✅ **Can be accessed by other applications**

## Backup & Restore

### Backup:
```cmd
mysqldump -u root -p ims_db > ims_backup.sql
```

### Restore:
```cmd
mysql -u root -p ims_db < ims_backup.sql
```

## Default Login Credentials

After first run:
- **Username**: admin
- **Password**: admin123

## MySQL Configuration (Optional)

For production, consider:
1. Creating a dedicated MySQL user (not root)
2. Using strong passwords
3. Enabling SSL connections
4. Setting up regular backups
5. Configuring connection pooling

### Create Dedicated User:
```sql
CREATE USER 'ims_user'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON ims_db.* TO 'ims_user'@'localhost';
FLUSH PRIVILEGES;
```

Then update application.properties:
```properties
spring.datasource.username=ims_user
spring.datasource.password=strong_password
```

## Success!

Your Inventory Management System is now using MySQL for permanent data storage!

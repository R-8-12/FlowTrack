@echo off
echo ========================================
echo  MySQL Database Setup for IMS
echo ========================================
echo.

REM Check if MySQL is installed
where mysql >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL is not installed or not in PATH!
    echo.
    echo Please install MySQL from: https://dev.mysql.com/downloads/installer/
    echo.
    pause
    exit /b 1
)

echo [OK] MySQL is installed
echo.

REM Prompt for MySQL root password
set /p MYSQL_PASSWORD="Enter MySQL root password (default: root): "
if "%MYSQL_PASSWORD%"=="" set MYSQL_PASSWORD=root

echo.
echo Creating database 'ims_db'...
echo.

REM Create database using SQL script
mysql -u root -p%MYSQL_PASSWORD% < setup-mysql.sql

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to create database!
    echo Please check your MySQL password and try again.
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  SUCCESS!
echo ========================================
echo.
echo Database 'ims_db' has been created successfully!
echo.
echo Next steps:
echo 1. Make sure MySQL credentials in application.properties match:
echo    - Username: root
echo    - Password: %MYSQL_PASSWORD%
echo.
echo 2. Run the application:
echo    test-and-run.bat
echo.
echo 3. Access at: http://localhost:8087
echo    - Username: admin
echo    - Password: admin123
echo.
pause

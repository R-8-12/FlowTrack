@echo off
echo ========================================
echo  Database Connection Test
echo ========================================
echo.

REM Check if MySQL is running
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo [ERROR] MySQL service is not running!
    echo Please start MySQL service first.
    pause
    exit /b 1
)

echo [OK] MySQL service is running
echo.

echo Testing database connection...
echo.
echo Please enter your MySQL root password when prompted.
echo.

mysql -u root -p -e "SHOW DATABASES LIKE 'IMS';"

if errorlevel 1 (
    echo.
    echo [ERROR] Could not connect to MySQL or IMS database doesn't exist!
    echo.
    echo To create the database, run:
    echo   mysql -u root -p
    echo   CREATE DATABASE IMS;
    echo   exit
    echo.
) else (
    echo.
    echo [OK] IMS database exists!
    echo.
    echo Checking tables...
    mysql -u root -p -e "USE IMS; SHOW TABLES;"
)

pause

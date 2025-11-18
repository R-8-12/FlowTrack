@echo off
echo ========================================
echo  Convert to Rupees and Add Graph History
echo ========================================
echo.

REM Prompt for MySQL root password
set /p MYSQL_PASSWORD="Enter MySQL root password (default: root): "
if "%MYSQL_PASSWORD%"=="" set MYSQL_PASSWORD=root

echo.
echo Applying changes...
echo - Converting prices to Rupees (₹)
echo - Adding 15 days of historical graph data
echo.

REM Run the SQL script
mysql -u root -p%MYSQL_PASSWORD% < update-to-rupees-and-history.sql

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to apply changes!
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
echo ✅ All prices converted to Indian Rupees (₹)
echo ✅ Historical graph data added (Nov 1-13, 2024)
echo.
echo Next steps:
echo 1. Refresh your browser (Ctrl + F5)
echo 2. View the dashboard graphs with historical data
echo 3. All prices now show in Rupees
echo.
echo The graphs will now show:
echo - 15 days of historical trends
echo - Date labels (Nov 1, Nov 2, etc.)
echo - Realistic inventory patterns
echo.
pause

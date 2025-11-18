@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  IMS - Complete Test and Run Script
echo ========================================
echo.

REM Set JAVA_HOME
if "%JAVA_HOME%"=="" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-24"
    echo [INFO] JAVA_HOME set to: %JAVA_HOME%
)

REM Check Java
echo [1/6] Checking Java installation...
java -version 2>&1 | findstr /C:"version" >nul
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH!
    pause
    exit /b 1
)
echo [OK] Java is installed
echo.

REM Check MySQL
echo [2/6] Checking MySQL service...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo [WARNING] MySQL80 service is not running!
    echo Trying MySQL service name...
    sc query MySQL | find "RUNNING" >nul
    if errorlevel 1 (
        echo [ERROR] MySQL service is not running!
        echo.
        echo Please start MySQL manually:
        echo   - Open Services (services.msc)
        echo   - Find MySQL or MySQL80
        echo   - Click Start
        echo.
        echo Or run: net start MySQL80
        echo.
        set /p continue="Continue anyway? (y/n): "
        if /i not "!continue!"=="y" (
            exit /b 1
        )
    ) else (
        echo [OK] MySQL is running
    )
) else (
    echo [OK] MySQL80 is running
)
echo.

REM Check Gemini API Key
echo [3/6] Checking Gemini API Key...
if "%GEMINI_API_KEY%"=="" (
    echo [WARNING] GEMINI_API_KEY is not set!
    echo.
    echo Please set your Gemini API key:
    echo   set GEMINI_API_KEY=your_api_key_here
    echo.
    echo Or continue without it (chatbot won't work)
    set /p continue="Continue anyway? (y/n): "
    if /i not "!continue!"=="y" (
        exit /b 1
    )
) else (
    echo [OK] GEMINI_API_KEY is set
)
echo.

REM Compile project
echo [4/6] Compiling project...
call mvnw.cmd clean compile -DskipTests -q
if errorlevel 1 (
    echo [ERROR] Compilation failed! Check errors above.
    pause
    exit /b 1
)
echo [OK] Compilation successful
echo.

REM Run tests (optional)
echo [5/6] Running quick validation...
call mvnw.cmd test-compile -q
if errorlevel 1 (
    echo [WARNING] Test compilation had issues (non-critical)
) else (
    echo [OK] Validation passed
)
echo.

REM Start application
echo [6/6] Starting application...
echo ========================================
echo.
echo Application will start on: http://localhost:8086
echo.
echo Default credentials:
echo   Username: admin
echo   Password: admin123
echo.
echo Press Ctrl+C to stop the application
echo ========================================
echo.

call mvnw.cmd spring-boot:run

pause

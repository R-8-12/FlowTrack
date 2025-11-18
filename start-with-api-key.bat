@echo off
echo ========================================
echo  IMS Application Startup with API Key
echo ========================================
echo.

REM Prompt for API key
set /p GEMINI_API_KEY="Enter your Gemini API Key: "

if "%GEMINI_API_KEY%"=="" (
    echo ERROR: API key cannot be empty!
    pause
    exit /b 1
)

echo.
echo API Key set successfully!
echo Starting application...
echo.

REM Set JAVA_HOME
set "JAVA_HOME=C:\Program Files\Java\jdk-24"

REM Start the application
call mvnw.cmd spring-boot:run

pause

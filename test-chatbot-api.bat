@echo off
echo ========================================
echo Testing Chatbot API Configuration
echo ========================================
echo.

REM Check if API key is set
if defined GEMINI_API_KEY (
    echo ✅ GEMINI_API_KEY is set
    echo Key starts with: %GEMINI_API_KEY:~0,10%...
) else (
    echo ❌ GEMINI_API_KEY is NOT set
    echo.
    echo Please set it first:
    echo set GEMINI_API_KEY=your_actual_api_key_here
    echo.
    pause
    exit /b 1
)

echo.
echo Testing API endpoint...
echo.

curl -s http://localhost:8087/api/chatbot/test

echo.
echo.
echo ========================================
echo If you see "✅ Chatbot API is working!" above,
echo the backend is running correctly.
echo.
echo Now check the application logs for detailed
echo error messages when you send a chat message.
echo ========================================
echo.
pause

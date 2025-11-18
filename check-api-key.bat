@echo off
echo ========================================
echo Checking GEMINI_API_KEY Environment Variable
echo ========================================
echo.

if defined GEMINI_API_KEY (
    echo ✅ GEMINI_API_KEY is SET
    echo.
    echo Key starts with: %GEMINI_API_KEY:~0,10%...
    echo Key length: 
    echo %GEMINI_API_KEY% > temp.txt
    for %%A in (temp.txt) do echo %%~zA bytes
    del temp.txt
    echo.
    echo ✅ Your API key is configured!
) else (
    echo ❌ GEMINI_API_KEY is NOT SET
    echo.
    echo To set it, run:
    echo set GEMINI_API_KEY=your_actual_api_key_here
    echo.
    echo Or add it to System Environment Variables for permanent use.
)

echo.
echo ========================================
pause

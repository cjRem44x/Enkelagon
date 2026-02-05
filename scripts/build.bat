@echo off
cd /d "%~dp0.."
echo Building Enkelagon...
mvn clean compile -q
if %ERRORLEVEL% EQU 0 (
    echo Build successful!
) else (
    echo Build failed!
    exit /b 1
)

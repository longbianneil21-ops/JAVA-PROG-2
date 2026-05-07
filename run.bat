@echo off
cd /d "%~dp0"

set "JAVAFX=C:\javafx-sdk\lib"
set "MYSQL=lib\mysql-connector-j-9.7.0.jar"

echo Compiling...

javac ^
--module-path "%JAVAFX%" ^
--add-modules javafx.controls ^
-cp "%MYSQL%" ^
-d out ^
src\*.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed.
    pause
    exit /b
)

echo.
echo Running...

java ^
--module-path "%JAVAFX%" ^
--add-modules javafx.controls ^
-cp "out;%MYSQL%" ^
Main

echo.
echo Program finished.
pause
@echo off
echo ========================================
echo   Fix Maven Build Issues
echo ========================================
echo.

echo 1. Xoa target folder...
if exist target rmdir /s /q target
echo Done.

echo.
echo 2. Xoa Maven cache...
rmdir /s /q "%USERPROFILE%\.m2\repository\org\apache\maven\plugins\maven-compiler-plugin" 2>nul
echo Done.

echo.
echo 3. Clean Maven project...
call mvn clean
echo Done.

echo.
echo 4. Compile project...
call mvn compile -DskipTests
echo Done.

echo.
echo ========================================
echo   Neu van loi, thu chay:
echo   mvn clean compile -DskipTests -X
echo ========================================
pause


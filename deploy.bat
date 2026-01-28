@echo off
setlocal

set "TOMCAT_HOME=C:\xampp\tomcat"
set "TOMCAT_WEBAPPS=%TOMCAT_HOME%\webapps"
set "TEST_LIB=C:\Users\steve\Documents\S5\MrNaina\TEST\src\main\webapp\WEB-INF\lib"
set "JAR_FILE=target\SpringInit-1.jar"

echo ------------------------------
echo Compiling and installing into local Maven repository...
echo ------------------------------
call mvn -DskipTests clean install

echo mvn done, return : %ERRORLEVEL%

if errorlevel 1 (
    echo ERREUR : Maven compilation failed
    pause
    exit /b 1
)

if not exist "%JAR_FILE%" (
    echo ERREUR : JAR file not found : %JAR_FILE%
    pause
    exit /b 1
)

echo ------------------------------
echo Copying jar file to Test ...
echo ------------------------------

:: Suppression du jar dans Test si exist (supprime récursivement et silencieusement)
if exist "%TEST_LIB%" (
    rmdir /S /Q "%TEST_LIB%"
)
mkdir "%TEST_LIB%"

copy /Y "%JAR_FILE%" "%TEST_LIB%\"

if errorlevel 1 (
    echo ERREUR : La copie du JAR vers Test a échoué.
    pause
    exit /b 1
)

echo copy done successfully.

pause
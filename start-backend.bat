@echo off
cd /d %~dp0\backend
echo [OCMS] starting backend...
mvn spring-boot:run
pause

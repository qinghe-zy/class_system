@echo off
set DB_USER=%1
if "%DB_USER%"=="" set DB_USER=root
set DB_PASS=%2
if "%DB_PASS%"=="" set DB_PASS=123456
set DB_NAME=%3
if "%DB_NAME%"=="" set DB_NAME=ocms
mysql -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS `%DB_NAME%` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u%DB_USER% -p%DB_PASS% %DB_NAME% < %~dp0sql\schema.sql
mysql -u%DB_USER% -p%DB_PASS% %DB_NAME% < %~dp0sql\data.sql
echo [OCMS] database initialized: %DB_NAME%
pause

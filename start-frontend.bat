@echo off
cd /d %~dp0\frontend
if not exist node_modules (
  call npm install
)
echo [OCMS] starting frontend...
call npm run dev
pause

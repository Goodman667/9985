@echo off
setlocal
powershell -ExecutionPolicy Bypass -File "%~dp0restart-8080.ps1" %*
endlocal
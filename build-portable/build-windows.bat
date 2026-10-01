@echo off
rem Windows helper: build the portable package (see build-windows.ps1).
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-windows.ps1" %*
set "ERR=%ERRORLEVEL%"
if not "%ERR%"=="0" (
  echo.
  echo Build failed with exit code %ERR%.
  pause
)
exit /b %ERR%

@echo off
echo Building Minecraft 1.20.1...
powershell -ExecutionPolicy Bypass -File ..\build-version.ps1 -MinecraftVersion 1.20.1
echo.

echo Building Minecraft 1.20.2...
powershell -ExecutionPolicy Bypass -File ..\build-version.ps1 -MinecraftVersion 1.20.2
echo.

echo Building Minecraft 1.20.3...
powershell -ExecutionPolicy Bypass -File ..\build-version.ps1 -MinecraftVersion 1.20.3
echo.

echo All builds completed.
pause 
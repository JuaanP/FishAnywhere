# Script para elegir la versión de Minecraft
function Show-Menu {
    Clear-Host
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host "      CONSTRUCCIÓN DE MOD PARA MINECRAFT" -ForegroundColor Cyan
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Selecciona la versión de Minecraft para compilar:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  VERSIONES 1.20.x:" -ForegroundColor Magenta
    Write-Host "  1. Minecraft 1.20.1 (Java 17)" -ForegroundColor White
    Write-Host "  2. Minecraft 1.20.2 (Java 17)" -ForegroundColor White
    Write-Host "  3. Minecraft 1.20.3 (Java 17)" -ForegroundColor White
    Write-Host "  4. Minecraft 1.20.4 (Java 17)" -ForegroundColor White
    Write-Host "  5. Minecraft 1.20.6 (Java 17)" -ForegroundColor White
    Write-Host ""
    Write-Host "  VERSIONES 1.21.x:" -ForegroundColor Magenta
    Write-Host "  6. Minecraft 1.21.1 (Java 21)" -ForegroundColor White
    Write-Host "  7. Minecraft 1.21.2 (Java 21)" -ForegroundColor White
    Write-Host "  8. Minecraft 1.21.3 (Java 21)" -ForegroundColor White
    Write-Host "  9. Minecraft 1.21.4 (Java 21)" -ForegroundColor White
    Write-Host ""
    Write-Host "  OPCIONES ADICIONALES:" -ForegroundColor Magenta
    Write-Host "  A. Construir TODAS las versiones" -ForegroundColor Cyan
    Write-Host "  X. Salir" -ForegroundColor Red
    Write-Host ""
    Write-Host "==================================================" -ForegroundColor Cyan
}

function Execute-Build {
    param (
        [string]$scriptName
    )
    
    if (!(Test-Path $scriptName)) {
        Write-Host "Error: El script $scriptName no existe." -ForegroundColor Red
        return
    }
    
    Write-Host "Ejecutando $scriptName..." -ForegroundColor Cyan
    & .\$scriptName
    
    Write-Host "Presiona cualquier tecla para volver al menú principal..." -ForegroundColor Yellow
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

function Build-All {
    Write-Host "Construyendo todas las versiones..." -ForegroundColor Magenta
    
    # Construir todas las versiones en orden
    & .\build-1.20.1.ps1
    & .\build-1.20.2.ps1
    & .\build-1.20.3.ps1
    & .\build-1.20.4.ps1
    & .\build-1.20.6.ps1
    & .\build-1.21.1.ps1
    & .\build-1.21.2.ps1
    & .\build-1.21.3.ps1
    & .\build-1.21.4.ps1
    
    Write-Host "`nTodas las versiones han sido construidas." -ForegroundColor Green
    
    # Mostrar resumen
    Write-Host "`nResumen de archivos generados:" -ForegroundColor Yellow
    $buildFolders = Get-ChildItem -Path "builds" -Directory
    foreach ($folder in $buildFolders) {
        $jarCount = (Get-ChildItem -Path $folder.FullName -Filter "*.jar").Count
        if ($jarCount -gt 0) {
            Write-Host "  ✅ $($folder.Name): $jarCount archivos JAR" -ForegroundColor Green
        } else {
            Write-Host "  ❌ $($folder.Name): Sin archivos JAR" -ForegroundColor Red
        }
    }
    
    Write-Host "`nPresiona cualquier tecla para volver al menú principal..." -ForegroundColor Yellow
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

# Bucle principal del menú
do {
    Show-Menu
    $choice = Read-Host "Ingresa tu elección"
    
    switch ($choice) {
        "1" { Execute-Build "build-1.20.1.ps1" }
        "2" { Execute-Build "build-1.20.2.ps1" }
        "3" { Execute-Build "build-1.20.3.ps1" }
        "4" { Execute-Build "build-1.20.4.ps1" }
        "5" { Execute-Build "build-1.20.6.ps1" }
        "6" { Execute-Build "build-1.21.1.ps1" }
        "7" { Execute-Build "build-1.21.2.ps1" }
        "8" { Execute-Build "build-1.21.3.ps1" }
        "9" { Execute-Build "build-1.21.4.ps1" }
        "a" { Build-All }
        "A" { Build-All }
        "x" { return }
        "X" { return }
        default { 
            Write-Host "Opción no válida. Presiona cualquier tecla para continuar..." -ForegroundColor Red
            $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
    }
} while ($true) 
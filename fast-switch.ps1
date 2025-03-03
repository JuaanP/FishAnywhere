# Script para cambiar rápidamente la versión sin recarga completa
function Show-FastVersionMenu {
    Clear-Host
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host "      CAMBIO RÁPIDO DE VERSIÓN DEL PROYECTO" -ForegroundColor Cyan
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Este script cambia solo los archivos mínimos necesarios" -ForegroundColor Yellow
    Write-Host "para la detección de sintaxis y paquetes en el IDE" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Selecciona la versión de Minecraft:" -ForegroundColor Yellow
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
    Write-Host "  X. Salir" -ForegroundColor Red
    Write-Host ""
    
    # Mostrar versión actual
    if (Test-Path "gradle.properties") {
        $currentVersion = (Get-Content "gradle.properties" | Select-String "minecraft_version=").ToString().Split("=")[1]
        Write-Host ""
        Write-Host "Versión actual: $currentVersion" -ForegroundColor Green
    }
}

function Fast-SwitchVersion {
    param (
        [string]$version,
        [string]$javaVersion
    )
    
    Write-Host "Cambiando rápidamente a Minecraft $version..." -ForegroundColor Cyan
    
    # Obtener las versiones adecuadas según la versión de Minecraft
    switch -Regex ($version) {
        "1\.20\.1" {
            $fabricVersion = "0.83.0+1.20.1"
            $fabricLoaderVersion = "0.14.21"
            $forgeVersion = "47.1.0"
            $neoforgeVersion = "47.1.0"
            $neoforgeForm = "1.20.1-47.1.0"
            $modmenuVersion = "7.2.1"
            $disableNeoForge = $true
        }
        "1\.20\.2" {
            $fabricVersion = "0.91.1+1.20.2"
            $fabricLoaderVersion = "0.15.0"
            $forgeVersion = "48.1.0"
            $neoforgeVersion = "20.2.59"
            $neoforgeForm = "1.20.2-20240627.114801"
            $modmenuVersion = "8.0.0"
            $disableNeoForge = $false
        }
        "1\.20\.3" {
            $fabricVersion = "0.91.1+1.20.3"
            $fabricLoaderVersion = "0.15.0"
            $forgeVersion = "49.0.3"
            $neoforgeVersion = "20.3.9"
            $neoforgeForm = "1.20.3-20240627.114801"
            $modmenuVersion = "9.0.0"
            $disableNeoForge = $false
        }
        "1\.20\.4" {
            $fabricVersion = "0.97.2+1.20.4"
            $fabricLoaderVersion = "0.16.10"
            $forgeVersion = "49.1.37"
            $neoforgeVersion = "20.4.239"
            $neoforgeForm = "1.20.4-20240627.114801"
            $modmenuVersion = "9.2.0"
            $disableNeoForge = $false
        }
        "1\.20\.6" {
            $fabricVersion = "0.97.2+1.20.6"
            $fabricLoaderVersion = "0.16.10"
            $forgeVersion = "49.2.14"
            $neoforgeVersion = "20.6.36"
            $neoforgeForm = "1.20.6-20240627.114801"
            $modmenuVersion = "9.2.0"
            $disableNeoForge = $false
        }
        "1\.21\.1" {
            $fabricVersion = "0.92.0+1.21.1"
            $fabricLoaderVersion = "0.16.10"
            $forgeVersion = "52.0.1"
            $neoforgeVersion = "21.1.15"
            $neoforgeForm = "1.21.1-20240627.114801"
            $modmenuVersion = "10.0.0"
            $disableNeoForge = $false
        }
        "1\.21\.2" {
            $fabricVersion = "0.92.0+1.21.2"
            $fabricLoaderVersion = "0.16.10"
            $forgeVersion = "52.0.43"
            $neoforgeVersion = "21.2.0"
            $neoforgeForm = "1.21.2-20240808.144430"
            $modmenuVersion = "10.0.0"
            $disableNeoForge = $false
        }
        "1\.21\.3" {
            $fabricVersion = "0.93.1+1.21.3"
            $fabricLoaderVersion = "0.16.10"
            $forgeVersion = "52.0.63"
            $neoforgeVersion = "21.3.0"
            $neoforgeForm = "1.21.3-20240808.144430"
            $modmenuVersion = "11.0.3"
            $disableNeoForge = $false
        }
        "1\.21\.4" {
            $fabricVersion = "0.93.3+1.21.4"
            $fabricLoaderVersion = "0.16.10"
            $forgeVersion = "52.0.70"
            $neoforgeVersion = "21.4.5"
            $neoforgeForm = "1.21.4-20240808.144430"
            $modmenuVersion = "11.0.3"
            $disableNeoForge = $false
        }
        default {
            Write-Host "No hay configuración específica para la versión $version" -ForegroundColor Yellow
            return
        }
    }
    
    Write-Host "✓ Obtenidas configuraciones para Minecraft $version" -ForegroundColor Green
    
    # Actualizar solo los archivos mínimos necesarios
    # 1. Modificar las líneas relevantes en gradle.properties sin reescribir todo el archivo
    Write-Host "Actualizando gradle.properties..." -ForegroundColor Yellow
    $propContent = Get-Content "gradle.properties"
    $propContent = $propContent -replace "minecraft_version=.+", "minecraft_version=$version"
    $propContent = $propContent -replace "minecraft_version_range=\[.+\)", "minecraft_version_range=[$version, 1.22)"
    $propContent = $propContent -replace "java_version=.+", "java_version=$javaVersion"
    $propContent = $propContent -replace "fabric_version=.+", "fabric_version=$fabricVersion"
    $propContent = $propContent -replace "fabric_loader_version=.+", "fabric_loader_version=$fabricLoaderVersion"
    $propContent = $propContent -replace "forge_version=.+", "forge_version=$forgeVersion"
    $propContent = $propContent -replace "neoforge_version=.+", "neoforge_version=$neoforgeVersion"
    $propContent = $propContent -replace "neo_form_version=.+", "neo_form_version=$neoforgeForm"
    $propContent = $propContent -replace "modmenu_version=.+", "modmenu_version=$modmenuVersion"
    $propContent | Set-Content "gradle.properties"
    Write-Host "✓ Actualizado gradle.properties" -ForegroundColor Green
    
    # 2. Actualizar settings.gradle si es necesario
    if ($disableNeoForge) {
        Write-Host "Actualizando settings.gradle (desactivando NeoForge)..." -ForegroundColor Yellow
        $settingsContent = Get-Content "settings.gradle" -Raw
        $settingsWithoutNeoForge = $settingsContent -replace "include 'neoforge'", "// include 'neoforge' // Desactivado para $version"
        $settingsWithoutNeoForge | Set-Content "settings.gradle"
        Write-Host "✓ NeoForge desactivado en settings.gradle" -ForegroundColor Green
    } else {
        Write-Host "Actualizando settings.gradle (activando NeoForge)..." -ForegroundColor Yellow
        $settingsContent = Get-Content "settings.gradle" -Raw
        $settingsWithAllPlatforms = $settingsContent -replace "// include 'neoforge' // Desactivado .+", "include 'neoforge'"
        $settingsWithAllPlatforms | Set-Content "settings.gradle"
        Write-Host "✓ NeoForge activado en settings.gradle" -ForegroundColor Green
    }
    
    # 3. Opcional: Eliminar solo los archivos de caché mínimos necesarios para forzar la actualización
    Write-Host "Eliminando archivos de caché específicos para acelerar detección de sintaxis..." -ForegroundColor Yellow
    Remove-Item -Path ".idea/modules/common.main.iml" -ErrorAction SilentlyContinue
    Remove-Item -Path ".idea/modules/fabric.main.iml" -ErrorAction SilentlyContinue 
    Remove-Item -Path ".idea/modules/forge.main.iml" -ErrorAction SilentlyContinue
    Remove-Item -Path ".idea/modules/neoforge.main.iml" -ErrorAction SilentlyContinue
    Write-Host "✓ Limpieza de caché selectiva completada" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "¡Cambio rápido de versión completado!" -ForegroundColor Green
    Write-Host "Ahora puedes usar 'Refresh Gradle Project' en tu IDE (más rápido que recargar todo)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "NOTA: Este método rápido actualiza el detector de sintaxis y paquetes, pero" -ForegroundColor Cyan
    Write-Host "      algunas características avanzadas del IDE podrían requerir recarga completa" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Presiona cualquier tecla para continuar..." -ForegroundColor Yellow
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

# Bucle principal del menú
do {
    Show-FastVersionMenu
    $choice = Read-Host "Ingresa tu elección"
    
    switch ($choice.ToUpper()) {
        "1" { Fast-SwitchVersion "1.20.1" "17" }
        "2" { Fast-SwitchVersion "1.20.2" "17" }
        "3" { Fast-SwitchVersion "1.20.3" "17" }
        "4" { Fast-SwitchVersion "1.20.4" "17" }
        "5" { Fast-SwitchVersion "1.20.6" "17" }
        "6" { Fast-SwitchVersion "1.21.1" "21" }
        "7" { Fast-SwitchVersion "1.21.2" "21" }
        "8" { Fast-SwitchVersion "1.21.3" "21" }
        "9" { Fast-SwitchVersion "1.21.4" "21" }
        "X" { return }
        default { 
            Write-Host "Opción no válida. Presiona cualquier tecla para continuar..." -ForegroundColor Red
            $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
    }
} while ($true) 
# Script para cambiar la versión de Minecraft del proyecto
function Show-VersionMenu {
    Clear-Host
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host "      CAMBIO DE VERSIÓN DEL PROYECTO" -ForegroundColor Cyan
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Selecciona la versión de Minecraft para editar:" -ForegroundColor Yellow
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
    Write-Host "  B. Hacer backup de la configuración actual" -ForegroundColor White
    Write-Host "  R. Restaurar backup de configuración" -ForegroundColor White
    Write-Host "  C. Limpiar proyecto (./gradlew clean)" -ForegroundColor White
    Write-Host "  X. Salir" -ForegroundColor Red
    Write-Host ""
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host "NOTA: Esto solo cambiará la configuración para desarrollo" -ForegroundColor Yellow
    Write-Host "      No compilará el proyecto" -ForegroundColor Yellow
    
    # Mostrar versión actual
    if (Test-Path "gradle.properties") {
        $currentVersion = (Get-Content "gradle.properties" | Select-String "minecraft_version=").ToString().Split("=")[1]
        Write-Host ""
        Write-Host "Versión actual: $currentVersion" -ForegroundColor Green
    }
}

function Backup-Configuration {
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupFolder = "config_backups/$timestamp"
    
    if (!(Test-Path "config_backups" -PathType Container)) {
        New-Item -ItemType Directory -Path "config_backups" -Force | Out-Null
    }
    
    if (!(Test-Path $backupFolder -PathType Container)) {
        New-Item -ItemType Directory -Path $backupFolder -Force | Out-Null
    }
    
    # Hacer copia de los archivos de configuración
    if (Test-Path "gradle.properties") {
        Copy-Item "gradle.properties" -Destination "$backupFolder/gradle.properties"
    }
    
    if (Test-Path "settings.gradle") {
        Copy-Item "settings.gradle" -Destination "$backupFolder/settings.gradle"
    }
    
    if (Test-Path "build.gradle") {
        Copy-Item "build.gradle" -Destination "$backupFolder/build.gradle"
    }
    
    if (Test-Path "gradle/versions.toml") {
        if (!(Test-Path "$backupFolder/gradle" -PathType Container)) {
            New-Item -ItemType Directory -Path "$backupFolder/gradle" -Force | Out-Null
        }
        Copy-Item "gradle/versions.toml" -Destination "$backupFolder/gradle/versions.toml"
    }
    
    Write-Host "Backup guardado en: $backupFolder" -ForegroundColor Green
    Write-Host "Presiona cualquier tecla para continuar..." -ForegroundColor Yellow
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

function Restore-Configuration {
    if (!(Test-Path "config_backups" -PathType Container)) {
        Write-Host "No hay backups disponibles" -ForegroundColor Red
        Write-Host "Presiona cualquier tecla para continuar..." -ForegroundColor Yellow
        $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        return
    }
    
    $backupFolders = Get-ChildItem -Path "config_backups" -Directory | Sort-Object -Property Name -Descending
    
    if ($backupFolders.Count -eq 0) {
        Write-Host "No hay backups disponibles" -ForegroundColor Red
        Write-Host "Presiona cualquier tecla para continuar..." -ForegroundColor Yellow
        $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        return
    }
    
    Clear-Host
    Write-Host "Selecciona un backup para restaurar:" -ForegroundColor Yellow
    
    for ($i = 0; $i -lt [Math]::Min($backupFolders.Count, 10); $i++) {
        $folder = $backupFolders[$i]
        $formattedDate = [DateTime]::ParseExact($folder.Name.Split("_")[0], "yyyyMMdd", $null).ToString("dd/MM/yyyy")
        $formattedTime = $folder.Name.Split("_")[1].Insert(2, ":").Insert(5, ":")
        Write-Host "  $($i+1). Backup del $formattedDate a las $formattedTime" -ForegroundColor White
    }
    
    Write-Host "  0. Cancelar" -ForegroundColor Red
    
    $choice = Read-Host "Ingresa el número del backup"
    
    if ($choice -eq "0") {
        return
    }
    
    if ([int]$choice -gt 0 -and [int]$choice -le $backupFolders.Count) {
        $selectedFolder = $backupFolders[[int]$choice - 1].FullName
        
        # Restaurar archivos
        if (Test-Path "$selectedFolder/gradle.properties") {
            Copy-Item "$selectedFolder/gradle.properties" -Destination "gradle.properties" -Force
        }
        
        if (Test-Path "$selectedFolder/settings.gradle") {
            Copy-Item "$selectedFolder/settings.gradle" -Destination "settings.gradle" -Force
        }
        
        if (Test-Path "$selectedFolder/build.gradle") {
            Copy-Item "$selectedFolder/build.gradle" -Destination "build.gradle" -Force
        }
        
        if (Test-Path "$selectedFolder/gradle/versions.toml") {
            Copy-Item "$selectedFolder/gradle/versions.toml" -Destination "gradle/versions.toml" -Force
        }
        
        Write-Host "Configuración restaurada correctamente" -ForegroundColor Green
    } else {
        Write-Host "Opción no válida" -ForegroundColor Red
    }
    
    Write-Host "Presiona cualquier tecla para continuar..." -ForegroundColor Yellow
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

function Clean-Project {
    Write-Host "Limpiando proyecto..." -ForegroundColor Yellow
    & .\gradlew.bat clean --configure-on-demand
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Proyecto limpiado correctamente" -ForegroundColor Green
    } else {
        Write-Host "Error al limpiar el proyecto" -ForegroundColor Red
    }
    
    Write-Host "Presiona cualquier tecla para continuar..." -ForegroundColor Yellow
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

function Switch-Version {
    param (
        [string]$version,
        [string]$javaVersion
    )
    
    # Hacer backup automático antes de cambiar
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupFolder = "config_backups/auto_$timestamp"
    
    if (!(Test-Path "config_backups" -PathType Container)) {
        New-Item -ItemType Directory -Path "config_backups" -Force | Out-Null
    }
    
    if (!(Test-Path $backupFolder -PathType Container)) {
        New-Item -ItemType Directory -Path $backupFolder -Force | Out-Null
    }
    
    # Hacer copia de los archivos de configuración
    if (Test-Path "gradle.properties") {
        Copy-Item "gradle.properties" -Destination "$backupFolder/gradle.properties"
    }
    
    if (Test-Path "settings.gradle") {
        Copy-Item "settings.gradle" -Destination "$backupFolder/settings.gradle"
    }
    
    Write-Host "Backup automático guardado en: $backupFolder" -ForegroundColor Gray
    Write-Host "Cambiando a Minecraft $version (Java $javaVersion)..." -ForegroundColor Cyan
    
    $scriptName = "set-version-$version.ps1"
    if (Test-Path $scriptName) {
        & .\$scriptName
    } else {
        # Crear el script de configuración al vuelo ya que no existe
        $script = @"
# Script para cambiar la configuración del proyecto a Minecraft $version
`$version = "$version"
`$javaVersion = "$javaVersion"

Write-Host "Configurando proyecto para Minecraft `$version" -ForegroundColor Cyan

# Verificar que existe el JDK correcto
`$jdkPath = "C:/Program Files/Java/jdk-`$javaVersion"
if (!(Test-Path `$jdkPath)) {
    Write-Host "ADVERTENCIA: No se encuentra el JDK `$javaVersion en `$jdkPath" -ForegroundColor Yellow
    if (`$javaVersion -eq "21") {
        Write-Host "Intentando con JDK 17..." -ForegroundColor Yellow
        `$jdkPath = "C:/Program Files/Java/jdk-17"
        if (!(Test-Path `$jdkPath)) {
            Write-Host "ADVERTENCIA: Tampoco se encuentra JDK 17" -ForegroundColor Red
        } else {
            Write-Host "Usando JDK 17, pero se recomienda JDK 21 para Minecraft 1.21.x" -ForegroundColor Yellow
        }
    }
    Write-Host "El proyecto podría no funcionar correctamente en el IDE" -ForegroundColor Yellow
}
`$gradleJavaHome = `$jdkPath.Replace("\", "/")

# Configuración específica para cada versión
"@

        # Configurar propiedades específicas según versión
        $fabricVersion = ""
        $fabricLoaderVersion = "0.16.10"
        $forgeVersion = ""
        $neoforgeVersion = ""
        $neoforgeForm = ""
        $modmenuVersion = ""
        $enableNeoforge = $true
        
        # Configurar valores específicos basados en la versión
        switch -Regex ($version) {
            "1\.20\.1" {
                $fabricVersion = "0.83.0+1.20.1"
                $fabricLoaderVersion = "0.14.21"
                $forgeVersion = "47.1.0"
                $neoforgeVersion = "47.1.0"
                $neoforgeForm = "1.20.1-47.1.0"
                $modmenuVersion = "7.2.1"
                $enableNeoforge = $false
            }
            "1\.20\.2" {
                $fabricVersion = "0.91.1+1.20.2"
                $forgeVersion = "48.1.0"
                $neoforgeVersion = "20.2.59"
                $neoforgeForm = "1.20.2-20240627.114801"
                $modmenuVersion = "8.0.0"
            }
            "1\.20\.3" {
                $fabricVersion = "0.91.1+1.20.3"
                $forgeVersion = "49.0.3"
                $neoforgeVersion = "20.3.9"
                $neoforgeForm = "1.20.3-20240627.114801"
                $modmenuVersion = "9.0.0"
            }
            "1\.20\.4" {
                $fabricVersion = "0.97.2+1.20.4"
                $forgeVersion = "49.1.37"
                $neoforgeVersion = "20.4.239"
                $neoforgeForm = "1.20.4-20240627.114801"
                $modmenuVersion = "9.2.0"
            }
            "1\.20\.6" {
                $fabricVersion = "0.97.2+1.20.6"
                $forgeVersion = "49.2.14"
                $neoforgeVersion = "20.6.36"
                $neoforgeForm = "1.20.6-20240627.114801"
                $modmenuVersion = "9.2.0"
            }
            "1\.21\.1" {
                $fabricVersion = "0.92.0+1.21.1"
                $forgeVersion = "52.0.1"
                $neoforgeVersion = "21.1.15"
                $neoforgeForm = "1.21.1-20240627.114801"
                $modmenuVersion = "10.0.0"
            }
            "1\.21\.2" {
                $fabricVersion = "0.92.0+1.21.2"
                $forgeVersion = "52.0.43"
                $neoforgeVersion = "21.2.0"
                $neoforgeForm = "1.21.2-20240808.144430"
                $modmenuVersion = "10.0.0"
            }
            "1\.21\.3" {
                $fabricVersion = "0.93.1+1.21.3"
                $forgeVersion = "52.0.63"
                $neoforgeVersion = "21.3.0"
                $neoforgeForm = "1.21.3-20240808.144430"
                $modmenuVersion = "11.0.3"
            }
            "1\.21\.4" {
                $fabricVersion = "0.93.3+1.21.4"
                $forgeVersion = "52.0.70"
                $neoforgeVersion = "21.4.5"
                $neoforgeForm = "1.21.4-20240808.144430"
                $modmenuVersion = "11.0.3"
            }
            default {
                Write-Host "No hay configuración específica para la versión $version" -ForegroundColor Yellow
                $fabricVersion = "0.93.3+$version"
                $forgeVersion = "52.0.70"
                $neoforgeVersion = "21.4.5"
                $neoforgeForm = "$version-20240808.144430"
                $modmenuVersion = "11.0.3"
            }
        }
        
        # Agregar la configuración al script
        $script += @"

# Actualizar gradle.properties
`$gradleProps = @"
# [Project]
version=1.2.0
group=com.juaanp.seamlesstrading

# [Common]
mod_name=SeamlessTrading
mod_author=JuaanP
mod_id=seamlesstrading
license=MIT
credits=
description=New trade offers appear instantly as soon as the villager xp progress bar fills. No more level up and trade refresh delay.

minecraft_version=`$version
minecraft_version_range=[`$version, 1.22)
java_version=`$javaVersion

# Fabric
fabric_version=$fabricVersion
fabric_loader_version=$fabricLoaderVersion

# Forge
forge_version=$forgeVersion
forge_loader_version_range=[4,)

# NeoForge
neoforge_version=$neoforgeVersion
neo_form_version=$neoforgeForm
neoforge_loader_version_range=[1,)

# ModMenu
modmenu_version=$modmenuVersion

# Gradle - Configuraciones optimizadas para builds rápidas
org.gradle.jvmargs=-Xmx4G -XX:MaxMetaspaceSize=1G -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=false
org.gradle.java.home=`$gradleJavaHome
org.gradle.configureondemand=true

# Configuraciones para Minecraft mods (optimiza la compilación)
fabric.loom.multiProjectOptimisation=true
loom.platform=forge
generate_sources=false
generate_javadocs=false
"@

`$gradleProps | Set-Content "gradle.properties"

# Ajustar settings.gradle según plataformas disponibles
"@

        # Agregar configuración de settings.gradle
        if ($enableNeoforge) {
            $script += @"
# Restaurar settings.gradle para incluir todas las plataformas
`$settingsContent = Get-Content "settings.gradle" -Raw
`$settingsWithAllPlatforms = `$settingsContent -replace "// include 'neoforge' // Desactivado .+", "include 'neoforge'"
`$settingsWithAllPlatforms | Set-Content "settings.gradle"
"@
        } else {
            $script += @"
# Modificar settings.gradle para quitar NeoForge (no disponible para `$version)
`$settingsContent = Get-Content "settings.gradle" -Raw
`$settingsWithoutNeoForge = `$settingsContent -replace "include 'neoforge'", "// include 'neoforge' // Desactivado para `$version"
`$settingsWithoutNeoForge | Set-Content "settings.gradle"
"@
        }
        
        # También actualizar los archivos versions.toml si existe
        $script += @"

# Actualizar versions.toml si existe
if (Test-Path "gradle/versions.toml") {
    Write-Host "Actualizando gradle/versions.toml..." -ForegroundColor Yellow
    
    # Leer el archivo versions.toml
    `$versionsToml = Get-Content "gradle/versions.toml" -Raw
    
    # Actualizar la versión de Minecraft
    `$versionsToml = `$versionsToml -replace '(?m)^minecraft = ".+"', "minecraft = "`$version""
    
    # Actualizar la versión de Java
    `$versionsToml = `$versionsToml -replace '(?m)^java = ".+"', "java = "`$javaVersion""
    
    # Actualizar las versiones de los loaders
    `$versionsToml = `$versionsToml -replace '(?m)^fabric-api = ".+"', "fabric-api = "$fabricVersion""
    `$versionsToml = `$versionsToml -replace '(?m)^fabric-loader = ".+"', "fabric-loader = "$fabricLoaderVersion""
    `$versionsToml = `$versionsToml -replace '(?m)^forge = ".+"', "forge = "$forgeVersion""
    `$versionsToml = `$versionsToml -replace '(?m)^neoforge = ".+"', "neoforge = "$neoforgeVersion""
    
    # Guardar el archivo actualizado
    `$versionsToml | Set-Content "gradle/versions.toml"
}
"@
        
        # Finalizar script
        $script += @"

Write-Host "Proyecto configurado para Minecraft `$version!" -ForegroundColor Green
Write-Host "Recuerda recargar el proyecto en tu IDE para aplicar los cambios." -ForegroundColor Yellow
"@

        # Guardar y ejecutar el script
        $script | Set-Content $scriptName
        & .\$scriptName
    }
    
    Write-Host "¿Deseas limpiar el proyecto ahora? (S/N)" -ForegroundColor Yellow
    $cleanChoice = Read-Host
    if ($cleanChoice -eq "S" -or $cleanChoice -eq "s") {
        Clean-Project
    } else {
        Write-Host "Presiona cualquier tecla para volver al menú principal..." -ForegroundColor Yellow
        $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
}

# Bucle principal del menú
do {
    Show-VersionMenu
    $choice = Read-Host "Ingresa tu elección"
    
    switch ($choice.ToUpper()) {
        "1" { Switch-Version "1.20.1" "17" }
        "2" { Switch-Version "1.20.2" "17" }
        "3" { Switch-Version "1.20.3" "17" }
        "4" { Switch-Version "1.20.4" "17" }
        "5" { Switch-Version "1.20.6" "17" }
        "6" { Switch-Version "1.21.1" "21" }
        "7" { Switch-Version "1.21.2" "21" }
        "8" { Switch-Version "1.21.3" "21" }
        "9" { Switch-Version "1.21.4" "21" }
        "B" { Backup-Configuration }
        "R" { Restore-Configuration }
        "C" { Clean-Project }
        "X" { return }
        default { 
            Write-Host "Opción no válida. Presiona cualquier tecla para continuar..." -ForegroundColor Red
            $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
    }
} while ($true) 
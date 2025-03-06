param(
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion
)

# Ruta al directorio raíz del proyecto
$projectRoot = Join-Path $PSScriptRoot ".."

# Leer el archivo properties.toml
$versionsToml = Get-Content (Join-Path $PSScriptRoot "properties.toml") -Raw

# Convertir la versión de Minecraft al formato usado en properties.toml (1.20.4 -> 1_20_4)
$versionKey = $MinecraftVersion.Replace(".", "_")

# Obtener la versión de Java específica para esta versión de Minecraft
$javaVersion = [regex]::Match($versionsToml, "java-$versionKey\s*=\s*`"([^`"]*)`"").Groups[1].Value

if ([string]::IsNullOrEmpty($javaVersion)) {
    Write-Host "Error: No se encontró la versión de Java para Minecraft $MinecraftVersion" -ForegroundColor Red
    exit 1
}

Write-Host "Configurando proyecto para Minecraft $MinecraftVersion con Java $javaVersion..." -ForegroundColor Cyan

# Script genérico para cambiar la configuración del proyecto a cualquier versión de Minecraft
Write-Host "Configurando proyecto para Minecraft $MinecraftVersion" -ForegroundColor Cyan

# Normalizar la versión para buscar en el archivo TOML (1.20.1 -> 1_20_1)
$normalizedVersion = $MinecraftVersion -replace '\.', '_'

# Leer el archivo properties.toml
if (!(Test-Path "$PSScriptRoot\properties.toml")) {
    Write-Host "Error: No se encuentra el archivo properties.toml" -ForegroundColor Red
    exit 1
}
$versionsToml = Get-Content "$PSScriptRoot\properties.toml" -Raw

# Extraer propiedades del proyecto y del mod
$projectVersion = [regex]::Match($versionsToml, "\[project\][\s\S]*?version\s*=\s*`"([^`"]*)`"").Groups[1].Value
$projectGroup = [regex]::Match($versionsToml, "\[project\][\s\S]*?group\s*=\s*`"([^`"]*)`"").Groups[1].Value

$modName = [regex]::Match($versionsToml, "\[mod\][\s\S]*?name\s*=\s*`"([^`"]*)`"").Groups[1].Value
$modAuthor = [regex]::Match($versionsToml, "\[mod\][\s\S]*?author\s*=\s*`"([^`"]*)`"").Groups[1].Value
$modId = [regex]::Match($versionsToml, "\[mod\][\s\S]*?id\s*=\s*`"([^`"]*)`"").Groups[1].Value
$modLicense = [regex]::Match($versionsToml, "\[mod\][\s\S]*?license\s*=\s*`"([^`"]*)`"").Groups[1].Value
$modCredits = [regex]::Match($versionsToml, "\[mod\][\s\S]*?credits\s*=\s*`"([^`"]*)`"").Groups[1].Value
$modDescription = [regex]::Match($versionsToml, "\[mod\][\s\S]*?description\s*=\s*`"([^`"]*)`"").Groups[1].Value

# Verificar que la versión existe en el archivo TOML
$minecraftVersionCheck = [regex]::Match($versionsToml, "minecraft-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
if ($minecraftVersionCheck -eq "") {
    Write-Host "Error: La versión $MinecraftVersion no está definida en properties.toml" -ForegroundColor Red
    exit 1
}

# Extraer valores usando expresiones regulares
$minecraft = $MinecraftVersion
$minecraftRange = [regex]::Match($versionsToml, "minecraft-range-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value

# Extraer versiones específicas de cada loader
$fabricApi = [regex]::Match($versionsToml, "fabric-api-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
$fabricLoader = [regex]::Match($versionsToml, "fabric-loader\s*=\s*`"([^`"]*)`"").Groups[1].Value

$forge = [regex]::Match($versionsToml, "forge-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
$forgeLoaderRange = [regex]::Match($versionsToml, "forge-loader-range\s*=\s*`"([^`"]*)`"").Groups[1].Value

# Comprobar si NeoForge está disponible para esta versión
$neoforge = [regex]::Match($versionsToml, "neoforge-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
$neoformVersion = [regex]::Match($versionsToml, "neo-form-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
$neoforgeLoaderRange = [regex]::Match($versionsToml, "neoforge-loader-range\s*=\s*`"([^`"]*)`"").Groups[1].Value

# Si es 1.20.1, usar valores de 1.20.2 para NeoForge (no disponible para 1.20.1)
$neoforgeSupported = $true
if ($minecraft -eq "1.20.1" -or $neoforge -eq "") {
    if ($minecraft -eq "1.20.1") {
        $neoforge = [regex]::Match($versionsToml, "neoforge-1_20_2\s*=\s*`"([^`"]*)`"").Groups[1].Value
        $neoformVersion = [regex]::Match($versionsToml, "neo-form-1_20_2\s*=\s*`"([^`"]*)`"").Groups[1].Value
        Write-Host "NeoForge no está disponible para 1.20.1, usando valores de 1.20.2" -ForegroundColor Yellow
        $neoforgeSupported = $false
    } else {
        Write-Host "No se encontraron valores de NeoForge para la versión $minecraft" -ForegroundColor Yellow
        $neoforgeSupported = $false
    }
}

$modmenu = [regex]::Match($versionsToml, "modmenu-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value

# Verificar que existe el JDK correcto
$jdkPath = "C:/Program Files/Java/jdk-$javaVersion"
if (!(Test-Path $jdkPath)) {
    Write-Host "ADVERTENCIA: No se encuentra el JDK $javaVersion en $jdkPath" -ForegroundColor Yellow
    Write-Host "El proyecto podría no funcionar correctamente en el IDE" -ForegroundColor Yellow
}
$gradleJavaHome = $jdkPath.Replace("\", "/")

# Actualizar gradle.properties en la carpeta raíz del proyecto
$gradleProps = @"
# [Project]
version=$projectVersion
group=$projectGroup

# [Common]
mod_name=$modName
mod_author=$modAuthor
mod_id=$modId
license=$modLicense
credits=$modCredits
description=$modDescription

minecraft_version=$minecraft
minecraft_version_range=$minecraftRange
java_version=$javaVersion

# Fabric
fabric_version=$fabricApi
fabric_loader_version=$fabricLoader

# Forge
forge_version=$forge
forge_loader_version_range=$forgeLoaderRange

# NeoForge
neoforge_version=$neoforge
neo_form_version=$neoformVersion
neoforge_loader_version_range=$neoforgeLoaderRange

# ModMenu
modmenu_version=$modmenu

# Gradle
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false
org.gradle.java.home=C:/Program Files/Java/jdk-$javaVersion
"@

$gradleProps | Set-Content "$PSScriptRoot\..\gradle.properties"

# Modificar settings.gradle según si NeoForge está soportado o no
$settingsContent = Get-Content "$PSScriptRoot\..\settings.gradle" -Raw
if ($neoforgeSupported) {
    # Asegurarse de que NeoForge está habilitado
    $updatedSettings = $settingsContent -replace "// include 'neoforge'.+", "include 'neoforge'"
    $updatedSettings | Set-Content "$PSScriptRoot\..\settings.gradle"
    Write-Host "NeoForge habilitado en settings.gradle" -ForegroundColor Green
} else {
    # Desactivar NeoForge
    $updatedSettings = $settingsContent -replace "include 'neoforge'", "// include 'neoforge' // Desactivado para $minecraft"
    $updatedSettings | Set-Content "$PSScriptRoot\..\settings.gradle"
    Write-Host "NeoForge desactivado en settings.gradle para $minecraft" -ForegroundColor Yellow
}

Write-Host "Proyecto configurado para Minecraft $minecraft!" -ForegroundColor Green
Write-Host "Valores extraídos de properties.toml:" -ForegroundColor Cyan
Write-Host "  - Proyecto: $modName v$projectVersion" -ForegroundColor White
Write-Host "  - Java: $javaVersion" -ForegroundColor White
Write-Host "  - Fabric: $fabricApi (loader $fabricLoader)" -ForegroundColor White
Write-Host "  - Forge: $forge" -ForegroundColor White

if ($neoforgeSupported) {
    Write-Host "  - NeoForge: $neoforge" -ForegroundColor White
} else {
    if ($minecraft -eq "1.20.1") {
        Write-Host "  - NeoForge: $neoforge (usando valores de 1.20.2, pero desactivado)" -ForegroundColor Yellow
    } else {
        Write-Host "  - NeoForge: No soportado para esta versión" -ForegroundColor Yellow
    }
}

Write-Host "  - ModMenu: $modmenu" -ForegroundColor White
Write-Host "Recuerda recargar el proyecto en tu IDE para aplicar los cambios." -ForegroundColor Yellow

# Guardar el último mensaje en un archivo temporal para que build-version.ps1 pueda verificar el éxito
"Proyecto configurado para Minecraft $minecraft!" | Out-File -FilePath "$env:TEMP\set-version-output.txt" -Force 
param(
    [Parameter(Mandatory=$true)]
    [string]$Version,
    
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion,
    
    [Parameter(Mandatory=$false)]
    [string[]]$Loaders = @(),
    
    [Parameter(Mandatory=$false)]
    [string[]]$Platforms = @(),
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("release", "beta", "alpha")]
    [string]$ReleaseType = "release"
)

# Ruta al directorio raiz del proyecto
$projectRoot = Join-Path $PSScriptRoot ".."

# Verificar que local.properties exista y tenga las claves necesarias
$localPropertiesPath = "$projectRoot\local.properties"
if (!(Test-Path $localPropertiesPath)) {
    Write-Host "Error: No se encuentra el archivo local.properties" -ForegroundColor Red
    Write-Host "Crea este archivo con tus tokens de API para Modrinth y CurseForge" -ForegroundColor Yellow
    exit 1
}

$localPropertiesContent = Get-Content $localPropertiesPath -Raw
if (!($localPropertiesContent -match "modrinth_token=") -or !($localPropertiesContent -match "curseforge_token=")) {
    Write-Host "Error: El archivo local.properties no contiene las claves API necesarias" -ForegroundColor Red
    Write-Host "Asegurate de que el archivo contenga modrinth_token y curseforge_token" -ForegroundColor Yellow
    exit 1
}

# Primero configurar el proyecto para la version especificada
Write-Host "Configurando proyecto para Minecraft $MinecraftVersion..." -ForegroundColor Cyan
& "$PSScriptRoot\set-version.ps1" -MinecraftVersion $MinecraftVersion

# Definir loaders disponibles
$availableLoaders = @("fabric", "forge", "neoforge")

# Si no se especificaron loaders, preguntar al usuario
if ($Loaders.Count -eq 0) {
    Write-Host "`nSelecciona los loaders a publicar:" -ForegroundColor Cyan
    Write-Host "1. Todos los loaders compatibles" -ForegroundColor White
    Write-Host "2. Fabric" -ForegroundColor White
    Write-Host "3. Forge" -ForegroundColor White
    Write-Host "4. NeoForge" -ForegroundColor White
    Write-Host "5. Seleccion personalizada" -ForegroundColor White
    
    $selection = Read-Host "`nIngresa el numero de tu seleccion"
    
    switch ($selection) {
        "1" { $Loaders = $availableLoaders }
        "2" { $Loaders = @("fabric") }
        "3" { $Loaders = @("forge") }
        "4" { $Loaders = @("neoforge") }
        "5" {
            $Loaders = @()
            $includeFabric = Read-Host "Incluir Fabric? (S/N)"
            if ($includeFabric -eq "S" -or $includeFabric -eq "s") {
                $Loaders += "fabric"
            }
            
            $includeForge = Read-Host "Incluir Forge? (S/N)"
            if ($includeForge -eq "S" -or $includeForge -eq "s") {
                $Loaders += "forge"
            }
            
            $includeNeoForge = Read-Host "Incluir NeoForge? (S/N)"
            if ($includeNeoForge -eq "S" -or $includeNeoForge -eq "s") {
                $Loaders += "neoforge"
            }
            
            if ($Loaders.Count -eq 0) {
                Write-Host "No seleccionaste ningun loader. Saliendo..." -ForegroundColor Yellow
                exit 0
            }
        }
        default {
            Write-Host "Seleccion no valida. Utilizando todos los loaders compatibles." -ForegroundColor Yellow
            $Loaders = $availableLoaders
        }
    }
}

# Filtrar loaders no compatibles
$enabledLoaders = $Loaders
if ($MinecraftVersion -eq "1.20.1" -and $enabledLoaders -contains "neoforge") {
    Write-Host "NeoForge no es compatible con Minecraft 1.20.1, se excluira de la publicacion." -ForegroundColor Yellow
    $enabledLoaders = $enabledLoaders | Where-Object { $_ -ne "neoforge" }
}

# Si no quedan loaders compatibles, salir
if ($enabledLoaders.Count -eq 0) {
    Write-Host "No hay loaders compatibles con Minecraft $MinecraftVersion para los seleccionados. Saliendo..." -ForegroundColor Red
    exit 1
}

# Definir plataformas disponibles
$availablePlatforms = @("modrinth", "curseforge")

# Si no se especificaron plataformas, preguntar al usuario
if ($Platforms.Count -eq 0) {
    Write-Host "`nSelecciona las plataformas donde publicar:" -ForegroundColor Cyan
    Write-Host "1. Todas las plataformas" -ForegroundColor White
    Write-Host "2. Modrinth" -ForegroundColor White
    Write-Host "3. CurseForge" -ForegroundColor White
    
    $platformSelection = Read-Host "`nIngresa el numero de tu seleccion"
    
    switch ($platformSelection) {
        "1" { $Platforms = $availablePlatforms }
        "2" { $Platforms = @("modrinth") }
        "3" { $Platforms = @("curseforge") }
        default {
            Write-Host "Seleccion no valida. Utilizando todas las plataformas." -ForegroundColor Yellow
            $Platforms = $availablePlatforms
        }
    }
}

# Mostrar informacion de publicacion
Write-Host "`n=========================================================" -ForegroundColor Cyan
Write-Host "PUBLICACION DE MOD" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "Version: $Version" -ForegroundColor White
Write-Host "Minecraft: $MinecraftVersion" -ForegroundColor White
Write-Host "Loaders: $($enabledLoaders -join ', ')" -ForegroundColor White
Write-Host "Plataformas: $($Platforms -join ', ')" -ForegroundColor White
Write-Host "Tipo de lanzamiento: $ReleaseType" -ForegroundColor White
Write-Host "=========================================================`n" -ForegroundColor Cyan

# Compilar y publicar para cada loader
Push-Location $projectRoot
try {
    foreach ($loader in $enabledLoaders) {
        Write-Host "`n=========================================================" -ForegroundColor Cyan
        Write-Host "Publicando $loader para Minecraft $MinecraftVersion" -ForegroundColor Cyan
        Write-Host "=========================================================`n" -ForegroundColor Cyan
        
        # Configurar propiedades de publicacion
        $gradleArgs = @(
            ":${loader}:build"
        )
        
        # Anadir tareas especificas de plataforma
        if ($Platforms -contains "modrinth") {
            $gradleArgs += ":${loader}:modrinth"
        }
        
        if ($Platforms -contains "curseforge") {
            $gradleArgs += ":${loader}:curseforge"
        }
        
        # Anadir parametros comunes
        $gradleArgs += "-Pversion=$Version"
        $gradleArgs += "-PreleaseType=$ReleaseType"
        
        # Ejecutar la publicacion
        & ./gradlew $gradleArgs --info
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Publicacion exitosa para $loader" -ForegroundColor Green
        } else {
            Write-Host "Error: La publicacion de $loader fallo" -ForegroundColor Red
        }
    }
} finally {
    Pop-Location
}

Write-Host "`nProceso de publicacion finalizado" -ForegroundColor Green 
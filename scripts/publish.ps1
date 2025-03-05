param(
    [Parameter(Mandatory=$true)]
    [string]$Version,
    
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion,
    
    [Parameter(Mandatory=$false)]
    [string[]]$Loaders = @("fabric", "forge", "neoforge"),
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("release", "beta", "alpha")]
    [string]$ReleaseType = "release"
)

# Ruta al directorio raíz del proyecto
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

# Primero configurar el proyecto para la versión especificada
Write-Host "Configurando proyecto para Minecraft $MinecraftVersion..." -ForegroundColor Cyan
& "$PSScriptRoot\set-version.ps1" -MinecraftVersion $MinecraftVersion

# Filtrar loaders no compatibles
$enabledLoaders = $Loaders
if ($MinecraftVersion -eq "1.20.1" -and $enabledLoaders -contains "neoforge") {
    Write-Host "NeoForge no es compatible con Minecraft 1.20.1, se excluira de la publicacion." -ForegroundColor Yellow
    $enabledLoaders = $enabledLoaders | Where-Object { $_ -ne "neoforge" }
}

# Mostrar información de publicación
Write-Host "`n=========================================================" -ForegroundColor Cyan
Write-Host "PUBLICACION DE MOD" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "Version: $Version" -ForegroundColor White
Write-Host "Minecraft: $MinecraftVersion" -ForegroundColor White
Write-Host "Loaders: $($enabledLoaders -join ', ')" -ForegroundColor White
Write-Host "Tipo de lanzamiento: $ReleaseType" -ForegroundColor White
Write-Host "=========================================================`n" -ForegroundColor Cyan

# Preguntar confirmación
$confirmation = Read-Host "¿Deseas continuar con la publicacion? (S/N)"
if ($confirmation -ne "S" -and $confirmation -ne "s") {
    Write-Host "Publicacion cancelada." -ForegroundColor Yellow
    exit 0
}

# Compilar y publicar para cada loader
Push-Location $projectRoot
try {
    foreach ($loader in $enabledLoaders) {
        Write-Host "`n=========================================================" -ForegroundColor Cyan
        Write-Host "Publicando $loader para Minecraft $MinecraftVersion" -ForegroundColor Cyan
        Write-Host "=========================================================`n" -ForegroundColor Cyan
        
        # Configurar propiedades de publicación
        $gradleArgs = @(
            ":${loader}:build",
            ":${loader}:modrinth",
            ":${loader}:curseforge",
            "-Pversion=$Version",
            "-PreleaseType=$ReleaseType"
        )
        
        # Ejecutar la publicación
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
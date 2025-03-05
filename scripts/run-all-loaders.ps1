param(
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion
)

# Ruta al directorio raíz del proyecto
$projectRoot = Join-Path $PSScriptRoot ".."

# Primero configurar el proyecto para la versión especificada
Write-Host "Configurando proyecto para Minecraft $MinecraftVersion..." -ForegroundColor Cyan
& "$PSScriptRoot\set-version.ps1" -MinecraftVersion $MinecraftVersion

# Verificar si set-version.ps1 se ejecutó correctamente
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: La configuración del proyecto falló. No se puede continuar." -ForegroundColor Red
    exit 1
}

# Leer settings.gradle para determinar qué loaders están habilitados
$settingsContent = Get-Content "$projectRoot\settings.gradle" -Raw
$enabledLoaders = @()

# Verificar cada loader
$allLoaders = @("fabric", "forge", "neoforge")
foreach ($loader in $allLoaders) {
    if ($settingsContent -match "include '$loader'" -and $settingsContent -notmatch "// include '$loader'") {
        $enabledLoaders += $loader
    }
}

if ($enabledLoaders.Count -eq 0) {
    Write-Host "Error: No hay loaders habilitados para la versión $MinecraftVersion" -ForegroundColor Red
    exit 1
}

Write-Host "Loaders habilitados para Minecraft ${MinecraftVersion}: $($enabledLoaders -join ', ')" -ForegroundColor Green

# Ejecutar el cliente con cada loader
$errorCount = 0
foreach ($loader in $enabledLoaders) {
    Write-Host "`n=========================================================" -ForegroundColor Cyan
    Write-Host "Iniciando cliente con $loader para Minecraft $MinecraftVersion" -ForegroundColor Cyan
    Write-Host "=========================================================`n" -ForegroundColor Cyan
    
    & "$PSScriptRoot\run-client.ps1" -MinecraftVersion $MinecraftVersion -Loader $loader
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error al ejecutar el cliente con $loader" -ForegroundColor Red
        $errorCount++
    }
    
    # Pausa corta entre ejecuciones
    Start-Sleep -Seconds 2
}

# Mostrar resumen
Write-Host "`n=========================================================" -ForegroundColor Cyan
Write-Host "Resumen de ejecución para Minecraft $MinecraftVersion" -ForegroundColor Cyan
Write-Host "=========================================================`n" -ForegroundColor Cyan

Write-Host "Total de loaders ejecutados: $($enabledLoaders.Count)" -ForegroundColor White
if ($errorCount -gt 0) {
    Write-Host "Loaders con errores: $errorCount" -ForegroundColor Red
} else {
    Write-Host "Todos los loaders se ejecutaron correctamente" -ForegroundColor Green
} 
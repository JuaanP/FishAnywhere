# Script para compilar el proyecto para todas las versiones disponibles
# Usa una sintaxis simplificada para evitar problemas de codificacion

$projectRoot = Join-Path $PSScriptRoot ".."
$propertiesPath = "$PSScriptRoot\properties.toml"

# Verificar que existe el archivo properties.toml
if (-not (Test-Path $propertiesPath)) {
    Write-Host 'Error: No se encuentra el archivo properties.toml' -ForegroundColor Red
    exit 1
}

# Leer versiones de Minecraft del archivo properties.toml
$content = Get-Content $propertiesPath -Raw
$matches = [regex]::Matches($content, 'minecraft-([0-9_]+)\s*=\s*"([^"]*)"')
$versions = @()

foreach ($match in $matches) {
    $versions += $match.Groups[2].Value
}

# Ordenar versiones (mas recientes primero)
$versions = $versions | Sort-Object -Descending

if ($versions.Count -eq 0) {
    Write-Host 'Error: No se encontraron versiones de Minecraft' -ForegroundColor Red
    exit 1
}

# Mostrar versiones encontradas
Write-Host 'Versiones de Minecraft disponibles:' -ForegroundColor Cyan
$versions | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
Write-Host ''

# Preguntar si compilar todas o seleccionar
$answer = Read-Host 'Compilar todas las versiones? (S/N)'
if ($answer -ne 'S' -and $answer -ne 's') {
    Write-Host 'Ingresa las versiones a compilar (separadas por comas):'
    $input = Read-Host
    
    if ([string]::IsNullOrWhiteSpace($input)) {
        Write-Host 'No se seleccionaron versiones. Saliendo.' -ForegroundColor Yellow
        exit 0
    }
    
    $versions = $input -split ',' | ForEach-Object { $_.Trim() }
}

# Resultados
$results = @{}

# Compilar cada version
foreach ($ver in $versions) {
    Write-Host ''
    Write-Host ('=' * 50) -ForegroundColor Cyan
    Write-Host "Compilando version $ver" -ForegroundColor Cyan
    Write-Host ('=' * 50) -ForegroundColor Cyan
    Write-Host ''
    
    & "$PSScriptRoot\build-version.ps1" -MinecraftVersion $ver
    
    if ($LASTEXITCODE -eq 0) {
        $results[$ver] = $true
    } else {
        $results[$ver] = $false
    }
    
    Start-Sleep -Seconds 1
}

# Mostrar resumen
Write-Host ''
Write-Host ('=' * 50) -ForegroundColor Cyan
Write-Host 'RESUMEN DE COMPILACION' -ForegroundColor Cyan
Write-Host ('=' * 50) -ForegroundColor Cyan

$ok = 0
$error = 0

foreach ($ver in $versions) {
    $status = if ($results[$ver]) { 
        $ok++
        'OK' 
    } else { 
        $error++
        'ERROR' 
    }
    
    $color = if ($results[$ver]) { 'Green' } else { 'Red' }
    Write-Host "  $ver : $status" -ForegroundColor $color
}

Write-Host ''
Write-Host "Total: $($versions.Count) | Exitosas: $ok | Fallidas: $error" -ForegroundColor White

# Verificar si hay archivos JAR generados
$allVersionDirs = Get-ChildItem "$projectRoot\builds\mc-*" -Directory -ErrorAction SilentlyContinue
if ($allVersionDirs) {
    $totalJars = (Get-ChildItem $allVersionDirs.FullName -Filter "*.jar").Count
    Write-Host "Archivos JAR generados: $totalJars" -ForegroundColor Green
    Write-Host "Ubicación: $projectRoot\builds\mc-*" -ForegroundColor White
} else {
    Write-Host "No se encontraron directorios de salida con archivos JAR." -ForegroundColor Yellow
}

Write-Host 'Proceso finalizado' -ForegroundColor Cyan 
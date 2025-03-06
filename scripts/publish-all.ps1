param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("release", "beta", "alpha")]
    [string]$ReleaseType = "release"
)

# Obtener la versión del mod desde gradle.properties
$gradlePropertiesPath = Join-Path $PSScriptRoot "../gradle.properties"
$modVersion = (Get-Content $gradlePropertiesPath | Where-Object { $_ -match "^version=" }) -replace "version=", ""

# Obtener versiones de properties.toml
$propertiesPath = Join-Path $PSScriptRoot "properties.toml"
$content = Get-Content $propertiesPath -Raw
$versions = [regex]::Matches($content, '(?<=minecraft-)[0-9_]+(?=\s*=\s*"[0-9.]+")') | 
            ForEach-Object { $_.Value -replace '_', '.' }

Write-Host "Publicando mod v$modVersion para las siguientes versiones:" -ForegroundColor Cyan
$versions | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }

$confirmation = Read-Host "`n¿Deseas continuar? (S/N)"
if ($confirmation -ne "S" -and $confirmation -ne "s") {
    Write-Host "Publicación cancelada." -ForegroundColor Yellow
    exit 0
}

foreach ($mcVersion in $versions) {
    Write-Host "`nPublicando para Minecraft $mcVersion..." -ForegroundColor Magenta
    & "$PSScriptRoot\publish.ps1" -Version $modVersion -MinecraftVersion $mcVersion -ReleaseType $ReleaseType
} 
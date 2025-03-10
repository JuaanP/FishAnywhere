# Minimalista
$versions = @("1.20.6", "1.21.1", "1.21.2", "1.21.3", "1.21.4")
foreach ($v in $versions) {
    Write-Host "Building $v..."
    & "$PSScriptRoot\..\build-version.ps1" -MinecraftVersion $v
}
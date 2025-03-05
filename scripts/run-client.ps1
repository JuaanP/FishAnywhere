param(
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion,
    
    [Parameter(Mandatory=$true)]
    [ValidateSet("fabric", "forge", "neoforge")]
    [string]$Loader
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

# Leer settings.gradle para verificar que el loader especificado está habilitado
$settingsContent = Get-Content "$projectRoot\settings.gradle" -Raw
$loaderEnabled = $settingsContent -match "include '$Loader'" -and $settingsContent -notmatch "// include '$Loader'"

if (-not $loaderEnabled) {
    Write-Host "Error: El loader $Loader no está habilitado para la versión $MinecraftVersion" -ForegroundColor Red
    Write-Host "Verifica que el loader esté disponible para esta versión y esté habilitado en settings.gradle." -ForegroundColor Yellow
    exit 1
}

# Determinar la tarea correcta según el loader
$runTask = "$Loader`:"

# Fabric y Forge usan 'runClient'
if ($Loader -eq "fabric" -or $Loader -eq "forge") {
    $runTask += "runClient"
}
# NeoForge a veces usa 'client' en lugar de 'runClient'
elseif ($Loader -eq "neoforge") {
    # Verificar qué tareas están disponibles para NeoForge
    Push-Location $projectRoot
    try {
        $tasksOutput = & ./gradlew "$Loader`:tasks" --console=plain
        if ($tasksOutput -match "client\s+-\s+") {
            $runTask += "client"
        } else {
            $runTask += "runClient"
        }
    }
    finally {
        Pop-Location
    }
}

Write-Host "Ejecutando cliente de Minecraft $MinecraftVersion con $Loader..." -ForegroundColor Green

# Configurar opciones para una mejor experiencia de desarrollo
$env:GRADLE_OPTS = "-Xmx3G -XX:+UseParallelGC -Dorg.gradle.project.minecraft.runs.skipDownload=true"

# Ejecutar el cliente
Push-Location $projectRoot
try {
    & ./gradlew $runTask --stacktrace
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Cliente cerrado correctamente." -ForegroundColor Green
    } else {
        Write-Host "El cliente se cerró con un código de error: $LASTEXITCODE" -ForegroundColor Red
    }
}
finally {
    Pop-Location
} 
param(
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion,
    
    [Parameter(Mandatory=$false)]
    [string[]]$Loaders = @("fabric", "forge", "neoforge")
)

# Ruta al directorio raíz del proyecto
$projectRoot = Join-Path $PSScriptRoot ".."

# Configurar el proyecto para la versión especificada
Write-Host "Configurando proyecto para Minecraft $MinecraftVersion..." -ForegroundColor Cyan
& "$PSScriptRoot\set-version.ps1" -MinecraftVersion $MinecraftVersion

# Filtrar loaders no compatibles
$enabledLoaders = $Loaders

# Excluir NeoForge para 1.20.1 ya que no es compatible
if ($MinecraftVersion -eq "1.20.1" -and $enabledLoaders -contains "neoforge") {
    Write-Host "NeoForge no es compatible con Minecraft 1.20.1, se excluirá de la compilación." -ForegroundColor Yellow
    $enabledLoaders = $enabledLoaders | Where-Object { $_ -ne "neoforge" }
}

Write-Host "Loaders seleccionados para Minecraft ${MinecraftVersion}: $($enabledLoaders -join ', ')" -ForegroundColor Green

# Compilar cada loader seleccionado
foreach ($loader in $enabledLoaders) {
    Write-Host "`n=========================================================" -ForegroundColor Cyan
    Write-Host "Compilando $loader para Minecraft $MinecraftVersion" -ForegroundColor Cyan
    Write-Host "=========================================================`n" -ForegroundColor Cyan
    
    # Ejecutar la tarea de build para este loader
    Push-Location $projectRoot
    try {
        # Excluir tareas problemáticas específicas para cada loader
        $excludeTasks = @()
        if ($loader -eq "fabric") {
            $excludeTasks = @("sourcesJar", "remapSourcesJar", "javadocJar")
        } elseif ($loader -eq "forge" -or $loader -eq "neoforge") {
            $excludeTasks = @("sourcesJar", "javadocJar")
        }
        
        $excludeParams = $excludeTasks | ForEach-Object { "--exclude-task", $_ }
        
        # Ejecutar la compilación con las tareas excluidas
        & ./gradlew ":${loader}:build" --no-daemon @excludeParams
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Compilación exitosa para $loader" -ForegroundColor Green
            
            # Mostrar archivos JAR generados
            $jarFiles = Get-ChildItem "$projectRoot\$loader\build\libs\*.jar" -ErrorAction SilentlyContinue | 
                        Where-Object { $_.Name -notmatch "sources|javadoc|dev" }
            
            if ($jarFiles) {
                foreach ($jar in $jarFiles) {
                    Write-Host "  - $($jar.Name): $($jar.FullName)" -ForegroundColor White
                }
            } else {
                Write-Host "  No se encontraron archivos JAR para $loader" -ForegroundColor Yellow
            }
        } else {
            Write-Host "Error: La compilación de $loader falló" -ForegroundColor Red
        }
    } finally {
        Pop-Location
    }
}

Write-Host "`nProceso de compilación finalizado" -ForegroundColor Cyan 
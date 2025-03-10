# Script para compilar simultáneamente varias versiones de Minecraft

# Configurar versiones a compilar
$versions = @(
    "1.20.1",
    "1.20.2", 
    "1.20.3"
)

# Crear trabajos en segundo plano para cada versión
$jobs = @()
foreach ($version in $versions) {
    Write-Host "Iniciando compilación para Minecraft $version en segundo plano..." -ForegroundColor Cyan
    
    # Crear un trabajo para cada versión
    $job = Start-Job -ScriptBlock {
        param($scriptRoot, $mcVersion)
        & "$scriptRoot\..\build-version.ps1" -MinecraftVersion $mcVersion 
    } -ArgumentList $PSScriptRoot, $version
    
    # Guardar referencia al trabajo
    $jobs += @{
        Version = $version
        Job = $job
    }
}

# Mostrar información mientras se ejecutan los trabajos
Write-Host "`nCompilaciones en progreso: $($jobs.Count)" -ForegroundColor Yellow
do {
    $runningJobs = $jobs | Where-Object { $_.Job.State -eq 'Running' }
    Write-Host "Compilaciones activas: $($runningJobs.Count) | " -NoNewline -ForegroundColor Cyan
    foreach ($jobInfo in $runningJobs) {
        Write-Host "$($jobInfo.Version) " -NoNewline -ForegroundColor White
    }
    Write-Host ""
    Start-Sleep -Seconds 5
} while ($runningJobs.Count -gt 0)

# Mostrar resultados
Write-Host "`n===================================================================" -ForegroundColor Magenta
Write-Host "RESULTADOS DE COMPILACIONES" -ForegroundColor Magenta
Write-Host "===================================================================" -ForegroundColor Magenta

foreach ($jobInfo in $jobs) {
    $jobOutput = Receive-Job -Job $jobInfo.Job -Keep
    $success = $jobInfo.Job.State -eq 'Completed'
    $color = if ($success) { 'Green' } else { 'Red' }
    $status = if ($success) { 'ÉXITO' } else { 'FALLÓ' }
    
    Write-Host "Minecraft $($jobInfo.Version): " -NoNewline
    Write-Host "$status" -ForegroundColor $color
    
    # Opcionalmente, guarda la salida en un archivo de log
    $logFile = "build-$($jobInfo.Version).log"
    $jobOutput | Out-File -FilePath "$PSScriptRoot\$logFile"
    Write-Host "  Log guardado en: $PSScriptRoot\$logFile"
}

# Limpiar trabajos
$jobs | ForEach-Object { Remove-Job -Job $_.Job -Force }

Write-Host "`nTodas las compilaciones han finalizado." -ForegroundColor Green 
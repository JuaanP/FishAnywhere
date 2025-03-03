# Script ultra simple para pruebas (evita problemas de sintaxis)

# Mostrar texto coloreado
function ShowMsg {
    param($msg, $col = "White")
    Write-Host $msg -ForegroundColor $col
}

# Ejecutar una prueba
function RunTest {
    param($ver, $platform, $clean = $false)
    
    ShowMsg "----------------------------------------------------" "Cyan"
    ShowMsg "Probando Minecraft $ver con $platform" "Cyan"
    ShowMsg "----------------------------------------------------" "Cyan"
    
    # Cambiar versión usando los scripts set-version
    ShowMsg "Cambiando a $ver..." "Yellow"
    $setVersionScript = "set-version-$ver.ps1"
    
    if (Test-Path $setVersionScript) {
        & .\$setVersionScript
        ShowMsg "Versión cambiada con éxito usando $setVersionScript" "Green"
    } else {
        ShowMsg "¡Error! No se encontró el script $setVersionScript" "Red"
        return @{
            Version = $ver
            Platform = $platform
            ExitCode = 1
            Success = $false
            Message = "No existe script para esta versión"
        }
    }
    
    # Limpiar si es necesario
    if ($clean) {
        ShowMsg "Limpiando proyecto..." "Yellow"
        & .\gradlew.bat clean
    }
    
    # Ejecutar
    $taskName = "$($platform.ToLower()):runClient"
    ShowMsg "Ejecutando $taskName..." "Green"
    & .\gradlew.bat $taskName --stacktrace
    
    # Resultado
    return @{
        Version = $ver
        Platform = $platform
        ExitCode = $LASTEXITCODE
        Success = ($LASTEXITCODE -eq 0)
    }
}

# INICIO DEL SCRIPT PRINCIPAL
ShowMsg "====================================================" "Cyan"
ShowMsg "         PRUEBAS DE MINECRAFT MULTIVERSION         " "Cyan"
ShowMsg "====================================================" "Cyan"
ShowMsg ""

# Verificar existencia de scripts set-version
$setVersionScripts = Get-ChildItem -Path "." -Filter "set-version-*.ps1" | Sort-Object Name
if ($setVersionScripts.Count -eq 0) {
    ShowMsg "¡Error! No se encontraron scripts set-version-*.ps1" "Red"
    ShowMsg "Asegúrate de tener scripts para cada versión que quieras probar" "Red"
    ShowMsg "Presiona Enter para salir..." "Red"
    Read-Host | Out-Null
    exit 1
}

# Mostrar versiones disponibles
ShowMsg "Versiones disponibles:" "Green"
$versiones = @{}
foreach ($script in $setVersionScripts) {
    $verMatch = $script.Name -match "set-version-(.+)\.ps1"
    if ($verMatch) {
        $version = $matches[1]
        $versiones[$version] = $script.FullName
        ShowMsg "  • $version" "White"
    }
}
ShowMsg ""

# Preguntas iniciales
$doClean = $false
$resp = Read-Host "Limpiar antes de cada prueba? (s/n)"
if ($resp -eq "s") {
    $doClean = $true
    ShowMsg "Se limpiará el proyecto antes de cada prueba" "Yellow"
}

$runAll = $true
$resp = Read-Host "Ejecutar todas las pruebas? (s/n)"
if ($resp -ne "s") {
    $runAll = $false
    ShowMsg "Solo se ejecutarán pruebas principales" "Yellow"
}

# Guardar configuración actual para restaurarla después
ShowMsg "Guardando estado actual del proyecto..." "Yellow"
$backupFolder = "config_backup_test"
if (!(Test-Path $backupFolder)) {
    New-Item -ItemType Directory -Path $backupFolder -Force | Out-Null
}
if (Test-Path "gradle.properties") {
    Copy-Item "gradle.properties" -Destination "$backupFolder/gradle.properties"
}
if (Test-Path "settings.gradle") {
    Copy-Item "settings.gradle" -Destination "$backupFolder/settings.gradle"
}
ShowMsg "Estado guardado en $backupFolder" "Green"

# Definir pruebas básicas (solo con versiones que existan)
$basicTests = @(
    @{Ver="1.20.1"; Plat="Fabric"},
    @{Ver="1.20.1"; Plat="Forge"},
    @{Ver="1.20.4"; Plat="Fabric"},
    @{Ver="1.20.4"; Plat="Forge"},
    @{Ver="1.20.4"; Plat="NeoForge"},
    @{Ver="1.21.4"; Plat="Fabric"},
    @{Ver="1.21.4"; Plat="NeoForge"}
)

# Definir pruebas adicionales
$extraTests = @(
    @{Ver="1.20.2"; Plat="Fabric"},
    @{Ver="1.20.2"; Plat="Forge"},
    @{Ver="1.20.2"; Plat="NeoForge"},
    @{Ver="1.20.3"; Plat="Fabric"},
    @{Ver="1.20.3"; Plat="Forge"},
    @{Ver="1.20.3"; Plat="NeoForge"},
    @{Ver="1.20.6"; Plat="Fabric"},
    @{Ver="1.20.6"; Plat="Forge"},
    @{Ver="1.20.6"; Plat="NeoForge"},
    @{Ver="1.21.1"; Plat="Fabric"},
    @{Ver="1.21.1"; Plat="Forge"},
    @{Ver="1.21.1"; Plat="NeoForge"},
    @{Ver="1.21.2"; Plat="Fabric"},
    @{Ver="1.21.2"; Plat="Forge"},
    @{Ver="1.21.2"; Plat="NeoForge"},
    @{Ver="1.21.3"; Plat="Fabric"},
    @{Ver="1.21.3"; Plat="Forge"},
    @{Ver="1.21.3"; Plat="NeoForge"}
)

# Filtrar pruebas a solo las versiones con scripts disponibles
$basicTests = $basicTests | Where-Object { $versiones.ContainsKey($_.Ver) }
$extraTests = $extraTests | Where-Object { $versiones.ContainsKey($_.Ver) }

# Seleccionar pruebas a ejecutar
$allTests = if ($runAll) { $basicTests + $extraTests } else { $basicTests }
$testCount = $allTests.Count
ShowMsg "Se ejecutarán $testCount pruebas" "Cyan"

# Ejecutar pruebas
$allResults = @()
$testNum = 0

foreach ($test in $allTests) {
    $testNum++
    $ver = $test.Ver
    $plat = $test.Plat
    
    ShowMsg ""
    ShowMsg "Prueba $testNum de $testCount" "Cyan"
    ShowMsg "Minecraft $ver con $plat" "Cyan"
    
    $result = RunTest -ver $ver -platform $plat -clean $doClean
    $allResults += $result
    
    ShowMsg "Prueba terminada. Presiona Enter para continuar..." "Yellow"
    Read-Host | Out-Null
}

# Agrupar resultados por versión
ShowMsg ""
ShowMsg "====================================================" "Cyan"
ShowMsg "                  RESULTADOS                       " "Cyan"
ShowMsg "====================================================" "Cyan"

# Agrupar por versión manualmente (evita problemas de sintaxis)
$verResults = @{}
foreach ($result in $allResults) {
    $v = $result.Version
    if (-not $verResults.ContainsKey($v)) {
        $verResults[$v] = @()
    }
    $verResults[$v] += $result
}

# Mostrar por versión
foreach ($v in $verResults.Keys | Sort-Object) {
    ShowMsg ""
    ShowMsg "Minecraft $v" "Magenta"
    
    foreach ($r in $verResults[$v]) {
        $estado = if ($r.Success) { "OK" } else { "Error ($($r.ExitCode))" }
        $color = if ($r.Success) { "Green" } else { "Red" }
        ShowMsg "  $($r.Platform): $estado" $color
    }
}

# Estadísticas
$total = $allResults.Count
$exitos = ($allResults | Where-Object { $_.Success }).Count
$fallos = $total - $exitos
$tasa = [math]::Round(($exitos / $total) * 100, 2)

ShowMsg ""
ShowMsg "Estadísticas:" "Cyan"
ShowMsg "  Total: $total" "White"
ShowMsg "  Exitosos: $exitos" "Green"
ShowMsg "  Fallidos: $fallos" "Red"
ShowMsg "  Tasa de éxito: $tasa%" "Yellow"

# Restaurar configuración original
ShowMsg ""
ShowMsg "Restaurando configuración original..." "Yellow"
if (Test-Path "$backupFolder/gradle.properties") {
    Copy-Item "$backupFolder/gradle.properties" -Destination "gradle.properties" -Force
}
if (Test-Path "$backupFolder/settings.gradle") {
    Copy-Item "$backupFolder/settings.gradle" -Destination "settings.gradle" -Force
}
ShowMsg "Configuración original restaurada" "Green"

ShowMsg ""
ShowMsg "Pruebas completadas. Presiona Enter para salir..." "Green"
Read-Host | Out-Null 
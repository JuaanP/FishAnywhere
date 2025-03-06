param(
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion,
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("fabric", "forge", "neoforge", "all")]
    [string]$Loader,
    
    [Parameter(Mandatory=$false)]
    [string[]]$Loaders = @(),
    
    [Parameter(Mandatory=$false)]
    [switch]$b,
    
    [Parameter(Mandatory=$false)]
    [switch]$f
)

# Procesar la entrada del usuario para analizar flags y argumentos
if ($MinecraftVersion -match '\s') {
    $parts = $MinecraftVersion -split '\s+'
    $versionInput = $parts[0].Trim()
    
    # Buscar flags -f o -b
    foreach ($part in $parts[1..$parts.Length]) {
        if ($part -eq '-f') {
            $f = $true
            Write-Host "Activado compilación hacia adelante" -ForegroundColor Cyan
        }
        elseif ($part -eq '-b') {
            $b = $true
            Write-Host "Activado compilación hacia atrás" -ForegroundColor Cyan
        }
        # Ver si hay un loader especificado
        elseif ($part -match '^(fabric|forge|neoforge|all)$' -and [string]::IsNullOrEmpty($Loader)) {
            $Loader = $part
            Write-Host "Usando loader: $Loader" -ForegroundColor Cyan
        }
    }
    
    # Actualizar MinecraftVersion sin argumentos adicionales
    $MinecraftVersion = $versionInput
}

# Función para leer versiones disponibles de properties.toml
function Get-AvailableVersions {
    $propertiesPath = Join-Path $PSScriptRoot "properties.toml"
    if (-not (Test-Path $propertiesPath)) {
        Write-Host "Error: No se encuentra el archivo properties.toml" -ForegroundColor Red
        return @()
    }
    
    $content = Get-Content $propertiesPath -Raw
    $versions = [regex]::Matches($content, '(?<=minecraft-)[0-9_]+(?=\s*=\s*"[0-9.]+")') | 
                ForEach-Object { $_.Value -replace '_', '.' }
    
    # Convertir a versiones semánticas para ordenar correctamente
    $semanticVersions = $versions | ForEach-Object {
        $parts = $_ -split '\.'
        
        # Establecer Patch como 0 si no existe (en lugar de usar ??)
        $patch = 0
        if ($parts.Length -gt 2) {
            $patch = [int]$parts[2]
        }
        
        [PSCustomObject]@{
            Original = $_
            Major = [int]$parts[0]
            Minor = [int]$parts[1]
            Patch = $patch
        }
    }
    
    # Ordenar las versiones (descendente)
    $sortedVersions = $semanticVersions | Sort-Object Major, Minor, Patch -Descending | ForEach-Object { $_.Original -replace '\.', '_' }
    
    # Convertir de formato 1_20_6, etc. al formato 1.20.6 para MinecraftVersion
    $result = $sortedVersions | ForEach-Object { $_ -replace '_', '.' }
    return $result
}

# Procesar versiones si se especifican flags -b o -f
$availableVersions = @()
$versionsToCompile = @()

if ($b -or $f) {
    $availableVersions = Get-AvailableVersions
    
    if ($availableVersions.Count -eq 0) {
        Write-Host "Error: No se pudieron obtener las versiones disponibles" -ForegroundColor Red
        exit 1
    }
    
    # Versión inicial sin separar (para buscar en la lista)
    $initialVersion = $MinecraftVersion -split ',' | Select-Object -First 1 | ForEach-Object { $_.Trim() }
    
    # Encontrar el índice de la versión especificada
    $index = $availableVersions.IndexOf($initialVersion)
    
    if ($index -eq -1) {
        Write-Host "Error: La versión $initialVersion no se encuentra en la lista de versiones disponibles" -ForegroundColor Red
        Write-Host "Versiones disponibles: $($availableVersions -join ', ')" -ForegroundColor Yellow
        exit 1
    }
    
    if ($b) {
        # De la versión especificada hacia atrás (versiones anteriores)
        $versionsToCompile = $availableVersions[$index..($availableVersions.Count-1)]
        Write-Host "Compilando versiones hacia atrás desde ${initialVersion}: $($versionsToCompile -join ', ')" -ForegroundColor Cyan
    }
    elseif ($f) {
        # De la versión especificada hacia adelante (versiones posteriores)
        $versionsToCompile = $availableVersions[0..$index] | Sort-Object # Invertir para orden ascendente
        Write-Host "Compilando versiones hacia adelante desde ${initialVersion}: $($versionsToCompile -join ', ')" -ForegroundColor Cyan
    }
} else {
    # Procesar múltiples versiones si se pasan separadas por comas
    $versionsToCompile = $MinecraftVersion -split ',' | ForEach-Object { $_.Trim() }
}

# Después de determinar las versiones a compilar, pero ANTES del bucle foreach
# Preguntar por el loader una sola vez si no está especificado
$currentLoader = $Loader
if (-not $PSBoundParameters.ContainsKey('Loader') -and $Loaders.Count -eq 0) {
    $options = @{
        1 = "fabric"
        2 = "forge"
        3 = "neoforge"
        4 = "all"
    }
    
    Write-Host "`nSelecciona el loader a compilar para todas las versiones:" -ForegroundColor Yellow
    Write-Host "1. Fabric" -ForegroundColor Cyan
    Write-Host "2. Forge" -ForegroundColor Cyan
    Write-Host "3. NeoForge" -ForegroundColor Cyan
    Write-Host "4. Todos" -ForegroundColor Cyan
    
    do {
        $selection = Read-Host "Ingresa el número de la opción (1-4)"
    } while (-not $options.ContainsKey([int]$selection))
    
    $currentLoader = $options[[int]$selection]
    Write-Host "Seleccionaste: $currentLoader" -ForegroundColor Green
}

# Determinar los loaders a compilar (una sola vez)
if (($currentLoader -eq "all" -or $null -eq $currentLoader) -and $Loaders.Count -eq 0) {
    $enabledLoaders = @("fabric", "forge", "neoforge")
} elseif ($null -ne $currentLoader -and $currentLoader -ne "all") {
    $enabledLoaders = @($currentLoader)
} else {
    $enabledLoaders = $Loaders
}

# Asegurarse de que siempre haya al menos un loader válido
if ($enabledLoaders.Count -eq 0) {
    Write-Host "No se han especificado loaders válidos. Se usará 'fabric' por defecto." -ForegroundColor Yellow
    $enabledLoaders = @("fabric")
}

# Para cada versión de Minecraft, ejecutar el proceso de compilación
foreach ($mcVersion in $versionsToCompile) {
    Write-Host "`n=======================================================================" -ForegroundColor Magenta
    Write-Host "COMPILANDO MINECRAFT $mcVersion" -ForegroundColor Magenta
    Write-Host "=======================================================================`n" -ForegroundColor Magenta

    # Ruta al directorio raíz del proyecto
    $projectRoot = Join-Path $PSScriptRoot ".."

    # Configurar el proyecto para la versión especificada
    Write-Host "Configurando proyecto para Minecraft $mcVersion..." -ForegroundColor Cyan
    & "$PSScriptRoot\set-version.ps1" -MinecraftVersion $mcVersion

    # Leer la versión de Java de gradle.properties
    $gradlePropertiesPath = Join-Path $projectRoot "gradle.properties"
    $javaVersion = (Get-Content $gradlePropertiesPath | Where-Object { $_ -match "^java_version=" }) -replace "^java_version=", ""

    # Verificar que exista la versión de Java correcta
    $javaHome = "C:/Program Files/Java/jdk-$javaVersion"
    if (!(Test-Path $javaHome)) {
        Write-Host "Error: No se encuentra JDK $javaVersion en $javaHome" -ForegroundColor Red
        Write-Host "Por favor, instala Java $javaVersion o actualiza la configuración" -ForegroundColor Red
        continue  # Saltar a la siguiente versión en lugar de salir
    }
    Write-Host "Usando Java $javaVersion desde $javaHome" -ForegroundColor Green

    # Filtrar loaders no compatibles para esta versión específica
    $versionEnabledLoaders = $enabledLoaders.Clone()
    if (($mcVersion -eq "1.20.1" -or $mcVersion -eq "1.20.3") -and $versionEnabledLoaders -contains "neoforge") {
        Write-Host "NeoForge no es compatible con Minecraft $mcVersion, se excluirá de la compilación." -ForegroundColor Yellow
        $versionEnabledLoaders = $versionEnabledLoaders | Where-Object { $_ -ne "neoforge" }
    }

    Write-Host "Loaders seleccionados para Minecraft ${mcVersion}: $($versionEnabledLoaders -join ', ')" -ForegroundColor Green

    # Compilar cada loader seleccionado para esta versión
    foreach ($loader in $versionEnabledLoaders) {
        Write-Host "`n=========================================================" -ForegroundColor Cyan
        Write-Host "Compilando $loader para Minecraft $mcVersion" -ForegroundColor Cyan
        Write-Host "=========================================================`n" -ForegroundColor Cyan
        
        if ($loader -eq "neoforge") {
            # Para NeoForge, ejecutar en un proceso completamente independiente
            $neoforgeScript = @"
Set-Location "$projectRoot"
`$env:JAVA_HOME = "$javaHome"
./gradlew `:${loader}:build` --no-daemon --exclude-task sourcesJar --exclude-task javadocJar
exit `$LASTEXITCODE
"@
            $tempScriptPath = Join-Path $env:TEMP "build-neoforge-temp.ps1"
            $neoforgeScript | Out-File -FilePath $tempScriptPath -Encoding utf8
            
            $process = Start-Process -FilePath "powershell" -ArgumentList "-ExecutionPolicy Bypass -File `"$tempScriptPath`"" -NoNewWindow -Wait -PassThru
            
            if ($process.ExitCode -eq 0) {
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
            
            # Limpiar archivo temporal
            Remove-Item -Path $tempScriptPath -ErrorAction SilentlyContinue
        } else {
            # Para otros loaders, ejecutar normalmente
            Push-Location $projectRoot
            try {
                # Establecer JAVA_HOME sin más parámetros
                $env:JAVA_HOME = $javaHome
                
                # Ejecutar sin parámetros adicionales de Java
                & ./gradlew ":${loader}:build" --no-daemon --exclude-task sourcesJar --exclude-task javadocJar
                
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
    }
}

Write-Host "`nProceso de compilación finalizado para todas las versiones" -ForegroundColor Cyan 
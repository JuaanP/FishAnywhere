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

# Leer la versión de Java de gradle.properties
$gradlePropertiesPath = Join-Path $projectRoot "gradle.properties"
$javaVersion = (Get-Content $gradlePropertiesPath | Where-Object { $_ -match "^java_version=" }) -replace "java_version=", ""

# Verificar que exista la versión de Java correcta
$javaHome = "C:/Program Files/Java/jdk-$javaVersion"
if (!(Test-Path $javaHome)) {
    Write-Host "Error: No se encuentra JDK $javaVersion en $javaHome" -ForegroundColor Red
    Write-Host "Por favor, instala Java $javaVersion o actualiza la configuración" -ForegroundColor Red
    exit 1
}
Write-Host "Usando Java $javaVersion desde $javaHome" -ForegroundColor Green

# Filtrar loaders no compatibles
$enabledLoaders = $Loaders

# Excluir NeoForge para versiones problemáticas
if (($MinecraftVersion -eq "1.20.1" -or $MinecraftVersion -eq "1.20.3") -and $enabledLoaders -contains "neoforge") {
    Write-Host "NeoForge no es compatible con Minecraft $MinecraftVersion, se excluirá de la compilación." -ForegroundColor Yellow
    $enabledLoaders = $enabledLoaders | Where-Object { $_ -ne "neoforge" }
}

Write-Host "Loaders seleccionados para Minecraft ${MinecraftVersion}: $($enabledLoaders -join ', ')" -ForegroundColor Green

# Compilar cada loader seleccionado
foreach ($loader in $enabledLoaders) {
    Write-Host "`n=========================================================" -ForegroundColor Cyan
    Write-Host "Compilando $loader para Minecraft $MinecraftVersion" -ForegroundColor Cyan
    Write-Host "=========================================================`n" -ForegroundColor Cyan
    
    if ($loader -eq "neoforge") {
        # Para NeoForge, ejecutar en un proceso completamente independiente
        $neoforgeScript = @"
Set-Location "$projectRoot"
`$env:JAVA_HOME = "$javaHome"
./gradlew `:${loader}:build` --no-daemon --exclude-task sourcesJar --exclude-task javadocJar -Dorg.gradle.java.home="$javaHome"
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
            # Establecer JAVA_HOME y ejecutar Gradle con la versión de Java correcta
            $env:JAVA_HOME = $javaHome
            
            # Ejecutar la compilación con las tareas excluidas y especificando org.gradle.java.home
            & ./gradlew ":${loader}:build" --no-daemon --exclude-task sourcesJar --exclude-task javadocJar "-Dorg.gradle.java.home=$javaHome"
            
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

Write-Host "`nProceso de compilación finalizado" -ForegroundColor Cyan 
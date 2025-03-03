# Script para construir el mod para todas las versiones de Minecraft configuradas
Write-Host "Iniciando construcción para múltiples versiones de Minecraft" -ForegroundColor Cyan

# Verificar que existe el JDK correcto antes de comenzar
$jdkPath = "C:/Program Files/Java/jdk-17"
$jdk21Path = "C:/Program Files/Java/jdk-21" # Para versiones que requieren Java 21

# Verificar que existen los JDKs necesarios
if (!(Test-Path $jdkPath)) {
    Write-Host "ERROR: No se encuentra el JDK 17 en $jdkPath" -ForegroundColor Red
    Write-Host "Por favor, instala JDK 17 y asegúrate que la ruta sea correcta" -ForegroundColor Red
    exit 1
}

# Convertir la ruta para que funcione con Gradle
$gradleJavaHome = $jdkPath.Replace("\", "/")
$gradleJava21Home = $jdk21Path.Replace("\", "/")

# Crear carpeta principal de builds si no existe
if (!(Test-Path "builds")) {
    New-Item -ItemType Directory -Path "builds" -Force | Out-Null
}

# Lista de versiones de Minecraft soportadas con sus loaders compatibles
$versions = @(
    @{
        Version = "1.20.1"
        Loaders = @("fabric", "forge") # NeoForge no disponible para 1.20.1
        JavaVersion = "17"
    },
    @{
        Version = "1.20.2"
        Loaders = @("fabric", "forge", "neoforge")
        JavaVersion = "17"
    },
    @{
        Version = "1.20.3"
        Loaders = @("fabric", "forge", "neoforge")
        JavaVersion = "17"
    },
    @{
        Version = "1.20.4"
        Loaders = @("fabric", "forge", "neoforge")
        JavaVersion = "17"
    },
    @{
        Version = "1.20.6"
        Loaders = @("fabric", "forge", "neoforge")
        JavaVersion = "17"
    },
    @{
        Version = "1.21.1"
        Loaders = @("fabric", "forge", "neoforge")
        JavaVersion = "21"
    },
    @{
        Version = "1.21.2"
        Loaders = @("fabric", "forge", "neoforge")
        JavaVersion = "21"
    }
)

# Para cada versión de Minecraft
foreach ($versionInfo in $versions) {
    $version = $versionInfo.Version
    $loaders = $versionInfo.Loaders
    $javaVer = $versionInfo.JavaVersion
    
    # Seleccionar el JDK correcto según la versión de Java requerida
    if ($javaVer -eq "21") {
        $currentJavaHome = $gradleJava21Home
        
        # Verificar si existe el JDK 21
        if (!(Test-Path $jdk21Path)) {
            Write-Host "ADVERTENCIA: No se encuentra JDK 21 para versión ${version}. Intentando usar JDK 17..." -ForegroundColor Yellow
            $currentJavaHome = $gradleJavaHome
        }
    } else {
        $currentJavaHome = $gradleJavaHome
    }
    
    Write-Host "`n===== Construyendo para Minecraft ${version} (Java ${javaVer}) =====" -ForegroundColor Cyan

    # Convertir versión al formato usado en versions.toml (1.20.4 -> 1_20_4)
    $normalizedVersion = $version -replace '\.', '_'
    
    # Leer el archivo versions.toml para obtener las versiones correspondientes
    $versionsToml = Get-Content "gradle/versions.toml" -Raw

    # Extraer valores usando expresiones regulares
    $minecraft = $version
    $minecraftRange = [regex]::Match($versionsToml, "minecraft-range-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value

    if ($version -match "^1\.20\.") {
        $javaVersion = [regex]::Match($versionsToml, "java-1_20_x\s*=\s*`"([^`"]*)`"").Groups[1].Value
    } else {
        $javaVersion = [regex]::Match($versionsToml, "java-1_21_x\s*=\s*`"([^`"]*)`"").Groups[1].Value
    }

    $fabricApi = [regex]::Match($versionsToml, "fabric-api-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
    $fabricLoader = [regex]::Match($versionsToml, "fabric-loader\s*=\s*`"([^`"]*)`"").Groups[1].Value

    $forge = [regex]::Match($versionsToml, "forge-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
    $forgeLoaderRange = [regex]::Match($versionsToml, "forge-loader-range\s*=\s*`"([^`"]*)`"").Groups[1].Value

    $neoforge = ""
    $neoformVersion = ""
    
    # Solo extraer versiones de NeoForge si este loader es compatible con esta versión
    if ($loaders -contains "neoforge") {
        $neoforge = [regex]::Match($versionsToml, "neoforge-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
        $neoformVersion = [regex]::Match($versionsToml, "neo-form-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value
    }
    
    $neoforgeLoaderRange = [regex]::Match($versionsToml, "neoforge-loader-range\s*=\s*`"([^`"]*)`"").Groups[1].Value
    $modmenu = [regex]::Match($versionsToml, "modmenu-$normalizedVersion\s*=\s*`"([^`"]*)`"").Groups[1].Value

    # Crear archivo settings.gradle temporal para incluir solo los proyectos necesarios
    $settingsGradle = @"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    name = 'Fabric'
                    url = uri("https://maven.fabricmc.net")
                }
            }
            filter {
                includeGroup("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = 'NeoForge'
                    url = uri("https://maven.neoforged.net/releases")
                }
            }
            filter {
                includeGroupAndSubgroups("net.neoforged")
                includeGroup("codechicken")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = 'Forge'
                    url = uri("https://maven.minecraftforge.net")
                }
            }
            filter {
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = 'Sponge Snapshots'
                    url = uri("https://repo.spongepowered.org/repository/maven-public")
                }
            }
            filter {
                includeGroupAndSubgroups("org.spongepowered")
            }
        }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

dependencyResolutionManagement {
    versionCatalogs {
        versions {
            from(files("gradle/versions.toml"))
        }
    }
}

// This should match the folder name of the project, or else IDEA may complain
rootProject.name = 'SeamlessTrading'

// Incluir siempre los módulos básicos
include 'common'

// Incluir solo los loaders necesarios para esta versión
"@

    foreach ($loader in $loaders) {
        $settingsGradle += "`ninclude '$loader'"
    }

    # Guardar el archivo de configuración de proyectos
    $settingsGradle | Set-Content "settings.gradle"

    # Crear el archivo gradle.properties con configuraciones optimizadas
    $gradleProps = @"
# [Project]
version=1.2.0
group=com.juaanp.seamlesstrading

# [Common]
mod_name=SeamlessTrading
mod_author=JuaanP
mod_id=seamlesstrading
license=MIT
credits=
description=New trade offers appear instantly as soon as the villager xp progress bar fills. No more level up and trade refresh delay.

minecraft_version=${minecraft}
minecraft_version_range=${minecraftRange}
java_version=${javaVersion}

# Fabric
fabric_version=${fabricApi}
fabric_loader_version=${fabricLoader}

# Forge
forge_version=${forge}
forge_loader_version_range=${forgeLoaderRange}

# NeoForge
neoforge_version=${neoforge}
neo_form_version=${neoformVersion}
neoforge_loader_version_range=${neoforgeLoaderRange}

# ModMenu
modmenu_version=${modmenu}

# Gradle - Configuraciones optimizadas para builds rápidas
org.gradle.jvmargs=-Xmx4G -XX:MaxMetaspaceSize=1G -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=false
org.gradle.java.home=${currentJavaHome}
org.gradle.configureondemand=true

# Configuraciones para Minecraft mods (optimiza la compilación)
fabric.loom.multiProjectOptimisation=true
loom.platform=forge
generate_sources=false
generate_javadocs=false
"@

    # Guardar el archivo actualizado
    $gradleProps | Set-Content "gradle.properties"

    # Mostrar información
    Write-Host "Propiedades actualizadas para Minecraft ${version}:" -ForegroundColor Cyan
    Write-Host "  - Java: ${javaVersion} (ruta: ${currentJavaHome})"
    Write-Host "  - Fabric: ${fabricApi} (loader ${fabricLoader})"
    Write-Host "  - Forge: ${forge}"
    if ($loaders -contains "neoforge") {
        Write-Host "  - NeoForge: ${neoforge}"
    } else {
        Write-Host "  - NeoForge: No disponible para esta versión" -ForegroundColor Yellow
    }

    # Limpiar cache de Gradle
    if ($version -eq $versions[0].Version) {
        Write-Host "Limpiando cache de Gradle..." -ForegroundColor Yellow
        & .\gradlew.bat --stop
    }

    # Limpiar proyectos antes de construir
    Write-Host "Limpiando proyecto anterior..." -ForegroundColor Yellow
    & .\gradlew.bat clean --configure-on-demand --max-workers=4

    # Verificar si hay errores en el clean
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ADVERTENCIA: Falló la limpieza del proyecto, pero continuamos..." -ForegroundColor Yellow
    }
    
    # Construir sólo los loaders compatibles para esta versión
    Write-Host "Ejecutando build para Minecraft ${version} con loaders: $($loaders -join ', ')" -ForegroundColor Green
    
    # En lugar de build, usamos tasks específicas para evitar sources y acelerar el proceso
    $buildTasks = @()
    foreach ($loader in $loaders) {
        # Usamos remapJar o jar directamente según el loader, evitando tasks innecesarias
        if ($loader -eq "fabric") {
            $buildTasks += ":${loader}:remapJar"
        } elseif ($loader -eq "forge") {
            $buildTasks += ":${loader}:jar"
        } elseif ($loader -eq "neoforge") {
            $buildTasks += ":${loader}:jar"
        }
    }
    
    # Configurar variables de entorno para la build
    $env:GRADLE_OPTS = "-Xmx4G -XX:+UseParallelGC -Dorg.gradle.project.minecraft.runs.skipDownload=true"
    
    # Ejecutar sólo las tareas para los loaders compatibles
    if ($buildTasks.Length -gt 0) {
        Write-Host "Ejecutando: gradlew $($buildTasks -join ' ')" -ForegroundColor Yellow
        & .\gradlew.bat $buildTasks --parallel --build-cache --configure-on-demand --max-workers=4
    } else {
        Write-Host "No hay loaders compatibles para esta versión." -ForegroundColor Red
        continue
    }

    # Verificar si la compilación fue exitosa
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Falló la compilación para Minecraft ${version}. Continuando con la siguiente versión..." -ForegroundColor Red
        continue
    } else {
        Write-Host "Compilación exitosa para Minecraft ${version}" -ForegroundColor Green
    }

    # Crear la carpeta de builds para esta versión si no existe
    $buildFolder = "builds/${version}"
    if (!(Test-Path $buildFolder)) {
        New-Item -ItemType Directory -Path $buildFolder -Force | Out-Null
    }

    # Mover los archivos JAR a la carpeta de builds
    Write-Host "Moviendo artefactos a la carpeta builds/${version}..." -ForegroundColor Yellow

    # Copiar JAR solo para los loaders compatibles
    foreach ($loader in $loaders) {
        $jarFolder = "${loader}/build/libs"
        if (Test-Path $jarFolder) {
            Write-Host "Buscando JARs en $jarFolder..." -ForegroundColor Yellow
            $jarFiles = Get-ChildItem -Path $jarFolder -Filter "*.jar"
            
            if ($jarFiles.Count -eq 0) {
                Write-Host "   No se encontraron archivos JAR en $jarFolder" -ForegroundColor Yellow
            } else {
                Write-Host "   Encontrados $($jarFiles.Count) archivos JAR" -ForegroundColor Green
                foreach ($jar in $jarFiles) {
                    if ($jar.Name -notmatch "-sources" -and $jar.Name -notmatch "-dev") {
                        Write-Host "   Copiando $($jar.Name) a $buildFolder" -ForegroundColor Green
                        Copy-Item -Path $jar.FullName -Destination $buildFolder -Force
                    } else {
                        Write-Host "   Omitiendo $($jar.Name) (archivo de desarrollo/fuentes)" -ForegroundColor DarkGray
                    }
                }
            }
        } else {
            Write-Host "La carpeta $jarFolder no existe" -ForegroundColor Yellow
        }
    }

    # Verificar archivos copiados
    if (Test-Path $buildFolder) {
        $copiedFiles = Get-ChildItem -Path $buildFolder -Filter "*.jar"
        if ($copiedFiles.Count -eq 0) {
            Write-Host "ADVERTENCIA: No se copió ningún archivo JAR a $buildFolder" -ForegroundColor Red
            
            # Buscar JARs en todas las carpetas build
            Write-Host "Buscando JARs en todo el proyecto..." -ForegroundColor Yellow
            $allJars = Get-ChildItem -Path . -Recurse -Filter "*.jar" | Where-Object { $_.DirectoryName -like "*build*" }
            if ($allJars.Count -gt 0) {
                Write-Host "   Encontrados $($allJars.Count) archivos JAR en otras ubicaciones:" -ForegroundColor Green
                foreach ($jar in $allJars) {
                    Write-Host "   - $($jar.FullName)"
                    # Copiar todos los JARs encontrados (excepto los -sources y -dev)
                    if ($jar.Name -notmatch "-sources" -and $jar.Name -notmatch "-dev") {
                        Copy-Item -Path $jar.FullName -Destination $buildFolder -Force
                        Write-Host "     Copiado a $buildFolder" -ForegroundColor Green
                    }
                }
            } else {
                Write-Host "   No se encontraron archivos JAR en ninguna parte" -ForegroundColor Red
            }
        } else {
            Write-Host "`nSe copiaron $($copiedFiles.Count) archivos JAR a $buildFolder" -ForegroundColor Green
            foreach ($file in $copiedFiles) {
                Write-Host "  - $($file.Name)" -ForegroundColor Green
            }
        }
    }

    Write-Host "`nCompletada la construcción para Minecraft ${version}" -ForegroundColor Green
    Write-Host "------------------------------------------------------------" -ForegroundColor DarkGray
    
    # Restaurar el archivo settings.gradle original al final
    if ($version -eq $versions[-1].Version) {
        # Restaurar el archivo original completo
        $originalSettings = @"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    name = 'Fabric'
                    url = uri("https://maven.fabricmc.net")
                }
            }
            filter {
                includeGroup("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = 'NeoForge'
                    url = uri("https://maven.neoforged.net/releases")
                }
            }
            filter {
                includeGroupAndSubgroups("net.neoforged")
                includeGroup("codechicken")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = 'Forge'
                    url = uri("https://maven.minecraftforge.net")
                }
            }
            filter {
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = 'Sponge Snapshots'
                    url = uri("https://repo.spongepowered.org/repository/maven-public")
                }
            }
            filter {
                includeGroupAndSubgroups("org.spongepowered")
            }
        }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

dependencyResolutionManagement {
    versionCatalogs {
        versions {
            from(files("gradle/versions.toml"))
        }
    }
}

// This should match the folder name of the project, or else IDEA may complain
rootProject.name = 'SeamlessTrading'
include("common")
include("fabric")
include("forge")
include("neoforge")
"@
        $originalSettings | Set-Content "settings.gradle"
    }
}

# Limpiar recursos temporales al terminar
& .\gradlew.bat --stop
Write-Host "`n¡Proceso completado! Todas las versiones han sido construidas." -ForegroundColor Cyan

# Mostrar resumen de builds
Write-Host "`nResumen de archivos generados:" -ForegroundColor Green
$buildFolders = Get-ChildItem -Path "builds" -Directory
foreach ($folder in $buildFolders) {
    $jarCount = (Get-ChildItem -Path $folder.FullName -Filter "*.jar").Count
    if ($jarCount -gt 0) {
        Write-Host "  ✅ $($folder.Name): $jarCount archivos JAR" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $($folder.Name): Sin archivos JAR" -ForegroundColor Red
    }
} 
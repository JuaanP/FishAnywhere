# Script para construir el mod para Minecraft 1.21.3
$version = "1.21.3"
$loaders = @("fabric", "forge", "neoforge")
$javaVersion = "21"

Write-Host "Iniciando construcción para Minecraft $version" -ForegroundColor Cyan

# Verificar que existe el JDK correcto
$jdkPath = "C:/Program Files/Java/jdk-21"
if (!(Test-Path $jdkPath)) {
    Write-Host "ADVERTENCIA: No se encuentra el JDK 21, intentando usar JDK 17..." -ForegroundColor Yellow
    $jdkPath = "C:/Program Files/Java/jdk-17"
    if (!(Test-Path $jdkPath)) {
        Write-Host "ERROR: No se encuentra ningún JDK compatible" -ForegroundColor Red
        exit 1
    }
}
$gradleJavaHome = $jdkPath.Replace("\", "/")

# Crear carpeta de builds si no existe
$buildFolder = "builds/${version}"
if (!(Test-Path $buildFolder -PathType Container)) {
    New-Item -ItemType Directory -Path $buildFolder -Force | Out-Null
}

# Configurar settings.gradle para incluir todos los loaders
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

rootProject.name = 'SeamlessTrading'
include 'common'
include 'fabric'
include 'forge'
include 'neoforge'
"@

$settingsGradle | Set-Content "settings.gradle"

# Actualizar gradle.properties
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

minecraft_version=$version
minecraft_version_range=[1.21.3, 1.22)
java_version=21

# Fabric
fabric_version=0.93.2+1.21.3
fabric_loader_version=0.16.10

# Forge
forge_version=52.0.63
forge_loader_version_range=[4,)

# NeoForge
neoforge_version=21.3.0
neo_form_version=1.21.3-20240808.144430
neoforge_loader_version_range=[1,)

# ModMenu
modmenu_version=11.0.3

# Gradle - Configuraciones optimizadas para builds rápidas
org.gradle.jvmargs=-Xmx4G -XX:MaxMetaspaceSize=1G -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=false
org.gradle.java.home=$gradleJavaHome
org.gradle.configureondemand=true

# Configuraciones para Minecraft mods (optimiza la compilación)
fabric.loom.multiProjectOptimisation=true
loom.platform=forge
generate_sources=false
generate_javadocs=false
"@

$gradleProps | Set-Content "gradle.properties"

# Limpiar proyecto
Write-Host "Limpiando proyecto..." -ForegroundColor Yellow
& .\gradlew.bat clean --configure-on-demand --max-workers=4

# Construir
Write-Host "Compilando para Minecraft $version..." -ForegroundColor Green
$env:GRADLE_OPTS = "-Xmx4G -XX:+UseParallelGC -Dorg.gradle.project.minecraft.runs.skipDownload=true"

# Tareas específicas para cada loader
$buildTasks = @()
foreach ($loader in $loaders) {
    if ($loader -eq "fabric") { $buildTasks += ":${loader}:remapJar" }
    elseif ($loader -eq "forge") { $buildTasks += ":${loader}:jar" }
    elseif ($loader -eq "neoforge") { $buildTasks += ":${loader}:jar" }
}

& .\gradlew.bat $buildTasks --parallel --build-cache --configure-on-demand --max-workers=4

# Verificar resultado
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilación exitosa para Minecraft $version" -ForegroundColor Green
    
    # Copiar JARs
    Write-Host "Copiando archivos JAR a $buildFolder..." -ForegroundColor Yellow
    foreach ($loader in $loaders) {
        $jarFolder = "${loader}/build/libs"
        if (Test-Path $jarFolder) {
            $jarFiles = Get-ChildItem -Path $jarFolder -Filter "*.jar"
            foreach ($jar in $jarFiles) {
                if ($jar.Name -notmatch "-sources" -and $jar.Name -notmatch "-dev") {
                    Write-Host "  Copiando $($jar.Name)" -ForegroundColor Green
                    Copy-Item -Path $jar.FullName -Destination $buildFolder -Force
                }
            }
        }
    }
    
    # Verificar archivos copiados
    $copiedFiles = Get-ChildItem -Path $buildFolder -Filter "*.jar"
    Write-Host "Se copiaron $($copiedFiles.Count) archivos JAR a $buildFolder" -ForegroundColor Green
} else {
    Write-Host "La compilación falló para Minecraft $version" -ForegroundColor Red
}

Write-Host "Proceso completado para Minecraft $version" -ForegroundColor Cyan 
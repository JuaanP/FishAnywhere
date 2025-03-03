# Script para cambiar la configuración del proyecto a Minecraft 1.20.2
$version = "1.20.2"
$javaVersion = "17"

Write-Host "Configurando proyecto para Minecraft $version" -ForegroundColor Cyan

# Verificar que existe el JDK correcto
$jdkPath = "C:/Program Files/Java/jdk-$javaVersion"
if (!(Test-Path $jdkPath)) {
    Write-Host "ADVERTENCIA: No se encuentra el JDK $javaVersion en $jdkPath" -ForegroundColor Yellow
    Write-Host "El proyecto podría no funcionar correctamente en el IDE" -ForegroundColor Yellow
}
$gradleJavaHome = $jdkPath.Replace("\", "/")

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
minecraft_version_range=[1.20.2, 1.21)
java_version=$javaVersion

# Fabric
fabric_version=0.91.1+1.20.2
fabric_loader_version=0.15.0

# Forge
forge_version=48.1.0
forge_loader_version_range=[47,)

# NeoForge
neoforge_version=20.2.59
neo_form_version=1.20.2-20240627.114801
neoforge_loader_version_range=[1,)

# ModMenu
modmenu_version=8.0.0

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

# Restaurar settings.gradle para incluir todas las plataformas
$settingsContent = Get-Content "settings.gradle" -Raw
$settingsWithAllPlatforms = $settingsContent -replace "// include 'neoforge' // Desactivado .+", "include 'neoforge'"
$settingsWithAllPlatforms | Set-Content "settings.gradle"

Write-Host "Proyecto configurado para Minecraft $version!" -ForegroundColor Green
Write-Host "Recuerda recargar el proyecto en tu IDE para aplicar los cambios." -ForegroundColor Yellow 
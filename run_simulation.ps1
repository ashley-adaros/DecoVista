# Script para descargar el Gradle Wrapper y correr la simulación de consola en DecoVista
$ErrorActionPreference = "Stop"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Iniciando la preparación del entorno Gradle local...    " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

# 1. Asegurar directorios de Gradle Wrapper
$wrapperDir = "gradle/wrapper"
if (!(Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Force -Path $wrapperDir | Out-Null
    Write-Host "[1/3] Carpeta 'gradle/wrapper' creada." -ForegroundColor Green
} else {
    Write-Host "[1/3] Carpeta 'gradle/wrapper' ya existe." -ForegroundColor Gray
}

# 2. Descargar archivos oficiales de Gradle Wrapper (Versión v8.4.0)
$gradleRepo = "https://raw.githubusercontent.com/gradle/gradle/v8.4.0"

$filesToDownload = @{
    "gradlew" = "$gradleRepo/gradlew"
    "gradlew.bat" = "$gradleRepo/gradlew.bat"
    "gradle/wrapper/gradle-wrapper.jar" = "$gradleRepo/gradle/wrapper/gradle-wrapper.jar"
}

Write-Host "[2/3] Descargando archivos oficiales de Gradle Wrapper..." -ForegroundColor Green
foreach ($file in $filesToDownload.Keys) {
    if (!(Test-Path $file)) {
        Write-Host "      -> Descargando $file..." -ForegroundColor Gray
        Invoke-WebRequest -Uri $filesToDownload[$file] -OutFile $file -UseBasicParsing
    } else {
        Write-Host "      -> $file ya está presente localmente." -ForegroundColor Gray
    }
}

# 3. Dar permisos de ejecución (si estuviera en unix, pero en Windows no es crítico)
# Ejecutar simulación
Write-Host "[3/3] Compilando y ejecutando la simulación (:core:calculator:run)..." -ForegroundColor Green
Write-Host "---------------------------------------------------------" -ForegroundColor Gray

# Ejecutar el wrapper local
.\gradlew.bat :core:calculator:run

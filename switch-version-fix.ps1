function Clean-Project {
    Write-Host "Limpiando proyecto..." -ForegroundColor Yellow
    & .\gradlew.bat clean --configure-on-demand
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Proyecto limpiado correctamente" -ForegroundColor Green
    } else {
        Write-Host "Error al limpiar el proyecto" -ForegroundColor Red
    }
    
    Write-Host "Presiona cualquier tecla para continuar..." -ForegroundColor Yellow
    $null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
} 
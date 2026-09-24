# Este script realiza los siguientes pasos:

# 1. Comprueba que Docker Desktop esté activo, si no lo está,
# lo intenta abrir y espera a que esté listo

# 2. Comprueba que los puertos necesarios para el proyecto no estén ocupados.
# El script no cerrará los procesos que estén usando esos puertos,
# solo avisará al usuario para que lo haga manualmente
# Es posible que el script avise de puertos ocupados por contenedores viejos,
# en ese caso, al realizar -d --build, se volverán a levantar los contenedores
# y se aplicarán los cambios

# 3. Si no existe el archivo .env, lo crea con un secreto JWT aleatorio

# 4. Levanta los contenedores usando docker compose, si ya están levantados,
# los reconstruye para asegurarse de que se apliquen los cambios

Set-Location $PSScriptRoot
Write-Host "--- Iniciando comprobaciones ---" -ForegroundColor Yellow

# Comprobamos que Docker Desktop esté activo, si no lo está, lo intentamos abrir.
# Basicamnente empezamos un bucle que intenta ejecutar "docker ps" cada 5 segundos
# hasta que funcione, lo que indicará que Docker está listo para usarse
$dockerReady = $false
while (-not $dockerReady) {
    & docker ps > $null 2>&1
    # Si el comando se ejecuta correctamente, Docker está listo
    if ($LASTEXITCODE -eq 0) {
        $dockerReady = $true
    } else {
        # Docker no está listo, esperamos un poco y lo intentamos de nuevo
        Write-Host "Esperando a Docker..." -ForegroundColor Cyan
        if (-not (Get-Process "Docker Desktop" -ErrorAction SilentlyContinue)) {
            Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
        }
        Start-Sleep -s 5
    }
}
Write-Host "Docker está listo." -ForegroundColor Green

# Indicamos los puertos que vamos a usar para avisar si alguno está ocupado
# 3306: MySQL
# 8080: Backend Spring Boot
# 4200: Frontend
$ports = @(3306, 8080, 4200)
# Para cada puerto establecido, comprobamos si está en uso.
# Si lo está, avisamos al usuario pero no salimos del script
foreach ($port in $ports) {
    $checkPort = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($checkPort) {
        Write-Host "Puerto $port ocupado. Intenta cerrarlo antes." -ForegroundColor Red
        # No salimos, solo avisamos por si es un contenedor viejo
    }
}

# Si no existe .env, lo creamos con un secreto JWT aleatorio de 32 bytes en Base64.
# docker compose lee este archivo automáticamente y el backend no arranca sin el secreto
if (-not (Test-Path ".env")) {
    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $secret = [Convert]::ToBase64String($bytes)
    Set-Content -Path ".env" -Value "SMARTBOOKING_JWT_SECRET=$secret" -Encoding ascii
    Write-Host "Creado .env con un secreto JWT nuevo." -ForegroundColor Green
}

# Levantamos los contenedores, si ya están levantados, los reconstruimos para asegurarnos de
# que se apliquen los cambios
Write-Host "Lanzando contenedores..." -ForegroundColor Green
docker compose up -d --build
$ErrorActionPreference = "Stop"

# 1. Configurações de Caminhos
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

$FxLib = "C:\Users\Pichau\Downloads\openjfx-25.0.2_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib"
$JarName = "cooperpay-0.0.1-SNAPSHOT.jar"
$AppName = "CooperPay"
$MainClass = "cooperpay.fx.MainApp"

Write-Host "--- Iniciando processo de empacotamento ---" -ForegroundColor Cyan

# 1.5. Garantir que o app não esteja rodando
Write-Host "Verificando se o aplicativo está aberto..."
Stop-Process -Name "CooperPay" -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1 # Aguarda um instante para o Windows liberar os arquivos

# 2. Gerar o JAR via Maven
Write-Host "Limpando e compilando JAR..."
.\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Erro ao gerar o JAR com Maven." }

# 3. Limpar pasta de destino
if (Test-Path "dist") { Remove-Item "dist" -Recurse -Force }
New-Item -ItemType Directory -Path "dist" | Out-Null

# 4. Usar jpackage para criar o App Image (Não requer WiX Toolset)
# Nota: Ele vai incluir o JRE e os módulos do JavaFX
Write-Host "Gerando imagem do aplicativo (Pasta portatil em dist\CooperPay)..."
jpackage `
  --type app-image `
  --dest dist `
  --name $AppName `
  --input target `
  --main-jar $JarName `
  --main-class $MainClass `
  --module-path $FxLib `
  --add-modules javafx.controls,javafx.fxml `
  --vendor "CooperPay" `
  --description "Sistema de Gerenciamento de Pagamentos Pix"

Write-Host "Concluido! A pasta portatil esta em 'dist\CooperPay'." -ForegroundColor Green
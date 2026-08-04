$ErrorActionPreference = "Stop"

# 1. Configurações de Caminhos
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

# RECOMENDAÇÃO: Use o caminho para a pasta JMODS (baixe o arquivo jmods no site da Gluon)
$FxJmods = "C:\Users\Pichau\Downloads\openjfx-23.0.2_windows-x64_bin-jmods\javafx-jmods-23.0.2"
$JarName = "cooperpay-0.0.1-SNAPSHOT.jar"
$AppName = "CooperPay"
$MainClass = "cooperpay.fx.MainLauncher"

Write-Host "--- Iniciando processo de empacotamento ---" -ForegroundColor Cyan

# 1.5. Garantir que o app não esteja rodando para liberar os arquivos
Write-Host "Verificando se o aplicativo está aberto..."
# Tenta parar o executável gerado (CooperPay.exe)
Get-Process -Name "CooperPay*" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
# Tenta parar o javaw.exe que possa estar rodando dentro da pasta da aplicação
Get-Process | Where-Object { $_.Path -like "*\dist\CooperPay\*" -and $_.ProcessName -eq "javaw" } -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "Aguardando 3 segundos para o sistema liberar os arquivos..."
Start-Sleep -Seconds 3 # Dá mais tempo para o Windows liberar os arquivos após o encerramento

# 2. Gerar o JAR via Maven
Write-Host "Limpando, compilando e copiando dependencias..."
.\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Erro ao gerar o JAR com Maven." }

# Copia todas as bibliotecas do Spring para uma pasta temporária para o jpackage
New-Item -ItemType Directory -Path "target\libs" -Force | Out-Null
.\mvnw.cmd dependency:copy-dependencies "-DoutputDirectory=target\libs" -DincludeScope=runtime

# IMPORTANTE: Para o jpackage, precisamos do JAR "fino" (original).
# O Spring Boot renomeia o JAR original para .jar.original durante o build.
if (Test-Path "target\$JarName.original") {
    Copy-Item "target\$JarName.original" -Destination "target\libs\$JarName"
} else {
    # Fallback, mas isso pode causar problemas se for um fat JAR e o jpackage não conseguir encontrar a MainClass
    Write-Warning "Original JAR not found at target\$JarName.original. Using the main JAR, which might be a fat JAR and cause issues with jpackage."
    Copy-Item "target\$JarName" -Destination "target\libs\"
}

# 3. Limpar pasta de destino
$maxRetries = 5
$retryDelaySeconds = 1
$removed = $false

if (Test-Path "dist") {
    Write-Host "Tentando remover pasta 'dist'..." -ForegroundColor Yellow
    for ($i = 0; $i -lt $maxRetries; $i++) {
        try {
            Remove-Item "dist" -Recurse -Force -ErrorAction Stop
            $removed = $true
            Write-Host "Pasta 'dist' removida com sucesso." -ForegroundColor Green
            break
        } catch {
            Write-Warning "Falha ao remover 'dist' (tentativa $($i+1)/$maxRetries): $($_.Exception.Message)"
            if ($i -lt ($maxRetries - 1)) {
                Write-Host "Aguardando $retryDelaySeconds segundos antes de tentar novamente..."
                Start-Sleep -Seconds $retryDelaySeconds
            }
        }
    }
    if (-not $removed) {
        throw "Nao foi possivel remover a pasta 'dist' apos $maxRetries tentativas. Verifique se algum processo a esta usando."
    }
}
New-Item -ItemType Directory -Path "dist" | Out-Null

# 4. Usar jpackage para criar o App Image (Não requer WiX Toolset)
# Nota: Ele vai incluir o JRE e os módulos do JavaFX
Write-Host "Gerando imagem do aplicativo (Pasta portatil em dist\CooperPay)..."
jpackage `
  --type app-image `
  --dest dist `
  --name $AppName `
  --input target\libs `
  --main-jar $JarName `
  --main-class $MainClass `
  --module-path $FxJmods `
  --add-modules javafx.controls,javafx.fxml,java.logging,java.sql,java.naming,java.desktop,jdk.crypto.ec,java.management,java.instrument,java.security.jgss,jdk.unsupported,java.net.http `
  --java-options "--enable-native-access=ALL-UNNAMED,javafx.graphics -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dprism.verbose=true" `
  --win-console `
  --description "Sistema de Gerenciamento de Pagamentos Pix"

Write-Host "Concluido! A pasta portatil esta em 'dist\CooperPay'." -ForegroundColor Green
$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

# Caminho atualizado conforme sua extração do SDK 23.0.2
$FxLib = "C:\Users\Pichau\Downloads\openjfx-23.0.2_windows-x64_bin-sdk\javafx-sdk-23.0.2\lib"
$OutDir = "out2"

$MainClass = "cooperpay.fx.MainLauncher"

if (-not (Test-Path $FxLib)) {
    throw "Pasta JavaFX lib nao encontrada em: $FxLib"
}

# Limpamos apenas a pasta out2 para garantir que o cliente pegue os recursos novos
if (Test-Path $OutDir) {
    Write-Host "Limpando pasta de saida..."
    Remove-Item -Path $OutDir -Recurse -Force -ErrorAction SilentlyContinue
}
New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
Start-Sleep -Milliseconds 500

# Compilamos apenas se a pasta target não existir ou se houver mudanças, 
# mas sem o 'clean' para não derrubar o servidor que já estiver rodando.
Write-Host "Compilando projeto completo (Maven)..."
.\mvnw.cmd compile -DskipTests
if ($LASTEXITCODE -ne 0) {
    throw "Falha na compilacao Maven."
}

Write-Host "Sincronizando bibliotecas (Spring Boot)..."
$DependencyDir = "target\dependency"
if (-not (Test-Path $DependencyDir)) { New-Item -ItemType Directory -Path $DependencyDir | Out-Null }

.\mvnw.cmd dependency:copy-dependencies "-DoutputDirectory=$DependencyDir" -DincludeScope=runtime
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao copiar dependencias Maven."
}

Write-Host "Executando..."
# Construindo classpath absoluto para carregar o Spring corretamente
$FullCP = "target\classes;target\dependency\*"
java "-Dfile.encoding=UTF-8" "-Dsun.jnu.encoding=UTF-8" --enable-native-access=ALL-UNNAMED --module-path "$FxLib" --add-modules javafx.controls,javafx.fxml -cp "$FullCP" $MainClass
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao executar MainApp."
}

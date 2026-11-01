$ErrorActionPreference = "Stop"

$sdkDir = "C:\Users\edson\AppData\Local\Android\Sdk"
$buildToolsDir = "$sdkDir\build-tools\35.0.0"
$platformJar = "$sdkDir\platforms\android-35\android.jar"
$javaDir = "C:\Program Files\Java\jdk-17\bin"
$appDir = "C:\topicos-em-ia\tec\android_app"

Write-Host ">>> Iniciando compilação do APK Comanda Falada..."

Set-Location $appDir

# Criar diretórios limpos
if (Test-Path "build") { Remove-Item -Recurse -Force "build" }
New-Item -ItemType Directory -Force -Path "build\gen" | Out-Null
New-Item -ItemType Directory -Force -Path "build\obj" | Out-Null
New-Item -ItemType Directory -Force -Path "build\dex" | Out-Null
New-Item -ItemType Directory -Force -Path "build\bin" | Out-Null

# 1. Compilar recursos com AAPT2
Write-Host "[1/6] Compilando recursos com aapt2..."
& "$buildToolsDir\aapt2.exe" compile --dir res -o build\resources.zip

# 2. Vincular recursos e gerar R.java e APK base
Write-Host "[2/6] Vinculando recursos e gerando R.java..."
& "$buildToolsDir\aapt2.exe" link -I $platformJar --manifest AndroidManifest.xml -o build\bin\app-unaligned.apk --java build\gen build\resources.zip --auto-add-overlay

# 3. Compilar classes Java
Write-Host "[3/6] Compilando classes Java com javac..."
$javaFiles = Get-ChildItem -Recurse -Include *.java "$appDir\src", "$appDir\build\gen" | ForEach-Object { $_.FullName }
& "$javaDir\javac.exe" -encoding UTF-8 -source 8 -target 8 -cp $platformJar -d build\obj $javaFiles

# 4. Converter bytecode Java em Dalvik DEX com D8
Write-Host "[4/6] Convertendo para DEX com d8..."
$classFiles = Get-ChildItem -Recurse -Include *.class "$appDir\build\obj" | ForEach-Object { $_.FullName }
& "$buildToolsDir\d8.bat" --lib $platformJar --output build\dex $classFiles

# Adicionar classes.dex ao APK base usando jar/zip
Set-Location "$appDir\build\dex"
& "$javaDir\jar.exe" -uf "$appDir\build\bin\app-unaligned.apk" classes.dex
Set-Location $appDir

# 5. Alinhar APK com zipalign
Write-Host "[5/6] Alinhando APK com zipalign..."
& "$buildToolsDir\zipalign.exe" -v -p -f 4 "$appDir\build\bin\app-unaligned.apk" "$appDir\build\bin\app-aligned.apk"

# 6. Criar keystore de depuração se não existir e assinar
$keystorePath = "$appDir\debug.keystore"
if (-not (Test-Path $keystorePath)) {
    Write-Host "Gerando chave debug para assinatura..."
    & "$javaDir\keytool.exe" -genkey -v -keystore $keystorePath -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
}

Write-Host "[6/6] Assinando APK com apksigner..."
& "$buildToolsDir\apksigner.bat" sign --ks $keystorePath --ks-pass pass:android --key-pass pass:android --out "$appDir\build\bin\comanda-falada.apk" "$appDir\build\bin\app-aligned.apk"

# Copiar para o destino final em tec/
Copy-Item "$appDir\build\bin\comanda-falada.apk" "C:\topicos-em-ia\tec\comanda-falada.apk" -Force

Write-Host ">>> APK gerado com sucesso em: C:\topicos-em-ia\tec\comanda-falada.apk"
Get-Item "C:\topicos-em-ia\tec\comanda-falada.apk" | Select-Object Name, Length, LastWriteTime

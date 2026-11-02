# Script de automação e gravação da tela do celular Android
Write-Host "Iniciando gravacao de tela no aparelho Samsung Galaxy A15..."

# Iniciar gravacao em background no Android
$screenrecordJob = Start-Job -ScriptBlock {
    adb shell screenrecord --size 720x1560 --bit-rate 4000000 /sdcard/android_demo.mp4
}

Start-Sleep -Seconds 2

# Garantir que o app esta aberto e visivel
adb shell am start -n br.ufrpe.comandafalada/.MainActivity
Start-Sleep -Seconds 4

# Caso 1: Buscar Bar Acustico
Write-Host "Clicando no Caso 1..."
adb shell input tap 540 642
Start-Sleep -Seconds 10

# Caso 2: Fazer Pedido e Pix
Write-Host "Clicando no Caso 2..."
adb shell input tap 540 817
Start-Sleep -Seconds 12

# Caso 3: Teste sob Ruido de Bar
Write-Host "Clicando no Caso 3..."
adb shell input tap 540 992
Start-Sleep -Seconds 12

# Finalizar gravacao
Write-Host "Finalizando gravacao no celular..."
adb shell pkill -INT screenrecord
Start-Sleep -Seconds 3

# Puxar o arquivo para o PC
adb pull /sdcard/android_demo.mp4 C:\topicos-em-ia\tec\android_demo.mp4

Write-Host "Concluido com sucesso!"

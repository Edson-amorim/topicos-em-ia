Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer

# Escolher a melhor voz disponivel (Leticia ou Maria)
$installed = $synth.GetInstalledVoices() | ForEach-Object { $_.VoiceInfo.Name }
if ($installed -contains "Letícia-F123") {
    $synth.SelectVoice("Letícia-F123")
    Write-Host "Usando voz: Leticia-F123"
} else {
    $synth.SelectVoice("Microsoft Maria Desktop")
    Write-Host "Usando voz: Microsoft Maria Desktop"
}

$synth.Rate = 0  # Velocidade natural
$audioOut = "C:\topicos-em-ia\tec\narracao_completa.wav"
$synth.SetOutputToWaveFile($audioOut)

$texto = @"
Olá! Esta é a demonstração tecnológica do projeto Comanda Falada, do aluno Edson Amorim, para a disciplina Tópicos Avançados em Inteligência Artificial na UFRPE.
No terminal, verificamos no histórico do git os dois commits oficiais: o commit 84bb835 com o exemplo oficial do faster-whisper em CPU, e o commit c032f22 com a alteração mínima completa e APK Android.
Ao executar o pipeline no computador, integramos o transformer de voz ao grafo de conhecimento de bares calmos, ao cálculo exato da comanda e à geração do código Pix.
Agora vemos o aplicativo nativo instalado e funcionando diretamente na tela do aparelho celular Android Samsung Galaxy A15 do aluno.
No Caso 1, ao tocar para buscar um bar calmo, a busca por grafo seleciona o Bar Cais do Sertão, com baixo ruído de 58 decibéis, espaço acessível e a 130 metros de distância calculados por GPS.
No Caso 2, realizamos o pedido de caldinho e suco de laranja: as regras determinísticas somam a conta exata de 22 reais sem erro de centavos, geram a senha falada 8186 e o código Pix Copia e Cola.
No Caso 3, testamos o áudio gravado sob ruído real de bar: o modelo acústico comete falha fonética, confundindo a palavra chopes com corpos, comprovando com honestidade por que o usuário não pode delegar tudo cegamente para a inteligência artificial, sendo indispensável a confirmação na tela acessível com TalkBack e o fechamento por regras matemáticas exatas.
Muito obrigado!
"@

$synth.Speak($texto)
$synth.Dispose()

Write-Host "Audio sintetizado com sucesso em $audioOut"

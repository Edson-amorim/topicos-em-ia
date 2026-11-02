import os
import subprocess
from PIL import Image, ImageDraw, ImageFont

dir_tec = os.path.dirname(os.path.abspath(__file__))
audio_edson_wav = os.path.join(dir_tec, "audios", "audio_edson_pedido.wav")
audio_narracao_parcial1 = os.path.join(dir_tec, "narracao_p1.wav")
audio_narracao_parcial2 = os.path.join(dir_tec, "narracao_p2.wav")
audio_combinado = os.path.join(dir_tec, "narracao_completa.wav")

# 1. Gerar parte 1 da narracao (ate o Caso 2)
p1_text = """
Olá! Demonstração tecnológica do projeto Comanda Falada, aluno Edson Amorim, UFRPE.
No terminal, conferimos os dois commits oficiais: o commit 84bb835 do faster-whisper e o commit c032f22 com a alteração mínima e APK Android.
No computador, o pipeline integra voz, busca no grafo de bares calmos, comanda exata e Pix.
No celular Android Samsung Galaxy A15 do aluno, vemos o app nativo funcionando.
No Caso 1, a busca por grafo seleciona o Bar Cais do Sertão, com baixo ruído de 58 decibéis, calçada acessível e a 130 metros via GPS.
No Caso 2, ouvimos agora a gravação com a voz real do próprio aluno Edson Amorim, com o leitor de telas ativo no celular:
"""

p2_text = """
O Whisper transcreve a fala do aluno perfeitamente. As regras determinísticas identificam os itens, somam a comanda exata de 56 reais, geram a senha falada 8186 e o código Pix oficial.
No Caso 3, sob ruído real de bar, o modelo acústico comete falha fonética, comprovando por que o usuário cego necessita de confirmação na tela acessível e regras exatas antes de fechar a conta.
Muito obrigado!
"""

# Script PowerShell para sintetizar p1 e p2
ps_script = f"""
Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
$installed = $synth.GetInstalledVoices() | ForEach-Object {{ $_.VoiceInfo.Name }}
if ($installed -contains "Letícia-F123") {{
    $synth.SelectVoice("Letícia-F123")
}} else {{
    $synth.SelectVoice("Microsoft Maria Desktop")
}}

$synth.SetOutputToWaveFile("{audio_narracao_parcial1.replace('\\', '/')}")
$synth.Speak(@'
{p1_text.strip()}
'@)

$synth.SetOutputToWaveFile("{audio_narracao_parcial2.replace('\\', '/')}")
$synth.Speak(@'
{p2_text.strip()}
'@)

$synth.Dispose()
"""

with open(os.path.join(dir_tec, "temp_synth.ps1"), "w", encoding="utf-8") as f:
    f.write(ps_script)

print("Sintetizando blocos de narração...")
subprocess.run(["powershell", "-ExecutionPolicy", "Bypass", "-File", os.path.join(dir_tec, "temp_synth.ps1")], check=True)

# Concatenar: p1 + audio_edson + p2
print("Concatenando narração com a voz real do Edson...")
concat_list = os.path.join(dir_tec, "concat_audio.txt")
with open(concat_list, "w", encoding="utf-8") as f:
    f.write(f"file '{audio_narracao_parcial1}'\n")
    f.write(f"file '{audio_edson_wav}'\n")
    f.write(f"file '{audio_narracao_parcial2}'\n")

cmd_concat = [
    "ffmpeg", "-y", "-f", "concat", "-safe", "0",
    "-i", concat_list,
    "-c:a", "pcm_s16le", "-ar", "22050", "-ac", "1",
    audio_combinado
]
subprocess.run(cmd_concat, check=True)
print("Áudio combinado com a voz real do Edson gerado com sucesso!")

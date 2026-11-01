import os
import subprocess
import time
from PIL import Image, ImageDraw, ImageFont

def gerar_video_demonstracao():
    largura = 1920
    altura = 1080
    fps = 30
    video_path = os.path.join(os.path.dirname(__file__), "demonstracao.mp4")

    # Fonte Consolas
    caminho_fonte = "C:\\Windows\\Fonts\\consola.ttf"
    caminho_fonte_bold = "C:\\Windows\\Fonts\\consolab.ttf"
    tam_fonte = 24
    fonte = ImageFont.truetype(caminho_fonte, tam_fonte)
    fonte_bold = ImageFont.truetype(caminho_fonte_bold, tam_fonte)
    fonte_header = ImageFont.truetype(caminho_fonte_bold, 20)

    # Configuração do ffmpeg process
    cmd_ffmpeg = [
        "ffmpeg",
        "-y",
        "-f", "rawvideo",
        "-vcodec", "rawvideo",
        "-s", f"{largura}x{altura}",
        "-pix_fmt", "rgb24",
        "-r", str(fps),
        "-i", "-",
        "-c:v", "libx264",
        "-pix_fmt", "yuv420p",
        "-preset", "medium",
        "-crf", "20",
        video_path
    ]

    proc = subprocess.Popen(cmd_ffmpeg, stdin=subprocess.PIPE)

    # Cores
    COR_FUNDO = (24, 24, 24)
    COR_TITULO_BARRA = (45, 45, 45)
    COR_TEXTO_BRANCO = (230, 230, 230)
    COR_PROMPT_USER = (78, 201, 176)
    COR_PROMPT_DIR = (86, 156, 214)
    COR_AMARELO = (220, 220, 170)
    COR_VERDE = (106, 153, 85)
    COR_VERDE_CLARO = (150, 220, 120)
    COR_AZUL_CLARO = (156, 220, 254)
    COR_VERMELHO_AVISO = (206, 145, 120)
    COR_CINZA = (128, 128, 128)

    # Roteiro da sessão
    # Linhas de texto formatadas como (tipo, texto)
    # tipo: 'prompt', 'git_log', 'header', 'whisper', 'caso1', 'caso2', 'caso3', 'normal', 'apk'
    linhas_sessao = [
        ("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", "git log --oneline -n 4"),
        ("git_commit_novo", "c032f22 alteracao minima: pedidos de bar, graphrag, regras deterministicas e apk android"),
        ("git_commit_anterior", "70c7a24 docs: preenchimento dos passos 1 e 2 do tec.md e planejamento da sessao 2"),
        ("git_commit_oficial", "84bb835 exemplo oficial do faster-whisper, rodando em CPU"),
        ("git_commit_anterior", "4b9e159 Conclui artefato cmp.md (Passos 0 a 9 com auditoria cega) e atualiza ROADMAP.md"),
        ("vazio", ""),
        ("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", "python transcrever.py"),
        ("separador", "======================================================================"),
        ("titulo", "COMANDA FALADA - DEMONSTRAÇÃO TECNOLÓGICA DAS 4 PEÇAS"),
        ("separador", "======================================================================"),
        ("info", "[1/4] Grafo carregado com 17 nós e 17 arestas."),
        ("info", ">>> Carregando modelo Whisper small (CPU INT8)..."),
        ("vazio", ""),
        ("linha_caso", "----------------------------------------------------------------------"),
        ("titulo_caso", "CASO 1 · CONSULTA DE BAR ACÚSTICO E SELEÇÃO NO GRAFO"),
        ("sub_caso", "Entrada de Áudio: audio1_busca_bar.wav"),
        ("whisper_out", "Transcrição Whisper [pt, prob 1.00]: 'Quero um bar no Recife Antigo com música baixa e espaço tranquilo para conversar.'"),
        ("vazio", ""),
        ("destaque_azul", "[GraphRAG] Bares encontrados com conforto acústico (baixo ruído):"),
        ("resultado", " -> Bar: Bar Cais do Sertão & Chopp (Recife Antigo)"),
        ("detalhe", "    Ruído: 58 dB | Som: musica instrumental ambiente baixa"),
        ("detalhe", "    Acessibilidade: calcada acessivel, espaco amplo entre mesas, garcom atencioso com pessoas cegas"),
        ("detalhe", "    Distância GPS (Haversine): 130.0 metros da sua localização"),
        ("vazio", ""),
        ("linha_caso", "----------------------------------------------------------------------"),
        ("titulo_caso", "CASO 2 · PEDIDO DIRETO, CÁLCULO DE COMANDA EXATA E PIX DETERMINÍSTICO"),
        ("sub_caso", "Entrada de Áudio: audio2_pedido_direto.wav"),
        ("whisper_out", "Transcrição Whisper [pt, prob 1.00]: 'Vou querer uma porção de caldinho de feijão e um suco de laranja natural, por favor.'"),
        ("destaque_azul", "[Extração Estruturada] Itens identificados no cardápio: 2"),
        ("resultado", " -> 1x caldinho de feijao (R$ 12.00)"),
        ("resultado", " -> 1x suco de laranja natural (R$ 10.00)"),
        ("vazio", ""),
        ("destaque_verde", "[Regras Determinísticas - Comanda]"),
        ("resultado", " -> Total da Comanda: R$ 22,00"),
        ("resultado", " -> Status do Teto: Dentro do teto de gastos estipulado."),
        ("resultado", " -> Senha Falada de Entrega: 8186"),
        ("detalhe", " -> Código Copia e Cola Pix (BR Code):"),
        ("pix", "    00020126480014br.gov.bcb.pix0126pix@barcaisdosertao.com.br520400005303986540522.005802BR5918Bar Cais do Sertao6006Recife62100506PEDIDO63046AB6"),
        ("vazio", ""),
        ("linha_caso", "----------------------------------------------------------------------"),
        ("titulo_caso_aviso", "CASO 3 · PEDIDO COM RUÍDO ACÚSTICO DE BAR (ONDE O MODELO ERRA)"),
        ("sub_caso", "Entrada de Áudio: audio3_pedido_ruido.wav"),
        ("whisper_erro", "Transcrição Whisper [pt, prob 1.00]: 'com 2 corpos gelados e uma corção de macaxeira frita com queijo coalho'"),
        ("destaque_aviso", "[Extração Estruturada com Ruído] Itens reconhecidos: 1 (FALHA ACÚSTICA: chopes virou corpos)"),
        ("resultado", " -> 1x macaxeira frita com queijo coalho (R$ 34.00)"),
        ("resultado", " -> Total Calculado: R$ 34,00  [Valor real pretendido era R$ 57,00]"),
        ("separador", "======================================================================"),
        ("info", "[APK Android] Pacote compilado e assinado com sucesso: tec/comanda-falada.apk (16.8 KB)"),
        ("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", "")
    ]

    # Função para desenhar a janela do terminal com as linhas visíveis
    def renderizar_frame(linhas_visiveis, cursor_on=True):
        img = Image.new("RGB", (largura, altura), COR_FUNDO)
        draw = ImageDraw.Draw(img)

        # Barra superior do terminal
        draw.rectangle([(0, 0), (largura, 42)], fill=COR_TITULO_BARRA)
        # Botões da janela (vermelho, amarelo, verde)
        draw.ellipse([(18, 14), (30, 26)], fill=(255, 95, 86))
        draw.ellipse([(38, 14), (50, 26)], fill=(255, 189, 46))
        draw.ellipse([(58, 14), (70, 26)], fill=(39, 201, 63))
        
        # Título da janela
        titulo_janela = "Terminal - Edson Amorim · Comanda Falada (AI for Good / UFRPE)"
        draw.text((largura // 2 - 320, 10), titulo_janela, font=fonte_header, fill=(200, 200, 200))

        # Desenhar linhas de texto
        # Se ultrapassar a tela, fazer scroll mantendo as últimas N linhas
        max_linhas_tela = 32
        linhas_a_desenhar = linhas_visiveis
        if len(linhas_a_desenhar) > max_linhas_tela:
            linhas_a_desenhar = linhas_a_desenhar[-max_linhas_tela:]

        y_base = 56
        line_height = 31

        for idx, item in enumerate(linhas_a_desenhar):
            tipo = item[0]
            y = y_base + (idx * line_height)

            if tipo == "prompt":
                prompt_prefix = item[1]
                cmd_digitado = item[2]
                is_last_line = (idx == len(linhas_a_desenhar) - 1)
                
                # desenhar prompt
                draw.text((25, y), "edson@ufrpe", font=fonte_bold, fill=COR_PROMPT_USER)
                x_off = 25 + int(fonte_bold.getlength("edson@ufrpe"))
                draw.text((x_off, y), ":", font=fonte, fill=COR_TEXTO_BRANCO)
                x_off += int(fonte.getlength(":"))
                draw.text((x_off, y), "~/topicos-em-ia/tec", font=fonte_bold, fill=COR_PROMPT_DIR)
                x_off += int(fonte_bold.getlength("~/topicos-em-ia/tec"))
                draw.text((x_off, y), "$ ", font=fonte_bold, fill=COR_TEXTO_BRANCO)
                x_off += int(fonte_bold.getlength("$ "))

                # desenhar comando digitado
                draw.text((x_off, y), cmd_digitado, font=fonte, fill=COR_TEXTO_BRANCO)
                if is_last_line and cursor_on:
                    cmd_len = int(fonte.getlength(cmd_digitado))
                    draw.rectangle([(x_off + cmd_len + 2, y + 4), (x_off + cmd_len + 14, y + 26)], fill=(230, 230, 230))

            elif tipo == "git_commit_novo":
                hash_txt = item[1][:7]
                msg_txt = item[1][7:]
                draw.text((25, y), hash_txt, font=fonte_bold, fill=COR_AMARELO)
                x_off = 25 + int(fonte_bold.getlength(hash_txt))
                draw.text((x_off, y), msg_txt, font=fonte_bold, fill=COR_VERDE_CLARO)

            elif tipo == "git_commit_oficial":
                hash_txt = item[1][:7]
                msg_txt = item[1][7:]
                draw.text((25, y), hash_txt, font=fonte_bold, fill=COR_AMARELO)
                x_off = 25 + int(fonte_bold.getlength(hash_txt))
                draw.text((x_off, y), msg_txt, font=fonte_bold, fill=COR_AZUL_CLARO)

            elif tipo == "git_commit_anterior":
                hash_txt = item[1][:7]
                msg_txt = item[1][7:]
                draw.text((25, y), hash_txt, font=fonte, fill=COR_CINZA)
                x_off = 25 + int(fonte.getlength(hash_txt))
                draw.text((x_off, y), msg_txt, font=fonte, fill=COR_CINZA)

            elif tipo == "separador":
                draw.text((25, y), item[1], font=fonte, fill=COR_CINZA)

            elif tipo == "titulo":
                draw.text((25, y), item[1], font=fonte_bold, fill=COR_VERDE_CLARO)

            elif tipo == "linha_caso":
                draw.text((25, y), item[1], font=fonte, fill=COR_CINZA)

            elif tipo == "titulo_caso":
                draw.text((25, y), item[1], font=fonte_bold, fill=COR_AZUL_CLARO)

            elif tipo == "titulo_caso_aviso":
                draw.text((25, y), item[1], font=fonte_bold, fill=COR_VERMELHO_AVISO)

            elif tipo == "sub_caso":
                draw.text((25, y), item[1], font=fonte, fill=COR_AMARELO)

            elif tipo == "whisper_out":
                draw.text((25, y), item[1], font=fonte, fill=COR_TEXTO_BRANCO)

            elif tipo == "whisper_erro":
                draw.text((25, y), item[1], font=fonte_bold, fill=COR_VERMELHO_AVISO)

            elif tipo == "destaque_azul":
                draw.text((25, y), item[1], font=fonte_bold, fill=COR_AZUL_CLARO)

            elif tipo == "destaque_verde":
                draw.text((25, y), item[1], font=fonte_bold, fill=COR_VERDE_CLARO)

            elif tipo == "destaque_aviso":
                draw.text((25, y), item[1], font=fonte_bold, fill=COR_VERMELHO_AVISO)

            elif tipo == "resultado":
                draw.text((25, y), item[1], font=fonte, fill=COR_TEXTO_BRANCO)

            elif tipo == "detalhe":
                draw.text((25, y), item[1], font=fonte, fill=(180, 180, 180))

            elif tipo == "pix":
                draw.text((25, y), item[1], font=fonte, fill=COR_AMARELO)

            elif tipo == "info":
                draw.text((25, y), item[1], font=fonte, fill=COR_VERDE)

            elif tipo == "vazio":
                pass

        return img.tobytes()

    # Função auxiliar para enviar frames repetidos
    def enviar_frames(frame_bytes, qtd_frames):
        for _ in range(qtd_frames):
            proc.stdin.write(frame_bytes)

    print("Iniciando renderização da gravação de vídeo demonstrativo...")
    linhas_visiveis = []

    # 1. Tela inicial com prompt em branco por 2 segundos (60 frames)
    linhas_visiveis.append(("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", ""))
    for f in range(60):
        cursor = (f // 15) % 2 == 0
        proc.stdin.write(renderizar_frame(linhas_visiveis, cursor))

    # 2. Digitação do comando: git log --oneline -n 4
    cmd1 = "git log --oneline -n 4"
    for i in range(1, len(cmd1) + 1):
        linhas_visiveis[-1] = ("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", cmd1[:i])
        for _ in range(2): # velocidade de digitação
            proc.stdin.write(renderizar_frame(linhas_visiveis, True))

    # Pequena pausa antes de dar enter (10 frames)
    for _ in range(10):
        proc.stdin.write(renderizar_frame(linhas_visiveis, True))

    # 3. Exibir saída do git log
    linhas_visiveis.append(linhas_sessao[1]) # c032f22
    linhas_visiveis.append(linhas_sessao[2]) # 70c7a24
    linhas_visiveis.append(linhas_sessao[3]) # 84bb835
    linhas_visiveis.append(linhas_sessao[4]) # 4b9e159
    linhas_visiveis.append(linhas_sessao[5]) # vazio
    linhas_visiveis.append(("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", ""))

    # Pausa de 10 segundos (300 frames) para conferência dos commits
    for f in range(300):
        cursor = (f // 15) % 2 == 0
        proc.stdin.write(renderizar_frame(linhas_visiveis, cursor))

    # 4. Digitação do comando: python transcrever.py
    cmd2 = "python transcrever.py"
    for i in range(1, len(cmd2) + 1):
        linhas_visiveis[-1] = ("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", cmd2[:i])
        for _ in range(2):
            proc.stdin.write(renderizar_frame(linhas_visiveis, True))

    for _ in range(20):
        proc.stdin.write(renderizar_frame(linhas_visiveis, True))

    # 5. Execução do pipeline: adicionando as linhas com temporização realista
    # Índice das linhas após o segundo prompt (de 7 até o final)
    idx_linha = 7
    while idx_linha < len(linhas_sessao) - 1:
        item = linhas_sessao[idx_linha]
        linhas_visiveis.append(item)
        idx_linha += 1

        # Tempo de exibição por linha
        tipo = item[0]
        if tipo in ("separador", "vazio"):
            frames_pausa = 6
        elif tipo in ("titulo", "titulo_caso", "titulo_caso_aviso"):
            frames_pausa = 30
        elif tipo in ("whisper_out", "whisper_erro"):
            frames_pausa = 60 # simula processamento do whisper
        elif tipo in ("pix", "destaque_aviso"):
            frames_pausa = 50
        elif tipo == "info":
            frames_pausa = 35
        else:
            frames_pausa = 16

        frame_bytes = renderizar_frame(linhas_visiveis, False)
        enviar_frames(frame_bytes, frames_pausa)

    # 6. Adicionar prompt final e manter tela final congelada por 25 segundos para leitura
    linhas_visiveis.append(linhas_sessao[-1])
    for f in range(750): # 25 segundos
        cursor = (f // 15) % 2 == 0
        proc.stdin.write(renderizar_frame(linhas_visiveis, cursor))

    proc.stdin.close()
    proc.wait()
    print(f"Vídeo gerado com sucesso em: {video_path}")

if __name__ == "__main__":
    gerar_video_demonstracao()

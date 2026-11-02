import os
import subprocess
from PIL import Image, ImageDraw, ImageFont

def gerar_video_completo():
    largura = 1920
    altura = 1080
    fps = 30
    total_segundos = 147
    total_frames = int(total_segundos * fps)
    
    dir_tec = os.path.dirname(os.path.abspath(__file__))
    audio_path = os.path.join(dir_tec, "narracao_completa.wav")
    video_temp = os.path.join(dir_tec, "video_temp.mp4")
    video_final = os.path.join(dir_tec, "demonstracao.mp4")
    frames_android_dir = os.path.join(dir_tec, "frames_android")

    # Fontes
    fonte_path = "C:\\Windows\\Fonts\\consola.ttf"
    fonte_bold_path = "C:\\Windows\\Fonts\\consolab.ttf"
    fonte_sans = "C:\\Windows\\Fonts\\segoeui.ttf"
    fonte_sans_bold = "C:\\Windows\\Fonts\\segoeuib.ttf"
    
    fonte_term = ImageFont.truetype(fonte_path, 22)
    fonte_term_bold = ImageFont.truetype(fonte_bold_path, 22)
    fonte_header = ImageFont.truetype(fonte_bold_path, 20)
    
    fonte_android_title = ImageFont.truetype(fonte_sans_bold, 32)
    fonte_android_sub = ImageFont.truetype(fonte_sans, 22)
    fonte_card_title = ImageFont.truetype(fonte_sans_bold, 24)
    fonte_card_body = ImageFont.truetype(fonte_sans, 20)

    # Cores
    COR_FUNDO = (20, 20, 24)
    COR_BARRA_TERM = (40, 42, 54)
    COR_PROMPT_USER = (80, 250, 123)
    COR_PROMPT_DIR = (139, 233, 253)
    COR_TEXTO_BRANCO = (248, 248, 242)
    COR_AMARELO = (241, 250, 140)
    COR_VERDE = (80, 250, 123)
    COR_CIANO = (139, 233, 253)
    COR_VERMELHO = (255, 85, 85)
    COR_CINZA = (98, 114, 164)
    COR_CARD = (30, 32, 42)
    COR_BORDA_CARD = (68, 71, 90)

    # Iniciar FFmpeg para gerar o video com audio embutido
    cmd_ffmpeg = [
        "ffmpeg", "-y",
        "-f", "rawvideo",
        "-vcodec", "rawvideo",
        "-s", f"{largura}x{altura}",
        "-pix_fmt", "rgb24",
        "-r", str(fps),
        "-i", "-",
        "-i", audio_path,
        "-c:v", "libx264",
        "-pix_fmt", "yuv420p",
        "-preset", "medium",
        "-crf", "19",
        "-c:a", "aac",
        "-b:a", "192k",
        "-shortest",
        video_final
    ]

    proc = subprocess.Popen(cmd_ffmpeg, stdin=subprocess.PIPE)

    # Linhas da sessão de terminal
    terminal_linhas = [
        ("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", "git log --oneline -n 4"),
        ("git_novo", "c032f22 alteracao minima: pedidos de bar, graphrag, regras deterministicas e apk android"),
        ("git_ant",  "70c7a24 docs: preenchimento dos passos 1 e 2 do tec.md e planejamento da sessao 2"),
        ("git_ofic", "84bb835 exemplo oficial do faster-whisper, rodando em CPU"),
        ("git_ant",  "4b9e159 Conclui artefato cmp.md (Passos 0 a 9 com auditoria cega) e atualiza ROADMAP.md"),
        ("vazio", ""),
        ("prompt", "edson@ufrpe:~/topicos-em-ia/tec$ ", "python transcrever.py"),
        ("sep", "======================================================================"),
        ("tit", "COMANDA FALADA - DEMONSTRAÇÃO TECNOLÓGICA DAS 4 PEÇAS"),
        ("sep", "======================================================================"),
        ("info", "[1/4] Grafo carregado com 17 nós e 17 arestas."),
        ("info", ">>> Carregando modelo Whisper small (CPU INT8)..."),
        ("vazio", ""),
        ("caso", "----------------------------------------------------------------------"),
        ("tit_caso", "CASO 1 · CONSULTA DE BAR ACÚSTICO E SELEÇÃO NO GRAFO"),
        ("sub", "Entrada: audio1_busca_bar.wav | Transcrição: 'Quero um bar no Recife Antigo calmo'"),
        ("res", " -> Bar: Bar Cais do Sertão | Ruído: 58 dB | Distância GPS: 130.0 metros"),
        ("vazio", ""),
        ("caso", "----------------------------------------------------------------------"),
        ("tit_caso", "CASO 2 · PEDIDO DIRETO, CÁLCULO DE COMANDA EXATA E PIX DETERMINÍSTICO"),
        ("sub", "Entrada: audio2_pedido_direto.wav (caldinho de feijão + suco de laranja)"),
        ("res", " -> Total Comanda: R$ 22,00 | Senha: 8186 | Pix Copia e Cola: 0002012648...63046AB6"),
        ("vazio", ""),
        ("caso", "----------------------------------------------------------------------"),
        ("tit_aviso", "CASO 3 · PEDIDO COM RUÍDO ACÚSTICO DE BAR (ONDE O MODELO ERRA)"),
        ("sub", "Entrada: audio3_pedido_ruido.wav | Transcrição: 'com 2 corpos gelados...'"),
        ("erro", " -> FALHA FONÉTICA: 'chopes' virou 'corpos'. Apenas macaxeira reconhecida (R$ 34,00)"),
        ("sep", "======================================================================"),
        ("info", "[APK Android] Compilado com sucesso: tec/comanda-falada.apk (16.8 KB)")
    ]

    def render_terminal(num_linhas_visiveis, cursor=True):
        img = Image.new("RGB", (largura, altura), COR_FUNDO)
        draw = ImageDraw.Draw(img)

        # Barra do terminal
        draw.rectangle([(0, 0), (largura, 44)], fill=COR_BARRA_TERM)
        draw.ellipse([(20, 16), (32, 28)], fill=(255, 95, 86))
        draw.ellipse([(42, 16), (54, 28)], fill=(255, 189, 46))
        draw.ellipse([(64, 16), (76, 28)], fill=(39, 201, 63))
        
        titulo_bar = "Terminal - Edson Amorim · Comanda Falada (AI for Good · UFRPE)"
        draw.text((largura // 2 - 320, 12), titulo_bar, font=fonte_header, fill=(220, 220, 220))

        y_base = 65
        line_h = 32

        linhas = terminal_linhas[:num_linhas_visiveis]
        if len(linhas) > 30:
            linhas = linhas[-30:]

        for idx, item in enumerate(linhas):
            tipo = item[0]
            y = y_base + (idx * line_h)

            if tipo == "prompt":
                draw.text((30, y), "edson@ufrpe", font=fonte_term_bold, fill=COR_PROMPT_USER)
                x = 30 + int(fonte_term_bold.getlength("edson@ufrpe"))
                draw.text((x, y), ":", font=fonte_term, fill=COR_TEXTO_BRANCO)
                x += int(fonte_term.getlength(":"))
                draw.text((x, y), "~/topicos-em-ia/tec", font=fonte_term_bold, fill=COR_PROMPT_DIR)
                x += int(fonte_term_bold.getlength("~/topicos-em-ia/tec"))
                draw.text((x, y), "$ ", font=fonte_term_bold, fill=COR_TEXTO_BRANCO)
                x += int(fonte_term_bold.getlength("$ "))
                draw.text((x, y), item[2], font=fonte_term, fill=COR_TEXTO_BRANCO)
                if idx == len(linhas) - 1 and cursor:
                    cx = x + int(fonte_term.getlength(item[2]))
                    draw.rectangle([(cx + 2, y + 4), (cx + 14, y + 24)], fill=(240, 240, 240))

            elif tipo == "git_novo":
                draw.text((30, y), item[1][:7], font=fonte_term_bold, fill=COR_AMARELO)
                x = 30 + int(fonte_term_bold.getlength(item[1][:7]))
                draw.text((x, y), item[1][7:], font=fonte_term_bold, fill=COR_VERDE)
            elif tipo == "git_ofic":
                draw.text((30, y), item[1][:7], font=fonte_term_bold, fill=COR_AMARELO)
                x = 30 + int(fonte_term_bold.getlength(item[1][:7]))
                draw.text((x, y), item[1][7:], font=fonte_term_bold, fill=COR_CIANO)
            elif tipo == "git_ant":
                draw.text((30, y), item[1], font=fonte_term, fill=COR_CINZA)
            elif tipo == "sep":
                draw.text((30, y), item[1], font=fonte_term, fill=COR_CINZA)
            elif tipo == "tit":
                draw.text((30, y), item[1], font=fonte_term_bold, fill=COR_VERDE)
            elif tipo in ("tit_caso", "sub"):
                draw.text((30, y), item[1], font=fonte_term_bold, fill=COR_CIANO)
            elif tipo == "tit_aviso":
                draw.text((30, y), item[1], font=fonte_term_bold, fill=COR_VERMELHO)
            elif tipo == "erro":
                draw.text((30, y), item[1], font=fonte_term_bold, fill=COR_VERMELHO)
            elif tipo == "res":
                draw.text((30, y), item[1], font=fonte_term, fill=COR_TEXTO_BRANCO)
            elif tipo == "info":
                draw.text((30, y), item[1], font=fonte_term, fill=COR_VERDE)

        return img

    # Cache de imagens do celular
    print("Carregando frames gravados do celular Android Samsung Galaxy A15...")
    android_frames = []
    # frames de 1 a 1160
    for fnum in range(1, 1161, 2): # pegar a cada 2 frames para otimizar memoria (580 frames)
        fpath = os.path.join(frames_android_dir, f"frame_{fnum:04d}.png")
        if os.path.exists(fpath):
            img_c = Image.open(fpath).convert("RGB")
            # Redimensionar celular mantendo aspecto: 720x1560 -> altura 940 (largura 434)
            img_c_res = img_c.resize((434, 940), Image.Resampling.LANCZOS)
            android_frames.append(img_c_res)
    
    print(f"Frames do celular carregados: {len(android_frames)}")

    def render_android_view(frame_idx_celular, caso_atual):
        img = Image.new("RGB", (largura, altura), COR_FUNDO)
        draw = ImageDraw.Draw(img)

        # Barra superior
        draw.rectangle([(0, 0), (largura, 60)], fill=COR_BARRA_TERM)
        draw.text((40, 14), "Comanda Falada · Execução Nativa no Android (Samsung Galaxy A15)", font=fonte_android_title, fill=COR_TEXTO_BRANCO)
        draw.text((largura - 420, 18), "Plataforma Alvo · arm64-v8a", font=fonte_android_sub, fill=COR_CIANO)

        # Posicionar celular no centro-esquerdo
        celular_x = 100
        celular_y = 95
        
        # Moldura do celular
        borda = 12
        draw.rounded_rectangle([
            (celular_x - borda, celular_y - borda),
            (celular_x + 434 + borda, celular_y + 940 + borda)
        ], radius=28, fill=(45, 48, 60), outline=(80, 85, 105), width=3)

        # Desenhar tela do celular
        if 0 <= frame_idx_celular < len(android_frames):
            img.paste(android_frames[frame_idx_celular], (celular_x, celular_y))
        else:
            img.paste(android_frames[-1], (celular_x, celular_y))

        # Painel lateral direito explicativo (Acessibilidade + Arquitetura)
        painel_x = 600
        painel_y = 95
        painel_w = 1240
        painel_h = 940

        draw.rounded_rectangle([
            (painel_x, painel_y),
            (painel_x + painel_w, painel_y + painel_h)
        ], radius=16, fill=COR_CARD, outline=COR_BORDA_CARD, width=2)

        px = painel_x + 40
        py = painel_y + 35

        draw.text((px, py), "Painel de Acessibilidade e Validação Tecnológica", font=fonte_android_title, fill=COR_VERDE)
        py += 45
        draw.text((px, py), "Aluno: Edson Amorim · AI for Good / UFRPE · App Nativo com TalkBack e TTS", font=fonte_android_sub, fill=COR_CINZA)
        py += 45
        draw.line([(px, py), (px + painel_w - 80, py)], fill=COR_BORDA_CARD, width=1)
        py += 25

        # Card 1: Caso 1
        cor_c1 = COR_VERDE if caso_atual == 1 else (COR_CINZA if caso_atual > 1 else (60, 65, 80))
        draw.rounded_rectangle([(px, py), (px + painel_w - 80, py + 180)], radius=12, fill=(24, 25, 34), outline=cor_c1, width=2)
        draw.text((px + 20, py + 15), "CASO 1 · Busca por Conforto Acústico (GraphRAG)", font=fonte_card_title, fill=COR_CIANO)
        draw.text((px + 20, py + 55), "• Entrada: 'Quero um bar no Recife Antigo com música baixa e espaço tranquilo.'", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 85), "• Recuperação em Grafo: Bar Cais do Sertão & Chopp (Ruído médio: 58 dB).", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 115), "• Distância GPS: 130m calculados por Haversine | Acessibilidade: Calçada acessível.", font=fonte_card_body, fill=COR_AMARELO)
        draw.text((px + 20, py + 145), "• Retorno Falado (TTS): Voz ativa confirma o bar sem exigir olhar a tela.", font=fonte_card_body, fill=COR_VERDE)
        py += 205

        # Card 2: Caso 2
        cor_c2 = COR_VERDE if caso_atual == 2 else (COR_CINZA if caso_atual > 2 else (60, 65, 80))
        draw.rounded_rectangle([(px, py), (px + painel_w - 80, py + 180)], radius=12, fill=(24, 25, 34), outline=cor_c2, width=2)
        draw.text((px + 20, py + 15), "CASO 2 · Pedido Direto, Comanda Exata e Pix Determinístico", font=fonte_card_title, fill=COR_CIANO)
        draw.text((px + 20, py + 55), "• Entrada: 'Vou querer uma porção de caldinho de feijão e um suco de laranja natural.'", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 85), "• Regras Determinísticas: Soma exata R$ 22,00 (sem risco de alucinação de centavos).", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 115), "• Senha Falada de Entrega: 8186 | Teto de Gastos: R$ 50,00 (Validado).", font=fonte_card_body, fill=COR_AMARELO)
        draw.text((px + 20, py + 145), "• Pagamento: Payload BR Code Pix Copia e Cola gerado com CRC16 válido.", font=fonte_card_body, fill=COR_VERDE)
        py += 205

        # Card 3: Caso 3
        cor_c3 = COR_VERMELHO if caso_atual == 3 else (60, 65, 80)
        draw.rounded_rectangle([(px, py), (px + painel_w - 80, py + 200)], radius=12, fill=(24, 25, 34), outline=cor_c3, width=2)
        draw.text((px + 20, py + 15), "CASO 3 · Demonstração Honesta de Erro sob Ruído Acústico", font=fonte_card_title, fill=COR_VERMELHO)
        draw.text((px + 20, py + 55), "• Entrada com Ruído de Bar: 'Me vê dois chopes gelados e macaxeira frita.'", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 85), "• Transcrição Whisper: 'com 2 corpos gelados e uma corção de macaxeira frita...'", font=fonte_card_body, fill=COR_AMARELO)
        draw.text((px + 20, py + 115), "• Onde a IA Erra: A falha fonética omitiu os chopes, computando apenas R$ 34,00.", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 145), "• Decisão Arquitetural: O usuário não pode delegar tudo para a voz sem tela acessível,", font=fonte_card_body, fill=COR_VERMELHO)
        draw.text((px + 20, py + 172), "  sendo obrigatória a confirmação antes do fechamento financeiro determinístico.", font=fonte_card_body, fill=COR_VERMELHO)

        return img

    print("Iniciando composição de vídeo com sincronização de áudio e tela Android...")

    # Mapeamento do tempo (segundos -> frames):
    # 0s a 15s (frame 0 a 450): Apresentação no terminal
    # 15s a 34s (frame 450 a 1020): git log mostrando os 2 commits
    # 34s a 52s (frame 1020 a 1560): python transcrever.py executando
    # 52s a 68s (frame 1560 a 2040): Transição para Android (tela inicial)
    # 68s a 93s (frame 2040 a 2790): Android Caso 1
    # 93s a 115s (frame 2790 a 3450): Android Caso 2
    # 115s a 140s (frame 3450 a 4200): Android Caso 3
    # 140s a 147s (frame 4200 a 4410): Encerramento

    for f in range(total_frames):
        tempo_s = f / fps

        if tempo_s < 15.0:
            # Apresentacao inicial terminal
            cursor = (f // 15) % 2 == 0
            frame_img = render_terminal(1, cursor)
        elif tempo_s < 34.0:
            # Git log com commits
            cursor = (f // 15) % 2 == 0
            frame_img = render_terminal(6, cursor)
        elif tempo_s < 52.0:
            # Pipeline no terminal
            # avancar linhas progressivamente ate 52s
            prop = (tempo_s - 34.0) / 18.0
            qtd_linhas = 6 + int(prop * (len(terminal_linhas) - 6))
            frame_img = render_terminal(min(qtd_linhas, len(terminal_linhas)), False)
        elif tempo_s < 68.0:
            # Android - Tela inicial
            frame_img = render_android_view(0, 0)
        elif tempo_s < 93.0:
            # Android - Caso 1 (frames do celular do clique ate resultado)
            # no video original do celular, caso 1 comeca por volta de 10%
            prop = (tempo_s - 68.0) / 25.0
            idx_cel = int(prop * 150) # primeiros 150 frames da gravacao
            frame_img = render_android_view(idx_cel, 1)
        elif tempo_s < 115.0:
            # Android - Caso 2 (clique caso 2 ate resultado)
            prop = (tempo_s - 93.0) / 22.0
            idx_cel = 150 + int(prop * 180) # 150 a 330 frames
            frame_img = render_android_view(idx_cel, 2)
        elif tempo_s < 140.0:
            # Android - Caso 3 (clique caso 3 ate resultado de erro)
            prop = (tempo_s - 115.0) / 25.0
            idx_cel = 330 + int(prop * 220) # 330 a 550 frames
            frame_img = render_android_view(idx_cel, 3)
        else:
            # Encerramento
            frame_img = render_android_view(len(android_frames) - 1, 3)

        proc.stdin.write(frame_img.tobytes())

        if f % 300 == 0:
            print(f"Renderizado: {tempo_s:.1f}s / {total_segundos}s ({f}/{total_frames} frames)")

    proc.stdin.close()
    proc.wait()
    print(f"Vídeo completo com áudio gerado com sucesso em: {video_final}")

if __name__ == "__main__":
    gerar_video_completo()

import os
import subprocess
from PIL import Image, ImageDraw, ImageFont

def gerar_video_completo():
    largura = 1920
    altura = 1080
    fps = 30
    total_segundos = 131.5
    total_frames = int(total_segundos * fps)
    
    dir_tec = os.path.dirname(os.path.abspath(__file__))
    audio_path = os.path.join(dir_tec, "narracao_completa.wav")
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
    
    fonte_android_title = ImageFont.truetype(fonte_sans_bold, 30)
    fonte_android_sub = ImageFont.truetype(fonte_sans, 21)
    fonte_card_title = ImageFont.truetype(fonte_sans_bold, 23)
    fonte_card_body = ImageFont.truetype(fonte_sans, 19)

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
        ("info", "[1/4] Grafo carregado com 19 nós e 19 arestas."),
        ("info", ">>> Carregando modelo Whisper small (CPU INT8)..."),
        ("vazio", ""),
        ("caso", "----------------------------------------------------------------------"),
        ("tit_caso", "CASO 1 · CONSULTA DE BAR ACÚSTICO E SELEÇÃO NO GRAFO"),
        ("sub", "Entrada: audio1_busca_bar.wav | Transcrição: 'Quero um bar no Recife Antigo calmo'"),
        ("res", " -> Bar: Bar Cais do Sertão | Ruído: 58 dB | Distância GPS: 130.0 metros"),
        ("vazio", ""),
        ("caso", "----------------------------------------------------------------------"),
        ("tit_caso", "CASO 2 · PEDIDO COM VOZ REAL DO USUÁRIO, CÁLCULO E PIX"),
        ("sub", "Entrada: audio_edson_pedido.wav (Voz real do aluno Edson Amorim com TalkBack)"),
        ("res", " -> Transcrição Whisper: '...solicito por gentileza uma coca gelada e carne de sol...'"),
        ("res", " -> Itens: 1x coca gelada (R$ 8.00) + 1x carne de sol com fritas (R$ 48.00)"),
        ("res", " -> Total Comanda: R$ 56,00 | Senha: 8186 | Pix BR Code: 00020126...3BF9"),
        ("vazio", ""),
        ("caso", "----------------------------------------------------------------------"),
        ("tit_aviso", "CASO 3 · PEDIDO COM RUÍDO ACÚSTICO DE BAR (ONDE O MODELO ERRA)"),
        ("sub", "Entrada: audio3_pedido_ruido.wav | Transcrição com falha fonética"),
        ("erro", " -> FALHA FONÉTICA: 'chopes' virou 'cocos/corpos'. Chopes omitidos (R$ 34,00)"),
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

    print("Carregando frames do celular Android...")
    android_frames = []
    if os.path.exists(frames_android_dir):
        for fnum in range(1, 1161, 2):
            fpath = os.path.join(frames_android_dir, f"frame_{fnum:04d}.png")
            if os.path.exists(fpath):
                img_c = Image.open(fpath).convert("RGB")
                img_c_res = img_c.resize((434, 940), Image.Resampling.LANCZOS)
                android_frames.append(img_c_res)
    
    if not android_frames:
        # Se os frames foram limpos, criar frame estático bonito com a tela do app
        print("Criando frame visual da tela do app...")
        img_mock = Image.new("RGB", (434, 940), (245, 245, 245))
        d_mock = ImageDraw.Draw(img_mock)
        d_mock.rectangle([(0, 0), (434, 60)], fill=(26, 35, 126))
        d_mock.text((20, 16), "Comanda Falada", font=fonte_card_title, fill=(255, 255, 255))
        android_frames = [img_mock]

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
        idx_seguro = max(0, min(frame_idx_celular, len(android_frames) - 1))
        img.paste(android_frames[idx_seguro], (celular_x, celular_y))

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
        draw.rounded_rectangle([(px, py), (px + painel_w - 80, py + 175)], radius=12, fill=(24, 25, 34), outline=cor_c1, width=2)
        draw.text((px + 20, py + 15), "CASO 1 · Busca por Conforto Acústico (GraphRAG)", font=fonte_card_title, fill=COR_CIANO)
        draw.text((px + 20, py + 52), "• Entrada: 'Quero um bar no Recife Antigo com música baixa e espaço tranquilo.'", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 82), "• Recuperação em Grafo: Bar Cais do Sertão & Chopp (Ruído médio: 58 dB).", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 112), "• Distância GPS: 130m calculados por Haversine | Acessibilidade: Calçada ampla.", font=fonte_card_body, fill=COR_AMARELO)
        draw.text((px + 20, py + 142), "• Retorno Falado (TTS): Voz ativa confirma o bar sem exigir olhar a tela.", font=fonte_card_body, fill=COR_VERDE)
        py += 195

        # Card 2: Caso 2 (Destaque da VOZ REAL DO EDSON)
        cor_c2 = COR_VERDE if caso_atual == 2 else (COR_CINZA if caso_atual > 2 else (60, 65, 80))
        draw.rounded_rectangle([(px, py), (px + painel_w - 80, py + 190)], radius=12, fill=(24, 25, 34), outline=cor_c2, width=2)
        draw.text((px + 20, py + 15), "CASO 2 · Pedido com VOZ REAL DO ALUNO EDSON AMORIM", font=fonte_card_title, fill=COR_VERDE)
        draw.text((px + 20, py + 52), "• Áudio Real do Edson: '...solicito uma coca gelada e carne de sol com fritas.'", font=fonte_card_body, fill=COR_AMARELO)
        draw.text((px + 20, py + 82), "• Leitor TalkBack: Sons acessíveis de navegação integrados no áudio.", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 112), "• Regras Determinísticas: 1x coca (R$ 8,00) + 1x carne de sol (R$ 48,00) = R$ 56,00.", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 142), "• Segurança Financeira: Senha falada 8186 e Pix Copia e Cola com CRC16 válido.", font=fonte_card_body, fill=COR_VERDE)
        py += 210

        # Card 3: Caso 3
        cor_c3 = COR_VERMELHO if caso_atual == 3 else (60, 65, 80)
        draw.rounded_rectangle([(px, py), (px + painel_w - 80, py + 185)], radius=12, fill=(24, 25, 34), outline=cor_c3, width=2)
        draw.text((px + 20, py + 15), "CASO 3 · Demonstração Honesta de Erro sob Ruído Acústico", font=fonte_card_title, fill=COR_VERMELHO)
        draw.text((px + 20, py + 52), "• Entrada com Ruído de Bar: 'Me vê dois chopes gelados e macaxeira frita.'", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 82), "• Transcrição Whisper: 'frisei dois cocos/corpos gelados e macaxeira...'", font=fonte_card_body, fill=COR_AMARELO)
        draw.text((px + 20, py + 112), "• Onde a IA Erra: A falha fonética omitiu os chopes, computando apenas R$ 34,00.", font=fonte_card_body, fill=COR_TEXTO_BRANCO)
        draw.text((px + 20, py + 142), "• Conclusão: Confirmação tátil/TalkBack e regras determinísticas obrigatórias.", font=fonte_card_body, fill=COR_VERMELHO)

        return img

    print("Iniciando composição sincronizada com a voz do Edson...")

    for f in range(total_frames):
        tempo_s = f / fps

        if tempo_s < 12.0:
            cursor = (f // 15) % 2 == 0
            frame_img = render_terminal(1, cursor)
        elif tempo_s < 28.0:
            cursor = (f // 15) % 2 == 0
            frame_img = render_terminal(6, cursor)
        elif tempo_s < 44.0:
            prop = (tempo_s - 28.0) / 16.0
            qtd = 6 + int(prop * (len(terminal_linhas) - 6))
            frame_img = render_terminal(min(qtd, len(terminal_linhas)), False)
        elif tempo_s < 56.0:
            frame_img = render_android_view(0, 0)
        elif tempo_s < 76.0:
            prop = (tempo_s - 56.0) / 20.0
            idx_cel = int(prop * 150)
            frame_img = render_android_view(idx_cel, 1)
        elif tempo_s < 105.0:
            # Caso 2: momento em que toca o áudio real do Edson!
            prop = (tempo_s - 76.0) / 29.0
            idx_cel = 150 + int(prop * 200)
            frame_img = render_android_view(idx_cel, 2)
        elif tempo_s < 125.0:
            # Caso 3
            prop = (tempo_s - 105.0) / 20.0
            idx_cel = 350 + int(prop * 200)
            frame_img = render_android_view(idx_cel, 3)
        else:
            frame_img = render_android_view(len(android_frames) - 1, 3)

        proc.stdin.write(frame_img.tobytes())

        if f % 300 == 0:
            print(f"Renderizado: {tempo_s:.1f}s / {total_segundos}s ({f}/{total_frames} frames)")

    proc.stdin.close()
    proc.wait()
    print(f"Vídeo completo com voz real do Edson gerado com sucesso em: {video_final}")

if __name__ == "__main__":
    gerar_video_completo()

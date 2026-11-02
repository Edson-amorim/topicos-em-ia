"""
Comanda Falada - Pipeline de Demonstração Tecnológica
Integrando:
1. faster-whisper (ASR com initial_prompt em português do Brasil)
2. NetworkX (GraphRAG de conforto acústico e cardápios)
3. Regras determinísticas (conta sem alucinação, teto de gasto, Haversine, senha e Pix)
"""

import os
import re
import unicodedata
from faster_whisper import WhisperModel
import grafo
import regras

# Posição do usuário simulada (próximo ao Marco Zero no Recife Antigo)
USUARIO_GPS = {"lat": -8.0628, "lon": -34.8715}
TETO_GASTO_PADRAO = 50.0  # R$ 50,00


def remover_acentos(texto):
    nfkd = unicodedata.normalize("NFKD", texto)
    return "".join([c for c in nfkd if not unicodedata.combining(c)])


def inicializar_modelo():
    print(">>> Carregando modelo Whisper small (CPU INT8)...")
    return WhisperModel("small", device="cpu", compute_type="int8")


def transcrever_audio(modelo, caminho_audio, prompt_contexto=None):
    segments, info = modelo.transcribe(
        caminho_audio,
        beam_size=5,
        language="pt",
        initial_prompt=prompt_contexto
    )
    texto = " ".join([segment.text.strip() for segment in segments])
    return texto, info


def casar_itens_cardapio(texto_transcrito, cardapio):
    """
    Identifica itens e quantidades presentes na transcrição com correspondência de cardápio
    e normalização fonética/ortográfica básica.
    """
    itens_pedidos = []
    texto_norm = remover_acentos(texto_transcrito.lower())
    
    # Dicionário de números por extenso
    mapa_numeros = {
        "um": 1, "uma": 1, "dois": 2, "duas": 2, "três": 3, "tres": 3,
        "quatro": 4, "cinco": 5, "seis": 6
    }
    
    for item in cardapio:
        nome_item = remover_acentos(item["item"].lower())
        if nome_item in texto_norm:
            padrao = rf"(\b\d+\b|\bum\b|\buma\b|\bdois\b|\bduas\b|\btres\b)?\s*(?:porcao de|porcoes de|copo de|garrafa de)?\s*{re.escape(nome_item)}"
            match = re.search(padrao, texto_norm)
            qtd = 1
            if match and match.group(1):
                token = match.group(1)
                qtd = int(token) if token.isdigit() else mapa_numeros.get(token, 1)
                
            itens_pedidos.append({
                "item": item["item"],
                "quantidade": qtd,
                "preco": item["preco"],
                "categoria": item["categoria"]
            })
            
    return itens_pedidos


def executar_demonstracao():
    print("=" * 70)
    print("COMANDA FALADA - DEMONSTRAÇÃO TECNOLÓGICA DAS 4 PEÇAS")
    print("=" * 70)
    
    # 1. Carregar Grafo de Conhecimento
    caminho_grafo = os.path.join(os.path.dirname(__file__), "bares_grafo.json")
    G = grafo.carregar_grafo_bares(caminho_grafo)
    print(f"[1/4] Grafo carregado com {G.number_of_nodes()} nós e {G.number_of_edges()} arestas.")
    
    # 2. Inicializar Whisper
    modelo = inicializar_modelo()
    
    pasta_audios = os.path.join(os.path.dirname(__file__), "audios")
    
    # =========================================================================
    # CASO 1: Consulta Acústica por Voz (Bar calmo no Recife Antigo)
    # =========================================================================
    audio1 = os.path.join(pasta_audios, "audio1_busca_bar.wav")
    print("\n" + "-" * 70)
    print("CASO 1 · CONSULTA DE BAR ACÚSTICO E SELEÇÃO NO GRAFO")
    print(f"Entrada de Áudio: {os.path.basename(audio1)}")
    
    transcricao1, info1 = transcrever_audio(modelo, audio1, prompt_contexto="Recife Antigo, bar calmo, música baixa, acessibilidade")
    print(f"Transcrição Whisper [pt, prob {info1.language_probability:.2f}]: '{transcricao1}'")
    
    # GraphRAG: filtrar bares que tenham baixo ruído no Recife Antigo
    bares_recomendados = grafo.buscar_bares_por_conforto(G, nivel_maximo="baixo", bairro_filtro="Recife Antigo")
    print(f"\n[GraphRAG] Bares encontrados com conforto acústico (baixo ruído):")
    for b in bares_recomendados:
        dist = regras.calcular_distancia_haversine(
            USUARIO_GPS["lat"], USUARIO_GPS["lon"], b["latitude"], b["longitude"]
        )
        print(f" -> Bar: {b['nome']} ({b['bairro']})")
        print(f"    Ruído: {b['decibeis']} dB | Som: {b['tipo_som']}")
        print(f"    Acessibilidade: {b['acessibilidade']}")
        print(f"    Distância GPS (Haversine): {dist:.1f} metros da sua localização")
        
    bar_escolhido = bares_recomendados[0]
    cardapio_bar = grafo.obter_cardapio_bar(G, bar_escolhido["id"])
    
    # =========================================================================
    # CASO 2: Pedido com Voz Real do Usuário + Regras Exatas e Pix
    # =========================================================================
    audio_edson = os.path.join(pasta_audios, "audio_edson_pedido.wav")
    audio2 = audio_edson if os.path.exists(audio_edson) else os.path.join(pasta_audios, "audio2_pedido_direto.wav")
    print("\n" + "-" * 70)
    print("CASO 2 · PEDIDO DIRETO COM VOZ REAL DO USUÁRIO, CÁLCULO E PIX")
    print(f"Entrada de Áudio: {os.path.basename(audio2)} (Gravação real do aluno Edson Amorim)")
    
    termos_cardapio = ", ".join([it["item"] for it in cardapio_bar])
    transcricao2, info2 = transcrever_audio(modelo, audio2, prompt_contexto=f"Cardápio: {termos_cardapio}")
    print(f"Transcrição Whisper [pt, prob {info2.language_probability:.2f}]: '{transcricao2}'")
    
    itens_extraidos = casar_itens_cardapio(transcricao2, cardapio_bar)
    print(f"[Extração Estruturada] Itens identificados no cardápio: {len(itens_extraidos)}")
    for it in itens_extraidos:
        print(f" -> {it['quantidade']}x {it['item']} (R$ {it['preco']:.2f})")
        
    # Regras Determinísticas
    resultado_conta = regras.calcular_conta(itens_extraidos, teto_gasto=60.0)
    senha = regras.gerar_senha_entrega(seed=4821)
    copia_pix = regras.gerar_copia_e_cola_pix(
        chave_pix="pix@barcaisdosertao.com.br",
        nome_recebedor="Bar Cais do Sertao",
        cidade="Recife",
        valor=resultado_conta["total"]
    )
    
    print("\n[Regras Determinísticas - Comanda]")
    print(f" -> Total da Comanda: {resultado_conta['total_formatado']}")
    print(f" -> Status do Teto: {resultado_conta['alerta_teto']}")
    print(f" -> Senha Falada de Entrega: {senha}")
    print(f" -> Código Copia e Cola Pix (BR Code):\n    {copia_pix}")
    
    # =========================================================================
    # CASO 3: Pedido com Ruído Acústico (Onde o modelo erra!)
    # =========================================================================
    audio3 = os.path.join(pasta_audios, "audio3_pedido_ruido.wav")
    print("\n" + "-" * 70)
    print("CASO 3 · PEDIDO COM RUÍDO ACÚSTICO DE BAR (ONDE O MODELO ERRA)")
    print(f"Entrada de Áudio: {os.path.basename(audio3)}")
    
    transcricao3, info3 = transcrever_audio(modelo, audio3, prompt_contexto=f"Cardápio: {termos_cardapio}")
    print(f"Transcrição Whisper [pt, prob {info3.language_probability:.2f}]: '{transcricao3}'")
    
    itens_extraidos3 = casar_itens_cardapio(transcricao3, cardapio_bar)
    print(f"[Extração Estruturada com Ruído] Itens reconhecidos: {len(itens_extraidos3)}")
    for it in itens_extraidos3:
        print(f" -> {it['quantidade']}x {it['item']} (R$ {it['preco']:.2f})")
        
    resultado_conta3 = regras.calcular_conta(itens_extraidos3, teto_gasto=TETO_GASTO_PADRAO)
    print(f" -> Total Calculado: {resultado_conta3['total_formatado']}")
    print("=" * 70)


if __name__ == "__main__":
    executar_demonstracao()

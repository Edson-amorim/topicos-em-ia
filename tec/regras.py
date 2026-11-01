"""
Módulo de regras exatas determinísticas para o Comanda Falada.
Responsável por:
1. Cálculo exato da comanda e teto de gastos (centavos inteiros, sem erro de ponto flutuante).
2. Distância GPS via fórmula de Haversine para validação de proximidade.
3. Geração de senha falada de 4 dígitos para confirmação segura de entrega.
4. Geração de payload Pix (BR Code padrão EMV / Banco Central) com cálculo determinístico de CRC16.
"""

import math
import random
from typing import List, Dict, Any, Tuple


def calcular_conta(itens_selecionados: List[Dict[str, Any]], teto_gasto: float = 100.0) -> Dict[str, Any]:
    """
    Soma exata da conta em centavos inteiros para evitar qualquer alucinação de LLM ou erro float.
    """
    total_centavos = 0
    linhas = []
    
    for item in itens_selecionados:
        qtd = int(item.get("quantidade", 1))
        # Converter preço em centavos
        preco_centavos = int(round(float(item.get("preco", 0.0)) * 100))
        subtotal_centavos = qtd * preco_centavos
        total_centavos += subtotal_centavos
        
        linhas.append({
            "item": item.get("item"),
            "quantidade": qtd,
            "preco_unitario": preco_centavos / 100.0,
            "subtotal": subtotal_centavos / 100.0
        })
        
    total = total_centavos / 100.0
    teto_atingido = total > teto_gasto
    
    return {
        "itens": linhas,
        "total": total,
        "total_formatado": f"R$ {total:.2f}".replace(".", ","),
        "teto_gasto": teto_gasto,
        "dentro_do_limite": not teto_atingido,
        "alerta_teto": f"Atenção: o pedido (R$ {total:.2f}) excedeu o seu teto de R$ {teto_gasto:.2f}." if teto_atingido else "Dentro do teto de gastos estipulado."
    }


def calcular_distancia_haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """
    Calcula a distância em metros entre duas coordenadas geográficas pelo método de Haversine.
    """
    r_terra = 6371000.0  # Raio médio da Terra em metros
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = (math.sin(delta_phi / 2.0) ** 2 +
         math.cos(phi1) * math.cos(phi2) * (math.sin(delta_lambda / 2.0) ** 2))
    c = 2.0 * math.atan2(math.sqrt(a), math.sqrt(1.0 - a))

    return round(r_terra * c, 1)


def gerar_senha_entrega(seed: int = None) -> str:
    """
    Gera uma senha falada de 4 dígitos para liberação do preparo/entrega ao garçom na mesa.
    """
    rng = random.Random(seed) if seed is not None else random
    return f"{rng.randint(1000, 9999)}"


def _calcular_crc16(payload: str) -> str:
    """Cálculo do CRC16-CCITT (0xFFFF) padrão EMV / Pix do Banco Central."""
    polinomio = 0x1021
    resultado = 0xFFFF
    
    dados = payload.encode('utf-8')
    for b in dados:
        resultado ^= (b << 8)
        for _ in range(8):
            if (resultado & 0x8000) != 0:
                resultado = ((resultado << 1) ^ polinomio) & 0xFFFF
            else:
                resultado = (resultado << 1) & 0xFFFF
                
    return f"{resultado:04X}"


def gerar_copia_e_cola_pix(chave_pix: str, nome_recebedor: str, cidade: str, valor: float, txid: str = "PEDIDO") -> str:
    """
    Gera string Copia e Cola Pix (BR Code estático) segundo a especificação do Banco Central.
    """
    valor_str = f"{valor:.2f}"
    
    # Campo 00: Versão do Payload (01)
    p00 = "000201"
    
    # Campo 26: Merchant Account Information
    gui = "0014br.gov.bcb.pix"
    chave = f"01{len(chave_pix):02d}{chave_pix}"
    payload_26 = gui + chave
    p26 = f"26{len(payload_26):02d}{payload_26}"
    
    # Campo 52: Merchant Category Code (0000 = padrão)
    p52 = "52040000"
    
    # Campo 53: Transaction Currency (986 = BRL)
    p53 = "5303986"
    
    # Campo 54: Transaction Amount
    p54 = f"54{len(valor_str):02d}{valor_str}"
    
    # Campo 58: Country Code (BR)
    p58 = "5802BR"
    
    # Campo 59: Merchant Name (máx 25 char)
    nome_fmt = nome_recebedor[:25].strip()
    p59 = f"59{len(nome_fmt):02d}{nome_fmt}"
    
    # Campo 60: Merchant City (máx 15 char)
    cidade_fmt = cidade[:15].strip()
    p60 = f"60{len(cidade_fmt):02d}{cidade_fmt}"
    
    # Campo 62: Additional Data Field (TXID)
    campo_txid = f"05{len(txid):02d}{txid}"
    p62 = f"62{len(campo_txid):02d}{campo_txid}"
    
    # Campo 63: CRC16 prefixo
    sem_crc = p00 + p26 + p52 + p53 + p54 + p58 + p59 + p60 + p62 + "6304"
    crc = _calcular_crc16(sem_crc)
    
    return sem_crc + crc

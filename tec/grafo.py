"""
Módulo de GraphRAG leve com NetworkX para modelagem de bares,
conforto acústico e cardápios estruturados.
"""

import json
import networkx as nx
from typing import Dict, Any, List


def carregar_grafo_bares(caminho_json: str = "bares_grafo.json") -> nx.DiGraph:
    """
    Constrói o Grafo de Conhecimento com bares, nós de acústica, nós de cardápio e coordenadas.
    """
    with open(caminho_json, "r", encoding="utf-8") as f:
        dados = json.load(f)
        
    G = nx.DiGraph()
    
    for bar in dados.get("bares", []):
        bar_id = bar["id"]
        # Nó do Bar
        G.add_node(
            bar_id,
            tipo="bar",
            nome=bar["nome"],
            bairro=bar["bairro"],
            latitude=bar["coordenadas"]["latitude"],
            longitude=bar["coordenadas"]["longitude"],
            acessibilidade=bar["conforto_acustico"]["acessibilidade"]
        )
        
        # Nó do Bairro
        bairro = bar["bairro"]
        G.add_node(bairro, tipo="bairro")
        G.add_edge(bar_id, bairro, relacao="LOCALIZADO_EM")
        
        # Nó de Conforto Acústico
        ruido = bar["conforto_acustico"]["nivel_ruido"]
        som_id = f"ruido_{ruido}"
        G.add_node(som_id, tipo="nivel_ruido", decibeis=bar["conforto_acustico"]["decibeis_medio"], detalhe=bar["conforto_acustico"]["tipo_som"])
        G.add_edge(bar_id, som_id, relacao="TEM_NIVEL_RUIDO")
        
        # Nós dos Itens do Cardápio
        for item in bar.get("cardapio", []):
            item_nome = item["item"]
            item_id = f"item_{item_nome.replace(' ', '_')}"
            G.add_node(item_id, tipo="item_cardapio", nome=item_nome, preco=item["preco"], categoria=item["categoria"])
            G.add_edge(bar_id, item_id, relacao="OFERECE_ITEM", preco=item["preco"])
            
    return G


def buscar_bares_por_conforto(G: nx.DiGraph, nivel_maximo: str = "baixo", bairro_filtro: str = None) -> List[Dict[str, Any]]:
    """
    Recupera bares no grafo que atendem aos requisitos de conforto acústico e localização.
    """
    niveis_aceitos = ["baixo"] if nivel_maximo == "baixo" else ["baixo", "medio"]
    candidatos = []
    
    for n, data in G.nodes(data=True):
        if data.get("tipo") == "bar":
            # Verificar se atende ao nível de ruído
            atende_ruido = False
            decibeis = 0
            detalhe_som = ""
            for vizinho in G.successors(n):
                viz_data = G.nodes[vizinho]
                if viz_data.get("tipo") == "nivel_ruido":
                    if vizinho.replace("ruido_", "") in niveis_aceitos:
                        atende_ruido = True
                        decibeis = viz_data.get("decibeis")
                        detalhe_som = viz_data.get("detalhe")
                        
            # Verificar se atende ao bairro
            bairro_ok = True
            if bairro_filtro:
                bairro_ok = (data.get("bairro").lower() == bairro_filtro.lower())
                
            if atende_ruido and bairro_ok:
                candidatos.append({
                    "id": n,
                    "nome": data.get("nome"),
                    "bairro": data.get("bairro"),
                    "latitude": data.get("latitude"),
                    "longitude": data.get("longitude"),
                    "acessibilidade": data.get("acessibilidade"),
                    "decibeis": decibeis,
                    "tipo_som": detalhe_som
                })
                
    return candidatos


def obter_cardapio_bar(G: nx.DiGraph, bar_id: str) -> List[Dict[str, Any]]:
    """Retorna a lista de itens e preços oferecidos pelo bar no grafo."""
    itens = []
    if bar_id in G:
        for vizinho in G.successors(bar_id):
            viz_data = G.nodes[vizinho]
            if viz_data.get("tipo") == "item_cardapio":
                preco = G.edges[bar_id, vizinho].get("preco")
                itens.append({
                    "item": viz_data.get("nome"),
                    "preco": preco,
                    "categoria": viz_data.get("categoria")
                })
    return itens

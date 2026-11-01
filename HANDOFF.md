# HANDOFF · Transição de Sessão (AI for Good · UFRPE)

**Data de Atualização:** 01/11/2026  
**Aluno:** Edson Amorim (`edsoninformatica28br@gmail.com`)  
**Repositório Local:** `C:\topicos-em-ia` (`https://github.com/Edson-amorim/topicos-em-ia`)  
**Projeto:** **Comanda Falada · Passo à Frente**  
**Artefato Atual:** `[3] artefato · tecnologia base demonstrável` (`C:\topicos-em-ia\va2\tec.md`)  
**Status Atual:** **Sessão 1 CONCLUÍDA (Passos 1 e 2)**  
**Próxima Sessão:** **Sessão 2 do `tec.md` (Passo 3: Alteração Mínima com Todas as Tecnologias + Execução Android e APK)**

---

## 1. Regras Críticas de Acessibilidade, Pedagogia e Contexto

1. **Acessibilidade para Leitor de Telas (`C:\topicos-em-ia\AGENTS.md`):**
   - Edson é uma pessoa cega e utiliza leitor de telas.
   - Toda comunicação no chat DEVE ser em português do Brasil (`pt-BR`), clara, direta e estruturada em texto corrido ou listas numeradas simples (evitando tabelas visuais complexas no chat, mas mantendo as tabelas Markdown obrigatórias dentro de `va2/tec.md`).
2. **Regra Pedagógica ("A IA faz, o aluno domina"):**
   - Entrevistar o Edson em **cada decisão, uma por vez**. Mostrar opções, explicar o porquê (comparando `tec_ref/excelente.md` e `tec_ref/raso.md`), e **nunca decidir por ele**.
   - No Passo 3, documentar honestamente onde o modelo erra (como no exemplo excelente, onde produtos ou unidades saem trocados).
   - Trechos marcados como **sem IA** (Passo 5 e as 3 linhas do Passo 6) devem vir das palavras do Edson. No `§ 6`, é estritamente proibido escrever, sugerir ou reformular as 3 linhas do aluno.
3. **Orçamento de Contexto (< 120k tokens):**
   - Cada sessão deve ficar abaixo de **100k–120k tokens** para nunca acionar autocompactação.
   - Ao concluir os objetivos da sessão (ou se aproximar de 100k tokens), salve todo o progresso em `va2/tec.md`, `ROADMAP.md` e `HANDOFF.md` e avise o Edson para abrir a próxima sessão.

---

## 2. O Que Foi Concluído na Sessão 1 (Passos 1 e 2)

- **Passo 1 (`§ 1 · as peças`):**
  - Tabela preenchida com as 4 peças completas da arquitetura:
    1. `faster-whisper` (MIT) — Transcrição PC/Servidor.
    2. `whisper.cpp` (MIT) — Transcrição nativa no celular Android.
    3. `NetworkX` (BSD-3-Clause) + `Qwen` (Apache 2.0) via `Ollama`/`llama.cpp` (MIT) — GraphRAG de cardápios e relatos de conforto acústico.
    4. Regras exatas determinísticas (MIT) — Soma de conta, teto de gasto, fórmula Haversine de GPS, senha falada de 4 dígitos e código Pix.
  - Justificativa do aluno incluída com as próprias palavras: escolha para ter menos erros de IA e menos burocracia para rodar.
- **Passo 2 (`§ 2 · o exemplo oficial, rodando`):**
  - Pasta `C:\topicos-em-ia\tec\` criada com `transcrever.py`, `requirements.txt`, `README.md` e o áudio oficial `audio.mp3`.
  - Instalado `faster-whisper` e dependências ajustadas para Python 3.13 (`av<19`).
  - Executado o exemplo com sucesso na CPU em modo INT8 com modelo `small`.
  - Saída oficial copiada diretamente para `va2/tec.md`.
  - **Commit 1 realizado e enviado ao GitHub:**
    `[84bb835 · exemplo oficial do faster-whisper, rodando em CPU](https://github.com/Edson-amorim/topicos-em-ia/commit/84bb835)`

---

## 3. Instruções Exatas para a Sessão 2 (Passo 3 do `tec.md`)

Na **Sessão 2**, o agente deve realizar:
1. **Preparação dos Dados do Problema:**
   - Criar `tec/audios/` com 3 áudios em português simulando pedidos reais para bares (ex.: um bar com acessibilidade e música baixa, um pedido com porção e suco, e um caso com ruído que induza erro real para demonstrar na folha).
   - Criar `tec/bares_grafo.json` com bares, relatos de conforto acústico, itens de cardápio e coordenadas GPS.
2. **Alteração Mínima (`tec/transcrever.py`):**
   - Alterar a entrada: áudios de pedidos de bar em vez de JFK.
   - Injetar `initial_prompt` com termos do cardápio.
   - Integrar a saída com extração estruturada do pedido + regras exatas determinísticas (conta sem erro, teto de gasto, Haversine, senha de 4 dígitos e Pix).
3. **Execução no Android e Geração do APK:**
   - Testar o binário `whisper-cli` para Android `arm64-v8a` no celular do Edson via `adb` (ou emulador), medindo tempo de carga, RTF e memória RAM.
   - Estruturar e gerar o APK de demonstração Android solicitado pelo Edson.
4. **Commit 2 e Preenchimento do `§ 3` de `va2/tec.md`:**
   - Fazer o **Commit 2** (`alteração mínima: pedidos de bar, graphrag, regras e android`).
   - Preencher a seção `§ 3` no `va2/tec.md` documentando detalhadamente as saídas e onde ela erra.
5. Atualizar `HANDOFF.md` e `ROADMAP.md` e orientar o Edson para a Sessão 3.

---

## 4. Prompt Pronto para o Aluno Colar na Sessão 2

```text
Olá! Concluímos a Sessão 1 do artefato va2/tec.md (Passos 1 e 2, com o Commit 1 no GitHub). Leia os arquivos C:\topicos-em-ia\HANDOFF.md, C:\topicos-em-ia\ROADMAP.md e C:\topicos-em-ia\va2\tec.md e inicie a Sessão 2 (Passo 3: alteração mínima com todas as tecnologias, medição no Android e geração do APK demonstrativo).
```

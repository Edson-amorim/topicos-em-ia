# ROADMAP · Tópicos Avançados em IA (AI for Good)
**Aluno:** Edson Amorim  
**Projeto:** Comanda Falada · Passo à Frente  
**Plataforma Alvo:** Android (validação via `adb` em dispositivo/emulador Android `arm64-v8a` + backend/pipeline local)

---

## Artefato 1 (VA2): Brainwriting Híbrido (`va2/brw.md`) — CONCLUÍDO
- [x] Passo 0: Preparar a folha `va2/brw.md`
- [x] Passo 1: Escolher onde atacar (Crise, 3 Tecnologias A/B/C, 3 Infraestruturas 1/2/3)
- [x] Passo 2: Conhecer os 5 clichês
- [x] Passo 3: Matriz 3 × 3, Mapa Impacto × Viabilidade, Tabela das Duas Perguntas (Interesse Próprio e Alternativa sem IA) e Ideia Escolhida
- [x] Passo 4: Aplicar os 9 motores com colunas de ganho individual e sustentação nas duas perguntas
- [x] Passo 5: O que fica, o que sai e Ideia Completa (`comanda falada · passo à frente`)
- [x] Passo 6: Refinamento (6a: dados reais verificados, aposta de 5 anos e ganho vs. custo; 6b: IA indispensável; 6c: 3 críticas e decisões)
- [x] Passo 7: Resumo sem IA em 5 linhas
- [x] Passo 8: Auditoria cega e envio ao GitHub

---

## Artefato 2 (VA2): Competidores e Contribuição (`va2/cmp.md`) — CONCLUÍDO
- [x] Passo 0: Preparar a folha `va2/cmp.md` com a ideia completa no topo
- [x] Passo 1: Definir a ficha da ideia (Cenário, Usuário, Problema do usuário, Tecnologia)
- [x] Passo 2: Coletar 10 ferramentas/apps existentes (com links abertos e conferidos e ficha C U P T)
- [x] Passo 3: Coletar 10 artigos científicos publicados (com links abertos, veículos conferidos da lista `veiculos.md` e ficha C U P T)
- [x] Passo 4: Escolher os 3 apps + 3 artigos mais próximos (pelo menos 2 em veículo de excelência e no máximo 1 do arXiv) com justificativa
- [x] Passo 5: Definir de 5 a 8 eixos de comparação (em conversa nova, com pelo menos 2 onde a ideia perde/empata) e preencher a Matriz com evidências
- [x] Passo 6: Escrever a frase da contribuição no molde oficial e indicar o trabalho mais próximo
- [x] Passo 7: Tentar derrubar a contribuição (em conversa nova com busca na web) e ajustar se necessário
- [x] Passo 8: Escrever o resumo sem IA em até 4 linhas
- [x] Passo 9: Rodar a auditoria cega (em conversa nova) e registrar a resposta do aluno sem IA item por item

---

## Artefato 3 (VA2): Tecnologia Base Demonstrável (`va2/tec.md`) — EM ANDAMENTO

### Regra de Ouro de Gestão de Sessões e Contexto
- **Limite rígido por sessão:** **máximo de 100k a 120k tokens** para evitar autocompactação e perda de precisão.
- **Transição entre sessões:** ao final de cada sessão (ou se o contexto se aproximar de 100k tokens), o agente atualiza `ROADMAP.md` e `HANDOFF.md` no disco e avisa o Edson para abrir uma nova sessão colando o prompt de retomada do `HANDOFF.md`.
- **Cobertura Tecnológica Obrigatória + Plataforma Alvo (Android):**
  1. **Peça Central 1 (Fala → Texto no PC/Servidor):** `faster-whisper` (`SYSTRAN/faster-whisper`, MIT) comparado a `openai/whisper` (MIT) e `Vosk` (`alphacep/vosk-api`, Apache 2.0).
  2. **Peça Central 2 (Fala → Texto rodando nativo no Android):** `whisper.cpp` (`ggml-org/whisper.cpp`, MIT, binário `arm64-v8a` / Android via `adb`), medindo tempo de carga, RTF (tempo/duração do áudio) e memória RAM diretamente no aparelho Android.
  3. **Peça Central 3 / Apoio de IA (GraphRAG + Modelo de Linguagem Local):** grafo de conhecimento de bares, cardápios bagunçados, ingredientes e relatos de conforto (`NetworkX`, BSD-3-Clause + busca em grafo/RAG) integrado a modelo de linguagem local aberto (`Qwen3-4B` / `Qwen2.5-3B` Apache 2.0 via `Ollama` MIT ou `llama.cpp` MIT) para filtrar bar e pratos e extrair o pedido estruturado.
  4. **Peça de Apoio 4 (Regras Exatas Determinísticas):** módulo de regras exatas em Python/Android para cálculo da conta sem erro, validação de teto de gasto, verificação de raio GPS (Haversine), geração da senha falada de 4 dígitos e código Pix (BRCode).

### Divisão em Sessões Planejadas

#### Sessão 0 (Atual — Planejamento e Preparação)
- [x] Ler e baixar `3-tecnologia-base.md`, `tec/modelo.md`, `tec/excelente.md` e `tec/raso.md`
- [x] Inspecionar os commits de referência do professor (`lsfcin/rastro-da-banca`) e o ambiente local (Python 3.13.2, `adb.exe`, 12 GB RAM)
- [x] Passo 0: Criar `va2/tec.md` com a ideia completa e o § 6b do `brw.md` no topo
- [x] Montar o `ROADMAP.md` e o `HANDOFF.md` para iniciar a Sessão 1 com contexto 100% limpo (0 tokens acumulados)

#### Sessão 1 · Passo 1 (Escolha das Peças) e Passo 2 (Commit 1: Exemplo Oficial Rodando) — CONCLUÍDO
- [x] **Passo 1 · As Peças (`§ 1` de `va2/tec.md`):**
  - Levantadas as 4 peças abertas com links oficiais e licenças conferidas (`faster-whisper` MIT, `whisper.cpp` MIT, `NetworkX` BSD-3 / `Qwen` Apache 2.0 via `Ollama` MIT, e regras exatas determinísticas).
  - Justificativas ditadas pelo Edson registradas na tabela do `§ 1` de `va2/tec.md`.
- [x] **Passo 2 · O Exemplo Oficial Rodando (`§ 2` de `va2/tec.md` + Commit 1):**
  - Criada a pasta `tec/` com `transcrever.py`, `requirements.txt`, `README.md` e o áudio de exemplo oficial `audio.mp3`.
  - Instalado `faster-whisper` e dependências compatíveis com Python 3.13 no Windows.
  - Executado o exemplo com saída real: transcrito o áudio com modelo `small` em CPU INT8.
  - Feito o **Commit 1** (`84bb835 · exemplo oficial do faster-whisper, rodando em CPU`) e enviado ao GitHub.
  - Preenchida a seção `§ 2` de `va2/tec.md`.

#### Sessão 2 · Passo 3 (Commit 2: Alteração Mínima com Todas as Tecnologias + Execução e APK Android)
- [ ] **Preparação dos Dados do Problema (`tec/audios/` e `tec/bares_grafo.json`):**
  - Gerar 3 áudios em português simulando pedidos reais de pessoa cega para bares (incluindo ruído de fundo e desafios de cardápio com erro real documentado).
  - Montar a base de cardápios e relatos de conforto acústico para o GraphRAG.
- [ ] **Alteração Mínima (`tec/transcrever.py`):**
  - Integrar transcrição + GraphRAG/LLM + Regras determinísticas (conta sem erro, Haversine/GPS, senha de 4 dígitos e Pix).
- [ ] **Execução e APK no Celular Android:**
  - Configurar `whisper.cpp` para Android `arm64-v8a` e medição nativa no aparelho do Edson via `adb` (medindo tempo de carga, RTF e RAM).
  - Gerar o pacote/APK de instalação demonstrável para o celular do Edson conforme solicitado.
- [ ] **Commit 2 e Preenchimento do `§ 3` de `va2/tec.md`:**
  - Fazer o **Commit 2** com a alteração mínima.
  - Colar comandos, saídas reais e documentar onde a saída erra no `§ 3` de `va2/tec.md`.
- [ ] Atualizar `HANDOFF.md` e `ROADMAP.md` ao final da Sessão 2.

#### Sessão 3 · Passo 4 (Vídeo), Passo 5 (Resumo sem IA) e Passo 6 (Auditoria Cega + Push Final)
- [ ] **Passo 4 · O Vídeo (`§ 4` de `va2/tec.md`):**
  - Gravar o vídeo curto de terminal/tela (mostrando `git log` com os dois commits, o comando rodando na máquina/Android via `adb` e a saída aparecendo; podemos automatizar a gravação em `.mp4` com `ffmpeg` capturando a janela do terminal para o Edson não ter dificuldade visual com o OBS!).
  - Publicar o vídeo em link que abra sem login (Release do próprio GitHub ou no repositório) e colar no `§ 4`.
- [ ] **Passo 5 · O que deu certo e o que errou, sem IA (`§ 5` de `va2/tec.md`):**
  - Entrevistar o Edson para ele ditar em até 4 linhas, com as próprias palavras, o que acertou, onde errou, quanto tempo/máquina pediu (no PC e no Android) e o que isso muda na ideia.
- [ ] **Passo 6 · Auditoria Cega (`§ 6` de `va2/tec.md`):**
  - Rodar um subagente auditor cético em contexto isolado com o prompt oficial do Passo 6 (conferindo links, licenças, código-fonte local/Android e o `git diff` entre o Commit 1 e o Commit 2).
  - Colar a tabela bruta do auditor no `§ 6` e colher do Edson as 3 linhas de resposta **sem IA** (1 por item).
  - Verificar consistência entre `va2/brw.md`, `va2/cmp.md` e `va2/tec.md`, fazer `git push` e preparar a mensagem para o Telegram do professor Lucas.

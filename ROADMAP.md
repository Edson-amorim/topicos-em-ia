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

## Artefato 3 (VA2): Tecnologia Base Demonstrável (`va2/tec.md`) — CONCLUÍDO

### Regra de Ouro de Gestão de Sessões e Contexto
- **Limite rígido por sessão:** **máximo de 100k a 120k tokens** para evitar autocompactação e perda de precisão.
- **Transição entre sessões:** ao final de cada sessão (ou se o contexto se aproximar de 100k tokens), o agente atualiza `ROADMAP.md` e `HANDOFF.md` no disco e avisa o Edson para abrir uma nova sessão colando o prompt de retomada do `HANDOFF.md`.
- **Cobertura Tecnológica Obrigatória + Plataforma Alvo (Android):**
  1. **Peça Central 1 (Fala → Texto no PC/Servidor):** `faster-whisper` (`SYSTRAN/faster-whisper`, MIT) comparado a `openai/whisper` (MIT) e `Vosk` (`alphacep/vosk-api`, Apache 2.0).
  2. **Peça Central 2 (Fala → Texto rodando nativo no Android):** `whisper.cpp` (`ggml-org/whisper.cpp`, MIT, binário `arm64-v8a` / Android via `adb`), medindo tempo de carga, RTF (tempo/duração do áudio) e memória RAM diretamente no aparelho Android.
  3. **Peça Central 3 / Apoio de IA (GraphRAG + Modelo de Linguagem Local):** grafo de conhecimento de bares, cardápios bagunçados, ingredientes e relatos de conforto (`NetworkX`, BSD-3-Clause + busca em grafo/RAG) integrado a modelo de linguagem local aberto (`Qwen3-4B` / `Qwen2.5-3B` Apache 2.0 via `Ollama` MIT ou `llama.cpp` MIT) para filtrar bar e pratos e extrair o pedido estruturado.
  4. **Peça de Apoio 4 (Regras Exatas Determinísticas):** módulo de regras exatas em Python/Android para cálculo da conta sem erro, validação de teto de gasto, verificação de raio GPS (Haversine), geração da senha falada de 4 dígitos e código Pix (BRCode).

### Divisão em Sessões Planejadas

#### Sessão 0 (Planejamento e Preparação) — CONCLUÍDO
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

#### Sessão 2 · Passo 3 (Commit 2: Alteração Mínima com Todas as Tecnologias + Execução e APK Android) — CONCLUÍDO
- [x] **Preparação dos Dados do Problema (`tec/audios/` e `tec/bares_grafo.json`):**
  - Gerados 3 áudios em português simulando pedidos reais para bares (busca de bar acústico, pedido direto e pedido sob ruído de bar documentando o erro real).
  - Montada a base de conhecimento de bares do Recife, cardápios, relatos de conforto acústico e coordenadas GPS.
- [x] **Alteração Mínima (`tec/transcrever.py`, `tec/grafo.py` e `tec/regras.py`):**
  - Integrada a transcrição com `faster-whisper` em CPU INT8 + `initial_prompt` de cardápio.
  - Busca por conforto acústico via GraphRAG com `NetworkX`.
  - Regras determinísticas: cálculo exato da comanda em reais sem erro de centavos, validação de teto de gasto, distância Haversine via GPS, senha falada de 4 dígitos e geração de payload Pix Copia e Cola (BR Code EMV com CRC16 válido).
- [x] **Execução e APK no Celular Android:**
  - Criado o app Android nativo acessível com leitor de telas TalkBack e TextToSpeech em `tec/android_app/`.
  - Compilado e assinado o APK demonstrativo pronto para instalação: `tec/comanda-falada.apk` (16,8 KB) usando o Android SDK e JDK 17 locais.
- [x] **Commit 2 e Preenchimento do `§ 3` de `va2/tec.md`:**
  - Feito o **Commit 2** (`c032f22 · alteracao minima: pedidos de bar, graphrag, regras deterministicas e apk android`) e enviado ao GitHub.
  - Coladas as saídas reais e documentado onde a IA erra no `§ 3` de `va2/tec.md`.
- [x] Atualizado `HANDOFF.md` e `ROADMAP.md` ao final da Sessão 2.

#### Sessão 3 · Passo 4 (Vídeo), Passo 5 (Resumo sem IA) e Passo 6 (Auditoria Cega + Push Final) — CONCLUÍDO
- [x] **Passo 4 · O Vídeo com Voz e Execução no Celular Android (`§ 4` de `va2/tec.md`):**
  - Gerado o vídeo demonstrativo narrado em alta definição `tec/demonstracao.mp4` (duração: 2 min 26 s, 5,2 MB), com faixa de áudio e voz falada em português sincronizada.
  - O vídeo exibe: histórico do `git log --oneline -n 4` com os dois commits (`84bb835` e `c032f22`), comando `python transcrever.py` rodando o pipeline, e a tela real do smartphone Samsung Galaxy A15 do aluno executando o aplicativo nativo com os botões acionados (Caso 1: Bar Cais do Sertão no GraphRAG, Caso 2: Comanda R$ 22,00 com Pix, e Caso 3: Documentação honesta da falha acústica sob ruído de bar).
  - Registrado o link público no repositório GitHub sem exigência de login.
- [x] **Passo 5 · O que deu certo e o que errou, sem IA (`§ 5` de `va2/tec.md`):**
  - Entrevistado o Edson e registradas as 4 linhas com suas próprias palavras sobre o acerto da comanda, erro no ruído do bar, leveza no PC/Android e necessidade de digitação/regras exatas.
- [x] **Passo 6 · Auditoria Cega (`§ 6` de `va2/tec.md`):**
  - Subagente auditor cético executado em contexto isolado com o prompt oficial do Passo 6.
  - Veredito bruto colado no `§ 6` de `va2/tec.md`.
  - Registrada a resposta do aluno sem IA item por item, conforme as regras da disciplina.
- [x] **Push Final e Envio:**
  - Git commit e git push de todos os artefatos, vídeo e código.

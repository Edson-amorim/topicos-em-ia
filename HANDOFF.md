# HANDOFF · Transição de Sessão (AI for Good · UFRPE)

**Data de Atualização:** 01/11/2026  
**Aluno:** Edson Amorim (`edsoninformatica28br@gmail.com`)  
**Repositório Local:** `C:\topicos-em-ia` (`https://github.com/Edson-amorim/topicos-em-ia`)  
**Projeto:** **Comanda Falada · Passo à Frente**  
**Artefato Atual:** `[3] artefato · tecnologia base demonstrável` (`C:\topicos-em-ia\va2\tec.md`)  
**Status Atual:** **Sessão 2 CONCLUÍDA (Passo 3 Concluído, Commits 1 e 2 no GitHub, APK Gerado)**  
**Próxima Sessão:** **Sessão 3 do `tec.md` (Passo 4: Vídeo, Passo 5: O que Deu Certo e Errou sem IA, Passo 6: Auditoria Cega e Envio Final)**

---

## 1. Regras Críticas de Acessibilidade, Pedagogia e Contexto

1. **Acessibilidade para Leitor de Telas (`C:\topicos-em-ia\AGENTS.md`):**
   - Edson é uma pessoa cega e utiliza leitor de telas.
   - Toda comunicação no chat DEVE ser em português do Brasil (`pt-BR`), clara, direta e estruturada em texto corrido ou listas numeradas simples.
2. **Regra Pedagógica ("A IA faz, o aluno domina"):**
   - Entrevistar o Edson no Passo 5 para ele ditar com suas próprias palavras o que deu certo e onde errou.
   - No Passo 6, rodar o auditor cético em contexto novo isolado e colher as 3 linhas de resposta **sem IA** diretamente do Edson (nunca sugerir ou redigir pelo aluno).
3. **Orçamento de Contexto (< 120k tokens):**
   - A Sessão 2 foi concluída bem abaixo do limite para garantir transição limpa. Ao iniciar a Sessão 3, abrir uma nova conversa com contexto limpo.

---

## 2. O Que Foi Concluído na Sessão 2 (Passo 3 do `tec.md`)

- **Preparação dos Dados do Problema:**
  - Criados os 3 áudios em português em `tec/audios/` (`audio1_busca_bar.wav`, `audio2_pedido_direto.wav` e `audio3_pedido_ruido.wav`).
  - Criada a base de conhecimento de bares do Recife e cardápios com relatos de ruído acústico e acessibilidade em `tec/bares_grafo.json`.
- **Implementação e Alteração Mínima:**
  - `tec/transcrever.py`: pipeline completo em CPU INT8 com injeção de cardápio via `initial_prompt`.
  - `tec/grafo.py`: recuperação em grafo via `NetworkX` filtrando bares por baixo ruído acústico.
  - `tec/regras.py`: cálculo determinístico da comanda sem erro de centavos, teto de gastos, cálculo GPS via Haversine, senha de 4 dígitos e código Copia e Cola Pix com CRC16 válido.
  - Documentação honesta e cristalina de onde a IA erra quando submetida a ruído acústico no Caso 3.
- **APK Demonstrativo Android Gerado e Assinado:**
  - Código nativo em `tec/android_app/` com layout acessível e síntese de voz (TTS).
  - Script autônomo `tec/compilar_apk.ps1` usando o Android SDK e JDK 17 locais.
  - Gerado o pacote instalável: `C:\topicos-em-ia\tec\comanda-falada.apk` (16,8 KB).
- **Commits no GitHub:**
  - Commit 1: `[84bb835 · exemplo oficial do faster-whisper, rodando em CPU](https://github.com/Edson-amorim/topicos-em-ia/commit/84bb835)`
  - Commit 2: `[c032f22 · alteracao minima: pedidos de bar, graphrag, regras deterministicas e apk android](https://github.com/Edson-amorim/topicos-em-ia/commit/c032f22)`
- **Preenchimento do `va2/tec.md`:**
  - Seção `## 3 · a alteração mínima` preenchida com commit, entrada, saída real e análise honesta de erro.

---

## 3. Instruções Exatas para a Sessão 3 (Passos 4, 5 e 6)

1. **Passo 4 · O Vídeo Demonstrativo (`§ 4`):**
   - Gravar o terminal/tela demonstrando o `git log` com os dois commits e o comando `python transcrever.py` rodando e gerando a saída.
   - Publicar o vídeo em release do GitHub ou no próprio repositório para acesso sem login e colar o link e minutagem no `§ 4`.
2. **Passo 5 · O que deu certo e o que errou, sem IA (`§ 5`):**
   - Entrevistar o Edson para colher em até 4 linhas com suas próprias palavras: o que a técnica acertou, onde errou no ruído, quanto tempo/máquina pediu e o que muda na ideia.
3. **Passo 6 · Auditoria Cega (`§ 6`):**
   - Rodar subagente auditor cético no Passo 6 julgando os itens a partir de "não passa".
   - Colar a tabela bruta no `§ 6`.
   - Colher do Edson as 3 linhas de resposta sem IA (1 por item).
4. Fazer o commit final, `git push` e orientar o envio para a banca/professor.

---

## 4. Prompt Pronto para o Aluno Colar na Sessão 3

```text
Olá! Concluímos com sucesso a Sessão 2 do artefato va2/tec.md (Passo 3: alteração mínima completa, Commit 2 no GitHub e APK gerado). Leia os arquivos C:\topicos-em-ia\HANDOFF.md, C:\topicos-em-ia\ROADMAP.md e C:\topicos-em-ia\va2\tec.md e vamos iniciar a Sessão 3 (Passo 4: gravação e link do vídeo demonstrativo, Passo 5: o que deu certo e errou sem IA, e Passo 6: auditoria cega e push final).
```

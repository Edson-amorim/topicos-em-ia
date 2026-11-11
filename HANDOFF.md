# HANDOFF · Transição de Sessão (AI for Good · UFRPE)

**Data de Atualização:** 08/11/2026  
**Aluno:** Edson Amorim (`edsoninformatica28br@gmail.com`)  
**Repositório Local:** `C:\topicos-em-ia` (`https://github.com/Edson-amorim/topicos-em-ia`)  
**Projeto:** **Comanda Falada · Passo à Frente**  
**Artefato Concluído:** `[3] artefato · tecnologia base demonstrável` (`C:\topicos-em-ia\va2\tec.md`) — **CONCLUÍDO COM SUCESSO**  
**Status Atual:** **Artefatos 1 (`brw.md`), 2 (`cmp.md`), 3 (`tec.md`), Apresentação (`apresentacao.html`) e APK Android v6.0 com Banco de Dados SQLite (`tec/comanda-falada.apk`) Concluídos e Auditados!**  
**Próximo Artefato:** **Artefato 4 (Método / Experimentos / Artigo da VA2) ou Evoluções do App Android**

---

## 1. O Que Foi Concluído no Aplicativo Android `Comanda Falada` (v6.0)

- **Arquivo APK Pronto para Instalação (Independente, Não Precisa da Pasta):**
  - `C:\topicos-em-ia\tec\comanda-falada.apk` (~115,6 KB, assinado com V1/V2/V3).
- **Ícone Oficial Personalizado (`@mipmap/ic_launcher` e `tec/icone_comanda_falada.png`):**
  - Traz o **Símbolo Universal da Acessibilidade** no topo e **duas pessoas (uma pessoa com deficiência e uma pessoa sem deficiência) segurando juntas um cardápio aberto** com prato e itens de alimentação, em alto contraste e com descrição completa para o TalkBack.
- **Código-Fonte Android e Banco de Dados SQLite:**
  - `C:\topicos-em-ia\tec\android_app\src\br\ufrpe\comandafalada\MainActivity.java`
  - `C:\topicos-em-ia\tec\android_app\src\br\ufrpe\comandafalada\ComandaBancoDadosHelper.java`
  - `C:\topicos-em-ia\tec\android_app\res\layout\activity_main.xml`
  - `C:\topicos-em-ia\tec\android_app\AndroidManifest.xml`
  - Scripts: `C:\topicos-em-ia\tec\compilar_apk.ps1` e `C:\topicos-em-ia\tec\gerar_icone.ps1`
- **Funcionalidades Implementadas no APK v6.0:**
  1. **Leitor de Tela Nativo do Sistema Android (TalkBack):**
     - Removido qualquer sintetizador `TextToSpeech` interno separado para nunca atropelar o leitor do sistema.
     - Comunicação 100% direta e imediata pelo leitor de tela nativo do sistema (`TalkBack` via `View.announceForAccessibility(...)` post-frame e `contentDescription`, sem duplicações, sem chamadas concorrentes antes de diálogos e sem recitar itens desnecessários ao filtrar).
  2. **Filtros Completos do Brasil (Região, Estado/UF, Cidade com os 185 Municípios de PE e Categorias Exclusivas para Comer e Consumir):**
     - **Filtro por Região:** Todas as 5 regiões do Brasil (`Nordeste`, `Sudeste`, `Sul`, `Centro-Oeste`, `Norte`).
     - **Filtro por Estado (UF):** Todos os 27 estados brasileiros (`AC` a `TO`).
     - **Filtro por Cidade:** Inclui **todos os 185 municípios de Pernambuco** (`TODAS_CIDADES_PERNAMBUCO`), além das principais cidades dos 27 estados e uma opção **"🔎 Pesquisar / Digitar Nome da Cidade..."** no topo da lista.
     - **Filtro por Categoria (`CATEGORIAS_ESTABELECIMENTO` — Apenas locais onde o usuário pode comer e consumir):**
       1. `Todas as Categorias (Para Comer e Consumir)`
       2. `Restaurantes, Bistrôs e Self-Service`
       3. `Bares, Botecos e Petiscarias`
       4. `Padarias, Cafeterias e Confeitarias`
       5. `Pizzarias, Hamburguerias e Lanchonetes`
       6. `Churrascarias, Espetinhos e Peixarias`
       7. `Sorveterias, Açaí, Docerias e Tapiocarias`
       8. `Mercados Gastronômicos, Quiosques e Bistrôs Culturais`
     - **Localizar Qualquer Estabelecimento com Cardápio Disponível:** Busca por texto, voz ou compartilhamento do Google Maps que encontra ou gera automaticamente o cardápio adequado à categoria para qualquer estabelecimento de qualquer cidade do Brasil.
  3. **Integração de Mão Dupla com o Google Maps (App -> Maps e Maps -> App):**
     - **Do Comanda Falada para o Google Maps:** Botão `📍 Maps` ao lado de cada bar, botão `📍 Rota no Maps` para traçar a rota até o bar selecionado e botão `🗺️ Ver Bares da Cidade no Maps`.
     - **Do Google Maps para o Comanda Falada (Abrir Cardápio pelo Maps):**
       - Registrados `intent-filter`s no `AndroidManifest.xml` (`ACTION_SEND` `text/plain` e `ACTION_VIEW` para `geo:`, `maps.google.com`, `google.com/maps`, `maps.app.goo.gl`, `goo.gl`).
       - Quando o usuário está no **Google Maps**, escolhe qualquer bar ou restaurante, toca em **"Compartilhar"** e seleciona **Comanda Falada** (ou abre um link do Google Maps), o `MainActivity` recebe o local (`tratarIntentRecebidaDoGoogleMaps`), identifica o bar e a cidade e **abre imediatamente o cardápio interativo** daquele bar para o usuário escolher os itens!
       - Também inclui o botão `📲 Abrir Cardápio de Bar Vindo do Google Maps` dentro do app para importar da área de transferência ou testar o fluxo em 1 toque.
  4. **Cardápio Completo Interativo (16 a 24 Itens por Estabelecimento + Janela Rápida + Rolagem/Foco Automático) + Soma da Comanda com 10% + Pagamento nos Bancos do Celular:**
     - **Cardápio Completo (`completarCardapioDoEstabelecimento`):** Todos os estabelecimentos (tanto os 20 pré-carregados quanto qualquer local aberto via filtro de cidade/categoria ou Google Maps) possuem agora um **cardápio completo de 16 a 24 itens** divididos por seções (`Entrada`, `Petisco`, `Prato Principal`, `Prato Individual`, `Lanche / Pizzaria / Tapiocaria`, `Bebida sem Álcool`, `Bebida Alcoólica / Drinks` e `Sobremesa / Cafeteria`).
     - **Abertura Imediata em Janela Modal Acessível (`abrirDialogoCardapioCompletoInterativo`) + Foco Automático (`rolarParaCardapioEAbrirJanelaCompleta`):** Ao tocar em qualquer estabelecimento na lista (ou abrir pelo Google Maps / busca), o app rola automaticamente até a Seção 2, move o foco do TalkBack para o título do cardápio e abre imediatamente uma janela com **100% dos itens numerados** para o usuário escolher sem precisar varrer a lista de estabelecimentos. Também há o botão dedicado `📖 Abrir Cardápio Completo em Lista Rápida` no topo da Seção 2.
     - Escolha de pratos, petiscos, bebidas e sobremesas por toque (`➕` / `➖ 1`) ou por comando de voz (`🎙️ Falar Pedido para a IA Somar na Comanda`).
     - **Pergunta sobre a Taxa de 10% do Garçom (`perguntarTaxaDezPorCento`):** Na soma da comanda (ao tocar no botão `💁 Deseja adicionar os 10% na soma?`, ao ouvir a soma ou antes de copiar o Pix e abrir o banco), o app pergunta em voz alta pelo leitor de tela (TalkBack) e na tela se o usuário quer adicionar os **10% de taxa de serviço**, exibindo o **Subtotal dos itens**, o **valor exato dos 10%** e a **Soma Total com ou sem os 10%**!
     - Soma exata em centavos (`R$`), alerta de teto de gastos (`R$ 50`, `R$ 100`, `R$ 150`, `R$ 200`), senha falada de 4 dígitos e geração de **Pix Copia e Cola (BR Code BACEN com CRC16 válido)**.
     - Detecção dos **aplicativos de bancos instalados no celular** (`Nubank`, `Banco Inter`, `Itaú`, `Bradesco`, `Banco do Brasil`, `Caixa`, `Caixa Tem`, `Santander`, `PicPay`, `Mercado Pago`, `C6 Bank`, `PagBank`, `Neon`, `Next`, `Will Bank`, `Sicredi`, `Sicoob`, etc.): copia o Pix automaticamente e abre o app do banco escolhido.
  5. **Banco de Dados Relacional Nativo `SQLite` (`ComandaBancoDadosHelper` — `comanda_falada.db`):**
     - **5 Tabelas Relacionais Offline:**
       1. `estabelecimentos` (armazena o catálogo nacional e qualquer local novo vindo do Google Maps ou filtro);
       2. `cardapio_itens` (armazena todos os pratos, bebidas, preços em centavos e itens cadastrados pelo usuário);
       3. `comanda_ativa` (salva automaticamente cada item adicionado ou removido em tempo real, evitando perda de dados ao fechar o app ou alternar para o Maps/Banco);
       4. `configuracoes_sessao` (persiste o último estabelecimento aberto, o teto de gastos e a escolha dos 10%);
       5. `historico_comandas` (registra data/hora, nome do local, cidade/UF, itens consumidos, subtotal, 10%, total pago e senha de entrega).
     - **Botões Dedicados na Interface:**
       - `➕ Cadastrar Novo Prato / Bebida no Banco de Dados (SQLite)` na Seção 2;
       - `🗄️ Banco de Dados SQLite: Salvar / Ouvir Histórico de Comandas` na Seção 3.

---

## 2. O Que Foi Concluído nos Artefatos Acadêmicos (`va2/`)

1. `va2/brw.md` (Brainwriting Híbrido) — **Concluído**
2. `va2/cmp.md` (Competidores e Contribuição) — **Concluído e Auditado**
3. `va2/tec.md` (Tecnologia Base Demonstrável) — **Concluído e Auditado**
4. `apresentacao.html` (Apresentação Web Acessível com 5 abas e áudio) — **Concluído**
5. `tec/demonstracao.mp4` (Vídeo demonstrativo com voz real do Edson e captura no Android) — **Concluído**

---

## 3. Outros Projetos Atualizados na Sessão Anterior (Jogos Acessíveis)

- **Yu-Gi-Oh! Duelo Acessível da Luna integrado ao Jogos da Luna:**
  - Pasta do projeto: `C:\jogos_acessivels\JogosDaLuna_Android`
  - APK pronto: `C:\jogos_acessivels\JogosDaLuna.apk` (permite ao jogador informar o nome e escolher entre 3 decks clássicos acessíveis no TalkBack).

---

## 4. Prompt Pronto para Iniciar a Nova Sessão

Copie e cole o texto abaixo ao abrir uma nova sessão no Antigravity:

```text
Leia o arquivo C:\topicos-em-ia\HANDOFF.md para carregar todo o contexto do projeto Comanda Falada · Passo à Frente (C:\topicos-em-ia).
Verifique que o APK C:\topicos-em-ia\tec\comanda-falada.apk (v4.0) já está compilado com:
1. Leitor de tela nativo do sistema Android (TalkBack via AccessibilityManager, sem TTS interno separado);
2. Filtros por Região (5 regiões), Estado/UF (27 estados) e Cidade;
3. Integração bidirecional com o Google Maps (abrir rota no Maps e, quando o usuário compartilhar ou abrir um bar pelo Google Maps, abrir direto o cardápio do bar no Comanda Falada);
4. Cardápio interativo, soma automática da comanda pela IA com Pix Copia e Cola e botão para abrir os aplicativos de bancos instalados no celular.
Me confirme que leu o HANDOFF.md e me pergunte qual será o nosso próximo passo!
```

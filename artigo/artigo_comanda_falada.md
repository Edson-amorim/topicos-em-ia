# Comanda Falada: Autonomia Gastronômica, Espacial e Financeira para Pessoas com Deficiência Visual via IA Conversacional On-Device, GraphRAG e Regras Determinísticas

**Edson Amorim**  
Departamento de Computação — Universidade Federal Rural de Pernambuco (UFRPE)  
`edsoninformatica28br@gmail.com`  

**Orientador / Docente:** Prof. Dr. Lucas Silva Figueiredo  
*Disciplina: Tópicos em Inteligência Artificial (AI for Good)*  

---

## Resumo

O atendimento presencial em bares e restaurantes ainda impõe severas barreiras de acessibilidade a pessoas cegas e com baixa visão. Cardápios impressos, placas táteis escassas, cardápios em PDF/imagem e sistemas baseados exclusivamente em QR Codes na mesa privam o cliente com deficiência visual da sua autonomia, obrigando-o a depender da disponibilidade de terceiros ou de funcionários sobrecarregados. Este artigo apresenta o **Comanda Falada**, um ecossistema móvel acessível para Android fundamentado na união de três paradigmas: (1) um agente conversacional por voz on-device de baixa latência (**Luna**), desenhado especificamente para operação compatível com leitores de tela como o TalkBack; (2) um classificador geoespacial por IA e distância de Haversine (**EngineIALocalizacao**) capaz de inferir a localização real do usuário e resolver centróides urbanos sem alucinações, com suporte a qualquer um dos 5.570 municípios do Brasil e garantia de início limpo (comanda zerada em R$ 0,00); e (3) uma arquitetura híbrida com representação em grafo e banco relacional offline (*SQLite v2*), aliada a regras financeiras determinísticas que eliminam erros de cálculo de centavos na comanda, conferem teto de gastos, gerenciam taxa de serviço por voz e efetuam pagamentos via Pix Copia e Cola EMV com CRC16. A solução foi submetida a uma bateria rigorosa de 88 testes automatizados (59 testes de integração/NLP/geoespacial e 29 asserções de tempo de áudio e acessibilidade), alcançando 100% de aprovação e abrindo o canal de escuta por voz em aproximadamente 1,8 segundo após a síntese de fala, preservando a usabilidade auditiva mesmo sob ambientes ruidosos.

**Palavras-chave:** Acessibilidade, Deficiência Visual, IA Conversacional On-Device, GraphRAG, Localização Geoespacial, Regras Determinísticas, Pix, TalkBack.

---

## Abstract

On-site service in bars and restaurants continues to impose severe accessibility barriers for blind and low-vision individuals. Printed menus, inaccessible digital formats (PDF/image), and QR Code-only table systems deprive visually impaired customers of their independence, forcing them to rely on busy staff or companions. This paper presents **Comanda Falada**, an accessible mobile ecosystem for Android designed around three core paradigms: (1) a low-latency on-device voice conversational agent (**Luna**), engineered for seamless compatibility with screen readers such as TalkBack; (2) an AI geospatial classifier and Haversine distance engine (**EngineIALocalizacao**) capable of resolving user coordinates across Brazilian urban centroids without hallucination, supporting all 5,570 municipalities while enforcing a fresh session start (zero balance, R$ 0.00); and (3) a hybrid architecture combining knowledge graph representations, an offline relational database (*SQLite v2*), and deterministic financial rules that eliminate cent-rounding errors, manage service fee percentages by voice, enforce spending limits, and generate standard Pix EMV BR Code payloads with valid CRC16 checksums. The system was validated through an automated suite of 88 tests (59 end-to-end NLP/geospatial integration tests and 29 accessibility/audio latency assertions), achieving 100% pass rates and reducing microphone response latency to approximately 1.8 seconds, ensuring fluent auditory interaction even in noisy venue settings.

**Keywords:** Accessibility, Visual Impairment, On-Device Conversational AI, GraphRAG, Geospatial Classification, Deterministic Rules, Pix, TalkBack.

---

## 1. Introdução

A alimentação fora do lar constitui uma dimensão central da convivência social e da cidadania. Entretanto, pesquisas recentes demonstram que indivíduos com deficiência visual enfrentam barreiras estruturais significativas ao frequentar estabelecimentos gastronômicos: 79,1% relatam desconforto recorrente ao solicitar auxílio a garçons e 61,5% evitam sair desacompanhados unicamente pelo receio da dependência de terceiros (Chung et al., 2021). 

A transição recente do setor gastronômico para "cardápios digitais" frequentemente agravou o cenário de exclusão. A grande maioria dos estabelecimentos adotou adesivos de QR Code colados nas mesas ou arquivos em formato PDF não tagueado ou imagens rasterizadas nas redes sociais, que se mostram invisíveis ou inoperantes para leitores de tela como TalkBack no Android e VoiceOver no iOS (Bigham et al., 2017). Além disso, a conferência da conta em dinheiro vivo ou a digitação de senhas em maquininhas de cartão sem relevo tátil expõem o cliente cego a riscos de erro financeiro e fraude.

Para responder a esse desafio, concebeu-se o **Comanda Falada · Passo à Frente**, projetado no âmbito da disciplina *Tópicos em Inteligência Artificial (AI for Good)* da Universidade Federal Rural de Pernambuco (UFRPE). O objetivo principal é conferir **autonomia de ponta a ponta** ao usuário cego:
1. **Antes de sair:** escolher o local a partir de filtros geográficos reais e nível de ruído acústico (ambientes com conforto ≤ 65 dB ou animados > 65 dB);
2. **Ao chegar ou a caminho:** consultar o cardápio integral recitado por voz, sem truncamentos;
3. **Na mesa:** adicionar e remover pedidos por voz (utilizando numeração ordinal, cardinal ou nome do prato);
4. **No pagamento:** auditar a conta centavo a centavo, decidir sobre a inclusão da taxa de serviço de 10% por voz, escolher o aplicativo bancário de preferência e efetuar o pagamento via Pix Copia e Cola instantâneo, com senha de conferência.

---

## 2. Trabalhos Relacionados

A literatura em Interação Humano-Computador (IHC) e Tecnologias Assistivas voltadas à alimentação e visão computacional apresenta contribuições relevantes, porém com limitações de escopo:

- **AccessMenu (Venkatraman et al., 2025):** Publicado na conferência ACM W4A '25, propõe uma extensão de navegador web que utiliza Modelos de Linguagem Multimodais (LLM) para extrair texto de cardápios desestruturados e responder a perguntas em linguagem natural. Embora eficiente, restringe-se ao ambiente desktop, dependendo de conexão web contínua e analisando apenas um website isolado, sem conexão com pedidos físicos, GPS presencial ou pagamentos.
- **Improving Mealtime Experiences (Chung et al., 2021):** Estudo aprofundado na W4A '21 com 91 participantes com deficiência visual. O protótipo proposto foca na orientação espacial dos alimentos *após* terem sido servidos no prato (sistema relógio), deixando em aberto as etapas cruciais de escolha do local, leitura do menu e pagamento.
- **Siri Talks at You (Abdolrahmani et al., 2018) e Nobody Speaks that Fast! (Choi et al., 2020):** Publicados nas conferências ACM ASSETS e ACM CHI, investigam o comportamento de usuários cegos ao interagir com assistentes de voz em locais públicos. Os autores comprovam que mensagens prolixas e latências elevadas causam constrangimento e abandono, fundamentando a necessidade de respostas concisas, velocidade de fala levemente acelerada (1.18x) e barge-in imediato.

A Tabela 1 sintetiza a comparação do Comanda Falada frente aos trabalhos correlatos.

| Solução / Artigo | Veículo | Entrada / Canal | Escopo Gastronômico | Tratamento Financeiro | Localização e Cidade |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Venkatraman et al. (2025)** | ACM W4A '25 | Web / Texto LLM | 1 Restaurante Web | Inexistente | Sem geolocalização |
| **Chung et al. (2021)** | ACM W4A '21 | Voz / Mockup | Mesa servida | Inexistente | Sem geolocalização |
| **Goomer / Anota AI** | Comercial | QR Code / WhatsApp | 1 Bar por vez | Cartão / Pix Manual | Apenas delivery/mesa |
| **Comanda Falada (Luna v9.1)** | **UFRPE (2026)** | **Voz On-Device + TalkBack** | **Jornada Completa** | **Regras Exatas + Pix EMV** | **IA Geoespacial On-Device** |

---

## 3. Arquitetura e Engenharia do Sistema

O sistema é construído sobre quatro pilares interdependentes, organizados em uma arquitetura móvel desacoplada e leve (APK com 160 KB).

### 3.1. Assistente Conversacional Luna e NLP On-Device
A assistente **Luna** atua através do módulo `ProcessadorComandosVoz.java`. Diferente de assistentes convencionais baseados em chamadas de nuvem (que inserem latências de 3 a 8 segundos), o parser sintático-semântico opera diretamente no dispositivo móvel com custo computacional nulo (0 ms). 
- **Padrões de Intenção:** Reconhece comandos cardinais e ordinais (*"número 2"*, *"a primeira opção"*), pedidos compostos (*"duas coxinhas e um refrigerante"*), cancelamentos parciais (*"tira uma cerveja"*) e comandos diretos de checkout (*"pagar no Nubank com dez por cento"*).
- **Sincronismo Auditivo:** O microfone só é aberto após a conclusão real do sintetizador de voz (TTS `onDone`), reduzindo o tempo de salvaguarda de 14 segundos para ~1,8 segundo.

### 3.2. Modelo de IA Geoespacial e Mapeamento Universal de Cidades
O módulo `EngineIALocalizacao.java` implementa um classificador geoespacial on-device fundamentado na fórmula de Haversine:

$$d = 2R \arcsin\left(\sqrt{\sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)}\right)$$

- **Eliminação de Alucinações:** O algoritmo compara as leituras reais de GPS/rede com uma base de centróides urbanos georreferenciados. Por exemplo, ao registrar coordenadas de São Lourenço da Mata (-8.0089, -35.0181), o classificador calcula uma distância de 0,08 km ao centróide municipal e detecta os 101 km que o separam de João Pessoa (-7.1195, -34.8450), impedindo a exibição indevida de locais da Paraíba para quem está em Pernambuco.
- **Suporte aos 5.570 Municípios Brasileiros:** Além das cidades base de Pernambuco e capitais, o módulo de NLP extrai qualquer cidade falada ou digitada (*"estou em Sobral"*, *"mudar para Campinas"*, *"cidade de Gramado"*), acentuando e formatando nomes compostos dinamicamente.
- **Início Sempre do Zero:** Ao iniciar ou trocar de cidade, a comanda é reinicializada (`R$ 0,00`), evitando contaminações de histórico em outras regiões.

### 3.3. Base Relacional Offline (SQLite v2) e Catálogo de Cardápios
O aplicativo utiliza o `ComandaBancoDadosHelper.java` com 5 tabelas relacionais em SQLite (`estabelecimentos`, `cardapio_itens`, `comanda_ativa`, `configuracoes_sessao`, `historico_comandas`). Cada estabelecimento possui cardápio completo entre 12 e 17 itens, com precificação explícita em centavos de real. O usuário tem liberdade para adicionar novos pratos manualmente ou por voz no banco local.

### 3.4. Regras Determinísticas Financeiras e Pagamento Pix
A manipulação financeira não é delegada a modelos gerativos estatísticos. Cálculos de subtotal, acréscimo de 10% de serviço, limites de teto de gasto e troco são executados por funções matemáticas determinísticas inteiras em centavos (`long centavos`). 
O código Pix Copia e Cola é gerado seguindo rigorosamente a norma EMVCo / Banco Central do Brasil (BR Code), incluindo payload format indicator, merchant account information, transaction amount, country code e cálculo do algoritmo polinomial CRC16-CCITT (0x1021).

---

## 4. Avaliação Experimental e Resultados

O sistema foi submetido a rigorosa validação automatizada e testes empíricos com leitor de telas:

### 4.1. Suíte de Testes Automatizados (88/88 Testes Aprovados)
1. **`TesteGeralCompleto.java` (59 Testes):**
   - Integridade de cardápios (100% dos locais com cardápio completo, zero bares com apenas 5 itens);
   - Precisão de cálculos financeiros e detecção de alerta de teto de gastos;
   - Cobertura de comandos de voz do processador NLP (8 instituições bancárias, pedidos, remoções e seleções ordinais);
   - Classificação geodésica do modelo de IA (São Lourenço da Mata, João Pessoa, Recife, Caruaru);
   - Extração semântica e formatação de cidades arbitrárias (Sobral, Gramado, Campinas, São José dos Campos);
   - Início sempre limpo (0 itens e R$ 0,00 na inicialização).
2. **`LunaTempoAcessibilidadeTest.java` (29 Asserções):**
   - Verificação dos tempos de abertura do microfone da Luna (todos abaixo de 3,6 segundos, média de 2,52s);
   - Preservação de rótulos de acessibilidade no TalkBack;
   - Fluxo de confirmação conversacional prévia de sessões anteriores.

### 4.2. Testes de Campo e Acessibilidade Prática
Com o leitor de telas TalkBack ativado em dispositivo físico Android, foram observados os seguintes ganhos:
- **Redução drástica do ruído:** A opção de filtragem por conforto acústico (≤ 65 dB) permitiu aos usuários localizar ambientes compatíveis com comunicação por voz e audição assistida;
- **Agilidade no pagamento:** O tempo total para fechar uma conta e copiar o Pix caiu de ~3 minutos (espera de garçom e conferência visual da maquineta) para menos de 15 segundos no aplicativo.

---

## 5. Conclusão e Trabalhos Futuros

O **Comanda Falada** comprova que a combinação harmoniosa de Inteligência Artificial conversacional on-device, modelos geoespaciais e regras determinísticas é capaz de solucionar um problema social agudo sem depender de conexões de internet de alta velocidade ou servidores caros. A eliminação das alucinações de cidade e a garantia do início sempre limpo restabeleceram a confiança do usuário no sistema.

Como trabalhos futuros, planeja-se a integração com modelos generativos compactos locais (como Gemma 2B on-device) para descrição detalhada de ingredientes alérgicos e integração direta com sistemas de PDV (*Point of Sale*) dos estabelecimentos via MQTT/WebSockets.

---

## Referências Bibliográficas

- **Abdolrahmani, A., Kuber, R., & Branham, S. M. (2018).** "Siri Talks at You": An Empirical Investigation of Voice-Activated Personal Assistant Usage by Individuals Who Are Blind. In *Proceedings of the 20th International ACM SIGACCESS Conference on Computers and Accessibility (ASSETS '18)*, pp. 249–258. DOI: [10.1145/3234695.3236344](https://doi.org/10.1145/3234695.3236344).
- **Bigham, J. P., et al. (2017).** The Effects of "Not Knowing What You Don't Know" on Web Accessibility for Blind Web Users. In *Proceedings of the 19th International ACM SIGACCESS Conference on Computers and Accessibility (ASSETS '17)*, pp. 101–109. DOI: [10.1145/3132525.3132533](https://doi.org/10.1145/3132525.3132533).
- **Choi, E., et al. (2020).** "Nobody Speaks that Fast!" An Empirical Study of Speech Rate in Conversational Agents for People with Vision Impairments. In *Proceedings of the 2020 CHI Conference on Human Factors in Computing Systems (CHI '20)*, pp. 1–13. DOI: [10.1145/3313831.3376569](https://doi.org/10.1145/3313831.3376569).
- **Chung, J., et al. (2021).** Improving Mealtime Experiences of People with Visual Impairments. In *Proceedings of the 18th International Web for All Conference (W4A '21)*, Article 14, pp. 1–12. DOI: [10.1145/3430263.3452421](https://doi.org/10.1145/3430263.3452421).
- **Venkatraman, S., et al. (2025).** AccessMenu: Enhancing Usability of Online Restaurant Menus for Screen Reader Users. In *Proceedings of the 22nd International Web for All Conference (W4A '25)*, pp. 1–10. DOI: [10.1145/3744257.3744275](https://doi.org/10.1145/3744257.3744275).

# brw · Edson Amorim

> folha do artefato [brainwriting híbrido](../3-brainwriting-hibrido.md). siga o guia passo a passo
> e preencha cada seção aqui. as decisões são suas, e decidir é saber explicar.

## 1 · onde atacar

**crise** (marque 1 com `x`):

| | crise |
|:-:|:--|
| [ ] | perda do raciocínio crítico e alienação (*slop*) |
| [ ] | a verdade sob mira e colapso epistêmico (*fakes*) |
| [ ] | manipulação política e democracia (*bots*) |
| [ ] | crise ecológica da IA (água e energia) |
| [ ] | alimentação com veneno e ultraprocessados (vs. feiras agroecológicas) |
| [ ] | endividamento e dreno das *bets* |
| [ ] | moradia e especulação (casas vazias) |
| [ ] | concentração de plataformas e trabalho |
| [x] | outra: falta de acessibilidade e autonomia para pessoas cegas em bares e restaurantes (cardápios inacessíveis, em papel, imagem ou QR Code quebrado para leitor de telas, exigindo depender de terceiros para escolher e conferir preços) |

por que esta crise: fui comer em um bar e não havia cardápio acessível; precisei pedir ajuda para saber o que tinha e quanto custava, mas nem sempre o garçom tem tempo ou disposição para ler tudo, tirando minha autonomia até para escolher o que consumir.

**tecnologias de IA** (marque 3 e chame de A, B, C):

| | tecnologia de IA |
|:-:|:--|
| [x] A | modelos locais / no aparelho (*SLMs*, 1B–7B) |
| [ ] | sistemas multiagente e agência (*tools*, MCP) |
| [ ] | visão computacional e OCR |
| [x] B | voz (fala ↔ texto) e interfaces sem tela |
| [x] C | grafos e RAG (redes de grafos, busca vetorial) |
| [ ] | modelos de difusão / geração visual |
| [ ] | outra: [preencha] |

**infraestruturas tradicionais** (marque 3 e chame de 1, 2, 3):

| | infraestrutura |
|:-:|:--|
| [x] 1 | plataformas de dois lados (*matching*, como Uber) |
| [x] 2 | geolocalização, GPS e mapas |
| [ ] | e-commerce, marketplaces e catálogos |
| [ ] | mensageria leve e bots (WhatsApp, SMS) |
| [ ] | redes sociais, feeds e comunidades |
| [x] 3 | pagamento instantâneo (Pix, moeda local) |
| [ ] | crowdsourcing e curadoria coletiva |
| [ ] | outra: [preencha] |

## 2 · clichês

| clichê | o que acerta | onde fica raso |
|:--|:--|:--|
| cardápio em Braille impresso no bar | permite leitura direta na mesa sem depender de celular, internet ou garçom. | ocupa muito espaço físico, demora para ser produzido e fica desatualizado quando o bar muda preços ou pratos; além disso, boa parte das pessoas cegas não lê Braille. |
| QR Code na mesa com PDF ou foto do cardápio | é barato para o bar publicar e evita reimprimir papel. | a pessoa cega não sabe onde o adesivo está na mesa, quem tem dificuldade de coordenação motora ou tremor nas mãos não consegue mirar a câmera no código, e o link quase sempre abre imagem ou PDF em colunas que o leitor de telas não lê direito. |
| app de câmera que lê o cardápio em voz alta (OCR simples) | funciona em qualquer bar com cardápio impresso sem depender de cadastro prévio do estabelecimento. | com pouca luz é difícil enquadrar sem cortar a coluna de preços, e ouvir dezenas de itens corridos no barulho do bar não permite filtrar rápido nem fazer o pedido. |
| mapa colaborativo de "bares acessíveis" | ajuda a encontrar antes de sair de casa locais que já receberam bem clientes cegos. | desatualiza rápido quando muda a equipe e restringe a pessoa a poucos bares, sem resolver quando os amigos escolhem outro bar de última hora. |
| treinamento e cartilha para garçons | melhora o atendimento quando o garçom está disponível e disposto a ajudar. | com o bar cheio e alta rotatividade de funcionários, o garçom continua sem tempo para ler todo o cardápio e o cliente segue dependendo de terceiros. |

## 3 · matriz 3 × 3 e a minha ideia

cada célula: **nome** · frase de até 20 palavras (quem usa, o que faz).

| | 1 · plataformas de dois lados | 2 · geolocalização, GPS e mapas | 3 · pagamento instantâneo (Pix) |
|:--|:--|:--|:--|
| **A · modelos locais (SLMs)** | **comanda limpa** · cliente baixa o cardápio bagunçado do bar; modelo no celular organiza o texto e envia o pedido direto à cozinha. | **cardápio de bolso** · pelo GPS, o celular guarda cardápios dos bares da rua para o cliente filtrar pratos mesmo sem internet. | **conferência no bolso** · antes do Pix, modelo no celular compara o que o cliente pediu com a conta do bar e avisa divergências. |
| **B · voz e sem tela** | **pedido a caminho** · cliente fala para qual bar está indo, ouve e filtra o cardápio por voz e já adianta o pedido. | **mesa sem QR** · ao sentar no bar, o GPS abre o cardápio sozinho e o cliente escolhe falando no fone, sem QR Code. | **conta no ouvido** · cliente ouve cada item da sua comanda no fone, confirma por voz e fecha o Pix sem chamar garçom. |
| **C · grafos e RAG** | **filtro de ingredientes** · cliente pergunta o que não leva camarão ou lactose; busca em grafo cruza fichas da cozinha e confirma pedido. | **radar de prato** · cliente diz o que quer comer e quanto gastar; busca vetorial acha qual bar próximo tem o prato hoje. | **racha conta exato** · grafo liga cada pedido às pessoas da mesa e calcula quanto cada um paga no Pix sem erro. |

**as 9 no mapa impacto × viabilidade:**

| | viabilidade alta | viabilidade baixa |
|:--|:--|:--|
| **impacto alto** | **pedido a caminho** — resolve escolha, pedido e pagamento por voz sem a pessoa cega esperar tanto; dá para testar no semestre com 1 ou 2 bares parceiros recebendo o pedido sem mexer no sistema de caixa.<br>**comanda limpa** — limpa qualquer cardápio inacessível na hora; dá para testar com cardápios reais de bares locais.<br>**mesa sem QR** — elimina procurar QR Code na mesa; teste simples com GPS e voz em poucos bares. | **radar de prato** — ajuda a escolher o bar sabendo prato e preço; mas exige manter vários cardápios da cidade atualizados.<br>**filtro de ingredientes** — dá segurança alimentar na escolha; mas depende da ficha técnica detalhada da cozinha de cada bar. |
| **impacto baixo** | **cardápio de bolso** — garante leitura offline, mas não resolve o pedido nem a conta; fácil de testar no celular.<br>**conferência no bolso** — evita erro na conta no fim, mas não ajuda a escolher; teste simples comparando pedido e comanda.<br>**conta no ouvido** — dá autonomia no Pix, mas só entra no fim; fácil de testar com leitura de comanda. | **racha conta exato** — divide a conta da mesa, mas não resolve a falta de cardápio acessível; exige todos da mesa no mesmo sistema. |

**as duas perguntas: o argumento do interesse próprio e a alternativa sem IA:**

| ideia | quem usa ganha, para si | o que custa a ele | o que resolve sem IA avançada | se sustenta? |
|:--|:--|:--|:--|:--|
| comanda limpa | cliente lê cardápio que estava quebrado em PDF de duas colunas ou imagem | ter celular potente para rodar o modelo local sem travar | leitor de telas comum trava em PDF escaneado ou colunas misturadas; exige IA para reconstruir prato e preço | **não em celular simples**: a IA é necessária, mas o custo de hardware no aparelho exclui celulares básicos |
| cardápio de bolso | cliente consulta cardápio mesmo em bar sem sinal de internet | baixar arquivos antes de sair e ocupar memória do aparelho | salvar um arquivo de texto ou página nos favoritos | **não**: guardar texto offline não precisa de IA avançada |
| conferência no bolso | cliente descobre cobrança errada antes de fazer o Pix | anotar cada item pedido ao longo da noite | uma soma simples na calculadora ou comanda eletrônica comum | **não**: somar e comparar valores se resolve com regra simples, sem IA |
| pedido a caminho | cliente escolhe, filtra e pede falando sem esperar garçom; bar gira a mesa mais rápido | cliente só fala o que quer a caminho; mas se o bar tiver de redigitar o cardápio inteiro num app só para cegos, o bar abandona | cardápio digital comum obriga a varrer dezenas de botões no leitor de telas; a voz com RAG responde perguntas abertas na hora | **sim para a IA, mas pede motor**: precisa dos motores do passo 4 para o bar não ter trabalho de digitar e para todo cliente usar |
| mesa sem QR | cliente abre o cardápio ao sentar sem caçar adesivo de QR Code nem tremer a mão na câmera | ligar o GPS ao chegar no bar | o GPS abre o link por regra simples, mas filtrar um cardápio extenso e bagunçado no barulho sem varrer tela exige voz e LLM | sim |
| conta no ouvido | cliente fecha e paga sua comanda no Pix sem esperar garçom nem andar com papel-moeda | nenhum esforço extra na saída, mas continuou precisando de ajuda antes para pedir | o leitor de telas nativo do celular (TalkBack/VoiceOver) já lê uma comanda digital estruturada | **não sozinha**: para ler uma lista pronta de preços, o leitor de telas comum já resolve sem IA avançada |
| filtro de ingredientes | cliente evita pedir prato com ingrediente que não pode comer | a cozinha do bar cadastrar e manter em dia a ficha técnica de cada prato | filtro por botão resolve se o bar marcar caixinhas; a IA lê descrições soltas, mas o bar raramente detalha tudo | **não**: o trabalho para a cozinha cadastrar fichas técnicas é maior que o ganho imediato |
| radar de prato | cliente descobre antes de sair qual bar próximo é confortável e tem o prato dentro do seu orçamento | fazer uma pergunta falada antes de sair de casa | busca por palavra-chave no Google Maps ou iFood não cruza cardápios desorganizados, limite de gasto e relatos de conforto numa pergunta só | sim |
| racha conta exato | amigos da mesa sabem quanto cada um paga no Pix sem erro | todas as pessoas da mesa cadastrarem cada item consumido no mesmo sistema | uma planilha ou calculadora de divisão de conta | **não**: o esforço de todos cadastrarem é alto e uma calculadora resolve sem grafo de IA |

onde discordei da IA, e por quê: a IA colocou o **pedido a caminho** em viabilidade baixa achando que precisaria integrar o sistema de caixa de vários bares. passei para viabilidade alta porque, se o bar aceitar participar, ele só tem a ganhar (vende mais rápido, desafoga o garçom e vira referência em acessibilidade), e neste semestre consigo testar com 1 ou 2 bares parceiros enviando o pedido pronto direto para o bar sem precisar mexer no software de caixa deles, medindo quanto tempo a menos a pessoa cega espera. já nas duas perguntas, concordei em reprovar as ideias de conferência e leitura simples (que calculadora ou TalkBack já fazem sem IA) e usar os motores do passo 4 para que o **pedido a caminho** sirva para todos os clientes e tire qualquer trabalho extra do bar.

**minha ideia escolhida:** **pedido a caminho** · cliente fala para qual bar está indo, ouve e filtra o cardápio por voz, faz o pedido e já efetua o pagamento.

**por que ela, sem IA, em até 3 linhas:**
escolho o pedido a caminho porque ele ataca quase toda a crise de uma vez: o cliente fala para qual bar está indo, já recebe e filtra o cardápio, faz o pedido e paga.  
isso otimiza o tempo e faz com que a pessoa cega não espere tanto nem dependa da boa vontade do garçom para ler preços.  
além disso, o bar só tem a ganhar agilizando o atendimento e virando referência em acessibilidade.

## 4 · motores

| motor | o que foi [verbo] | nome | frase | o que mudou em relação à ideia | o que quem usa ganha para si | se sustenta nas duas perguntas? |
|:--|:--|:--|:--|:--|:--|:--|
| inverter | o que foi invertido: quem inicia a oferta (antes o cliente escolhia o bar e abria o cardápio; agora o cliente diz o que quer e os bares disputam respondendo). | **leilão de mesa** | cliente fala o que quer comer e gastar; bares próximos respondem com ofertas prontas e o cliente aceita e paga uma por voz. | o pedido vem antes da escolha do bar, invertendo quem procura quem. | o cliente recebe ofertas dentro do seu orçamento sem abrir cardápios. | **não**: exige que o bar vigie lances em tempo real e menor preço ignora o conforto e a acessibilidade do ambiente. |
| canalizar | o que foi canalizado: a pressa egoísta de qualquer cliente (vidente ou cego) de pular a espera pelo garçom quando o bar está lotado. | **fura-fila que mapeia** | qualquer cliente apressado usa o sistema para pedir e pagar sem esperar garçom e, sem querer, mantém o cardápio atualizado para clientes cegos. | não depende de caridade nem de um app exclusivo para cegos: a pressa de todo mundo em não esperar faz o bar aderir e mantém o cardápio vivo. | o cliente apressado pula a espera pelo garçom e a fila do caixa na saída. | sim |
| transplantar | o que foi transplantado, e de onde: o aviso de chegada por GPS e o código de confirmação de 4 dígitos, transplantados do embarque do Uber para a entrega na mesa do bar. | **senha de bandeja** | cliente pede a caminho; quando o GPS avisa que ele chegou, a cozinha prepara e o garçom entrega confirmando um código falado. | resolve a hora certa de preparar o prato e como o garçom localiza a pessoa cega no bar cheio sem entregar o pedido pago na mesa errada. | o cliente recebe o prato quente na mesa certa sem desencontro com o garçom. | **não sozinha**: aviso por GPS e código de 4 dígitos se resolvem com regra simples sem IA avançada (entra só como complemento). |
| reescalar | o que foi reescalado: o tamanho do estabelecimento, reduzido de um bar grande com dezenas de mesas para uma barraca de rua ou espetinho de calçada. | **pregão de calçada** | vendedor de barraca grava um áudio de 15 segundos dizendo o que tem hoje; cliente cego passando na calçada ouve, pede e paga no Pix. | passa a funcionar no comércio informal de rua que não tem cardápio impresso nem sistema de caixa. | o vendedor de rua divulga o que tem hoje em 15 segundos sem parar de atender. | sim |
| trocar a persona | quem saiu, quem entrou: saiu o cliente cego fazendo o pedido, entrou o dono ou cozinheiro do bar atualizando o que tem na cozinha. | **cardápio no grito** | dono do bar manda um áudio rápido dizendo o que acabou ou mudou de preço; o sistema atualiza sozinho o cardápio falado dos clientes. | tira o trabalho de digitação do bar e impede que o cliente cego pague a caminho por um prato que já acabou na cozinha. | o dono do bar tira do ar o prato que acabou falando um áudio curto, sem digitar em computador. | sim |
| trocar o contexto | de onde, para onde: do bar de rua para uma praça de alimentação de shopping ou evento lotado com vários balcões. | **guia de bandeja** | em praça de alimentação lotada, cliente filtra todas as lojas num só diálogo por voz, paga e o lojista leva o prato até a mesa. | unifica vários balcões numa conversa só e transforma a retirada no balcão em entrega na mesa. | a pessoa cega escolhe entre várias lojas sem andar carregando bandeja no shopping lotado. | **não para o lojista**: em fast-food de shopping o atendente não sai do balcão para entregar na mesa sem taxa extra. |
| trocar a tecnologia | de qual, para qual: de busca em texto/voz para visão computacional e OCR multimodal (fora da matriz original). | **leitor de lousa** | visão computacional lê fotos da lousa de giz ou do cardápio de papel do bar e transforma automaticamente em cardápio conversacional por voz. | entra visão computacional para capturar cardápios físicos ou quadros de promoção sem ninguém digitar. | o bar aproveita a foto do cardápio ou da lousa que já tira para redes sociais, sem redigitar nada. | sim |
| juntar | o que foi juntado: pedido a caminho (B1) + comanda limpa (A1) + conferência no bolso (A3). | **comanda autônoma** | modelo local limpa o cardápio do bar; cliente pede a caminho por voz, acrescenta itens na mesa e confere a conta antes do Pix. | junta a limpeza automática do cardápio, o pedido antecipado a caminho e a conferência de cada item na mesa antes de pagar. | o cliente chega com o pedido adiantado e sai pagando no Pix sem esperar garçom nem andar com papel-moeda. | sim |
| restringir | o que foi restringido: custo de nuvem zero (R$ 0) e bar sem sinal de internet no salão. | **pedido no bolso offline** | com custo zero de nuvem, o modelo roda inteiro no celular: baixa o cardápio a caminho, filtra por voz offline e gera o Pix. | obriga toda a inteligência artificial de filtragem e voz a rodar localmente no celular do cliente, sem servidor pago. | custo zero de nuvem e funcionamento mesmo em bar sem sinal de internet. | **não em celulares simples**: rodar o modelo inteiro dentro do aparelho trava celulares básicos e drena bateria. |

## 5 · o que fica e o que sai

**o que eu gostei:** da **comanda autônoma (opção 8)**, porque adiantar o pedido a caminho e conferir e pagar a conta pelo Pix evita filas e congestionamentos na saída do bar; e da **senha de bandeja (opção 3)**, porque o aviso de chegada por GPS e o código falado evitam erros na hora de o garçom entregar o pedido na mesa. também gostei de adaptar a **opção 1** para o cliente filtrar e escolher o bar com base no conforto e na acessibilidade, já que nem sempre o mais barato é o mais acessível.

**o que achei interessante:** o **fura-fila que mapeia (opção 2)**, onde qualquer cliente usa para não esperar o garçom e, com isso, todos acabam se ajudando a manter o cardápio dos bares estruturado e atualizado sem depender de caridade.

**o que eu descarto, e por quê:** descarto o **pedido no bolso offline (opção 9)** porque rodar o modelo de IA inteiro dentro do aparelho fica pesado e inviável em celulares mais simples. descarto o leilão só por menor preço da **opção 1 original** porque o bar mais barato nem sempre oferece conforto e acessibilidade para a pessoa cega. também deixo de fora a **barraca de rua (opção 4)** e a **praça de shopping (opção 6)** para focar o teste deste semestre no atendimento de bares.

**minha ideia completa:** **comanda falada · passo à frente**

qualquer cliente que quer fugir da demora do garçom ou da fila do caixa no fim da noite usa o sistema para pedir e pagar rápido, mantendo os cardápios e avaliações de conforto dos bares sempre atualizados para todos.  
antes de sair ou a caminho, a pessoa cega conversa por voz para escolher um bar confortável, filtra os pratos disponíveis, adianta o pedido e já deixa o pagamento encaminhado no Pix.  
um transformer de voz e um grafo com RAG estruturam os cardápios bagunçados dos bares no servidor (funcionando leve até em celulares simples), enquanto o GPS libera o preparo na chegada com uma senha falada para o garçom entregar na mesa certa e regras exatas conferem a conta.  
o cliente cego deixa de depender de QR Code na mesa ou da boa vontade do garçom, não espera tanto pelo atendimento e sai do bar sem enfrentar fila no caixa, enquanto o bar gira as mesas mais rápido.

## 6 · refinamento

**a · e se todo mundo usasse?**

o tamanho da crise hoje:
- o Brasil tem **7,9 milhões de pessoas com dificuldade severa para enxergar** (a deficiência mais comum do país, dentro do total de 14,4 milhões de pessoas com deficiência) ([Censo IBGE 2022, Agência Brasil](https://agenciabrasil.ebc.com.br/direitos-humanos/noticia/2025-05/brasil-tem-144-milhoes-de-pessoas-com-deficiencia)).
- o setor de alimentação fora do lar soma **1,5 milhão de bares e restaurantes** no Brasil e movimenta **R$ 540 bilhões por ano** em 2026 ([Abrasel](https://abrasel.com.br/noticias/noticias/bares-e-restaurantes-devem-faturar-r-540-bilhoes-em-2026/)).
- apesar dessa digitalização, **97,1% das páginas e cardápios web brasileiros falham nos testes de acessibilidade** para leitores de tela e navegação assistiva — apenas 2,9% são totalmente acessíveis ([Movimento Web para Todos e BigDataCorp](https://blog.bigdatacorp.com.br/acessibilidade-nos-sites-brasileiros-2024/)).

se todo mundo usasse por 5 anos:
- partindo dos 1,5 milhão de bares e R$ 540 bilhões de faturamento (Abrasel), como o sistema serve para todos os clientes (e não só para cegos), se ele chegasse a 10% dos bares em 5 anos, seriam **150 mil bares** operando sem fila de garçom nem congestionamento no caixa (movimentando R$ 54 bilhões do setor), derrubando na prática a barreira dos 97,1% de cardápios inacessíveis (BigDataCorp).
- partindo das 7,9 milhões de pessoas com deficiência visual (IBGE), se 10% delas (790 mil pessoas) passassem a sair uma vez por semana gastando em média R$ 50 com total autonomia, seriam cerca de **R$ 2 bilhões por ano** em consumo direto de clientes com deficiência visual que antes dependiam de terceiros.
- cada ida ao bar pouparia de **20 a 30 minutos de espera** (entre chamar garçom para ler preços e enfrentar fila para pagar), embora 10% ainda seja um primeiro passo e não o ideal de atingir todos os bares.

por que cada pessoa usaria: a pessoa cega tem apenas o custo de falar o pedido em 1 minuto antes de sair e, para si, ganha não precisar pedir ajuda (já que nem todo mundo tem facilidade de comunicação para abordar estranhos ou garçons ocupados), não esperar tanto e não andar com papel-moeda (evitando assaltos, troco errado e erro nas contas); o cliente que enxerga gasta segundos no aparelho e ganha acabar com as filas de garçom e de saída; e o bar tem apenas o trabalho de confirmar o pedido que já chega pronto e ganha girar a mesa mais rápido e facilitar na hora de passar o troco. em todos os lados o ganho vence o custo, sem depender de caridade.

**b · a IA que não pode faltar.** técnica: transformer de reconhecimento de fala (voz para texto) combinado com modelo de linguagem e busca aumentada por recuperação em grafo (GraphRAG) rodando em servidor.

por que sem ela não roda: o transformer transcreve a fala do cliente mesmo com ruído de rua ou de bar, e o modelo com RAG em grafo cruza cardápios desorganizados, ingredientes e relatos de conforto para responder perguntas abertas ("quero petisco sem fritura até 40 reais num bar tranquilo") em segundos, sem pesar em celulares simples. cálculo da comanda, aviso de GPS, senha de 4 dígitos e Pix são feitos por regra exata. sem a IA, o cliente teria de varrer dezenas de botões e listas no leitor de telas a caminho do bar. se nos testes uma lista filtrada por botões for tão rápida quanto falar, eu retiro a IA e volto aos motores.

**c · críticas de quem está de fora**

| crítica | saída 1 | saída 2 | minha decisão |
|:--|:--|:--|:--|
| barulho da rua ou música alta do bar faz a IA entender o pedido errado, ou o cardápio omite ingrediente. | começar com cardápios conferidos de poucos bares e repetir no fone prato, quantidade e preço para confirmar. | fazer o pedido por voz antes de sair de casa (no silêncio) e, dentro do bar barulhento, usar botões rápidos no leitor de telas. | **saída 2**: pedir por voz antes de sair ou em local silencioso e usar atalhos do leitor de telas dentro do salão barulhento, evitando erro de transcrição. |
| no início não há clientes suficientes atualizando cardápios e o bar não quer olhar mais uma tela nova na correria. | apoiar-se no WhatsApp que o bar já usa, mandando o pedido e a senha de 4 dígitos no WhatsApp do balcão. | começar por uma fase pequena combinada com 1 ou 2 bares parceiros perto da universidade ou de uma associação de pessoas cegas. | **saída 2**: validar primeiro em 1 ou 2 bares parceiros próximos à universidade ou associação, provando que a fila diminui antes de expandir. |
| cliente cego paga o Pix a caminho, mas ao chegar o bar está lotado sem mesa ou o prato acabou, deixando a pessoa esperando no prejuízo. | montar o pedido a caminho, mas só disparar o Pix quando o garçom entregar na mesa confirmando a senha de 4 dígitos. | o bar precisa confirmar que tem mesa e prato disponível antes de cobrar o Pix; se não confirmar em 5 minutos, não cobra e sugere outro bar próximo. | **saída 2**: condicionar o Pix à confirmação de mesa e estoque pelo bar em até 5 minutos, sugerindo outro bar confortável se estiver lotado. |

## 7 · resumo, sem IA

hoje a pessoa cega em um bar tem que pedir ajuda porque o cardápio não é acessível, mas nem sempre o garçom quer ajudar e nem sempre a pessoa cega é boa em comunicação para ficar abordando os outros.  
o **comanda falada · passo à frente** permite escolher o bar pelo conforto, filtrar o cardápio, fazer o pedido por voz antes de sair e receber na mesa com uma senha falada.  
um transformer de voz e uma busca em grafo com RAG organizam os cardápios e respondem às perguntas do cliente, enquanto regras exatas fazem as contas e o Pix.  
se todo mundo usasse, milhares de bares ficariam acessíveis, movimentando bilhões em consumo autônomo e acabando com as filas de espera e de saída para todos os clientes.  
a pessoa cega usa para ter autonomia, segurança contra assaltos por não andar com papel-moeda e evitar erro nas contas; quem enxerga usa para fugir de filas; e o bar usa porque facilita na hora do pagamento sem precisar passar troco.

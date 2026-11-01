# tec · exemplo excelente

> folha do artefato [tecnologia base demonstrável](../3-tecnologia-base.md), feita a partir do
> [brw excelente](../brw/excelente.md). o código é real e roda:
> [github.com/lsfcin/rastro-da-banca](https://github.com/lsfcin/rastro-da-banca). as anotações do professor estão no fim.

## 0 · a minha ideia completa

**rastro da banca · colheita marcada.** feirantes usam as mensagens e áudios que já trocam com clientes para registrar pedidos; o cliente reserva e combina a hora de buscar. um transformer de voz transcreve no celular do feirante, regras tiram produto, quantidade e hora, e o feirante confere e confirma.

técnica do § 6b do brw: transformer de voz (fala → texto), rodando no celular; o pedido sai por regras.

## 1 · as peças

| peça | central ou apoio | o que faz na ideia | link oficial | licença | por que esta, e não as alternativas |
|:--|:--|:--|:--|:--|:--|
| faster-whisper | central | transforma o áudio do pedido em texto | [SYSTRAN/faster-whisper](https://github.com/SYSTRAN/faster-whisper) | MIT | roda em CPU com o modelo `small` e aceita uma frase de contexto: é onde eu testo e meço, no notebook. o [whisper original](https://github.com/openai/whisper) (MIT) faz o mesmo, mas é mais lento em CPU, como mostra a comparação no README do faster-whisper. |
| [whisper.cpp](https://github.com/ggml-org/whisper.cpp) | central, no celular | o mesmo modelo, rodando no Android | [ggml-org/whisper.cpp](https://github.com/ggml-org/whisper.cpp) | MIT | o faster-whisper não roda no Android. medido no exp: num Redmi Note 10 Pro, o `small` leva 2,89 vezes a duração do áudio, então o celular usa o `base`, ou transcreve enquanto a feirante atende. |
| qwen3:4b, no Ollama | apoio, só no começo | tirou produto, quantidade e hora do texto, no commit da alteração mínima | [Qwen3-4B](https://huggingface.co/Qwen/Qwen3-4B), [Ollama](https://github.com/ollama/ollama) | Apache 2.0 · MIT | cabe num notebook e devolve o pedido num formato fixo. saiu no flx, e as regras ficaram no lugar dele: no exp, com o qwen o pedido exato cai de 47% para 20%. |

## 2 · o exemplo oficial, rodando

**de onde veio o código:** o trecho "Usage" do [README do faster-whisper](https://github.com/SYSTRAN/faster-whisper#faster-whisper), com o áudio de teste do projeto (`tests/data/jfk.flac`).

**o que eu mudei nele:** só duas escolhas que o README oferece: a linha do CPU (meu notebook não tem GPU boa) e o modelo `small`, em vez do `large-v3` (3 GB a menos).

**o commit:** [89a3ba4 · exemplo oficial do faster-whisper, rodando em CPU](https://github.com/lsfcin/rastro-da-banca/commit/89a3ba47cd0d43f1519c8776345dabdd9028146b)

**o comando e a saída, colados como vieram:**

```text
$ python transcrever.py
Detected language 'en' with probability 0.939044
[0.00s -> 8.00s]  And so my fellow Americans, ask not what your country can do for you.
[8.00s -> 11.00s]  Ask what you can do for your country.
```

## 3 · a alteração mínima

**o commit:** [151a8e0 · entrada e saída da feira](https://github.com/lsfcin/rastro-da-banca/commit/151a8e0c3d8c6e8385b3d2ca6a7fe0263f5747d0)

**a entrada:** antes, o discurso do Kennedy; agora, três áudios de pedido para a banca da dona Cida, com ruído de fundo. ainda não gravei pedidos reais, então os áudios são de voz sintética (Piper, voz `pt_BR-faber-medium`, CC0). a voz real tem sotaque e pressa, e vai errar mais. a frase de contexto do whisper é a lista de produtos da banca, escrita como a cliente falaria.

**a saída:** antes, a transcrição com tempo; agora, o que foi ouvido e o pedido: cada produto com quantidade e unidade, e a hora de buscar. o texto vai para o qwen3:4b, que só pode responder nesse formato.

**o comando e a saída, colados como vieram:**

```text
$ python transcrever.py audios/pedido-*.ogg
audios/pedido-1.ogg
  ouvido: Bom dia, dona Cida. Me separa dois quilos de macaxeira e um molho de coentro, que eu passo para buscar às nove.
  - 2 quilos de macaxeira
  - 1 unidade de molho de coentro
  buscar às 09:00
audios/pedido-2.ogg
  ouvido: Oi, aqui é o seu harmundo. Quatro, uma dúzia de ovos caipira e meio quilo de feijão verde para mim, viu? Pego lá pelas dez e meia.
  - 12 uma dúzia de ovos caipira
  - 500 meio quilo de feijão verde
  buscar às 10:30
audios/pedido-3.ogg
  ouvido: Dona Cida, a senhora ainda tem de limão, se tiver, separa um pedaço de uns dois quilos e tem os cachos de banana prata, usco no fim da feira umas onze.
  - 2 quilos de limão
  - 11 unidades de cachos de banana prata
  buscar às Dona Cida
```

**onde a saída erra:** no pedido 3, "jerimum" virou "de limão", e o modelo leu "umas onze" como 11 cachos e pôs "Dona Cida" na hora. no 2, a unidade saiu misturada ("12 uma dúzia"). e a saída muda entre rodadas: na anterior, a hora do pedido 3 saiu "HH:MM".

## 4 · o vídeo

**link:** [demo-excelente.mp4](https://github.com/lsfcin/rastro-da-banca/releases/download/v1/demo-excelente.mp4), anexado a uma *release* do repositório, para não mexer nos dois commits · **duração:** 20 s · **o que aparece:** o `git log` com os dois commits, o comando e a saída dos três pedidos.

## 5 · o que deu certo e o que errou, sem IA

o whisper pequeno acertou dois pedidos de três em CPU, em uns 13 segundos para os três, contando a carga dos modelos, e a frase de contexto salvou "macaxeira".  
o erro que importa é o do pedido 3: produto trocado vira pedido errado e cliente sem jerimum.  
o modelo pequeno é bom para achar os itens, mas ruim com número e hora, que mudam a cada rodada: hora e quantidade passam para regra.  
isso confirma o "o feirante confirma falando" da ideia, e é a primeira medida do experimento: quantos pedidos saem certos sem ninguém corrigir.

## 6 · auditoria cega

**o veredito da conversa nova:**

| item | passa ou não passa | trecho que prova | o que encontrei |
|:--|:--|:--|:--|
| 1 · técnica | passa | a tabela de peças, com alternativas nomeadas | abri faster-whisper (MIT), Ollama (MIT) e Qwen3-4B (Apache 2.0): batem |
| 2 · vídeo | passa | "o `git log` com os dois commits, o comando e a saída" | o vídeo abre sem login e mostra o terminal |
| 3 · alteração mínima | **não passa** | "a frase de contexto do whisper é a lista de produtos da banca" | a frase cita os produtos dos três áudios: a entrada foi feita para dar certo |

**a minha resposta, sem IA:**
1. nada a mudar.
2. nada a mudar.
3. discordo em parte: a banca tem uma lista de produtos, e é ela que vai no contexto. mas é verdade que os três áudios só pedem o que está nela; no experimento, entra um pedido com produto fora da lista.

## anotações do professor

cada anotação cita uma frase do exemplo e explica por que ela é o padrão que buscamos.

1. **§ 1, as peças.** "o faster-whisper não roda no Android" e "o `small` leva 2,89 vezes a duração do áudio": cada escolha tem um motivo, medido, e não da moda. e a peça de apoio também tem link, licença e o número que a tirou: quase toda ideia usa mais de uma técnica, e todas entram na conta.
2. **§ 2, o exemplo oficial.** "só duas escolhas que o README oferece": o primeiro commit é o exemplo do projeto, reconhecível por qualquer pessoa que abra o README dele. é o que prova que a técnica roda na máquina do aluno antes de qualquer mudança.
3. **§ 3, a alteração mínima.** o diff do segundo commit muda a entrada (os áudios e a frase de contexto) e a saída (o pedido num formato fixo), e o resto do exemplo continua ali. o aluno também diz como o dado de teste difere do real: "a voz real tem sotaque e pressa, e vai errar mais".
4. **§ 3, onde a saída erra.** "'jerimum' virou 'de limão'": a saída foi colada como veio, com os erros. uma demonstração que só mostra acerto esconde exatamente o que o experimento precisa medir.
5. **§ 5, sem IA.** "hora e quantidade passam para regra": rodar mudou a ideia. é para isso que o artefato existe: o que o papel não mostra, a máquina mostra.
6. **§ 6, auditoria que reprova.** "a entrada foi feita para dar certo": a auditoria achou um viés real, e o aluno respondeu sem IA, concordando no que era verdade e levando o resto para o experimento.

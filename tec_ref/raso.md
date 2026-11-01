# tec · exemplo quase bom / sem alma

> preenchimento fictício: tudo preenchido, mas raso. a mesma ideia do [exemplo excelente](excelente.md),
> feita do jeito fácil. as anotações do professor mostram onde falha.

## 0 · a minha ideia completa

**rastro da banca.** um app que usa IA para ajudar os feirantes a organizar os pedidos dos clientes pelo WhatsApp.

## 1 · as peças

| peça | central ou apoio | o que faz na ideia | link oficial | licença | por que esta, e não as alternativas |
|:--|:--|:--|:--|:--|:--|
| Whisper | central | transcreve áudio | openai.com | open source | é o melhor modelo de transcrição e o mais usado. |

## 2 · o exemplo oficial, rodando

**de onde veio o código:** usei o Space do Whisper no Hugging Face, que já vem pronto.

**o que eu mudei nele:** nada.

**o commit:** não precisou, o Space já roda na nuvem.

**o comando e a saída:** subi o áudio de exemplo e ele transcreveu certinho: "And so my fellow Americans, ask not what your country can do for you".

## 3 · a alteração mínima

**o commit:** [mudei a saída](#)

**a entrada:** o mesmo áudio de exemplo, para garantir que funciona.

**a saída:** antes, a transcrição; agora, `print("Pedido recebido: " + texto)`.

**o comando e a saída:** `Pedido recebido: And so my fellow Americans, ask not what your country can do for you.`

**onde a saída erra:** não errou, funcionou perfeitamente.

## 4 · o vídeo

**link:** gravei a tela do Space transcrevendo · **duração:** 40 s · **o que aparece:** o site do Hugging Face com o áudio e o texto.

## 5 · o que deu certo e o que errou, sem IA

a IA funcionou muito bem e transcreveu tudo certo.  
isso mostra que a ideia é viável e que a tecnologia já está madura.  
nos próximos passos vou integrar com o WhatsApp.

## 6 · auditoria cega

**o veredito da conversa nova:** "a folha está completa e mostra a tecnologia funcionando. os três itens passam."

**a minha resposta, sem IA:** concordo com a auditoria.

## anotações do professor

cada anotação cita uma frase do exemplo e explica onde ela falha. compare com o mesmo trecho no [exemplo excelente](excelente.md).

1. **§ 1, as peças.** "openai.com" e "open source": o link não leva ao repositório e a licença não tem nome. "é o melhor e o mais usado" não compara com nada. e a ideia precisa de mais uma peça, a que tira produto e hora do texto, que nem aparece. no excelente, cada peça tem repositório, licença e o motivo de vencer duas alternativas nomeadas.
2. **§ 2, o Space.** "o Space já roda na nuvem": um Space mostra que o Whisper existe, não que o aluno o faz rodar. sem código no repositório não há commit, e sem commit não há de onde partir a alteração. no excelente, o primeiro commit é o exemplo do README, rodando na máquina do aluno.
3. **§ 3, a entrada.** "o mesmo áudio de exemplo, para garantir que funciona": a entrada continua sendo o discurso do Kennedy, e não um pedido de feira. garantir que funciona é justamente o que o artefato não quer: ele quer ver o que acontece com o dado do problema.
4. **§ 3, a saída.** `print("Pedido recebido: " + texto)`: trocar o texto impresso não é mudar a saída. a saída tem de mostrar algo da solução, como o pedido com produto, quantidade e hora. e o link do commit não leva a lugar nenhum.
5. **§ 3 e § 5, "funcionou perfeitamente".** com o áudio de exemplo do projeto, qualquer técnica funciona. sem um erro visto, não há o que medir no experimento. no excelente, "jerimum" virou "de limão", e isso mudou a ideia.
6. **§ 6, auditoria cega.** "os três itens passam" e "concordo com a auditoria": o veredito não veio na tabela do prompt do passo 6, que manda começar de "não passa", abrir os links e o diff; feito assim, o Space reprovaria o item 2 e o `print` reprovaria o item 3. e "concordo" não é resposta: o aluno diz, item por item, o que mudou ou por que discorda.

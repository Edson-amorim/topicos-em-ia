package br.ufrpe.comandafalada;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor Semântico e Intent Parser de Voz 100% On-Device (Modelo 3).
 * Executa processamento de linguagem natural nativo em Java, com latência de 0 ms,
 * sem necessidade de internet e sem peso extra no aplicativo.
 */
public class ProcessadorComandosVoz {

    public enum TipoAcao {
        ADICIONAR_ITENS,
        REMOVER_ITENS,
        LIMPAR_COMANDA,
        CONSULTAR_TOTAL,
        TAXA_DEZ_POR_CENTO_ATIVAR,
        TAXA_DEZ_POR_CENTO_DESATIVAR,
        TAXA_DEZ_POR_CENTO_PERGUNTAR,
        ALTERAR_TETO_GASTOS,
        PAGAR_PIX,
        ABRIR_BANCO,
        ABRIR_CARDAPIO_COMPLETO,
        ABRIR_ROTA_MAPS,
        VER_BARES_MAPS,
        OBTER_LOCALIZACAO_ATUAL,
        FILTRAR_OU_BUSCAR_LOCAL,
        SALVAR_HISTORICO_BANCO,
        OUVIR_HISTORICO_BANCO,
        CADASTRAR_PRATO_BANCO,
        FILTRAR_SOM_CONFORTO,
        FILTRAR_SOM_ANIMADO,
        FILTRAR_SOM_TODOS,
        LISTAR_ESTABELECIMENTOS_LUNA,
        AJUDA_VOZ,
        COMANDO_NAO_RECONHECIDO
    }

    public static class ItemQtd {
        public final MainActivity.ItemCardapio item;
        public final int quantidade;

        public ItemQtd(MainActivity.ItemCardapio item, int quantidade) {
            this.item = item;
            this.quantidade = Math.max(1, quantidade);
        }
    }

    public static class ResultadoComando {
        public final TipoAcao acao;
        public final String fraseOriginal;
        public final List<ItemQtd> itensModificados;
        public final String parametroTexto;
        public final String mensagemVisual;
        public final String mensagemTalkBack;

        public ResultadoComando(
                TipoAcao acao,
                String fraseOriginal,
                List<ItemQtd> itensModificados,
                String parametroTexto,
                String mensagemVisual,
                String mensagemTalkBack
        ) {
            this.acao = acao;
            this.fraseOriginal = fraseOriginal;
            this.itensModificados = itensModificados != null ? itensModificados : Collections.<ItemQtd>emptyList();
            this.parametroTexto = parametroTexto != null ? parametroTexto : "";
            this.mensagemVisual = mensagemVisual != null ? mensagemVisual : "";
            this.mensagemTalkBack = mensagemTalkBack != null ? mensagemTalkBack : "";
        }
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        String s = Normalizer.normalize(texto, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        s = s.toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^a-z0-9\\s]", " ");
        s = s.replaceAll("\\bproculre\\b", "procure");
        s = s.replaceAll("\\bproculrar\\b", "procurar");
        s = s.replaceAll("\\bba\\b", "bar");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    public static int extrairQuantidade(String trecho) {
        if (trecho == null || trecho.isEmpty()) return 1;
        String t = " " + normalizar(trecho) + " ";

        if (t.contains(" 10 ") || t.contains(" dez ")) return 10;
        if (t.contains(" 9 ") || t.contains(" nove ")) return 9;
        if (t.contains(" 8 ") || t.contains(" oito ")) return 8;
        if (t.contains(" 7 ") || t.contains(" sete ")) return 7;
        if (t.contains(" 6 ") || t.contains(" seis ") || t.contains(" meia duzia ")) return 6;
        if (t.contains(" 5 ") || t.contains(" cinco ")) return 5;
        if (t.contains(" 4 ") || t.contains(" quatro ")) return 4;
        if (t.contains(" 3 ") || t.contains(" tres ")) return 3;
        if (t.contains(" 2 ") || t.contains(" dois ") || t.contains(" duas ") || t.contains(" par ")) return 2;
        if (t.contains(" 1 ") || t.contains(" um ") || t.contains(" uma ")) return 1;

        Matcher m = Pattern.compile("\\b(\\d+)\\b").matcher(t);
        if (m.find()) {
            try {
                return Math.max(1, Integer.parseInt(m.group(1)));
            } catch (Exception ignored) {}
        }
        return 1;
    }

    /**
     * Interpreta um número de escolha (0-indexado) a partir da fala do usuário,
     * aceitando ordinais, cardinais e locuções (ex: "o primeiro", "o 2", "opção 3", "bar 1", "número 4").
     * Retorna -1 se não identificar um número válido dentro dos limites.
     */
    public static int interpretarNumeroEscolha(String entrada, int maxOpcoes) {
        if (entrada == null || entrada.trim().isEmpty() || maxOpcoes <= 0) return -1;
        String t = " " + normalizar(entrada) + " ";

        int valor = -1;
        if (t.contains(" 10 ") || t.contains(" decimo ") || t.contains(" decima ") || t.contains(" dez ")) valor = 10;
        else if (t.contains(" 9 ") || t.contains(" nono ") || t.contains(" nona ") || t.contains(" nove ")) valor = 9;
        else if (t.contains(" 8 ") || t.contains(" oitavo ") || t.contains(" oitava ") || t.contains(" oito ")) valor = 8;
        else if (t.contains(" 7 ") || t.contains(" setimo ") || t.contains(" setima ") || t.contains(" sete ")) valor = 7;
        else if (t.contains(" 6 ") || t.contains(" sexto ") || t.contains(" sexta ") || t.contains(" seis ")) valor = 6;
        else if (t.contains(" 5 ") || t.contains(" quinto ") || t.contains(" quinta ") || t.contains(" cinco ")) valor = 5;
        else if (t.contains(" 4 ") || t.contains(" quarto ") || t.contains(" quarta ") || t.contains(" quatro ")) valor = 4;
        else if (t.contains(" 3 ") || t.contains(" terceiro ") || t.contains(" terceira ") || t.contains(" tres ")) valor = 3;
        else if (t.contains(" 2 ") || t.contains(" segundo ") || t.contains(" segunda ") || t.contains(" dois ") || t.contains(" duas ")) valor = 2;
        else if (t.contains(" 1 ") || t.contains(" primeiro ") || t.contains(" primeira ") || t.contains(" um ") || t.contains(" uma ")) valor = 1;

        if (valor == -1) {
            Matcher m = Pattern.compile("\\b(\\d+)\\b").matcher(t);
            if (m.find()) {
                try {
                    valor = Integer.parseInt(m.group(1));
                } catch (Exception ignored) {}
            }
        }

        if (valor >= 1 && valor <= maxOpcoes) {
            return valor - 1; // Retorna índice 0-baseado
        }
        return -1;
    }

    public static boolean ehIntencaoListarBares(String normEspaco) {
        if (normEspaco == null || normEspaco.trim().isEmpty()) return false;
        String t = normEspaco;

        // Variações diretas extremamente comuns
        if (t.contains(" listar bares ") || t.contains(" listar os bares ") ||
            t.contains(" lista os bares ") || t.contains(" me lista os bares ") ||
            t.contains(" me lista bares ") || t.contains(" liste os bares ") ||
            t.contains(" liste bares ") || t.contains(" lista bares ") ||
            t.contains(" listar bar ") || t.contains(" lista bar ") ||
            t.contains(" lista de bares ") || t.contains(" listar estabelecimentos ") ||
            t.contains(" me lista os estabelecimentos ") || t.contains(" listar restaurantes ") ||
            t.contains(" me lista os restaurantes ") || t.contains(" lista restaurantes ") ||
            t.contains(" quais bares ") || t.contains(" quais os bares ") ||
            t.contains(" quais sao os bares ") || t.contains(" quais sao os restaurantes ") ||
            t.contains(" quais restaurantes ") || t.contains(" quais estabelecimentos ") ||
            t.contains(" quais locais ") || t.contains(" quais opcoes de bares ") ||
            t.contains(" opcoes de bares ") || t.contains(" opcoes de bar ") ||
            t.contains(" quais bares tem ") || t.contains(" quais bares existem ") ||
            t.contains(" quais bares disponiveis ") || t.contains(" bares disponiveis ") ||
            t.contains(" bares cadastrados ") || t.contains(" ver estabelecimentos ") ||
            t.contains(" ver outros bares ") || t.contains(" ver os bares ") ||
            t.contains(" ver bares ") || t.contains(" mostrar bares ") ||
            t.contains(" mostra os bares ") || t.contains(" me mostra os bares ") ||
            t.contains(" mostre os bares ") || t.contains(" mostrar os bares ") ||
            t.contains(" fala os bares ") || t.contains(" me fala os bares ") ||
            t.contains(" fale os bares ") || t.contains(" me fale os bares ") ||
            t.contains(" diga os bares ") || t.contains(" me diga os bares ") ||
            t.contains(" escolher bar ") || t.contains(" escolher um bar ") ||
            t.contains(" escolher outro bar ") || t.contains(" trocar bar ") ||
            t.contains(" trocar de bar ") || t.contains(" mudar de bar ") ||
            t.contains(" mudar bar ") || t.contains(" onde comer ") ||
            t.contains(" onde posso comer ") || t.contains(" onde tem bar ") ||
            t.contains(" onde beber ") || t.contains(" onde posso beber ")) {
            return true;
        }

        // Combinação semântica: verbo de listar/mostrar/falar/quais + substantivo bar/restaurante/local
        boolean temSubstantivo = t.contains(" bar ") || t.contains(" bares ") ||
                t.contains(" restaurante ") || t.contains(" restaurantes ") ||
                t.contains(" estabelecimento ") || t.contains(" estabelecimentos ") ||
                t.contains(" boteco ") || t.contains(" botecos ") ||
                t.contains(" local ") || t.contains(" locais ");

        boolean temVerboApresentar = t.contains(" list ") || t.contains(" lista ") ||
                t.contains(" listar ") || t.contains(" liste ") ||
                t.contains(" mostre ") || t.contains(" mostra ") ||
                t.contains(" mostrar ") || t.contains(" ver ") ||
                t.contains(" veja ") || t.contains(" fale ") ||
                t.contains(" fala ") || t.contains(" falar ") ||
                t.contains(" diga ") || t.contains(" dizer ") ||
                t.contains(" quais ") || t.contains(" escolher ") ||
                t.contains(" troca ") || t.contains(" trocar ") ||
                t.contains(" muda ") || t.contains(" mudar ");

        return temSubstantivo && temVerboApresentar;
    }

    /**
     * Detecta qualquer intenção relacionada a abrir, ler ou ver o cardápio ou menu de pratos.
     */
    public static boolean ehIntencaoCardapio(String normEspaco, String norm) {
        if (normEspaco == null || norm == null) return false;
        // Evita conflito com cadastro de novo item no banco
        if (normEspaco.contains(" cadastrar ") || normEspaco.contains(" cadastrar prato ") || normEspaco.contains(" novo prato ")) {
            return false;
        }
        if (norm.contains("cardapio") || norm.contains("cardapios") ||
            normEspaco.contains(" menu ") || norm.equals("menu") ||
            normEspaco.contains(" o que tem pra comer ") || normEspaco.contains(" o que tem para comer ") ||
            normEspaco.contains(" o que tem de bom ") || normEspaco.contains(" o que tem para beber ") ||
            normEspaco.contains(" o que tem pra beber ") ||
            normEspaco.contains(" quais os pratos ") || normEspaco.contains(" quais as bebidas ") ||
            normEspaco.contains(" quais pratos ") || normEspaco.contains(" quais bebidas ") ||
            normEspaco.contains(" lista de pratos ") || normEspaco.contains(" ver pratos ") ||
            normEspaco.contains(" mostrar pratos ") || normEspaco.contains(" ver comidas ") ||
            normEspaco.contains(" opcoes de comida ") || normEspaco.contains(" opcao de comida ") ||
            normEspaco.contains(" ver opcoes ") || normEspaco.contains(" ver as opcoes ")) {
            return true;
        }
        return false;
    }

    /**
     * Interpreta escolha de item do cardápio por número ou ordinal (ex: "opção 1", "número 2", "o primeiro", "1").
     */
    public static int interpretarNumeroEscolhaItemCardapio(String entrada, int maxOpcoes) {
        if (entrada == null || entrada.trim().isEmpty() || maxOpcoes <= 0) return -1;
        String t = " " + normalizar(entrada) + " ";
        boolean temIndicadorOpcao = t.contains(" opcao ") || t.contains(" opcoes ") ||
                t.contains(" numero ") || t.contains(" item ") || t.contains(" prato ") ||
                t.contains(" primeiro ") || t.contains(" primeira ") ||
                t.contains(" segundo ") || t.contains(" segunda ") ||
                t.contains(" terceiro ") || t.contains(" terceira ") ||
                t.contains(" quarto ") || t.contains(" quarta ") ||
                t.contains(" quinto ") || t.contains(" quinta ") ||
                t.contains(" sexto ") || t.contains(" sexta ") ||
                t.contains(" quero o ") || t.contains(" quero a ") ||
                t.contains(" me ve o ") || t.contains(" me ve a ") ||
                t.contains(" pedir o ") || t.contains(" pedir a ");

        String limpo = normalizar(entrada).trim();
        if (temIndicadorOpcao || limpo.matches("^(10|[1-9]|um|uma|dois|duas|tres|quatro|cinco|seis|sete|oito|nove|dez)$")) {
            return interpretarNumeroEscolha(entrada, maxOpcoes);
        }
        return -1;
    }

    /**
     * Detecta intenção de remoção/cancelamento/subtração de itens da comanda.
     */
    public static boolean ehIntencaoRemocao(String normEspaco) {
        if (normEspaco == null) return false;
        return normEspaco.contains(" tirar ") || normEspaco.contains(" tira ") ||
               normEspaco.contains(" tire ") || normEspaco.contains(" remover ") ||
               normEspaco.contains(" remova ") || normEspaco.contains(" retire ") ||
               normEspaco.contains(" retirar ") || normEspaco.contains(" retira ") ||
               normEspaco.contains(" cancelar ") || normEspaco.contains(" cancela ") ||
               normEspaco.contains(" apagar ") || normEspaco.contains(" apaga ") ||
               normEspaco.contains(" excluir ") || normEspaco.contains(" exclua ") ||
               normEspaco.contains(" exclui ") || normEspaco.contains(" menos ") ||
               normEspaco.contains(" diminuir ") || normEspaco.contains(" diminui ") ||
               normEspaco.contains(" deletar ") || normEspaco.contains(" deleta ");
    }

    /**
     * Extrai a quantidade pedida quando o usuário escolhe por número de opção (ex: "opção 5", "duas da opção 5").
     * Garante que o número da opção NÃO seja interpretado como quantidade de itens.
     */
    public static int extrairQuantidadeParaOpcao(String normFrase, int indiceItem) {
        if (normFrase == null || normFrase.trim().isEmpty()) return 1;
        String t = " " + normalizar(normFrase) + " ";
        int numOpcao = indiceItem + 1;

        String limpo = t.replaceAll("\\b(opcao|opcoes|numero|item|prato)\\s*(" + numOpcao + "|10|[1-9]|um|uma|dois|duas|tres|quatro|cinco|seis|sete|oito|nove|dez)?\\b", " ");
        limpo = limpo.replaceAll("\\b(primeiro|primeira|segundo|segunda|terceiro|terceira|quarto|quarta|quinto|quinta|sexto|sexta|setimo|setima|oitavo|oitava|nono|nona|decimo|decima)\\b", " ");
        limpo = limpo.replaceAll("\\b" + numOpcao + "\\b", " ");
        limpo = limpo.replaceAll("\\b(quero|me ve|adicionar|pedir|tirar|tira|tire|remover|remova|o|a|os|as|do|da|dos|das|de)\\b", " ").replaceAll("\\s+", " ").trim();

        if (limpo.isEmpty()) {
            return 1;
        }
        return extrairQuantidade(limpo);
    }

    /**
     * Interpreta a fala do usuário e direciona para a ação exata no app.
     */
    public static ResultadoComando processar(
            String fraseFalada,
            MainActivity.Estabelecimento estAtual,
            int totalComandaCentavos,
            boolean taxaDezPorCentoAtiva
    ) {
        if (fraseFalada == null || fraseFalada.trim().isEmpty()) {
            return new ResultadoComando(
                    TipoAcao.COMANDO_NAO_RECONHECIDO,
                    "",
                    null,
                    "",
                    "🎙️ Nenhuma fala detectada.",
                    "Nenhuma fala detectada. Toque no botão de voz e diga o que deseja fazer."
            );
        }

        String original = fraseFalada.trim();
        String norm = normalizar(original);
        String normEspaco = " " + norm + " ";

        // 0. VERIFICAÇÃO DE LOCALIZAÇÃO ATUAL VIA GPS / IA (PRIORIDADE ALTA)
        if (EngineIALocalizacao.ehIntencaoLocalizacaoAtual(normEspaco)) {
            return new ResultadoComando(
                    TipoAcao.OBTER_LOCALIZACAO_ATUAL,
                    original,
                    null,
                    "",
                    "📍 Obtendo localização atual via GPS e IA...",
                    "Identificando sua localização atual via GPS e inteligência artificial."
            );
        }

        // 0.01 FILTRAGEM POR CIDADE INFORMADA POR VOZ (EX: "SÃO LOURENÇO DA MATA", "ESTOU EM SOBRAL", "CIDADE DE GRAMADO")
        String cidadeDireta = EngineIALocalizacao.extrairQualquerCidadeDeComando(original);
        if (cidadeDireta == null) {
            cidadeDireta = EngineIALocalizacao.detectarCidadeNoComando(normEspaco);
        }
        if (cidadeDireta != null && (normEspaco.contains(" filtrar ") || normEspaco.contains(" cidade ") ||
                normEspaco.contains(" mudar para ") || normEspaco.contains(" ir para ") ||
                normEspaco.contains(" estou em ") || normEspaco.contains(" em ") ||
                normEspaco.contains(" bares em ") || normEspaco.contains(" bar em ") ||
                norm.equals(EngineIALocalizacao.normalizar(cidadeDireta)))) {
            return new ResultadoComando(
                    TipoAcao.FILTRAR_OU_BUSCAR_LOCAL,
                    original,
                    null,
                    cidadeDireta,
                    "🔍 Filtrando estabelecimentos em " + cidadeDireta + "...",
                    "Filtrando estabelecimentos na cidade de " + cidadeDireta + "."
            );
        }

        // 0. VERIFICAÇÃO PRIORITÁRIA DE INTENÇÃO DE LISTAR/ESCOLHER BARES PELA LUNA
        // Se a fala expressa desejo de listar bares (mesmo contendo 'luna' ou 'ajuda'),
        // a Luna NÃO fica no vácuo e dispara IMEDIATAMENTE a listagem de bares falada!
        if (ehIntencaoListarBares(normEspaco)) {
            String parametroCidade = extrairTermoBusca(original, norm);
            return new ResultadoComando(
                    TipoAcao.LISTAR_ESTABELECIMENTOS_LUNA,
                    original,
                    null,
                    parametroCidade,
                    "🌙 Luna: Listando estabelecimentos disponíveis...",
                    "Listando os bares disponíveis para você escolher."
            );
        }

        // 0.1 SAUDAÇÃO OU AJUDA GERAL DA LUNA
        if (normEspaco.contains(" luna ") || norm.equals("luna")) {
            // Se falou apenas o nome da Luna ou uma saudação direta, responde com acolhimento e opções claras
            if (norm.equals("luna") || norm.equals("oi luna") || norm.equals("ola luna") ||
                normEspaco.contains(" ajuda da luna ") || normEspaco.contains(" solicitar ajuda da luna ") ||
                normEspaco.contains(" luna me ajuda ") || normEspaco.contains(" falar com a luna ")) {

                return new ResultadoComando(
                        TipoAcao.AJUDA_VOZ,
                        original,
                        null,
                        "",
                        "🌙 Luna a postos!\n\nVocê pode:\n• Dizer \"Listar bares\" ou falar o nome do bar\n• Fazer pedidos (ex: \"Quero 2 coxinhas e 1 cerveja\")\n• Consultar a conta (ex: \"Quanto deu a comanda?\")\n• Alternar os 10% (ex: \"Com 10%\" ou \"Sem 10%\")\n• Pagar com Pix (ex: \"Pagar com Pix\")",
                        "Olá! Eu sou a Luna, sua assistente com inteligência artificial. Você pode me pedir para listar os bares, anotar seus pedidos, consultar o valor da conta ou pagar com Pix. O que deseja fazer?"
                );
            }
            // Se falou 'luna' no meio de outro comando (ex: 'luna me vê 2 coxinhas' ou 'luna quanto deu a conta'),
            // remove 'luna' para facilitar o reconhecimento dos pratos e das demais ações.
            norm = norm.replaceAll("\\bluna\\b", " ").replaceAll("\\s+", " ").trim();
            normEspaco = " " + norm + " ";

            // Reavalia intenção de listar bares após retirar a palavra 'luna'
            if (ehIntencaoListarBares(normEspaco)) {
                return new ResultadoComando(
                        TipoAcao.LISTAR_ESTABELECIMENTOS_LUNA,
                        original,
                        null,
                        extrairTermoBusca(original, norm),
                        "🌙 Luna: Listando estabelecimentos disponíveis...",
                        "Listando os bares disponíveis para você escolher."
                );
            }
        }

        // 1. AJUDA DE VOZ
        if (normEspaco.contains(" ajuda ") || normEspaco.contains(" o que falar ") ||
            normEspaco.contains(" como usar ") || normEspaco.contains(" comandos ") ||
            normEspaco.contains(" opcoes de voz ") || normEspaco.contains(" o que posso falar ")) {
            String msg = "Comandos de voz disponíveis: você pode pedir pratos e bebidas, pedir para tirar itens, consultar a conta, ativar ou tirar os dez por cento, filtrar locais com conforto acústico ou animados com som, pedir rota no Maps, gerar Pix, abrir seu banco ou pesquisar bares e cidades.";
            return new ResultadoComando(
                    TipoAcao.AJUDA_VOZ,
                    original,
                    null,
                    "",
                    "🎙️ Ajuda do Assistente de Voz:\n" + msg,
                    msg
            );
        }

        // 1.1 FILTRO DE SOM E CONFORTO ACÚSTICO
        if (normEspaco.contains(" conforto acustico ") || normEspaco.contains(" silencioso ") ||
            normEspaco.contains(" silenciosos ") || normEspaco.contains(" pouco ruido ") ||
            normEspaco.contains(" pouco barulho ") || normEspaco.contains(" sem barulho ") ||
            normEspaco.contains(" bar calmo ") || normEspaco.contains(" bares calmos ") ||
            normEspaco.contains(" local calmo ") || normEspaco.contains(" ambiente calmo ") ||
            normEspaco.contains(" musica baixa ") || normEspaco.contains(" som baixo ")) {
            return new ResultadoComando(
                    TipoAcao.FILTRAR_SOM_CONFORTO,
                    original,
                    null,
                    "",
                    "🔉 Filtrando locais com Conforto Acústico (silenciosos, até 65 dB)...",
                    "Filtro acústico ativado: mostrando apenas locais com conforto acústico e pouco ruído."
            );
        }

        if (normEspaco.contains(" com som ") || normEspaco.contains(" som alto ") ||
            normEspaco.contains(" animado ") || normEspaco.contains(" animados ") ||
            normEspaco.contains(" musica ao vivo ") || normEspaco.contains(" agito ") ||
            normEspaco.contains(" forro ") || normEspaco.contains(" samba ") ||
            normEspaco.contains(" sem conforto acustico ") || normEspaco.contains(" barulhento ") ||
            normEspaco.contains(" movimentado ") || normEspaco.contains(" movimentados ")) {
            return new ResultadoComando(
                    TipoAcao.FILTRAR_SOM_ANIMADO,
                    original,
                    null,
                    "",
                    "🎉 Filtrando locais Animados / Com Som (música ao vivo e agito, acima de 65 dB)...",
                    "Filtro de som ativado: mostrando locais animados e com som, acima de sessenta e cinco decibéis."
            );
        }

        if (normEspaco.contains(" todos os sons ") || normEspaco.contains(" qualquer som ") ||
            normEspaco.contains(" todos os ambientes ") || normEspaco.contains(" tirar filtro de som ") ||
            normEspaco.contains(" limpar filtro de som ")) {
            return new ResultadoComando(
                    TipoAcao.FILTRAR_SOM_TODOS,
                    original,
                    null,
                    "",
                    "🔊 Mostrando todos os ambientes (qualquer nível de som)...",
                    "Filtro de som desativado: mostrando todos os ambientes sonoros."
            );
        }

        // 2. ABRIR CARDÁPIO COMPLETO / LER CARDÁPIO / OPÇÕES DO CARDÁPIO
        if (ehIntencaoCardapio(normEspaco, norm)) {
            return new ResultadoComando(
                    TipoAcao.ABRIR_CARDAPIO_COMPLETO,
                    original,
                    null,
                    "",
                    "📖 Abrindo e lendo o Cardápio Completo...",
                    "Abrindo e lendo os itens do cardápio completo deste estabelecimento."
            );
        }

        // 3. CONSULTAR TOTAL / PEDIR A CONTA
        if (normEspaco.contains(" quanto deu ") || normEspaco.contains(" quanto ta dando ") ||
            normEspaco.contains(" quanto esta dando ") || normEspaco.contains(" qual o total ") ||
            normEspaco.contains(" quanto e ") || normEspaco.contains(" quanto deu a conta ") ||
            normEspaco.contains(" quanto ta a conta ") || normEspaco.contains(" quanto deu a comanda ") ||
            normEspaco.contains(" quanto ta a comanda ") || normEspaco.contains(" quanto custa ") ||
            normEspaco.contains(" pedir a conta ") || normEspaco.contains(" ver conta ") ||
            normEspaco.contains(" valor da conta ") || normEspaco.contains(" valor da comanda ") ||
            normEspaco.contains(" saldo ") || normEspaco.contains(" total da comanda ") ||
            normEspaco.contains(" total da conta ") || normEspaco.contains(" quanto ficou ") ||
            normEspaco.contains(" minha comanda ") || normEspaco.contains(" minha conta ") ||
            norm.equals("conta") || norm.equals("comanda") || norm.equals("total") || norm.equals("quanto deu")) {
            return new ResultadoComando(
                    TipoAcao.CONSULTAR_TOTAL,
                    original,
                    null,
                    "",
                    "💰 Consultando total da comanda...",
                    "Consultando a sua comanda ativa."
            );
        }

        // 4. LIMPAR OU ZERAR COMANDA
        if (normEspaco.contains(" zerar comanda ") || normEspaco.contains(" limpar comanda ") ||
            normEspaco.contains(" cancelar tudo ") || normEspaco.contains(" zerar conta ") ||
            normEspaco.contains(" apagar pedido ") || normEspaco.contains(" reiniciar conta ") ||
            normEspaco.contains(" apagar comanda ") || normEspaco.contains(" limpar conta ")) {
            return new ResultadoComando(
                    TipoAcao.LIMPAR_COMANDA,
                    original,
                    null,
                    "",
                    "🗑️ Zerando todos os itens da comanda ativa...",
                    "Todos os itens da sua comanda foram zerados."
            );
        }

        // 5. BANCO ESPECÍFICO (NUBANK, INTER, CAIXA, ITAÚ, BRADESCO, BB, ETC.)
        String bancoAlvo = identificarBanco(normEspaco);
        if (bancoAlvo != null) {
            return new ResultadoComando(
                    TipoAcao.ABRIR_BANCO,
                    original,
                    null,
                    bancoAlvo,
                    "📲 Copiando Pix e abrindo o banco " + bancoAlvo + "...",
                    "Código Pix copiado para a área de transferência. Abrindo aplicativo do " + bancoAlvo + "."
            );
        }

        // 6. TAXA DE 10% DO GARÇOM / RESPOSTAS RÁPIDAS
        if (norm.equals("sim") || norm.equals("quero") || norm.equals("pode ser") ||
            norm.equals("com certeza") || norm.equals("claro") || norm.equals("positivo") ||
            norm.equals("adicionar 10") || norm.equals("com 10") || norm.equals("colocar 10")) {
            return new ResultadoComando(
                    TipoAcao.TAXA_DEZ_POR_CENTO_ATIVAR,
                    original,
                    null,
                    "",
                    "💁 Taxa de 10% incluída na comanda.",
                    "Taxa de serviço de dez por cento adicionada à sua conta."
            );
        }
        if (norm.equals("nao") || norm.equals("nao quero") || norm.equals("sem 10") ||
            norm.equals("tirar 10") || norm.equals("sem taxa") || norm.equals("negativo")) {
            return new ResultadoComando(
                    TipoAcao.TAXA_DEZ_POR_CENTO_DESATIVAR,
                    original,
                    null,
                    "",
                    "💁 Taxa de 10% desativada na comanda.",
                    "Taxa de serviço de dez por cento removida da sua conta."
            );
        }

        if (normEspaco.contains(" 10 por cento ") || normEspaco.contains(" dez por cento ") ||
            normEspaco.contains(" taxa ") || normEspaco.contains(" gorjeta ") || normEspaco.contains(" taxa de servico ") ||
            normEspaco.contains(" 10 ") || normEspaco.contains(" dez ")) {

            if (normEspaco.contains(" tirar ") || normEspaco.contains(" sem ") ||
                normEspaco.contains(" remover ") || normEspaco.contains(" nao quero ") ||
                normEspaco.contains(" cancelar ") || normEspaco.contains(" tira ")) {
                return new ResultadoComando(
                        TipoAcao.TAXA_DEZ_POR_CENTO_DESATIVAR,
                        original,
                        null,
                        "",
                        "💁 Taxa de 10% desativada na comanda.",
                        "Taxa de serviço de dez por cento removida da sua conta."
                );
            } else if (normEspaco.contains(" colocar ") || normEspaco.contains(" adicionar ") ||
                       normEspaco.contains(" com ") || normEspaco.contains(" incluir ") ||
                       normEspaco.contains(" bota ") || normEspaco.contains(" poe ") ||
                       normEspaco.contains(" bota ")) {
                return new ResultadoComando(
                        TipoAcao.TAXA_DEZ_POR_CENTO_ATIVAR,
                        original,
                        null,
                        "",
                        "💁 Taxa de 10% incluída na comanda.",
                        "Taxa de serviço de dez por cento adicionada à sua conta."
                );
            } else {
                return new ResultadoComando(
                        TipoAcao.TAXA_DEZ_POR_CENTO_PERGUNTAR,
                        original,
                        null,
                        "",
                        "💁 Alternando taxa de 10% do garçom...",
                        "Alternando a taxa de dez por cento da sua conta."
                );
            }
        }

        // 6.1 TETO DE GASTOS / ALTERAR LIMITE
        if (normEspaco.contains(" teto ") || normEspaco.contains(" teto de gastos ") ||
            normEspaco.contains(" alterar teto ") || normEspaco.contains(" mudar teto ") ||
            normEspaco.contains(" limite de gastos ") || normEspaco.contains(" limite ") ||
            normEspaco.contains(" alterar limite ") || normEspaco.contains(" trocar teto ")) {
            return new ResultadoComando(
                    TipoAcao.ALTERAR_TETO_GASTOS,
                    original,
                    null,
                    "",
                    "🎯 Alternando teto de gastos da comanda...",
                    "Alternando limite de teto de gastos da comanda."
            );
        }

        // 7. PAGAMENTO POR PIX / FECHAR A CONTA
        if (normEspaco.contains(" pix ") || normEspaco.contains(" pagar com pix ") ||
            normEspaco.contains(" gerar pix ") || normEspaco.contains(" copiar pix ") ||
            normEspaco.contains(" codigo pix ") || normEspaco.contains(" pagar a conta ") ||
            normEspaco.contains(" pagar conta ") || normEspaco.contains(" fechar a conta ") ||
            normEspaco.contains(" fechar conta ") || normEspaco.contains(" fechar minha conta ") ||
            normEspaco.contains(" quero pagar ") || normEspaco.contains(" vamos pagar ") ||
            normEspaco.contains(" encerrar conta ") || normEspaco.contains(" efetuar pagamento ")) {
            return new ResultadoComando(
                    TipoAcao.PAGAR_PIX,
                    original,
                    null,
                    "",
                    "📲 Gerando Pix e preparando abertura dos bancos...",
                    "Código Pix gerado com sucesso. Escolha o banco para pagar."
            );
        }

        // 8. GOOGLE MAPS & ROTAS
        if (normEspaco.contains(" rota ") || normEspaco.contains(" como chegar ") ||
            normEspaco.contains(" tracar rota ") || normEspaco.contains(" ir ate o bar ") ||
            normEspaco.contains(" ir para o bar ") || normEspaco.contains(" abrir maps ") ||
            normEspaco.contains(" ver rota ")) {
            return new ResultadoComando(
                    TipoAcao.ABRIR_ROTA_MAPS,
                    original,
                    null,
                    "",
                    "📍 Traçando rota no Google Maps...",
                    "Abrindo a rota no Google Maps até o estabelecimento selecionado."
            );
        }

        // 8. GOOGLE MAPS (BUSCA DE BARES NO MAPA / NOMEPS)
        if (normEspaco.contains(" maps ") || normEspaco.contains(" no maps ") ||
            normEspaco.contains(" nomeps ") || normEspaco.contains(" no meps ") ||
            normEspaco.contains(" google maps ") || normEspaco.contains(" no mapa ") ||
            normEspaco.contains(" ver no mapa ") || normEspaco.contains(" buscar no mapa ") ||
            normEspaco.contains(" procurar no maps ") || normEspaco.contains(" buscar no maps ") ||
            normEspaco.contains(" abrir maps ") || normEspaco.contains(" bares no maps ")) {

            String consultaMaps = extrairConsultaMaps(original, norm);
            return new ResultadoComando(
                    TipoAcao.VER_BARES_MAPS,
                    original,
                    null,
                    consultaMaps,
                    "🗺️ Abrindo busca de bares no Google Maps: \"" + (consultaMaps.isEmpty() ? "Bares da cidade" : consultaMaps) + "\"...",
                    "Abrindo o Google Maps para buscar bares " + (consultaMaps.isEmpty() ? "nesta cidade" : "em " + consultaMaps) + "."
            );
        }

        // 8. BANCO DE DADOS SQLITE (HISTÓRICO E CADASTRO)
        if (normEspaco.contains(" salvar no historico ") || normEspaco.contains(" salvar comanda ") ||
            normEspaco.contains(" guardar comanda ") || normEspaco.contains(" salvar no banco ") ||
            normEspaco.contains(" gravar no banco ")) {
            return new ResultadoComando(
                    TipoAcao.SALVAR_HISTORICO_BANCO,
                    original,
                    null,
                    "",
                    "🗄️ Salvando comanda no banco de dados SQLite...",
                    "Comanda salva com sucesso no banco de dados histórico."
            );
        }

        if (normEspaco.contains(" ouvir historico ") || normEspaco.contains(" ler historico ") ||
            normEspaco.contains(" historico de comandas ") || normEspaco.contains(" ver historico ") ||
            normEspaco.contains(" comandas salvas ") || normEspaco.contains(" historico de contas ")) {
            return new ResultadoComando(
                    TipoAcao.OUVIR_HISTORICO_BANCO,
                    original,
                    null,
                    "",
                    "🗄️ Abrindo histórico de comandas salvas...",
                    "Abrindo lista do histórico de comandas salvas no SQLite."
            );
        }

        if (normEspaco.contains(" cadastrar prato ") || normEspaco.contains(" novo prato ") ||
            normEspaco.contains(" cadastrar item ") || normEspaco.contains(" adicionar prato no banco ")) {
            return new ResultadoComando(
                    TipoAcao.CADASTRAR_PRATO_BANCO,
                    original,
                    null,
                    "",
                    "➕ Abrindo cadastro de novo prato no banco de dados...",
                    "Abrindo formulário acessível para cadastrar novo item no cardápio."
            );
        }

        // 9. REMOVER ITENS DO CARDÁPIO
        if (ehIntencaoRemocao(normEspaco) && estAtual != null && !estAtual.cardapio.isEmpty()) {
            String fraseSemVerbo = norm.replaceAll(
                    "\\b(tirar|tira|tire|remover|remova|retirar|retira|retire|cancelar|cancela|excluir|exclua|exclui|apagar|apaga|diminuir|diminui|menos|deletar|deleta)\\b",
                    " "
            ).replaceAll("\\s+", " ").trim();

            List<ItemQtd> itensParaRemover = identificarItensComQuantidade(fraseSemVerbo, estAtual);
            if (itensParaRemover.isEmpty()) {
                itensParaRemover = identificarItensComQuantidade(norm, estAtual);
            }

            if (!itensParaRemover.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < itensParaRemover.size(); i++) {
                    ItemQtd iq = itensParaRemover.get(i);
                    sb.append(iq.quantidade).append("x ").append(iq.item.nome);
                    if (i < itensParaRemover.size() - 1) sb.append(", ");
                }
                return new ResultadoComando(
                        TipoAcao.REMOVER_ITENS,
                        original,
                        itensParaRemover,
                        "",
                        "➖ Removendo da comanda: " + sb.toString(),
                        "Removendo da sua conta: " + sb.toString()
                );
            }
        }

        // 10. ADICIONAR ITENS AO CARDÁPIO
        if (estAtual != null && !estAtual.cardapio.isEmpty()) {
            List<ItemQtd> itensParaAdicionar = identificarItensComQuantidade(norm, estAtual);
            if (!itensParaAdicionar.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < itensParaAdicionar.size(); i++) {
                    ItemQtd iq = itensParaAdicionar.get(i);
                    sb.append(iq.quantidade).append("x ").append(iq.item.nome);
                    if (i < itensParaAdicionar.size() - 1) sb.append(", ");
                }
                return new ResultadoComando(
                        TipoAcao.ADICIONAR_ITENS,
                        original,
                        itensParaAdicionar,
                        "",
                        "➕ Adicionando à comanda: " + sb.toString(),
                        "Adicionado à sua comanda: " + sb.toString()
                );
            }
        }

        // 11. FILTRO OU BUSCA DE ESTABELECIMENTO / CIDADE / ESTADO
        if (normEspaco.contains(" buscar ") || normEspaco.contains(" procurar ") ||
            normEspaco.contains(" localizar ") || normEspaco.contains(" ir para ") ||
            normEspaco.contains(" filtrar ") || normEspaco.contains(" bar ") || normEspaco.contains(" ba ") ||
            normEspaco.contains(" bares ") || normEspaco.contains(" bas ") ||
            normEspaco.contains(" boteco ") || normEspaco.contains(" botecos ") ||
            normEspaco.contains(" restaurante ") || normEspaco.contains(" restaurantes ") || normEspaco.contains(" cidade ") ||
            normEspaco.contains(" estado ") || normEspaco.contains(" pernambuco ") ||
            normEspaco.contains(" recife ") || normEspaco.contains(" olinda ") ||
            normEspaco.contains(" caruaru ") || normEspaco.contains(" campina grande ") ||
            normEspaco.contains(" igarassu ") || normEspaco.contains(" camaragibe ") ||
            normEspaco.contains(" camaragibi ") || normEspaco.contains(" sao lourenco ") ||
            normEspaco.contains(" sao lorensso ") || normEspaco.contains(" lorensso ") ||
            normEspaco.contains(" sao paulo ")) {

            String termoBusca = extrairTermoBusca(original, norm);
            return new ResultadoComando(
                    TipoAcao.FILTRAR_OU_BUSCAR_LOCAL,
                    original,
                    null,
                    termoBusca,
                    "🔍 Pesquisando local ou aplicando filtro por voz: \"" + termoBusca + "\"...",
                    "Pesquisando estabelecimento ou filtrando por " + termoBusca + "."
            );
        }

        // 12. COMANDO NÃO RECONHECIDO (NUNCA DEIXAR O USUÁRIO NO VÁCUO)
        return new ResultadoComando(
                TipoAcao.COMANDO_NAO_RECONHECIDO,
                original,
                null,
                "",
                "🎙️ Luna não reconheceu: \"" + original + "\".\n\nVocê pode pedir por voz:\n• \"Listar bares\" ou \"Mudar de bar\"\n• Falar o nome do prato ou bebida\n• \"Quanto tá a conta?\"\n• \"Com 10%\" ou \"Sem 10%\"\n• \"Pagar com Pix\"",
                "Não entendi esse comando. Você pode me pedir para listar os bares, fazer um pedido, consultar a comanda ou pagar com Pix. Diga o que deseja fazer."
        );
    }

    public static String identificarBanco(String normEspaco) {
        if (normEspaco == null) return null;
        if (normEspaco.contains(" nubank ") || normEspaco.contains(" nu ") || normEspaco.contains(" roxinho ")) return "Nubank";
        if (normEspaco.contains(" banco inter ") || normEspaco.contains(" inter ") || normEspaco.contains(" laranjinha ") || normEspaco.contains(" intermedium ")) return "Banco Inter";
        if (normEspaco.contains(" itau ") || normEspaco.contains(" itaucard ") || normEspaco.contains(" itau unibanco ")) return "Itaú";
        if (normEspaco.contains(" bradesco ")) return "Bradesco";
        if (normEspaco.contains(" banco do brasil ") || normEspaco.contains(" bb ") || normEspaco.contains(" brasil ")) return "Banco do Brasil";
        if (normEspaco.contains(" caixa ") || normEspaco.contains(" caixa tem ") || normEspaco.contains(" caixa economica ") || normEspaco.contains(" caixa federal ")) return "Caixa";
        if (normEspaco.contains(" santander ")) return "Santander";
        if (normEspaco.contains(" picpay ") || normEspaco.contains(" pic pay ")) return "PicPay";
        if (normEspaco.contains(" mercado pago ") || normEspaco.contains(" mercadopago ")) return "Mercado Pago";
        if (normEspaco.contains(" c6 ") || normEspaco.contains(" c6 bank ")) return "C6 Bank";
        if (normEspaco.contains(" pagbank ") || normEspaco.contains(" pagseguro ") || normEspaco.contains(" pag bank ")) return "PagBank";
        if (normEspaco.contains(" neon ")) return "Neon";
        if (normEspaco.contains(" next ")) return "Next";
        if (normEspaco.contains(" will bank ") || normEspaco.contains(" will ")) return "Will Bank";
        if (normEspaco.contains(" sicredi ")) return "Sicredi";
        if (normEspaco.contains(" sicoob ")) return "Sicoob";
        return null;
    }

    private static String extrairTermoBusca(String original, String norm) {
        String t = original;
        String[] prefixes = new String[]{
                "buscar bar ", "buscar restaurante ", "buscar local ", "buscar ",
                "procurar bar ", "procurar restaurante ", "procurar ",
                "localizar bar ", "localizar restaurante ", "localizar ",
                "ir para o bar ", "ir para o restaurante ", "ir para ",
                "estou na cidade de ", "estou em ", "estou na ", "estou no ",
                "mudar para a cidade de ", "mudar cidade para ", "mudar para ",
                "filtrar por cidade ", "filtrar cidade ", "filtrar por ", "filtrar ",
                "cidade de "
        };
        for (String p : prefixes) {
            if (normalizar(t).startsWith(normalizar(p))) {
                t = t.substring(p.length()).trim();
                break;
            }
        }
        return t.trim();
    }

    private static String extrairConsultaMaps(String original, String norm) {
        String resultado = norm;
        String[] descartar = new String[]{
                "buscar no maps", "procurar no maps", "abrir no maps", "ver no maps",
                "bares no maps", "bar no maps", "ba no maps", "no maps", "nomeps", "no meps",
                "google maps", "buscar no mapa", "ver no mapa", "no mapa", "abrir maps", "maps", "mapa",
                "bares em", "bar em", "ba em", "restaurantes em", "procurar", "buscar", "pesquisar"
        };
        for (String d : descartar) {
            resultado = resultado.replaceAll("\\b" + java.util.regex.Pattern.quote(d) + "\\b", " ");
        }
        return resultado.replaceAll("\\s+", " ").trim();
    }

    private static List<ItemQtd> identificarItensComQuantidade(String normFrase, MainActivity.Estabelecimento est) {
        List<ItemQtd> resultado = new ArrayList<ItemQtd>();
        if (est == null || est.cardapio == null || est.cardapio.isEmpty()) return resultado;

        // Dividir a frase em orações por conectivos: " e ", ", ", " mais ", " com "
        String[] pedacos = normFrase.split("\\b(e|mais|com|,)\\b");
        for (String pedaco : pedacos) {
            String pNorm = pedaco.trim();
            if (pNorm.isEmpty()) continue;

            MainActivity.ItemCardapio melhorItem = null;
            int maiorScore = 0;

            for (MainActivity.ItemCardapio it : est.cardapio) {
                int score = calcularScoreItem(pNorm, it);
                if (score > maiorScore) {
                    maiorScore = score;
                    melhorItem = it;
                }
            }

            if (melhorItem != null && maiorScore >= 5) {
                int qtd = extrairQuantidade(pNorm);
                // Evitar duplicar mesmo item no mesmo comando
                boolean jaExiste = false;
                for (int i = 0; i < resultado.size(); i++) {
                    if (resultado.get(i).item.id.equals(melhorItem.id)) {
                        jaExiste = true;
                        break;
                    }
                }
                if (!jaExiste) {
                    resultado.add(new ItemQtd(melhorItem, qtd));
                }
            }
        }

        // Se a divisão por conectivos não achou nada, tenta na frase inteira
        if (resultado.isEmpty()) {
            for (MainActivity.ItemCardapio it : est.cardapio) {
                int score = calcularScoreItem(normFrase, it);
                if (score >= 5) {
                    int qtd = extrairQuantidade(normFrase);
                    resultado.add(new ItemQtd(it, qtd));
                    break;
                }
            }
        }

        // Se a busca textual por nome do prato não achou nada, tenta identificar por número/opção do cardápio
        // (ex: "opção 1", "número 2", "o primeiro", "1", "quero o 2", "duas da opção 1")
        if (resultado.isEmpty()) {
            int indice = interpretarNumeroEscolhaItemCardapio(normFrase, est.cardapio.size());
            if (indice >= 0 && indice < est.cardapio.size()) {
                int qtd = extrairQuantidadeParaOpcao(normFrase, indice);
                resultado.add(new ItemQtd(est.cardapio.get(indice), qtd));
            }
        }

        return resultado;
    }

    private static int calcularScoreItem(String trecho, MainActivity.ItemCardapio it) {
        String nomeNorm = normalizar(it.nome);
        if (trecho.contains(nomeNorm)) return 10;

        int score = 0;
        String[] palavrasNome = nomeNorm.split("\\s+");
        for (String pn : palavrasNome) {
            if (pn.length() > 2 && trecho.contains(pn)) {
                score += 3;
            }
        }

        if (it.palavrasChave != null) {
            for (String pc : it.palavrasChave) {
                String pcNorm = normalizar(pc);
                if (pcNorm.length() > 2 && trecho.contains(pcNorm)) {
                    score += 6;
                }
            }
        }

        String catNorm = normalizar(it.categoria);
        if (catNorm.length() > 3 && trecho.contains(catNorm)) {
            score += 2;
        }

        return score;
    }
}

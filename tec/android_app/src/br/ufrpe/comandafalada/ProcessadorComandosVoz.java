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
        PAGAR_PIX,
        ABRIR_BANCO,
        ABRIR_CARDAPIO_COMPLETO,
        ABRIR_ROTA_MAPS,
        VER_BARES_MAPS,
        FILTRAR_OU_BUSCAR_LOCAL,
        SALVAR_HISTORICO_BANCO,
        OUVIR_HISTORICO_BANCO,
        CADASTRAR_PRATO_BANCO,
        FILTRAR_SOM_CONFORTO,
        FILTRAR_SOM_ANIMADO,
        FILTRAR_SOM_TODOS,
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

        // 2. ABRIR CARDÁPIO COMPLETO
        if (normEspaco.contains(" abrir cardapio ") || normEspaco.contains(" ver cardapio ") ||
            normEspaco.contains(" ler cardapio ") || normEspaco.contains(" cardapio completo ") ||
            normEspaco.contains(" lista do cardapio ") || normEspaco.contains(" opcoes do cardapio ") ||
            normEspaco.contains(" o que tem pra comer ") || normEspaco.contains(" o que tem para comer ")) {
            return new ResultadoComando(
                    TipoAcao.ABRIR_CARDAPIO_COMPLETO,
                    original,
                    null,
                    "",
                    "📖 Abrindo janela com o Cardápio Completo...",
                    "Abrindo janela com o cardápio completo deste estabelecimento."
            );
        }

        // 3. CONSULTAR TOTAL / PEDIR A CONTA
        if (normEspaco.contains(" quanto deu ") || normEspaco.contains(" quanto ta dando ") ||
            normEspaco.contains(" quanto esta dando ") || normEspaco.contains(" qual o total ") ||
            normEspaco.contains(" fechar conta ") || normEspaco.contains(" fechar a conta ") ||
            normEspaco.contains(" pedir a conta ") || normEspaco.contains(" ver conta ") ||
            normEspaco.contains(" valor da conta ") || normEspaco.contains(" saldo ") ||
            normEspaco.contains(" total da comanda ") || normEspaco.contains(" quanto ficou ")) {
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

        // 5. TAXA DE 10% DO GARÇOM
        if (normEspaco.contains(" 10 por cento ") || normEspaco.contains(" dez por cento ") ||
            normEspaco.contains(" taxa ") || normEspaco.contains(" gorjeta ") || normEspaco.contains(" taxa de servico ")) {

            if (normEspaco.contains(" tirar ") || normEspaco.contains(" sem ") ||
                normEspaco.contains(" remover ") || normEspaco.contains(" nao quero ") ||
                normEspaco.contains(" cancelar ")) {
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
                        "💁 Abrindo diálogo da Taxa de Serviço de 10%...",
                        "Deseja adicionar os dez por cento de taxa de serviço na sua conta?"
                );
            }
        }

        // 6. PAGAMENTO POR PIX OU ABRIR BANCO
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

        if (normEspaco.contains(" pix ") || normEspaco.contains(" pagar com pix ") ||
            normEspaco.contains(" gerar pix ") || normEspaco.contains(" copiar pix ") ||
            normEspaco.contains(" codigo pix ") || normEspaco.contains(" pagar a conta ") ||
            normEspaco.contains(" efetuar pagamento ")) {
            return new ResultadoComando(
                    TipoAcao.PAGAR_PIX,
                    original,
                    null,
                    "",
                    "📲 Gerando Pix e preparando abertura dos bancos...",
                    "Código Pix gerado com sucesso. Escolha o banco para pagar."
            );
        }

        // 7. GOOGLE MAPS & ROTAS
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

        if (normEspaco.contains(" bares no maps ") || normEspaco.contains(" bares no mapa ") ||
            normEspaco.contains(" ver no mapa ") || normEspaco.contains(" mostrar no mapa ")) {
            return new ResultadoComando(
                    TipoAcao.VER_BARES_MAPS,
                    original,
                    null,
                    "",
                    "🗺️ Abrindo bares da cidade no Google Maps...",
                    "Abrindo bares e restaurantes desta cidade no Google Maps."
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
        boolean ehRemocao = normEspaco.contains(" tirar ") || normEspaco.contains(" remover ") ||
                            normEspaco.contains(" cancelar ") || normEspaco.contains(" apagar ") ||
                            normEspaco.contains(" menos ") || normEspaco.contains(" diminuir ");

        if (ehRemocao && estAtual != null && !estAtual.cardapio.isEmpty()) {
            List<ItemQtd> itensParaRemover = identificarItensComQuantidade(norm, estAtual);
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
            normEspaco.contains(" filtrar ") || normEspaco.contains(" bar ") ||
            normEspaco.contains(" restaurante ") || normEspaco.contains(" cidade ") ||
            normEspaco.contains(" estado ") || normEspaco.contains(" pernambuco ") ||
            normEspaco.contains(" recife ") || normEspaco.contains(" olinda ") ||
            normEspaco.contains(" caruaru ") || normEspaco.contains(" campina grande ") ||
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

        // 12. FALLBACK INTELIGENTE (TENTA LOCALIZAR BAR OU INFORMA OPÇÕES)
        if (original.length() >= 3) {
            return new ResultadoComando(
                    TipoAcao.FILTRAR_OU_BUSCAR_LOCAL,
                    original,
                    null,
                    original,
                    "🔍 Tentando localizar estabelecimento por voz: \"" + original + "\"...",
                    "Buscando estabelecimento correspondente a " + original + "."
            );
        }

        return new ResultadoComando(
                TipoAcao.COMANDO_NAO_RECONHECIDO,
                original,
                null,
                "",
                "🎙️ Você disse: \"" + original + "\". Diga 'ajuda' para ouvir comandos.",
                "Não entendi o comando. Diga ajuda para ouvir todas as opções."
        );
    }

    private static String identificarBanco(String normEspaco) {
        if (normEspaco.contains(" nubank ") || normEspaco.contains(" roxinho ")) return "Nubank";
        if (normEspaco.contains(" banco inter ") || normEspaco.contains(" inter ")) return "Banco Inter";
        if (normEspaco.contains(" itau ")) return "Itaú";
        if (normEspaco.contains(" bradesco ")) return "Bradesco";
        if (normEspaco.contains(" banco do brasil ") || normEspaco.contains(" bb ")) return "Banco do Brasil";
        if (normEspaco.contains(" caixa ") || normEspaco.contains(" caixa tem ") || normEspaco.contains(" caixa economica ")) return "Caixa";
        if (normEspaco.contains(" santander ")) return "Santander";
        if (normEspaco.contains(" picpay ")) return "PicPay";
        if (normEspaco.contains(" mercado pago ")) return "Mercado Pago";
        if (normEspaco.contains(" c6 ") || normEspaco.contains(" c6 bank ")) return "C6 Bank";
        if (normEspaco.contains(" pagbank ") || normEspaco.contains(" pagseguro ")) return "PagBank";
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
                "filtrar por ", "filtrar "
        };
        for (String p : prefixes) {
            if (normalizar(t).startsWith(normalizar(p))) {
                t = t.substring(p.length()).trim();
                break;
            }
        }
        return t.trim();
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
                if (trecho.contains(pcNorm)) {
                    score += 4;
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

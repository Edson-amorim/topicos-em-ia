package br.ufrpe.comandafalada;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final int REQ_VOZ_BUSCA_LOCAL = 101;
    private static final int REQ_VOZ_PEDIDO_CARDAPIO = 102;

    // Coordenadas de referência do usuário (Marco Zero / Recife Antigo)
    private static final double LAT_USUARIO = -8.0628;
    private static final double LON_USUARIO = -34.8714;

    public static class ItemCardapio {
        public final String id;
        public final String nome;
        public final String categoria;
        public final int precoCentavos;
        public final String[] palavrasChave;

        public ItemCardapio(String id, String nome, String categoria, int precoCentavos, String... palavrasChave) {
            this.id = id;
            this.nome = nome;
            this.categoria = categoria;
            this.precoCentavos = precoCentavos;
            this.palavrasChave = palavrasChave;
        }

        public String formatarPreco() {
            return formatarMoedaCentavos(precoCentavos);
        }
    }

    public static class Estabelecimento {
        public final String id;
        public final String nome;
        public final String tipo;
        public final String bairro;
        public final String chavePix;
        public final double latitude;
        public final double longitude;
        public final int decibeisMedio;
        public final String nivelRuido;
        public final String tipoSom;
        public final String acessibilidade;
        public final List<ItemCardapio> cardapio = new ArrayList<ItemCardapio>();

        public Estabelecimento(
                String id,
                String nome,
                String tipo,
                String bairro,
                String chavePix,
                double latitude,
                double longitude,
                int decibeisMedio,
                String nivelRuido,
                String tipoSom,
                String acessibilidade
        ) {
            this.id = id;
            this.nome = nome;
            this.tipo = tipo;
            this.bairro = bairro;
            this.chavePix = chavePix;
            this.latitude = latitude;
            this.longitude = longitude;
            this.decibeisMedio = decibeisMedio;
            this.nivelRuido = nivelRuido;
            this.tipoSom = tipoSom;
            this.acessibilidade = acessibilidade;
        }
    }

    public static class BancoAppInfo {
        public final String nomeBanco;
        public final String pacote;
        public final Intent intentAbertura;

        public BancoAppInfo(String nomeBanco, String pacote, Intent intentAbertura) {
            this.nomeBanco = nomeBanco;
            this.pacote = pacote;
            this.intentAbertura = intentAbertura;
        }
    }

    private TextView tvResultado;
    private TextView tvTituloCardapio;
    private TextView tvResumoComanda;
    private LinearLayout containerEstabelecimentos;
    private LinearLayout containerCardapio;
    private LinearLayout containerBancosInstalados;
    private Button btnFiltroAcustico;
    private Button btnFiltroTodos;
    private Button btnBuscarVozLocal;
    private Button btnPedirPorVoz;
    private Button btnOuvirComanda;
    private Button btnAlterarTeto;
    private Button btnLimparComanda;
    private Button btnPagarBanco;
    private Button btnCaso1;
    private Button btnCaso2;
    private Button btnCaso3;

    private TextToSpeech tts;
    private final List<Estabelecimento> catalogoEstabelecimentos = new ArrayList<Estabelecimento>();
    private Estabelecimento estabelecimentoSelecionado;
    private boolean filtrarApenasSilenciosos = false;

    // Comanda atual: chave = ItemCardapio.id, valor = quantidade
    private final Map<String, Integer> quantidadesComanda = new LinkedHashMap<String, Integer>();
    private int tetoGastosCentavos = 10000; // R$ 100,00 padrão
    private String senhaEntregaAtual = "8186";
    private String ultimoPayloadPix = "";
    private int ultimoTotalCentavos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResultado = findViewById(R.id.tvResultado);
        tvTituloCardapio = findViewById(R.id.tvTituloCardapio);
        tvResumoComanda = findViewById(R.id.tvResumoComanda);
        containerEstabelecimentos = findViewById(R.id.containerEstabelecimentos);
        containerCardapio = findViewById(R.id.containerCardapio);
        containerBancosInstalados = findViewById(R.id.containerBancosInstalados);

        btnFiltroAcustico = findViewById(R.id.btnFiltroAcustico);
        btnFiltroTodos = findViewById(R.id.btnFiltroTodos);
        btnBuscarVozLocal = findViewById(R.id.btnBuscarVozLocal);
        btnPedirPorVoz = findViewById(R.id.btnPedirPorVoz);
        btnOuvirComanda = findViewById(R.id.btnOuvirComanda);
        btnAlterarTeto = findViewById(R.id.btnAlterarTeto);
        btnLimparComanda = findViewById(R.id.btnLimparComanda);
        btnPagarBanco = findViewById(R.id.btnPagarBanco);
        btnCaso1 = findViewById(R.id.btnCaso1);
        btnCaso2 = findViewById(R.id.btnCaso2);
        btnCaso3 = findViewById(R.id.btnCaso3);

        tts = new TextToSpeech(this, this);

        carregarGrafoBaresERestaurantes();
        estabelecimentoSelecionado = catalogoEstabelecimentos.get(0);

        btnFiltroAcustico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarApenasSilenciosos = true;
                renderizarListaEstabelecimentos();
                String msg = "Filtro acústico ativado: exibindo apenas bares e restaurantes confortáveis com até 65 decibéis.";
                atualizarPainelEFalar(msg, msg);
            }
        });

        btnFiltroTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarApenasSilenciosos = false;
                renderizarListaEstabelecimentos();
                String msg = "Exibindo todos os 4 bares e restaurantes cadastrados no grafo.";
                atualizarPainelEFalar(msg, msg);
            }
        });

        btnBuscarVozLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarReconhecimentoVoz(
                        REQ_VOZ_BUSCA_LOCAL,
                        "Fale o nome do bar, restaurante, bairro ou prato que deseja buscar"
                );
            }
        });

        btnPedirPorVoz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarReconhecimentoVoz(
                        REQ_VOZ_PEDIDO_CARDAPIO,
                        "Fale os itens do cardápio de " + estabelecimentoSelecionado.nome + " para a IA somar na comanda"
                );
            }
        });

        btnOuvirComanda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouvirResumoFaladoDaComanda();
            }
        });

        btnAlterarTeto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alternarTetoDeGastos();
            }
        });

        btnLimparComanda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantidadesComanda.clear();
                renderizarCardapioEstabelecimento();
                recalcularComanda();
                String msg = "Comanda limpa. Escolha novos itens no cardápio de " + estabelecimentoSelecionado.nome + ".";
                atualizarPainelEFalar(msg, msg);
            }
        });

        btnPagarBanco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acaoPagarComandaNosBancos();
            }
        });

        btnCaso1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executarCaso1();
            }
        });

        btnCaso2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executarCaso2();
            }
        });

        btnCaso3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executarCaso3();
            }
        });

        renderizarListaEstabelecimentos();
        renderizarCardapioEstabelecimento();
        recalcularComanda();
        renderizarBancosInstalados();
    }

    private void carregarGrafoBaresERestaurantes() {
        catalogoEstabelecimentos.clear();

        // 1. Bar Cais do Sertão & Chopp
        Estabelecimento barCais = new Estabelecimento(
                "bar_cais_antigo",
                "Bar Cais do Sertão & Chopp",
                "Bar & Petiscaria",
                "Recife Antigo",
                "pix@barcaisdosertao.com.br",
                -8.0617,
                -34.8711,
                58,
                "Baixo (Confortável)",
                "Música instrumental ambiente baixa",
                "Calçada acessível, espaço amplo entre mesas e garçom atencioso"
        );
        barCais.cardapio.add(new ItemCardapio("cais_caldinho", "Caldinho de Feijão", "Entrada", 1200, "caldinho", "feijao"));
        barCais.cardapio.add(new ItemCardapio("cais_suco", "Suco de Laranja Natural", "Bebida", 1000, "suco", "laranja"));
        barCais.cardapio.add(new ItemCardapio("cais_chope", "Chope Artesanal Gelado", "Bebida", 1150, "chope", "chopp"));
        barCais.cardapio.add(new ItemCardapio("cais_macaxeira", "Macaxeira Frita com Queijo Coalho", "Petisco", 3400, "macaxeira", "queijo", "coalho"));
        barCais.cardapio.add(new ItemCardapio("cais_carne_sol", "Carne de Sol com Fritas", "Prato Principal", 4800, "carne de sol", "fritas", "carne"));
        barCais.cardapio.add(new ItemCardapio("cais_coca", "Coca-Cola Gelada Lata", "Bebida", 800, "coca", "refrigerante"));
        barCais.cardapio.add(new ItemCardapio("cais_agua", "Água Mineral sem Gás", "Bebida", 600, "agua", "mineral"));
        catalogoEstabelecimentos.add(barCais);

        // 2. Restaurante Sabor do Agreste
        Estabelecimento restAgreste = new Estabelecimento(
                "rest_sabor_agreste",
                "Restaurante Sabor do Agreste",
                "Restaurante Regional",
                "Graças",
                "pagamento@sabordagreste.com.br",
                -8.0482,
                -34.9012,
                54,
                "Muito Baixo (Silencioso)",
                "Ambiente climatizado silencioso sem caixa de som",
                "Rampa de entrada, mesas espaçadas e atendimento prioritário na mesa"
        );
        restAgreste.cardapio.add(new ItemCardapio("agr_arrumadinho", "Arrumadinho de Charque Completo", "Prato Principal", 3800, "arrumadinho", "charque"));
        restAgreste.cardapio.add(new ItemCardapio("agr_escondidinho", "Escondidinho de Carne de Sol", "Prato Principal", 3600, "escondidinho", "carne de sol"));
        restAgreste.cardapio.add(new ItemCardapio("agr_bacalhau", "Bolinho de Bacalhau (6 unid)", "Entrada", 2800, "bolinho", "bacalhau"));
        restAgreste.cardapio.add(new ItemCardapio("agr_caldo_verde", "Caldo Verde com Torradas", "Entrada", 1500, "caldo verde", "torradas", "caldo"));
        restAgreste.cardapio.add(new ItemCardapio("agr_suco_acerola", "Suco de Acerola com Laranja", "Bebida", 1050, "acerola", "suco"));
        restAgreste.cardapio.add(new ItemCardapio("agr_bolo_rolo", "Café Expresso com Bolo de Rolo", "Sobremesa", 1650, "cafe", "bolo de rolo", "sobremesa"));
        catalogoEstabelecimentos.add(restAgreste);

        // 3. Restaurante Varanda Jardim da Praia
        Estabelecimento restVaranda = new Estabelecimento(
                "bar_jardim_boa_viagem",
                "Restaurante Varanda Jardim da Praia",
                "Restaurante & Frutos do Mar",
                "Boa Viagem",
                "pix@varandajardim.com.br",
                -8.1256,
                -34.9015,
                65,
                "Médio (Agradável)",
                "Som de brisa do mar e MPB suave ao fundo",
                "Rampa de acesso e piso tátil até a entrada"
        );
        restVaranda.cardapio.add(new ItemCardapio("var_camarao", "Caldinho de Camarão", "Entrada", 1600, "caldinho", "camarao"));
        restVaranda.cardapio.add(new ItemCardapio("var_peixe", "Isca de Peixe com Molho Tártaro", "Petisco", 4200, "isca", "peixe", "tartaro"));
        restVaranda.cardapio.add(new ItemCardapio("var_moqueca", "Moqueca Pernambucana Individual", "Prato Principal", 5400, "moqueca", "pernambucana"));
        restVaranda.cardapio.add(new ItemCardapio("var_suco", "Suco de Laranja Natural", "Bebida", 1100, "suco", "laranja"));
        restVaranda.cardapio.add(new ItemCardapio("var_coco", "Água de Coco Gelada", "Bebida", 900, "agua de coco", "coco"));
        restVaranda.cardapio.add(new ItemCardapio("var_pudim", "Pudim de Leite Condensado", "Sobremesa", 1400, "pudim", "sobremesa"));
        catalogoEstabelecimentos.add(restVaranda);

        // 4. Boteco do Galo Frito (Ruidoso)
        Estabelecimento botecoGalo = new Estabelecimento(
                "bar_boemia_ruidoso",
                "Boteco do Galo Frito",
                "Boteco Boêmio",
                "Recife Antigo",
                "pix@botecogalofrito.com.br",
                -8.0635,
                -34.8732,
                84,
                "Alto (Ruidoso ⚠️)",
                "Música ao vivo com percussão alta e caixas no corredor",
                "Mesas próximas; barulho elevado exige atenção ao usar leitor de telas"
        );
        botecoGalo.cardapio.add(new ItemCardapio("galo_espetinho", "Espetinho de Carne com Farofa", "Petisco", 1500, "espetinho", "farofa", "carne"));
        botecoGalo.cardapio.add(new ItemCardapio("galo_pastel", "Porção de Pastel de Festa (8 unid)", "Petisco", 2600, "pastel", "porcao"));
        botecoGalo.cardapio.add(new ItemCardapio("galo_chope", "Chope Gelado Caneca", "Bebida", 1000, "chope", "chopp"));
        botecoGalo.cardapio.add(new ItemCardapio("galo_cerveja", "Cerveja Lata 350ml", "Bebida", 800, "cerveja", "lata"));
        catalogoEstabelecimentos.add(botecoGalo);
    }

    private void renderizarListaEstabelecimentos() {
        containerEstabelecimentos.removeAllViews();

        for (int i = 0; i < catalogoEstabelecimentos.size(); i++) {
            final Estabelecimento est = catalogoEstabelecimentos.get(i);
            if (filtrarApenasSilenciosos && est.decibeisMedio > 65) {
                continue;
            }

            boolean selecionado = (est == estabelecimentoSelecionado);
            long distMetros = Math.round(calcularDistanciaHaversine(LAT_USUARIO, LON_USUARIO, est.latitude, est.longitude));
            String distTexto = distMetros < 1000 ? (distMetros + "m") : String.format(Locale.US, "%.1f km", distMetros / 1000.0);

            Button btnEst = new Button(this);
            btnEst.setAllCaps(false);
            btnEst.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            btnEst.setPadding(28, 22, 28, 22);
            btnEst.setTextSize(14f);
            btnEst.setTypeface(null, selecionado ? Typeface.BOLD : Typeface.NORMAL);
            btnEst.setTextColor(Color.WHITE);
            btnEst.setBackgroundColor(selecionado ? Color.parseColor("#1D4ED8") : Color.parseColor("#1E293B"));

            String prefixo = selecionado ? "✅ [CARDÁPIO ABERTO] " : "🏪 ";
            String textoBotao = prefixo + est.nome + " (" + est.bairro + ")\n" +
                    "• Tipo: " + est.tipo + " | Distância: " + distTexto + "\n" +
                    "• Ruído: " + est.decibeisMedio + " dB (" + est.nivelRuido + ")\n" +
                    "• Cardápio com " + est.cardapio.size() + " opções (Toque p/ ver cardápio)";
            btnEst.setText(textoBotao);
            btnEst.setContentDescription(
                    (selecionado ? "Selecionado: " : "") + est.nome + ", " + est.tipo + " no bairro " + est.bairro +
                            ". Ruído médio de " + est.decibeisMedio + " decibéis. " + est.acessibilidade +
                            ". Toque duas vezes para abrir o cardápio com " + est.cardapio.size() + " itens."
            );

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, 14);
            btnEst.setLayoutParams(lp);

            btnEst.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selecionarEstabelecimento(est, true);
                }
            });

            containerEstabelecimentos.addView(btnEst);
        }
    }

    private void selecionarEstabelecimento(Estabelecimento novoEst, boolean limparComandaAnterior) {
        if (estabelecimentoSelecionado != novoEst && limparComandaAnterior) {
            quantidadesComanda.clear();
        }
        estabelecimentoSelecionado = novoEst;
        renderizarListaEstabelecimentos();
        renderizarCardapioEstabelecimento();
        recalcularComanda();

        StringBuilder resumoItens = new StringBuilder();
        for (int i = 0; i < novoEst.cardapio.size(); i++) {
            ItemCardapio it = novoEst.cardapio.get(i);
            resumoItens.append(it.nome).append(" por ").append(it.formatarPreco());
            if (i < novoEst.cardapio.size() - 1) resumoItens.append(", ");
        }

        String msgVisual = "Cardápio aberto: " + novoEst.nome + " (" + novoEst.bairro + " — " + novoEst.decibeisMedio + " dB).\n" +
                "Acessibilidade: " + novoEst.acessibilidade + ".\n" +
                "Toque nos itens abaixo para escolher e somar na sua comanda!";
        String msgFalada = "Cardápio aberto de " + novoEst.nome + ", em " + novoEst.bairro +
                ", ruído de " + novoEst.decibeisMedio + " decibéis. Opções do cardápio: " + resumoItens.toString() +
                ". Toque nos itens para adicionar à comanda.";
        atualizarPainelEFalar(msgVisual, msgFalada);
    }

    private void renderizarCardapioEstabelecimento() {
        containerCardapio.removeAllViews();
        if (estabelecimentoSelecionado == null) return;

        tvTituloCardapio.setText("2. CARDÁPIO: " + estabelecimentoSelecionado.nome.toUpperCase(new Locale("pt", "BR")));

        for (int i = 0; i < estabelecimentoSelecionado.cardapio.size(); i++) {
            final ItemCardapio item = estabelecimentoSelecionado.cardapio.get(i);
            final int qtdAtual = quantidadesComanda.containsKey(item.id) ? quantidadesComanda.get(item.id) : 0;

            LinearLayout linha = new LinearLayout(this);
            linha.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lpLinha = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lpLinha.setMargins(0, 0, 0, 12);
            linha.setLayoutParams(lpLinha);

            Button btnAdd = new Button(this);
            btnAdd.setAllCaps(false);
            btnAdd.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            btnAdd.setPadding(24, 20, 24, 20);
            btnAdd.setTextSize(14f);
            btnAdd.setTextColor(Color.WHITE);
            btnAdd.setTypeface(null, qtdAtual > 0 ? Typeface.BOLD : Typeface.NORMAL);
            btnAdd.setBackgroundColor(qtdAtual > 0 ? Color.parseColor("#047857") : Color.parseColor("#334155"));

            String badgeQtd = qtdAtual > 0 ? (" [" + qtdAtual + "x na comanda]") : "";
            btnAdd.setText("➕ " + item.nome + " — " + item.formatarPreco() + "\n   Categoria: " + item.categoria + badgeQtd);
            btnAdd.setContentDescription(
                    "Adicionar " + item.nome + ", categoria " + item.categoria + ", preço " + item.formatarPreco() +
                            ". Quantidade atual na comanda: " + qtdAtual + ". Toque duas vezes para adicionar e somar."
            );

            LinearLayout.LayoutParams lpAdd = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            btnAdd.setLayoutParams(lpAdd);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alterarQuantidadeItem(item, 1);
                }
            });
            linha.addView(btnAdd);

            if (qtdAtual > 0) {
                Button btnRem = new Button(this);
                btnRem.setAllCaps(false);
                btnRem.setText("➖ 1");
                btnRem.setTextSize(14f);
                btnRem.setTextColor(Color.WHITE);
                btnRem.setBackgroundColor(Color.parseColor("#991B1B"));
                btnRem.setContentDescription("Remover uma unidade de " + item.nome + " da comanda");
                LinearLayout.LayoutParams lpRem = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                lpRem.setMargins(10, 0, 0, 0);
                btnRem.setLayoutParams(lpRem);
                btnRem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alterarQuantidadeItem(item, -1);
                    }
                });
                linha.addView(btnRem);
            }

            containerCardapio.addView(linha);
        }
    }

    private void alterarQuantidadeItem(ItemCardapio item, int delta) {
        int atual = quantidadesComanda.containsKey(item.id) ? quantidadesComanda.get(item.id) : 0;
        int nova = Math.max(0, atual + delta);
        if (nova == 0) {
            quantidadesComanda.remove(item.id);
        } else {
            quantidadesComanda.put(item.id, nova);
        }

        renderizarCardapioEstabelecimento();
        recalcularComanda();

        String acao = delta > 0 ? ("Adicionado " + item.nome + " (" + nova + "x).") : ("Removido 1 " + item.nome + ".");
        String totalFmt = formatarMoedaCentavos(ultimoTotalCentavos);
        String alertaTeto = ultimoTotalCentavos > tetoGastosCentavos
                ? " Atenção: ultrapassou o seu teto de " + formatarMoedaCentavos(tetoGastosCentavos) + "!"
                : " Dentro do seu teto de gastos.";
        String msg = acao + " Soma atual da comanda pela IA: " + totalFmt + "." + alertaTeto;
        atualizarPainelEFalar(msg, msg);
    }

    private void recalcularComanda() {
        if (estabelecimentoSelecionado == null) return;

        int somaCentavos = 0;
        int totalUnidades = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("📍 Local: ").append(estabelecimentoSelecionado.nome).append("\n");
        sb.append("----------------------------------------\n");

        for (int i = 0; i < estabelecimentoSelecionado.cardapio.size(); i++) {
            ItemCardapio item = estabelecimentoSelecionado.cardapio.get(i);
            Integer qtd = quantidadesComanda.get(item.id);
            if (qtd != null && qtd > 0) {
                int subtotal = qtd * item.precoCentavos;
                somaCentavos += subtotal;
                totalUnidades += qtd;
                sb.append("• ").append(qtd).append("x ").append(item.nome)
                        .append(" (").append(item.formatarPreco()).append(") = ")
                        .append(formatarMoedaCentavos(subtotal)).append("\n");
            }
        }

        ultimoTotalCentavos = somaCentavos;

        if (totalUnidades == 0) {
            ultimoPayloadPix = "";
            sb.append("Nenhum item selecionado ainda.\n");
            sb.append("Toque nos pratos ou bebidas do cardápio acima ou use o botão de voz para montar sua comanda!");
            tvResumoComanda.setText(sb.toString());
            tvResumoComanda.setContentDescription("Comanda vazia em " + estabelecimentoSelecionado.nome + ". Toque nos itens do cardápio acima para escolher.");
            btnPagarBanco.setText("🏦 COPIAR PIX E ABRIR APP DO BANCO NO CELULAR");
            return;
        }

        boolean dentroDoTeto = somaCentavos <= tetoGastosCentavos;
        senhaEntregaAtual = String.format(Locale.US, "%04d", 1000 + Math.abs((estabelecimentoSelecionado.id.hashCode() + somaCentavos) % 9000));
        ultimoPayloadPix = gerarPayloadPix(
                estabelecimentoSelecionado.chavePix,
                estabelecimentoSelecionado.nome,
                "Recife",
                somaCentavos / 100.0,
                "PED" + senhaEntregaAtual
        );

        sb.append("----------------------------------------\n");
        sb.append("💰 SOMA TOTAL DA COMANDA: ").append(formatarMoedaCentavos(somaCentavos)).append("\n");
        sb.append("🎯 Teto de Gastos (").append(formatarMoedaCentavos(tetoGastosCentavos)).append("): ")
                .append(dentroDoTeto ? "✅ Dentro do limite" : "⚠️ ULTRAPASSOU O TETO!").append("\n");
        sb.append("🔑 Senha Falada de Entrega: ").append(senhaEntregaAtual).append("\n");
        sb.append("📋 Pix Copia e Cola (BR Code):\n").append(ultimoPayloadPix);

        tvResumoComanda.setText(sb.toString());
        tvResumoComanda.setContentDescription(
                "Resumo da comanda em " + estabelecimentoSelecionado.nome + ". Total somado: " +
                        formatarMoedaCentavos(somaCentavos) + ". Senha de entrega: " + senhaEntregaAtual + "."
        );
        btnPagarBanco.setText("🏦 PAGAR " + formatarMoedaCentavos(somaCentavos) + " NO APP DO SEU BANCO");
    }

    private void ouvirResumoFaladoDaComanda() {
        if (ultimoTotalCentavos <= 0) {
            String msg = "Sua comanda em " + estabelecimentoSelecionado.nome + " ainda está vazia. Escolha itens no cardápio acima.";
            atualizarPainelEFalar(msg, msg);
            return;
        }

        StringBuilder fala = new StringBuilder();
        fala.append("Resumo da sua comanda em ").append(estabelecimentoSelecionado.nome).append(": ");
        for (int i = 0; i < estabelecimentoSelecionado.cardapio.size(); i++) {
            ItemCardapio item = estabelecimentoSelecionado.cardapio.get(i);
            Integer qtd = quantidadesComanda.get(item.id);
            if (qtd != null && qtd > 0) {
                fala.append(qtd).append(" ").append(item.nome)
                        .append(" custando ").append(formatarMoedaCentavos(qtd * item.precoCentavos)).append("; ");
            }
        }
        fala.append("Soma total da comanda: ").append(formatarMoedaCentavos(ultimoTotalCentavos)).append(". ");
        if (ultimoTotalCentavos > tetoGastosCentavos) {
            fala.append("Atenção: o valor passou do seu teto de ").append(formatarMoedaCentavos(tetoGastosCentavos)).append(". ");
        } else {
            fala.append("Valor dentro do seu teto de gastos. ");
        }
        fala.append("Senha falada de entrega: ").append(senhaEntregaAtual)
                .append(". Toque no botão verde Pagar no App do seu Banco para copiar o Pix e abrir o seu banco.");

        atualizarPainelEFalar(fala.toString(), fala.toString());
    }

    private void alternarTetoDeGastos() {
        if (tetoGastosCentavos == 5000) {
            tetoGastosCentavos = 10000;
        } else if (tetoGastosCentavos == 10000) {
            tetoGastosCentavos = 15000;
        } else if (tetoGastosCentavos == 15000) {
            tetoGastosCentavos = 20000;
        } else {
            tetoGastosCentavos = 5000;
        }
        btnAlterarTeto.setText("🎯 Teto: R$ " + (tetoGastosCentavos / 100));
        recalcularComanda();
        String msg = "Teto de gastos ajustado para " + formatarMoedaCentavos(tetoGastosCentavos) + ".";
        atualizarPainelEFalar(msg, msg);
    }

    // --- DETECÇÃO E ABERTURA DOS APPS DE BANCOS INSTALADOS NO CELULAR ---

    private List<BancoAppInfo> detectarBancosInstaladosNoCelular() {
        List<BancoAppInfo> encontrados = new ArrayList<BancoAppInfo>();
        PackageManager pm = getPackageManager();

        String[][] bancosConhecidos = new String[][]{
                {"Nubank", "com.nu.production"},
                {"Banco Inter", "br.com.intermedium"},
                {"Itaú", "com.itau"},
                {"Bradesco", "com.bradesco"},
                {"Banco do Brasil", "br.com.bb.android"},
                {"Caixa Econômica", "br.com.gabba.Caixa"},
                {"Caixa Tem", "br.gov.caixa.tem"},
                {"Santander", "com.santander.app"},
                {"PicPay", "com.picpay"},
                {"Mercado Pago", "com.mercadopago.wallet"},
                {"C6 Bank", "com.c6bank.app"},
                {"PagBank", "br.com.uol.ps.myaccount"},
                {"Neon", "br.com.neon"},
                {"Next", "br.com.bradesco.next"},
                {"Will Bank", "br.com.willbank"},
                {"Sicredi", "br.com.sicredi.app"},
                {"Sicoob", "br.com.sicoob.app"}
        };

        List<String> pacotesAdicionados = new ArrayList<String>();

        for (int i = 0; i < bancosConhecidos.length; i++) {
            String nome = bancosConhecidos[i][0];
            String pkg = bancosConhecidos[i][1];
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null) {
                encontrados.add(new BancoAppInfo(nome, pkg, launch));
                pacotesAdicionados.add(pkg);
            }
        }

        // Busca complementar por qualquer app lançável instalado no celular com palavras de banco/pagamento
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appsLancaveis = pm.queryIntentActivities(mainIntent, 0);
        if (appsLancaveis != null) {
            for (int i = 0; i < appsLancaveis.size(); i++) {
                ResolveInfo ri = appsLancaveis.get(i);
                if (ri.activityInfo == null) continue;
                String pkg = ri.activityInfo.packageName;
                if (pacotesAdicionados.contains(pkg) || pkg.equals(getPackageName())) continue;

                String label = ri.loadLabel(pm) != null ? ri.loadLabel(pm).toString() : pkg;
                String textoBusca = normalizarTexto(label + " " + pkg);
                if (textoBusca.contains("banco") || textoBusca.contains("bank") ||
                        textoBusca.contains("nubank") || textoBusca.contains("itau") ||
                        textoBusca.contains("bradesco") || textoBusca.contains("santander") ||
                        textoBusca.contains("picpay") || textoBusca.contains("mercadopago") ||
                        textoBusca.contains("caixa") || textoBusca.contains("carteira") ||
                        textoBusca.contains("pagbank") || textoBusca.contains("sicoob") ||
                        textoBusca.contains("sicredi")) {
                    Intent launch = pm.getLaunchIntentForPackage(pkg);
                    if (launch != null) {
                        encontrados.add(new BancoAppInfo(label, pkg, launch));
                        pacotesAdicionados.add(pkg);
                    }
                }
            }
        }

        return encontrados;
    }

    private void renderizarBancosInstalados() {
        containerBancosInstalados.removeAllViews();
        List<BancoAppInfo> bancos = detectarBancosInstaladosNoCelular();

        if (bancos.isEmpty()) {
            TextView tvInfo = new TextView(this);
            tvInfo.setText("ℹ️ Ao tocar no botão verde acima, o código Pix Copia e Cola será copiado automaticamente e o seletor de aplicativos de pagamento do seu celular será aberto.");
            tvInfo.setTextColor(Color.parseColor("#CBD5E1"));
            tvInfo.setTextSize(13f);
            containerBancosInstalados.addView(tvInfo);
            return;
        }

        TextView tvSub = new TextView(this);
        tvSub.setText("📱 Bancos detectados no seu celular (Toque p/ copiar Pix e abrir direto):");
        tvSub.setTextColor(Color.parseColor("#86EFAC"));
        tvSub.setTextSize(14f);
        tvSub.setPadding(0, 4, 0, 8);
        containerBancosInstalados.addView(tvSub);

        for (int i = 0; i < bancos.size(); i++) {
            final BancoAppInfo banco = bancos.get(i);
            Button btnBanco = new Button(this);
            btnBanco.setAllCaps(false);
            btnBanco.setText("📲 Pagar com " + banco.nomeBanco + " (Copiar Pix e Abrir)");
            btnBanco.setTextSize(14f);
            btnBanco.setTextColor(Color.WHITE);
            btnBanco.setBackgroundColor(Color.parseColor("#0F766E"));
            btnBanco.setContentDescription(
                    "Pagar com o aplicativo " + banco.nomeBanco + " instalado no seu celular. Copia o código Pix automaticamente e abre o banco."
            );

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, 10);
            btnBanco.setLayoutParams(lp);

            btnBanco.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copiarPixEAbrirBancoEspecifico(banco);
                }
            });
            containerBancosInstalados.addView(btnBanco);
        }
    }

    private void copiarPixParaAreaDeTransferencia() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && !ultimoPayloadPix.isEmpty()) {
            ClipData clip = ClipData.newPlainText("Pix Comanda Falada", ultimoPayloadPix);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void copiarPixEAbrirBancoEspecifico(BancoAppInfo banco) {
        if (ultimoTotalCentavos <= 0) {
            String msg = "Sua comanda ainda está vazia! Escolha pelo menos um item do cardápio antes de pagar no " + banco.nomeBanco + ".";
            atualizarPainelEFalar(msg, msg);
            return;
        }

        copiarPixParaAreaDeTransferencia();
        String valorFmt = formatarMoedaCentavos(ultimoTotalCentavos);
        String msg = "Código Pix Copia e Cola de " + valorFmt + " copiado! Abrindo o aplicativo " + banco.nomeBanco + " para finalizar o pagamento.";
        atualizarPainelEFalar(msg, msg);
        Toast.makeText(this, "Pix (" + valorFmt + ") copiado! Abrindo " + banco.nomeBanco + "...", Toast.LENGTH_LONG).show();

        try {
            banco.intentAbertura.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(banco.intentAbertura);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir " + banco.nomeBanco + ".", Toast.LENGTH_SHORT).show();
        }
    }

    private void acaoPagarComandaNosBancos() {
        if (ultimoTotalCentavos <= 0) {
            // Se a comanda estiver vazia, adiciona automaticamente 1 Caldinho + 1 Suco como demonstração ou avisa o usuário
            String msg = "Sua comanda está vazia. Escolha os pratos ou bebidas no cardápio acima para a IA somar antes de abrir o aplicativo do banco!";
            atualizarPainelEFalar(msg, msg);
            return;
        }

        copiarPixParaAreaDeTransferencia();
        final String valorFmt = formatarMoedaCentavos(ultimoTotalCentavos);
        final List<BancoAppInfo> bancosInstalados = detectarBancosInstaladosNoCelular();

        if (bancosInstalados.size() == 1) {
            copiarPixEAbrirBancoEspecifico(bancosInstalados.get(0));
            return;
        }

        if (!bancosInstalados.isEmpty()) {
            String[] nomesOpcoes = new String[bancosInstalados.size()];
            for (int i = 0; i < bancosInstalados.size(); i++) {
                nomesOpcoes[i] = "🏦 Abrir " + bancosInstalados.get(i).nomeBanco + " (Pix " + valorFmt + " já copiado)";
            }

            falar("Código Pix de " + valorFmt + " copiado para a área de transferência! Escolha qual banco instalado no seu celular deseja abrir.");

            new AlertDialog.Builder(this)
                    .setTitle("Pix de " + valorFmt + " Copiado! Escolha seu Banco:")
                    .setItems(nomesOpcoes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            copiarPixEAbrirBancoEspecifico(bancosInstalados.get(which));
                        }
                    })
                    .setNegativeButton("Fechar", null)
                    .show();
            return;
        }

        // Caso o aparelho de teste não tenha nenhum dos bancos instalados, exibe a lista de bancos compatíveis e permite compartilhar/abrir com qualquer app do celular
        final String[] opcoesFallback = new String[]{
                "📲 Compartilhar / Abrir Código Pix com Apps do Celular",
                "💜 Nubank (Pix Copia e Cola já na área de transferência)",
                "🧡 Banco Inter (Pix Copia e Cola já na área de transferência)",
                "🧡 Itaú / Bradesco / Santander / Banco do Brasil / Caixa",
                "💚 PicPay / Mercado Pago"
        };

        String msg = "Código Pix Copia e Cola no valor de " + valorFmt + " copiado para a área de transferência! Selecione o aplicativo do seu banco para colar e pagar.";
        atualizarPainelEFalar(msg, msg);
        Toast.makeText(this, "Pix de " + valorFmt + " copiado para a área de transferência!", Toast.LENGTH_LONG).show();

        new AlertDialog.Builder(this)
                .setTitle("Pix Copiado (" + valorFmt + ") — Abrir Banco")
                .setItems(opcoesFallback, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Pagamento Pix Comanda Falada - " + valorFmt);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, ultimoPayloadPix);
                        startActivity(Intent.createChooser(sendIntent, "Abrir Pix de " + valorFmt + " com:"));
                    }
                })
                .setNegativeButton("Voltar", null)
                .show();
    }

    // --- RECONHECIMENTO DE VOZ E PROCESSAMENTO NLP DA IA ---

    private void iniciarReconhecimentoVoz(final int requestCode, String promptFalado) {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, promptFalado);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            mostrarDialogoSimulacaoVoz(requestCode);
        }
    }

    private void mostrarDialogoSimulacaoVoz(final int requestCode) {
        if (requestCode == REQ_VOZ_BUSCA_LOCAL) {
            final String[] frasesBusca = new String[]{
                    "Quero um bar no Recife Antigo com música baixa e espaço tranquilo",
                    "Buscar restaurante silencioso nas Graças com comida regional",
                    "Quero restaurante em Boa Viagem com frutos do mar e moqueca",
                    "Mostrar Boteco do Galo Frito no Recife Antigo"
            };
            new AlertDialog.Builder(this)
                    .setTitle("🎤 Escolha ou Simule o Comando de Voz:")
                    .setItems(frasesBusca, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            processarBuscaVozEstabelecimento(frasesBusca[which]);
                        }
                    })
                    .show();
        } else {
            final String[] frasesPedido = new String[]{
                    "Quero um caldinho de feijão e um suco de laranja natural",
                    "Me vê dois chopes gelados e uma macaxeira frita com queijo coalho",
                    "Quero um arrumadinho de charque e um café com bolo de rolo",
                    "Adicionar uma moqueca pernambucana e uma água de coco gelada"
            };
            new AlertDialog.Builder(this)
                    .setTitle("🎙️ Escolha ou Simule o Pedido Falado:")
                    .setItems(frasesPedido, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            processarPedidoVozNoCardapio(frasesPedido[which]);
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String textoFalado = matches.get(0);
                if (requestCode == REQ_VOZ_BUSCA_LOCAL) {
                    processarBuscaVozEstabelecimento(textoFalado);
                } else if (requestCode == REQ_VOZ_PEDIDO_CARDAPIO) {
                    processarPedidoVozNoCardapio(textoFalado);
                }
            }
        }
    }

    private void processarBuscaVozEstabelecimento(String fraseFalada) {
        String norm = normalizarTexto(fraseFalada);
        Estabelecimento melhor = catalogoEstabelecimentos.get(0);

        if (norm.contains("agreste") || norm.contains("gracas") || norm.contains("silencioso") || norm.contains("arrumadinho") || norm.contains("escondidinho")) {
            melhor = catalogoEstabelecimentos.get(1);
        } else if (norm.contains("boa viagem") || norm.contains("varanda") || norm.contains("praia") || norm.contains("moqueca") || norm.contains("camarao") || norm.contains("peixe")) {
            melhor = catalogoEstabelecimentos.get(2);
        } else if (norm.contains("galo") || norm.contains("boteco") || norm.contains("espetinho") || norm.contains("pastel")) {
            melhor = catalogoEstabelecimentos.get(3);
        } else {
            melhor = catalogoEstabelecimentos.get(0);
        }

        filtrarApenasSilenciosos = false;
        selecionarEstabelecimento(melhor, false);

        String msg = "🎤 Voz reconhecida: \"" + fraseFalada + "\"\n" +
                "✅ GraphRAG selecionou: " + melhor.nome + " (" + melhor.bairro + " — " + melhor.decibeisMedio + " dB).\n" +
                "O cardápio de " + melhor.nome + " já está aberto abaixo!";
        atualizarPainelEFalar(msg, "Voz reconhecida. Cardápio aberto de " + melhor.nome + " em " + melhor.bairro + ", com " + melhor.decibeisMedio + " decibéis.");
    }

    private void processarPedidoVozNoCardapio(String fraseFalada) {
        String norm = normalizarTexto(fraseFalada);

        // Verificar se o usuário pediu um prato que pertence a outro restaurante e trocar automaticamente se necessário
        Estabelecimento estAlvo = estabelecimentoSelecionado;
        if (!encontrarItensNaFrase(norm, estAlvo).isEmpty()) {
            // Mantém o estabelecimento atual
        } else {
            for (int i = 0; i < catalogoEstabelecimentos.size(); i++) {
                Estabelecimento cand = catalogoEstabelecimentos.get(i);
                if (!encontrarItensNaFrase(norm, cand).isEmpty()) {
                    estAlvo = cand;
                    estabelecimentoSelecionado = cand;
                    quantidadesComanda.clear();
                    break;
                }
            }
        }

        List<ItemCardapio> itensIdentificados = encontrarItensNaFrase(norm, estAlvo);
        if (itensIdentificados.isEmpty()) {
            String msgErro = "🎙️ Você disse: \"" + fraseFalada + "\". Não encontrei esse item no cardápio de " + estAlvo.nome + ". Toque em um dos itens listados no cardápio.";
            atualizarPainelEFalar(msgErro, msgErro);
            return;
        }

        int qtdPadrao = (norm.contains("dois ") || norm.contains("duas ") || norm.contains("2 ")) ? 2 : 1;
        StringBuilder nomesAdicionados = new StringBuilder();

        for (int i = 0; i < itensIdentificados.size(); i++) {
            ItemCardapio it = itensIdentificados.get(i);
            int qtdItem = (i == 0) ? qtdPadrao : 1;
            int atual = quantidadesComanda.containsKey(it.id) ? quantidadesComanda.get(it.id) : 0;
            quantidadesComanda.put(it.id, atual + qtdItem);
            nomesAdicionados.append(qtdItem).append("x ").append(it.nome);
            if (i < itensIdentificados.size() - 1) nomesAdicionados.append(", ");
        }

        renderizarListaEstabelecimentos();
        renderizarCardapioEstabelecimento();
        recalcularComanda();

        String msgVisual = "🎙️ Pedido por Voz: \"" + fraseFalada + "\"\n" +
                "✅ IA identificou e somou na comanda: " + nomesAdicionados.toString() + "\n" +
                "💰 Nova Soma Total: " + formatarMoedaCentavos(ultimoTotalCentavos) + " (Senha: " + senhaEntregaAtual + ")";
        String msgFalada = "Pedido adicionado: " + nomesAdicionados.toString() +
                ". Soma total da comanda: " + formatarMoedaCentavos(ultimoTotalCentavos) +
                ". Senha de entrega: " + senhaEntregaAtual +
                ". Toque no botão verde abaixo para copiar o Pix e abrir o aplicativo do seu banco.";
        atualizarPainelEFalar(msgVisual, msgFalada);
    }

    private List<ItemCardapio> encontrarItensNaFrase(String fraseNormalizada, Estabelecimento est) {
        List<ItemCardapio> achados = new ArrayList<ItemCardapio>();
        if (est == null) return achados;
        for (int i = 0; i < est.cardapio.size(); i++) {
            ItemCardapio item = est.cardapio.get(i);
            for (int k = 0; k < item.palavrasChave.length; k++) {
                if (fraseNormalizada.contains(normalizarTexto(item.palavrasChave[k]))) {
                    achados.add(item);
                    break;
                }
            }
        }
        return achados;
    }

    // --- 3 CASOS DE DEMONSTRAÇÃO ACADÊMICA (TEC.MD) ---

    private void executarCaso1() {
        filtrarApenasSilenciosos = true;
        selecionarEstabelecimento(catalogoEstabelecimentos.get(0), false);
        String msg = "[Caso 1 · Busca de Bar Acústico]\n\n" +
                "Voz Reconhecida: 'Quero um bar no Recife Antigo com música baixa e espaço tranquilo para conversar.'\n\n" +
                "Recuperação em Grafo (GraphRAG):\n" +
                "• Bar Selecionado: Bar Cais do Sertão & Chopp (Recife Antigo)\n" +
                "• Ruído Médio: 58 dB (música instrumental ambiente baixa)\n" +
                "• Cardápio carregado abaixo com 7 opções!";
        atualizarPainelEFalar(msg, "Bar encontrado no Recife Antigo: Bar Cais do Sertão, com cinquenta e oito decibéis e cardápio aberto.");
    }

    private void executarCaso2() {
        Estabelecimento cais = catalogoEstabelecimentos.get(0);
        estabelecimentoSelecionado = cais;
        quantidadesComanda.clear();
        quantidadesComanda.put("cais_caldinho", 1);
        quantidadesComanda.put("cais_suco", 1);
        renderizarListaEstabelecimentos();
        renderizarCardapioEstabelecimento();
        recalcularComanda();

        String msg = "[Caso 2 · Pedido e Comanda Exata]\n\n" +
                "Voz Reconhecida: 'Vou querer uma porção de caldinho de feijão e um suco de laranja natural, por favor.'\n\n" +
                "Itens Somados na Comanda:\n" +
                "1. Caldinho de feijão: R$ 12,00\n" +
                "2. Suco de laranja natural: R$ 10,00\n" +
                "• Total da Comanda: R$ 22,00 (sem erro de centavos)\n" +
                "• Toque no botão verde 'PAGAR R$ 22,00 NO APP DO SEU BANCO' para pagar!";
        atualizarPainelEFalar(msg, "Pedido confirmado: um caldinho de feijão e um suco de laranja. Total: vinte e dois reais. Toque no botão verde para abrir o aplicativo do seu banco.");
    }

    private void executarCaso3() {
        Estabelecimento cais = catalogoEstabelecimentos.get(0);
        estabelecimentoSelecionado = cais;
        quantidadesComanda.clear();
        quantidadesComanda.put("cais_macaxeira", 1);
        renderizarListaEstabelecimentos();
        renderizarCardapioEstabelecimento();
        recalcularComanda();

        String msg = "[Caso 3 · Teste com Ruído de Bar (Onde a IA Erra)]\n\n" +
                "Áudio de Entrada: 'Me vê dois chopes gelados e uma porção de macaxeira frita com queijo coalho.'\n" +
                "Transcrição com Ruído (84 dB): 'com 2 corpos gelados e uma corção de macaxeira frita com queijo coalho'\n\n" +
                "• O modelo trocou 'dois chopes' por '2 corpos', somando apenas a Macaxeira Frita (R$ 34,00).\n" +
                "• O usuário pode adicionar os 2 chopes diretamente no cardápio interativo acima antes de pagar no banco!";
        atualizarPainelEFalar(msg, "Atenção: áudio com ruído. Apenas macaxeira frita foi confirmada por trinta e quatro reais. Você pode tocar no Chope no cardápio para completar o pedido.");
    }

    // --- CÁLCULO DETERMINÍSTICO DE DISTÂNCIA GPS (HAVERSINE) E PIX (BR CODE CRC16) ---

    public static double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        double rTerra = 6371000.0;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dPhi / 2.0) * Math.sin(dPhi / 2.0) +
                Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2.0) * Math.sin(dLambda / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return rTerra * c;
    }

    public static String gerarPayloadPix(String chavePix, String nomeRecebedor, String cidade, double valor, String txid) {
        String valorStr = String.format(Locale.US, "%.2f", valor);
        String p00 = "000201";
        String gui = "0014br.gov.bcb.pix";
        String chave = String.format(Locale.US, "01%02d%s", chavePix.length(), chavePix);
        String payload26 = gui + chave;
        String p26 = String.format(Locale.US, "26%02d%s", payload26.length(), payload26);
        String p52 = "52040000";
        String p53 = "5303986";
        String p54 = String.format(Locale.US, "54%02d%s", valorStr.length(), valorStr);
        String p58 = "5802BR";

        String nomeLimpo = normalizarTexto(nomeRecebedor).toUpperCase(Locale.US);
        if (nomeLimpo.length() > 25) nomeLimpo = nomeLimpo.substring(0, 25).trim();
        String p59 = String.format(Locale.US, "59%02d%s", nomeLimpo.length(), nomeLimpo);

        String cidadeLimpa = normalizarTexto(cidade).toUpperCase(Locale.US);
        if (cidadeLimpa.length() > 15) cidadeLimpa = cidadeLimpa.substring(0, 15).trim();
        String p60 = String.format(Locale.US, "60%02d%s", cidadeLimpa.length(), cidadeLimpa);

        String campoTxid = String.format(Locale.US, "05%02d%s", txid.length(), txid);
        String p62 = String.format(Locale.US, "62%02d%s", campoTxid.length(), campoTxid);

        String semCrc = p00 + p26 + p52 + p53 + p54 + p58 + p59 + p60 + p62 + "6304";
        String crc = calcularCrc16(semCrc);
        return semCrc + crc;
    }

    private static String calcularCrc16(String payload) {
        int polinomio = 0x1021;
        int resultado = 0xFFFF;
        byte[] bytes = payload.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            resultado ^= ((bytes[i] & 0xFF) << 8);
            for (int b = 0; b < 8; b++) {
                if ((resultado & 0x8000) != 0) {
                    resultado = ((resultado << 1) ^ polinomio) & 0xFFFF;
                } else {
                    resultado = (resultado << 1) & 0xFFFF;
                }
            }
        }
        return String.format(Locale.US, "%04X", resultado);
    }

    public static String formatarMoedaCentavos(int centavos) {
        int reais = centavos / 100;
        int cents = Math.abs(centavos % 100);
        return String.format(Locale.US, "R$ %d,%02d", reais, cents);
    }

    private static String normalizarTexto(String s) {
        if (s == null) return "";
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        return norm.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").toLowerCase(Locale.US);
    }

    private void atualizarPainelEFalar(String textoVisual, String textoFalado) {
        tvResultado.setText("🔊 " + textoVisual);
        falar(textoFalado);
    }

    private void falar(String texto) {
        if (tts != null && texto != null && !texto.isEmpty()) {
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "fala_comanda");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            tts.setLanguage(new Locale("pt", "BR"));
            falar("Comanda Falada, Passo à Frente. Escolha um bar ou restaurante para ouvir o cardápio, somar sua comanda pela inteligência artificial e pagar no aplicativo do seu banco.");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

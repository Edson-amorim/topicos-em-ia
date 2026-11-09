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
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int REQ_VOZ_BUSCA_LOCAL = 101;
    private static final int REQ_VOZ_PEDIDO_CARDAPIO = 102;

    // Coordenadas de referência (Marco Zero / Recife Antigo)
    private static final double LAT_USUARIO = -8.0628;
    private static final double LON_USUARIO = -34.8714;

    public static class EstadoInfo {
        public final String nome;
        public final String uf;
        public final String capital;
        public final String regiao;
        public final double lat;
        public final double lon;
        public final String[] cidadesPrincipais;

        public EstadoInfo(String nome, String uf, String capital, String regiao, double lat, double lon, String... cidadesPrincipais) {
            this.nome = nome;
            this.uf = uf;
            this.capital = capital;
            this.regiao = regiao;
            this.lat = lat;
            this.lon = lon;
            this.cidadesPrincipais = (cidadesPrincipais != null && cidadesPrincipais.length > 0)
                    ? cidadesPrincipais
                    : new String[]{capital};
        }
    }

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
        public final String regiao;
        public final String estado;
        public final String uf;
        public final String cidade;
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
                String regiao,
                String estado,
                String uf,
                String cidade,
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
            this.regiao = regiao;
            this.estado = estado;
            this.uf = uf;
            this.cidade = cidade;
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

    private static final String[] REGIOES_BRASIL = new String[]{
            "Todas as Regiões",
            "Nordeste",
            "Sudeste",
            "Sul",
            "Centro-Oeste",
            "Norte"
    };

    private static final String[] CATEGORIAS_ESTABELECIMENTO = new String[]{
            "Todas as Categorias (Para Comer e Consumir)",
            "Restaurantes, Bistrôs e Self-Service",
            "Bares, Botecos e Petiscarias",
            "Padarias, Cafeterias e Confeitarias",
            "Pizzarias, Hamburguerias e Lanchonetes",
            "Churrascarias, Espetinhos e Peixarias",
            "Sorveterias, Açaí, Docerias e Tapiocarias",
            "Mercados Gastronômicos, Quiosques e Bistrôs Culturais"
    };

    private static final String[] TODAS_CIDADES_PERNAMBUCO = new String[]{
            "Recife", "Olinda", "Jaboatão dos Guararapes", "Caruaru", "Petrolina",
            "Paulista", "Cabo de Santo Agostinho", "Camaragibe", "Garanhuns", "Vitória de Santo Antão",
            "Igarassu", "São Lourenço da Mata", "Santa Cruz do Capibaribe", "Abreu e Lima", "Ipojuca",
            "Serra Talhada", "Araripina", "Gravatá", "Carpina", "Goiana",
            "Belo Jardim", "Arcoverde", "Ouricuri", "Escada", "Pesqueira",
            "Surubim", "Palmares", "Moreno", "Bezerros", "Salgueiro",
            "São Bento do Una", "Buíque", "Limoeiro", "Paudalho", "Timbaúba",
            "Brejo da Madre de Deus", "Bom Conselho", "Ribeirão", "Toritama", "Barreiros",
            "Águas Belas", "Lajedo", "Santa Maria da Boa Vista", "Afogados da Ingazeira", "Aliança",
            "Agrestina", "Água Preta", "Afrânio", "Alagoinha", "Altinho",
            "Amaraji", "Angelim", "Araçoiaba", "Barra de Guabiraba", "Belém de Maria",
            "Belém do São Francisco", "Betânia", "Bodocó", "Bom Jardim", "Bonito",
            "Brejão", "Brejinho", "Buenos Aires", "Cabrobó", "Cachoeirinha",
            "Caetés", "Calçado", "Calumbi", "Camocim de São Félix", "Camutanga",
            "Canhotinho", "Capoeiras", "Carnaíba", "Carnaubeira da Penha", "Casinhas",
            "Catende", "Cedro", "Chã de Alegria", "Chã Grande", "Condado",
            "Correntes", "Cortês", "Cumaru", "Cupira", "Custódia",
            "Dormentes", "Exu", "Feira Nova", "Fernando de Noronha", "Ferreiros",
            "Flores", "Floresta", "Frei Miguelinho", "Gameleira", "Glória do Goitá",
            "Granito", "Iati", "Ibimirim", "Ibirajuba", "Iguaracy",
            "Inajá", "Ingazeira", "Ipubi", "Itacuruba", "Itaíba",
            "Itamaracá (Ilha de Itamaracá)", "Itambé", "Itapetim", "Itapissuma", "Itaquitinga",
            "Jaqueira", "Jataúba", "Jatobá", "João Alfredo", "Joaquim Nabuco",
            "Jucati", "Jupi", "Jurema", "Lagoa de Itaenga", "Lagoa do Carro",
            "Lagoa do Ouro", "Lagoa dos Gatos", "Lagoa Grande", "Machados", "Manari",
            "Maraial", "Mirandiba", "Moreilândia", "Nazaré da Mata", "Orobó",
            "Orocó", "Palmeirina", "Panelas", "Paranatama", "Parnamirim",
            "Passira", "Pedra", "Petrolândia", "Poção", "Pombos",
            "Primavera", "Quipapá", "Quixaba", "Riacho das Almas", "Rio Formoso",
            "Sairé", "Salgadinho", "Salgueiro", "Saloá", "Sanharó",
            "Santa Cruz", "Santa Cruz da Baixa Verde", "Santa Filomena", "Santa Maria do Cambucá", "Santa Terezinha",
            "São Benedito do Sul", "São Caitano", "São João", "São Joaquim do Monte", "São José da Coroa Grande",
            "São José do Belmonte", "São José do Egito", "São Vicente Férrer", "Serrita", "Sertânia",
            "Sirinhaém", "Solidão", "Tabira", "Tacaimbó", "Tacaratu",
            "Tamandaré", "Taquaritinga do Norte", "Terezinha", "Terra Nova", "Tracunhaém",
            "Trindade", "Triunfo", "Tupanatinga", "Tuparetama", "Venturosa",
            "Verdejante", "Vertente do Lério", "Vertentes", "Vicência", "Xexéu"
    };

    private static final EstadoInfo[] ESTADOS_BRASIL = new EstadoInfo[]{
            // Nordeste
            new EstadoInfo("Pernambuco", "PE", "Recife", "Nordeste", -8.0617, -34.8711,
                    TODAS_CIDADES_PERNAMBUCO),
            new EstadoInfo("Bahia", "BA", "Salvador", "Nordeste", -12.9714, -38.5086,
                    "Salvador", "Feira de Santana", "Vitória da Conquista", "Camaçari", "Juazeiro", "Itabuna", "Lauro de Freitas", "Ilhéus", "Jequié", "Teixeira de Freitas", "Barreiras", "Alagoinhas", "Porto Seguro", "Simões Filho", "Paulo Afonso", "Eunápolis", "Santo Antônio de Jesus", "Valença", "Candeias", "Guanambi", "Jacobina", "Serrinha", "Senhor do Bonfim", "Dias d'Ávila"),
            new EstadoInfo("Ceará", "CE", "Fortaleza", "Nordeste", -3.7258, -38.4984,
                    "Fortaleza", "Caucaia", "Juazeiro do Norte", "Maracanaú", "Sobral", "Crato", "Itapipoca", "Maranguape", "Iguatu", "Quixadá", "Pacatuba", "Aquiraz", "Quixeramobim", "Canindé", "Russas", "Tianguá", "Crateús", "Aracati", "Cascavel", "Pacajus", "Icó", "Horizonte", "Camocim", "Jericoacoara"),
            new EstadoInfo("Paraíba", "PB", "João Pessoa", "Nordeste", -7.1195, -34.8450,
                    "João Pessoa", "Campina Grande", "Santa Rita", "Patos", "Bayeux", "Sousa", "Cabedelo", "Cajazeiras", "Guarabira", "Sapé", "Mamanguape", "Queimadas", "São Bento", "Monteiro", "Esperança", "Pombal", "Catolé do Rocha", "Conde", "Areia", "Bananeiras"),
            new EstadoInfo("Rio Grande do Norte", "RN", "Natal", "Nordeste", -5.7945, -35.2110,
                    "Natal", "Mossoró", "Parnamirim", "São Gonçalo do Amarante", "Macaíba", "Ceará-Mirim", "Caicó", "Assu", "Currais Novos", "São José de Mipibu", "Santa Cruz", "Nova Cruz", "Apodi", "João Câmara", "Pau dos Ferros", "Tibau do Sul (Pipa)", "Touros"),
            new EstadoInfo("Alagoas", "AL", "Maceió", "Nordeste", -9.6658, -35.7350,
                    "Maceió", "Arapiraca", "Rio Largo", "Palmeira dos Índios", "União dos Palmares", "Penedo", "São Miguel dos Campos", "Campo Alegre", "Delmiro Gouveia", "Coruripe", "Marechal Deodoro", "Santana do Ipanema", "Maragogi", "Piranhas"),
            new EstadoInfo("Sergipe", "SE", "Aracaju", "Nordeste", -10.9472, -37.0731,
                    "Aracaju", "Nossa Senhora do Socorro", "Lagarto", "Itabaiana", "São Cristóvão", "Estância", "Tobias Barreto", "Itabaianinha", "Simão Dias", "Nossa Senhora da Glória", "Propriá", "Canindé de São Francisco"),
            new EstadoInfo("Maranhão", "MA", "São Luís", "Nordeste", -2.5307, -44.3068,
                    "São Luís", "Imperatriz", "São José de Ribamar", "Timon", "Caxias", "Paço do Lumiar", "Codó", "Açailândia", "Bacabal", "Balsas", "Santa Inês", "Barra do Corda", "Pinheiro", "Chapadinha", "Barreirinhas"),
            new EstadoInfo("Piauí", "PI", "Teresina", "Nordeste", -5.0892, -42.8016,
                    "Teresina", "Parnaíba", "Picos", "Piripiri", "Floriano", "Barras", "Campo Maior", "União", "Altos", "Esperantina", "José de Freitas", "Pedro II", "Oeiras", "São Raimundo Nonato", "Luís Correia"),
            // Sudeste
            new EstadoInfo("São Paulo", "SP", "São Paulo", "Sudeste", -23.5558, -46.6896,
                    "São Paulo", "Campinas", "Santos", "Ribeirão Preto", "São José dos Campos", "Sorocaba", "Guarulhos"),
            new EstadoInfo("Rio de Janeiro", "RJ", "Rio de Janeiro", "Sudeste", -22.9711, -43.1825,
                    "Rio de Janeiro", "Niterói", "Búzios", "Petrópolis", "Paraty", "Cabo Frio"),
            new EstadoInfo("Minas Gerais", "MG", "Belo Horizonte", "Sudeste", -19.9386, -43.9352,
                    "Belo Horizonte", "Uberlândia", "Ouro Preto", "Tiradentes", "Juiz de Fora", "Poços de Caldas"),
            new EstadoInfo("Espírito Santo", "ES", "Vitória", "Sudeste", -20.3155, -40.3128,
                    "Vitória", "Vila Velha", "Guarapari", "Serra", "Domingos Martins"),
            // Sul
            new EstadoInfo("Rio Grande do Sul", "RS", "Porto Alegre", "Sul", -30.0392, -51.2214,
                    "Porto Alegre", "Gramado", "Canela", "Caxias do Sul", "Pelotas", "Bento Gonçalves"),
            new EstadoInfo("Paraná", "PR", "Curitiba", "Sul", -25.4284, -49.2733,
                    "Curitiba", "Foz do Iguaçu", "Londrina", "Maringá", "Ponta Grossa"),
            new EstadoInfo("Santa Catarina", "SC", "Florianópolis", "Sul", -27.5954, -48.5480,
                    "Florianópolis", "Balneário Camboriú", "Blumenau", "Joinville", "Itajaí"),
            // Centro-Oeste
            new EstadoInfo("Distrito Federal", "DF", "Brasília", "Centro-Oeste", -15.8195, -47.9022,
                    "Brasília", "Taguatinga", "Águas Claras", "Guará"),
            new EstadoInfo("Goiás", "GO", "Goiânia", "Centro-Oeste", -16.6869, -49.2648,
                    "Goiânia", "Caldas Novas", "Pirenópolis", "Anápolis", "Rio Verde"),
            new EstadoInfo("Mato Grosso", "MT", "Cuiabá", "Centro-Oeste", -15.6014, -56.0979,
                    "Cuiabá", "Chapada dos Guimarães", "Rondonópolis", "Sinop"),
            new EstadoInfo("Mato Grosso do Sul", "MS", "Campo Grande", "Centro-Oeste", -20.4697, -54.6201,
                    "Campo Grande", "Bonito", "Dourados", "Corumbá"),
            // Norte
            new EstadoInfo("Pará", "PA", "Belém", "Norte", -1.4485, -48.5002,
                    "Belém", "Santarém", "Alter do Chão", "Ananindeua", "Marabá"),
            new EstadoInfo("Amazonas", "AM", "Manaus", "Norte", -3.1190, -60.0217,
                    "Manaus", "Parintins", "Itacoatiara", "Manacapuru"),
            new EstadoInfo("Tocantins", "TO", "Palmas", "Norte", -10.1840, -48.3336,
                    "Palmas", "Araguaína", "Gurupi"),
            new EstadoInfo("Rondônia", "RO", "Porto Velho", "Norte", -8.7619, -63.9039,
                    "Porto Velho", "Ji-Paraná", "Ariquemes"),
            new EstadoInfo("Acre", "AC", "Rio Branco", "Norte", -9.9754, -67.8249,
                    "Rio Branco", "Cruzeiro do Sul"),
            new EstadoInfo("Amapá", "AP", "Macapá", "Norte", 0.0349, -51.0694,
                    "Macapá", "Santana"),
            new EstadoInfo("Roraima", "RR", "Boa Vista", "Norte", 2.8235, -60.6758,
                    "Boa Vista", "Rorainópolis")
    };

    private TextView tvResultado;
    private TextView tvTituloCardapio;
    private TextView tvResumoComanda;
    private EditText etBuscaQualquerBar;
    private LinearLayout containerEstabelecimentos;
    private LinearLayout containerCardapio;
    private LinearLayout containerBancosInstalados;
    private Button btnFiltroRegiao;
    private Button btnFiltroEstado;
    private Button btnFiltroCidade;
    private Button btnFiltroCategoria;
    private Button btnGoogleMapsRota;
    private Button btnGoogleMapsExplorar;
    private Button btnImportarBarDoMaps;
    private Button btnLocalizarBarTexto;
    private Button btnFiltroAcustico;
    private Button btnFiltroTodos;
    private Button btnBuscarVozLocal;
    private Button btnPedirPorVoz;
    private Button btnTaxaServico;
    private Button btnOuvirComanda;
    private Button btnAlterarTeto;
    private Button btnLimparComanda;
    private Button btnPagarBanco;
    private Button btnCaso1;
    private Button btnCaso2;
    private Button btnCaso3;

    private AccessibilityManager accessibilityManager;
    private final List<Estabelecimento> catalogoEstabelecimentos = new ArrayList<Estabelecimento>();
    private Estabelecimento estabelecimentoSelecionado;

    private String regiaoFiltroAtual = "Todas as Regiões";
    private String ufFiltroAtual = "TODOS";
    private String cidadeFiltroAtual = "TODAS";
    private String categoriaFiltroAtual = "Todas as Categorias (Para Comer e Consumir)";
    private String termoBuscaAtual = "";
    private boolean filtrarApenasSilenciosos = false;

    private final Map<String, Integer> quantidadesComanda = new LinkedHashMap<String, Integer>();
    private int tetoGastosCentavos = 10000; // R$ 100,00 padrão
    private boolean adicionarTaxaDezPorCento = false;
    private int ultimoSubtotalCentavos = 0;
    private int ultimaTaxaServicoCentavos = 0;
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
        etBuscaQualquerBar = findViewById(R.id.etBuscaQualquerBar);
        containerEstabelecimentos = findViewById(R.id.containerEstabelecimentos);
        containerCardapio = findViewById(R.id.containerCardapio);
        containerBancosInstalados = findViewById(R.id.containerBancosInstalados);

        btnFiltroRegiao = findViewById(R.id.btnFiltroRegiao);
        btnFiltroEstado = findViewById(R.id.btnFiltroEstado);
        btnFiltroCidade = findViewById(R.id.btnFiltroCidade);
        btnFiltroCategoria = findViewById(R.id.btnFiltroCategoria);
        btnGoogleMapsRota = findViewById(R.id.btnGoogleMapsRota);
        btnGoogleMapsExplorar = findViewById(R.id.btnGoogleMapsExplorar);
        btnImportarBarDoMaps = findViewById(R.id.btnImportarBarDoMaps);
        btnLocalizarBarTexto = findViewById(R.id.btnLocalizarBarTexto);
        btnFiltroAcustico = findViewById(R.id.btnFiltroAcustico);
        btnFiltroTodos = findViewById(R.id.btnFiltroTodos);
        btnBuscarVozLocal = findViewById(R.id.btnBuscarVozLocal);
        btnPedirPorVoz = findViewById(R.id.btnPedirPorVoz);
        btnTaxaServico = findViewById(R.id.btnTaxaServico);
        btnOuvirComanda = findViewById(R.id.btnOuvirComanda);
        btnAlterarTeto = findViewById(R.id.btnAlterarTeto);
        btnLimparComanda = findViewById(R.id.btnLimparComanda);
        btnPagarBanco = findViewById(R.id.btnPagarBanco);
        btnCaso1 = findViewById(R.id.btnCaso1);
        btnCaso2 = findViewById(R.id.btnCaso2);
        btnCaso3 = findViewById(R.id.btnCaso3);

        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        carregarGrafoNacionalDeBares();
        estabelecimentoSelecionado = catalogoEstabelecimentos.get(0);

        btnFiltroRegiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogoFiltroRegiao();
            }
        });

        btnFiltroEstado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogoFiltroEstado();
            }
        });

        btnFiltroCidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogoFiltroCidade();
            }
        });

        btnFiltroCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogoFiltroCategoria();
            }
        });

        btnGoogleMapsRota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (estabelecimentoSelecionado != null) {
                    abrirBarNoGoogleMaps(estabelecimentoSelecionado, true);
                }
            }
        });

        btnGoogleMapsExplorar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explorarBaresDaCidadeNoGoogleMaps();
            }
        });

        btnImportarBarDoMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirFluxoBarVindoDoGoogleMaps();
            }
        });

        btnLocalizarBarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String digitado = etBuscaQualquerBar.getText() != null ? etBuscaQualquerBar.getText().toString().trim() : "";
                if (digitado.isEmpty()) {
                    abrirDialogoSugestoesDeBusca();
                } else {
                    localizarQualquerBarComCardapio(digitado);
                }
            }
        });

        btnFiltroAcustico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarApenasSilenciosos = true;
                renderizarListaEstabelecimentos();
                String msg = "Filtro acústico ativado: exibindo apenas bares e restaurantes com conforto acústico (até 65 decibéis).";
                atualizarPainelEFalar(msg, msg);
            }
        });

        btnFiltroTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regiaoFiltroAtual = "Todas as Regiões";
                ufFiltroAtual = "TODOS";
                cidadeFiltroAtual = "TODAS";
                categoriaFiltroAtual = "Todas as Categorias (Para Comer e Consumir)";
                termoBuscaAtual = "";
                filtrarApenasSilenciosos = false;
                etBuscaQualquerBar.setText("");
                atualizarRotulosFiltros();
                renderizarListaEstabelecimentos();
                String msg = "Todos os filtros limpos. Exibindo apenas estabelecimentos onde o usuário pode comer e consumir com cardápio disponível.";
                atualizarPainelEFalar(msg, msg);
            }
        });

        btnBuscarVozLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarReconhecimentoVoz(
                        REQ_VOZ_BUSCA_LOCAL,
                        "Fale o nome de qualquer bar, restaurante, cidade, estado ou região do Brasil"
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

        btnTaxaServico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                perguntarTaxaDezPorCento(false, null);
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

        atualizarRotulosFiltros();
        renderizarListaEstabelecimentos();
        renderizarCardapioEstabelecimento();
        recalcularComanda();
        renderizarBancosInstalados();

        // Verifica se o aplicativo foi aberto a partir do Google Maps (Compartilhar ou Link de Mapa)
        tratarIntentRecebidaDoGoogleMaps(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        tratarIntentRecebidaDoGoogleMaps(intent);
    }

    // --- SELETORES DE FILTRO POR REGIÃO, ESTADO (UF) E CIDADE ---

    private void abrirDialogoFiltroRegiao() {
        falar("Escolha a região do Brasil para filtrar os bares e restaurantes.");
        new AlertDialog.Builder(this)
                .setTitle("🗺️ Filtrar por Região do Brasil:")
                .setItems(REGIOES_BRASIL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        regiaoFiltroAtual = REGIOES_BRASIL[which];
                        ufFiltroAtual = "TODOS";
                        cidadeFiltroAtual = "TODAS";
                        termoBuscaAtual = "";
                        atualizarRotulosFiltros();
                        renderizarListaEstabelecimentos();

                        List<Estabelecimento> visiveis = obterEstabelecimentosFiltrados();
                        if (!visiveis.isEmpty()) {
                            selecionarEstabelecimento(visiveis.get(0), true);
                        }
                        String msg = "Região selecionada: " + regiaoFiltroAtual + ". Encontrados " + visiveis.size() +
                                " bares com cardápio. Você também pode filtrar por Estado e por Cidade.";
                        atualizarPainelEFalar(msg, msg);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirDialogoFiltroEstado() {
        final List<EstadoInfo> estadosDisponiveis = new ArrayList<EstadoInfo>();
        for (int i = 0; i < ESTADOS_BRASIL.length; i++) {
            EstadoInfo st = ESTADOS_BRASIL[i];
            if (regiaoFiltroAtual.equals("Todas as Regiões") || st.regiao.equalsIgnoreCase(regiaoFiltroAtual)) {
                estadosDisponiveis.add(st);
            }
        }

        final String[] opcoes = new String[estadosDisponiveis.size() + 1];
        opcoes[0] = "🏳️ Todos os Estados (" + regiaoFiltroAtual + ")";
        for (int i = 0; i < estadosDisponiveis.size(); i++) {
            EstadoInfo st = estadosDisponiveis.get(i);
            opcoes[i + 1] = st.nome + " (" + st.uf + ") — Região " + st.regiao;
        }

        falar("Escolha o Estado para filtrar os bares e as cidades.");
        new AlertDialog.Builder(this)
                .setTitle("🏳️ Filtrar por Estado (UF):")
                .setItems(opcoes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            ufFiltroAtual = "TODOS";
                            cidadeFiltroAtual = "TODAS";
                            atualizarRotulosFiltros();
                            renderizarListaEstabelecimentos();
                            String msg = "Exibindo bares de todos os estados em " + regiaoFiltroAtual + ".";
                            atualizarPainelEFalar(msg, msg);
                            return;
                        }

                        EstadoInfo escolhido = estadosDisponiveis.get(which - 1);
                        ufFiltroAtual = escolhido.uf;
                        regiaoFiltroAtual = escolhido.regiao;
                        cidadeFiltroAtual = "TODAS";
                        termoBuscaAtual = "";

                        garantirBarComCardapioNaCidade(escolhido, escolhido.capital);

                        atualizarRotulosFiltros();
                        renderizarListaEstabelecimentos();

                        List<Estabelecimento> visiveis = obterEstabelecimentosFiltrados();
                        if (!visiveis.isEmpty()) {
                            selecionarEstabelecimento(visiveis.get(0), true);
                        }
                        String msg = "Estado selecionado: " + escolhido.nome + " (" + escolhido.uf + "). Agora você pode tocar no filtro de Cidade para escolher entre " +
                                escolhido.cidadesPrincipais.length + " cidades de " + escolhido.nome + "!";
                        atualizarPainelEFalar(msg, msg);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirDialogoFiltroCidade() {
        exibirDialogoCidadesFiltradas("");
    }

    private void exibirDialogoCidadesFiltradas(final String filtroDigitado) {
        final List<String> cidadesLista = new ArrayList<String>();
        final List<EstadoInfo> estadoDeCadaCidade = new ArrayList<EstadoInfo>();
        final String normFiltro = normalizarTexto(filtroDigitado);

        for (int i = 0; i < ESTADOS_BRASIL.length; i++) {
            EstadoInfo st = ESTADOS_BRASIL[i];
            if (!ufFiltroAtual.equals("TODOS") && !st.uf.equalsIgnoreCase(ufFiltroAtual)) {
                continue;
            }
            if (!regiaoFiltroAtual.equals("Todas as Regiões") && !st.regiao.equalsIgnoreCase(regiaoFiltroAtual)) {
                continue;
            }
            for (int c = 0; c < st.cidadesPrincipais.length; c++) {
                String nomeCid = st.cidadesPrincipais[c];
                if (!normFiltro.isEmpty() && !normalizarTexto(nomeCid).contains(normFiltro)) {
                    continue;
                }
                cidadesLista.add(nomeCid);
                estadoDeCadaCidade.add(st);
            }
        }

        final String[] opcoes = new String[cidadesLista.size() + 2];
        opcoes[0] = "🔎 Pesquisar / Digitar Nome da Cidade...";
        opcoes[1] = "🏙️ Todas as Cidades";
        for (int i = 0; i < cidadesLista.size(); i++) {
            EstadoInfo st = estadoDeCadaCidade.get(i);
            opcoes[i + 2] = cidadesLista.get(i) + " (" + st.uf + ")";
        }

        String tituloDialogo = ufFiltroAtual.equals("PE")
                ? "🏙️ Todas as 185 Cidades de Pernambuco (" + cidadesLista.size() + "):"
                : "🏙️ Filtrar por Cidade (" + cidadesLista.size() + " opções):";

        falar("Escolha a cidade na lista de " + cidadesLista.size() + " municípios ou toque na primeira opção para pesquisar a cidade pelo nome.");
        new AlertDialog.Builder(this)
                .setTitle(tituloDialogo)
                .setItems(opcoes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            abrirDialogoPesquisarCidadePorNome();
                            return;
                        }
                        if (which == 1) {
                            cidadeFiltroAtual = "TODAS";
                            atualizarRotulosFiltros();
                            renderizarListaEstabelecimentos();
                            String msg = "Filtro de cidade limpo. Mostrando todas as cidades.";
                            atualizarPainelEFalar(msg, msg);
                            return;
                        }

                        String cidadeEscolhida = cidadesLista.get(which - 2);
                        EstadoInfo stEscolhido = estadoDeCadaCidade.get(which - 2);
                        selecionarCidadeEEstado(stEscolhido, cidadeEscolhida);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirDialogoPesquisarCidadePorNome() {
        final EditText input = new EditText(this);
        input.setHint("Ex: Caruaru, Petrolina, Gravatá, Pesqueira...");
        input.setSingleLine(true);
        falar("Digite o nome de qualquer cidade de Pernambuco ou do Brasil e toque em Filtrar Cidade.");

        new AlertDialog.Builder(this)
                .setTitle("🔎 Digite o Nome da Cidade:")
                .setView(input)
                .setPositiveButton("Filtrar Cidade", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String texto = input.getText() != null ? input.getText().toString().trim() : "";
                        if (texto.isEmpty()) {
                            exibirDialogoCidadesFiltradas("");
                            return;
                        }
                        exibirDialogoCidadesFiltradas(texto);
                    }
                })
                .setNeutralButton("Ver Todas (185 de PE)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ufFiltroAtual = "PE";
                        regiaoFiltroAtual = "Nordeste";
                        exibirDialogoCidadesFiltradas("");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void selecionarCidadeEEstado(EstadoInfo stEscolhido, String cidadeEscolhida) {
        regiaoFiltroAtual = stEscolhido.regiao;
        ufFiltroAtual = stEscolhido.uf;
        cidadeFiltroAtual = cidadeEscolhida;
        termoBuscaAtual = "";

        garantirBarComCardapioNaCidade(stEscolhido, cidadeEscolhida);

        atualizarRotulosFiltros();
        renderizarListaEstabelecimentos();

        List<Estabelecimento> visiveis = obterEstabelecimentosFiltrados();
        if (!visiveis.isEmpty()) {
            selecionarEstabelecimento(visiveis.get(0), true);
        }

        String msg = "Cidade selecionada: " + cidadeEscolhida + ", " + stEscolhido.nome +
                ". Cardápio de " + estabelecimentoSelecionado.nome + " aberto! Você também pode tocar no botão do Google Maps para abrir a rota.";
        atualizarPainelEFalar(msg, msg);
    }

    private void abrirDialogoFiltroCategoria() {
        falar("Escolha a categoria de estabelecimento onde você deseja comer e consumir.");
        new AlertDialog.Builder(this)
                .setTitle("🍽️ Filtrar por Categoria (Só Locais p/ Comer):")
                .setItems(CATEGORIAS_ESTABELECIMENTO, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        categoriaFiltroAtual = CATEGORIAS_ESTABELECIMENTO[which];
                        termoBuscaAtual = "";

                        EstadoInfo stAlvo = !ufFiltroAtual.equals("TODOS") ? buscarEstadoPorUf(ufFiltroAtual) : ESTADOS_BRASIL[0];
                        if (stAlvo == null) stAlvo = ESTADOS_BRASIL[0];
                        String cidAlvo = !cidadeFiltroAtual.equals("TODAS") ? cidadeFiltroAtual : stAlvo.capital;

                        if (!categoriaFiltroAtual.startsWith("Todas as Categorias")) {
                            garantirEstabelecimentoNaCidadeECategoria(stAlvo, cidAlvo, categoriaFiltroAtual);
                        }

                        atualizarRotulosFiltros();
                        renderizarListaEstabelecimentos();

                        List<Estabelecimento> visiveis = obterEstabelecimentosFiltrados();
                        if (!visiveis.isEmpty()) {
                            selecionarEstabelecimento(visiveis.get(0), true);
                        }

                        String msg = "Categoria selecionada: " + categoriaFiltroAtual + " em " + cidAlvo +
                                ". Exibindo " + visiveis.size() + " locais com alimentação e cardápio aberto!";
                        atualizarPainelEFalar(msg, msg);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- INTEGRAÇÃO COM O GOOGLE MAPS (ROTA GUIADA E BUSCA DE BARES NO MAPA) ---

    private void abrirBarNoGoogleMaps(Estabelecimento est, boolean iniciarNavegacao) {
        if (est == null) return;

        String consulta = est.nome + ", " + est.bairro + ", " + est.cidade + " - " + est.uf;
        String msg = "Abrindo o Google Maps para " + est.nome + " em " + est.cidade + ", " + est.estado + ".";
        atualizarPainelEFalar(msg, msg);
        Toast.makeText(this, "Abrindo Google Maps: " + est.nome + " (" + est.cidade + "/" + est.uf + ")", Toast.LENGTH_LONG).show();

        try {
            Uri uriGeo = Uri.parse("geo:" + est.latitude + "," + est.longitude + "?q=" + Uri.encode(consulta));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uriGeo);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
                return;
            }
        } catch (Exception ignored) {
        }

        // Fallback para qualquer app de mapas ou navegador com Google Maps oficial
        try {
            Uri uriWeb = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(consulta));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, uriWeb);
            startActivity(webIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o Google Maps.", Toast.LENGTH_SHORT).show();
        }
    }

    private void explorarBaresDaCidadeNoGoogleMaps() {
        String localAlvo;
        if (!cidadeFiltroAtual.equals("TODAS")) {
            localAlvo = cidadeFiltroAtual + " - " + ufFiltroAtual;
        } else if (!ufFiltroAtual.equals("TODOS")) {
            EstadoInfo st = buscarEstadoPorUf(ufFiltroAtual);
            localAlvo = st != null ? (st.capital + " - " + st.uf) : ufFiltroAtual;
        } else if (estabelecimentoSelecionado != null) {
            localAlvo = estabelecimentoSelecionado.cidade + " - " + estabelecimentoSelecionado.uf;
        } else {
            localAlvo = "Recife - PE";
        }

        String tipoBuscaMaps = categoriaFiltroAtual.startsWith("Todas as Categorias")
                ? "Restaurantes, Padarias, Cafeterias, Pizzarias, Lanchonetes e Bares"
                : categoriaFiltroAtual;
        String queryMaps = tipoBuscaMaps + " em " + localAlvo;
        String msg = "Abrindo o Google Maps com " + tipoBuscaMaps + " em " + localAlvo + ".";
        atualizarPainelEFalar(msg, msg);
        Toast.makeText(this, "Google Maps: " + queryMaps, Toast.LENGTH_LONG).show();

        try {
            Uri uriGeo = Uri.parse("geo:0,0?q=" + Uri.encode(queryMaps));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uriGeo);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
                return;
            }
        } catch (Exception ignored) {
        }

        try {
            Uri uriWeb = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(queryMaps));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, uriWeb);
            startActivity(webIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o Google Maps.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Recebe um bar ou restaurante compartilhado ou aberto diretamente pelo Google Maps
     * (via botão "Compartilhar" no Google Maps -> "Comanda Falada" ou link de mapa)
     * e abre imediatamente o cardápio interativo daquele estabelecimento.
     */
    private void tratarIntentRecebidaDoGoogleMaps(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;

        String textoExtraido = null;

        if (Intent.ACTION_SEND.equals(action)) {
            String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            StringBuilder bruto = new StringBuilder();
            if (subject != null && !subject.trim().isEmpty()) {
                bruto.append(subject.trim()).append(" ");
            }
            if (text != null && !text.trim().isEmpty()) {
                bruto.append(text.trim());
            }
            textoExtraido = extrairNomeBarDeTextoDoMaps(bruto.toString());
        } else if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            if (data != null) {
                textoExtraido = extrairNomeBarDeUriDoMaps(data);
            }
        }

        if (textoExtraido != null && !textoExtraido.trim().isEmpty()) {
            etBuscaQualquerBar.setText(textoExtraido);
            localizarQualquerBarComCardapio(textoExtraido);
            String avisoMaps = "📍 Recebido do Google Maps: cardápio de " + estabelecimentoSelecionado.nome +
                    " (" + estabelecimentoSelecionado.cidade + "/" + estabelecimentoSelecionado.uf + ") aberto automaticamente! " +
                    "Escolha os itens abaixo para a IA somar na comanda.";
            atualizarPainelEFalar(avisoMaps, avisoMaps);
        }
    }

    private String extrairNomeBarDeTextoDoMaps(String bruto) {
        if (bruto == null) return "";
        // Remove URLs (https://maps.app.goo.gl/..., https://goo.gl/maps/..., etc.)
        String semUrls = bruto.replaceAll("https?://\\S+", " ")
                .replaceAll("(?i)veja este lugar no google maps:?", " ")
                .replaceAll("(?i)compartilhado via google maps", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (!semUrls.isEmpty()) {
            return semUrls;
        }
        return "Bar do Google Maps (" + (!cidadeFiltroAtual.equals("TODAS") ? cidadeFiltroAtual : "Recife") + ")";
    }

    private String extrairNomeBarDeUriDoMaps(Uri uri) {
        if (uri == null) return "";
        String q = uri.getQueryParameter("q");
        if (q != null && !q.trim().isEmpty()) {
            return Uri.decode(q).replace("+", " ").trim();
        }
        String query = uri.getQueryParameter("query");
        if (query != null && !query.trim().isEmpty()) {
            return Uri.decode(query).replace("+", " ").trim();
        }
        String path = uri.getPath();
        if (path != null && path.contains("/place/")) {
            int idx = path.indexOf("/place/") + 7;
            String resto = path.substring(idx);
            int barra = resto.indexOf('/');
            if (barra > 0) resto = resto.substring(0, barra);
            String decodificado = Uri.decode(resto).replace("+", " ").trim();
            if (!decodificado.isEmpty()) return decodificado;
        }
        String ssp = uri.getSchemeSpecificPart();
        if (ssp != null && ssp.contains("?q=")) {
            int idx = ssp.indexOf("?q=") + 3;
            String resto = Uri.decode(ssp.substring(idx)).replace("+", " ").trim();
            if (!resto.isEmpty()) return resto;
        }
        return "Bar Localizado no Google Maps (" + (!cidadeFiltroAtual.equals("TODAS") ? cidadeFiltroAtual : "Recife") + ")";
    }

    private void abrirFluxoBarVindoDoGoogleMaps() {
        String textoClipboard = "";
        try {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null && cb.hasPrimaryClip() && cb.getPrimaryClip() != null && cb.getPrimaryClip().getItemCount() > 0) {
                CharSequence cs = cb.getPrimaryClip().getItemAt(0).getText();
                if (cs != null) textoClipboard = cs.toString().trim();
            }
        } catch (Exception ignored) {
        }

        final String clipLimpo = extrairNomeBarDeTextoDoMaps(textoClipboard);
        final boolean temClipUtil = !clipLimpo.isEmpty() && clipLimpo.length() >= 3 && !clipLimpo.startsWith("000201");

        final List<String> opcoes = new ArrayList<String>();
        if (temClipUtil) {
            opcoes.add("📋 Abrir Cardápio do Local Copiado: " + clipLimpo);
        }
        opcoes.add("🗺️ Abrir Google Maps Agora (No Maps: toque em Compartilhar -> Comanda Falada)");
        opcoes.add("🍽️ Simular Bar Compartilhado do Maps: Bar Cais do Sertão (Recife - PE)");
        opcoes.add("🍽️ Simular Bar Compartilhado do Maps: Mirante do Alto da Sé (Olinda - PE)");
        opcoes.add("🍽️ Simular Bar Compartilhado do Maps: Bodega Mestre Vitalino (Caruaru - PE)");
        opcoes.add("🍽️ Simular Bar Compartilhado do Maps: Bar & Bistrô Esquina Paulista (São Paulo - SP)");

        String explicacao = "Para abrir qualquer cardápio pelo Google Maps: dentro do Google Maps, escolha o bar, toque em Compartilhar e selecione Comanda Falada! Ou escolha uma opção abaixo:";
        falar(explicacao);

        new AlertDialog.Builder(this)
                .setTitle("📲 Cardápio pelo Google Maps")
                .setItems(opcoes.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String escolha = opcoes.get(which);
                        if (temClipUtil && which == 0) {
                            etBuscaQualquerBar.setText(clipLimpo);
                            localizarQualquerBarComCardapio(clipLimpo);
                        } else if (escolha.startsWith("🗺️")) {
                            explorarBaresDaCidadeNoGoogleMaps();
                        } else {
                            int idx = escolha.indexOf(": ");
                            String localSimulado = idx > 0 ? escolha.substring(idx + 2) : escolha;
                            Intent intentSimulada = new Intent(Intent.ACTION_SEND);
                            intentSimulada.setType("text/plain");
                            intentSimulada.putExtra(Intent.EXTRA_SUBJECT, localSimulado);
                            intentSimulada.putExtra(Intent.EXTRA_TEXT, localSimulado + " https://maps.app.goo.gl/exemplo");
                            tratarIntentRecebidaDoGoogleMaps(intentSimulada);
                        }
                    }
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void abrirDialogoSugestoesDeBusca() {
        final String[] exemplos = new String[]{
                "Instituto & Castelo Brennand + Café do Museu (Recife - PE)",
                "Padaria, Confeitaria & Café Colonial do Parque (Recife - PE)",
                "Pizzaria & Hamburgueria Forno de Pedra (Recife - PE)",
                "Mercado de São José & Polo Cultural (Recife - PE)",
                "Sorveteria, Açaí & Tapiocaria da Orla (Recife - PE)",
                "Complexo Gastronômico Bodódromo (Petrolina - PE)",
                "Mirante & Tapioquinha do Alto da Sé (Olinda - PE)",
                "Bodega & Bar Mestre Vitalino (Caruaru - PE)"
        };
        falar("Digite o nome de qualquer restaurante, padaria, pizzaria, café, museu, ponto turístico ou cidade na caixa de texto, ou escolha uma sugestão abaixo.");
        new AlertDialog.Builder(this)
                .setTitle("🔎 Localizar Qualquer Local p/ Comer ou Visitar:")
                .setItems(exemplos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etBuscaQualquerBar.setText(exemplos[which]);
                        localizarQualquerBarComCardapio(exemplos[which]);
                    }
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void atualizarRotulosFiltros() {
        btnFiltroRegiao.setText("🗺️ Região: " + regiaoFiltroAtual + " (Toque p/ mudar)");
        btnFiltroRegiao.setContentDescription("Filtrar por Região do Brasil. Região atual: " + regiaoFiltroAtual + ". Toque duas vezes para escolher a região.");

        String textoEstado = "Todos os Estados";
        if (!ufFiltroAtual.equals("TODOS")) {
            EstadoInfo st = buscarEstadoPorUf(ufFiltroAtual);
            if (st != null) {
                textoEstado = st.nome + " (" + st.uf + ")";
            } else {
                textoEstado = ufFiltroAtual;
            }
        }
        btnFiltroEstado.setText("🏳️ Estado (UF): " + textoEstado + " (Toque p/ mudar)");
        btnFiltroEstado.setContentDescription("Filtrar por Estado. Estado atual: " + textoEstado + ". Toque duas vezes para escolher qualquer estado do Brasil.");

        String textoCidade = cidadeFiltroAtual.equals("TODAS") ? "Todas as Cidades" : cidadeFiltroAtual;
        btnFiltroCidade.setText("🏙️ Cidade: " + textoCidade + " (Toque p/ escolher)");
        btnFiltroCidade.setContentDescription("Filtrar por Cidade. Cidade atual: " + textoCidade + ". Toque duas vezes para escolher a cidade.");

        btnFiltroCategoria.setText("🍽️ Categoria: " + categoriaFiltroAtual + " (Toque p/ mudar)");
        btnFiltroCategoria.setContentDescription("Filtrar por Categoria de estabelecimento para comer ou visitar. Categoria atual: " + categoriaFiltroAtual + ". Toque duas vezes para escolher.");

        if (estabelecimentoSelecionado != null) {
            btnGoogleMapsRota.setText("📍 Rota no Maps: " + estabelecimentoSelecionado.nome);
            btnGoogleMapsRota.setContentDescription("Abrir localização e rota no Google Maps para " + estabelecimentoSelecionado.nome + " em " + estabelecimentoSelecionado.cidade);
        }
    }

    private EstadoInfo buscarEstadoPorUf(String uf) {
        for (int i = 0; i < ESTADOS_BRASIL.length; i++) {
            if (ESTADOS_BRASIL[i].uf.equalsIgnoreCase(uf)) {
                return ESTADOS_BRASIL[i];
            }
        }
        return null;
    }

    private String detectarCidadeNoTexto(String textoNorm, EstadoInfo stPreferido) {
        if (stPreferido != null) {
            for (int c = 0; c < stPreferido.cidadesPrincipais.length; c++) {
                String cid = stPreferido.cidadesPrincipais[c];
                if (textoNorm.contains(normalizarTexto(cid))) {
                    return cid;
                }
            }
        }
        for (int i = 0; i < ESTADOS_BRASIL.length; i++) {
            EstadoInfo st = ESTADOS_BRASIL[i];
            for (int c = 0; c < st.cidadesPrincipais.length; c++) {
                String cid = st.cidadesPrincipais[c];
                if (textoNorm.contains(normalizarTexto(cid))) {
                    return cid;
                }
            }
        }
        if (!cidadeFiltroAtual.equals("TODAS")) return cidadeFiltroAtual;
        return stPreferido != null ? stPreferido.capital : "Recife";
    }

    private EstadoInfo detectarEstadoNoTexto(String textoNorm) {
        for (int i = 0; i < ESTADOS_BRASIL.length; i++) {
            EstadoInfo st = ESTADOS_BRASIL[i];
            for (int c = 0; c < st.cidadesPrincipais.length; c++) {
                if (textoNorm.contains(normalizarTexto(st.cidadesPrincipais[c]))) {
                    return st;
                }
            }
            String nomeSt = normalizarTexto(st.nome);
            String ufSt = st.uf.toLowerCase(Locale.US);
            if (textoNorm.contains(nomeSt) ||
                    textoNorm.contains(" " + ufSt + " ") || textoNorm.endsWith(" " + ufSt) ||
                    textoNorm.contains("-" + ufSt) || textoNorm.contains("(" + ufSt + ")")) {
                return st;
            }
        }
        return null;
    }

    private void localizarQualquerBarComCardapio(String consultaOriginal) {
        String norm = normalizarTexto(consultaOriginal);
        if (norm.isEmpty()) return;

        Estabelecimento encontrado = null;
        for (int i = 0; i < catalogoEstabelecimentos.size(); i++) {
            Estabelecimento est = catalogoEstabelecimentos.get(i);
            String tudoEst = normalizarTexto(est.nome + " " + est.tipo + " " + est.cidade + " " + est.bairro + " " + est.estado + " " + est.uf);
            if (tudoEst.contains(norm) || norm.contains(normalizarTexto(est.nome))) {
                encontrado = est;
                break;
            }
        }

        if (encontrado == null) {
            EstadoInfo estadoDetectado = detectarEstadoNoTexto(norm);
            if (estadoDetectado == null && !ufFiltroAtual.equals("TODOS")) {
                estadoDetectado = buscarEstadoPorUf(ufFiltroAtual);
            }
            if (estadoDetectado == null) {
                estadoDetectado = ESTADOS_BRASIL[0];
            }

            String cidadeDetectada = detectarCidadeNoTexto(norm, estadoDetectado);
            String categoriaDetectada = detectarCategoriaNoTexto(norm);
            String nomeFormatado = capitalizarFrase(consultaOriginal.trim());

            encontrado = criarEstabelecimentoPorCategoria(
                    "local_dinamico_" + Math.abs(norm.hashCode()),
                    nomeFormatado,
                    categoriaDetectada,
                    estadoDetectado,
                    cidadeDetectada
            );
            catalogoEstabelecimentos.add(0, encontrado);
        }

        regiaoFiltroAtual = encontrado.regiao;
        ufFiltroAtual = encontrado.uf;
        cidadeFiltroAtual = encontrado.cidade;
        categoriaFiltroAtual = "Todas as Categorias (Para Comer e Consumir)";
        filtrarApenasSilenciosos = false;
        termoBuscaAtual = "";
        atualizarRotulosFiltros();
        selecionarEstabelecimento(encontrado, true);

        String msgVisual = "✅ Local p/ Comer e Consumir Localizado:\n" +
                "• " + encontrado.nome + " [" + encontrado.tipo + "] — " + encontrado.cidade + "/" + encontrado.uf + "\n" +
                "• Conforto Acústico: " + encontrado.decibeisMedio + " dB (" + encontrado.nivelRuido + ")\n" +
                "• Toque em 'Rota no Maps' ou escolha a comida/bebida no cardápio abaixo!";
        String msgFalada = "Local para comer encontrado: " + encontrado.nome + ", categoria " + encontrado.tipo +
                ", na cidade de " + encontrado.cidade + ", estado de " + encontrado.estado +
                ". O cardápio de comidas e bebidas está aberto abaixo!";
        atualizarPainelEFalar(msgVisual, msgFalada);
    }

    private String detectarCategoriaNoTexto(String norm) {
        if (norm.contains("padaria") || norm.contains("panificadora") || norm.contains("cafe") || norm.contains("cafeteria") || norm.contains("confeitaria")) {
            return "Padarias, Cafeterias e Confeitarias";
        }
        if (norm.contains("pizza") || norm.contains("pizzaria") || norm.contains("hamburguer") || norm.contains("burger") || norm.contains("lanchonete") || norm.contains("pastel") || norm.contains("lanche")) {
            return "Pizzarias, Hamburguerias e Lanchonetes";
        }
        if (norm.contains("churrasc") || norm.contains("espetinho") || norm.contains("parrilla") || norm.contains("peixaria") || norm.contains("frutos do mar") || norm.contains("bododromo")) {
            return "Churrascarias, Espetinhos e Peixarias";
        }
        if (norm.contains("sorvet") || norm.contains("acai") || norm.contains("doceria") || norm.contains("tapioca") || norm.contains("bolo") || norm.contains("gelato")) {
            return "Sorveterias, Açaí, Docerias e Tapiocarias";
        }
        if (norm.contains("mercado") || norm.contains("feira") || norm.contains("quiosque") || norm.contains("bistro") || norm.contains("mirante")) {
            return "Mercados Gastronômicos, Quiosques e Bistrôs Culturais";
        }
        if (norm.contains("bar") || norm.contains("boteco") || norm.contains("chopp") || norm.contains("petisc")) {
            return "Bares, Botecos e Petiscarias";
        }
        if (!categoriaFiltroAtual.startsWith("Todas as Categorias")) {
            return categoriaFiltroAtual;
        }
        return "Restaurantes, Bistrôs e Self-Service";
    }

    private boolean pertenceACategoria(Estabelecimento est, String categoriaFiltro) {
        if (categoriaFiltro == null || categoriaFiltro.startsWith("Todas as Categorias")) return true;
        String t = normalizarTexto(est.tipo + " " + est.nome);
        String c = normalizarTexto(categoriaFiltro);
        if (c.contains("restaurantes")) {
            return t.contains("restaurante") || t.contains("gastronomia") || t.contains("culinaria") || t.contains("regional") || t.contains("cozinha") || t.contains("self");
        }
        if (c.contains("bares")) {
            return t.contains("bar") || t.contains("boteco") || t.contains("petisc") || t.contains("chopp");
        }
        if (c.contains("padarias")) {
            return t.contains("padaria") || t.contains("cafe") || t.contains("confeitaria") || t.contains("panificadora");
        }
        if (c.contains("pizzarias")) {
            return t.contains("pizza") || t.contains("hamburguer") || t.contains("lanchonete") || t.contains("lanche") || t.contains("sanduiche");
        }
        if (c.contains("churrascarias")) {
            return t.contains("churrasc") || t.contains("parrilla") || t.contains("carne") || t.contains("espetinho") || t.contains("peix") || t.contains("frutos do mar");
        }
        if (c.contains("sorveterias")) {
            return t.contains("sorvet") || t.contains("acai") || t.contains("doce") || t.contains("tapioca");
        }
        if (c.contains("mercados")) {
            return t.contains("mercado") || t.contains("quiosque") || t.contains("bistro") || t.contains("gastronomico") || t.contains("bodega") || t.contains("mirante");
        }
        return true;
    }

    private void garantirBarComCardapioNaCidade(EstadoInfo st, String cidade) {
        String catAlvo = categoriaFiltroAtual.startsWith("Todas as Categorias")
                ? "Restaurantes, Bistrôs e Self-Service"
                : categoriaFiltroAtual;
        garantirEstabelecimentoNaCidadeECategoria(st, cidade, catAlvo);
    }

    private void garantirEstabelecimentoNaCidadeECategoria(EstadoInfo st, String cidade, String categoria) {
        for (int i = 0; i < catalogoEstabelecimentos.size(); i++) {
            Estabelecimento est = catalogoEstabelecimentos.get(i);
            if (est.uf.equalsIgnoreCase(st.uf) && est.cidade.equalsIgnoreCase(cidade) && pertenceACategoria(est, categoria)) {
                return;
            }
        }
        String prefixoNome;
        String cNorm = normalizarTexto(categoria);
        if (cNorm.contains("padarias")) {
            prefixoNome = "Padaria & Café Central de " + cidade;
        } else if (cNorm.contains("pizzarias")) {
            prefixoNome = "Pizzaria & Hamburgueria Artesanal de " + cidade;
        } else if (cNorm.contains("churrascarias")) {
            prefixoNome = "Churrascaria & Peixaria Brasa de " + cidade;
        } else if (cNorm.contains("sorveterias")) {
            prefixoNome = "Tapiocaria, Açaí & Sorveteria de " + cidade;
        } else if (cNorm.contains("mercados")) {
            prefixoNome = "Mercado Gastronômico & Bistrô de " + cidade;
        } else if (cNorm.contains("bares")) {
            prefixoNome = "Bar & Petiscaria Estação de " + cidade;
        } else {
            prefixoNome = "Restaurante & Cozinha Regional de " + cidade;
        }

        Estabelecimento novo = criarEstabelecimentoPorCategoria(
                "loc_" + Math.abs((cidade + "_" + st.uf + "_" + categoria).hashCode()),
                prefixoNome,
                categoria,
                st,
                cidade
        );
        catalogoEstabelecimentos.add(0, novo);
    }

    private Estabelecimento criarEstabelecimentoPorCategoria(
            String idPrefix,
            String nomeLocal,
            String categoria,
            EstadoInfo st,
            String cidade
    ) {
        String chavePix = "pix@" + normalizarTexto(nomeLocal).replaceAll("[^a-z0-9]", "") + ".com.br";
        String cNorm = normalizarTexto(categoria);
        Estabelecimento novo = new Estabelecimento(
                idPrefix,
                nomeLocal,
                categoria,
                st.regiao,
                st.nome,
                st.uf,
                cidade,
                "Centro / Ponto de Visitação",
                chavePix,
                st.lat,
                st.lon,
                55,
                "Baixo (Confortável)",
                "Ambiente acolhedor e tranquilo para comer, conversar e visitar",
                "Entrada acessível com rampa, integrado ao Google Maps, cardápio lido pelo TalkBack e pagamento Pix"
        );

        if (cNorm.contains("padarias")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Café Expresso ou Café com Leite Grande", "Cafeteria", 850, "cafe", "expresso", "leite"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Cuscuz Recheado com Queijo Coalho e Carne de Sol", "Prato Regional", 2200, "cuscuz", "queijo", "carne"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Pão na Chapa / Tapioca Quentinha na Manteiga", "Padaria", 1100, "pao", "tapioca", "manteiga"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Fatia de Bolo de Rolo ou Torta Confeitada", "Confeitaria", 1500, "bolo", "torta", "doce"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Suco Natural de Laranja ou Acerola", "Bebida", 1000, "suco", "laranja", "acerola"));
            return novo;
        }
        if (cNorm.contains("pizzarias")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Pizza Grande Artesanal (Calabresa / Marguerita / Frango)", "Pizzaria", 5200, "pizza", "calabresa", "frango"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Hambúrguer Artesanal na Brasa com Queijo e Fritas", "Hamburgueria", 3200, "hamburguer", "burger", "fritas"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Sanduíche Bauru / Pastel Especial Recheado", "Lanchonete", 2100, "sanduiche", "pastel", "lanche"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Suco Natural ou Refrigerante Gelado", "Bebida", 950, "suco", "refrigerante", "coca"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Petit Gateau ou Brownie com Sorvete", "Sobremesa", 1800, "brownie", "sorvete", "sobremesa"));
            return novo;
        }
        if (cNorm.contains("churrascarias")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Picanha ou Carne de Sol na Brasa Completa", "Churrasco", 5900, "picanha", "carne", "churrasco"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Peixe Inteiro Grelhado / Camarão ao Alho e Óleo", "Peixaria", 5400, "peixe", "camarao"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Espetinho Completo com Baião de Dois e Macaxeira", "Espetinho", 2600, "espetinho", "baiao", "macaxeira"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Suco Natural da Fruta Jarra / Copo", "Bebida", 1100, "suco", "jarra"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Pudim de Leite ou Cartola Tradicional", "Sobremesa", 1600, "pudim", "cartola", "sobremesa"));
            return novo;
        }
        if (cNorm.contains("sorveterias")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Tigela de Açaí Completo com Frutas e Granola (500ml)", "Açaí", 2200, "acai", "tigela", "frutas"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Taça de Sorvete Artesanal Regional (2 bolas)", "Sorveteria", 1600, "sorvete", "taca", "gelato"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Tapioca Recheada (Coco com Queijo ou Carne de Sol)", "Tapiocaria", 1800, "tapioca", "coco", "queijo"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Doces Regionais / Bolo de Rolo com Sorvete", "Doceria", 1700, "doce", "bolo de rolo"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Água de Coco ou Água Mineral Gelada", "Bebida", 700, "agua", "coco"));
            return novo;
        }
        if (cNorm.contains("mercados")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Prato Típico Regional (Carne de Sol, Baião e Macaxeira)", "Almoço Regional", 3800, "prato", "almoco", "regional", "carne"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Tapioca Recheada de Queijo Coalho com Carne de Sol", "Tapiocaria", 1800, "tapioca", "queijo", "carne"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Pastel Especial Quentinho com Caldo de Cana / Suco", "Lanche Tradicional", 1600, "pastel", "caldo", "suco"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Café Especial com Bolo de Rolo Pernambucano", "Cafeteria", 1500, "cafe", "bolo"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Água de Coco Gelada ou Suco Natural da Fruta", "Bebida", 800, "agua", "coco", "suco"));
            return novo;
        }

        return criarEstabelecimentoComCardapioRegional(idPrefix, nomeLocal, st, cidade);
    }

    private Estabelecimento criarEstabelecimentoComCardapioRegional(
            String idPrefix,
            String nomeBar,
            EstadoInfo st,
            String cidade
    ) {
        String chavePix = "pix@" + normalizarTexto(nomeBar).replaceAll("[^a-z0-9]", "") + ".com.br";
        Estabelecimento novo = new Estabelecimento(
                idPrefix,
                nomeBar,
                "Bar & Gastronomia Regional",
                st.regiao,
                st.nome,
                st.uf,
                cidade,
                "Centro / Polo Gastronômico",
                chavePix,
                st.lat,
                st.lon,
                57,
                "Baixo (Confortável)",
                "Ambiente agradável com som acústico controlado",
                "Entrada acessível, localizado no Google Maps, cardápio estruturado para TalkBack e Pix imediato"
        );

        if (st.regiao.equals("Nordeste")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Carne de Sol na Nata com Macaxeira", "Prato Principal", 4600, "carne", "sol", "macaxeira"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Caldinho Regional (Feijão ou Camarão)", "Entrada", 1400, "caldinho", "feijao", "camarao"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Queijo Coalho Assado com Mel de Engenho", "Petisco", 2200, "queijo", "coalho", "mel"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Chope Pilsen Gelado 350ml", "Bebida", 1100, "chope", "chopp", "cerveja"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Suco Natural da Fruta (Cajá / Laranja)", "Bebida", 1000, "suco", "caja", "laranja"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_6", "Cartola ou Bolo de Rolo com Café", "Sobremesa", 1700, "cartola", "bolo", "sobremesa"));
        } else if (st.regiao.equals("Sudeste")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Picanha Fatiada na Chapa com Mandioca", "Prato Principal", 5800, "picanha", "chapa", "mandioca"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Porção de Bolinho de Bacalhau / Feijoada", "Entrada", 2900, "bolinho", "bacalhau", "feijoada"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Pastel Artesanal Recheado (6 unid)", "Petisco", 2600, "pastel", "porcao"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Chope Artesanal Gelado", "Bebida", 1300, "chope", "chopp", "cerveja"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Suco de Laranja Natural", "Bebida", 1100, "suco", "laranja"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_6", "Pudim de Leite ou Doce de Leite Mineiro", "Sobremesa", 1600, "pudim", "doce", "sobremesa"));
        } else if (st.regiao.equals("Sul")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Entrecot Grelhado na Brasa com Polenta", "Prato Principal", 5900, "entrecot", "churrasco", "polenta"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Tábua de Linguiça Campeira e Queijos", "Petisco", 3600, "linguica", "queijo", "tabua"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Porção de Polenta Frita com Parmesão", "Entrada", 2400, "polenta", "frita"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Chope Artesanal Weiss / Pilsen", "Bebida", 1300, "chope", "chopp", "cerveja"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Suco de Uva Integral da Serra", "Bebida", 1150, "suco", "uva"));
        } else if (st.regiao.equals("Centro-Oeste")) {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Filé de Pintado na Brasa com Mandioca", "Prato Principal", 5400, "pintado", "peixe", "mandioca"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Empadão Goiano Tradicional", "Entrada", 2400, "empadao", "goiano"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Porção de Pastelzinho de Carne Seca", "Petisco", 2700, "pastel", "carne"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Chope Claro Bem Gelado", "Bebida", 1150, "chope", "chopp", "cerveja"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Suco Natural do Cerrado / Laranja", "Bebida", 1050, "suco", "laranja"));
        } else {
            novo.cardapio.add(new ItemCardapio(idPrefix + "_1", "Peixe Amazônico Grelhado (Tambaqui / Filhote)", "Prato Principal", 5600, "tambaqui", "filhote", "peixe"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_2", "Tacacá Tradicional ou Caldinho Regional", "Entrada", 2500, "tacaca", "caldinho"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_3", "Bolinho de Pirarucu com Macaxeira", "Petisco", 3100, "bolinho", "pirarucu", "macaxeira"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_4", "Chope Gelado na Caneca", "Bebida", 1150, "chope", "chopp", "cerveja"));
            novo.cardapio.add(new ItemCardapio(idPrefix + "_5", "Suco de Cupuaçu ou Açaí na Tigela", "Bebida", 1400, "cupuacu", "acai", "suco"));
        }

        return novo;
    }

    private void carregarGrafoNacionalDeBares() {
        catalogoEstabelecimentos.clear();

        // 1. Pernambuco (PE) - Recife Antigo
        Estabelecimento barCais = new Estabelecimento(
                "bar_cais_antigo",
                "Bar Cais do Sertão & Chopp",
                "Bar & Petiscaria",
                "Nordeste", "Pernambuco", "PE", "Recife", "Recife Antigo",
                "pix@barcaisdosertao.com.br",
                -8.0617, -34.8711, 58, "Baixo (Confortável)",
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

        // 2. Pernambuco (PE) - Recife Graças
        Estabelecimento restAgreste = new Estabelecimento(
                "rest_sabor_agreste",
                "Restaurante Sabor do Agreste",
                "Restaurante Regional",
                "Nordeste", "Pernambuco", "PE", "Recife", "Graças",
                "pagamento@sabordagreste.com.br",
                -8.0482, -34.9012, 54, "Muito Baixo (Silencioso)",
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

        // 3. Pernambuco (PE) - Recife Boa Viagem
        Estabelecimento restVaranda = new Estabelecimento(
                "bar_jardim_boa_viagem",
                "Restaurante Varanda Jardim da Praia",
                "Restaurante & Frutos do Mar",
                "Nordeste", "Pernambuco", "PE", "Recife", "Boa Viagem",
                "pix@varandajardim.com.br",
                -8.1256, -34.9015, 65, "Médio (Agradável)",
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

        // 4. Pernambuco (PE) - Olinda
        Estabelecimento barOlinda = new Estabelecimento(
                "bar_alto_da_se_olinda",
                "Mirante & Tapioquinha do Alto da Sé",
                "Bar & Mirante Regional",
                "Nordeste", "Pernambuco", "PE", "Olinda", "Alto da Sé",
                "pix@mirantealtodase.com.br",
                -8.0138, -34.8492, 59, "Baixo (Confortável)",
                "Brisa do alto e chorinho pernambucano baixo",
                "Acesso ao terraço térreo, mesas espaçadas e garçom guia"
        );
        barOlinda.cardapio.add(new ItemCardapio("oli_tapioca", "Tapioca de Carne de Sol com Queijo Coalho", "Entrada", 1800, "tapioca", "queijo", "carne"));
        barOlinda.cardapio.add(new ItemCardapio("oli_caldinho", "Caldinho de Sururu e Feijão", "Entrada", 1400, "caldinho", "sururu", "feijao"));
        barOlinda.cardapio.add(new ItemCardapio("oli_peixe", "Peixada Pernambucana Completa", "Prato Principal", 5200, "peixada", "peixe"));
        barOlinda.cardapio.add(new ItemCardapio("oli_chope", "Chope Artesanal de Olinda", "Bebida", 1200, "chope", "chopp"));
        barOlinda.cardapio.add(new ItemCardapio("oli_suco", "Suco de Mangaba Natural", "Bebida", 1100, "suco", "mangaba"));
        catalogoEstabelecimentos.add(barOlinda);

        // 5. Pernambuco (PE) - Caruaru
        Estabelecimento barCaruaru = new Estabelecimento(
                "bar_alto_do_moura_caruaru",
                "Bodega & Bar Mestre Vitalino",
                "Bar & Carne de Sol",
                "Nordeste", "Pernambuco", "PE", "Caruaru", "Alto do Moura",
                "pix@bodegavitalino.com.br",
                -8.2842, -36.0125, 57, "Baixo (Confortável)",
                "Forró pé de serra instrumental suave",
                "Rampa de acesso na entrada e salão amplo"
        );
        barCaruaru.cardapio.add(new ItemCardapio("car_carne", "Carne de Sol do Agreste com Queijo Manteiga", "Prato Principal", 4900, "carne de sol", "queijo"));
        barCaruaru.cardapio.add(new ItemCardapio("car_buchada", "Porção de Sarapatel / Petisco Regional", "Entrada", 2400, "sarapatel", "porcao"));
        barCaruaru.cardapio.add(new ItemCardapio("car_macaxeira", "Macaxeira Cozida na Manteiga de Garrafa", "Petisco", 1800, "macaxeira", "manteiga"));
        barCaruaru.cardapio.add(new ItemCardapio("car_suco", "Suco de Graviola Natural", "Bebida", 1000, "suco", "graviola"));
        barCaruaru.cardapio.add(new ItemCardapio("car_chope", "Cerveja Gelada / Chope", "Bebida", 1100, "cerveja", "chope"));
        catalogoEstabelecimentos.add(barCaruaru);

        // 6. Bahia (BA) - Salvador
        Estabelecimento barBahia = new Estabelecimento(
                "bar_pelourinho_bahia",
                "Bar & Restaurante Solar do Pelourinho",
                "Bar & Culinária Baiana",
                "Nordeste", "Bahia", "BA", "Salvador", "Pelourinho",
                "pix@solardopelourinho.com.br",
                -12.9714, -38.5086, 60, "Baixo (Confortável)",
                "Violão e MPB suave",
                "Salão térreo acessível, cardápio falado e mesas largas"
        );
        barBahia.cardapio.add(new ItemCardapio("ba_acaraje", "Acarajé Completo no Prato", "Entrada", 2400, "acaraje", "vatapa"));
        barBahia.cardapio.add(new ItemCardapio("ba_moqueca", "Moqueca Baiana de Camarão e Peixe", "Prato Principal", 5800, "moqueca", "camarao", "peixe"));
        barBahia.cardapio.add(new ItemCardapio("ba_bobo", "Bobó de Camarão com Arroz", "Prato Principal", 5200, "bobo", "camarao"));
        barBahia.cardapio.add(new ItemCardapio("ba_suco", "Suco de Cacau Natural", "Bebida", 1200, "suco", "cacau"));
        barBahia.cardapio.add(new ItemCardapio("ba_cerveja", "Cerveja Artesanal Gelada", "Bebida", 1400, "cerveja", "chope"));
        catalogoEstabelecimentos.add(barBahia);

        // 7. Ceará (CE) - Fortaleza
        Estabelecimento barCeara = new Estabelecimento(
                "bar_beira_mar_ceara",
                "Boteco Brisa de Iracema",
                "Bar & Petiscaria Praiana",
                "Nordeste", "Ceará", "CE", "Fortaleza", "Meireles",
                "pix@brisadeiracema.com.br",
                -3.7258, -38.4984, 62, "Médio (Agradável)",
                "Som do mar e música instrumental",
                "Rampa de acesso na orla, piso tátil e atendimento guia"
        );
        barCeara.cardapio.add(new ItemCardapio("ce_baiao", "Baião de Dois Cremoso com Carne de Sol", "Prato Principal", 4400, "baiao", "carne de sol"));
        barCeara.cardapio.add(new ItemCardapio("ce_caranguejo", "Patinha de Caranguejo Empanada", "Petisco", 3200, "patinha", "caranguejo"));
        barCeara.cardapio.add(new ItemCardapio("ce_caldinho", "Caldinho de Peixe", "Entrada", 1400, "caldinho", "peixe"));
        barCeara.cardapio.add(new ItemCardapio("ce_cajuina", "Cajuína Gelada", "Bebida", 950, "cajuina", "suco"));
        barCeara.cardapio.add(new ItemCardapio("ce_chope", "Chope Pilsen Gelado", "Bebida", 1100, "chope", "chopp"));
        catalogoEstabelecimentos.add(barCeara);

        // 8. São Paulo (SP)
        Estabelecimento barSp = new Estabelecimento(
                "bar_vila_madalena_sp",
                "Bar & Bistrô Esquina Paulista",
                "Bar & Gastronomia",
                "Sudeste", "São Paulo", "SP", "São Paulo", "Vila Madalena",
                "pix@esquinapaulista.com.br",
                -23.5558, -46.6896, 57, "Baixo (Confortável)",
                "Jazz e Bossa Nova em volume baixo",
                "Entrada nivelada sem degraus, banheiro adaptado e garçom treinado"
        );
        barSp.cardapio.add(new ItemCardapio("sp_coxinha", "Coxinha Cremosa de Frango c/ Catupiry (4 unid)", "Entrada", 2600, "coxinha", "frango"));
        barSp.cardapio.add(new ItemCardapio("sp_picanha", "Picanha na Chapa com Mandioca", "Prato Principal", 6400, "picanha", "mandioca"));
        barSp.cardapio.add(new ItemCardapio("sp_pernil", "Sanduíche de Pernil Paulistano", "Petisco", 3100, "sanduiche", "pernil"));
        barSp.cardapio.add(new ItemCardapio("sp_chope", "Chope Artesanal IPA", "Bebida", 1600, "chope", "ipa"));
        barSp.cardapio.add(new ItemCardapio("sp_suco", "Suco de Laranja Natural", "Bebida", 1200, "suco", "laranja"));
        catalogoEstabelecimentos.add(barSp);

        // 9. Rio de Janeiro (RJ)
        Estabelecimento barRj = new Estabelecimento(
                "bar_lapa_rj",
                "Boteco Bossa de Copacabana",
                "Boteco Tradicional Carioca",
                "Sudeste", "Rio de Janeiro", "RJ", "Rio de Janeiro", "Copacabana",
                "pix@bossacopacabana.com.br",
                -22.9711, -43.1825, 59, "Baixo (Confortável)",
                "Bossa Nova acústica suave",
                "Calçada com piso tátil e mesas externas acessíveis"
        );
        barRj.cardapio.add(new ItemCardapio("rj_feijoada", "Bolinho de Feijoada (6 unid)", "Entrada", 2900, "bolinho", "feijoada"));
        barRj.cardapio.add(new ItemCardapio("rj_oswaldo", "Filé Oswaldo Aranha Completo", "Prato Principal", 5600, "file", "oswaldo"));
        barRj.cardapio.add(new ItemCardapio("rj_pastel", "Pastel de Camarão com Catupiry", "Petisco", 1400, "pastel", "camarao"));
        barRj.cardapio.add(new ItemCardapio("rj_chope", "Chope Carioca na Régua", "Bebida", 1100, "chope", "chopp"));
        barRj.cardapio.add(new ItemCardapio("rj_mate", "Mate Gelado da Praia com Limão", "Bebida", 900, "mate", "limao"));
        catalogoEstabelecimentos.add(barRj);

        // 10. Minas Gerais (MG)
        Estabelecimento barMg = new Estabelecimento(
                "bar_savassi_mg",
                "Bar & Prosa da Savassi",
                "Bar Mineiro",
                "Sudeste", "Minas Gerais", "MG", "Belo Horizonte", "Savassi",
                "pix@prosadasavassi.com.br",
                -19.9386, -43.9352, 55, "Muito Baixo (Silencioso)",
                "Clube da Esquina instrumental baixo",
                "Acesso plano, mesas amplas e atendimento imediato"
        );
        barMg.cardapio.add(new ItemCardapio("mg_tropeiro", "Feijão Tropeiro Mineiro Completo", "Prato Principal", 4200, "feijao", "tropeiro"));
        barMg.cardapio.add(new ItemCardapio("mg_paoqueijo", "Pão de Queijo Recheado com Pernil", "Entrada", 2800, "pao de queijo", "pernil"));
        barMg.cardapio.add(new ItemCardapio("mg_torresmo", "Torresmo de Barriga Crocante", "Petisco", 3200, "torresmo", "barriga"));
        barMg.cardapio.add(new ItemCardapio("mg_limonada", "Limonada Suíça Cremosa", "Bebida", 1000, "limonada", "suco"));
        barMg.cardapio.add(new ItemCardapio("mg_doce", "Doce de Leite com Queijo Canastra", "Sobremesa", 1800, "doce de leite", "queijo"));
        catalogoEstabelecimentos.add(barMg);

        // 11. Rio Grande do Sul (RS)
        Estabelecimento barRs = new Estabelecimento(
                "bar_cidade_baixa_rs",
                "Galpão & Chopp Gaúcho",
                "Bar & Parrilla",
                "Sul", "Rio Grande do Sul", "RS", "Porto Alegre", "Cidade Baixa",
                "pix@galpaogaucho.com.br",
                -30.0392, -51.2214, 56, "Baixo (Confortável)",
                "Música acústica ambiente",
                "Rampa de acesso e iluminação/espaço confortável"
        );
        barRs.cardapio.add(new ItemCardapio("rs_entrecot", "Entrecot Grelhado com Batata Rústica", "Prato Principal", 5900, "entrecot", "churrasco"));
        barRs.cardapio.add(new ItemCardapio("rs_polenta", "Polenta Frita com Parmesão", "Entrada", 2400, "polenta", "queijo"));
        barRs.cardapio.add(new ItemCardapio("rs_linguica", "Linguiça Campeira Aperitivo", "Petisco", 3400, "linguica", "campeira"));
        barRs.cardapio.add(new ItemCardapio("rs_chope", "Chope Artesanal Pilsen", "Bebida", 1200, "chope", "chopp"));
        barRs.cardapio.add(new ItemCardapio("rs_uva", "Suco de Uva Integral", "Bebida", 1100, "suco", "uva"));
        catalogoEstabelecimentos.add(barRs);

        // 12. Distrito Federal (DF)
        Estabelecimento barDf = new Estabelecimento(
                "bar_asa_sul_df",
                "Varanda do Cerrado Bar & Cozinha",
                "Bar & Restaurante",
                "Centro-Oeste", "Distrito Federal", "DF", "Brasília", "Asa Sul",
                "pix@varandadocerrado.com.br",
                -15.8195, -47.9022, 55, "Muito Baixo (Silencioso)",
                "MPB instrumental em volume de conversa",
                "Quadra plana com rampa e piso tátil completo"
        );
        barDf.cardapio.add(new ItemCardapio("df_galinhada", "Galinhada Goiana Tradicional", "Prato Principal", 4100, "galinhada", "frango"));
        barDf.cardapio.add(new ItemCardapio("df_pastel", "Pastelzinho de Carne Seca (6 unid)", "Entrada", 2500, "pastel", "carne"));
        barDf.cardapio.add(new ItemCardapio("df_tilapia", "Tilápia Grelhada com Legumes", "Prato Principal", 4600, "tilapia", "peixe"));
        barDf.cardapio.add(new ItemCardapio("df_suco", "Suco de Maracujá Natural", "Bebida", 1050, "suco", "maracuja"));
        barDf.cardapio.add(new ItemCardapio("df_chope", "Chope Claro Gelado", "Bebida", 1200, "chope", "chopp"));
        catalogoEstabelecimentos.add(barDf);

        // 13. Pará (PA)
        Estabelecimento barPa = new Estabelecimento(
                "bar_estacao_docas_pa",
                "Restaurante & Bar Estação Amazônia",
                "Bar & Culinária Amazônica",
                "Norte", "Pará", "PA", "Belém", "Campina",
                "pix@estacaoamazonia.com.br",
                -1.4485, -48.5002, 58, "Baixo (Confortável)",
                "Som ambiente da baía e violão regional suave",
                "Corredores largos, rampa de acesso e atendimento guiado"
        );
        barPa.cardapio.add(new ItemCardapio("pa_tacaca", "Tacacá Tradicional na Cuia", "Entrada", 2600, "tacaca", "jambu"));
        barPa.cardapio.add(new ItemCardapio("pa_filhote", "Filhote Grelhado com Arroz de Jambu", "Prato Principal", 6200, "filhote", "peixe", "jambu"));
        barPa.cardapio.add(new ItemCardapio("pa_unha", "Unha de Caranguejo Paraense", "Petisco", 2200, "unha", "caranguejo"));
        barPa.cardapio.add(new ItemCardapio("pa_cupuacu", "Suco de Cupuaçu Gelado", "Bebida", 1100, "cupuacu", "suco"));
        barPa.cardapio.add(new ItemCardapio("pa_acai", "Creme de Açaí com Tapioca", "Sobremesa", 1800, "acai", "tapioca"));
        catalogoEstabelecimentos.add(barPa);

        // 14. Boteco do Galo Frito (PE - Ruidoso para Caso 3)
        Estabelecimento botecoGalo = new Estabelecimento(
                "bar_boemia_ruidoso",
                "Boteco do Galo Frito",
                "Boteco Boêmio",
                "Nordeste", "Pernambuco", "PE", "Recife", "Recife Antigo",
                "pix@botecogalofrito.com.br",
                -8.0635, -34.8732, 84, "Alto (Ruidoso ⚠️)",
                "Música ao vivo com percussão alta e caixas no corredor",
                "Mesas próximas; barulho elevado exige atenção ao usar leitor de telas"
        );
        botecoGalo.cardapio.add(new ItemCardapio("galo_espetinho", "Espetinho de Carne com Farofa", "Petisco", 1500, "espetinho", "farofa", "carne"));
        botecoGalo.cardapio.add(new ItemCardapio("galo_pastel", "Porção de Pastel de Festa (8 unid)", "Petisco", 2600, "pastel", "porcao"));
        botecoGalo.cardapio.add(new ItemCardapio("galo_chope", "Chope Gelado Caneca", "Bebida", 1000, "chope", "chopp"));
        botecoGalo.cardapio.add(new ItemCardapio("galo_cerveja", "Cerveja Lata 350ml", "Bebida", 800, "cerveja", "lata"));
        catalogoEstabelecimentos.add(botecoGalo);
        // 15. Padaria, Cafeteria e Confeitaria (PE)
        Estabelecimento padariaRecife = new Estabelecimento(
                "padaria_cafe_jaqueira",
                "Padaria, Confeitaria & Café Colonial do Parque",
                "Padarias, Cafeterias e Confeitarias",
                "Nordeste", "Pernambuco", "PE", "Recife", "Jaqueira",
                "pix@padariacolonial.com.br",
                -8.0372, -34.9041, 52, "Muito Baixo (Silencioso)",
                "Ambiente silencioso de cafeteria e leitura",
                "Rampa de entrada, balcão rebaixado e atendimento na mesa"
        );
        padariaRecife.cardapio.add(new ItemCardapio("pad_cuscuz", "Cuscuz Pernambucano com Queijo Coalho e Carne de Sol", "Cafeteria", 2200, "cuscuz", "queijo", "carne"));
        padariaRecife.cardapio.add(new ItemCardapio("pad_cafe", "Café Expresso Duplo ou Cappuccino Cremoso", "Bebida", 950, "cafe", "expresso", "cappuccino"));
        padariaRecife.cardapio.add(new ItemCardapio("pad_bolo", "Fatia de Bolo de Rolo Tradicional com Queijo do Reino", "Confeitaria", 1600, "bolo", "rolo", "queijo"));
        padariaRecife.cardapio.add(new ItemCardapio("pad_pao", "Cesta de Pães Quentinhos na Manteiga e Suco Natural", "Padaria", 1800, "pao", "manteiga", "suco"));
        catalogoEstabelecimentos.add(padariaRecife);

        // 16. Pizzaria, Hamburgueria e Lanchonete (PE)
        Estabelecimento pizzariaRecife = new Estabelecimento(
                "pizzaria_forno_artesanal",
                "Pizzaria & Hamburgueria Forno de Pedra",
                "Pizzarias, Hamburguerias e Lanchonetes",
                "Nordeste", "Pernambuco", "PE", "Recife", "Espinheiro",
                "pix@fornodepedra.com.br",
                -8.0421, -34.8920, 56, "Baixo (Confortável)",
                "Som ambiente suave",
                "Entrada acessível sem degraus e mesas largas"
        );
        pizzariaRecife.cardapio.add(new ItemCardapio("piz_grande", "Pizza Grande no Forno a Lenha (Calabresa / Marguerita)", "Pizzaria", 5400, "pizza", "calabresa", "marguerita"));
        pizzariaRecife.cardapio.add(new ItemCardapio("piz_burger", "Hambúrguer Artesanal na Brasa com Queijo Cheddar e Fritas", "Hamburgueria", 3400, "hamburguer", "burger", "fritas"));
        pizzariaRecife.cardapio.add(new ItemCardapio("piz_suco", "Jarra de Suco Natural ou Refrigerante", "Bebida", 1400, "suco", "jarra", "refrigerante"));
        pizzariaRecife.cardapio.add(new ItemCardapio("piz_brownie", "Brownie de Chocolate Quente com Sorvete", "Sobremesa", 1900, "brownie", "sorvete"));
        catalogoEstabelecimentos.add(pizzariaRecife);

        // 17. Bistrô e Cafeteria do Jardim (PE - Visitar para Comer e Consumir)
        Estabelecimento bistroJardim = new Estabelecimento(
                "bistro_cafe_jardim_recife",
                "Bistrô & Cafeteria Jardim da Várzea",
                "Mercados Gastronômicos, Quiosques e Bistrôs Culturais",
                "Nordeste", "Pernambuco", "PE", "Recife", "Várzea",
                "pix@bistrojardimvarzea.com.br",
                -8.0531, -34.9628, 50, "Muito Baixo (Silencioso)",
                "Jardim silencioso com mesas ao ar livre",
                "Acessibilidade completa, rampa e atendimento prioritário na mesa"
        );
        bistroJardim.cardapio.add(new ItemCardapio("bis_quiche", "Quiche de Queijo do Reino com Salada e Grelhado", "Almoço", 3200, "quiche", "salada", "almoco"));
        bistroJardim.cardapio.add(new ItemCardapio("bis_sanduiche", "Sanduíche Artesanal no Pão Ciabatta com Carne de Sol", "Lanche", 2600, "sanduiche", "carne", "pao"));
        bistroJardim.cardapio.add(new ItemCardapio("bis_cafe", "Café Especial Moído na Hora com Bolo de Noiva Pernambucano", "Cafeteria", 1900, "cafe", "bolo"));
        bistroJardim.cardapio.add(new ItemCardapio("bis_suco", "Suco de Frutas Vermelhas ou Água de Coco Gelada", "Bebida", 1200, "suco", "agua", "coco"));
        catalogoEstabelecimentos.add(bistroJardim);

        // 18. Mercado Gastronômico de São José (PE)
        Estabelecimento mercadoSaoJose = new Estabelecimento(
                "mercado_sao_jose_recife",
                "Boxes Gastronômicos do Mercado de São José",
                "Mercados Gastronômicos, Quiosques e Bistrôs Culturais",
                "Nordeste", "Pernambuco", "PE", "Recife", "São José",
                "pix@mercadosaojose.com.br",
                -8.0684, -34.8781, 63, "Médio (Agradável)",
                "Ambiente tradicional de boxes gastronômicos regionais",
                "Corredores térreos e boxes de culinária típica pernambucana"
        );
        mercadoSaoJose.cardapio.add(new ItemCardapio("mer_macaxeira", "Macaxeira Cozida com Guisado de Carne de Sol / Bode", "Prato Regional", 3200, "macaxeira", "carne", "bode"));
        mercadoSaoJose.cardapio.add(new ItemCardapio("mer_tapioca", "Tapioca Rendada de Coco Fresco com Queijo Coalho", "Tapiocaria", 1500, "tapioca", "coco", "queijo"));
        mercadoSaoJose.cardapio.add(new ItemCardapio("mer_caldo", "Caldo de Cana Gelado com Limão + Pastel de Feira", "Lanche", 1400, "caldo", "cana", "pastel"));
        mercadoSaoJose.cardapio.add(new ItemCardapio("mer_doce", "Bolo de Rolo Pernambucano com Café Quentinho", "Sobremesa", 1600, "doce", "cafe", "bolo"));
        catalogoEstabelecimentos.add(mercadoSaoJose);

        // 19. Sorveteria, Açaí e Tapiocaria (PE)
        Estabelecimento sorveteriaBoaViagem = new Estabelecimento(
                "sorveteria_acai_orla",
                "Sorveteria, Açaí & Tapiocaria da Orla",
                "Sorveterias, Açaí, Docerias e Tapiocarias",
                "Nordeste", "Pernambuco", "PE", "Recife", "Boa Viagem",
                "pix@sorveteriaorla.com.br",
                -8.1180, -34.8965, 55, "Baixo (Confortável)",
                "Brisa da praia e ambiente familiar tranquilo",
                "Calçadão plano com piso tátil e mesas acessíveis"
        );
        sorveteriaBoaViagem.cardapio.add(new ItemCardapio("sor_acai", "Tigela de Açaí 500ml com Banana, Morango e Castanha", "Açaí", 2300, "acai", "tigela", "morango"));
        sorveteriaBoaViagem.cardapio.add(new ItemCardapio("sor_taca", "Sorvete Artesanal de Cajá, Mangaba e Castanha (2 bolas)", "Sorveteria", 1600, "sorvete", "caja", "mangaba"));
        sorveteriaBoaViagem.cardapio.add(new ItemCardapio("sor_tapioca", "Tapioca Doce de Cartola (Banana, Queijo e Canela)", "Tapiocaria", 1800, "tapioca", "cartola", "banana"));
        sorveteriaBoaViagem.cardapio.add(new ItemCardapio("sor_coco", "Água de Coco Gelada na Fruta", "Bebida", 700, "agua", "coco"));
        catalogoEstabelecimentos.add(sorveteriaBoaViagem);

        // 20. Churrascaria e Bodódromo de Petrolina (PE)
        Estabelecimento bododromoPetrolina = new Estabelecimento(
                "churrascaria_bododromo_petrolina",
                "Complexo Gastronômico Bodódromo & Churrascaria do Vale",
                "Churrascarias, Espetinhos e Peixarias",
                "Nordeste", "Pernambuco", "PE", "Petrolina", "Areia Branca",
                "pix@bododromopetrolina.com.br",
                -9.3891, -40.5027, 58, "Baixo (Confortável)",
                "Som ambiente regional do Vale do São Francisco",
                "Pátio amplo, plano e totalmente acessível"
        );
        bododromoPetrolina.cardapio.add(new ItemCardapio("pet_carne", "Picanha de Carneiro / Carne de Sol Assada na Brasa", "Churrasco", 6200, "picanha", "carne", "churrasco"));
        bododromoPetrolina.cardapio.add(new ItemCardapio("pet_peixe", "Surubim do Rio São Francisco Grelhado com Pirão", "Peixaria", 5800, "surubim", "peixe", "pirao"));
        bododromoPetrolina.cardapio.add(new ItemCardapio("pet_baiao", "Baião de Dois Cremoso com Macaxeira e Queijo Coalho", "Acompanhamento", 2400, "baiao", "macaxeira", "queijo"));
        bododromoPetrolina.cardapio.add(new ItemCardapio("pet_uva", "Suco de Uva Integral do Vale do São Francisco", "Bebida", 1200, "suco", "uva"));
        catalogoEstabelecimentos.add(bododromoPetrolina);
    }

    private List<Estabelecimento> obterEstabelecimentosFiltrados() {
        List<Estabelecimento> filtrados = new ArrayList<Estabelecimento>();
        for (int i = 0; i < catalogoEstabelecimentos.size(); i++) {
            Estabelecimento est = catalogoEstabelecimentos.get(i);
            if (!regiaoFiltroAtual.equals("Todas as Regiões") && !est.regiao.equalsIgnoreCase(regiaoFiltroAtual)) {
                continue;
            }
            if (!ufFiltroAtual.equals("TODOS") && !est.uf.equalsIgnoreCase(ufFiltroAtual)) {
                continue;
            }
            if (!cidadeFiltroAtual.equals("TODAS") && !est.cidade.equalsIgnoreCase(cidadeFiltroAtual)) {
                continue;
            }
            if (!categoriaFiltroAtual.startsWith("Todas as Categorias") && !pertenceACategoria(est, categoriaFiltroAtual)) {
                continue;
            }
            if (est.cardapio == null || est.cardapio.isEmpty()) {
                continue;
            }
            if (filtrarApenasSilenciosos && est.decibeisMedio > 65) {
                continue;
            }
            if (!termoBuscaAtual.isEmpty()) {
                String alvo = normalizarTexto(est.nome + " " + est.tipo + " " + est.cidade + " " + est.bairro + " " + est.estado + " " + est.uf + " " + est.regiao);
                if (!alvo.contains(normalizarTexto(termoBuscaAtual))) {
                    continue;
                }
            }
            filtrados.add(est);
        }
        return filtrados;
    }

    private void renderizarListaEstabelecimentos() {
        containerEstabelecimentos.removeAllViews();
        List<Estabelecimento> visiveis = obterEstabelecimentosFiltrados();

        if (visiveis.isEmpty()) {
            TextView tvVazio = new TextView(this);
            tvVazio.setText("Nenhum bar encontrado com esse filtro. Digite qualquer nome de bar na caixa acima e toque em 'Localizar Bar / Cardápio'!");
            tvVazio.setTextColor(Color.parseColor("#FCA5A5"));
            tvVazio.setTextSize(14f);
            tvVazio.setPadding(12, 12, 12, 12);
            containerEstabelecimentos.addView(tvVazio);
            return;
        }

        for (int i = 0; i < visiveis.size(); i++) {
            final Estabelecimento est = visiveis.get(i);
            boolean selecionado = (est == estabelecimentoSelecionado);

            LinearLayout linhaBar = new LinearLayout(this);
            linhaBar.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lpLinha = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lpLinha.setMargins(0, 0, 0, 14);
            linhaBar.setLayoutParams(lpLinha);

            Button btnEst = new Button(this);
            btnEst.setAllCaps(false);
            btnEst.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            btnEst.setPadding(24, 20, 24, 20);
            btnEst.setTextSize(13.5f);
            btnEst.setTypeface(null, selecionado ? Typeface.BOLD : Typeface.NORMAL);
            btnEst.setTextColor(Color.WHITE);
            btnEst.setBackgroundColor(selecionado ? Color.parseColor("#1D4ED8") : Color.parseColor("#1E293B"));

            String prefixo = selecionado ? "✅ [CARDÁPIO ABERTO] " : "🏪 ";
            String textoBotao = prefixo + est.nome + "\n" +
                    "• Categoria: " + est.tipo + "\n" +
                    "• Cidade: " + est.cidade + " - " + est.uf + " (" + est.bairro + " | " + est.regiao + ")\n" +
                    "• Ruído: " + est.decibeisMedio + " dB (" + est.nivelRuido + ")\n" +
                    "• Cardápio / Opções: " + est.cardapio.size() + " itens (Toque p/ abrir)";
            btnEst.setText(textoBotao);
            btnEst.setContentDescription(
                    (selecionado ? "Cardápio aberto: " : "") + est.nome + ", categoria " + est.tipo + ", na cidade de " + est.cidade +
                            ", Estado " + est.estado + ", Região " + est.regiao + ". Ruído médio de " + est.decibeisMedio +
                            " decibéis. Toque duas vezes para abrir o cardápio com " + est.cardapio.size() + " itens."
            );

            LinearLayout.LayoutParams lpEst = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            btnEst.setLayoutParams(lpEst);
            btnEst.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selecionarEstabelecimento(est, true);
                }
            });
            linhaBar.addView(btnEst);

            // Botão lateral direto para abrir este bar no Google Maps
            Button btnMaps = new Button(this);
            btnMaps.setAllCaps(false);
            btnMaps.setText("📍\nMaps");
            btnMaps.setTextSize(12f);
            btnMaps.setTextColor(Color.WHITE);
            btnMaps.setBackgroundColor(Color.parseColor("#B45309"));
            btnMaps.setContentDescription("Abrir " + est.nome + " de " + est.cidade + " no Google Maps");
            LinearLayout.LayoutParams lpMaps = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            lpMaps.setMargins(8, 0, 0, 0);
            btnMaps.setLayoutParams(lpMaps);
            btnMaps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirBarNoGoogleMaps(est, true);
                }
            });
            linhaBar.addView(btnMaps);

            containerEstabelecimentos.addView(linhaBar);
        }
    }

    private void selecionarEstabelecimento(Estabelecimento novoEst, boolean limparComandaAnterior) {
        if (estabelecimentoSelecionado != novoEst && limparComandaAnterior) {
            quantidadesComanda.clear();
        }
        estabelecimentoSelecionado = novoEst;
        atualizarRotulosFiltros();
        renderizarListaEstabelecimentos();
        renderizarCardapioEstabelecimento();
        recalcularComanda();

        StringBuilder resumoItens = new StringBuilder();
        for (int i = 0; i < novoEst.cardapio.size(); i++) {
            ItemCardapio it = novoEst.cardapio.get(i);
            resumoItens.append(it.nome).append(" por ").append(it.formatarPreco());
            if (i < novoEst.cardapio.size() - 1) resumoItens.append(", ");
        }

        String msgVisual = "Cardápio aberto: " + novoEst.nome + " (" + novoEst.cidade + "/" + novoEst.uf + " — Região " + novoEst.regiao + ").\n" +
                "Conforto Acústico: " + novoEst.decibeisMedio + " dB (" + novoEst.nivelRuido + ").\n" +
                "Toque em 📍 Maps para ver a rota ou escolha os itens no cardápio abaixo!";
        String msgFalada = "Cardápio aberto de " + novoEst.nome + ", na cidade de " + novoEst.cidade + ", " + novoEst.estado +
                ", ruído de " + novoEst.decibeisMedio + " decibéis. Cardápio disponível: " + resumoItens.toString() +
                ". Toque nos itens para adicionar à comanda.";
        atualizarPainelEFalar(msgVisual, msgFalada);
    }

    private void renderizarCardapioEstabelecimento() {
        containerCardapio.removeAllViews();
        if (estabelecimentoSelecionado == null) return;

        tvTituloCardapio.setText("2. CARDÁPIO: " + estabelecimentoSelecionado.nome.toUpperCase(new Locale("pt", "BR")) +
                " (" + estabelecimentoSelecionado.cidade + "/" + estabelecimentoSelecionado.uf + ")");

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
                            ". Quantidade atual na comanda: " + qtdAtual + ". Toque duas vezes para escolher e somar."
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
        sb.append("📍 Local: ").append(estabelecimentoSelecionado.nome)
                .append(" (").append(estabelecimentoSelecionado.cidade).append("/").append(estabelecimentoSelecionado.uf).append(")\n");
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

        ultimoSubtotalCentavos = somaCentavos;
        ultimaTaxaServicoCentavos = adicionarTaxaDezPorCento ? (int) Math.round(somaCentavos * 0.10) : 0;
        ultimoTotalCentavos = ultimoSubtotalCentavos + ultimaTaxaServicoCentavos;

        if (btnTaxaServico != null) {
            int valorDezExemplo = (int) Math.round(somaCentavos * 0.10);
            String sufixoValor = somaCentavos > 0 ? (" (" + formatarMoedaCentavos(valorDezExemplo) + ")") : "";
            String textoBtnTaxa = adicionarTaxaDezPorCento
                    ? "✅ 10% do Garçom Adicionado: Sim" + sufixoValor + " (Toque p/ mudar)"
                    : "💁 Deseja adicionar os 10% na soma? (Atual: Não" + sufixoValor + ")";
            btnTaxaServico.setText(textoBtnTaxa);
            btnTaxaServico.setContentDescription(
                    "Pergunta da soma: Deseja adicionar os dez por cento de taxa de serviço? Situação atual: " +
                            (adicionarTaxaDezPorCento ? "Sim, dez por cento incluído" : "Não, sem taxa de dez por cento") +
                            ". Toque duas vezes para responder Sim ou Não."
            );
        }

        if (totalUnidades == 0) {
            ultimoPayloadPix = "";
            sb.append("Nenhum item selecionado ainda.\n");
            sb.append("Toque nos pratos ou bebidas do cardápio acima ou fale seu pedido para a IA somar!");
            tvResumoComanda.setText(sb.toString());
            tvResumoComanda.setContentDescription("Comanda vazia em " + estabelecimentoSelecionado.nome + ". Toque nos itens do cardápio acima para escolher.");
            btnPagarBanco.setText("🏦 COPIAR PIX E ABRIR APP DO BANCO NO CELULAR");
            return;
        }

        boolean dentroDoTeto = ultimoTotalCentavos <= tetoGastosCentavos;
        senhaEntregaAtual = String.format(Locale.US, "%04d", 1000 + Math.abs((estabelecimentoSelecionado.id.hashCode() + ultimoTotalCentavos) % 9000));
        ultimoPayloadPix = gerarPayloadPix(
                estabelecimentoSelecionado.chavePix,
                estabelecimentoSelecionado.nome,
                estabelecimentoSelecionado.cidade,
                ultimoTotalCentavos / 100.0,
                "PED" + senhaEntregaAtual
        );

        sb.append("----------------------------------------\n");
        sb.append("🧾 Subtotal dos Itens: ").append(formatarMoedaCentavos(ultimoSubtotalCentavos)).append("\n");
        sb.append("💁 Taxa de Serviço (10%): ").append(
                adicionarTaxaDezPorCento
                        ? ("+ " + formatarMoedaCentavos(ultimaTaxaServicoCentavos) + " (Incluída)")
                        : "R$ 0,00 (Não incluída)"
        ).append("\n");
        sb.append("💰 SOMA TOTAL DA COMANDA: ").append(formatarMoedaCentavos(ultimoTotalCentavos)).append("\n");
        sb.append("🎯 Teto de Gastos (").append(formatarMoedaCentavos(tetoGastosCentavos)).append("): ")
                .append(dentroDoTeto ? "✅ Dentro do limite" : "⚠️ ULTRAPASSOU O TETO!").append("\n");
        sb.append("🔑 Senha Falada de Entrega: ").append(senhaEntregaAtual).append("\n");
        sb.append("📋 Pix Copia e Cola (BR Code):\n").append(ultimoPayloadPix);

        tvResumoComanda.setText(sb.toString());
        tvResumoComanda.setContentDescription(
                "Resumo da comanda em " + estabelecimentoSelecionado.nome +
                        ". Subtotal dos itens: " + formatarMoedaCentavos(ultimoSubtotalCentavos) +
                        ". Taxa de dez por cento: " + (adicionarTaxaDezPorCento ? formatarMoedaCentavos(ultimaTaxaServicoCentavos) : "não incluída") +
                        ". Soma total pela IA: " + formatarMoedaCentavos(ultimoTotalCentavos) +
                        ". Senha de entrega: " + senhaEntregaAtual + "."
        );
        btnPagarBanco.setText("🏦 PAGAR " + formatarMoedaCentavos(ultimoTotalCentavos) + " NO APP DO SEU BANCO");
    }

    /**
     * Pergunta em voz alta pelo leitor de tela (TalkBack) e em diálogo se o usuário deseja adicionar os 10% na soma.
     */
    private void perguntarTaxaDezPorCento(final boolean abrirPagamentoDepois, final BancoAppInfo bancoEspecifico) {
        if (ultimoSubtotalCentavos <= 0 && abrirPagamentoDepois) {
            String msg = "Sua comanda ainda está vazia! Escolha pelo menos um item do cardápio para a IA somar.";
            atualizarPainelEFalar(msg, msg);
            return;
        }

        final int subtotal = ultimoSubtotalCentavos;
        final int valorDez = (int) Math.round(subtotal * 0.10);
        final int totalComDez = subtotal + valorDez;

        final String[] opcoes = new String[]{
                "✅ Sim, adicionar os 10% (+" + formatarMoedaCentavos(valorDez) + " -> Total: " + formatarMoedaCentavos(totalComDez) + ")",
                "❌ Não adicionar os 10% (Manter apenas o subtotal: " + formatarMoedaCentavos(subtotal) + ")"
        };

        String perguntaFalada = "Deseja adicionar os dez por cento de taxa de serviço na soma da comanda? " +
                "Com os dez por cento (" + formatarMoedaCentavos(valorDez) + "), o total fica " + formatarMoedaCentavos(totalComDez) +
                ". Sem os dez por cento, o total fica " + formatarMoedaCentavos(subtotal) + ". Escolha Sim ou Não.";
        falar(perguntaFalada);

        new AlertDialog.Builder(this)
                .setTitle("💁 Deseja adicionar os 10% na soma?")
                .setItems(opcoes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adicionarTaxaDezPorCento = (which == 0);
                        recalcularComanda();

                        String confirmacao = adicionarTaxaDezPorCento
                                ? ("Taxa de 10% (" + formatarMoedaCentavos(ultimaTaxaServicoCentavos) + ") adicionada! Nova soma total da comanda: " + formatarMoedaCentavos(ultimoTotalCentavos) + ".")
                                : ("Sem taxa de 10%. Soma total da comanda mantida em: " + formatarMoedaCentavos(ultimoTotalCentavos) + ".");
                        atualizarPainelEFalar(confirmacao, confirmacao);

                        if (abrirPagamentoDepois) {
                            if (bancoEspecifico != null) {
                                executarAberturaBancoComPix(bancoEspecifico);
                            } else {
                                prosseguirPagamentoNosBancos();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ouvirResumoFaladoDaComanda() {
        if (ultimoTotalCentavos <= 0) {
            String msg = "Sua comanda em " + estabelecimentoSelecionado.nome + " ainda está vazia. Escolha itens no cardápio acima.";
            atualizarPainelEFalar(msg, msg);
            return;
        }

        StringBuilder fala = new StringBuilder();
        fala.append("Resumo da sua comanda em ").append(estabelecimentoSelecionado.nome)
                .append(" (").append(estabelecimentoSelecionado.cidade).append("): ");
        for (int i = 0; i < estabelecimentoSelecionado.cardapio.size(); i++) {
            ItemCardapio item = estabelecimentoSelecionado.cardapio.get(i);
            Integer qtd = quantidadesComanda.get(item.id);
            if (qtd != null && qtd > 0) {
                fala.append(qtd).append(" ").append(item.nome)
                        .append(" custando ").append(formatarMoedaCentavos(qtd * item.precoCentavos)).append("; ");
            }
        }
        fala.append("Subtotal dos itens: ").append(formatarMoedaCentavos(ultimoSubtotalCentavos)).append(". ");
        if (adicionarTaxaDezPorCento) {
            fala.append("Taxa de dez por cento incluída: ").append(formatarMoedaCentavos(ultimaTaxaServicoCentavos)).append(". ");
        } else {
            fala.append("Sem taxa de dez por cento. ");
        }
        fala.append("Soma total da comanda: ").append(formatarMoedaCentavos(ultimoTotalCentavos)).append(". ");
        if (ultimoTotalCentavos > tetoGastosCentavos) {
            fala.append("Atenção: o valor passou do seu teto de ").append(formatarMoedaCentavos(tetoGastosCentavos)).append(". ");
        } else {
            fala.append("Valor dentro do seu teto de gastos. ");
        }
        fala.append("Deseja adicionar ou alterar os dez por cento antes de pagar?");

        atualizarPainelEFalar(fala.toString(), fala.toString());
        perguntarTaxaDezPorCento(false, null);
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
            tvInfo.setText("ℹ️ Ao tocar no botão verde acima, o código Pix Copia e Cola será copiado automaticamente e o seletor de aplicativos de bancos do seu celular será aberto.");
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
        if (ultimoSubtotalCentavos <= 0) {
            String msg = "Sua comanda ainda está vazia! Escolha pelo menos um item do cardápio antes de pagar no " + banco.nomeBanco + ".";
            atualizarPainelEFalar(msg, msg);
            return;
        }
        perguntarTaxaDezPorCento(true, banco);
    }

    private void executarAberturaBancoComPix(BancoAppInfo banco) {
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
        if (ultimoSubtotalCentavos <= 0) {
            String msg = "Sua comanda está vazia. Escolha os pratos ou bebidas no cardápio acima para a IA somar antes de abrir o aplicativo do banco!";
            atualizarPainelEFalar(msg, msg);
            return;
        }
        perguntarTaxaDezPorCento(true, null);
    }

    private void prosseguirPagamentoNosBancos() {
        copiarPixParaAreaDeTransferencia();
        final String valorFmt = formatarMoedaCentavos(ultimoTotalCentavos);
        final List<BancoAppInfo> bancosInstalados = detectarBancosInstaladosNoCelular();

        if (bancosInstalados.size() == 1) {
            executarAberturaBancoComPix(bancosInstalados.get(0));
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
                            executarAberturaBancoComPix(bancosInstalados.get(which));
                        }
                    })
                    .setNegativeButton("Fechar", null)
                    .show();
            return;
        }

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
                    "Quero um bar no Recife Antigo em Pernambuco com música baixa",
                    "Localizar bar no Alto da Sé em Olinda",
                    "Buscar bar em Caruaru no Alto do Moura",
                    "Localizar Bar do Cuscuz em Campina Grande na Paraíba",
                    "Buscar bar na Vila Madalena em São Paulo",
                    "Localizar restaurante na Estação das Docas em Belém do Pará"
            };
            new AlertDialog.Builder(this)
                    .setTitle("🎤 Escolha ou Simule a Busca por Voz:")
                    .setItems(frasesBusca, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            etBuscaQualquerBar.setText(frasesBusca[which]);
                            localizarQualquerBarComCardapio(frasesBusca[which]);
                        }
                    })
                    .show();
        } else {
            final String[] frasesPedido = new String[]{
                    "Quero um caldinho de feijão e um suco de laranja natural",
                    "Me vê dois chopes gelados e uma macaxeira frita com queijo coalho",
                    "Adicionar o prato principal do cardápio e um suco natural",
                    "Quero uma porção de entrada e dois chopes gelados"
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
                    etBuscaQualquerBar.setText(textoFalado);
                    localizarQualquerBarComCardapio(textoFalado);
                } else if (requestCode == REQ_VOZ_PEDIDO_CARDAPIO) {
                    processarPedidoVozNoCardapio(textoFalado);
                }
            }
        }
    }

    private void processarPedidoVozNoCardapio(String fraseFalada) {
        String norm = normalizarTexto(fraseFalada);
        Estabelecimento estAlvo = estabelecimentoSelecionado;

        List<ItemCardapio> itensIdentificados = encontrarItensNaFrase(norm, estAlvo);
        if (itensIdentificados.isEmpty() && estAlvo != null && !estAlvo.cardapio.isEmpty()) {
            for (int i = 0; i < estAlvo.cardapio.size(); i++) {
                ItemCardapio item = estAlvo.cardapio.get(i);
                if (norm.contains(normalizarTexto(item.categoria))) {
                    itensIdentificados.add(item);
                }
            }
        }

        if (itensIdentificados.isEmpty()) {
            String msgErro = "🎙️ Você disse: \"" + fraseFalada + "\". Não encontrei esse item no cardápio de " + estAlvo.nome + ". Toque diretamente nos botões do cardápio abaixo.";
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
        regiaoFiltroAtual = "Nordeste";
        ufFiltroAtual = "PE";
        cidadeFiltroAtual = "Recife";
        filtrarApenasSilenciosos = true;
        atualizarRotulosFiltros();
        selecionarEstabelecimento(catalogoEstabelecimentos.get(0), false);
        String msg = "[Caso 1 · Busca de Bar Acústico]\n\n" +
                "Voz Reconhecida: 'Quero um bar no Recife Antigo com música baixa e espaço tranquilo para conversar.'\n\n" +
                "Recuperação em Grafo (GraphRAG):\n" +
                "• Região: Nordeste | Estado: Pernambuco (PE) | Cidade: Recife\n" +
                "• Bar Selecionado: Bar Cais do Sertão & Chopp (Recife Antigo)\n" +
                "• Ruído Médio: 58 dB (música instrumental ambiente baixa)\n" +
                "• Cardápio aberto e botão do Google Maps pronto para traçar a rota!";
        atualizarPainelEFalar(msg, "Bar encontrado em Recife, Pernambuco: Bar Cais do Sertão, com cinquenta e oito decibéis e cardápio aberto.");
    }

    private void executarCaso2() {
        regiaoFiltroAtual = "Nordeste";
        ufFiltroAtual = "PE";
        cidadeFiltroAtual = "Recife";
        filtrarApenasSilenciosos = false;
        atualizarRotulosFiltros();
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
        regiaoFiltroAtual = "Nordeste";
        ufFiltroAtual = "PE";
        cidadeFiltroAtual = "Recife";
        filtrarApenasSilenciosos = false;
        atualizarRotulosFiltros();
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

    // --- UTILITÁRIOS DETERMINÍSTICOS (HAVERSINE, PIX BR CODE CRC16, TEXTO) ---

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

    private static String capitalizarFrase(String s) {
        if (s == null || s.isEmpty()) return "";
        String[] partes = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            String p = partes[i];
            if (p.length() > 2 || i == 0) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            } else {
                sb.append(p);
            }
            if (i < partes.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    private void atualizarPainelEFalar(String textoVisual, String textoFalado) {
        tvResultado.setText("🔊 " + textoVisual);
        tvResultado.setContentDescription(textoFalado);
        falar(textoFalado);
    }

    /**
     * Envia o anúncio diretamente para o Leitor de Tela nativo do Sistema Android (TalkBack / Assistente de Voz),
     * sem criar um sintetizador TextToSpeech separado que atropele o leitor do sistema.
     */
    private void falar(String texto) {
        if (texto == null || texto.isEmpty()) return;
        if (tvResultado != null) {
            tvResultado.setContentDescription(texto);
            tvResultado.announceForAccessibility(texto);
        }
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            try {
                AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                event.setClassName(getClass().getName());
                event.setPackageName(getPackageName());
                event.getText().add(texto);
                accessibilityManager.sendAccessibilityEvent(event);
            } catch (Exception ignored) {
            }
        }
    }
}

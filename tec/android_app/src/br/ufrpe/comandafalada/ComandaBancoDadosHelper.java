package br.ufrpe.comandafalada;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Banco de Dados Relacional Nativo SQLite do Comanda Falada · Passo à Frente.
 * Garante persistência offline estruturada de:
 * 1. Estabelecimentos (catálogo nacional + locais importados do Google Maps / criados pelo usuário);
 * 2. Itens completos de cardápio (pratos, bebidas, sobremesas, preços em centavos e palavras-chave);
 * 3. Estado da Comanda Ativa (itens escolhidos, opção dos 10%, teto de gastos e local aberto);
 * 4. Histórico de Comandas e Pagamentos Pix realizados pelo usuário.
 */
public class ComandaBancoDadosHelper extends SQLiteOpenHelper {

    public static final String NOME_BANCO = "comanda_falada.db";
    public static final int VERSAO_BANCO = 1;

    public static class RegistroHistoricoComanda {
        public final long id;
        public final String dataHora;
        public final String nomeEstabelecimento;
        public final String cidadeUf;
        public final String resumoItens;
        public final int subtotalCentavos;
        public final boolean incluiuDezPorCento;
        public final int taxaCentavos;
        public final int totalCentavos;
        public final String senhaEntrega;

        public RegistroHistoricoComanda(
                long id,
                String dataHora,
                String nomeEstabelecimento,
                String cidadeUf,
                String resumoItens,
                int subtotalCentavos,
                boolean incluiuDezPorCento,
                int taxaCentavos,
                int totalCentavos,
                String senhaEntrega
        ) {
            this.id = id;
            this.dataHora = dataHora;
            this.nomeEstabelecimento = nomeEstabelecimento;
            this.cidadeUf = cidadeUf;
            this.resumoItens = resumoItens;
            this.subtotalCentavos = subtotalCentavos;
            this.incluiuDezPorCento = incluiuDezPorCento;
            this.taxaCentavos = taxaCentavos;
            this.totalCentavos = totalCentavos;
            this.senhaEntrega = senhaEntrega;
        }
    }

    public static class EstadoSessaoSalva {
        public final String idEstabelecimentoAtual;
        public final int tetoGastosCentavos;
        public final boolean adicionarTaxaDezPorCento;

        public EstadoSessaoSalva(String idEstabelecimentoAtual, int tetoGastosCentavos, boolean adicionarTaxaDezPorCento) {
            this.idEstabelecimentoAtual = idEstabelecimentoAtual;
            this.tetoGastosCentavos = tetoGastosCentavos;
            this.adicionarTaxaDezPorCento = adicionarTaxaDezPorCento;
        }
    }

    public ComandaBancoDadosHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS estabelecimentos (" +
                        "id TEXT PRIMARY KEY, " +
                        "nome TEXT NOT NULL, " +
                        "tipo TEXT NOT NULL, " +
                        "regiao TEXT NOT NULL, " +
                        "estado TEXT NOT NULL, " +
                        "uf TEXT NOT NULL, " +
                        "cidade TEXT NOT NULL, " +
                        "bairro TEXT NOT NULL, " +
                        "chave_pix TEXT NOT NULL, " +
                        "latitude REAL NOT NULL, " +
                        "longitude REAL NOT NULL, " +
                        "decibeis_medio INTEGER NOT NULL, " +
                        "nivel_ruido TEXT NOT NULL, " +
                        "tipo_som TEXT NOT NULL, " +
                        "acessibilidade TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS cardapio_itens (" +
                        "id TEXT PRIMARY KEY, " +
                        "estabelecimento_id TEXT NOT NULL, " +
                        "ordem INTEGER NOT NULL, " +
                        "nome TEXT NOT NULL, " +
                        "categoria TEXT NOT NULL, " +
                        "preco_centavos INTEGER NOT NULL, " +
                        "palavras_chave TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS comanda_ativa (" +
                        "estabelecimento_id TEXT NOT NULL, " +
                        "item_id TEXT NOT NULL, " +
                        "quantidade INTEGER NOT NULL, " +
                        "PRIMARY KEY (estabelecimento_id, item_id)" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS configuracoes_sessao (" +
                        "chave TEXT PRIMARY KEY, " +
                        "estabelecimento_atual_id TEXT, " +
                        "teto_centavos INTEGER NOT NULL, " +
                        "taxa_dez_por_cento INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS historico_comandas (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "data_hora TEXT NOT NULL, " +
                        "estabelecimento_nome TEXT NOT NULL, " +
                        "cidade_uf TEXT NOT NULL, " +
                        "resumo_itens TEXT NOT NULL, " +
                        "subtotal_centavos INTEGER NOT NULL, " +
                        "incluiu_dez_por_cento INTEGER NOT NULL, " +
                        "taxa_centavos INTEGER NOT NULL, " +
                        "total_centavos INTEGER NOT NULL, " +
                        "senha_entrega TEXT NOT NULL" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    /**
     * Salva ou sincroniza uma lista de estabelecimentos e todos os itens de seus cardápios no SQLite.
     */
    public void sincronizarCatalogoNoBanco(List<MainActivity.Estabelecimento> lista) {
        if (lista == null || lista.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < lista.size(); i++) {
                salvarEstabelecimentoInterno(db, lista.get(i));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void salvarOuAtualizarEstabelecimento(MainActivity.Estabelecimento est) {
        if (est == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            salvarEstabelecimentoInterno(db, est);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void salvarEstabelecimentoInterno(SQLiteDatabase db, MainActivity.Estabelecimento est) {
        ContentValues cv = new ContentValues();
        cv.put("id", est.id);
        cv.put("nome", est.nome);
        cv.put("tipo", est.tipo);
        cv.put("regiao", est.regiao);
        cv.put("estado", est.estado);
        cv.put("uf", est.uf);
        cv.put("cidade", est.cidade);
        cv.put("bairro", est.bairro);
        cv.put("chave_pix", est.chavePix);
        cv.put("latitude", est.latitude);
        cv.put("longitude", est.longitude);
        cv.put("decibeis_medio", est.decibeisMedio);
        cv.put("nivel_ruido", est.nivelRuido);
        cv.put("tipo_som", est.tipoSom);
        cv.put("acessibilidade", est.acessibilidade);
        db.insertWithOnConflict("estabelecimentos", null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        for (int i = 0; i < est.cardapio.size(); i++) {
            MainActivity.ItemCardapio it = est.cardapio.get(i);
            ContentValues cvItem = new ContentValues();
            cvItem.put("id", it.id);
            cvItem.put("estabelecimento_id", est.id);
            cvItem.put("ordem", i);
            cvItem.put("nome", it.nome);
            cvItem.put("categoria", it.categoria);
            cvItem.put("preco_centavos", it.precoCentavos);
            cvItem.put("palavras_chave", juntarPalavrasChave(it.palavrasChave));
            db.insertWithOnConflict("cardapio_itens", null, cvItem, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void salvarNovoItemCardapioNoBanco(String estabelecimentoId, MainActivity.ItemCardapio item, int ordem) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cvItem = new ContentValues();
        cvItem.put("id", item.id);
        cvItem.put("estabelecimento_id", estabelecimentoId);
        cvItem.put("ordem", ordem);
        cvItem.put("nome", item.nome);
        cvItem.put("categoria", item.categoria);
        cvItem.put("preco_centavos", item.precoCentavos);
        cvItem.put("palavras_chave", juntarPalavrasChave(item.palavrasChave));
        db.insertWithOnConflict("cardapio_itens", null, cvItem, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Carrega todos os estabelecimentos e seus cardápios salvos no SQLite.
     */
    public List<MainActivity.Estabelecimento> carregarEstabelecimentosDoBanco() {
        List<MainActivity.Estabelecimento> resultado = new ArrayList<MainActivity.Estabelecimento>();
        Map<String, MainActivity.Estabelecimento> mapaPorId = new LinkedHashMap<String, MainActivity.Estabelecimento>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cEst = db.rawQuery("SELECT * FROM estabelecimentos", null);
        try {
            while (cEst.moveToNext()) {
                MainActivity.Estabelecimento est = new MainActivity.Estabelecimento(
                        cEst.getString(cEst.getColumnIndexOrThrow("id")),
                        cEst.getString(cEst.getColumnIndexOrThrow("nome")),
                        cEst.getString(cEst.getColumnIndexOrThrow("tipo")),
                        cEst.getString(cEst.getColumnIndexOrThrow("regiao")),
                        cEst.getString(cEst.getColumnIndexOrThrow("estado")),
                        cEst.getString(cEst.getColumnIndexOrThrow("uf")),
                        cEst.getString(cEst.getColumnIndexOrThrow("cidade")),
                        cEst.getString(cEst.getColumnIndexOrThrow("bairro")),
                        cEst.getString(cEst.getColumnIndexOrThrow("chave_pix")),
                        cEst.getDouble(cEst.getColumnIndexOrThrow("latitude")),
                        cEst.getDouble(cEst.getColumnIndexOrThrow("longitude")),
                        cEst.getInt(cEst.getColumnIndexOrThrow("decibeis_medio")),
                        cEst.getString(cEst.getColumnIndexOrThrow("nivel_ruido")),
                        cEst.getString(cEst.getColumnIndexOrThrow("tipo_som")),
                        cEst.getString(cEst.getColumnIndexOrThrow("acessibilidade"))
                );
                resultado.add(est);
                mapaPorId.put(est.id, est);
            }
        } finally {
            cEst.close();
        }

        if (resultado.isEmpty()) {
            return resultado;
        }

        Cursor cItens = db.rawQuery("SELECT * FROM cardapio_itens ORDER BY ordem ASC", null);
        try {
            while (cItens.moveToNext()) {
                String estId = cItens.getString(cItens.getColumnIndexOrThrow("estabelecimento_id"));
                MainActivity.Estabelecimento alvo = mapaPorId.get(estId);
                if (alvo != null) {
                    String idItem = cItens.getString(cItens.getColumnIndexOrThrow("id"));
                    String nomeItem = cItens.getString(cItens.getColumnIndexOrThrow("nome"));
                    String catItem = cItens.getString(cItens.getColumnIndexOrThrow("categoria"));
                    int preco = cItens.getInt(cItens.getColumnIndexOrThrow("preco_centavos"));
                    String kwBruto = cItens.getString(cItens.getColumnIndexOrThrow("palavras_chave"));
                    String[] kws = separarPalavrasChave(kwBruto, nomeItem);
                    alvo.cardapio.add(new MainActivity.ItemCardapio(idItem, nomeItem, catItem, preco, kws));
                }
            }
        } finally {
            cItens.close();
        }

        return resultado;
    }

    /**
     * Persiste a comanda ativa do estabelecimento atual no SQLite.
     */
    public void salvarComandaAtivaNoBanco(String estabelecimentoId, Map<String, Integer> quantidades) {
        if (estabelecimentoId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("comanda_ativa", "estabelecimento_id = ?", new String[]{estabelecimentoId});
            if (quantidades != null) {
                for (Map.Entry<String, Integer> entry : quantidades.entrySet()) {
                    if (entry.getValue() != null && entry.getValue() > 0) {
                        ContentValues cv = new ContentValues();
                        cv.put("estabelecimento_id", estabelecimentoId);
                        cv.put("item_id", entry.getKey());
                        cv.put("quantidade", entry.getValue());
                        db.insertWithOnConflict("comanda_ativa", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Map<String, Integer> carregarComandaAtivaDoBanco(String estabelecimentoId) {
        Map<String, Integer> mapa = new LinkedHashMap<String, Integer>();
        if (estabelecimentoId == null) return mapa;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT item_id, quantidade FROM comanda_ativa WHERE estabelecimento_id = ?",
                new String[]{estabelecimentoId}
        );
        try {
            while (c.moveToNext()) {
                String itemId = c.getString(0);
                int qtd = c.getInt(1);
                if (qtd > 0) {
                    mapa.put(itemId, qtd);
                }
            }
        } finally {
            c.close();
        }
        return mapa;
    }

    public void salvarConfiguracaoSessao(String idEstabelecimentoAtual, int tetoGastosCentavos, boolean adicionarTaxaDezPorCento) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("chave", "sessao_principal");
        cv.put("estabelecimento_atual_id", idEstabelecimentoAtual);
        cv.put("teto_centavos", tetoGastosCentavos);
        cv.put("taxa_dez_por_cento", adicionarTaxaDezPorCento ? 1 : 0);
        db.insertWithOnConflict("configuracoes_sessao", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public EstadoSessaoSalva carregarConfiguracaoSessao() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT estabelecimento_atual_id, teto_centavos, taxa_dez_por_cento FROM configuracoes_sessao WHERE chave = 'sessao_principal'",
                null
        );
        try {
            if (c.moveToFirst()) {
                String idEst = c.getString(0);
                int teto = c.getInt(1);
                boolean taxa = c.getInt(2) == 1;
                return new EstadoSessaoSalva(idEst, teto > 0 ? teto : 10000, taxa);
            }
        } finally {
            c.close();
        }
        return null;
    }

    /**
     * Registra uma comanda no histórico permanente do SQLite.
     */
    public long registrarComandaNoHistorico(
            String nomeEstabelecimento,
            String cidadeUf,
            String resumoItens,
            int subtotalCentavos,
            boolean incluiuDezPorCento,
            int taxaCentavos,
            int totalCentavos,
            String senhaEntrega
    ) {
        SQLiteDatabase db = getWritableDatabase();
        String dataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(new Date());
        ContentValues cv = new ContentValues();
        cv.put("data_hora", dataHora);
        cv.put("estabelecimento_nome", nomeEstabelecimento);
        cv.put("cidade_uf", cidadeUf);
        cv.put("resumo_itens", resumoItens);
        cv.put("subtotal_centavos", subtotalCentavos);
        cv.put("incluiu_dez_por_cento", incluiuDezPorCento ? 1 : 0);
        cv.put("taxa_centavos", taxaCentavos);
        cv.put("total_centavos", totalCentavos);
        cv.put("senha_entrega", senhaEntrega);
        return db.insert("historico_comandas", null, cv);
    }

    public List<RegistroHistoricoComanda> listarHistoricoComandas() {
        List<RegistroHistoricoComanda> lista = new ArrayList<RegistroHistoricoComanda>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM historico_comandas ORDER BY id DESC LIMIT 50", null);
        try {
            while (c.moveToNext()) {
                lista.add(new RegistroHistoricoComanda(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("data_hora")),
                        c.getString(c.getColumnIndexOrThrow("estabelecimento_nome")),
                        c.getString(c.getColumnIndexOrThrow("cidade_uf")),
                        c.getString(c.getColumnIndexOrThrow("resumo_itens")),
                        c.getInt(c.getColumnIndexOrThrow("subtotal_centavos")),
                        c.getInt(c.getColumnIndexOrThrow("incluiu_dez_por_cento")) == 1,
                        c.getInt(c.getColumnIndexOrThrow("taxa_centavos")),
                        c.getInt(c.getColumnIndexOrThrow("total_centavos")),
                        c.getString(c.getColumnIndexOrThrow("senha_entrega"))
                ));
            }
        } finally {
            c.close();
        }
        return lista;
    }

    public void limparHistoricoComandas() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("historico_comandas", null, null);
    }

    public int contarEstabelecimentosNoBanco() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM estabelecimentos", null);
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            c.close();
        }
        return 0;
    }

    public int contarItensCardapioNoBanco() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM cardapio_itens", null);
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            c.close();
        }
        return 0;
    }

    public int contarHistoricoNoBanco() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM historico_comandas", null);
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            c.close();
        }
        return 0;
    }

    private String juntarPalavrasChave(String[] kws) {
        if (kws == null || kws.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kws.length; i++) {
            if (kws[i] != null) {
                sb.append(kws[i].trim());
                if (i < kws.length - 1) sb.append("|");
            }
        }
        return sb.toString();
    }

    private String[] separarPalavrasChave(String bruto, String fallbackNome) {
        if (bruto == null || bruto.trim().isEmpty()) {
            return new String[]{fallbackNome != null ? fallbackNome : "item"};
        }
        return bruto.split("\\|");
    }
}

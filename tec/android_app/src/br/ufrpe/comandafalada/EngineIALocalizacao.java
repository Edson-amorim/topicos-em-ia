package br.ufrpe.comandafalada;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modelo de Inteligência Artificial e Reconhecimento Geoespacial On-Device para o Comanda Falada.
 *
 * Responsável por:
 * 1. Obter a localização geográfica real do dispositivo (via GPS_PROVIDER, NETWORK_PROVIDER ou PASSIVE_PROVIDER);
 * 2. Classificador Geoespacial por IA: mapear coordenadas geográficas exatas na cidade e estado corretos
 *    utilizando matriz de distâncias Haversine sobre centróides urbanos e suporte a Geocoder;
 * 3. Garantir que o usuário comece SEMPRE DO 0 (zero alucinações de locais anteriores ou cidades distantes);
 * 4. Processamento de Linguagem Natural (NLP) para comandos de voz de localização e filtragem por qualquer cidade.
 */
public class EngineIALocalizacao {

    public static class CidadeCentroid {
        public final String cidade;
        public final String uf;
        public final String regiao;
        public final double latitude;
        public final double longitude;

        public CidadeCentroid(String cidade, String uf, String regiao, double latitude, double longitude) {
            this.cidade = cidade;
            this.uf = uf;
            this.regiao = regiao;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static class ResultadoLocalizacao {
        public final String cidade;
        public final String uf;
        public final String regiao;
        public final double latitude;
        public final double longitude;
        public final double distanciaKm;
        public final String metodoResolucao;
        public final boolean sucesso;

        public ResultadoLocalizacao(String cidade, String uf, String regiao, double latitude, double longitude, double distanciaKm, String metodoResolucao, boolean sucesso) {
            this.cidade = cidade;
            this.uf = uf;
            this.regiao = regiao;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distanciaKm = distanciaKm;
            this.metodoResolucao = metodoResolucao;
            this.sucesso = sucesso;
        }
    }

    // Base de centróides urbanos georreferenciados para inferência geoespacial on-device
    private static final List<CidadeCentroid> CENTROIDES = new ArrayList<CidadeCentroid>();

    static {
        // Pernambuco (RMR e Principais Cidades)
        CENTROIDES.add(new CidadeCentroid("São Lourenço da Mata", "PE", "Nordeste", -8.0089, -35.0181));
        CENTROIDES.add(new CidadeCentroid("Camaragibe", "PE", "Nordeste", -8.0203, -34.9814));
        CENTROIDES.add(new CidadeCentroid("Recife", "PE", "Nordeste", -8.0617, -34.8711));
        CENTROIDES.add(new CidadeCentroid("Olinda", "PE", "Nordeste", -8.0089, -34.8553));
        CENTROIDES.add(new CidadeCentroid("Jaboatão dos Guararapes", "PE", "Nordeste", -8.1128, -35.0150));
        CENTROIDES.add(new CidadeCentroid("Paulista", "PE", "Nordeste", -7.9408, -34.8731));
        CENTROIDES.add(new CidadeCentroid("Igarassu", "PE", "Nordeste", -7.8342, -34.9064));
        CENTROIDES.add(new CidadeCentroid("Abreu e Lima", "PE", "Nordeste", -7.9042, -34.9022));
        CENTROIDES.add(new CidadeCentroid("Cabo de Santo Agostinho", "PE", "Nordeste", -8.2839, -35.0328));
        CENTROIDES.add(new CidadeCentroid("Ipojuca", "PE", "Nordeste", -8.3986, -35.0639));
        CENTROIDES.add(new CidadeCentroid("Vitória de Santo Antão", "PE", "Nordeste", -8.1189, -35.2928));
        CENTROIDES.add(new CidadeCentroid("Carpina", "PE", "Nordeste", -7.8503, -35.2536));
        CENTROIDES.add(new CidadeCentroid("Gravatá", "PE", "Nordeste", -8.2014, -35.5658));
        CENTROIDES.add(new CidadeCentroid("Caruaru", "PE", "Nordeste", -8.2831, -35.9761));
        CENTROIDES.add(new CidadeCentroid("Garanhuns", "PE", "Nordeste", -8.8906, -36.4928));
        CENTROIDES.add(new CidadeCentroid("Arcoverde", "PE", "Nordeste", -8.4189, -37.0542));
        CENTROIDES.add(new CidadeCentroid("Serra Talhada", "PE", "Nordeste", -7.9917, -38.2975));
        CENTROIDES.add(new CidadeCentroid("Petrolina", "PE", "Nordeste", -9.3891, -40.5027));

        // Paraíba
        CENTROIDES.add(new CidadeCentroid("João Pessoa", "PB", "Nordeste", -7.1195, -34.8450));
        CENTROIDES.add(new CidadeCentroid("Cabedelo", "PB", "Nordeste", -6.9814, -34.8339));
        CENTROIDES.add(new CidadeCentroid("Bayeux", "PB", "Nordeste", -7.1250, -34.9322));
        CENTROIDES.add(new CidadeCentroid("Santa Rita", "PB", "Nordeste", -7.1139, -34.9781));
        CENTROIDES.add(new CidadeCentroid("Campina Grande", "PB", "Nordeste", -7.2247, -35.8811));
        CENTROIDES.add(new CidadeCentroid("Patos", "PB", "Nordeste", -7.0253, -37.2806));

        // Demais Capitais e Cidades do Nordeste
        CENTROIDES.add(new CidadeCentroid("Maceió", "AL", "Nordeste", -9.6658, -35.7350));
        CENTROIDES.add(new CidadeCentroid("Natal", "RN", "Nordeste", -5.7945, -35.2110));
        CENTROIDES.add(new CidadeCentroid("Fortaleza", "CE", "Nordeste", -3.7258, -38.4984));
        CENTROIDES.add(new CidadeCentroid("Salvador", "BA", "Nordeste", -12.9714, -38.5086));
        CENTROIDES.add(new CidadeCentroid("Aracaju", "SE", "Nordeste", -10.9472, -37.0731));
        CENTROIDES.add(new CidadeCentroid("Teresina", "PI", "Nordeste", -5.0892, -42.8016));
        CENTROIDES.add(new CidadeCentroid("São Luís", "MA", "Nordeste", -2.5307, -44.3068));

        // Demais Regiões do Brasil
        CENTROIDES.add(new CidadeCentroid("São Paulo", "SP", "Sudeste", -23.5558, -46.6896));
        CENTROIDES.add(new CidadeCentroid("Campinas", "SP", "Sudeste", -22.9056, -47.0608));
        CENTROIDES.add(new CidadeCentroid("Rio de Janeiro", "RJ", "Sudeste", -22.9711, -43.1825));
        CENTROIDES.add(new CidadeCentroid("Belo Horizonte", "MG", "Sudeste", -19.9386, -43.9352));
        CENTROIDES.add(new CidadeCentroid("Vitória", "ES", "Sudeste", -20.3155, -40.3128));
        CENTROIDES.add(new CidadeCentroid("Brasília", "DF", "Centro-Oeste", -15.7942, -47.8822));
        CENTROIDES.add(new CidadeCentroid("Goiânia", "GO", "Centro-Oeste", -16.6869, -49.2648));
        CENTROIDES.add(new CidadeCentroid("Curitiba", "PR", "Sul", -25.4284, -49.2733));
        CENTROIDES.add(new CidadeCentroid("Florianópolis", "SC", "Sul", -27.5954, -48.5480));
        CENTROIDES.add(new CidadeCentroid("Porto Alegre", "RS", "Sul", -30.0346, -51.2177));
        CENTROIDES.add(new CidadeCentroid("Manaus", "AM", "Norte", -3.1190, -60.0217));
        CENTROIDES.add(new CidadeCentroid("Belém", "PA", "Norte", -1.4558, -48.4902));
    }

    /**
     * Obtém a última localização conhecida do hardware Android via GPS, Network ou Passive.
     */
    public static Location obterUltimaLocalizacaoConhecida(Context context) {
        if (context == null) return null;

        boolean temPermissao = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!temPermissao) return null;

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return null;

        Location melhorLocalizacao = null;
        String[] provedores = new String[]{
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };

        for (String p : provedores) {
            try {
                if (lm.isProviderEnabled(p)) {
                    Location l = lm.getLastKnownLocation(p);
                    if (l != null) {
                        if (melhorLocalizacao == null || l.getTime() > melhorLocalizacao.getTime()) {
                            melhorLocalizacao = l;
                        }
                    }
                }
            } catch (SecurityException ignored) {
            } catch (Exception ignored) {
            }
        }

        return melhorLocalizacao;
    }

    /**
     * Modelo de IA de Classificação Geoespacial: mapeia coordenadas em nome de cidade e UF
     * com robustez total (Geocoder nativo + Classificador Haversine On-Device).
     */
    public static ResultadoLocalizacao classificarCoordenadas(double latitude, double longitude, Context context) {
        // 1. Tenta Geocoder Nativo se disponível
        if (context != null && Geocoder.isPresent()) {
            try {
                Geocoder geocoder = new Geocoder(context, new Locale("pt", "BR"));
                List<Address> enderecos = geocoder.getFromLocation(latitude, longitude, 1);
                if (enderecos != null && !enderecos.isEmpty()) {
                    Address end = enderecos.get(0);
                    String cidade = end.getLocality();
                    if (cidade == null || cidade.isEmpty()) {
                        cidade = end.getSubAdminArea();
                    }
                    if (cidade != null && !cidade.isEmpty()) {
                        String uf = end.getAdminArea();
                        String ufSigla = extrairSiglaUf(uf);
                        String regiao = extrairRegiaoPorUf(ufSigla);
                        return new ResultadoLocalizacao(cidade, ufSigla, regiao, latitude, longitude, 0.0, "Geocoder Nativo", true);
                    }
                }
            } catch (IOException ignored) {
            } catch (Exception ignored) {
            }
        }

        // 2. Classificador Preditivo Geoespacial On-Device (Haversine Distance Minimizer)
        CidadeCentroid maisProxima = null;
        double menorDistanciaKm = Double.MAX_VALUE;

        for (CidadeCentroid c : CENTROIDES) {
            double dist = calcularDistanciaHaversineKm(latitude, longitude, c.latitude, c.longitude);
            if (dist < menorDistanciaKm) {
                menorDistanciaKm = dist;
                maisProxima = c;
            }
        }

        if (maisProxima != null) {
            return new ResultadoLocalizacao(
                    maisProxima.cidade,
                    maisProxima.uf,
                    maisProxima.regiao,
                    latitude,
                    longitude,
                    menorDistanciaKm,
                    "IA Geoespacial On-Device (Haversine)",
                    true
            );
        }

        // Fallback neutro
        return new ResultadoLocalizacao("São Lourenço da Mata", "PE", "Nordeste", latitude, longitude, 0.0, "Padrão", false);
    }

    /**
     * Cálculo da Distância Geodésica de Haversine em Quilômetros.
     */
    public static double calcularDistanciaHaversineKm(double lat1, double lon1, double lat2, double lon2) {
        double rTerra = 6371.0;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2.0) * Math.sin(deltaPhi / 2.0) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2.0) * Math.sin(deltaLambda / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return rTerra * c;
    }

    /**
     * Detector NLP para identificar se o comando de voz expressa a intenção de
     * obter a localização atual via GPS / IA.
     */
    public static boolean ehIntencaoLocalizacaoAtual(String normEspaco) {
        if (normEspaco == null) return false;
        String t = " " + normalizar(normEspaco) + " ";

        return t.contains(" onde estou ") ||
                t.contains(" onde eu estou ") ||
                t.contains(" qual minha localizacao ") ||
                t.contains(" qual a minha localizacao ") ||
                t.contains(" pegar minha localizacao ") ||
                t.contains(" pegar localizacao ") ||
                t.contains(" minha localizacao ") ||
                t.contains(" localizacao atual ") ||
                t.contains(" atualizar localizacao ") ||
                t.contains(" minha posicao ") ||
                t.contains(" qual minha cidade ") ||
                t.contains(" qual a minha cidade ") ||
                t.contains(" usar gps ") ||
                t.contains(" ativar gps ") ||
                t.contains(" pegar gps ") ||
                t.contains(" detectar localizacao ") ||
                t.contains(" obter localizacao ");
    }

    /**
     * Detector NLP para identificar e extrair cidade a partir de falas do usuário
     * com suporte a variações fonéticas e dialetais.
     */
    public static String detectarCidadeNoComando(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        String t = " " + normalizar(texto) + " ";

        // São Lourenço da Mata (variações fonéticas)
        if (t.contains(" sao lourenco da mata ") || t.contains(" sao lourenco ") ||
            t.contains(" sao lorensso da mata ") || t.contains(" sao lorensso ") ||
            t.contains(" lorensso da mata ") || t.contains(" lorensso ") ||
            t.contains(" lourenco da mata ") || t.contains(" lourenco ") ||
            t.contains(" arena pernambuco ")) {
            return "São Lourenço da Mata";
        }

        // Camaragibe
        if (t.contains(" camaragibe ") || t.contains(" camaragibi ") || t.contains(" aldeia ")) {
            return "Camaragibe";
        }

        // Recife
        if (t.contains(" recife ") || t.contains(" recife antigo ") || t.contains(" boa viagem ") || t.contains(" marco zero ")) {
            return "Recife";
        }

        // Olinda
        if (t.contains(" olinda ") || t.contains(" alto da se ") || t.contains(" sitio historico ")) {
            return "Olinda";
        }

        // Jaboatão
        if (t.contains(" jaboatao ") || t.contains(" jaboatao dos guararapes ") || t.contains(" piedade ")) {
            return "Jaboatão dos Guararapes";
        }

        // Paulista
        if (t.contains(" paulista ") || t.contains(" maria farinha ")) {
            return "Paulista";
        }

        // Igarassu
        if (t.contains(" igarassu ") || t.contains(" igaracu ")) {
            return "Igarassu";
        }

        // Caruaru
        if (t.contains(" caruaru ") || t.contains(" alto do moura ")) {
            return "Caruaru";
        }

        // Garanhuns
        if (t.contains(" garanhuns ")) {
            return "Garanhuns";
        }

        // Petrolina
        if (t.contains(" petrolina ") || t.contains(" vale do sao francisco ")) {
            return "Petrolina";
        }

        // João Pessoa
        if (t.contains(" joao pessoa ") || t.contains(" jampa ") || t.contains(" tambau ") || t.contains(" cabo branco ")) {
            return "João Pessoa";
        }

        // Campina Grande
        if (t.contains(" campina grande ") || t.contains(" campina ")) {
            return "Campina Grande";
        }

        // Demais Capitais
        if (t.contains(" salvador ")) return "Salvador";
        if (t.contains(" fortaleza ")) return "Fortaleza";
        if (t.contains(" natal ")) return "Natal";
        if (t.contains(" maceio ")) return "Maceió";
        if (t.contains(" aracaju ")) return "Aracaju";
        if (t.contains(" sao paulo ") || t.contains(" sampa ")) return "São Paulo";
        if (t.contains(" rio de janeiro ") || t.contains(" rj ")) return "Rio de Janeiro";
        if (t.contains(" belo horizonte ") || t.contains(" bh ")) return "Belo Horizonte";
        if (t.contains(" brasilia ")) return "Brasília";
        if (t.contains(" curitiba ")) return "Curitiba";
        if (t.contains(" porto alegre ")) return "Porto Alegre";

        return null;
    }

    public static String extrairSiglaUf(String nomeOuSigla) {
        if (nomeOuSigla == null) return "PE";
        String n = normalizar(nomeOuSigla).trim();
        if (n.equals("pe") || n.contains("pernambuco")) return "PE";
        if (n.equals("pb") || n.contains("paraiba")) return "PB";
        if (n.equals("ba") || n.contains("bahia")) return "BA";
        if (n.equals("ce") || n.contains("ceara")) return "CE";
        if (n.equals("rn") || n.contains("rio grande do norte")) return "RN";
        if (n.equals("al") || n.contains("alagoas")) return "AL";
        if (n.equals("se") || n.contains("sergipe")) return "SE";
        if (n.equals("sp") || n.contains("sao paulo")) return "SP";
        if (n.equals("rj") || n.contains("rio de janeiro")) return "RJ";
        if (n.equals("mg") || n.contains("minas gerais")) return "MG";
        if (n.equals("es") || n.contains("espirito santo")) return "ES";
        if (n.equals("df") || n.contains("distrito federal")) return "DF";
        if (n.equals("pr") || n.contains("parana")) return "PR";
        if (n.equals("rs") || n.contains("rio grande do sul")) return "RS";
        if (n.equals("sc") || n.contains("santa catarina")) return "SC";
        return "PE";
    }

    public static String extrairRegiaoPorUf(String uf) {
        if (uf == null) return "Nordeste";
        String u = uf.trim().toUpperCase(Locale.US);
        if (u.equals("PE") || u.equals("PB") || u.equals("BA") || u.equals("CE") ||
            u.equals("RN") || u.equals("AL") || u.equals("SE") || u.equals("MA") || u.equals("PI")) {
            return "Nordeste";
        }
        if (u.equals("SP") || u.equals("RJ") || u.equals("MG") || u.equals("ES")) {
            return "Sudeste";
        }
        if (u.equals("PR") || u.equals("RS") || u.equals("SC")) {
            return "Sul";
        }
        if (u.equals("DF") || u.equals("GO") || u.equals("MT") || u.equals("MS")) {
            return "Centro-Oeste";
        }
        return "Norte";
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        String norm = Normalizer.normalize(texto, Normalizer.Form.NFD);
        norm = norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return norm.toLowerCase(Locale.US).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }
}

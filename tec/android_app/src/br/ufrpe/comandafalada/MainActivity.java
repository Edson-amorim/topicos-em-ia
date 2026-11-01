package br.ufrpe.comandafalada;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextView tvResultado;
    private Button btnCaso1;
    private Button btnCaso2;
    private Button btnCaso3;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResultado = findViewById(R.id.tvResultado);
        btnCaso1 = findViewById(R.id.btnCaso1);
        btnCaso2 = findViewById(R.id.btnCaso2);
        btnCaso3 = findViewById(R.id.btnCaso3);

        tts = new TextToSpeech(this, this);

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
    }

    private void executarCaso1() {
        String msg = "[Caso 1 · Busca de Bar Acústico]\n\n" +
                "Voz Reconhecida: 'Quero um bar no Recife Antigo com música baixa e espaço tranquilo para conversar.'\n\n" +
                "Recuperação em Grafo (GraphRAG):\n" +
                "• Bar Selecionado: Bar Cais do Sertão & Chopp\n" +
                "• Bairro: Recife Antigo\n" +
                "• Ruído Médio: 58 dB (música instrumental ambiente baixa)\n" +
                "• Acessibilidade: Calçada acessível, espaço amplo entre mesas, garçom atencioso.\n" +
                "• Distância GPS (Haversine): 130 metros da sua posição.";

        tvResultado.setText(msg);
        falar("Bar encontrado no Recife Antigo: Bar Cais do Sertão, com cinquenta e oito decibéis, música baixa e a cento e trinta metros.");
    }

    private void executarCaso2() {
        String copiaPix = "00020126480014br.gov.bcb.pix0126pix@barcaisdosertao.com.br520400005303986540522.005802BR5918Bar Cais do Sertao6006Recife62100506PEDIDO63046AB6";
        String msg = "[Caso 2 · Pedido e Comanda Exata]\n\n" +
                "Voz Reconhecida: 'Vou querer uma porção de caldinho de feijão e um suco de laranja natural, por favor.'\n\n" +
                "Itens Estruturados no Cardápio:\n" +
                "1. Caldinho de feijão: R$ 12,00\n" +
                "2. Suco de laranja natural: R$ 10,00\n\n" +
                "Regras Determinísticas:\n" +
                "• Total da Comanda: R$ 22,00 (sem erro de centavos)\n" +
                "• Teto de Gastos: R$ 50,00 (Dentro do limite)\n" +
                "• Senha Falada de Entrega: 8186\n" +
                "• Código Copia e Cola Pix (BR Code):\n" + copiaPix;

        tvResultado.setText(msg);
        falar("Pedido confirmado: um caldinho e um suco. Total: vinte e dois reais. Senha de entrega: oito um oito seis.");
    }

    private void executarCaso3() {
        String msg = "[Caso 3 · Teste com Ruído de Bar]\n\n" +
                "Áudio de Entrada: 'Me vê dois chopes gelados e uma porção de macaxeira frita com queijo coalho.'\n\n" +
                "Transcrição com Ruído Acústico: 'com 2 corpos gelados e uma corção de macaxeira frita com queijo coalho'\n\n" +
                "Onde a IA erra:\n" +
                "• O modelo trocou 'dois chopes gelados' por '2 corpos gelados' e 'uma porção' por 'uma corção'.\n" +
                "• A regra determinística não reconheceu o chope, computando apenas a macaxeira (R$ 34,00).\n" +
                "• Demonstra a necessidade de confirmação por voz na interface antes do pagamento Pix.";

        tvResultado.setText(msg);
        falar("Atenção: áudio com ruído. Apenas macaxeira frita foi confirmada. O chope não foi compreendido com clareza.");
    }

    private void falar(String texto) {
        if (tts != null) {
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "fala_id");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            tts.setLanguage(new Locale("pt", "BR"));
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

package br.com.alura.screenmatch.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URLEncoder;
import java.util.Scanner;

public class Traducao {
    private final ConsumoApi consumoApi;
    private final ConverteDados converteDados;

    public Traducao() {
        this.consumoApi = new ConsumoApi();
        this.converteDados = new ConverteDados();
    }

    public String obterTraducao(String texto) {
        try {
            // Monta a URL da API
            String url = "https://api.mymemory.translated.net/get?q=" + texto + "&langpair=en" + "%7C" + "pt-br";

            // Consume a API usando ConsumoApi
            String jsonResponse = consumoApi.obterDados(url);

            // Converte o JSON para objeto usando ConverteDados
            DadosTraducao dadosTraducao = converteDados.obterDados(jsonResponse, DadosTraducao.class);

            // Retorna o texto traduzido
            return dadosTraducao.getResponseData().getTranslatedText();

        } catch (Exception e) {
            System.err.println("Erro ao obter tradução: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        Traducao traducao = new Traducao();

        Scanner leitura = new Scanner(System.in);
        System.out.println("Digite o texto a ser traduzido: ");
        String texto = URLEncoder.encode(leitura.nextLine());
        leitura.close();
        String textoTraduzido = traducao.obterTraducao(texto);

        System.out.println("Texto traduzido: " + textoTraduzido);
    }
}

// Classe que mapeia o JSON principal
@JsonIgnoreProperties(ignoreUnknown = true)
class DadosTraducao {
    @JsonProperty("responseData")
    private ResponseData responseData;


    public ResponseData getResponseData() {
        return responseData;
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
class ResponseData {
    @JsonProperty("translatedText")
    private String translatedText;

    public String getTranslatedText() {
        return translatedText;
    }
}
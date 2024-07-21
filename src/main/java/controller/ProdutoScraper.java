package controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProdutoScraper {

    public static void main(String[] args) {
        // Criação do Scanner para ler a URL do usuário
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite a URL do produto:");
        String url = scanner.nextLine();
        scanner.close();

        // Realiza a requisição HTTP e obtém o HTML
        String html = fetchHTML(url);

        // Extrai as informações do produto
        if (html != null) {
            extractProductInfo(html);
        }
    }

    private static String fetchHTML(String url) {
        String html = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    html = EntityUtils.toString(entity);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }

    private static void extractProductInfo(String html) {
        Document document = Jsoup.parse(html);

        // Listas de seletores CSS para tentar encontrar os elementos desejados
        List<String> tituloSeletores = Arrays.asList(".product-name", ".product-title", "h1");
        List<String> precoSeletores = Arrays.asList(".saleInCents-value", ".price", ".product-price");
        List<String> imagemSeletores = Arrays.asList(".image-discount-badge", ".zoom-container__figure");
        List<String> descricaoSeletores = Arrays.asList(".feature__main-content", ".features--title", ".product-details");

        // Função auxiliar para encontrar o primeiro elemento não nulo a partir de uma lista de seletores
        Element tituloElemento = findFirstElement(document, tituloSeletores);
        Element precoElemento = findFirstElement(document, precoSeletores);
        Element imagemElemento = findFirstElement(document, imagemSeletores);
        Element descricaoElemento = findFirstElement(document, descricaoSeletores);

        String titulo = tituloElemento != null ? tituloElemento.text() : "N/A";
        String preco = precoElemento != null ? precoElemento.text() : "N/A";
        String imagemURL = imagemElemento != null ? imagemElemento.attr("src") : "N/A";
        String descricao = descricaoElemento != null ? descricaoElemento.text() : "N/A";

        // Encontrar todas as URLs das imagens do produto
        Elements imagemElementos = document.select(imagemSeletores.get(0));
        List<String> imagemURLs = imagemElementos.stream()
                .map(element -> element.attr("src"))
                .collect(Collectors.toList());


        // Exibe as informações do produto
        System.out.println("Título: " + titulo);
        System.out.println("Preço: " + preco);
        System.out.println("URL da Imagem: " + imagemURL);
        System.out.println("Descrição: " + descricao);
    }

    private static Element findFirstElement(Document document, List<String> seletores) {
        for (String seletor : seletores) {
            Element elemento = document.selectFirst(seletor);
            if (elemento != null) {
                return elemento;
            }
        }
        return null;
    }
}

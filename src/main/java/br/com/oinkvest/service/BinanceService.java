package br.com.oinkvest.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.com.oinkvest.dto.Candle;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BinanceService {

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<String> listarParesUsdt() {
        String url = "https://api.binance.com/api/v3/exchangeInfo";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("symbols")) {
            throw new RuntimeException("Erro ao obter os pares da Binance");
        }
        List<Map<String, Object>> symbols = (List<Map<String, Object>>) response.get("symbols");
        return symbols.stream()
                .filter(symbol -> "SPOT".equals(symbol.get("marketType")) ||
                        Boolean.TRUE.equals(symbol.get("isSpotTradingAllowed")))
                .filter(symbol -> ((String) symbol.get("symbol")).endsWith("USDT"))
                .map(symbol -> (String) symbol.get("symbol"))
                .sorted()
                .collect(Collectors.toList());
    }
@SuppressWarnings("unchecked")
    public String obterPrecoAtual(String symbol) {
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + symbol;
        Map<String, String> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("price")) {
            throw new RuntimeException("Erro ao obter o preço atual da Binance para o par: " + symbol);
        }
        return response.get("price");
    }

    @SuppressWarnings("unchecked")
    public String obterNomeCompleto(String symbol) {
        String url = "https://api.binance.com/api/v3/exchangeInfo";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        @SuppressWarnings("null")
        List<Map<String, Object>> symbols = (List<Map<String, Object>>) response.get("symbols");

        return symbols.stream()
                .filter(s -> symbol.equals(s.get("symbol")))
                .map(s -> (String) s.get("baseAsset")) // Ex: BTC
                .findFirst()
                .orElse(symbol);
    }

    public List<Candle> ultimosPrecos(String symbol, String interval, int limit) {
            String url = String.format("https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=%d", symbol, interval, limit);
            List<List<Object>> response = restTemplate.exchange(
        url, HttpMethod.GET, null, new ParameterizedTypeReference<List<List<Object>>>() {}).getBody();

    if (response == null) {
        throw new RuntimeException("Erro ao obter velas da Binance");
    }

    return response.stream().map(item -> new Candle(
        ((Number) item.get(0)).longValue(),        // openTime
        new BigDecimal((String) item.get(1)),      // open
        new BigDecimal((String) item.get(2)),      // high
        new BigDecimal((String) item.get(3)),      // low
        new BigDecimal((String) item.get(4)),      // close
        new BigDecimal((String) item.get(5))       // volume
    )).collect(Collectors.toList());
    }
}

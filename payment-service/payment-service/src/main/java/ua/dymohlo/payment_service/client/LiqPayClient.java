package ua.dymohlo.payment_service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiqPayClient {

    @Value("${liqpay.public-key}")
    private String publicKey;

    @Value("${liqpay.private-key}")
    private String privateKey;

    @Value("${liqpay.server-url}")
    private String serverUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_VERSION = "3";

    public Map<String, Object> processPayment(Map<String, Object> params) throws Exception {
        Map<String, Object> requestParams = new TreeMap<>(params);
        requestParams.put("version", API_VERSION);
        requestParams.put("public_key", publicKey);
        String jsonParams = objectMapper.writeValueAsString(requestParams);
        String data = Base64.getEncoder().encodeToString(jsonParams.getBytes(StandardCharsets.UTF_8));
        String signature = createSignature(data);

        log.info("Sending request to LiqPay API...");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("data", data);
        body.add("signature", signature);

        String response = webClient.post()
                .uri(serverUrl + "request")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Received response from LiqPay: {}", response);

        return objectMapper.readValue(response, HashMap.class);
    }

    public String createPaymentForm(Map<String, Object> params) throws JsonProcessingException {
        Map<String, Object> requestParams = new TreeMap<>(params);
        requestParams.put("version", API_VERSION);
        requestParams.put("public_key", publicKey);
        requestParams.put("sandbox", "1");

        String jsonParams = objectMapper.writeValueAsString(requestParams);
        String data = Base64.getEncoder().encodeToString(jsonParams.getBytes(StandardCharsets.UTF_8));

        String signature = createSignature(data);

        return generateHtmlForm(data, signature);
    }

    private String createSignature(String data) {
        try {
            String signString = privateKey + data + privateKey;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(signString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error creating signature", e);
        }
    }

    private String generateHtmlForm(String data, String signature) {
        return String.format(
                "<form method=\"post\" action=\"https://www.liqpay.ua/api/3/checkout\" accept-charset=\"utf-8\">\n" +
                        "<input type=\"hidden\" name=\"data\" value=\"%s\" />\n" +
                        "<input type=\"hidden\" name=\"signature\" value=\"%s\" />\n" +
                        "<input type=\"submit\" value=\"Pay with LiqPay\" />\n" +
                        "</form>",
                data, signature
        );
    }

    public Map<String, Object> processCallback(String data, String signature) {
        if (!signature.equals(createSignature(data))) {
            throw new SecurityException("Invalid signature");
        }

        try {
            String jsonData = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
            return objectMapper.readValue(jsonData, HashMap.class);
        } catch (Exception e) {
            throw new RuntimeException("Error processing callback", e);
        }
    }
}
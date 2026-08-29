package com.example.mcauthpro.network;

import com.example.mcauthpro.config.PluginConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TurnstileValidator {
    private final PluginConfig config;
    private final HttpClient httpClient;

    public TurnstileValidator(PluginConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(config.getHttpTimeoutSeconds()))
            .build();
    }

    public ValidateResult validate(String token, String remoteIp) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("secret", config.getSecretKey());
            requestBody.put("response", token);
            if (remoteIp != null && !remoteIp.isEmpty()) {
                requestBody.put("remoteip", remoteIp);
            }
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getSiteverifyUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                .timeout(Duration.ofSeconds(config.getHttpTimeoutSeconds()))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response.body());
        } catch (Exception e) {
            return new ValidateResult(false, "network-error", "验证请求异常: " + e.getMessage());
        }
    }

    private ValidateResult parseResponse(String responseBody) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
            boolean success = (boolean) jsonResponse.get("success");
            String hostname = (String) jsonResponse.get("hostname");
            if (success) {
                String expectedHostname = config.getExpectedHostname();
                if (expectedHostname != null && !expectedHostname.isEmpty() && !expectedHostname.equals(hostname)) {
                    return new ValidateResult(false, "hostname-mismatch", "hostname 不匹配: " + hostname);
                }
                return new ValidateResult(true, null, "验证成功");
            } else {
                Object errorCodes = jsonResponse.get("error-codes");
                String errorCode = errorCodes instanceof java.util.List<?> list
                    ? String.join(",", list.stream().map(String::valueOf).toList())
                    : "unknown";
                return new ValidateResult(false, errorCode, "Cloudflare 验证失败");
            }
        } catch (Exception e) {
            return new ValidateResult(false, "parse-error", "响应解析失败: " + e.getMessage());
        }
    }

    public static class ValidateResult {
        private final boolean success;
        private final String errorCode;
        private final String message;

        public ValidateResult(boolean success, String errorCode, String message) {
            this.success = success;
            this.errorCode = errorCode;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
    }
}
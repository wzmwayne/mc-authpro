package com.example.mcauthpro.network;

import com.example.mcauthpro.config.PluginConfig;

public class VerificationSite {
    private final PluginConfig config;

    public VerificationSite(PluginConfig config) {
        this.config = config;
    }

    public String getVerificationUrl(String sessionId) {
        String url = config.getStaticSiteBaseUrl() + "?session=" + sessionId;
        String token = config.getFrontendToken();
        if (token != null && !token.isEmpty()) {
            url += "&token=" + token;
        }
        return url;
    }
}
package com.example.mcauthpro.network;

import com.example.mcauthpro.config.PluginConfig;

import java.util.UUID;

public class VerificationSite {
    private final PluginConfig config;

    public VerificationSite(PluginConfig config) {
        this.config = config;
    }

    public String getVerificationUrl(UUID sessionId) {
        return config.getStaticSiteBaseUrl() + "?session=" + sessionId.toString();
    }

    public String getVerificationUrl(String sessionId) {
        return config.getStaticSiteBaseUrl() + "?session=" + sessionId;
    }
}
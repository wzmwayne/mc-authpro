package com.example.mcauthpro.network;

import com.example.mcauthpro.config.PluginConfig;

public class VerificationSite {
    private final PluginConfig config;

    public VerificationSite(PluginConfig config) {
        this.config = config;
    }

    public String getVerificationUrl(String key, String sitekey) {
        return config.getStaticSiteBaseUrl() + "?session=" + key + "&sitekey=" + sitekey;
    }
}
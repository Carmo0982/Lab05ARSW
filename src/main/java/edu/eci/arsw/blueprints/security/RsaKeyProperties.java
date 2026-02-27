package edu.eci.arsw.blueprints.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blueprints.security")
public record RsaKeyProperties(String issuer, Integer tokenTtlSeconds) {}

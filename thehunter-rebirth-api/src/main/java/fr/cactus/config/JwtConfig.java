package fr.cactus.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jwt")
public interface JwtConfig {

    String issuer();
}
package fr.cactus.config;

import io.smallrye.config.ConfigMapping;

import java.time.Duration;

@ConfigMapping(prefix = "auth.launcher")
public interface LauncherAuthConfig {
    Duration accessTokenDuration();
    Duration refreshTokenDuration();
}
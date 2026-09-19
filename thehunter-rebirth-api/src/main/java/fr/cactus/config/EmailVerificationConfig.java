package fr.cactus.config;

import io.smallrye.config.ConfigMapping;

import java.time.Duration;

@ConfigMapping(prefix = "account.email-verification")
public interface EmailVerificationConfig {

    Duration codeDuration();
}
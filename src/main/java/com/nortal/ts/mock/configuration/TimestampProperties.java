package com.nortal.ts.mock.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotNull;

@ConstructorBinding
@ConfigurationProperties(prefix = "ts")
public record TimestampProperties(
        @NotNull
        String keyStorePath,
        @NotNull
        String keyStoreType,
        @NotNull
        String keyStorePassword,
        @NotNull
        String keyAlias,
        @NotNull
        String keyPassword,
        @NotNull
        String signingAlgorithm,
        @NotNull
        String digestAlgorithm) {
}

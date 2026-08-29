package com.project.BookCarOnline.shared.config;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;

public final class AllowedOrigins {

    private AllowedOrigins() {
    }

    public static String[] parse(String configuredOrigins) {
        if (configuredOrigins == null || configuredOrigins.isBlank()) {
            throw new IllegalArgumentException("Configure at least one allowed origin");
        }

        LinkedHashSet<String> origins = new LinkedHashSet<>();
        Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .forEach(origin -> origins.add(validate(origin)));

        if (origins.isEmpty()) {
            throw new IllegalArgumentException("Configure at least one allowed origin");
        }
        return origins.toArray(String[]::new);
    }

    private static String validate(String origin) {
        if (origin.contains("*")) {
            throw new IllegalArgumentException("Allowed origins must not contain a wildcard");
        }

        URI uri;
        try {
            uri = URI.create(origin);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Allowed origins must be valid HTTP or HTTPS origins", exception);
        }

        boolean http = "http".equalsIgnoreCase(uri.getScheme());
        boolean https = "https".equalsIgnoreCase(uri.getScheme());
        if ((!http && !https) || uri.getHost() == null) {
            throw new IllegalArgumentException("Allowed origins must be HTTP or HTTPS origins");
        }
        if (uri.getRawUserInfo() != null
                || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null
                || uri.getPort() > 65535) {
            throw new IllegalArgumentException("Allowed origins must contain an HTTP or HTTPS origin only");
        }
        return origin;
    }
}

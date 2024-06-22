package com.bogdan_mierloiu.permissions_system.config;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PublicEndpoint {

    SWAGGER_UI("/swagger-ui"),
    API_DOCS("/v3/api-docs"),
    ACTUATOR("/actuator");

    private final String url;

    PublicEndpoint(final String url) {
        this.url = url;
    }

    public static boolean isUriPublic(final String requestUri) {
        return Arrays.stream(PublicEndpoint.values())
                .map(PublicEndpoint::getUrl)
                .anyMatch(requestUri::startsWith);
    }

}

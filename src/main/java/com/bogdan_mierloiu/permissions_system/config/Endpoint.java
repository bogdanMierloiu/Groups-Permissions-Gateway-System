package com.bogdan_mierloiu.permissions_system.config;

import lombok.Getter;

@Getter
public enum Endpoint {

    SWAGGER_UI("/swagger-ui/**"),
    API_DOCS("/v3/api-docs/**"),
    ACTUATOR("/actuator/**");

    private final String url;

    Endpoint(final String url) {
        this.url = url;
    }
}

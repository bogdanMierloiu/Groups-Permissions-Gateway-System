package com.bogdan_mierloiu.permissions_system.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedIssuer {

    private String uri;

    private String introspectionEndpoint;

    private String clientId;

    private String clientSecret;
}

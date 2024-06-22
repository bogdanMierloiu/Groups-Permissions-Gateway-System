package com.bogdan_mierloiu.permissions_system.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bogdan_mierloiu.permissions_system.entity.Role;
import com.bogdan_mierloiu.permissions_system.entity.User;
import com.bogdan_mierloiu.permissions_system.service.SignUpUser;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

@Slf4j
public class TokenIntrospector implements OpaqueTokenIntrospector {

    private final JwtDecoder jwtDecoder;
    private final SignUpUser signUpUser;
    private final RestTemplate restTemplate;
    private final Map<String, LinkedList<TrustedIssuer>> trustedIssuers;

    @Value("${local.issuer.uri}")
    private String issuerUriLocal;

    @Value("${spring.jwk.file.path}")
    private String springJwkPath;

    @Value("${springAuthServerKeysUri}")
    private String springAuthServerKeysUri;

    public TokenIntrospector(SignUpUser signUpUser) {
        this.signUpUser = signUpUser;
        this.restTemplate = new RestTemplateBuilder().build();
        this.trustedIssuers = new LinkedHashMap<>();
        this.jwtDecoder = new NimbusJwtDecoder(new ParseJwtProcessor());
    }

    @PostConstruct
    public void updateJwkOnStartup() {
        springAuthServerUpdateJwk();
    }

    public OAuth2AuthenticatedPrincipal introspect(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        User appUser = signUpUser.verifyExistOrSave(jwt);

        String issuer = jwt.getClaimAsString("iss");

        if (issuer.equals(issuerUriLocal)) {
            return springAuthServerIssuer(jwt, appUser.getRole());
        } else throw new OAuth2IntrospectionException("Issuer not trusted");
    }

    private OAuth2AuthenticatedPrincipal springAuthServerIssuer(Jwt jwtToken, Role role) {
        try {
            verifyTokenSignature(jwtToken, springJwkPath);
            return new DefaultOAuth2AuthenticatedPrincipal(jwtToken.getClaims(), roleConverter(role));
        } catch (SignatureVerificationException e) {
            springAuthServerUpdateJwk();
            verifyTokenSignature(jwtToken, springJwkPath);
            return new DefaultOAuth2AuthenticatedPrincipal(jwtToken.getClaims(), roleConverter(role));
        }
    }

    private void verifyTokenSignature(Jwt jwtToken, String jwkPath) {
        DecodedJWT jwt = JWT.decode(jwtToken.getTokenValue());
        try {
            JWKSet provider = JWKSet.load(new File(jwkPath));
            JWK jwk = provider.getKeyByKeyId(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256(jwk.toRSAKey().toRSAPublicKey(), null);
            algorithm.verify(jwt);
        } catch (Exception e) {
            throw new OAuth2IntrospectionException("Failed to verify token signature");
        }
    }

    private void springAuthServerUpdateJwk() {
        String response = restTemplate.getForObject(springAuthServerKeysUri, String.class);
        try {
            PrintWriter printWriter = new PrintWriter(springJwkPath);
            log.info("------ Spring Auth Server - JWK updated ------");
            printWriter.println(response);
            printWriter.close();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    public void addIssuers(TrustedIssuer... trustedIssuers) {
        for (TrustedIssuer trustedIssuer : trustedIssuers) {
            LinkedList<TrustedIssuer> clientList = this.trustedIssuers.get(trustedIssuer.getUri());
            if (clientList == null) {
                LinkedList<TrustedIssuer> newIssuer = new LinkedList<>();
                newIssuer.add(trustedIssuer);
                this.trustedIssuers.put(trustedIssuer.getUri(), newIssuer);
            } else {
                clientList.add(trustedIssuer);
            }
        }
    }

    private Collection<GrantedAuthority> roleConverter(Role role) {
        return Set.of(new SimpleGrantedAuthority(role.getName()));
    }

}

package com.bogdan_mierloiu.permissions_system.config;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class ParseJwtProcessor extends DefaultJWTProcessor<SecurityContext> {

    @Override
    public JWTClaimsSet process(SignedJWT jwt, SecurityContext context) {
        try {
            return jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new SecurityException("There was an error parsing the JWT");
        }
    }
}

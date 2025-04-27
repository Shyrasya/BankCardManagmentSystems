package com.bank.cardmanagement.security;

import com.bank.cardmanagement.security.model.JwtAuthentication;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtUtil {
    public JwtAuthentication createAuthentication(Claims claims) {
        Long id = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        return new JwtAuthentication(id, authority);
    }
}

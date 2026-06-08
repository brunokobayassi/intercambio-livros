package br.com.livros.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // Chave secreta para assinar o token — nunca expor isso em produção
    private static final Key CHAVE_SECRETA = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token válido por 8 horas
    private static final long EXPIRACAO_MS = 8 * 60 * 60 * 1000;

    /**
     * Gera um token JWT com o email do usuário como subject
     */
    public static String gerarToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRACAO_MS))
                .signWith(CHAVE_SECRETA)
                .compact();
    }

    /**
     * Valida o token — retorna true se for válido, false se inválido ou expirado
     */
    public static boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(CHAVE_SECRETA)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("Token inválido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o email (subject) do token
     */
    public static String extrairEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(CHAVE_SECRETA)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            System.out.println("Erro ao extrair email do token: " + e.getMessage());
            return null;
        }
    }
}
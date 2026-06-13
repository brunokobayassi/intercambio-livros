package br.com.livros.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "intercambio-livros-secret";
    private static final String ISSUER = "intercambio-livros";
    private static final long EXPIRACAO_MS = 8 * 60 * 60 * 1000;
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String gerarToken(long userId) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRACAO_MS))
                .sign(ALGORITHM);
    }

    public static boolean validarToken(String token) {
        try {
            buildVerifier().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public static Long extrairUsuarioId(String token) {
        try {
            DecodedJWT decoded = buildVerifier().verify(token);
            return decoded.getClaim("userId").asLong();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private static JWTVerifier buildVerifier() {
        return JWT.require(ALGORITHM)
                .withIssuer(ISSUER)
                .build();
    }
}

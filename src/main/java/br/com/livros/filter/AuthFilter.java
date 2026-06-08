package br.com.livros.filter;

import br.com.livros.util.JwtUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Libera requisições de login sem token
        String uri = httpRequest.getRequestURI();
        if (uri.endsWith("/api/login")) {
            chain.doFilter(request, response);
            return;
        }

        // Lê o header Authorization
        String authHeader = httpRequest.getHeader("Authorization");

        // Verifica se o header existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"erro\": \"Acesso não autorizado\"}");
            return;
        }

        // Extrai o token removendo o prefixo "Bearer "
        String token = authHeader.substring(7);

        // Valida o token
        if (!JwtUtil.validarToken(token)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"erro\": \"Acesso não autorizado\"}");
            return;
        }

        // Token válido — extrai o email e passa para o Controller via atributo
        String email = JwtUtil.extrairEmail(token);
        httpRequest.setAttribute("emailUsuario", email);

        // Continua para o Controller
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
package br.com.livros.controller;

import br.com.livros.dao.UsuarioDAO;
import br.com.livros.model.Usuario;
import br.com.livros.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/login")
public class LoginApiController extends HttpServlet {

    private final Gson gson = new Gson();

    /**
     * POST /api/login
     * Body: { "email": "...", "senha": "..." }
     * Retorna: { "token": "..." } ou { "erro": "..." }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Lê o body da requisição
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                body.append(linha);
            }
        }

        // Deserializa o JSON do body para um objeto
        JsonObject credenciais = gson.fromJson(body.toString(), JsonObject.class);

        // Valida se os campos foram enviados
        if (credenciais == null
                || !credenciais.has("email")
                || !credenciais.has("senha")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"Email e senha são obrigatórios\"}");
            return;
        }

        String email = credenciais.get("email").getAsString();
        String senha = credenciais.get("senha").getAsString();

        // Autentica no banco via DAO
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.autenticar(email, senha);

        // Credenciais inválidas
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"erro\": \"Email ou senha incorretos\"}");
            return;
        }

        // Gera o token JWT com o email do usuário
        String token = JwtUtil.gerarToken(usuario.getEmail());

        // Monta a resposta com o token e dados básicos do usuário
        JsonObject resposta = new JsonObject();
        resposta.addProperty("token", token);
        resposta.addProperty("id", usuario.getId());
        resposta.addProperty("nome", usuario.getNome());
        resposta.addProperty("email", usuario.getEmail());

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(resposta));
    }
}
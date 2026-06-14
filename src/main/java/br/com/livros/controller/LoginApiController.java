package br.com.livros.controller;

import br.com.livros.model.Usuario;
import br.com.livros.service.UsuarioService;
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
    private final UsuarioService usuarioService = new UsuarioService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject credenciais = lerBody(request);
        String email = credenciais != null && credenciais.has("email")
                ? credenciais.get("email").getAsString() : null;
        String senha = credenciais != null && credenciais.has("senha")
                ? credenciais.get("senha").getAsString() : null;

        try {
            Usuario usuario = usuarioService.autenticar(email, senha);
            String token = JwtUtil.gerarToken(usuario.getId());

            JsonObject resposta = new JsonObject();
            resposta.addProperty("token", token);
            resposta.addProperty("id", usuario.getId());
            resposta.addProperty("nome", usuario.getNome());
            resposta.addProperty("email", usuario.getEmail());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(resposta));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(erro(e.getMessage()));
        } catch (SecurityException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(erro(e.getMessage()));
        }
    }

    private JsonObject lerBody(HttpServletRequest req) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                body.append(linha);
            }
        }
        return gson.fromJson(body.toString(), JsonObject.class);
    }

    private String erro(String mensagem) {
        JsonObject obj = new JsonObject();
        obj.addProperty("erro", mensagem);
        return gson.toJson(obj);
    }
}

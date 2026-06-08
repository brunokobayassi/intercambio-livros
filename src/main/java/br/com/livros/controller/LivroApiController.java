package br.com.livros.controller;

import br.com.livros.dao.LivroDAO;
import br.com.livros.dao.UsuarioDAO;
import br.com.livros.model.Livro;
import br.com.livros.model.Usuario;
import br.com.livros.util.JwtUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/livros/*")
public class LivroApiController extends HttpServlet {

    private final Gson gson = new Gson();
    private final LivroDAO livroDAO = new LivroDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ================================================================
    // GET /api/livros — lista livros disponíveis de outros usuários
    // GET /api/livros/{id} — busca um livro pelo ID
    // ================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        // GET /api/livros/{id}
        if (pathInfo != null && !pathInfo.equals("/")) {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Livro livro = livroDAO.buscarPorId(id);

                if (livro == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"erro\": \"Livro não encontrado\"}");
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(livro));

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"ID inválido\"}");
            }
            return;
        }

        // GET /api/livros — lista livros de outros usuários
        String email = (String) request.getAttribute("emailUsuario");
        Usuario usuarioLogado = usuarioDAO.buscarPorEmail(email);

        List<Livro> livros = livroDAO.listarDeOutrosUsuarios(usuarioLogado.getId());

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(livros));
    }

    // ================================================================
    // POST /api/livros — cadastra um novo livro
    // ================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Lê o body
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                body.append(linha);
            }
        }

        // Deserializa o JSON para um objeto Livro
        Livro livro = gson.fromJson(body.toString(), Livro.class);

        // Valida campos obrigatórios
        if (livro == null
                || livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()
                || livro.getAutor() == null || livro.getAutor().trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"Título e autor são obrigatórios\"}");
            return;
        }

        // Pega o usuário logado pelo email do token
        String email = (String) request.getAttribute("emailUsuario");
        Usuario usuarioLogado = usuarioDAO.buscarPorEmail(email);

        livro.setUsuarioId(usuarioLogado.getId());

        boolean ok = livroDAO.cadastrarLivro(livro);

        if (ok) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"mensagem\": \"Livro cadastrado com sucesso\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\": \"Erro ao cadastrar livro\"}");
        }
    }

    // ================================================================
    // PUT /api/livros/{id} — atualiza título e autor de um livro
    // ================================================================
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"ID do livro é obrigatório\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            // Verifica se o livro existe
            Livro livroExistente = livroDAO.buscarPorId(id);
            if (livroExistente == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\": \"Livro não encontrado\"}");
                return;
            }

            // Verifica se o livro pertence ao usuário logado
            String email = (String) request.getAttribute("emailUsuario");
            Usuario usuarioLogado = usuarioDAO.buscarPorEmail(email);

            if (livroExistente.getUsuarioId() != usuarioLogado.getId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"erro\": \"Você não tem permissão para editar este livro\"}");
                return;
            }

            // Lê o body
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    body.append(linha);
                }
            }

            Livro livroAtualizado = gson.fromJson(body.toString(), Livro.class);

            // Valida campos
            if (livroAtualizado == null
                    || livroAtualizado.getTitulo() == null || livroAtualizado.getTitulo().trim().isEmpty()
                    || livroAtualizado.getAutor() == null || livroAtualizado.getAutor().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"Título e autor são obrigatórios\"}");
                return;
            }

            livroAtualizado.setId(id);
            livroAtualizado.setUsuarioId(usuarioLogado.getId());

            boolean ok = livroDAO.atualizar(livroAtualizado);

            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Livro atualizado com sucesso\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"erro\": \"Erro ao atualizar livro\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"ID inválido\"}");
        }
    }

    // ================================================================
    // DELETE /api/livros/{id} — deleta um livro
    // ================================================================
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"ID do livro é obrigatório\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            // Verifica se o livro existe
            Livro livro = livroDAO.buscarPorId(id);
            if (livro == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\": \"Livro não encontrado\"}");
                return;
            }

            // Verifica se o livro pertence ao usuário logado
            String email = (String) request.getAttribute("emailUsuario");
            Usuario usuarioLogado = usuarioDAO.buscarPorEmail(email);

            if (livro.getUsuarioId() != usuarioLogado.getId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"erro\": \"Você não tem permissão para deletar este livro\"}");
                return;
            }

            boolean ok = livroDAO.deletar(id);

            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Livro deletado com sucesso\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"erro\": \"Erro ao deletar livro\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"ID inválido\"}");
        }
    }
}
package br.com.livros.controller;

import br.com.livros.dao.LivroDAO;
import br.com.livros.model.Livro;
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

    // GET /api/livros       — lista livros disponíveis de outros usuários
    // GET /api/livros/{id}  — busca um livro pelo ID
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path != null && !path.equals("/")) {
            try {
                int id = Integer.parseInt(path.substring(1));
                Livro livro = livroDAO.buscarPorId(id);

                if (livro == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"erro\": \"Livro não encontrado\"}");
                    return;
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(livro));

            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"erro\": \"ID inválido\"}");
            }
            return;
        }

        long usuarioId = (Long) req.getAttribute("usuarioId");
        List<Livro> livros = livroDAO.listarDeOutrosUsuarios((int) usuarioId);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(livros));
    }

    // POST /api/livros — cadastra um novo livro
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Livro livro = lerBody(req, Livro.class);

        if (livro == null
                || livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()
                || livro.getAutor() == null || livro.getAutor().trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"Título e autor são obrigatórios\"}");
            return;
        }

        long usuarioId = (Long) req.getAttribute("usuarioId");
        livro.setUsuarioId((int) usuarioId);

        boolean ok = livroDAO.cadastrarLivro(livro);

        if (ok) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"mensagem\": \"Livro cadastrado com sucesso\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"erro\": \"Erro ao cadastrar livro\"}");
        }
    }

    // PUT /api/livros/{id} — substitui título e autor do livro (idempotente)
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"ID do livro é obrigatório\"}");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            long usuarioId = (Long) req.getAttribute("usuarioId");

            Livro livroExistente = livroDAO.buscarPorId(id);
            if (livroExistente == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"erro\": \"Livro não encontrado\"}");
                return;
            }

            if (livroExistente.getUsuarioId() != (int) usuarioId) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"erro\": \"Você não tem permissão para editar este livro\"}");
                return;
            }

            Livro livroAtualizado = lerBody(req, Livro.class);

            if (livroAtualizado == null
                    || livroAtualizado.getTitulo() == null || livroAtualizado.getTitulo().trim().isEmpty()
                    || livroAtualizado.getAutor() == null || livroAtualizado.getAutor().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"erro\": \"Título e autor são obrigatórios\"}");
                return;
            }

            livroAtualizado.setId(id);
            livroAtualizado.setUsuarioId((int) usuarioId);

            boolean ok = livroDAO.atualizar(livroAtualizado);

            if (ok) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"mensagem\": \"Livro atualizado com sucesso\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"erro\": \"Erro ao atualizar livro\"}");
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"ID inválido\"}");
        }
    }

    // DELETE /api/livros/{id} — remove um livro; retorna 204 sem corpo
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"ID do livro é obrigatório\"}");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            long usuarioId = (Long) req.getAttribute("usuarioId");

            Livro livro = livroDAO.buscarPorId(id);
            if (livro == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"erro\": \"Livro não encontrado\"}");
                return;
            }

            if (livro.getUsuarioId() != (int) usuarioId) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"erro\": \"Você não tem permissão para deletar este livro\"}");
                return;
            }

            boolean ok = livroDAO.deletar(id);

            if (ok) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"erro\": \"Erro ao deletar livro\"}");
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"ID inválido\"}");
        }
    }

    private <T> T lerBody(HttpServletRequest req, Class<T> tipo) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                body.append(linha);
            }
        }
        return gson.fromJson(body.toString(), tipo);
    }
}

package br.com.livros.controller;

import br.com.livros.model.Livro;
import br.com.livros.service.LivroService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet("/api/livros/*")
public class LivroApiController extends HttpServlet {

    private final Gson gson = new Gson();
    private final LivroService livroService = new LivroService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();

        try {
            if (path != null && !path.equals("/")) {
                int id = Integer.parseInt(path.substring(1));
                Livro livro = livroService.buscarPorId(id);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(livro));
            } else {
                List<Livro> livros = livroService.listarDeOutrosUsuarios(usuarioId);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(livros));
            }
        } catch (NoSuchElementException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro("ID inválido"));
        }
    }

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
            resp.getWriter().write(erro("Título e autor são obrigatórios"));
            return;
        }

        int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();

        try {
            livroService.cadastrar(livro, usuarioId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"mensagem\": \"Livro cadastrado com sucesso\"}");
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(erro("Erro ao cadastrar livro"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro("ID do livro é obrigatório"));
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();
            Livro dados = lerBody(req, Livro.class);

            if (dados == null
                    || dados.getTitulo() == null || dados.getTitulo().trim().isEmpty()
                    || dados.getAutor() == null || dados.getAutor().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(erro("Título e autor são obrigatórios"));
                return;
            }

            livroService.atualizar(id, dados, usuarioId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"mensagem\": \"Livro atualizado com sucesso\"}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro("ID inválido"));
        } catch (NoSuchElementException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(erro("Erro ao atualizar livro"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro("ID do livro é obrigatório"));
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();

            livroService.deletar(id, usuarioId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro("ID inválido"));
        } catch (NoSuchElementException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(erro("Erro ao deletar livro"));
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

    private String erro(String mensagem) {
        JsonObject obj = new JsonObject();
        obj.addProperty("erro", mensagem);
        return gson.toJson(obj);
    }
}

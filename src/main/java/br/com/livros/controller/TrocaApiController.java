package br.com.livros.controller;

import br.com.livros.model.TrocaDetalhada;
import br.com.livros.service.TrocaService;
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

@WebServlet("/api/trocas/*")
public class TrocaApiController extends HttpServlet {

    private final Gson gson = new Gson();
    private final TrocaService trocaService = new TrocaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();
        String path = req.getPathInfo();

        if ("/pendentes".equals(path)) {
            List<TrocaDetalhada> pendentes = trocaService.listarPendentes(usuarioId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(pendentes));
            return;
        }

        if ("/propostas".equals(path)) {
            List<TrocaDetalhada> propostas = trocaService.listarPropostas(usuarioId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(propostas));
            return;
        }

        List<TrocaDetalhada> pendentes = trocaService.listarPendentes(usuarioId);
        List<TrocaDetalhada> propostas = trocaService.listarPropostas(usuarioId);

        JsonObject resposta = new JsonObject();
        resposta.add("pendentes", gson.toJsonTree(pendentes));
        resposta.add("propostas", gson.toJsonTree(propostas));

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(resposta));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject dados = lerBody(req);

        if (dados == null || !dados.has("livroOferecidoId") || !dados.has("livroRecebidoId")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro("livroOferecidoId e livroRecebidoId são obrigatórios"));
            return;
        }

        int livroOferecidoId = dados.get("livroOferecidoId").getAsInt();
        int livroRecebidoId = dados.get("livroRecebidoId").getAsInt();
        int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();

        try {
            trocaService.propor(livroOferecidoId, livroRecebidoId, usuarioId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"mensagem\": \"Proposta de troca enviada com sucesso\"}");
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (NoSuchElementException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(erro("Erro ao enviar proposta de troca"));
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
            resp.getWriter().write(erro("ID da troca é obrigatório"));
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            JsonObject dados = lerBody(req);

            if (dados == null || !dados.has("acao")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(erro("Campo acao é obrigatório"));
                return;
            }

            String acao = dados.get("acao").getAsString().toLowerCase();
            String mensagem = trocaService.responder(id, acao);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"mensagem\": \"" + mensagem + "\"}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro("ID inválido"));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(erro(e.getMessage()));
        } catch (NoSuchElementException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(erro(e.getMessage()));
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

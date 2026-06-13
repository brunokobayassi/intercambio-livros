package br.com.livros.controller;

import br.com.livros.dao.LivroDAO;
import br.com.livros.dao.TrocaDAO;
import br.com.livros.model.TrocaDetalhada;
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

@WebServlet("/api/trocas/*")
public class TrocaApiController extends HttpServlet {

    private final Gson gson = new Gson();
    private final TrocaDAO trocaDAO = new TrocaDAO();
    private final LivroDAO livroDAO = new LivroDAO();

    // GET /api/trocas           — pendentes + propostas do usuário logado
    // GET /api/trocas/pendentes — só as pendentes recebidas
    // GET /api/trocas/propostas — só as propostas enviadas
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();
        String path = req.getPathInfo();

        if ("/pendentes".equals(path)) {
            List<TrocaDetalhada> pendentes = trocaDAO.listarPendentesDetalhadas(usuarioId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(pendentes));
            return;
        }

        if ("/propostas".equals(path)) {
            List<TrocaDetalhada> propostas = trocaDAO.listarMinhasPropostas(usuarioId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(propostas));
            return;
        }

        List<TrocaDetalhada> pendentes = trocaDAO.listarPendentesDetalhadas(usuarioId);
        List<TrocaDetalhada> propostas = trocaDAO.listarMinhasPropostas(usuarioId);

        JsonObject resposta = new JsonObject();
        resposta.add("pendentes", gson.toJsonTree(pendentes));
        resposta.add("propostas", gson.toJsonTree(propostas));

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(resposta));
    }

    // POST /api/trocas — propõe uma nova troca
    // Body: { "livroOferecidoId": 1, "livroRecebidoId": 2 }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject dados = lerBody(req);

        if (dados == null
                || !dados.has("livroOferecidoId")
                || !dados.has("livroRecebidoId")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"livroOferecidoId e livroRecebidoId são obrigatórios\"}");
            return;
        }

        int livroOferecidoId = dados.get("livroOferecidoId").getAsInt();
        int livroRecebidoId = dados.get("livroRecebidoId").getAsInt();
        int usuarioId = ((Long) req.getAttribute("usuarioId")).intValue();

        int donoOferecido = livroDAO.buscarDonoPorLivro(livroOferecidoId);
        if (donoOferecido != usuarioId) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"erro\": \"O livro oferecido não pertence a você\"}");
            return;
        }

        int usuarioSolicitadoId = livroDAO.buscarDonoPorLivro(livroRecebidoId);
        if (usuarioSolicitadoId == -1) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"erro\": \"Livro desejado não encontrado\"}");
            return;
        }

        if (usuarioSolicitadoId == usuarioId) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"Você não pode trocar livros com você mesmo\"}");
            return;
        }

        boolean ok = trocaDAO.inserirTroca(livroOferecidoId, livroRecebidoId, usuarioId, usuarioSolicitadoId);

        if (ok) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"mensagem\": \"Proposta de troca enviada com sucesso\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"erro\": \"Erro ao enviar proposta de troca\"}");
        }
    }

    // PUT /api/trocas/{id} — aceita ou recusa uma troca (idempotente)
    // Body: { "acao": "aceitar" } ou { "acao": "recusar" }
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"ID da troca é obrigatório\"}");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));

            JsonObject dados = lerBody(req);

            if (dados == null || !dados.has("acao")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"erro\": \"Campo acao é obrigatório\"}");
                return;
            }

            String acao = dados.get("acao").getAsString().toLowerCase();

            if (!acao.equals("aceitar") && !acao.equals("recusar")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"erro\": \"Acao deve ser aceitar ou recusar\"}");
                return;
            }

            String novoStatus = acao.equals("aceitar") ? "ACEITA" : "RECUSADA";
            boolean ok = trocaDAO.atualizarStatus(id, novoStatus);

            if (ok) {
                String mensagem = novoStatus.equals("ACEITA") ? "Troca aceita com sucesso" : "Troca recusada com sucesso";
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"mensagem\": \"" + mensagem + "\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"erro\": \"Troca não encontrada\"}");
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"erro\": \"ID inválido\"}");
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
}

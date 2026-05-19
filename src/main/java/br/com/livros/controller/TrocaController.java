package br.com.livros.controller;

import br.com.livros.dao.TrocaDAO;
import br.com.livros.model.TrocaDetalhada;
import br.com.livros.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/troca")
public class TrocaController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession();
        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuario");

        if (usuarioLogado == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        TrocaDAO trocaDAO = new TrocaDAO();
        List<TrocaDetalhada> pendentes = trocaDAO.listarPendentesDetalhadas(usuarioLogado.getId());
        List<TrocaDetalhada> minhasPropostas = trocaDAO.listarMinhasPropostas(usuarioLogado.getId());

        request.setAttribute("listaPendentes", pendentes);
        request.setAttribute("minhasPropostas", minhasPropostas);
        request.getRequestDispatcher("/principal.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession();
        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuario");

        if (usuarioLogado == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String idTrocaStr = request.getParameter("id");
        String acao = request.getParameter("acao");

        if (idTrocaStr != null && acao != null) {
            try {
                int idTroca = Integer.parseInt(idTrocaStr);
                String novoStatus = "";

                if (acao.equals("aceitar"))
                    novoStatus = "ACEITA";
                else if (acao.equals("recusar"))
                    novoStatus = "RECUSADA";

                if (!novoStatus.isEmpty()) {
                    new TrocaDAO().atualizarStatus(idTroca, novoStatus);
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: ID da troca inválido.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/troca");
    }
}
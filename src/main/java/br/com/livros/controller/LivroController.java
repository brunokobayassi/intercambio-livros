package br.com.livros.controller;

import br.com.livros.dao.LivroDAO;
import br.com.livros.dao.TrocaDAO;
import br.com.livros.model.Livro;
import br.com.livros.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/livros")
public class LivroController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession(false);
        if (sessao == null || sessao.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuario");
        LivroDAO livroDAO = new LivroDAO();

        List<Livro> livrosDisponiveis = livroDAO.listarDeOutrosUsuarios(usuarioLogado.getId());
        List<Livro> meusLivros = livroDAO.listarDoUsuario(usuarioLogado.getId());

        request.setAttribute("livrosDisponiveis", livrosDisponiveis);
        request.setAttribute("meusLivros", meusLivros);

        request.getRequestDispatcher("/livros.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession sessao = request.getSession(false);
        if (sessao == null || sessao.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String acao = request.getParameter("acao");
        if ("propor".equals(acao)) {
            proporTroca(request, response, sessao);
            return;
        }

        // Cadastro de livro
        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuario");

        String titulo = request.getParameter("titulo");
        String autor = request.getParameter("autor");

        if (titulo == null || titulo.trim().isEmpty()
                || autor == null || autor.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/troca");
            return;
        }

        Livro novoLivro = new Livro();
        novoLivro.setTitulo(titulo.trim());
        novoLivro.setAutor(autor.trim());
        novoLivro.setUsuarioId(usuarioLogado.getId());

        new LivroDAO().cadastrarLivro(novoLivro);
        response.sendRedirect(request.getContextPath() + "/troca");
    }

    private void proporTroca(HttpServletRequest request, HttpServletResponse response, HttpSession sessao)
            throws IOException {

        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuario");

        try {
            int livroOferecidoId = Integer.parseInt(request.getParameter("livroOferecidoId"));
            int livroRecebidoId = Integer.parseInt(request.getParameter("livroRecebidoId"));

            // Busca o dono do livro desejado para popular usuario_solicitado_id
            int usuarioSolicitadoId = new LivroDAO().buscarDonoPorLivro(livroRecebidoId);

            if (usuarioSolicitadoId == -1) {
                sessao.setAttribute("erro", "Não foi possível identificar o dono do livro.");
                response.sendRedirect(request.getContextPath() + "/livros");
                return;
            }

            boolean ok = new TrocaDAO().inserirTroca(
                    livroOferecidoId, livroRecebidoId,
                    usuarioLogado.getId(), usuarioSolicitadoId);

            if (ok) {
                sessao.setAttribute("mensagem", "Proposta enviada com sucesso!");
            } else {
                sessao.setAttribute("erro", "Não foi possível enviar a proposta.");
            }
        } catch (NumberFormatException e) {
            sessao.setAttribute("erro", "Dados inválidos na proposta.");
        }

        response.sendRedirect(request.getContextPath() + "/livros");
    }
}
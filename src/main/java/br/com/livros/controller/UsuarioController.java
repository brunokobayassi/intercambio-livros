package br.com.livros.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import br.com.livros.dao.UsuarioDAO;
import br.com.livros.model.Usuario;
import br.com.livros.service.UsuarioService;

@WebServlet("/usuarios")
public class UsuarioController extends HttpServlet {

    private UsuarioService usuarioService;

    @Override
    public void init() throws ServletException {
        this.usuarioService = new UsuarioService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Usuario> lista = usuarioService.listarUsuarios();
        request.setAttribute("listaUsuarios", lista);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String emailDigitado = request.getParameter("email");
        String senhaDigitada = request.getParameter("senha");

        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuarioLogado = dao.autenticar(emailDigitado, senhaDigitada);

        if (usuarioLogado != null) {
            HttpSession sessao = request.getSession();
            sessao.setAttribute("usuario", usuarioLogado);

            response.sendRedirect(request.getContextPath() + "/troca"); 
        } else {
            request.setAttribute("erroLogin", "E-mail ou senha incorretos.");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}
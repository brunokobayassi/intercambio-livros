<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — Sistema de Intercâmbio de Livros</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <div class="login-container">
        <div class="login-card">

            <div class="logo">
                <span class="icone">📚</span>
                <h1>Intercâmbio de Livros</h1>
                <p>Faça login para acessar o sistema</p>
            </div>

            <%-- Exibe mensagem de erro vinda do UsuarioController --%>
            <% if (request.getAttribute("erroLogin") != null) { %>
                <div class="alerta alerta-erro">
                    ⚠️ ${erroLogin}
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/usuarios" method="POST">

                <div class="form-grupo">
                    <label for="email">E-mail</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="seu@email.com"
                        required
                        autocomplete="email"
                    >
                </div>

                <div class="form-grupo">
                    <label for="senha">Senha</label>
                    <input
                        type="password"
                        id="senha"
                        name="senha"
                        placeholder="••••••••"
                        required
                        autocomplete="current-password"
                    >
                </div>

                <button type="submit" class="btn btn-primario">
                    Entrar
                </button>

            </form>

        </div>
    </div>

</body>
</html>
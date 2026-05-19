<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>

        <% if (session.getAttribute("usuario")==null) { response.sendRedirect(request.getContextPath() + "/index.jsp" );
            return; } %>

            <!DOCTYPE html>
            <html lang="pt-BR">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Livros Disponíveis — Intercâmbio de Livros</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
                <style>
                    .modal-overlay {
                        display: none;
                        position: fixed;
                        inset: 0;
                        background: rgba(0, 0, 0, .45);
                        z-index: 100;
                        align-items: center;
                        justify-content: center;
                    }

                    .modal-overlay.ativo {
                        display: flex;
                    }

                    .modal {
                        background: #fff;
                        border-radius: 10px;
                        padding: 2rem;
                        width: 100%;
                        max-width: 420px;
                        box-shadow: 0 8px 32px rgba(0, 0, 0, .18);
                    }

                    .modal h3 {
                        margin-bottom: 1rem;
                        color: #2c3e50;
                    }

                    .modal select {
                        width: 100%;
                        padding: .6rem .8rem;
                        border: 1px solid #ccc;
                        border-radius: 6px;
                        font-size: .95rem;
                        margin-bottom: 1.2rem;
                    }

                    .modal-acoes {
                        display: flex;
                        gap: .75rem;
                        justify-content: flex-end;
                    }

                    .btn-cancelar {
                        background: #ecf0f1;
                        color: #555;
                        border: none;
                        padding: .5rem 1.1rem;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: .9rem;
                    }

                    .btn-cancelar:hover {
                        background: #dce1e7;
                    }
                </style>
            </head>

            <body>

                <nav class="navbar">
                    <a href="${pageContext.request.contextPath}/troca" class="navbar-brand">
                        📚 Intercâmbio de Livros
                    </a>
                    <div class="navbar-usuario">
                        <span class="nome-usuario">Olá, ${sessionScope.usuario.nome}!</span>
                        <a href="${pageContext.request.contextPath}/troca">← Dashboard</a>
                        &nbsp;|&nbsp;
                        <a href="${pageContext.request.contextPath}/index.jsp">Sair</a>
                    </div>
                </nav>

                <div class="dashboard-wrapper">
                    <div class="card-secao" style="grid-column: 1 / -1;">

                        <h2>🔄 Livros disponíveis para troca</h2>

                        <c:if test="${not empty sessionScope.mensagem}">
                            <div class="alert alert-success">${sessionScope.mensagem}</div>
                            <c:remove var="mensagem" scope="session" />
                        </c:if>
                        <c:if test="${not empty sessionScope.erro}">
                            <div class="alert alert-error">${sessionScope.erro}</div>
                            <c:remove var="erro" scope="session" />
                        </c:if>

                        <c:choose>
                            <c:when test="${empty livrosDisponiveis}">
                                <table class="tabela-trocas">
                                    <tbody>
                                        <tr>
                                            <td class="tabela-vazia">
                                                📭 Nenhum livro disponível para troca no momento.
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </c:when>
                            <c:otherwise>
                                <table class="tabela-trocas">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Título</th>
                                            <th>Autor</th>
                                            <th>Dono</th>
                                            <th>Ação</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="livro" items="${livrosDisponiveis}">
                                            <tr>
                                                <td>${livro.id}</td>
                                                <td>${livro.titulo}</td>
                                                <td>${livro.autor}</td>
                                                <td>${livro.nomeDono}</td>
                                                <td>
                                                    <button class="btn btn-aceitar" data-id="${livro.id}"
                                                        data-titulo="${livro.titulo}"
                                                        onclick="abrirModal(this.dataset.id, this.dataset.titulo)">
                                                        🤝 Propor Troca
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:otherwise>
                        </c:choose>

                    </div>
                </div>

                <%-- Modal: escolher qual livro oferecer --%>
                    <div class="modal-overlay" id="modalOverlay">
                        <div class="modal">
                            <h3>🤝 Propor Troca</h3>
                            <p style="margin-bottom:1rem; color:#555;">
                                Você quer trocar por: <strong id="tituloDesejado"></strong>
                            </p>

                            <form action="${pageContext.request.contextPath}/livros" method="POST">
                                <input type="hidden" name="acao" value="propor" />
                                <input type="hidden" name="livroRecebidoId" id="livroRecebidoId" />

                                <label for="livroOferecidoId" style="font-size:.9rem; color:#555;">
                                    Qual livro você oferece em troca?
                                </label>
                                <select name="livroOferecidoId" id="livroOferecidoId" required>
                                    <c:choose>
                                        <c:when test="${empty meusLivros}">
                                            <option value="" disabled selected>
                                                Você não tem livros cadastrados
                                            </option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="" disabled selected>Selecione um livro...</option>
                                            <c:forEach var="meu" items="${meusLivros}">
                                                <option value="${meu.id}">${meu.titulo}</option>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </select>

                                <div class="modal-acoes">
                                    <button type="button" class="btn-cancelar" onclick="fecharModal()">
                                        Cancelar
                                    </button>
                                    <c:if test="${not empty meusLivros}">
                                        <button type="submit" class="btn btn-aceitar">
                                            Enviar Proposta
                                        </button>
                                    </c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <script>
                        function abrirModal(livroId, titulo) {
                            document.getElementById('livroRecebidoId').value = livroId;
                            document.getElementById('tituloDesejado').textContent = titulo;
                            document.getElementById('modalOverlay').classList.add('ativo');
                        }

                        function fecharModal() {
                            document.getElementById('modalOverlay').classList.remove('ativo');
                        }

                        document.getElementById('modalOverlay').addEventListener('click', function (e) {
                            if (e.target === this) fecharModal();
                        });
                    </script>

            </body>

            </html>
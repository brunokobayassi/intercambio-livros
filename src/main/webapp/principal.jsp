<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

            <% if (session.getAttribute("usuario")==null) { response.sendRedirect(request.getContextPath()
                + "/index.jsp" ); return; } %>

                <!DOCTYPE html>
                <html lang="pt-BR">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Dashboard — Intercâmbio de Livros</title>
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
                    <style>
                        .dashboard-wrapper {
                            max-width: 1400px;
                            margin: 32px auto;
                            padding: 0 32px;
                            display: flex;
                            flex-direction: column;
                            gap: 24px;
                        }

                        .dashboard-btn-topo {
                            width: 100%;
                        }

                        .dashboard-tabelas {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 24px;
                            align-items: start;
                        }

                        .dashboard-bottom {
                            max-width: 500px;
                        }

                        .card-secao {
                            overflow-x: auto;
                        }

                        .tabela-trocas {
                            width: 100%;
                            min-width: 0;
                            table-layout: auto;
                        }

                        .tabela-trocas th,
                        .tabela-trocas td {
                            padding: 12px 14px;
                            white-space: normal;
                            word-break: break-word;
                        }

                        .tabela-trocas th {
                            white-space: nowrap;
                        }

                        .acoes-troca {
                            display: flex;
                            flex-direction: column;
                            gap: 6px;
                            align-items: stretch;
                            width: 120px;
                        }

                        .acoes-troca form {
                            width: 100%;
                        }

                        .acoes-troca form button {
                            width: 100%;
                            margin-left: 0;
                        }

                        .btn-recusar {
                            margin-left: 0;
                        }

                        @media (max-width: 900px) {
                            .dashboard-tabelas {
                                grid-template-columns: 1fr;
                            }

                            .dashboard-bottom {
                                max-width: 100%;
                            }
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
                            <a href="${pageContext.request.contextPath}/index.jsp">Sair</a>
                        </div>
                    </nav>

                    <div class="dashboard-wrapper">

                        <%-- Botão topo --%>
                            <div class="dashboard-btn-topo">
                                <a href="${pageContext.request.contextPath}/livros" class="btn btn-primario">
                                    🔄 Ver Livros para Troca
                                </a>
                            </div>

                            <%-- Duas tabelas lado a lado --%>
                                <div class="dashboard-tabelas">

                                    <%-- Solicitações Pendentes --%>
                                        <div class="card-secao">
                                            <h2>
                                                🔔 Solicitações Pendentes
                                                <c:if test="${not empty listaPendentes}">
                                                    <span class="badge">${fn:length(listaPendentes)}</span>
                                                </c:if>
                                            </h2>
                                            <table class="tabela-trocas">
                                                <thead>
                                                    <tr>
                                                        <th>#</th>
                                                        <th>Livro Oferecido</th>
                                                        <th>Livro Desejado</th>
                                                        <th>Solicitante</th>
                                                        <th>Ações</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:choose>
                                                        <c:when test="${empty listaPendentes}">
                                                            <tr>
                                                                <td colspan="5" class="tabela-vazia">✅ Nenhuma
                                                                    solicitação pendente.</td>
                                                            </tr>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:forEach var="troca" items="${listaPendentes}">
                                                                <tr>
                                                                    <td>${troca.id}</td>
                                                                    <td>${troca.tituloLivroOferecido}</td>
                                                                    <td>${troca.tituloLivroRecebido}</td>
                                                                    <td>${troca.nomeProponente}</td>
                                                                    <td>
                                                                        <div class="acoes-troca">
                                                                            <form
                                                                                action="${pageContext.request.contextPath}/troca"
                                                                                method="POST">
                                                                                <input type="hidden" name="id"
                                                                                    value="${troca.id}">
                                                                                <input type="hidden" name="acao"
                                                                                    value="aceitar">
                                                                                <button type="submit"
                                                                                    class="btn btn-aceitar">✔
                                                                                    Aceitar</button>
                                                                            </form>
                                                                            <form
                                                                                action="${pageContext.request.contextPath}/troca"
                                                                                method="POST">
                                                                                <input type="hidden" name="id"
                                                                                    value="${troca.id}">
                                                                                <input type="hidden" name="acao"
                                                                                    value="recusar">
                                                                                <button type="submit"
                                                                                    class="btn btn-recusar">✖
                                                                                    Recusar</button>
                                                                            </form>
                                                                        </div>
                                                                    </td>
                                                                </tr>
                                                            </c:forEach>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </tbody>
                                            </table>
                                        </div>

                                        <%-- Minhas Propostas --%>
                                            <div class="card-secao">
                                                <h2>📤 Minhas Propostas</h2>
                                                <table class="tabela-trocas">
                                                    <thead>
                                                        <tr>
                                                            <th>#</th>
                                                            <th>Meu Livro</th>
                                                            <th>Livro Desejado</th>
                                                            <th>Solicitante</th>
                                                            <th>Solicitado</th>
                                                            <th>Status</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:choose>
                                                            <c:when test="${empty minhasPropostas}">
                                                                <tr>
                                                                    <td colspan="6" class="tabela-vazia">📭 Você ainda
                                                                        não fez nenhuma proposta.</td>
                                                                </tr>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:forEach var="troca" items="${minhasPropostas}">
                                                                    <tr>
                                                                        <td>${troca.id}</td>
                                                                        <td>${troca.tituloLivroOferecido}</td>
                                                                        <td>${troca.tituloLivroRecebido}</td>
                                                                        <td>${troca.nomeProponente}</td>
                                                                        <td>${troca.nomeSolicitado}</td>
                                                                        <td>
                                                                            <c:choose>
                                                                                <c:when
                                                                                    test="${troca.status == 'PENDENTE'}">
                                                                                    <span
                                                                                        style="color:#e67e22; font-weight:600;">⏳
                                                                                        Pendente</span>
                                                                                </c:when>
                                                                                <c:when
                                                                                    test="${troca.status == 'ACEITA'}">
                                                                                    <span
                                                                                        style="color:#27ae60; font-weight:600;">✔
                                                                                        Aceita</span>
                                                                                </c:when>
                                                                                <c:otherwise>
                                                                                    <span
                                                                                        style="color:#e74c3c; font-weight:600;">✖
                                                                                        Recusada</span>
                                                                                </c:otherwise>
                                                                            </c:choose>
                                                                        </td>
                                                                    </tr>
                                                                </c:forEach>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </tbody>
                                                </table>
                                            </div>

                                </div>

                                <%-- Cadastrar Livro --%>
                                    <div class="card-secao" style="width: 100%;">
                                        <h2>📖 Cadastrar Livro</h2>
                                        <form action="${pageContext.request.contextPath}/livros" method="POST"
                                            class="form-livro">
                                            <div class="form-grupo">
                                                <label for="titulo">Título</label>
                                                <input type="text" id="titulo" name="titulo"
                                                    placeholder="Título do livro" required>
                                            </div>
                                            <div class="form-grupo">
                                                <label for="autor">Autor</label>
                                                <input type="text" id="autor" name="autor" placeholder="Nome do autor"
                                                    required>
                                            </div>
                                            <button type="submit" class="btn btn-primario">+ Cadastrar Livro</button>
                                        </form>
                                    </div>

                    </div>

                </body>

                </html>
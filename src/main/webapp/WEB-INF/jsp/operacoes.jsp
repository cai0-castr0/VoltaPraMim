<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>
<c:url var="logoutAction" value="/operacoes/logout" />

<div style="display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap; margin-bottom: 1.25rem;">
    <div>
        <h1 style="margin-bottom: 0.25rem;">Painel de Operações</h1>
        <p class="detail-description" style="margin: 0;">Simulação acadêmica · Triagem de recebimento e devolução com verificação presencial.</p>
    </div>
    <form method="post" action="${logoutAction}">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
        <button class="button button-small" type="submit">Encerrar sessão</button>
    </form>
</div>

<c:if test="${param.sucesso == 'receber'}">
    <p class="notice" role="status">Recebimento confirmado: o item agora está disponível para retirada.</p>
</c:if>
<c:if test="${param.sucesso == 'devolver'}">
    <p class="notice" role="status">Devolução registrada: o item foi encerrado e removido das buscas públicas.</p>
</c:if>

<section class="detail-panel" style="margin-bottom: 1.5rem;">
    <h2>1. Aguardando entrega (<c:out value="${painel.aguardandoEntrega.size()}" />)</h2>
    <c:choose>
        <c:when test="${empty painel.aguardandoEntrega}">
            <p class="notice">Nenhum objeto aguardando entrega na sala.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="item" items="${painel.aguardandoEntrega}">
                <div style="border-top: 1px solid #e2e8f0; padding: 1rem 0; display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap;">
                    <div>
                        <span class="item-category"><c:out value="${item.referenciaPublica}" /> · <c:out value="${item.categoria.rotulo}" /></span>
                        <h3 style="margin: 0.25rem 0;"><a href="<c:url value='/itens/${item.id}' />"><c:out value="${item.titulo}" /></a></h3>
                        <small>Local encontrado: <c:out value="${item.local.rotulo}" /></small>
                    </div>
                    <form method="post" action="<c:url value='/operacoes/itens/${item.id}/status' />">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                        <input type="hidden" name="acao" value="receber">
                        <button class="button button-small" type="submit">Confirmar recebimento</button>
                    </form>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</section>

<section class="detail-panel" style="margin-bottom: 1.5rem;">
    <h2>2. Disponíveis para retirada (<c:out value="${painel.disponiveisParaRetirada.size()}" />)</h2>
    <c:choose>
        <c:when test="${empty painel.disponiveisParaRetirada}">
            <p class="notice">Nenhum objeto disponível para retirada no momento.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="item" items="${painel.disponiveisParaRetirada}">
                <div style="border-top: 1px solid #e2e8f0; padding: 1rem 0; display: flex; justify-content: space-between; align-items: flex-end; gap: 1rem; flex-wrap: wrap;">
                    <div>
                        <span class="item-category"><c:out value="${item.referenciaPublica}" /> · <c:out value="${item.categoria.rotulo}" /></span>
                        <h3 style="margin: 0.25rem 0;"><a href="<c:url value='/itens/${item.id}' />"><c:out value="${item.titulo}" /></a></h3>
                        <small>Local encontrado: <c:out value="${item.local.rotulo}" /></small>
                    </div>
                    <form method="post" action="<c:url value='/operacoes/itens/${item.id}/status' />" style="display: flex; flex-direction: column; gap: 0.5rem; align-items: flex-start;">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                        <input type="hidden" name="acao" value="devolver">
                        <label style="font-size: 0.9rem; display: flex; align-items: center; gap: 0.4rem; cursor: pointer;">
                            <input type="checkbox" name="posseVerificada" value="true" required>
                            Posse verificada presencialmente pelo atendente
                        </label>
                        <button class="button button-small" type="submit">Registrar devolução</button>
                    </form>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</section>

<section class="detail-panel">
    <h2>3. Histórico de devolvidos (<c:out value="${painel.devolvidos.size()}" />)</h2>
    <c:choose>
        <c:when test="${empty painel.devolvidos}">
            <p class="notice">Nenhuma devolução concluída nesta sessão.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="item" items="${painel.devolvidos}">
                <div style="border-top: 1px solid #e2e8f0; padding: 0.75rem 0;">
                    <span class="item-category"><c:out value="${item.referenciaPublica}" /> · <c:out value="${item.estadoRotulo}" /></span>
                    <p style="margin: 0.25rem 0 0;"><strong><c:out value="${item.titulo}" /></strong> — <c:out value="${item.categoria.rotulo}" /> (<c:out value="${item.local.rotulo}" />)</p>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="rodape.jspf" %>
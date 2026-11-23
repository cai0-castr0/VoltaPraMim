<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>
<c:url var="buscarUrl" value="/buscar" />
<p><a class="text-link" href="${buscarUrl}">← Voltar à busca</a></p>
<article class="detail-panel">
    <div class="item-card-top">
        <%@ include file="status-item.jspf" %>
        <span class="item-category"><c:out value="${item.categoria.rotulo}" /></span>
    </div>
    <h1><c:out value="${item.titulo}" /></h1>
    <c:if test="${not empty fotoUrl}">
        <img class="item-photo"
             src="<c:out value='${fotoUrl}' />"
             alt="Foto do objeto encontrado">
    </c:if>
    <c:if test="${not empty item.descricao}"><p class="detail-description"><c:out value="${item.descricao}" /></p></c:if>
    <dl class="detail-list">
        <div><dt>Local encontrado</dt><dd><c:out value="${item.local.rotulo}" /></dd></div>
        <div><dt>Encontrado em</dt><dd><c:out value="${dataEncontradoEm}" /></dd></div>
        <div><dt>Referência</dt><dd><c:out value="${item.referenciaPublica}" /></dd></div>
        <div><dt>Status</dt><dd><c:out value="${item.estadoRotulo}" /></dd></div>
    </dl>
    <c:choose>
        <c:when test="${item.disponivelParaRetirada}">
            <p class="notice">
                <strong>Disponível para retirada (simulação acadêmica):</strong> procure o atendimento da Sala de Operações informando a referência <strong><c:out value="${item.referenciaPublica}" /></strong> e descreva características não publicadas do objeto para comprovar a posse.
            </p>
        </c:when>
        <c:otherwise>
            <p class="notice">
                Este objeto ainda está aguardando entrega à Sala de Operações e não está disponível para retirada imediata. A referência serve apenas para localizar o registro.
            </p>
        </c:otherwise>
    </c:choose>
</article>
<%@ include file="rodape.jspf" %>
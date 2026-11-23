<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecalho.jspf" %>
<c:url var="loginAction" value="/operacoes/login" />
<c:url var="inicioUrl" value="/" />

<p><a class="text-link" href="${inicioUrl}">← Voltar à página inicial</a></p>

<article class="detail-panel" style="max-width: 30rem;">
    <h1>Painel de Operações</h1>
    <p class="detail-description">Acesso restrito ao atendimento da Sala de Operações (simulação acadêmica).</p>

    <c:if test="${param.saiu == '1'}">
        <p class="notice" role="status">Sessão encerrada com segurança.</p>
    </c:if>

    <c:if test="${not empty erro}">
        <p class="notice" role="alert" style="border-left: 4px solid #b91c1c;"><c:out value="${erro}" /></p>
    </c:if>

    <form method="post" action="${loginAction}">
        <div style="margin-bottom: 1rem;">
            <label for="senha" style="display: block; font-weight: 600; margin-bottom: 0.4rem;">
                Senha de demonstração
            </label>
            <input id="senha"
                   name="senha"
                   type="password"
                   required
                   autocomplete="current-password"
                   style="width: 100%; padding: 0.65rem; border: 1px solid #cbd5e1; border-radius: 0.5rem;">
        </div>
        <button class="button" type="submit">Entrar no painel</button>
    </form>

    <p class="notice" style="margin-top: 1.25rem;">
        Credencial configurada exclusivamente via variável de ambiente <code>VPM_OPERACOES_SENHA</code>.
    </p>
</article>
<%@ include file="rodape.jspf" %>
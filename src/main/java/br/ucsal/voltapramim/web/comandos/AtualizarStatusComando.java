package br.ucsal.voltapramim.web.comandos;

import br.ucsal.voltapramim.aplicacao.ConfirmarRecebimento;
import br.ucsal.voltapramim.aplicacao.RegistrarDevolucao;
import br.ucsal.voltapramim.web.ComandoWeb;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

public final class AtualizarStatusComando implements ComandoWeb {
    private static final String PREFIXO = "/operacoes/itens/";
    private static final String SUFIXO = "/status";

    private final ConfirmarRecebimento confirmarRecebimento;
    private final RegistrarDevolucao registrarDevolucao;

    public AtualizarStatusComando(
            ConfirmarRecebimento confirmarRecebimento,
            RegistrarDevolucao registrarDevolucao
    ) {
        this.confirmarRecebimento = confirmarRecebimento;
        this.registrarDevolucao = registrarDevolucao;
    }

    @Override
    public void executar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        UUID id = extrairId(caminho);

        String acao = request.getParameter("acao");
        if ("receber".equals(acao)) {
            confirmarRecebimento.executar(id);
            response.sendRedirect(request.getContextPath() + "/operacoes?sucesso=receber");
            return;
        }

        if ("devolver".equals(acao)) {
            String paramPosse = request.getParameter("posseVerificada");
            boolean posseVerificada = "true".equalsIgnoreCase(paramPosse) || "on".equalsIgnoreCase(paramPosse);
            registrarDevolucao.executar(id, posseVerificada);
            response.sendRedirect(request.getContextPath() + "/operacoes?sucesso=devolver");
            return;
        }

        throw new IllegalArgumentException("Ação administrativa inválida.");
    }

    private UUID extrairId(String caminho) {
        if (!caminho.startsWith(PREFIXO) || !caminho.endsWith(SUFIXO)) {
            throw new IllegalArgumentException("Caminho de atualização inválido.");
        }
        String bruto = caminho.substring(PREFIXO.length(), caminho.length() - SUFIXO.length());
        try {
            return UUID.fromString(bruto);
        } catch (IllegalArgumentException erro) {
            throw new IllegalArgumentException("Identificador de item inválido.");
        }
    }
}

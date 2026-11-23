package br.ucsal.voltapramim.web.comandos;

import br.ucsal.voltapramim.aplicacao.ListarOperacoes;
import br.ucsal.voltapramim.web.ComandoWeb;
import br.ucsal.voltapramim.web.Views;
import br.ucsal.voltapramim.web.seguranca.SessaoOperacoes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class OperacoesComando implements ComandoWeb {
    private final ListarOperacoes listarOperacoes;

    public OperacoesComando(ListarOperacoes listarOperacoes) {
        this.listarOperacoes = listarOperacoes;
    }

    @Override
    public void executar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            request.setAttribute("csrfToken", SessaoOperacoes.garantirTokenCsrf(sessao));
        }
        request.setAttribute("tituloPagina", "Painel de Operações · VoltaPraMim");
        request.setAttribute("painel", listarOperacoes.executar());
        Views.render(request, response, "operacoes");
    }
}
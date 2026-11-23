package br.ucsal.voltapramim.web.comandos;

import br.ucsal.voltapramim.web.ComandoWeb;
import br.ucsal.voltapramim.web.Views;
import br.ucsal.voltapramim.web.seguranca.AutenticadorOperacoes;
import br.ucsal.voltapramim.web.seguranca.SessaoOperacoes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class TelaLoginOperacoesComando implements ComandoWeb {
    private final AutenticadorOperacoes autenticador;

    public TelaLoginOperacoesComando(AutenticadorOperacoes autenticador) {
        this.autenticador = autenticador;
    }

    @Override
    public void executar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (SessaoOperacoes.estaAutenticado(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/operacoes");
            return;
        }
        request.setAttribute("tituloPagina", "Acesso restrito · Operações");
        request.setAttribute("senhaConfigurada", autenticador.senhaConfigurada());
        Views.render(request, response, "login-operacoes");
    }
}
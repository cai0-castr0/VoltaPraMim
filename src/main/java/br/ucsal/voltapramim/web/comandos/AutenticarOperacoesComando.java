package br.ucsal.voltapramim.web.comandos;

import br.ucsal.voltapramim.web.ComandoWeb;
import br.ucsal.voltapramim.web.Views;
import br.ucsal.voltapramim.web.seguranca.AutenticadorOperacoes;
import br.ucsal.voltapramim.web.seguranca.SessaoOperacoes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class AutenticarOperacoesComando implements ComandoWeb {
    private final AutenticadorOperacoes autenticador;

    public AutenticarOperacoesComando(AutenticadorOperacoes autenticador) {
        this.autenticador = autenticador;
    }

    @Override
    public void executar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.setAttribute("tituloPagina", "Acesso restrito · Operações");
        request.setAttribute("senhaConfigurada", autenticador.senhaConfigurada());

        if (!autenticador.senhaConfigurada()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute(
                    "erro",
                    "Variável de ambiente VPM_OPERACOES_SENHA não configurada no servidor."
            );
            Views.render(request, response, "login-operacoes");
            return;
        }

        String senha = request.getParameter("senha");
        if (!autenticador.autenticar(senha)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute("erro", "Senha de operações inválida.");
            Views.render(request, response, "login-operacoes");
            return;
        }

        SessaoOperacoes.iniciarSessaoAutenticada(request);
        response.sendRedirect(request.getContextPath() + "/operacoes");
    }
}
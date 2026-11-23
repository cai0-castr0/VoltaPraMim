package br.ucsal.voltapramim.web.comandos;

import br.ucsal.voltapramim.web.ComandoWeb;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class LogoutOperacoesComando implements ComandoWeb {
    @Override
    public void executar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/operacoes/login?saiu=1");
    }
}
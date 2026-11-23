package br.ucsal.voltapramim.web.filtro;

import br.ucsal.voltapramim.web.seguranca.SessaoOperacoes;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class AutenticacaoOperacoesFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        if ("/operacoes/login".equals(caminho)) {
            chain.doFilter(request, response);
            return;
        }

        if (!SessaoOperacoes.estaAutenticado(request.getSession(false))) {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                response.sendRedirect(request.getContextPath() + "/operacoes/login");
            } else {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Autenticação obrigatória para ações administrativas."
                );
            }
            return;
        }

        chain.doFilter(request, response);
    }
}
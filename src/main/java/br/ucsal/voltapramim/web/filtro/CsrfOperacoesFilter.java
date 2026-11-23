package br.ucsal.voltapramim.web.filtro;

import br.ucsal.voltapramim.web.seguranca.SessaoOperacoes;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class CsrfOperacoesFilter implements Filter {
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

        HttpSession sessao = request.getSession(false);
        if (sessao != null && SessaoOperacoes.estaAutenticado(sessao)) {
            String tokenSessao = SessaoOperacoes.garantirTokenCsrf(sessao);
            request.setAttribute("csrfToken", tokenSessao);
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String tokenFormulario = request.getParameter("csrfToken");
            if (tokenFormulario == null || tokenFormulario.isBlank()) {
                tokenFormulario = request.getHeader("X-CSRF-Token");
            }

            if (!SessaoOperacoes.validarTokenCsrf(sessao, tokenFormulario)) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Token CSRF ausente ou inválido."
                );
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
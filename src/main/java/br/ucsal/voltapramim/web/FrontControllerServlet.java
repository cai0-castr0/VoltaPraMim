package br.ucsal.voltapramim.web;

import br.ucsal.voltapramim.aplicacao.ItemNaoEncontradoException;
import br.ucsal.voltapramim.config.AppBootstrap;
import br.ucsal.voltapramim.dominio.TransicaoInvalidaException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public final class FrontControllerServlet extends HttpServlet {
    private Roteador roteador;

    @Override
    public void init() throws ServletException {
        Object configurado = getServletContext()
                .getAttribute(AppBootstrap.ROTEADOR);

        if (!(configurado instanceof Roteador valor)) {
            throw new ServletException(
                    "Roteador da aplicação não foi configurado."
            );
        }

        roteador = valor;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {
        despachar(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {
        despachar(request, response);
    }

    private void despachar(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String caminho = request.getRequestURI()
                .substring(request.getContextPath().length());

        ComandoWeb comando = roteador
                .resolver(request.getMethod(), caminho)
                .orElse(null);

        if (comando == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            comando.executar(request, response);
        } catch (ItemNaoEncontradoException erro) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    erro.getMessage()
            );
        } catch (TransicaoInvalidaException erro) {
            response.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    erro.getMessage()
            );
        } catch (IllegalArgumentException erro) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    erro.getMessage()
            );
        } catch (Exception erro) {
            getServletContext().log(
                    "Falha ao processar a requisição.",
                    erro
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Não foi possível concluir a solicitação."
                );
            }
        }
    }
}
package br.ucsal.voltapramim.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class SessaoOperacoes {
    public static final String ATRIBUTO_AUTENTICADO = "voltapramim.operacoes.autenticado";
    public static final String ATRIBUTO_CSRF = "voltapramim.operacoes.csrf";
    public static final int TIMEOUT_SEGUNDOS = 30 * 60;

    private static final SecureRandom RANDOM = new SecureRandom();

    private SessaoOperacoes() {
    }

    public static boolean estaAutenticado(HttpSession sessao) {
        return sessao != null && Boolean.TRUE.equals(sessao.getAttribute(ATRIBUTO_AUTENTICADO));
    }

    public static HttpSession iniciarSessaoAutenticada(HttpServletRequest request) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession nova = request.getSession(true);
        nova.setMaxInactiveInterval(TIMEOUT_SEGUNDOS);
        nova.setAttribute(ATRIBUTO_AUTENTICADO, Boolean.TRUE);
        nova.setAttribute(ATRIBUTO_CSRF, gerarToken());
        return nova;
    }

    public static String garantirTokenCsrf(HttpSession sessao) {
        Object atual = sessao.getAttribute(ATRIBUTO_CSRF);
        if (atual instanceof String token && !token.isBlank()) {
            return token;
        }
        String novoToken = gerarToken();
        sessao.setAttribute(ATRIBUTO_CSRF, novoToken);
        return novoToken;
    }

    public static boolean validarTokenCsrf(HttpSession sessao, String tokenRecebido) {
        if (sessao == null || tokenRecebido == null || tokenRecebido.isBlank()) {
            return false;
        }
        Object esperado = sessao.getAttribute(ATRIBUTO_CSRF);
        if (!(esperado instanceof String tokenEsperado) || tokenEsperado.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                tokenEsperado.getBytes(StandardCharsets.UTF_8),
                tokenRecebido.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
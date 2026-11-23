package br.ucsal.voltapramim.web.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.function.Function;

public final class AutenticadorOperacoes {
    public static final String VAR_SENHA_OPERACOES = "VPM_OPERACOES_SENHA";
    public static final String VAR_SENHA_LEGADO = "VPM_ADMIN_PASSWORD";

    private final Function<String, String> leitorAmbiente;

    public AutenticadorOperacoes() {
        this(chave -> {
            String env = System.getenv(chave);
            if (env != null && !env.isBlank()) {
                return env;
            }
            return System.getProperty(chave);
        });
    }

    public AutenticadorOperacoes(Function<String, String> leitorAmbiente) {
        this.leitorAmbiente = leitorAmbiente;
    }

    public boolean senhaConfigurada() {
        return obterSenhaConfigurada().isPresent();
    }

    public boolean autenticar(String senhaInformada) {
        if (senhaInformada == null || senhaInformada.isBlank()) {
            return false;
        }
        Optional<String> esperada = obterSenhaConfigurada();
        if (esperada.isEmpty()) {
            return false;
        }
        byte[] a = esperada.get().getBytes(StandardCharsets.UTF_8);
        byte[] b = senhaInformada.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private Optional<String> obterSenhaConfigurada() {
        String principal = leitorAmbiente.apply(VAR_SENHA_OPERACOES);
        if (principal != null && !principal.isBlank()) {
            return Optional.of(principal);
        }
        String fallback = leitorAmbiente.apply(VAR_SENHA_LEGADO);
        if (fallback != null && !fallback.isBlank()) {
            return Optional.of(fallback);
        }
        return Optional.empty();
    }
}
package br.ucsal.voltapramim.web;

import br.ucsal.voltapramim.aplicacao.BuscarItens;
import br.ucsal.voltapramim.aplicacao.CadastrarItem;
import br.ucsal.voltapramim.aplicacao.ConfirmarRecebimento;
import br.ucsal.voltapramim.aplicacao.ListarOperacoes;
import br.ucsal.voltapramim.aplicacao.PosseNaoVerificadaException;
import br.ucsal.voltapramim.aplicacao.RegistrarDevolucao;
import br.ucsal.voltapramim.dominio.Categoria;
import br.ucsal.voltapramim.dominio.LocalCampus;
import br.ucsal.voltapramim.dominio.SituacaoItem;
import br.ucsal.voltapramim.dominio.evento.EventoDominio;
import br.ucsal.voltapramim.dominio.evento.ItemDevolvido;
import br.ucsal.voltapramim.dominio.evento.ItemRecebido;
import br.ucsal.voltapramim.infraestrutura.busca.BuscaPorRelevancia;
import br.ucsal.voltapramim.infraestrutura.busca.BuscaRecentes;
import br.ucsal.voltapramim.infraestrutura.eventos.PublicadorEmMemoria;
import br.ucsal.voltapramim.infraestrutura.fotos.ArmazenamentoLocalDeFotos;
import br.ucsal.voltapramim.infraestrutura.persistencia.ItemRepositorioEmMemoria;
import br.ucsal.voltapramim.web.comandos.AtualizarStatusComando;
import br.ucsal.voltapramim.web.comandos.AutenticarOperacoesComando;
import br.ucsal.voltapramim.web.comandos.LogoutOperacoesComando;
import br.ucsal.voltapramim.web.comandos.OperacoesComando;
import br.ucsal.voltapramim.web.filtro.AutenticacaoOperacoesFilter;
import br.ucsal.voltapramim.web.filtro.CsrfOperacoesFilter;
import br.ucsal.voltapramim.web.seguranca.AutenticadorOperacoes;
import br.ucsal.voltapramim.web.seguranca.SessaoOperacoes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class OperacoesSegurancaTest {

    @Test
    void roteadorResolveRotasAdministrativasERejeitaMetodoInvalido() {
        ComandoWeb vazio = (req, res) -> {};
        ComandoWeb atualizar = (req, res) -> {};

        Roteador roteador = new Roteador(
                Map.of(
                        new ChaveRota("GET", "/operacoes/login"), vazio,
                        new ChaveRota("POST", "/operacoes/login"), vazio,
                        new ChaveRota("POST", "/operacoes/logout"), vazio,
                        new ChaveRota("GET", "/operacoes"), vazio
                ),
                vazio,
                vazio,
                atualizar
        );

        String id = UUID.randomUUID().toString();
        assertSame(vazio, roteador.resolver("GET", "/operacoes/login").orElseThrow());
        assertSame(vazio, roteador.resolver("POST", "/operacoes/login").orElseThrow());
        assertSame(vazio, roteador.resolver("POST", "/operacoes/logout").orElseThrow());
        assertSame(vazio, roteador.resolver("GET", "/operacoes").orElseThrow());
        assertSame(atualizar, roteador.resolver("POST", "/operacoes/itens/" + id + "/status").orElseThrow());
        assertTrue(roteador.resolver("GET", "/operacoes/itens/" + id + "/status").isEmpty());
    }

    @Test
    void autenticacaoFilterBloqueiaAcessoSemSessao() throws Exception {
        AutenticacaoOperacoesFilter filtro = new AutenticacaoOperacoesFilter();
        AtomicBoolean cadeiaExecutada = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> cadeiaExecutada.set(true);

        SimuladorHttp getPainel = new SimuladorHttp("GET", "/operacoes", null);
        filtro.doFilter(getPainel.request(), getPainel.response(), chain);
        assertFalse(cadeiaExecutada.get());
        assertEquals("/operacoes/login", getPainel.redirectUrl.get());

        SimuladorHttp postStatus = new SimuladorHttp("POST", "/operacoes/itens/" + UUID.randomUUID() + "/status", null);
        filtro.doFilter(postStatus.request(), postStatus.response(), chain);
        assertFalse(cadeiaExecutada.get());
        assertEquals(401, postStatus.statusCode.get());
    }

    @Test
    void csrfFilterBloqueiaPostSemTokenOuInvalidoEAceitaTokenValido() throws Exception {
        CsrfOperacoesFilter filtro = new CsrfOperacoesFilter();
        SimuladorSessao sessaoAnterior = new SimuladorSessao();

        SimuladorHttp login = new SimuladorHttp("POST", "/operacoes/login", sessaoAnterior);
        login.params.put("senha", "segredo-teste");
        AutenticadorOperacoes auth = new AutenticadorOperacoes(nome -> "segredo-teste");
        new AutenticarOperacoesComando(auth).executar(login.request(), login.response());

        assertEquals("/operacoes", login.redirectUrl.get());
        assertTrue(sessaoAnterior.invalidada.get());

        HttpSession sessaoAutenticada = login.request().getSession(false);
        assertNotNull(sessaoAutenticada);
        assertTrue(SessaoOperacoes.estaAutenticado(sessaoAutenticada));

        String tokenValido = SessaoOperacoes.garantirTokenCsrf(sessaoAutenticada);
        AtomicBoolean cadeiaExecutada = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> cadeiaExecutada.set(true);

        // POST sem token CSRF -> 403
        SimuladorHttp postSemToken = new SimuladorHttp("POST", "/operacoes/itens/" + UUID.randomUUID() + "/status", login.sessao);
        filtro.doFilter(postSemToken.request(), postSemToken.response(), chain);
        assertFalse(cadeiaExecutada.get());
        assertEquals(403, postSemToken.statusCode.get());

        // POST com token CSRF inválido -> 403
        SimuladorHttp postTokenErrado = new SimuladorHttp("POST", "/operacoes/itens/" + UUID.randomUUID() + "/status", login.sessao);
        postTokenErrado.params.put("csrfToken", "token-adulterado");
        filtro.doFilter(postTokenErrado.request(), postTokenErrado.response(), chain);
        assertFalse(cadeiaExecutada.get());
        assertEquals(403, postTokenErrado.statusCode.get());

        // POST com token CSRF válido -> prossegue
        SimuladorHttp postValido = new SimuladorHttp("POST", "/operacoes/itens/" + UUID.randomUUID() + "/status", login.sessao);
        postValido.params.put("csrfToken", tokenValido);
        filtro.doFilter(postValido.request(), postValido.response(), chain);
        assertTrue(cadeiaExecutada.get());
    }

    @Test
    void fluxoOperacoesRecebeVerificaPosseDevolveEPublicaEventos(@TempDir Path pasta) throws Exception {
        var repositorio = new ItemRepositorioEmMemoria();
        var publicador = new PublicadorEmMemoria();
        List<EventoDominio> eventos = new ArrayList<>();
        publicador.inscrever(eventos::add);

        var cadastrar = new CadastrarItem(repositorio, publicador, new ArmazenamentoLocalDeFotos(pasta));
        var buscar = new BuscarItens(repositorio, new BuscaPorRelevancia(), new BuscaRecentes());
        var confirmar = new ConfirmarRecebimento(repositorio, publicador);
        var devolver = new RegistrarDevolucao(repositorio, publicador);
        var listar = new ListarOperacoes(repositorio);

        var item = cadastrar.executar("Garrafa Térmica", "Azul fosco", Categoria.OUTROS, LocalCampus.BIBLIOTECA);
        var atualizarComando = new AtualizarStatusComando(confirmar, devolver);
        var painelComando = new OperacoesComando(listar);

        SimuladorSessao sessao = new SimuladorSessao();
        HttpSession httpSession = sessao.proxy();
        SessaoOperacoes.garantirTokenCsrf(httpSession);

        // 1. Painel lista item em aguardandoEntrega
        SimuladorHttp getPainel = new SimuladorHttp("GET", "/operacoes", sessao);
        painelComando.executar(getPainel.request(), getPainel.response());
        assertEquals("/WEB-INF/jsp/operacoes.jsp", getPainel.forwardedPath.get());

        // 2. Confirmar recebimento
        SimuladorHttp postReceber = new SimuladorHttp("POST", "/operacoes/itens/" + item.getId() + "/status", sessao);
        postReceber.params.put("acao", "receber");
        atualizarComando.executar(postReceber.request(), postReceber.response());
        assertEquals(SituacaoItem.DISPONIVEL_RETIRADA, repositorio.buscarPorId(item.getId()).orElseThrow().getSituacao());
        assertTrue(eventos.stream().anyMatch(e -> e instanceof ItemRecebido));

        // 3. Devolver sem posse verificada falha e não altera estado
        SimuladorHttp postDevolverSemPosse = new SimuladorHttp("POST", "/operacoes/itens/" + item.getId() + "/status", sessao);
        postDevolverSemPosse.params.put("acao", "devolver");
        assertThrows(PosseNaoVerificadaException.class,
                () -> atualizarComando.executar(postDevolverSemPosse.request(), postDevolverSemPosse.response()));
        assertEquals(SituacaoItem.DISPONIVEL_RETIRADA, repositorio.buscarPorId(item.getId()).orElseThrow().getSituacao());

        // 4. Devolver com posse verificada conclui, publica evento e remove da busca pública
        SimuladorHttp postDevolverComPosse = new SimuladorHttp("POST", "/operacoes/itens/" + item.getId() + "/status", sessao);
        postDevolverComPosse.params.put("acao", "devolver");
        postDevolverComPosse.params.put("posseVerificada", "true");
        atualizarComando.executar(postDevolverComPosse.request(), postDevolverComPosse.response());
        assertEquals(SituacaoItem.DEVOLVIDO, repositorio.buscarPorId(item.getId()).orElseThrow().getSituacao());
        assertTrue(eventos.stream().anyMatch(e -> e instanceof ItemDevolvido));
        assertTrue(buscar.executar("", null, null).isEmpty());

        // 5. Logout invalida sessão
        SimuladorHttp postLogout = new SimuladorHttp("POST", "/operacoes/logout", sessao);
        new LogoutOperacoesComando().executar(postLogout.request(), postLogout.response());
        assertTrue(sessao.invalidada.get());
        assertEquals("/operacoes/login?saiu=1", postLogout.redirectUrl.get());
    }

    private static final class SimuladorSessao {
        final Map<String, Object> attrs = new HashMap<>();
        final AtomicBoolean invalidada = new AtomicBoolean(false);
        final AtomicInteger timeout = new AtomicInteger();

        HttpSession proxy() {
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getAttribute" -> invalidada.get() ? null : attrs.get(args[0]);
                        case "setAttribute" -> {
                            if (!invalidada.get()) {
                                attrs.put((String) args[0], args[1]);
                            }
                            yield null;
                        }
                        case "invalidate" -> {
                            invalidada.set(true);
                            attrs.clear();
                            yield null;
                        }
                        case "setMaxInactiveInterval" -> {
                            timeout.set((Integer) args[0]);
                            yield null;
                        }
                        default -> null;
                    }
            );
        }
    }

    private static final class SimuladorHttp {
        final String method;
        final String uri;
        SimuladorSessao sessao;
        final Map<String, String> params = new HashMap<>();
        final Map<String, String> headers = new HashMap<>();
        final Map<String, Object> attrs = new HashMap<>();
        final AtomicInteger statusCode = new AtomicInteger(200);
        final AtomicReference<String> redirectUrl = new AtomicReference<>();
        final AtomicReference<String> forwardedPath = new AtomicReference<>();

        SimuladorHttp(String method, String uri, SimuladorSessao sessao) {
            this.method = method;
            this.uri = uri;
            this.sessao = sessao;
        }

        HttpServletRequest request() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    (proxy, m, args) -> switch (m.getName()) {
                        case "getMethod" -> method;
                        case "getRequestURI" -> uri;
                        case "getContextPath" -> "";
                        case "getParameter" -> params.get(args[0]);
                        case "getHeader" -> headers.get(args[0]);
                        case "getAttribute" -> attrs.get(args[0]);
                        case "setAttribute" -> {
                            attrs.put((String) args[0], args[1]);
                            yield null;
                        }
                        case "getSession" -> {
                            boolean create = args == null || (Boolean) args[0];
                            if (sessao != null && !sessao.invalidada.get()) {
                                yield sessao.proxy();
                            }
                            if (create) {
                                sessao = new SimuladorSessao();
                                yield sessao.proxy();
                            }
                            yield null;
                        }
                        case "getRequestDispatcher" -> (RequestDispatcher) Proxy.newProxyInstance(
                                RequestDispatcher.class.getClassLoader(),
                                new Class<?>[]{RequestDispatcher.class},
                                (p, dm, dargs) -> {
                                    if ("forward".equals(dm.getName())) {
                                        forwardedPath.set((String) args[0]);
                                    }
                                    return null;
                                }
                        );
                        default -> null;
                    }
            );
        }

        HttpServletResponse response() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    (proxy, m, args) -> switch (m.getName()) {
                        case "sendRedirect" -> {
                            statusCode.set(302);
                            redirectUrl.set((String) args[0]);
                            yield null;
                        }
                        case "sendError" -> {
                            statusCode.set((Integer) args[0]);
                            yield null;
                        }
                        case "setStatus" -> {
                            statusCode.set((Integer) args[0]);
                            yield null;
                        }
                        case "isCommitted" -> false;
                        default -> null;
                    }
            );
        }
    }
}
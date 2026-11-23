package br.ucsal.voltapramim.config;

import br.ucsal.voltapramim.aplicacao.BuscarItens;
import br.ucsal.voltapramim.aplicacao.CadastrarItem;
import br.ucsal.voltapramim.aplicacao.ConfirmarRecebimento;
import br.ucsal.voltapramim.aplicacao.ConsultarItem;
import br.ucsal.voltapramim.aplicacao.ListarOperacoes;
import br.ucsal.voltapramim.aplicacao.RegistrarDevolucao;
import br.ucsal.voltapramim.aplicacao.porta.ArmazenamentoDeFotos;
import br.ucsal.voltapramim.aplicacao.porta.RepositorioDeItens;
import br.ucsal.voltapramim.infraestrutura.busca.BuscaPorRelevancia;
import br.ucsal.voltapramim.infraestrutura.busca.BuscaRecentes;
import br.ucsal.voltapramim.infraestrutura.demo.DadosDemonstracao;
import br.ucsal.voltapramim.infraestrutura.eventos.AuditoriaObserver;
import br.ucsal.voltapramim.infraestrutura.eventos.EstatisticasObserver;
import br.ucsal.voltapramim.infraestrutura.eventos.PublicadorEmMemoria;
import br.ucsal.voltapramim.infraestrutura.fotos.ArmazenamentoLocalDeFotos;
import br.ucsal.voltapramim.infraestrutura.persistencia.ItemRepositorioEmMemoria;
import br.ucsal.voltapramim.web.ChaveRota;
import br.ucsal.voltapramim.web.ComandoWeb;
import br.ucsal.voltapramim.web.Roteador;
import br.ucsal.voltapramim.web.comandos.AtualizarStatusComando;
import br.ucsal.voltapramim.web.comandos.AutenticarOperacoesComando;
import br.ucsal.voltapramim.web.comandos.BuscaComando;
import br.ucsal.voltapramim.web.comandos.DesignSystemComando;
import br.ucsal.voltapramim.web.comandos.DetalheComando;
import br.ucsal.voltapramim.web.comandos.FotoComando;
import br.ucsal.voltapramim.web.comandos.InicioComando;
import br.ucsal.voltapramim.web.comandos.LogoutOperacoesComando;
import br.ucsal.voltapramim.web.comandos.NovoItemComando;
import br.ucsal.voltapramim.web.comandos.OperacoesComando;
import br.ucsal.voltapramim.web.comandos.SalvarItemComando;
import br.ucsal.voltapramim.web.comandos.TelaLoginOperacoesComando;
import br.ucsal.voltapramim.web.seguranca.AutenticadorOperacoes;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.nio.file.Path;
import java.util.Map;

public final class AppBootstrap implements ServletContextListener {
    public static final String ROTEADOR = "voltapramim.roteador";

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        RepositorioDeItens repositorio =
                new ItemRepositorioEmMemoria();

        PublicadorEmMemoria publicador =
                new PublicadorEmMemoria();

        publicador.inscrever(new AuditoriaObserver());
        publicador.inscrever(new EstatisticasObserver());

        ArmazenamentoDeFotos fotos =
                new ArmazenamentoLocalDeFotos(diretorioFotos());

        CadastrarItem cadastrar =
                new CadastrarItem(repositorio, publicador, fotos);

        BuscarItens buscar = new BuscarItens(
                repositorio,
                new BuscaPorRelevancia(),
                new BuscaRecentes()
        );

        ConsultarItem consultar = new ConsultarItem(repositorio);
        ConfirmarRecebimento confirmarRecebimento =
                new ConfirmarRecebimento(repositorio, publicador);
        RegistrarDevolucao registrarDevolucao =
                new RegistrarDevolucao(repositorio, publicador);
        ListarOperacoes listarOperacoes =
                new ListarOperacoes(repositorio);
        AutenticadorOperacoes autenticador =
                new AutenticadorOperacoes();

        DadosDemonstracao.carregar(cadastrar);

        Map<ChaveRota, ComandoWeb> comandos = Map.ofEntries(
                Map.entry(new ChaveRota("GET", "/"), new InicioComando(buscar)),
                Map.entry(new ChaveRota("GET", "/home"), new InicioComando(buscar)),
                Map.entry(new ChaveRota("GET", "/buscar"), new BuscaComando(buscar)),
                Map.entry(new ChaveRota("GET", "/achei"), new NovoItemComando()),
                Map.entry(new ChaveRota("POST", "/achei"), new SalvarItemComando(cadastrar)),
                Map.entry(new ChaveRota("GET", "/design-system"), new DesignSystemComando()),
                Map.entry(new ChaveRota("GET", "/operacoes/login"), new TelaLoginOperacoesComando(autenticador)),
                Map.entry(new ChaveRota("POST", "/operacoes/login"), new AutenticarOperacoesComando(autenticador)),
                Map.entry(new ChaveRota("POST", "/operacoes/logout"), new LogoutOperacoesComando()),
                Map.entry(new ChaveRota("GET", "/operacoes"), new OperacoesComando(listarOperacoes))
        );

        evento.getServletContext().setAttribute(
                ROTEADOR,
                new Roteador(
                        comandos,
                        new DetalheComando(consultar),
                        new FotoComando(fotos),
                        new AtualizarStatusComando(confirmarRecebimento, registrarDevolucao)
                )
        );
    }

    private Path diretorioFotos() {
        String diretorioConfigurado =
                System.getenv("VPM_FOTOS_DIR");

        if (diretorioConfigurado == null
                || diretorioConfigurado.isBlank()) {

            return Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "voltapramim-fotos"
            );
        }

        return Path.of(diretorioConfigurado);
    }
}
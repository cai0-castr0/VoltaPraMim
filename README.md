# VoltaPraMim

Página inicial do mural acadêmico de achados e perdidos da UCSal. A aplicação usa Java 17, Maven, Servlet/JSP e CSS/JavaScript puros. A aplicação carrega itens fictícios de demonstração em memória.

## Requisitos

- JDK 17
- Maven 3.9+

## Executar localmente

Na raiz do projeto:

```sh
mvn test
mvn jetty:run
```

Para habilitar o painel administrativo (`/operacoes`), defina a senha de demonstração via variável de ambiente antes de iniciar o servidor (no PowerShell, use `-Djetty.http.port=8081` caso a porta `8080` já esteja em uso):

```powershell
$env:VPM_FOTOS_DIR = "$env:USERPROFILE\voltapramim-fotos"
$env:VPM_OPERACOES_SENHA = "sua-senha-local"
mvn jetty:run
```

Abra <http://localhost:8080> (ou `http://localhost:8080/operacoes` para o painel de atendimento). O servidor continua rodando no terminal; use `Ctrl+C` para pará-lo. Os dados de demonstração são recriados a cada inicialização.

## Fotos de objetos

O cadastro aceita foto opcional para itens que não sejam da categoria
`DOCUMENTOS`.

Formatos permitidos:

- JPEG;
- PNG;
- até 5 MiB.

A aplicação valida o conteúdo real do arquivo, não apenas sua extensão.
Arquivos enviados ficam fora de `src/main/webapp`, recebem um identificador
UUID e são acessados somente pela rota controlada `/fotos/{id}`.

## Estrutura da página

- `AppBootstrap` conecta um único `ItemRepositorioEmMemoria` ao cadastro, à busca, aos detalhes e ao painel de operações; `DadosDemonstracao` carrega os exemplos nesse repositório.
- `FrontControllerServlet` e `Roteador` encaminham rotas públicas (`/`, `/home`, `/buscar`, `/achei`, `/itens/*`, `/fotos/*`, `/design-system`) e administrativas (`/operacoes`, `/operacoes/login`, `/operacoes/logout`, `/operacoes/itens/{id}/status`) aos respectivos comandos.
- `AutenticacaoOperacoesFilter` e `CsrfOperacoesFilter` protegem `/operacoes` e `/operacoes/*` com sessão `HttpOnly` (timeout de 30 minutos) e token CSRF por sessão.
- A busca instantânea em `static/js/home.js` filtra os itens ativos renderizados; enviar o formulário abre a busca completa em `/buscar`.
- Os dados ficam em memória e são apagados ao reiniciar. Não há banco de dados nem framework front-end.

A especificação revisada menciona Java 21; este repositório usa Java 17. A versão definitiva deve ser confirmada pela equipe.

## Funcionalidades existentes

- Página inicial com itens recentes.
- Cadastro público com título, descrição, categoria e local.
- Busca sem diferenciar maiúsculas/minúsculas ou acentos; filtros de categoria e local podem ser combinados.
- Página de detalhes com referência pública, status e instrução de retirada condicional ao estado `Disponível para retirada`.
- Ciclo de vida modelado com State (`Aguardando entrega` → `Disponível para retirada` → `Devolvido`) com atualização atômica por item.
- Busca por relevância e por itens recentes usando Strategy.
- Eventos de domínio (`ItemCadastrado`, `ItemRecebido`, `ItemDevolvido`) com observers para auditoria e contagem discriminada por tipo.
- Despacho HTTP por comandos.
- Upload opcional de imagens JPEG e PNG para itens permitidos, bloqueio de foto para Documentos e armazenamento local privado com UUID.
- Painel de operações (`/operacoes`) autenticado via `VPM_OPERACOES_SENHA`, proteção CSRF em mutações `POST`, confirmação de recebimento e registro de devolução com verificação presencial de posse.

## Limites desta versão

O armazenamento é em memória e os itens são apagados ao reiniciar. As fotos podem permanecer no diretório local configurado por `VPM_FOTOS_DIR`; a limpeza desse diretório ainda não é automatizada. O painel `/operacoes` funciona como **simulação acadêmica**: utiliza credencial única de demonstração via ambiente, sem contas individuais, sem integração institucional com sistemas da UCSal e sem aceite formal da Sala de Operações.

Os testes automatizados (`mvn test` — 57 testes aprovados) cobrem regras de estado, eventos e observadores, busca e filtros, upload de fotos, roteamento, autenticação e proteção CSRF. O roteiro manual registra verificações complementares de produto e operação.

O PDF de especificação está em [`docs/especificacao/VoltaPraMim_Project.pdf`](docs/especificacao/VoltaPraMim_Project.pdf). Consulte também a [arquitetura](docs/arquitetura.md), as [decisões pendentes](docs/decisoes.md), o [plano de validação](docs/validacao.md) e o [roteiro de teste manual](tests/roteiro-teste-manual.md).
# Roteiro de teste manual

Os itens abaixo descrevem verificações planejadas. Registre resultados somente depois de executá-las; ainda não há resultados anotados.

1. Iniciar a aplicação com `mvn jetty:run` (com `VPM_OPERACOES_SENHA` configurada no ambiente) e abrir `http://localhost:8080`.
2. Confirmar a página inicial e os três itens fictícios.
3. Abrir `/buscar`, pesquisar por texto com e sem acentos e combinar categoria/local.
4. Abrir um detalhe e confirmar referência e estado “Aguardando entrega”.
5. Enviar cadastro válido e conferir que o item aparece nos resultados.
6. Tentar enviar campos obrigatórios vazios e título acima de 80 caracteres.
7. Verificar que itens aguardando não prometem retirada.
8. Conferir foco por teclado, labels, navegação no celular e contraste.


## Upload de fotos

| Cenário | Passos | Resultado esperado |
|---|---|---|
| Cadastro sem foto | Abrir `/achei`, preencher título, categoria e local; enviar sem selecionar arquivo. | Cadastro é concluído e o detalhe não mostra área de imagem. |
| JPEG válido | Selecionar uma imagem JPEG menor que 5 MiB e cadastrar um item comum. | Cadastro é concluído; o detalhe mostra a imagem. |
| PNG válido | Selecionar uma imagem PNG menor que 5 MiB e cadastrar um item comum. | Cadastro é concluído; o detalhe mostra a imagem. |
| Arquivo inválido | Selecionar um arquivo de texto ou outro formato não permitido. | Formulário mostra mensagem clara; nenhum Item é cadastrado. |
| Arquivo grande | Selecionar imagem maior que 5 MiB. | Formulário mostra mensagem sobre o limite; nenhum Item é cadastrado. |
| Documento com foto | Selecionar categoria Documentos e anexar uma imagem. | Formulário rejeita o cadastro; nenhum arquivo deve ser salvo. |
| Foto inexistente | Abrir `/fotos/5e2edca0-858f-4e4c-827c-29308063a86b`. | Servidor retorna HTTP 404. |
| Foto malformada | Abrir `/fotos/../segredo`. | Servidor retorna HTTP 404 ou rejeita a rota; nenhum arquivo externo é acessado. |

## Painel de operações

| Cenário | Passos | Resultado esperado |
|---|---|---|
| Acesso sem sessão | Abrir `/operacoes` diretamente no navegador sem autenticar. | Redireciona para `/operacoes/login` e nenhum item é exibido ou alterado. |
| Senha incorreta | Enviar senha diferente de `VPM_OPERACOES_SENHA` em `/operacoes/login`. | Retorna HTTP 401 com mensagem clara e não inicia sessão. |
| Login válido | Informar a senha configurada em `VPM_OPERACOES_SENHA` e enviar. | Inicia sessão e abre `/operacoes` com listas separadas por estado. |
| POST sem CSRF | Enviar requisição `POST` para `/operacoes/itens/{id}/status` sem `csrfToken` ou com token inválido. | Servidor retorna HTTP 403 e mantém o estado original do item. |
| Confirmar recebimento | Clicar em **Confirmar recebimento** em um item na lista *Aguardando entrega*. | Item passa para *Disponíveis para retirada* e `/itens/{id}` exibe a instrução de retirada. |
| Devolução sem posse | Tentar enviar devolução de item disponível sem marcar a confirmação presencial de posse. | Ação é recusada e o item permanece disponível para retirada. |
| Devolução com posse | Marcar *Posse verificada presencialmente pelo atendente* e clicar em **Registrar devolução**. | Item vai para o histórico de *Devolvidos* e deixa de aparecer nas buscas públicas (`/` e `/buscar`). |
| Logout | Clicar em **Encerrar sessão** no painel. | Sessão é invalidada e novo acesso a `/operacoes` exige login novamente. |

## Fora desta versão

Integração institucional com a Sala de Operações da UCSal, persistência em banco H2 e busca com sinônimos (Decorator) permanecem fora do núcleo obrigatório desta entrega.

| Data | Verificação | Resultado real | Evidência / observações |
| --- | --- | --- | --- |
| 23/11/2026 | Fluxo completo do painel `/operacoes` em `mvn jetty:run` | Aprovado em ambiente local | Bloqueio sem sessão, login via `VPM_OPERACOES_SENHA`, recebimento, verificação presencial de posse, devolução (remoção da busca pública) e logout validados. |
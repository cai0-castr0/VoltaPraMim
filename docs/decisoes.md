# Decisões pendentes

Este registro separa regras definidas no guia de decisões que ainda precisam de confirmação da equipe ou possuem limites declarados no protótipo.

| Tema | Direção inicial | Estado |
| --- | --- | --- |
| Atendimento da Sala de Operações | Fluxo implementado como simulação acadêmica em `/operacoes`, sem aceite institucional formal nem integração com sistemas da UCSal. | Simulado |
| Credencial e sessão administrativa | Senha de demonstração lida exclusivamente via `VPM_OPERACOES_SENHA`, sessão `HttpOnly` com expiração de 30 minutos; sem contas individuais. | Implementado (MVP) |
| Proteção CSRF no painel | Token vinculado à sessão validado no servidor em todos os `POST` sob `/operacoes` (`401` sem sessão, `403` sem token válido). | Implementado |
| Local e horário de retirada | Instrução de retirada exibida em `/itens/{id}` apenas após confirmação de recebimento (`DISPONIVEL_RETIRADA`); horários fixos dependem de aceite. | Parcial |
| Persistência após reinício | Armazenamento em memória com atualização atômica por item; dados desaparecem a cada reinício do servidor (avaliar H2 como evolução). | Proposto |
| Verificação de posse | Exige confirmação explícita do atendente após conferência presencial de característica não publicada; referência `VPM-XXXXX` apenas localiza o registro. | Implementado |
| Moderação do conteúdo público | Definir regras e limites antes de uso real. | Pendente |
| Busca com sinônimos | Adicionar Decorator após estabilizar o núcleo e seus testes. | Condicional |
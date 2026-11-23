# Plano de validação

As hipóteses de produto abaixo dependem de testes externos com usuários e conversa institucional, permanecendo registradas sem resultados fabricados[cite: 45]. A seção seguinte documenta a verificação técnica e funcional executada pela equipe no protótipo.

| Hipótese | Método | Evidência a registrar | Estado |
| --- | --- | --- | --- |
| Quem encontra aceita cadastrar um item em menos de um minuto. | Teste cronometrado com pessoas fora da equipe. | Tempo observado, contexto e dificuldades, sem dados pessoais. | Não iniciado |
| Quem perdeu consultaria o site sem login. | Formulário anônimo e teste guiado. | Tarefas concluídas, dúvidas e observações. | Não iniciado |
| Sala de Operações pode centralizar a retirada. | Conversa com responsável e aceite operacional. | Data, papel do interlocutor e decisão; se não confirmado, manter simulação. | Simulado (sem aceite formal) |

## Verificação técnica e funcional executada

| Data | Verificação | Resultado real | Evidência / observações |
| --- | --- | --- | --- |
| 23/11/2026 | Suíte automatizada JUnit (`mvn test`) | Aprovado (`Tests run: 57, Failures: 0, Errors: 0, Skipped: 0`) | Cobre domínio/State, eventos/Observer, busca/Strategy, upload de fotos, roteamento/Command, sessão e CSRF. |
| 23/11/2026 | Fluxo completo no navegador (`mvn jetty:run`) | Aprovado em ambiente local | Validados bloqueio sem sessão (`/operacoes` -> `/operacoes/login`), login via `VPM_OPERACOES_SENHA`, recebimento, verificação presencial de posse, devolução (saída da busca pública) e logout em simulação acadêmica. |
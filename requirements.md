## Descrição do Projeto Prático​

O projeto prático consiste em implementar um software completo para Web
utilizando no front-end as tecnologias HTML, CSS e JS e no back-end o
ecossistema Java EE.
Os requisitos funcionais e não funcionais comum a todas as aplicações
(sistema web) são:

1. Autenticar usuário: O sistema deverá solicitar a autenticação de
usuário por meio de e-mail e senha. Somente usuários autenticados
poderão ter acesso à página principal da aplicação.
2. Manter usuário: O sistema deverá permitir o cadastro de usuários
que poderão acessar os recursos da aplicação e executar funções
conforme seu perfil de usuário.
3. Manter perfil de usuário: O sistema deverá permitir o cadastro de
perfil de usuários com configurações específicas de acesso a cada
recurso funcional disponível na aplicação.
4. Exibir opções de navegação de recursos: O sistema deverá
apresentar um mecanismo de navegação para todas as interfaces de
usuário de modo que permita retornar ao passo anterior ou a interface
principal da aplicação. Uma sugestão para implementação desse
mecanismo de navegação é a utilização de âncoras (links)
disponíveis em um menu da aplicação.
5. Dois casos de uso específico do domínio do seu problema: Cada
integrante do grupo deverá definir pelo menos dois requisitos
funcionais para sua aplicação e desenvolvê-los integralmente. Não
serão aceitos como casos de uso cadastros de dados auxiliares
(Ex: cadastro de tipo, cadastro de status, cadastro de categoria, etc…)
Caso sua aplicação possua cadastro de usuário a partir da interface de autenticação (login), é importante que defina pelo menos um caso de
uso com necessidade de permissões de acesso distintas. Ex: caso seja
um e-commerce, a função compra pode ser utilizada por qualquer
usuário autenticado, porém o cadastro de produtos só pode ser
acessado por um usuário com perfil administrador.​
6. Rastreabilidade e Auditoria: O sistema deve manter TODAS as
ações executadas por todos os usuários a fim de permitir auditoria,
logo, um registro de log de uso do sistema deverá ser mantido
contendo minimamente:
  
  a) a ação executada,  
  b) o usuário executor e  
  c) data e hora da ação executada.

Requisitos Não Funcionais:
- Linguagem Java EE: O sistema deverá obrigatoriamente ser
implementado em linguagem Java. É desejável que seja utilizada a
versão 11 ou superior.
- Modelo MVC: O sistema deverá obrigatoriamente utilizar o modelo
de desenvolvimento em camadas MVC independente do
framework utilizado.
- JAX-RS: O sistema deverá obrigatoriamente utilizar a especificação
JAX-RS para a implementação dos endpoints das requisições REST.
- Quarkus: É desejável que o sistema utilize o framework Quarkus.
- Utilização dos padrões DAO e Entity: Para cada entidade do seu
sistema, deverá ser criada uma classe na camada de modelo que
represente a estrutura de dados da entidade (Entity) e o respectivo
objeto de acesso a dados (DAO) para manter e recuperar os dados da
respectiva entidade.
- Utilização do padrão BO: TODAS as regras de negócios deverão
ser implementadas utilizando objetos Business Object (BO). Isso
inclui também as validações de dados a serem persistidos.
- Comunicação entre back-end e front-end exclusivamente por
DTO: Por motivos de segurança, uma entidade nunca deve ser
transitada na estrutura em que é persistida para o front-end. Logo,
para cada interface de usuário (UI), os DTOs necessários para o funcionamento da respectiva UI deverão ser projetados para trafegar
somente os dados necessários.
A avaliação do projeto prático será realizada subdividida em três
avaliações, sendo uma por bimestre:
2º Bimestre: Desenvolvimento Front-End
- Aplicação dos fundamentos (AF);
- Avaliação do desenvolvimento, estilização e funcionamento
completo das interfaces de usuário (FE).
- Apresentação e Arguição (AA) - 10 pontos
  - Apresentação coletiva
  - Pelo menos 3 perguntas por aluno com resposta individual
​
3º Bimestre: Desenvolvimento Back-End e Comunicação Front-End e
Back-End
- Aplicação dos fundamentos (AF);
- Avaliação do desenvolvimento, estilização e funcionamento
completo das interfaces de usuário (FE).
- Fluxo completo dos casos de uso sem banco de dados (FC)
  - comunicação entre front-end, back-end
- Apresentação e Arguição (AA) - 10 pontos
  - Apresentação coletiva
  - Pelo menos 3 perguntas por aluno com resposta individual
4º Bimestre: Desenvolvimento Back-End, Comunicação Front-End e
Back-End e Persistência.
- Aplicação dos fundamentos (AF);
- Avaliação do desenvolvimento, estilização e funcionamento
completo das interfaces de usuário (FE).
- Fluxo completo dos casos de uso sem banco de dados (FC)
     - comunicação entre front-end, back-end
- Persistência e recuperação dos dados (PE)

- Apresentação e Arguição (AA) - 10 pontos
     - Apresentação coletiva
     - Pelo menos 3 perguntas por aluno com resposta individual

Critérios de avaliação:
- Corretude
- Completude
- Clareza
Cálculo do nota final individual:
NB2 = (AF + FE) * (AA / 10);
NB3 = (AF + FE + FC) * (AA / 10);
NB4 = (AF + FE + FC + PE) * (AA / 10);
NF = NB1 + NB2 + NB3 +NB4 / 4;
Aprovação: NF >= 6.0
Sobre a avaliação:
- A data e horário limite para TÉRMINO entrega e apresentação
será o penúltimo dia de aula às 22h.
- Não serão aceitos trabalhos após essa data e horário limite.
- Não serão aceitos apresentações após essa data e horário
limite.
- Não haverá possibilidade de reapresentação.
- Ausência na avaliação Apresentação e Arguição implicará na
reprovação automática (AA = 0).
- Os códigos fontes do trabalho deverão ser entregues via
repositório de código (Github, Gitlab, etc…)
Bom trabalho.
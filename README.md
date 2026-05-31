# Room Reservation API

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

Esta é uma API REST para o gerenciamento de reservas de salas, construída com o ecossistema Spring Boot.
O projeto foi estruturado com foco em confiabilidade e padronização, utilizando Docker para isolar a aplicação,
volumes para garantir a persistência dos dados do PostgreSQL, e um usuário sem privilégios de root para aumentar a segurança do contêiner. 
Além disso, a lógica de negócio é validada por testes unitários.

## Tecnologias Utilizadas

* **Java 21**
* **Spring Boot 4.x**
* **Spring Data JPA**
* **Apache Maven**
* **PostgreSQL 17**
* **Docker & Docker Compose**
* **Hibernate Validator**
* **JUnit 5 & Mockito**

## Diferenciais Arquiteturais (Práticas Sênior)

* **Multi-Stage Build no Dockerfile:**
 A imagem final de produção utiliza uma abordagem de múltiplas etapas.
 A compilação é feita em um estágio temporário com o JDK e Maven, enquanto a execução final ocorre em um ambiente JRE leve,
 reduzindo drasticamente o tamanho da imagem final e a superfície de ataque.
* **Princípio do Menor Privilégio:**
O contêiner de execução não roda como `root`. 
Foi criado um usuário de sistema dedicado (`spring`) com permissões estritas para executar a aplicação Java, mitigando riscos de segurança.
* **Isolamento de Rede:** 
Os serviços da API e do banco de dados comunicam-se através de uma rede privada virtual em ponte (`bridge network`), 
permitindo a comunicação interna segura via DNS do Docker.
* **Persistência de Dados:** 
Configuração de volumes gerenciados para o PostgreSQL, garantindo a resiliência e imutabilidade dos dados
mesmo após a destruição ou reinicialização dos contêineres.
* **Padrão DTO com Records:** 
Utilização do recurso `record` do Java para criar Data Transfer Objects imutáveis,
garantindo que entidades do banco de dados (`@Entity`) nunca sejam expostas diretamente nos endpoints.

## Regras de Negócio e Validações

O coração da aplicação reside nas proteções de integridade implementadas nas camadas de *Service* e *Repository*:

**Salas (Rooms)**
* **Unicidade:**
O sistema impede o cadastro de salas com nomes duplicados.
* **Soft Delete:**
A exclusão de uma sala não apaga o registro do banco de dados (evitando a quebra de histórico de reservas passadas). 
Em vez disso, o status da sala transita para `INATIVA`.

**Usuários (Users)**
* **Documento Real:** 
A API não faz apenas uma checagem de tamanho de string.
A validação de CPF é real, calculando os dígitos verificadores através da anotação `@CPF` do Hibernate Validator.
* **Unicidade e Imutabilidade:** 
Não é possível cadastrar CPFs duplicados, e o documento de identidade não pode ser alterado durante operações de atualização de usuário (PUT).

**Reservas (Reservations)**
* **Anti-Colisão de Horários (Overlapping):** 
A API possui uma verificação algorítmica no repositório (`hasOverlap` e `hasOverlapIgnoringId`) que
impede que uma mesma sala seja reservada no mesmo intervalo de tempo (data/hora), garantindo o bloqueio mesmo durante atualizações de reservas existentes.
* **Coerência de Tempo:** 
Uma reserva só é aceita se o `start_time` for rigorosamente anterior ao `end_time`.
* **Máquina de Estados:** 
O cancelamento e a conclusão de reservas obedecem a uma transição restrita, onde apenas reservas com status `ATIVA` podem ser movimentadas.

## Como Executar o Projeto
### Passo Inicial: Obtendo o Código
Independentemente da forma escolhida para rodar o projeto, o primeiro passo é clonar o repositório e entrar na pasta raiz:

Clone o repositório para sua máquina local:
   ```bash
   git clone https://github.com/seu-usuario/room-reservation-api.git
   ```

Navegue até a pasta raiz do projeto:
   ```bash
   cd room-reservation-api
   ```


### Opção 1: Execução Completa via Docker

### Pré-requisitos
* **Docker Desktop** instalado e em execução.

Execute o comando do Docker Compose para construir a imagem da API e subir a infraestrutura:
   ```bash
   docker compose up --build
   ```

A API estará disponível e pronta para receber requisições em http://localhost:8080.
O banco de dados PostgreSQL estará acessível externamente na porta 5432.

### Opção 2: Execução Local
Caso deseje rodar a aplicação diretamente na sua máquina (por exemplo, no IntelliJ IDEA) para visualizar o código em tempo de execução:

### Pré-requisitos
* **Java 21 (JDK)** configurado.

* **Apache Maven** instalado (ou uso do Maven Wrapper mvnw incluso no projeto).

* **Docker Desktop** (Apenas para subir o banco de dados isolado).

### Passo a passo:
Suba apenas o contêiner do banco de dados utilizando o Compose:
   ```bash
   docker compose up reservation-db -d
   ```

Na sua IDE, certifique-se de que o profile ativo é o `dev` (o Spring Boot tentará conectar no `localhost:5432` por padrão).

Execute a classe principal `RoomReservationApiApplication.java`.

### Como Executar os Testes

Para rodar a suíte de testes unitários e validar as regras de negócio da aplicação (sem a necessidade de subir o banco de dados), execute o comando abaixo na raiz do projeto:

```bash
./mvnw test
```

### Configuração de Variáveis de Ambiente
O `arquivo docker-compose.yml` gerencia e injeta as seguintes variáveis de ambiente necessárias para a inicialização do ecossistema:
| Variável | Descrição | Valor Padrão no Compose |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Define o perfil ativo do Spring | `dev` |
| `DB_HOST` | Host do banco de dados na rede Docker | `db` |
| `DB_USER` | Usuário de autenticação do PostgreSQL | `henrique` |
| `DB_PASS` | Senha de autenticação do PostgreSQL | `1234` |

### Endpoints da API
Todos os métodos de listagem (`GET`) suportam paginação nativa através da passagem de parâmetros na URL (`ex: ?page=0&size=10&sort=id`).

**Salas(`/api/v1/rooms`)**
  * `POST /` - Cadastra uma nova sala.
  * `GET /` - Lista todas as salas.
  * `GET /{id}` - Busca uma sala específica.
  * `PUT /{id}` - Atualiza nome ou capacidade de uma sala.
  * `DELETE /{id}` - Desativa uma sala (Soft Delete).

**Usuários(`/api/v1/users`)**
  * `POST /` - Cadastra um novo usuário.
  * `GET /` - Lista todos os usuários.
  * `GET /{id}` - Busca um usuário específico.
  * `PUT /{id}` - Atualiza dados do usuário (exceto CPF).
  * `DELETE /{id}` - Remove um usuário do sistema.

**Reservas(`/api/v1/reservations`)**
  * `POST /` - Solicita a reserva de uma sala.
  * `GET /` - Lista todas as reservas.
  * `GET /{id}` - Busca os detalhes de uma reserva específica.
  * `PUT /{id}` - Atualiza a sala ou o horário de uma reserva ativa.
  * `DELETE /{id}` - Cancela uma reserva ativa.

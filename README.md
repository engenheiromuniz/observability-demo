# observability-demo 
### André MUNIZ

Projeto de estudo prático de **observabilidade em aplicações Java/Spring Boot**, construído para consolidar conceitos de métricas, logs, tracing e monitoramento — do ponto de vista de quem desenvolve a aplicação, não de quem administra a ferramenta de monitoramento.

## Visão geral

A aplicação expõe métricas de negócio e de infraestrutura (JVM) através do **Spring Boot Actuator** e do **Micrometer**, coletadas por um **Prometheus** e visualizadas em dashboards no **Grafana**. Além disso, produz **logs estruturados em JSON** com propagação de **Correlation ID**, permitindo rastrear todas as linhas de log pertencentes a uma mesma requisição.

## Arquitetura

```mermaid
flowchart LR
    A[Spring Boot App<br/>Micrometer + Logback] -->|expõe /actuator/prometheus| B[Prometheus<br/>coleta via scrape]
    B -->|armazena série temporal| C[Grafana<br/>dashboards]
    A -->|logs JSON com correlationId| D[Console / futura stack de logs]
```

- **Aplicação**: expõe métricas e produz logs estruturados; não coleta nem armazena nada sozinha.
- **Prometheus**: periodicamente "puxa" (*scrape*) os dados do endpoint da aplicação e os guarda como série temporal.
- **Grafana**: lê o Prometheus como fonte de dados e monta os dashboards visuais.
- **Logback**: formata cada linha de log como JSON, incluindo o `correlationId` da requisição.

## Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem / Framework | Java 17+, Spring Boot 3.3 |
| Instrumentação (métricas) | Spring Boot Actuator, Micrometer |
| Logging estruturado | Logback + Logstash Encoder (JSON) |
| Coleta de métricas | Prometheus |
| Visualização | Grafana |
| Empacotamento da infra | Docker Compose |

## Como rodar

**Pré-requisitos:** Java 17+, Maven, Docker Desktop.

**1. Subir a aplicação:**
```bash
mvn org.springframework.boot:spring-boot-maven-plugin:3.3.4:run
```
Aplicação disponível em `http://localhost:8080`.

**2. Subir Prometheus + Grafana:**
```bash
cd observability-stack
docker compose up -d
```

| Serviço | URL | Credenciais |
|---|---|---|
| Aplicação | http://localhost:8080 | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / admin |

## Endpoints de negócio

| Endpoint | Descrição | Métrica gerada |
|---|---|---|
| `GET /hello?nome=Andre` | Saudação simples | `app.hello.requests` (Counter) |
| `GET /slow` | Simula operação lenta | `app.slow.duration` (Timer) |

Todo endpoint produz logs estruturados em JSON, com um `correlationId` único por requisição (reaproveitado do header `X-Correlation-Id` se o cliente enviar, ou gerado automaticamente).

## Endpoints de observabilidade (Actuator)

| Endpoint | Descrição |
|---|---|
| `/actuator/health` | Status de saúde da aplicação, com detalhes de componentes |
| `/actuator/metrics` | Lista de todas as métricas disponíveis |
| `/actuator/prometheus` | Métricas no formato que o Prometheus consome |

## Estrutura do projeto

```
observability-demo/
├── src/main/java/com/amztec/observabilitydemo/
│   ├── ObservabilityDemoApplication.java
│   ├── DemoController.java          # endpoints + métricas customizadas
│   └── CorrelationIdFilter.java     # geração/propagação do correlation ID
├── src/main/resources/
│   ├── application.properties        # configuração do Actuator/Micrometer
│   └── logback-spring.xml            # configuração de logs em JSON
├── observability-stack/
│   ├── docker-compose.yml            # Prometheus + Grafana
│   └── prometheus.yml                # configuração de scrape
├── pom.xml
└── README.md
```

## Conceitos demonstrados

- **Instrumentação de código**: como uma aplicação expõe dados para ferramentas externas, via métricas customizadas (`Counter`, `Timer`) e automáticas (JVM, heap, threads).
- **Modelo *pull* de coleta**: o Prometheus inicia a coleta, puxando dados periodicamente — diferente do modelo *push* (explorado em paralelo num laboratório com Zabbix Agent).
- **PromQL**: consulta a séries temporais coletadas (ex: `process_uptime_seconds`).
- **Logging estruturado (JSON)**: logs pensados para serem lidos por máquina/ferramenta de busca, não só por humano.
- **Correlation ID**: rastreamento de todas as linhas de log de uma mesma requisição através de um identificador único propagado via MDC (SLF4J).
- **Separação de responsabilidades**: a aplicação só expõe dado; quem decide o que alertar e como visualizar fica nas ferramentas de observabilidade.

## Roadmap do estudo

- [x] Instrumentação com Actuator + Micrometer
- [x] Integração com Prometheus + Grafana
- [x] Logging estruturado (JSON) e Correlation ID
- [ ] Health checks customizados (liveness/readiness)
- [ ] Distributed tracing com OpenTelemetry
- [ ] Vocabulário e práticas de SRE (SLI/SLO/SLA)

## Autor

André Muniz — projeto de estudo para aprofundamento em observabilidade e preparação para entrevistas técnicas na área de desenvolvimento Java.
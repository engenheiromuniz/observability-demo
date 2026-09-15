# observability-demo

Projeto de estudo para entender, na prática, como uma aplicação Spring Boot
expõe dados de observabilidade (métricas) para ferramentas externas
consumirem — o mesmo papel que os "itens" cumprem no Zabbix, só que aqui
quem produz o dado é a própria aplicação, via [Micrometer](https://micrometer.io/).

## Como rodar

Pré-requisitos: Java 17+ e Maven instalados.

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Endpoints de negócio (geram métricas customizadas)

- `GET /hello?nome=Andre` — incrementa um contador (`app.hello.requests`)
- `GET /slow` — simula uma operação lenta e registra a duração (`app.slow.duration`)

Chame esses endpoints algumas vezes antes de olhar as métricas, para ter
dado de verdade para ver.

## Endpoints de observabilidade (Actuator)

- `GET /actuator/health` — status de saúde da aplicação (o "está vivo?")
- `GET /actuator/metrics` — lista todas as métricas disponíveis
- `GET /actuator/metrics/app.hello.requests` — detalhe de uma métrica específica
- `GET /actuator/prometheus` — todas as métricas no formato que Prometheus/Grafana leem

## Mapa mental: Zabbix vs. Spring Boot Actuator

| Conceito Zabbix | Equivalente aqui |
|---|---|
| Item | Métrica registrada no `MeterRegistry` |
| Agente coletando e enviando dado | Endpoint `/actuator/prometheus` sendo "raspado" (scrape) por uma ferramenta |
| Trigger | Regra de alerta configurada na ferramenta de monitoramento (fora da aplicação) |
| Template | Conjunto de métricas padrão que o Actuator já expõe de graça (JVM, memória, threads, etc.) sem você escrever nada |

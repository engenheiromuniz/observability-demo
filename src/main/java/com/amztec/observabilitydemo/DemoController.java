package com.amztec.observabilitydemo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    // Um Counter é uma métrica que só sobe (ex: total de requisições).
    // Equivalente conceitual a um "item" no Zabbix, só que quem cria e
    // atualiza o valor é a própria aplicação, não um agente externo.
    private final Counter helloCounter;

    // Um Timer mede duração + contagem de chamadas ao mesmo tempo,
    // ótimo para "quanto tempo esse endpoint está demorando".
    private final Timer slowTimer;

    public DemoController(MeterRegistry registry) {
        this.helloCounter = Counter.builder("app.hello.requests")
                .description("Total de chamadas ao endpoint /hello")
                .register(registry);

        this.slowTimer = Timer.builder("app.slow.duration")
                .description("Duração das chamadas ao endpoint /slow")
                .register(registry);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "mundo") String nome) {
        helloCounter.increment();
        return "Olá, " + nome + "!";
    }

    @GetMapping("/slow")
    public String slow() throws InterruptedException {
        // Simula uma operação lenta (ex: chamada a um banco de dados)
        // para termos algo interessante para ver no /actuator/metrics.
        return slowTimer.record(() -> {
            try {
                Thread.sleep((long) (Math.random() * 500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Operação concluída";
        });
    }
}

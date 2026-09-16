package com.amztec.observabilitydemo;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Simula a verificação de uma dependência externa (ex: um serviço de
 * pagamento, uma API de terceiros). Em um projeto real, aqui dentro
 * você faria uma chamada de verdade (ex: um ping HTTP) e reportaria o
 * resultado real.
 *
 * Esse health indicator aparece automaticamente dentro de
 * /actuator/health, na seção "components", com o nome "paymentService"
 * (derivado do nome da classe, sem o sufixo "HealthIndicator").
 */
@Component
public class PaymentServiceHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean paymentServiceDisponivel = checarServicoDePagamento();

        if (paymentServiceDisponivel) {
            return Health.up()
                    .withDetail("mensagem", "Serviço de pagamento respondendo normalmente")
                    .build();
        }

        return Health.down()
                .withDetail("mensagem", "Serviço de pagamento não respondeu a tempo")
                .build();
    }

    private boolean checarServicoDePagamento() {
        // Simulação: em produção, aqui entraria uma chamada HTTP real
        // (ex: RestClient.get("https://pagamentos.exemplo.com/health")).
        // Para fins de estudo, alternamos o resultado aleatoriamente.
        return Math.random() > 0.3;
    }
}
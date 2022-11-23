package com.example.workflow;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RabbitConfiguration {
    /**
     * Der Name des Arbeitsmoduls Direktschalter
     */
    public static final String WORKING_EXCHANGE = "working_direct_exchange5";

    /**
     * demoDer Warteschlangenname des Unternehmens
     */
    public static final String WORKING_DEMO_QUEUE = "working_demo_queue5";

    /**
     * demoDer Routenschlüssel des Unternehmens
     */
    public static final String WORKING_DEMO_ROUTINGKEY = "working_demo_key5";

    /**
     * Service-Switch (ein Service-Switch pro Projekt)
     * 1.Direkten Austausch definieren, queueTest binden
     * 2.durable="true" Rabbitmq muss beim Neustart keinen neuen Switch erstellen
     * 3.Der direkte Switch ist relativ einfach, und die Übereinstimmungsregel lautet: Wenn der Routing-Schlüssel übereinstimmt, wird die Nachricht an die entsprechende Warteschlange gesendet
     * Es gibt kein Konzept der Weiterleitung von Schlüsseln in der Fanout-Exchange, die Nachrichten an alle Warteschlangen sendet, die an diese Exchange gebunden sind.
     * Topic Exchange: Sie verwenden das Prinzip der Fuzzy-Matching-Routing-Schlüssel, um Nachrichten an die Warteschlange weiterzuleiten
     */
    @Bean
    public DirectExchange workingExchange() {
        return new DirectExchange(WORKING_EXCHANGE, true, false);
    }

    /**
     * Erstellen einer Warteschlange (eine Warteschlange für einen Service, ein Routenschlüssel-Benennungsformat, Projektname - Servicename)
     * 1. Name der Warteschlange
     * 2.durable="true" Persistenz RabbitMQ muss beim Neustart keine neue Warteschlange erstellen
     * 3.exclusive gibt an, ob die Nachrichtenwarteschlange nur in der aktuellen Verbindung aktiv ist und der Standardwert false ist
     * 4.Auto-delete bedeutet, dass die Nachrichtenwarteschlange automatisch gelöscht wird, wenn sie nicht verwendet wird Der Standardwert ist false
     * 5. Für nack- oder Sendetimeouts an die Warteschlange für unzustellbare Nachrichten ist args an die Warteschlange für unzustellbare Nachrichten gebunden
     */
    @Bean
    public Queue workingDemoQueue() {
        return QueueBuilder.durable(WORKING_DEMO_QUEUE)
                .withArgument(RETRY_LETTER_QUEUE_KEY, WORKING_RETRY_EXCHANGE_NAME)
                .withArgument(RETRY_LETTER_ROUTING_KEY, WORKING_DEMO_RETRY_ROUTING_KEY)
                .build();
    }

    /**
     * Der Switch ist an einen Routenschlüssel gebunden
     */
    @Bean
    public Binding workingDemoBinding() {
        return BindingBuilder.bind(workingDemoQueue()).to(workingExchange())
                .with(WORKING_DEMO_ROUTINGKEY);
    }

    /**
     * Verzögerungswarteschlange Switch-Konfigurations-ID (fest)
     */
    public static final String RETRY_LETTER_QUEUE_KEY = "x-dead-letter-exchange";

    /**
     * Verzögerungs-Warteschlangenswitch-Konfigurationsschlüssel-ID (fest)
     */
    public static final String RETRY_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    /**
     * Konfigurationstimeout-Enumeration für verzögerte Warteschlangennachrichten (behoben)
     */
    public static final String RETRY_MESSAGE_TTL = "x-message-ttl";

    /**
     * workinModulverzögerungs-Warteschlangenschalter
     */
    public final static String WORKING_RETRY_EXCHANGE_NAME = "working_retry_exchange5";

    /**
     * workingModul-DEMO-Serviceverzögerungswarteschlange
     */
    public final static String WORKING_DEMO_RETRY_QUEUE_NAME = "working_demo_retry_queue5";

    /**
     * workingModul DEMO Verzögerungswarteschlangen-Routenschlüssel
     */
    public final static String WORKING_DEMO_RETRY_ROUTING_KEY = "working_demo_retry_key5";

    /**
     * Warteschlangenwechsel verzögern
     *
     * @return
     */
    @Bean
    public DirectExchange workingRetryExchange() {
        return new DirectExchange(WORKING_RETRY_EXCHANGE_NAME, true, false);
    }

    /**
     * Erstellen einer Verzögerungswarteschlange Eine Dienstwarteschlange erfordert eine Verzögerungswarteschlange
     *
     * @return
     */
    @Bean
    public Queue workingDemoRetryQueue() {
        Map<String, Object> args = new ConcurrentHashMap<>(3);
        // Erneutes Bereitstellen der Nachricht an Exchange for Business Switch
        args.put(RETRY_LETTER_QUEUE_KEY, WORKING_EXCHANGE);
        args.put(RETRY_LETTER_ROUTING_KEY, WORKING_DEMO_ROUTINGKEY);
        // Die Nachricht wird in der Warteschlange für 3s verzögert und tritt dann zu einer Zeitüberschreitung auf, und die Nachricht wird erneut an die Warteschlange zugestellt, die dem x-dead-letter-exchange entspricht, und der Routingschlüssel gibt sich selbst an
        args.put(RETRY_MESSAGE_TTL, 3 * 2000);
        return new Queue(WORKING_DEMO_RETRY_QUEUE_NAME, true, false, false, args);
    }

    /**
     * Binden Sie die oben definierte Beziehung
     */
    @Bean
    public Binding retryDirectBinding() {
        return BindingBuilder.bind(workingDemoRetryQueue()).to(workingRetryExchange()).with(WORKING_DEMO_RETRY_ROUTING_KEY);
    }

    /**
     * Warteschlange für unfähige Nachrichten
     */
    public final static String FAIL_QUEUE_NAME = "fail_queue5";

    /**
     * Totbuchstaben-Schalter
     */
    public final static String FAIL_EXCHANGE_NAME = "fail_exchange5";

    /**
     * Weiterleiten unzustellbarer Buchstaben
     */
    public final static String FAIL_ROUTING_KEY = "fail_routing5";

    /**
     * Erstellen einer Konfigurationswarteschlange für unzustellbare Nachrichten
     */
    @Bean
    public Queue deadQueue() {
        return new Queue(FAIL_QUEUE_NAME, true, false, false);
    }

    /**
     * Totbuchstaben-Schalter
     *
     * @return
     */
    @Bean
    public DirectExchange deadExchange() {
        return new DirectExchange(FAIL_EXCHANGE_NAME, true, false);
    }

    /**
     * Einbände
     *
     * @return
     */
    @Bean
    public Binding failBinding() {
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with(FAIL_ROUTING_KEY);
    }

    @Bean
    public MessagePostProcessor correlationIdProcessor() {
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message, Correlation correlation) {
                MessageProperties messageProperties = message.getMessageProperties();
                if (correlation instanceof CorrelationData) {
                    String correlationId = ((CorrelationData) correlation).getId();
                    messageProperties.setCorrelationId(correlationId);
                }
                return message;
            }

            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                return message;
            }
        };
        return messagePostProcessor;
    }
}


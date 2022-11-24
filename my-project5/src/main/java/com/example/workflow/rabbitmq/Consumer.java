package com.example.workflow.rabbitmq;

import com.rabbitmq.client.Channel;
import com.example.workflow.RabbitConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    MessagePostProcessor correlationIdProcessor;

    @RabbitListener(queues = RabbitConfiguration.WORKING_DEMO_QUEUE)
    protected void consumer(Message message, Channel channel) {
        MessageProperties messageProperties = message.getMessageProperties();
        String correlationId = messageProperties.getCorrelationId();
        try {
            logger.info("================================");
            logger.info("Beginnen Sie mit der Verarbeitung der Nachricht: " + correlationId);
            long result = System.currentTimeMillis() / Integer.parseInt(new String(message.getBody()));
            channel.basicAck(messageProperties.getDeliveryTag(), false);
            logger.info("Verarbeiten der Nachrichtenergebnisse: " + result);
            logger.info("Die Nachricht wurde erfolgreich verarbeitet: " + correlationId);
        } catch (Exception e) {
        	System.out.println("info: " + message);
            String correlationData = (String) messageProperties.getHeaders().get("spring_listener_return_correlation"); // spring_returned_message_correlation
            message.getMessageProperties().setHeader("x-last-fail-reason", "Exception:[" + e.getMessage() + "]");
            logger.error("Nachricht konnte nicht verarbeitet werden: [" + e.getMessage() + "], Ursprüngliche Nachricht: [" + new String(message.getBody()) + "] correlationId:" + correlationId);
            long retryCount = getRetryCount(messageProperties);
            try {
                if (retryCount <= 3) {
                    // Die Anzahl der Wiederholungen beträgt weniger als das 3-fache,NACK REQUEUE FALSE Wechseln zur Warteschlange für verzögerte Wiederholungen und Rückkehr zur Arbeitswarteschlange nach dem Timeout
                    logger.info("NACK Nachrichten starten tag:" + messageProperties.getDeliveryTag() + " retryCount: " + retryCount);
                    channel.basicNack(messageProperties.getDeliveryTag(), false, false);
                } else {
                    // Wenn die Anzahl der Wiederholungsversuche das Dreifache überschreitet, wird die Nachricht an die Fehlerwarteschlange gesendet, um auf die Verarbeitung durch einen bestimmten Verbraucher oder manuell zu warten.
                    channel.basicAck(messageProperties.getDeliveryTag(), false);
                    rabbitTemplate.convertAndSend(
                            RabbitConfiguration.FAIL_EXCHANGE_NAME,
                            RabbitConfiguration.FAIL_ROUTING_KEY,
                            message,
                            correlationIdProcessor,
                            new CorrelationData(correlationId)
                    );
                    logger.info("Wenn es dreimal hintereinander fehlschlägt, senden Sie die Nachricht an die Warteschlange für unzustellbare Nachrichten und senden Sie die Nachricht: " + new String(message.getBody()));
                }
            } catch (Exception ee) {
                // TODO Datei- oder Datenbank-Fallbackschema
                logger.error("Senden einer Exception für unzustellbare Buchstaben: " + ee.getMessage() + ",Ursprüngliche Nachricht: " + new String(message.getBody()), ee);
            }
        }
        
        
    }
    
    
    /**
     * Ruft ab, wie oft die Nachricht wiederholt wurde
     */
    public long getRetryCount(MessageProperties messageProperties) {
        if (null != messageProperties) {
            List<Map<String, ?>> deaths = messageProperties.getXDeathHeader();
            if (deaths != null && deaths.size() > 0) {
                return (Long) deaths.get(0).get("count");
            }
        }
        return 0L;
    }
}

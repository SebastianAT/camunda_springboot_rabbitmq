import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

//@Slf4j
@Component
public class ConfirmCallbackService implements RabbitTemplate.ConfirmCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;


    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);            //bestimmen ConfirmCallback
    }


    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
       // log.info("Senden von Nachrichten{} correlationData={} ,ack={}, cause={}", ack ? "success" : "error", correlationData.getId(), ack, cause);
    }
}

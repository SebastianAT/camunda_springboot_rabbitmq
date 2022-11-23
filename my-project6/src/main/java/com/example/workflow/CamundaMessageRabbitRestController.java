package com.example.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;
import java.util.UUID;

@RestController
public class CamundaMessageRabbitRestController{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //@Autowired
    //private Receiver receiver;

    //@Autowired
    //private ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    MessagePostProcessor correlationIdProcessor;
    
    @RequestMapping(value = "/rabbit/message",
            method = RequestMethod.POST,
            produces = "application/json")
    String addMessage(@RequestBody String message){
       
    	logger.info("create random uuid for correlation");
        CorrelationData data = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(RabbitConfiguration.WORKING_EXCHANGE, RabbitConfiguration.WORKING_DEMO_ROUTINGKEY,
                message, correlationIdProcessor, data);
       
        return "OK,addMessage:" + message;
        //return new ResponseEntity(HttpStatus.ACCEPTED);
    }
}

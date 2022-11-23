package com.example.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.workflow.rabbitmq.CamundaMessageExchange;
import com.example.workflow.rabbitmq.Receiver;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class CamundaMessageRabbitRestController{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Receiver receiver;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/rabbit/message",
            method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity addMessage(@RequestBody String message){
        try {
            // @TODO Add better error handling for covertValue method as the true error is not bubbled.
            // Will likely require abstraction replacement of the validation functions of MessageRestServiceImpl
            /*CorrelationMessageDto dto = objectMapper.readValue(message, CorrelationMessageDto.class);
System.out.println("Save msg in queue msg: " + message + " corrMsg: " + dto.toString());*/
        }catch(Exception e){
            throw new HttpMessageNotReadableException(e.getMessage());
        }

        rabbitTemplate.convertAndSend(CamundaMessageExchange.topicExchangeName, "camunda.bpmn.message3", message);
        /*try {
			receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }
}

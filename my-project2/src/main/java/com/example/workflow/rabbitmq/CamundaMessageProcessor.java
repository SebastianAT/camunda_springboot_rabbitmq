package com.example.workflow.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.impl.MessageRestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service
public class CamundaMessageProcessor{

    @Autowired
    private ProcessEngine engine;

    @Autowired
    private ObjectMapper objectMapper;

    public Response processMessage(String message){
        CorrelationMessageDto messageDto = null;
		try {
			messageDto = objectMapper.readValue(message, CorrelationMessageDto.class);
			System.out.println("MESSAGE: " + message);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			System.out.println("Error json: ");
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.out.println("Error json: ");
			e.printStackTrace();
		}
        MessageRestServiceImpl service = new MessageRestServiceImpl(engine.getName(), objectMapper);
        System.out.println("Take out msg from queue ...");
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	Response response = service.deliverMessage(messageDto);
        System.out.println("Response deliverMessage " + response.toString());
        return response;
    }
}

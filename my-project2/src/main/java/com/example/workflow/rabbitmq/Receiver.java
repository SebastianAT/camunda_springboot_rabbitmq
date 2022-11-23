package com.example.workflow.rabbitmq;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;

@Component
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    CamundaMessageProcessor messageProcessor;

    public void receiveMessage(String message) {
        try {
            Response response = messageProcessor.processMessage(message);
        }catch(Exception e){
            // @TODO: Fix up error catching with the service
            /*if(e.getMessage().contains("MismatchingMessageCorrelationException")) {
        		System.out.println("CONTAINS MismatchingCorre TRUE - TRY RESENDING MESSAGE \n" + message);
        		
        		final String REST_ENDPOINT = "http://localhost:8092/rabbit/message";
        		HttpEntity<String> entity2;
                
        		System.out.println("\033[32;1mSend msg to: \033[0m"+ REST_ENDPOINT);

        		HttpHeaders headers = new HttpHeaders();
        		headers.setContentType(MediaType.APPLICATION_JSON);
        		
        		entity2 = new HttpEntity<String>(message, headers);
        		
        		ResponseEntity<Object> result = null;
    			try {
    				result = new RestTemplate().postForEntity(REST_ENDPOINT, entity2, null);
    			} catch(Exception e2) {
    				System.out.println("\n\033[33;1mERROR send out1\033[0m\n");
    			}
        		
        		
        		
        	} else {
        		System.out.println("ERROoooR: " + e.getMessage());
        	}*/
        }finally{
            latch.countDown();
        }

    }

    public CountDownLatch getLatch() {
        return latch;
    }
}

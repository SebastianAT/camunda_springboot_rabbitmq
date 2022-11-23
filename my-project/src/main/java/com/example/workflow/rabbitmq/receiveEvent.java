package com.example.workflow.rabbitmq;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class receiveEvent implements JavaDelegate {
	
	@Autowired
    CamundaMessageProcessor messageProcessor;
	
	public void receiveMessage(String message) {
        try {
          //  Response response = messageProcessor.processMessage(message);
        	System.out.println("receiveEvent class in receiveMessage method");
        }catch(Exception e){
            // @TODO: Fix up error catching with the service
            System.out.println("ERROR: " + e.getMessage());
        }

    }
	
	public void receiveMessage2(String message) {
        try {
          //  Response response = messageProcessor.processMessage(message);
        	System.out.println("receiveEvent class in receiveMessage method executed with call");
        }catch(Exception e){
            // @TODO: Fix up error catching with the service
            System.out.println("ERROR: " + e.getMessage());
        }

    }

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub
		receiveMessage2("ghg");
		
		
	}

}

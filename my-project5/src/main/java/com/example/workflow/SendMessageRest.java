package com.example.workflow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component()
public class SendMessageRest implements JavaDelegate {
	
	/*@Autowired
	final RuntimeService runtimeService;

    public SendMessageRest(RuntimeService runtimeService){
        this.runtimeService= runtimeService;
    }*/
    
	private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageRest.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String correlationKey = (String) execution.getVariable("correlationKey");
        String messageName = "msg_" + execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getKey();
        BpmnModelInstance modelInstance = execution.getBpmnModelInstance();
        FlowNode sendTask = modelInstance.getModelElementById(execution.getCurrentActivityId());
        String receiveID = sendTask.getDocumentations().iterator().next().getTextContent();
        
        System.out.println("receiveID: " + receiveID);
        // String messageID = (String) execution.getVariable("messageName");
        String targetParticipant = execution.getCurrentActivityName().substring(execution.getCurrentActivityName().indexOf("Send(")+5, execution.getCurrentActivityName().indexOf(")"));
       // {c} P3
        boolean decisionVariable = false;
        String decision = "";
        if(targetParticipant.contains("{") && targetParticipant.contains("}")) {
        	decisionVariable = true;
        	decision = targetParticipant.substring(targetParticipant.indexOf("{")+1, targetParticipant.indexOf("}"));
        	targetParticipant = targetParticipant.substring(targetParticipant.lastIndexOf("}")+2);
        }
        
        messageName = "msg_" + receiveID;
        
        DateTime time = new DateTime();
        System.out.println("\n\033[33;1mSending out message\033[0m " + time.getHourOfDay() + ":" + time.getMinuteOfHour() + ":" + time.getSecondOfMinute());  
        System.out.println("\033[32;1mmessageName: \033[0m"+ messageName + " \033[32;1mtargetParticipant: \033[0m" + targetParticipant);
        
        /*{
        	"messageName":"msg_Process_1kaxj2c_P1",
        	"correlationKeys":{
        			"correlationKey":{
        				"value":"correlation0.0331547946214632"
        			}
        	}
        }*/
        HttpEntity<String> entity2;
        
		JSONObject obj = new JSONObject();
		JSONObject var = new JSONObject();
		JSONObject cKey = new JSONObject();
		
		JSONObject decisionJson = new JSONObject();
		/*"processVariables" : {
		"aVariable" : {"value" : "aNewValue"}
	  }*/
		
		if(decision.length() == 0 && !decisionVariable) {
			obj.put("messageName", messageName);
		} else {
			obj.put("messageName", messageName);
			decisionJson.put("value", execution.getVariable(decision).toString());
			var.put(decision, decisionJson);
			obj.put("processVariables", var);
		}
		//cKey.put("value", correlationKey);
		//var.put("correlationKey", cKey);
		
		//obj.put("correlationKeys", var);
        
		System.out.println(obj.toString());
		
		String businessKey = execution.getProcessBusinessKey();
		System.out.println("\033[32;1mBusinessKey:\033[0m "+ businessKey + "\n");
		
		// find out who is target e.g P1, P2 ...
		// find port => P1 = 8081, P2 = 8082, P3 = 8083 ...
		//String targetParticipant = messageID.substring(messageID.indexOf("(")+1, messageID.indexOf(")"));
		
		String host = "";
		String port = "8091";
		if(targetParticipant.equals("P1")) {
			port = "8091";
		} else if(targetParticipant.equals("P2")) {
			port = "8092";
		} else if(targetParticipant.equals("P3")) {
			port = "8093";
		} else if(targetParticipant.equals("P4")) {
			port = "8094";
		} else if(targetParticipant.equals("P5")) {
			port = "8095";
		} else if(targetParticipant.equals("P6")) {
			port = "8096";
		}
			boolean gotIt = false;
			try {
				InetAddress.getByName(targetParticipant);
				gotIt = true;
			} catch (UnknownHostException e) {
				// just don't die
			}
			if (gotIt) {
				host = targetParticipant;
			} else {
				host = "localhost";
			}
			
		//host = "192.168.1.116";
		final String REST_ENDPOINT = "http://" + host + ":" + port + "/rabbit/message";  ///rest/message
		
		System.out.println("\033[32;1mSend msg to: \033[0m"+ targetParticipant + " \033[32;1mwith port: \033[0m" + port + " " + REST_ENDPOINT);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		entity2 = new HttpEntity<String>(obj.toString(), headers);
		
		/*HttpEntity<RestMessageDto> entity = new HttpEntity<RestMessageDto>(//
				new RestMessageDto(messageName, businessKey), // here is the JSON body
				headers);*/
		boolean successful = false;
		while(!successful) {
			ResponseEntity<Object> result = null;
			try {
				result = new RestTemplate().postForEntity(REST_ENDPOINT, entity2, null);
			} catch(Exception e) {
				System.out.println("\n\033[33;1mERROR send out1\033[0m\n");
			}
			if(result != null) {
				if (!result.getStatusCode().is2xxSuccessful()) {
					Thread.sleep(2000);
					System.out.println("\n\033[33;1mERROR send out2\033[0m\n");
					//throw new RuntimeException(result.getStatusCode().toString() + ": " + result.getBody());
					
				} else {
					successful = true;
					System.out.println("\n\033[33;1mSending out message done\033[0m\n");
				}
			} else {
				Thread.sleep(2000);
				System.out.println("\n\033[33;1mERROR send out3\033[0m\n");
			}
			
			
		}
		
	}
}

package com.example.workflow.rabbitmq;

import com.rabbitmq.client.Channel;
import com.example.workflow.RabbitConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.impl.MessageRestServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    MessagePostProcessor correlationIdProcessor;
    @Autowired
    private ProcessEngine camunda;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @RabbitListener(queues = RabbitConfiguration.WORKING_DEMO_QUEUE)
    //@Transactional  
    /*public void dummyShipGoodsCommand(String orderId) {
      // and call back directly with a generated transactionId
      handleGoodsShippedEvent(orderId, "gfgggd-fhfhdhdfh-ghgf-f");
    }

    public void handleGoodsShippedEvent(String orderId, String shipmentId) {
      camunda.getRuntimeService().createMessageCorrelation("msg_task120") //
          .correlateWithResult();
    }*/
    protected void consumer(Message message, Channel channel) {
    	/*MessageProperties messageProperties = message.getMessageProperties();
    	String msg2 = new String(message.getBody());
    	System.out.println("msgNEU: " + msg2);
    	String msg = msg2.substring(msg2.indexOf("msg_"), msg2.lastIndexOf("\"") );
      	System.out.println("sub msg: " + msg);
    	camunda.getRuntimeService().createMessageCorrelation(msg) //
        .correlateWithResult();
    	try {
			channel.basicAck(messageProperties.getDeliveryTag(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    	
        MessageProperties messageProperties = message.getMessageProperties();
        String correlationId = messageProperties.getCorrelationId();
        try {
            logger.info("================================");
            logger.info("Beginnen Sie mit der Verarbeitung der Nachricht: " + correlationId + " " + new String(message.getBody()));
            logger.info("Vor result");
            logger.info("Vor systemTime: " + System.currentTimeMillis());
           // logger.info("Vor parseInt msg: " + Integer.parseInt(new String(message.getBody())));
            //logger.info("on calc: " + (System.currentTimeMillis() / Integer.parseInt(new String(message.getBody()))) );
            long result2 = System.currentTimeMillis() / Integer.parseInt(new String(message.getBody()));
            logger.info("Nach result");
            logger.info("Vor basicACK");
            
            
            final String REST_ENDPOINT = "http://localhost:8096/engine-rest/message";
    		HttpEntity<String> entity2;
            
    		System.out.println("\033[32;1mSend msg to: \033[0m"+ REST_ENDPOINT + " msg: " + new String(message.getBody()));

    		HttpHeaders headers = new HttpHeaders();
    		headers.setContentType(MediaType.APPLICATION_JSON);
    		
    		entity2 = new HttpEntity<String>(new String(message.getBody()), headers);
    		
    		ResponseEntity<Object> result = null;

			result = new RestTemplate().postForEntity(REST_ENDPOINT, entity2, null);
		
            
            channel.basicAck(messageProperties.getDeliveryTag(), false);
            
            
            /*CorrelationMessageDto messageDto = null;
    		try {
    			System.out.println("MESSAGE: " + message);
    			messageDto = objectMapper.readValue(message.getBody(), CorrelationMessageDto.class);
    			
    			
    			
    		} catch (JsonMappingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JsonProcessingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            MessageRestServiceImpl service = new MessageRestServiceImpl(camunda.getName(), objectMapper);
            System.out.println("Take out msg from queue ...");
            Response response = service.deliverMessage(messageDto);
            System.out.println("response status: " + response.getStatusInfo());*/
            
            
            /*channel.basicConsume(RabbitConfiguration.WORKING_DEMO_QUEUE, true, (consumerTag, message2) -> {
            	String m = new String(message2.getBody(), "UTF-8");
            	System.out.println("received msg = " + m);
            }, consumerTag -> {});*/
            logger.info("Nach basicACK");
            logger.info("Verarbeiten der Nachrichtenergebnisse: " + result2);
            logger.info("Die Nachricht wurde erfolgreich verarbeitet: " + correlationId);
        } catch (Exception e) {
        	System.out.println("\nexception: " + e.getMessage() + "\n");
        	System.out.println("\ninfo: " + message + "\n");
            String correlationData = (String) messageProperties.getHeaders().get("spring_returned_message_correlation"); //  spring_listener_return_correlation
            message.getMessageProperties().setHeader("x-last-fail-reason", "Exception:[" + e.getMessage() + "]");
            logger.error("Nachricht konnte nicht verarbeitet werden: [" + e.getMessage() + "], Ursprüngliche Nachricht: [" + new String(message.getBody()) + "] correlationId:" + correlationId);
            long retryCount = getRetryCount(messageProperties);
            try {
                if (retryCount <= 3) {
                	final String REST_ENDPOINT = "http://localhost:8096/engine-rest/message";
            		HttpEntity<String> entity2;
                    
            		System.out.println("\033[32;1mSend msg to: \033[0m"+ REST_ENDPOINT + " msg: " + new String(message.getBody()));

            		HttpHeaders headers = new HttpHeaders();
            		headers.setContentType(MediaType.APPLICATION_JSON);
            		
            		entity2 = new HttpEntity<String>(new String(message.getBody()), headers);
            		
            		ResponseEntity<Object> result = null;
        			
            		try {
            			result = new RestTemplate().postForEntity(REST_ENDPOINT, entity2, null);
            			channel.basicAck(messageProperties.getDeliveryTag(), false);
            		} catch(Exception eee) {
            			// Die Anzahl der Wiederholungen beträgt weniger als das 3-fache,NACK REQUEUE FALSE Wechseln zur Warteschlange für verzögerte Wiederholungen und Rückkehr zur Arbeitswarteschlange nach dem Timeout
                        logger.info("NACK Nachrichten starten tag:" + messageProperties.getDeliveryTag() + " retryCount: " + retryCount);
                        channel.basicNack(messageProperties.getDeliveryTag(), false, false);
            		}
                } else {
                    // Wenn die Anzahl der Wiederholungsversuche das Dreifache überschreitet, wird die Nachricht an die Fehlerwarteschlange gesendet, um auf die Verarbeitung durch einen bestimmten Verbraucher oder manuell zu warten.
                    channel.basicAck(messageProperties.getDeliveryTag(), false);
                    rabbitTemplate.convertAndSend(
                            RabbitConfiguration.FAIL_EXCHANGE_NAME,
                            RabbitConfiguration.FAIL_ROUTING_KEY,
                            message,
                            correlationIdProcessor,
                            new CorrelationData(correlationId)  //correlationData
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
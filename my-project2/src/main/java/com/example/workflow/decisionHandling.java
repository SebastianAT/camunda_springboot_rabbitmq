package com.example.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component()
public class decisionHandling implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub
		
		
		
		if(execution.getVariable("count") == null) {
			execution.setVariable("count", 0);
			execution.setVariable("c", "false");
			System.out.println("COUNT: " + (int) execution.getVariable("count"));
		} else if((int) execution.getVariable("count") < 20) {
			int count = (int) execution.getVariable("count");
			execution.setVariable("count", count+1);
			execution.setVariable("c", "false");
			System.out.println("COUNT: " + (int) execution.getVariable("count"));
		} else {
			execution.setVariable("c", "true");
			System.out.println("COUNT: REACHED set c=true");
		}
		


	}

}

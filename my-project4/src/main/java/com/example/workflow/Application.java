package com.example.workflow;

import org.camunda.bpm.engine.variable.Variables;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.camunda.community.migration.CamundaPlatform7AdapterConfig;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;

@SpringBootApplication
@EnableZeebeClient
@Import(CamundaPlatform7AdapterConfig.class)
@ZeebeDeployment(resources = "classpath:*.bpmn")
public class Application {

  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
  }

}
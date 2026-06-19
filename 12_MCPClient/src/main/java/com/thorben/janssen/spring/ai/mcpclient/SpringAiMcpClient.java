package com.thorben.janssen.spring.ai.mcpclient;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Push
@SpringBootApplication
public class SpringAiMcpClient implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiMcpClient.class, args);
	}

}

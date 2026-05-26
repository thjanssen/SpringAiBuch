package com.thorben.janssen.spring.ai.multimodel;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiApplication.class, args);
	}

}

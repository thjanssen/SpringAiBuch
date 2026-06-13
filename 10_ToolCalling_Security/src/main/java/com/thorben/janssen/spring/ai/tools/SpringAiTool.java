package com.thorben.janssen.spring.ai.tools;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Push
@SpringBootApplication
public class SpringAiTool implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiTool.class, args);
	}

}

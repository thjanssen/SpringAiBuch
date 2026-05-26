package com.thorben.janssen.spring.ai.prompting.ui;

import com.thorben.janssen.spring.ai.prompting.service.ChatService;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.springframework.ai.chat.client.ChatClient;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Route("")
public class MainView extends VerticalLayout implements RouterLayout {

    private List<VerticalLayout> tabContents = new ArrayList<>();


    MainView(ChatClient.Builder chatClientBuilder, ChatService chatService) {
        setSizeFull();
        setMargin(false);

        tabContents.add(createTabContent(chatService));

        Tab tab1 = new Tab("Chat1");
        Tab newTab = new Tab("+");
        Tabs tabs = new Tabs(tab1, newTab);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(newTab)) {
                Tab tab = new Tab("Chat "+(tabs.getSelectedIndex()+1));
                tabs.addTabAtIndex(tabs.getSelectedIndex(), tab);

                var content = createTabContent(chatService);
                tabContents.add(content);
                add(content);

                tabs.setSelectedTab(tab);
            }

            for (int i=0; i<tabContents.size(); i++) {
                tabContents.get(i).setVisible(tabs.getSelectedIndex()==i);
            }
        });

        add(tabs, tabContents.get(0));
    }

    private VerticalLayout createTabContent(ChatService chatService) {
        var conversationId = UUID.randomUUID();

        VerticalLayout messageList = new VerticalLayout();
        Scroller messageScroller = new Scroller(messageList);
        MessageInput messageInput = new MessageInput();
        VerticalLayout content1 = new VerticalLayout(messageScroller, messageInput);
        content1.setSizeFull();
        content1.setPadding(false);
        content1.setSpacing(false);

        setFlexGrow(1, content1);

        messageScroller.setSizeFull();
        messageInput.setWidthFull();

        messageInput.addSubmitListener(ev -> {

            // Add user input as markdown message
            messageList.add(new MarkdownMessage(ev.getValue(),"Me"));

            // Placeholder message for the upcoming AI reply
            MarkdownMessage reply = new MarkdownMessage("Assistant");
            messageList.add(reply);

            // Call ChatController to interact with LLM and stream back the reply to UI
            chatService.chat(ev.getValue()).subscribe(cr -> {
                getUI().orElseThrow().access(() -> {
                    reply.appendMarkdownAsync(cr);
                    reply.scrollIntoView();
                });
            });

        });

        return content1;
    }
}

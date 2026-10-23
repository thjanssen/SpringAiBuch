package com.thorben.janssen.spring.ai.agents.ui;

import com.thorben.janssen.spring.ai.agents.agent.ChatService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Route("")
public class MainView extends VerticalLayout implements RouterLayout {

    private List<VerticalLayout> tabContents = new ArrayList<>();


    MainView(ChatService chatService) {
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

            // Call the ChatAgent in the background and show its reply in the UI
            UI ui = UI.getCurrent();
            CompletableFuture.supplyAsync(() -> chatService.chat(ev.getValue(), conversationId))
                    .exceptionally(e -> "Es ist ein Fehler aufgetreten: " + e.getMessage())
                    .thenAccept(answer -> ui.access(() -> {
                        reply.appendMarkdownAsync(answer);
                        reply.scrollIntoView();
                    }));

        });

        return content1;
    }
}

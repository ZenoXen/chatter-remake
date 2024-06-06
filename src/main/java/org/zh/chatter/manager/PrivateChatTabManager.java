package org.zh.chatter.manager;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.zh.chatter.util.Constants;

import java.util.HashMap;
import java.util.Map;

@Component
public class PrivateChatTabManager {
    @Getter
    @Setter
    private TabPane chatArea;
    private final Map<String, Tab> tabMap;
    private static final String PRIVATE_CHAT_TAB_FXML_PATH = "fxml/private-chat-tab.fxml";
    private static final String TAB_ID_PREFIX = "private_chat_";
    private final ApplicationContext applicationContext;

    public PrivateChatTabManager(ApplicationContext applicationContext) {
        this.tabMap = new HashMap<>();
        this.applicationContext = applicationContext;
    }

    private String buildTabId(String targetUserId) {
        return TAB_ID_PREFIX.concat(targetUserId);
    }

    public Tab addTab(String targetUserId, String targetUsername) throws Exception {
        FXMLLoader loader = new FXMLLoader(new ClassPathResource(PRIVATE_CHAT_TAB_FXML_PATH).getURL());
        loader.setControllerFactory(applicationContext::getBean);
        Tab newTab = loader.load();
        newTab.setText(targetUsername);
        String tabId = this.buildTabId(targetUserId);
        newTab.setId(tabId);
        tabMap.put(tabId, newTab);
        return newTab;
    }

    public Tab getTab(String targetUserId) {
        return tabMap.get(this.buildTabId(targetUserId));
    }

    public void removeTab(String tabId) {
        tabMap.remove(tabId);
    }
}

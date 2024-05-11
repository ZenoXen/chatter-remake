package org.zh.chatter;

import cn.hutool.core.util.RandomUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.util.WaitForAsyncUtils;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.NetworkInterfaceHolder;
import org.zh.chatter.model.bo.NetworkInterfaceBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.model.vo.UserVO;

import java.util.concurrent.TimeUnit;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest
public class UserInterfaceTests {

    private static final String NETWORK_INTERFACE_NAME = "Realtek PCIe 2.5GbE Family Controller";
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private static final String FXML_PATH = "fxml/main.fxml";
    private static final String CSS_PATH = "css/styles.css";
    private static final String WINDOW_TITLE = "局域网聊天室";

    private Stage stage;
    @Autowired
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Autowired
    private NetworkInterfaceHolder networkInterfaceHolder;

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(new ClassPathResource(FXML_PATH).getURL());
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(new ClassPathResource(CSS_PATH).getURL().toExternalForm());
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.show();
        this.stage = stage;
    }

    @Test
    public void shouldExitApp(FxRobot robot) throws InterruptedException {
        MenuBar bar = robot.lookup("#topMenuBar").query();
        MenuItem menuItem = bar.getMenus().get(0).getItems().get(1);
        robot.interactNoWait(menuItem::fire);
        Assertions.assertFalse(stage.isShowing());
    }

    @Test
    public void testModifyUsername(FxRobot robot) throws Exception {
        MenuBar bar = robot.lookup("#topMenuBar").query();
        MenuItem menuItem = bar.getMenus().get(0).getItems().get(0);
        robot.interactNoWait(menuItem::fire);
        //等两秒钟直到弹窗显示
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS, () -> robot.lookup(".dialog-pane").query().isVisible());
        String newUsername = RandomUtil.randomString(5);
        robot.write(newUsername);
        robot.press(KeyCode.ENTER);
        Assertions.assertEquals(newUsername, currentUserInfoHolder.getCurrentUser().getUsername());
    }

    @Test
    public void testSendMessage(FxRobot robot) {
        String inputContent = RandomUtil.randomString(100);
        robot.clickOn("#inputArea").write(inputContent);
        robot.clickOn("#sendButton");
        FxAssert.verifyThat("#messageArea", ListViewMatchers.hasItems(1));
        ListView listView = robot.lookup("#messageArea").queryAs(ListView.class);
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        boolean messagePresent = listView.getItems().stream().anyMatch(l -> {
            ChatMessageVO message = (ChatMessageVO) l;
            return message.getMessage().equals(inputContent) && message.getSenderId().equals(currentUser.getId()) && message.getSenderName().equals(currentUser.getUsername());
        });
        Assertions.assertTrue(messagePresent);
    }

    @Test
    public void testClearChatArea(FxRobot robot) {
        this.testSendMessage(robot);
        robot.clickOn("#clearButton");
        FxAssert.verifyThat("#messageArea", ListViewMatchers.hasItems(0));
    }

    @Test
    public void testUserListDialog(FxRobot robot) {
        robot.clickOn("#userListButton");
        FxAssert.verifyThat(".table-view", TableViewMatchers.hasNumRows(1));
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        TableView tableView = robot.lookup(".table-view").queryAs(TableView.class);
        boolean hasUser = tableView.getItems().contains(new UserVO(currentUser.getId(), currentUser.getUsername(), true));
        Assertions.assertTrue(hasUser);
    }

    @Test
    public void testChangeNetworkInterface(FxRobot robot) throws InterruptedException {
        MenuBar bar = robot.lookup("#topMenuBar").query();
        MenuItem menuItem = bar.getMenus().get(0).getItems().get(1);
        robot.interactNoWait(menuItem::fire);
        ComboBox<NetworkInterfaceBO> box = robot.lookup(".combo-box").query();
        FxAssert.verifyThat(box, NodeMatchers.isVisible());
        for (Node child : box.getChildrenUnmodifiable()) {
            if (child.getStyleClass().contains("arrow-button")) {
                Node arrowRegion = ((Pane) child).getChildren().get(0);
                robot.clickOn(arrowRegion);
                Thread.sleep(100);
                robot.clickOn(NETWORK_INTERFACE_NAME);
            }
        }
        robot.press(KeyCode.ENTER);
        Assertions.assertEquals(networkInterfaceHolder.getSelectedNetworkInterface().getDisplayName(), NETWORK_INTERFACE_NAME);
    }
}

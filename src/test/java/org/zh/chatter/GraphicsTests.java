package org.zh.chatter;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest
public class GraphicsTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private static final String FXML_PATH = "fxml/main.fxml";
    private static final String CSS_PATH = "css/styles.css";
    private static final String WINDOW_TITLE = "局域网聊天室";

    private Stage stage;

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
}

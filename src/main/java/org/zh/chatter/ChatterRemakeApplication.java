package org.zh.chatter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
public class ChatterRemakeApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    private static final String FXML_PATH = "fxml/main.fxml";
    private static final String CSS_PATH = "css/styles.css";
    private static final String WINDOW_TITLE = "局域网聊天室";

    @Override
    public void init() throws Exception {
        applicationContext = SpringApplication.run(ChatterRemakeApplication.class);
    }

    @Override
    public void stop() throws Exception {
        applicationContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(new ClassPathResource(FXML_PATH).getURL());
        Scene scene = new Scene(root);
        scene.getStylesheets().add(new ClassPathResource(CSS_PATH).getURL().toExternalForm());
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.show();
    }
}

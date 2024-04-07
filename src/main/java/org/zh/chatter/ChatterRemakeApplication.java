package org.zh.chatter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
public class ChatterRemakeApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(new ClassPathResource("fxml/scene.fxml").getURL());
        Scene scene = new Scene(root);
        scene.getStylesheets().add(new ClassPathResource("css/styles.css").getURL().toExternalForm());
        stage.setTitle("局域网聊天室");
        stage.setScene(scene);
        stage.show();
    }
}

package org.zh.chatter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.zh.chatter.exception.handler.GlobalExceptionHandler;
import org.zh.chatter.manager.NotificationManager;
import org.zh.chatter.manager.StageHolder;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ChatterRemakeApplication extends Application {

    private ConfigurableApplicationContext applicationContext;
    private GlobalExceptionHandler globalExceptionHandler;

    private static final String MAIN_FXML_PATH = "fxml/main.fxml";
    private static final String CSS_PATH = "css/styles.css";
    private static final String WINDOW_TITLE = "局域网聊天室";

    private Stage stage;

    @Override
    public void init() {
        applicationContext = SpringApplication.run(ChatterRemakeApplication.class);
        applicationContext.getBean(StageHolder.class).setStage(this.stage);
        globalExceptionHandler = new GlobalExceptionHandler(applicationContext.getBean(NotificationManager.class));
    }

    @Override
    public void stop() {
        applicationContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        //未捕获的异常，使用全局异常处理
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> globalExceptionHandler.handleException(t, e)));
        try {
            FXMLLoader loader = new FXMLLoader(new ClassPathResource(MAIN_FXML_PATH).getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(new ClassPathResource(CSS_PATH).getURL().toExternalForm());
            stage.setTitle(WINDOW_TITLE);
            stage.setScene(scene);
            stage.show();
            this.stage = stage;
        } catch (Throwable t) {
            globalExceptionHandler.showErrorDialog(Thread.currentThread(), t);
        }
    }
}

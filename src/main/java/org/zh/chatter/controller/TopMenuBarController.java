package org.zh.chatter.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class TopMenuBarController {
    private static final String ABOUT_ALERT_TITLE = "关于";
    private static final String ABOUT_FILE_PATH = "about.txt";
    @FXML
    private MenuBar topMenuBar;

    public void handleExit(ActionEvent actionEvent) {
        //退出
        Stage stage = (Stage) topMenuBar.getScene().getWindow();
        stage.close();
    }

    public void handleAboutPopup(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(ABOUT_ALERT_TITLE);
        alert.setHeaderText(null);
        alert.setContentText(new ClassPathResource(ABOUT_FILE_PATH).getContentAsString(StandardCharsets.UTF_8));
        alert.showAndWait();
    }

    public void changeName(ActionEvent actionEvent) {
        //todo 修改名字
    }
}

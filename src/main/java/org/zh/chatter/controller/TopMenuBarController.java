package org.zh.chatter.controller;


import jakarta.annotation.Resource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.zh.chatter.manager.CurrentUserInfoHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class TopMenuBarController {
    private static final String ABOUT_ALERT_TITLE = "关于";
    private static final String ABOUT_FILE_PATH = "about.txt";
    private static final String MODIFY_USERNAME_DIALOG_TITLE = "修改用户名";
    private static final String MODIFY_USERNAME_DIALOG_HEADER_TEXT = "请输入用户名";
    private static final String INPUT_INVALID_TITLE = "输入有误";
    private static final String INPUT_INVALID_CONTENT_TEXT = "输入不能为空";
    @FXML
    private MenuBar topMenuBar;

    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;

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

    public void showChangeNamePopup(ActionEvent actionEvent) {
        TextInputDialog textInputDialog = new TextInputDialog(currentUserInfoHolder.getCurrentUser().getUsername());
        textInputDialog.setTitle(MODIFY_USERNAME_DIALOG_TITLE);
        textInputDialog.setHeaderText(MODIFY_USERNAME_DIALOG_HEADER_TEXT);
        textInputDialog.show();
        textInputDialog.setResultConverter(buttonType -> {
            String username = "";
            if (buttonType == ButtonType.OK) {
                String result = textInputDialog.getEditor().getText();
                if (Strings.isNotEmpty(result)) {
                    currentUserInfoHolder.getCurrentUser().setUsername(result);
                    username = result;
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle(INPUT_INVALID_TITLE);
                    alert.setHeaderText(null);
                    alert.setContentText(INPUT_INVALID_CONTENT_TEXT);
                    alert.show();
                }
            }
            return username;
        });
    }
}

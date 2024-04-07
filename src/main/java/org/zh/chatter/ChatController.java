package org.zh.chatter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    private TextArea inputArea;

    @FXML
    private TextArea messageArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void handleMessageSend() {
        //清除输入框文本，并发送文本
        String text = inputArea.getText();
        inputArea.clear();
        messageArea.appendText(text + "\n");
    }
}

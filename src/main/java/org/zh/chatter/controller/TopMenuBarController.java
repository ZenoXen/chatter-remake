package org.zh.chatter.controller;


import jakarta.annotation.Resource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.zh.chatter.component.PrivateChatButtonActions;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.NetworkInterfaceHolder;
import org.zh.chatter.manager.PrivateChatTabManager;
import org.zh.chatter.model.bo.NetworkInterfaceBO;
import org.zh.chatter.network.TcpClient;
import org.zh.chatter.network.UdpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class TopMenuBarController {
    private static final String ABOUT_ALERT_TITLE = "关于";
    private static final String ABOUT_FILE_PATH = "about.txt";
    private static final String MODIFY_USERNAME_DIALOG_TITLE = "修改用户名";
    private static final String MODIFY_USERNAME_DIALOG_HEADER_TEXT = "请输入用户名";
    private static final String INPUT_INVALID_TITLE = "输入有误";
    private static final String INPUT_INVALID_CONTENT_TEXT = "输入不能为空";
    private static final String CHANGE_NETWORK_INTERFACE_DIALOG_TITLE = "更换聊天网卡";
    private static final String OPEN_REMOTE_PRIVATE_CHAT_DIALOG_TITLE = "连接远程私聊";
    private static final String CHANGE_NETWORK_INTERFACE_HEADER_TEXT = "请从下列网卡中选择你的聊天网卡";
    private static final String OPEN_REMOTE_PRIVATE_CHAT_HEADER_TEXT = "请输入连接地址";
    @FXML
    private MenuBar topMenuBar;

    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;

    @Resource
    private NetworkInterfaceHolder networkInterfaceHolder;

    @Resource
    private UdpServer udpServer;

    @Resource
    private PrivateChatButtonActions privateChatButtonActions;

    @Resource
    private PrivateChatTabManager privateChatTabManager;

    @Resource
    private TcpClient tcpClient;

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

    public void showChangeNetworkInterfacePopup(ActionEvent actionEvent) {
        NetworkInterface selectedNetworkInterface = networkInterfaceHolder.getSelectedNetworkInterface();
        Collection<NetworkInterface> allNetworkInterfaces = networkInterfaceHolder.getAllNetworkInterfaces();
        ChoiceDialog<NetworkInterfaceBO> dialog = new ChoiceDialog<>(new NetworkInterfaceBO(selectedNetworkInterface), allNetworkInterfaces.stream().map(NetworkInterfaceBO::new).collect(Collectors.toList()));
        dialog.setTitle(CHANGE_NETWORK_INTERFACE_DIALOG_TITLE);
        dialog.setHeaderText(CHANGE_NETWORK_INTERFACE_HEADER_TEXT);
        dialog.setContentText(null);
        dialog.show();
        dialog.setResultConverter(buttonType -> {
            NetworkInterfaceBO networkInterfaceBO = null;
            if (buttonType == ButtonType.OK) {
                networkInterfaceBO = dialog.getSelectedItem();
                udpServer.changeNetworkInterface(networkInterfaceBO.getNetworkInterface());
            }
            return networkInterfaceBO;
        });
    }

    public void openRemotePrivateChat(ActionEvent actionEvent) {
        TextInputDialog addressInputDialog = new TextInputDialog();
        addressInputDialog.setTitle(OPEN_REMOTE_PRIVATE_CHAT_DIALOG_TITLE);
        addressInputDialog.setHeaderText(OPEN_REMOTE_PRIVATE_CHAT_HEADER_TEXT);
        addressInputDialog.setContentText(null);
        Optional<String> inputAddress = addressInputDialog.showAndWait();
        inputAddress.ifPresent(addr -> {
            String[] split = addr.split(":");
            if (split.length < 2) {
                throw new RuntimeException("远程地址输入错误");
            }
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(split[0]);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            tcpClient.sendRemotePrivateChatUserInfoExchangeRequest(inetAddress, Integer.parseInt(split[1]));
        });
    }
}

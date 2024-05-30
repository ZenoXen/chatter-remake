package org.zh.chatter.component;

import cn.hutool.core.collection.CollectionUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.zh.chatter.model.vo.UserVO;
import org.zh.chatter.network.TcpClient;

import java.io.File;
import java.util.List;

@Slf4j
public class UserListDialog extends Dialog<Void> {
    private static final String ID_COLUMN_NAME = "用户id";
    private static final String USERNAME_COLUMN_NAME = "用户名";
    private static final String CLOSE_BUTTON_TEXT = "关闭";
    private static final String SEND_FILE_BUTTON_COLUMN_NAME = "操作";
    private static final String SEND_FILE_BUTTON_TEXT = "发送文件";
    private static final String DIALOG_TITLE = "用户列表";
    private static final String SELECT_FILE_TITLE = "选择要发送的文件";
    private static final int WIDTH = 300;
    private static final int ID_COLUMN_MAX_WIDTH = 150;
    private static final int NAME_COLUMN_MAX_WIDTH = 150;
    private static final int HEIGHT = 500;

    public UserListDialog(List<UserVO> users, TcpClient tcpClient) {
        this.setTitle(DIALOG_TITLE);
        TableView<UserVO> tableView = new TableView<>();
        TableColumn<UserVO, String> idColumn = new TableColumn<>(ID_COLUMN_NAME);
        TableColumn<UserVO, String> nameColumn = new TableColumn<>(USERNAME_COLUMN_NAME);
        TableColumn<UserVO, Void> sendFileColumn = new TableColumn<>(SEND_FILE_BUTTON_COLUMN_NAME);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setMaxWidth(NAME_COLUMN_MAX_WIDTH);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(ID_COLUMN_MAX_WIDTH);

        sendFileColumn.setCellFactory(param -> new TableCell<>() {
            private final Button sendFileButton = new Button(SEND_FILE_BUTTON_TEXT);

            {
                sendFileButton.setOnAction(event -> {
                    UserVO userVO = getTableView().getItems().get(getIndex());
                    //弹出windows文件选框
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(SELECT_FILE_TITLE);
                    File chosenFile = fileChooser.showOpenDialog(sendFileButton.getScene().getWindow());
                    if (chosenFile != null) {
                        tcpClient.sendFileTransferRequest(userVO, chosenFile);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(sendFileButton);
                }
            }
        });

        ObservableList<TableColumn<UserVO, ?>> tableColumns = tableView.getColumns();
        tableColumns.addAll(CollectionUtil.newArrayList(idColumn, nameColumn, sendFileColumn));
        tableView.setItems(FXCollections.observableList(users));
        this.getDialogPane().setContent(tableView);
        this.getDialogPane().getButtonTypes().add(new ButtonType(CLOSE_BUTTON_TEXT, ButtonBar.ButtonData.CANCEL_CLOSE));
        this.setWidth(WIDTH);
        this.setHeight(HEIGHT);
    }
}

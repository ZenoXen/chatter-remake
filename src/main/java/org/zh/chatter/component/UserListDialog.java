package org.zh.chatter.component;

import cn.hutool.core.collection.CollectionUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.zh.chatter.model.vo.UserVO;

import java.util.List;

public class UserListDialog extends Dialog<Void> {
    private static final String ID_COLUMN_NAME = "用户id";
    private static final String USERNAME_COLUMN_NAME = "用户名";
    private static final String CLOSE_BUTTON_TEXT = "关闭";
    private final TableView<UserVO> tableView;
    private static final String DIALOG_TITLE = "用户列表";

    public UserListDialog(List<UserVO> users) {
        this.setTitle(DIALOG_TITLE);
        this.tableView = new TableView<>();
        TableColumn<UserVO, String> idColumn = new TableColumn<>(ID_COLUMN_NAME);
        TableColumn<UserVO, String> nameColumn = new TableColumn<>(USERNAME_COLUMN_NAME);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        ObservableList<TableColumn<UserVO, ?>> tableColumns = tableView.getColumns();
        tableColumns.addAll(CollectionUtil.newArrayList(idColumn, nameColumn));
        tableView.setItems(FXCollections.observableList(users));
        this.getDialogPane().setContent(tableView);
        this.getDialogPane().getButtonTypes().add(new ButtonType(CLOSE_BUTTON_TEXT, ButtonBar.ButtonData.CANCEL_CLOSE));
    }
}

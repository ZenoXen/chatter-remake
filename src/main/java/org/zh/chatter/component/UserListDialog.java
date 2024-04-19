package org.zh.chatter.component;

import cn.hutool.core.collection.CollectionUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.zh.chatter.model.vo.UserVO;

import java.util.List;

public class UserListDialog extends Dialog<Void> {
    private final TableView<UserVO> tableView;
    private static final String DIALOG_TITLE = "用户列表";

    public UserListDialog(List<UserVO> users) {
        this.setTitle(DIALOG_TITLE);
        tableView = new TableView<>();
        TableColumn<UserVO, String> idColumn = new TableColumn<>("用户id");
        TableColumn<UserVO, String> nameColumn = new TableColumn<>("用户名");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        ObservableList<TableColumn<UserVO, ?>> tableColumns = tableView.getColumns();
        tableColumns.addAll(CollectionUtil.newArrayList(idColumn, nameColumn));
        tableView.setItems(FXCollections.observableList(users));
        this.getDialogPane().setContent(tableView);
        this.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
    }
}

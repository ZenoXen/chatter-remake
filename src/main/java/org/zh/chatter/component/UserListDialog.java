package org.zh.chatter.component;

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
        TableColumn<UserVO, String> nameColumn = new TableColumn<>("用户名");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        ObservableList<TableColumn<UserVO, ?>> tableColumns = tableView.getColumns();
        tableColumns.add(nameColumn);
        tableView.setItems(FXCollections.observableList(users));
        this.getDialogPane().setContent(tableView);
        this.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
    }
}

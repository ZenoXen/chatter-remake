package org.zh.chatter.component;

import cn.hutool.core.collection.CollectionUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.model.bo.FileTaskBO;

import java.time.LocalDateTime;

public class FileTransferDialog extends Dialog<Void> {

    private static final String CLOSE_BUTTON_TEXT = "关闭";
    private static final String DIALOG_TITLE = "文件发送列表";
    private static final String TOP_LABEL = "文件任务";
    private static final String BOTTOM_LABEL = "进行中的任务";
    private static final String TOP_TABLE_PLACEHOLDER = "没有文件任务";
    private static final String BOTTOM_TABLE_PLACEHOLDER = "没有进行中的任务";
    public static final int SPACING = 10;
    public static final int TOP_RIGHT_BOTTOM_LEFT = 10;
    public static final double TOP_BOX_HEIGHT_RATIO = 0.35;
    public static final double BOTTOM_BOX_HEIGHT_RATIO = 0.65;

    private final int width;
    private final int height;
    private final VBox topBox;
    private final VBox bottomBox;
    //非活动状态的任务
    private final TableView<FileTaskBO> topTaskTable;
    //活动状态的任务
    private final TableView<FileTaskBO> bottomTaskTable;
    private final BorderPane borderPane;
    private final FileTaskManager fileTaskManager;

    public FileTransferDialog(int width,
                              int height,
                              FileTaskManager fileTaskManager) {
        this.setTitle(DIALOG_TITLE);
        this.width = width;
        this.height = height;
        this.fileTaskManager = fileTaskManager;
        TableView<FileTaskBO> topTaskTable = this.generateTopTaskTable(fileTaskManager.getInactiveTasks());
        TableView<FileTaskBO> bottomTaskTable = this.generateBottomTaskTable(fileTaskManager.getOngoingTasks());
        this.topTaskTable = topTaskTable;
        this.bottomTaskTable = bottomTaskTable;
        this.topBox = this.generateTopVbox(topTaskTable);
        this.bottomBox = this.generateBottomVbox(bottomTaskTable);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(this.topBox);
        borderPane.setCenter(this.bottomBox);
        this.borderPane = borderPane;

        this.getDialogPane().setContent(borderPane);
        this.getDialogPane().getButtonTypes().add(new ButtonType(CLOSE_BUTTON_TEXT, ButtonBar.ButtonData.CANCEL_CLOSE));
    }

    private TableView<FileTaskBO> generateTopTaskTable(ObservableList<FileTaskBO> tasks) {
        TableView<FileTaskBO> fileTaskTable = new TableView<>();
        fileTaskTable.setItems(tasks);
        fileTaskTable.setPlaceholder(new Label(TOP_TABLE_PLACEHOLDER));
        TableColumn<FileTaskBO, String> idCol = new TableColumn<>("任务ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().getTaskId());

        TableColumn<FileTaskBO, String> fileNameCol = new TableColumn<>("文件名");
        fileNameCol.setCellValueFactory(cellData -> cellData.getValue().getFileName());

        TableColumn<FileTaskBO, String> senderCol = new TableColumn<>("发送者");
        senderCol.setCellValueFactory(cellData -> cellData.getValue().getSenderName());

        TableColumn<FileTaskBO, LocalDateTime> sendTimeCol = new TableColumn<>("发送时间");
        sendTimeCol.setCellValueFactory(cellData -> cellData.getValue().getSendTime());

        TableColumn<FileTaskBO, Long> fileSizeCol = new TableColumn<>("文件大小");
        fileSizeCol.setCellValueFactory(cellData -> cellData.getValue().getFileSize().asObject());

        TableColumn<FileTaskBO, FileTaskStatusEnum> statusCol = new TableColumn<>("任务状态");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().getStatus());

        TableColumn<FileTaskBO, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button receiveButton = new Button("接收文件");

            {
                receiveButton.setOnAction(event -> {
                    FileTaskBO task = getTableView().getItems().get(getIndex());
                    task.getStatus().set(FileTaskStatusEnum.TRANSFERRING);
                    fileTaskManager.addOrUpdateTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !getTableView().getItems().get(getIndex()).getStatus().get().equals(FileTaskStatusEnum.PENDING)) {
                    setGraphic(null);
                } else {
                    setGraphic(receiveButton);
                }
            }
        });

        ObservableList<TableColumn<FileTaskBO, ?>> columns = fileTaskTable.getColumns();
        columns.addAll(CollectionUtil.newArrayList(idCol, fileNameCol, senderCol, sendTimeCol, fileSizeCol, statusCol, actionCol));
        return fileTaskTable;
    }

    private VBox generateTopVbox(TableView<FileTaskBO> fileTaskTable) {
        VBox topBox = new VBox(new Label(TOP_LABEL), fileTaskTable);
        topBox.setPadding(new Insets(TOP_RIGHT_BOTTOM_LEFT));
        topBox.setSpacing(SPACING);
        topBox.setPrefHeight(height * TOP_BOX_HEIGHT_RATIO);
        return topBox;
    }

    private TableView<FileTaskBO> generateBottomTaskTable(ObservableList<FileTaskBO> tasks) {
        TableView<FileTaskBO> fileTaskTable = new TableView<>();
        fileTaskTable.setItems(tasks);
        fileTaskTable.setPlaceholder(new Label(BOTTOM_TABLE_PLACEHOLDER));
        TableColumn<FileTaskBO, String> idColOngoing = new TableColumn<>("任务ID");
        idColOngoing.setCellValueFactory(cellData -> cellData.getValue().getTaskId());

        TableColumn<FileTaskBO, String> fileNameColOngoing = new TableColumn<>("文件名");
        fileNameColOngoing.setCellValueFactory(cellData -> cellData.getValue().getFileName());

        TableColumn<FileTaskBO, String> senderColOngoing = new TableColumn<>("发送者");
        senderColOngoing.setCellValueFactory(cellData -> cellData.getValue().getSenderName());

        TableColumn<FileTaskBO, LocalDateTime> sendTimeColOngoing = new TableColumn<>("发送时间");
        sendTimeColOngoing.setCellValueFactory(cellData -> cellData.getValue().getSendTime());

        TableColumn<FileTaskBO, Long> fileSizeColOngoing = new TableColumn<>("文件大小");
        fileSizeColOngoing.setCellValueFactory(cellData -> cellData.getValue().getFileSize().asObject());

        TableColumn<FileTaskBO, FileTaskStatusEnum> statusColOngoing = new TableColumn<>("任务状态");
        statusColOngoing.setCellValueFactory(cellData -> cellData.getValue().getStatus());

        TableColumn<FileTaskBO, Double> progressCol = new TableColumn<>("进度");
        progressCol.setCellValueFactory(cellData -> cellData.getValue().getTransferProgress().asObject());
        progressCol.setCellFactory(ProgressBarTableCell.forTableColumn());

        TableColumn<FileTaskBO, Void> suspendCol = new TableColumn<>("操作1");
        suspendCol.setCellFactory(param -> new TableCell<>() {
            private final Button suspendButton = new Button("暂停");

            {
                suspendButton.setOnAction(event -> {
                    FileTaskBO task = getTableView().getItems().get(getIndex());
                    task.getStatus().set(FileTaskStatusEnum.SUSPENDED);
                    fileTaskManager.addOrUpdateTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(suspendButton);
                }
            }
        });

        TableColumn<FileTaskBO, Void> cancellCol = new TableColumn<>("操作2");
        cancellCol.setCellFactory(param -> new TableCell<>() {
            private final Button cancellButton = new Button("取消");

            {
                cancellButton.setOnAction(event -> {
                    FileTaskBO task = getTableView().getItems().get(getIndex());
                    task.getStatus().set(FileTaskStatusEnum.CANCELLED);
                    fileTaskManager.addOrUpdateTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(cancellButton);
                }
            }
        });

        ObservableList<TableColumn<FileTaskBO, ?>> columns = fileTaskTable.getColumns();
        columns.addAll(CollectionUtil.newArrayList(idColOngoing, fileNameColOngoing, senderColOngoing, sendTimeColOngoing, fileSizeColOngoing, statusColOngoing, progressCol, cancellCol));
        return fileTaskTable;
    }

    private VBox generateBottomVbox(TableView<FileTaskBO> fileTaskTable) {
        VBox bottomBox = new VBox(new Label(BOTTOM_LABEL), fileTaskTable);
        bottomBox.setPadding(new Insets(TOP_RIGHT_BOTTOM_LEFT));
        bottomBox.setSpacing(SPACING);
        bottomBox.setPrefHeight(height * BOTTOM_BOX_HEIGHT_RATIO);
        return bottomBox;
    }
}

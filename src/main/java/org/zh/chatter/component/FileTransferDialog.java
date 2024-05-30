package org.zh.chatter.component;

import cn.hutool.core.collection.CollectionUtil;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.bo.FileTransferStatusChangedNotificationBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.model.vo.FileTaskCellVO;

import java.time.LocalDateTime;

public class FileTransferDialog extends Dialog<Void> {

    private static final String CLOSE_BUTTON_TEXT = "关闭";
    private static final String DIALOG_TITLE = "文件发送列表";
    private static final String TOP_LABEL = "文件任务";
    private static final String BOTTOM_LABEL = "进行中的任务";
    private static final String TOP_TABLE_PLACEHOLDER = "没有文件任务";
    private static final String BOTTOM_TABLE_PLACEHOLDER = "没有进行中的任务";
    private static final int SPACING = 10;
    private static final int TOP_RIGHT_BOTTOM_LEFT = 10;
    private static final double TOP_BOX_HEIGHT_RATIO = 0.35;
    private static final double BOTTOM_BOX_HEIGHT_RATIO = 0.65;

    private final int height;
    private final int width;
    private final FileTaskManager fileTaskManager;
    private final CurrentUserInfoHolder currentUserInfoHolder;

    public FileTransferDialog(int width,
                              int height,
                              FileTaskManager fileTaskManager,
                              CurrentUserInfoHolder currentUserInfoHolder) {
        this.setTitle(DIALOG_TITLE);
        this.width = width;
        this.height = height;
        this.fileTaskManager = fileTaskManager;
        this.currentUserInfoHolder = currentUserInfoHolder;
        TableView<FileTaskCellVO> topTaskTable = this.generateTopTaskTable(fileTaskManager.getInactiveTasks());
        TableView<FileTaskCellVO> bottomTaskTable = this.generateBottomTaskTable(fileTaskManager.getOngoingTasks());
        //非活动状态的任务
        //活动状态的任务
        VBox topBox = this.generateTopVbox(topTaskTable);
        VBox bottomBox = this.generateBottomVbox(bottomTaskTable);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topBox);
        borderPane.setCenter(bottomBox);

        this.getDialogPane().setContent(borderPane);
        this.getDialogPane().getButtonTypes().add(new ButtonType(CLOSE_BUTTON_TEXT, ButtonBar.ButtonData.CANCEL_CLOSE));
    }

    private TableView<FileTaskCellVO> generateTopTaskTable(ObservableList<FileTaskCellVO> tasks) {
        TableView<FileTaskCellVO> fileTaskTable = new TableView<>();
        fileTaskTable.setItems(tasks);
        fileTaskTable.setPlaceholder(new Label(TOP_TABLE_PLACEHOLDER));
        TableColumn<FileTaskCellVO, String> isMySelfColumn = new TableColumn<>("我发送的？");
        isMySelfColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIsMySelf() ? "是" : ""));

        TableColumn<FileTaskCellVO, String> idCol = new TableColumn<>("任务ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().getTaskId());

        TableColumn<FileTaskCellVO, String> fileNameCol = new TableColumn<>("文件名");
        fileNameCol.setCellValueFactory(cellData -> cellData.getValue().getFileName());

        TableColumn<FileTaskCellVO, String> senderCol = new TableColumn<>("发送者");
        senderCol.setCellValueFactory(cellData -> cellData.getValue().getSenderName());

        TableColumn<FileTaskCellVO, LocalDateTime> sendTimeCol = new TableColumn<>("发送时间");
        sendTimeCol.setCellValueFactory(cellData -> cellData.getValue().getSendTime());

        TableColumn<FileTaskCellVO, Long> fileSizeCol = new TableColumn<>("文件大小");
        fileSizeCol.setCellValueFactory(cellData -> cellData.getValue().getFileSize().asObject());

        TableColumn<FileTaskCellVO, FileTaskStatusEnum> statusCol = new TableColumn<>("任务状态");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().getStatus());

        ObservableList<TableColumn<FileTaskCellVO, ?>> columns = fileTaskTable.getColumns();
        columns.addAll(CollectionUtil.newArrayList(idCol, fileNameCol, senderCol, sendTimeCol, fileSizeCol, statusCol));
        return fileTaskTable;
    }

    private VBox generateTopVbox(TableView<FileTaskCellVO> fileTaskTable) {
        VBox topBox = new VBox(new Label(TOP_LABEL), fileTaskTable);
        topBox.setPadding(new Insets(TOP_RIGHT_BOTTOM_LEFT));
        topBox.setSpacing(SPACING);
        topBox.setPrefWidth(width);
        topBox.setPrefHeight(height * TOP_BOX_HEIGHT_RATIO);
        return topBox;
    }

    private TableView<FileTaskCellVO> generateBottomTaskTable(ObservableList<FileTaskCellVO> tasks) {
        TableView<FileTaskCellVO> fileTaskTable = new TableView<>();
        fileTaskTable.setItems(tasks);
        fileTaskTable.setPlaceholder(new Label(BOTTOM_TABLE_PLACEHOLDER));
        TableColumn<FileTaskCellVO, String> idColOngoing = new TableColumn<>("任务ID");
        idColOngoing.setCellValueFactory(cellData -> cellData.getValue().getTaskId());

        TableColumn<FileTaskCellVO, String> fileNameColOngoing = new TableColumn<>("文件名");
        fileNameColOngoing.setCellValueFactory(cellData -> cellData.getValue().getFileName());

        TableColumn<FileTaskCellVO, String> senderColOngoing = new TableColumn<>("发送者");
        senderColOngoing.setCellValueFactory(cellData -> cellData.getValue().getSenderName());

        TableColumn<FileTaskCellVO, LocalDateTime> sendTimeColOngoing = new TableColumn<>("发送时间");
        sendTimeColOngoing.setCellValueFactory(cellData -> cellData.getValue().getSendTime());

        TableColumn<FileTaskCellVO, Long> fileSizeColOngoing = new TableColumn<>("文件大小");
        fileSizeColOngoing.setCellValueFactory(cellData -> cellData.getValue().getFileSize().asObject());

        TableColumn<FileTaskCellVO, FileTaskStatusEnum> statusColOngoing = new TableColumn<>("任务状态");
        statusColOngoing.setCellValueFactory(cellData -> cellData.getValue().getStatus());

        TableColumn<FileTaskCellVO, Double> progressCol = new TableColumn<>("进度");
        progressCol.setCellValueFactory(cellData -> cellData.getValue().getTransferProgress().asObject());
        progressCol.setCellFactory(ProgressBarTableCell.forTableColumn());

        TableColumn<FileTaskCellVO, Void> suspendCol = new TableColumn<>("操作1");
        suspendCol.setCellFactory(param -> new TableCell<>() {
            private final Button suspendButton = new Button("暂停");

            {
                suspendButton.setOnAction(event -> {
                    FileTaskCellVO cellVO = getTableView().getItems().get(getIndex());
                    String taskId = cellVO.getTaskId().get();
                    FileTaskBO task = fileTaskManager.getTask(taskId);
                    if (task != null && FileTaskStatusEnum.TRANSFERRING.equals(task.getStatus())) {
                        task.setStatus(FileTaskStatusEnum.SUSPENDED);
                        fileTaskManager.addOrUpdateTask(task);
                        NioSocketChannel channel = task.getChannel();
                        String currentUserId = currentUserInfoHolder.getCurrentUser().getId();
                        FileTransferStatusChangedNotificationBO fileTransferStatusChangedNotificationBO = new FileTransferStatusChangedNotificationBO();
                        fileTransferStatusChangedNotificationBO.setTargetStatus(FileTaskStatusEnum.SUSPENDED);
                        channel.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_STATUS_CHANGED_NOTIFICATION, taskId, currentUserId, fileTransferStatusChangedNotificationBO));
                    }
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

        TableColumn<FileTaskCellVO, Void> cancellCol = new TableColumn<>("操作2");
        cancellCol.setCellFactory(param -> new TableCell<>() {
            private final Button cancellButton = new Button("取消");

            {
                cancellButton.setOnAction(event -> {
                    FileTaskCellVO cellVO = getTableView().getItems().get(getIndex());
                    String taskId = cellVO.getTaskId().get();
                    FileTaskBO task = fileTaskManager.getTask(taskId);
                    if (task != null && FileTaskStatusEnum.ON_GOING_STATUSES.contains(task.getStatus())) {
                        task.setStatus(FileTaskStatusEnum.CANCELLED);
                        fileTaskManager.addOrUpdateTask(task);
                        NioSocketChannel channel = task.getChannel();
                        String currentUserId = currentUserInfoHolder.getCurrentUser().getId();
                        FileTransferStatusChangedNotificationBO fileTransferStatusChangedNotificationBO = new FileTransferStatusChangedNotificationBO();
                        fileTransferStatusChangedNotificationBO.setTargetStatus(FileTaskStatusEnum.CANCELLED);
                        channel.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_STATUS_CHANGED_NOTIFICATION, taskId, currentUserId, fileTransferStatusChangedNotificationBO));
                        channel.close();
                    }
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

        ObservableList<TableColumn<FileTaskCellVO, ?>> columns = fileTaskTable.getColumns();
        columns.addAll(CollectionUtil.newArrayList(idColOngoing, fileNameColOngoing, senderColOngoing, sendTimeColOngoing, fileSizeColOngoing, statusColOngoing, progressCol, cancellCol));
        return fileTaskTable;
    }

    private VBox generateBottomVbox(TableView<FileTaskCellVO> fileTaskTable) {
        VBox bottomBox = new VBox(new Label(BOTTOM_LABEL), fileTaskTable);
        bottomBox.setPadding(new Insets(TOP_RIGHT_BOTTOM_LEFT));
        bottomBox.setSpacing(SPACING);
        bottomBox.setPrefWidth(width);
        bottomBox.setPrefHeight(height * BOTTOM_BOX_HEIGHT_RATIO);
        return bottomBox;
    }
}

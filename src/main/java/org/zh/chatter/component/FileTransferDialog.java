package org.zh.chatter.component;

import cn.hutool.core.collection.CollectionUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.model.vo.FileTaskCellVO;

public class FileTransferDialog extends Dialog<Void> {

    private static final String CLOSE_BUTTON_TEXT = "关闭";
    private static final String DIALOG_TITLE = "文件发送列表";
    private static final String TOP_LABEL = "历史文件任务";
    private static final String BOTTOM_LABEL = "进行中的任务";
    private static final String TOP_TABLE_PLACEHOLDER = "没有历史文件任务";
    private static final String BOTTOM_TABLE_PLACEHOLDER = "没有进行中的任务";
    private static final int SPACING = 10;
    private static final int TOP_RIGHT_BOTTOM_LEFT = 10;
    private static final double TOP_BOX_HEIGHT_RATIO = 0.35;
    private static final double BOTTOM_BOX_HEIGHT_RATIO = 0.65;

    private final int height;
    private final int width;

    public FileTransferDialog(int width,
                              int height,
                              FileTaskButtonActions fileTaskButtonActions,
                              ObservableList<FileTaskCellVO> inactiveTasks,
                              ObservableList<FileTaskCellVO> ongoingTasks) {
        this.setTitle(DIALOG_TITLE);
        this.width = width;
        this.height = height;
        TableView<FileTaskCellVO> topTaskTable = this.generateTopTaskTable(inactiveTasks);
        TableView<FileTaskCellVO> bottomTaskTable = this.generateBottomTaskTable(ongoingTasks, fileTaskButtonActions);
        //非活动状态的任务
        VBox topBox = this.generateTopVbox(topTaskTable);
        //活动状态的任务
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

        TableColumn<FileTaskCellVO, String> sendTimeCol = new TableColumn<>("发送时间");
        sendTimeCol.setCellValueFactory(cellData -> cellData.getValue().getSendTime());

        TableColumn<FileTaskCellVO, String> fileSizeCol = new TableColumn<>("文件大小");
        fileSizeCol.setCellValueFactory(cellData -> cellData.getValue().getFileSize());

        TableColumn<FileTaskCellVO, FileTaskStatusEnum> statusCol = new TableColumn<>("任务状态");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().getStatus());

        ObservableList<TableColumn<FileTaskCellVO, ?>> columns = fileTaskTable.getColumns();
        columns.addAll(CollectionUtil.newArrayList(isMySelfColumn, idCol, fileNameCol, senderCol, sendTimeCol, fileSizeCol, statusCol));
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

    private TableView<FileTaskCellVO> generateBottomTaskTable(ObservableList<FileTaskCellVO> tasks, FileTaskButtonActions fileTaskButtonActions) {
        TableView<FileTaskCellVO> fileTaskTable = new TableView<>();
        fileTaskTable.setItems(tasks);
        fileTaskTable.setPlaceholder(new Label(BOTTOM_TABLE_PLACEHOLDER));
        TableColumn<FileTaskCellVO, String> idColOngoing = new TableColumn<>("任务ID");
        idColOngoing.setCellValueFactory(cellData -> cellData.getValue().getTaskId());

        TableColumn<FileTaskCellVO, String> fileNameColOngoing = new TableColumn<>("文件名");
        fileNameColOngoing.setCellValueFactory(cellData -> cellData.getValue().getFileName());

        TableColumn<FileTaskCellVO, String> senderColOngoing = new TableColumn<>("发送者");
        senderColOngoing.setCellValueFactory(cellData -> cellData.getValue().getSenderName());

        TableColumn<FileTaskCellVO, String> sendTimeColOngoing = new TableColumn<>("发送时间");
        sendTimeColOngoing.setCellValueFactory(cellData -> cellData.getValue().getSendTime());

        TableColumn<FileTaskCellVO, String> fileSizeColOngoing = new TableColumn<>("文件大小");
        fileSizeColOngoing.setCellValueFactory(cellData -> cellData.getValue().getFileSize());

        TableColumn<FileTaskCellVO, FileTaskStatusEnum> statusColOngoing = new TableColumn<>("任务状态");
        statusColOngoing.setCellValueFactory(cellData -> cellData.getValue().getStatus());

        TableColumn<FileTaskCellVO, Double> progressCol = new TableColumn<>("进度");
        progressCol.setCellValueFactory(cellData -> cellData.getValue().getTransferProgress().asObject());
        progressCol.setCellFactory(ProgressBarTableCell.forTableColumn());

        TableColumn<FileTaskCellVO, Button> suspendCol = new TableColumn<>("操作1");
        suspendCol.setCellFactory(ActionButtonTableCell.forTableColumn("暂停", fileTaskButtonActions.getSuspendButtonAction(), fileTaskButtonActions.getSuspendButtonShowAction()));

        TableColumn<FileTaskCellVO, Button> cancelCol = new TableColumn<>("操作2");
        cancelCol.setCellFactory(ActionButtonTableCell.forTableColumn("取消", fileTaskButtonActions.getCancelButtonAction(), null));

        ObservableList<TableColumn<FileTaskCellVO, ?>> columns = fileTaskTable.getColumns();
        columns.addAll(CollectionUtil.newArrayList(idColOngoing, fileNameColOngoing, senderColOngoing, sendTimeColOngoing, fileSizeColOngoing, statusColOngoing, progressCol, cancelCol));
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

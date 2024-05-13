package org.zh.chatter.exception.handler;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.zh.chatter.controller.ErrorController;
import org.zh.chatter.enums.NotificationTypeEnum;
import org.zh.chatter.manager.NotificationManager;
import org.zh.chatter.model.vo.NotificationVO;

import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.time.LocalDateTime;

@Slf4j
public class GlobalExceptionHandler {

    private static final String ERROR_FXML_PATH = "fxml/error.fxml";
    private static final String ERROR_DIALOG_TITLE = "错误报告";
    private final NotificationManager notificationManager;

    public GlobalExceptionHandler(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    /**
     * 应用通用异常处理
     *
     * @param t
     * @param e
     */
    public void handleException(Thread t, Throwable e) {
        //如果是网络异常，则显示到右侧通知栏；如果是其他异常，弹窗展示
        if ((e instanceof RuntimeException || e instanceof InvocationTargetException) && e.getCause() != null) {
            this.handleException(t, e.getCause());
        } else if (e instanceof SocketException) {
            notificationManager.addNotification(new NotificationVO(NotificationTypeEnum.NETWORK_ERROR, LocalDateTime.now(), "请检查网络设置"));
        } else {
            this.showErrorDialog(t, e);
        }
    }

    /**
     * 通用错误弹窗代码
     *
     * @param t
     * @param e
     */
    public void showErrorDialog(Thread t, Throwable e) {
        log.error(String.format("线程%s出现非预期异常：", t), e);
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(ERROR_DIALOG_TITLE);
        try {
            FXMLLoader loader = new FXMLLoader(new ClassPathResource(ERROR_FXML_PATH).getURL());
            Parent root = loader.load();
            ((ErrorController) loader.getController()).setErrorText(e.getMessage());
            Scene scene = new Scene(root, 250, 400);
            dialog.setScene(scene);
            dialog.show();
        } catch (Exception exception) {
            log.error("错误弹窗失败：", exception);
        }
    }
}

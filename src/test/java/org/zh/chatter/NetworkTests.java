package org.zh.chatter;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.embedded.EmbeddedChannel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.zh.chatter.enums.CommonDataTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.model.bo.ChatMessageBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.dto.UdpCommonDataDTO;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.model.vo.UserVO;
import org.zh.chatter.network.UdpCommonChannelInboundHandler;
import org.zh.chatter.network.UdpCommonDataDecoder;
import org.zh.chatter.network.UdpCommonDataEncoder;
import org.zh.chatter.util.NetworkUtil;

import java.time.LocalDateTime;

@ExtendWith(ApplicationExtension.class)
@SpringBootTest
public class NetworkTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private static final String FXML_PATH = "fxml/main.fxml";
    private static final String CSS_PATH = "css/styles.css";
    private static final String WINDOW_TITLE = "局域网聊天室";

    private Stage stage;
    @Autowired
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Autowired
    private UdpCommonDataDecoder udpCommonDataDecoder;
    @Autowired
    private UdpCommonChannelInboundHandler udpCommonChannelInboundHandler;
    @Autowired
    private UdpCommonDataEncoder udpCommonDataEncoder;
    @Value("${app.port.udp}")
    private Integer port;
    @Autowired
    private ObjectMapper objectMapper;

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(new ClassPathResource(FXML_PATH).getURL());
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(new ClassPathResource(CSS_PATH).getURL().toExternalForm());
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.show();
        this.stage = stage;
    }

    @Test
    public void testReceiveChatMessage(FxRobot robot) throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(udpCommonDataDecoder, udpCommonChannelInboundHandler, udpCommonDataEncoder);
        String randomStr = RandomUtil.randomString(100);
        NodeUserBO messageUser = new NodeUserBO();
        String testId = "test-id";
        messageUser.setId(testId);
        String testUsername = "test-user";
        messageUser.setUsername(testUsername);
        messageUser.setJoinTime(LocalDateTime.now());
        ChatMessageBO chatMessageBO = new ChatMessageBO(messageUser, randomStr, LocalDateTime.now());
        String content = objectMapper.writeValueAsString(chatMessageBO);
        NetworkUtil.getAllBroadcastAddresses().stream().findAny().ifPresent(a -> {
            UdpCommonDataDTO udpCommonDataDTO = new UdpCommonDataDTO(CommonDataTypeEnum.CHAT_MESSAGE.getCode(), null, a.getAddress(), port, content);
            embeddedChannel.writeInbound(udpCommonDataDTO);
            //界面上是否有聊天记录
            ListView listView = robot.lookup("#messageArea").queryAs(ListView.class);
            NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
            boolean messagePresent = listView.getItems().stream().anyMatch(l -> {
                ChatMessageVO message = (ChatMessageVO) l;
                return message.getMessage().equals(randomStr) && message.getSenderId().equals(testId) && message.getSenderName().equals(testUsername);
            });
            Assertions.assertTrue(messagePresent);
            //用户列表是否有该用户
            robot.clickOn("#userListButton");
            TableView tableView = robot.lookup(".table-view").queryAs(TableView.class);
            boolean hasUser = tableView.getItems().contains(new UserVO(messageUser.getId(), messageUser.getUsername(), false));
            Assertions.assertTrue(hasUser);
        });
    }
}

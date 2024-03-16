package cn.edu.tongji.healper.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.edu.tongji.healper.entity.ChatMessageEntity;
import cn.edu.tongji.healper.entity.ClientEntity;
import cn.edu.tongji.healper.entity.ConsultantEntity;
import cn.edu.tongji.healper.entity.User;
import cn.edu.tongji.healper.outdto.UserType;
import cn.edu.tongji.healper.service.ConsultService;
import cn.edu.tongji.healper.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
@ServerEndpoint(value = "/websocket/{userphone}")
@Component
@SaCheckLogin
public class WebSocketServer {

    private String userphone;

    private static CopyOnWriteArraySet<WebSocketServer> webSocketServerSet = new CopyOnWriteArraySet<>();

    private Session session = null;

    private static UserService userService;

    private static ConsultService consultService;
·
    @Autowired
    public void setUserService(UserService userService) {
        WebSocketServer.userService = userService;
    }

    @Autowired
    public void setChatService(ConsultService consultService) {
        WebSocketServer.consultService = consultService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userphone") String userphone) {
        // 建立连接
        this.session = session;
        webSocketServerSet.add(this);
        this.userphone = userphone;
//        this.sendMessage("测试一下");
    }

    @OnClose
    public void onClose() {
        // 关闭链接
        webSocketServerSet.remove(this);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject jsonMessage = JSON.parseObject(message);
        System.out.println(jsonMessage);
        for (WebSocketServer item: webSocketServerSet) {
            try {
                String toUserphone = jsonMessage.getString("toUserphone");
                if (item.userphone.equals(toUserphone)) {
                    String fromUserphone = this.userphone;
                    User fromUser = userService.findUserByPhone(fromUserphone);
                    User toUser = userService.findUserByPhone(toUserphone);
                    Integer clientId, consultantId;
                    UserType sender;
                    if (fromUser.getClass() == ClientEntity.class && toUser.getClass() == ConsultantEntity.class) {
                        clientId = fromUser.getId();
                        sender = UserType.client;
                        consultantId = toUser.getId();
                    } else if (fromUser.getClass() == ConsultantEntity.class && toUser.getClass() == ClientEntity.class) {
                        consultantId = fromUser.getId();
                        sender = UserType.consultant;
                        clientId = toUser.getId();
                    } else {
                        throw new RuntimeException("Userphone error!");
                    }
                    ChatMessageEntity chatMessage = consultService.addChatMessage(
                            clientId, consultantId,
                            jsonMessage.getString("content"),
                            sender
                    );
                    item.sendMessage(fromUser.getId(), toUser.getId(), chatMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(Integer fromId, Integer toId, ChatMessageEntity messageEntity) throws IOException{
        synchronized (this.session) {
            JSONObject message = new JSONObject();
            message.put("fromId", fromId);
            message.put("toId", toId);
            message.put("createTime", messageEntity.getCreateTime());
            message.put("content", messageEntity.getContent());
            this.session.getBasicRemote().sendText(message.toJSONString());
        }
    }

}
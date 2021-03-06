package com.theironyard.controllers;

import com.theironyard.entities.Messagea;
import com.theironyard.entities.User;
import com.theironyard.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jeffryporter on 7/29/16.
 */
@Controller
public class VSChatController
{
    @Autowired
    UserRepository users;

    @Autowired
    WorkRepository works;

    @Autowired
    ItemRepository items;

    @Autowired
    MessageaRepository messages;

    @Autowired
    PhotoRepository photos;


    static SimpMessagingTemplate messenger;

    @Autowired
    public VSChatController(SimpMessagingTemplate messenger)
    {
        this.messenger = messenger;
    }
    //User author, User recipient, Item item, String body, LocalDateTime time, String conversation)
    @MessageMapping("/topic/chat/{conversation}")
    @SendTo("/chat/{conversation}")
    public Message sendMessage(Message msg)
    {

        if (new String((byte[]) msg.getPayload()).length() > 0)
        {
            //System.out.println(new String((byte[]) msg.getPayload()));
            LinkedHashMap mapper = new LinkedHashMap();
            JacksonJsonParser parser = new JacksonJsonParser();
            mapper = (LinkedHashMap) parser.parseMap(new String((byte[]) msg.getPayload()));
            User user = users.findByUsername((String) mapper.get("name"));
            String conversation = (String) mapper.get("conversation");
            Messagea mess = new Messagea(user, users.findOne(Integer.valueOf((String) mapper.get("receiverid"))), items.findOne(Integer.valueOf((String) mapper.get("itemid"))), (String) mapper.get("body"), LocalDateTime.now(), (String) mapper.get("conversation"));
            messages.save(mess);
            this.messenger.convertAndSend("/topic/room/"+conversation, msg);
        }

        return msg;

    }
}

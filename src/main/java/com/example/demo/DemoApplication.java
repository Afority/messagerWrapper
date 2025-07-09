package com.example.demo;

import com.example.demo.messager.Messager;
import com.example.demo.messager.classes.Message;
import com.example.demo.messager.classes.SimpleMessage;
import org.openqa.selenium.WebElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@SpringBootApplication
public class DemoApplication {
    static Messager messager = null;

    //    private static int messageReadCounter = 0;
//
//    record Message(Integer id, String message) {}
//
    @GetMapping("/messages")
    public List<Message> messages(@RequestParam(required = false) String sort,
                                  @RequestParam(required = false) Integer limit) {
        List<Message> messages = messager.getAllMessagesInCurrentChat();

        if (sort != null) {
            if (sort.equals("asc")) {
                messages.sort(Comparator.comparingInt(Message::id).reversed());
            }
            else if (!sort.equals("desc")) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid sort parameter");
            }
        }
        if (limit != null) {
            if (limit > messages.size()) {
                limit = messages.size();
            }
            return messages.subList(0, limit);
        }
        return messages;
    }

//
//    @GetMapping("/getMessages")
//    public String index() {
//        StringBuilder builder = new StringBuilder();
//        for (int i = messageReadCounter; i < messager.getMessages().size(); ++i) {
//            try {
//                builder.append(messager.getMessageText(i) + "\n");
//                messageReadCounter = ++i;
//            }
//            catch (Exception e) {continue;}
//        }
//        return builder.toString();
//    }
//
//    @PostMapping("/sendMessage")
//    public String sendMessage(@RequestBody SimpleMessage message){
//        messager.sendMessage(message.content());
//        return "success";
//    }

    public static void main(String[] args) {
        messager = Messager.getInstance();
        SpringApplication.run(DemoApplication.class, args);
    }
}

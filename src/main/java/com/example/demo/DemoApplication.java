package com.example.demo;

import com.example.demo.messager.Messager;
import com.example.demo.messager.classes.Message;
import com.example.demo.messager.classes.SimpleMessage;

import org.openqa.selenium.NoSuchElementException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Comparator;
import java.util.List;


@RestController
@SpringBootApplication
public class DemoApplication {
    static Messager messager = null;

    @GetMapping("/messages")
    public List<Message> messages(
            @RequestParam String chatId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer limit) {
        messager.goToTheChat(chatId);
        try {
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
        catch (NoSuchElementException ignored){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
    }

    private record ChatMessage(String chatId, String message) { };

    @PostMapping("/messages")
    public String sendMessage(@RequestBody ChatMessage message){
        messager.goToTheChat(message.chatId());
        try {
            messager.sendMessage(message.message());
            return "success";
        }
        catch (NoSuchElementException ignored){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "chat not found");
        }
    }

    public static void main(String[] args) {
        messager = Messager.getInstance();
        SpringApplication.run(DemoApplication.class, args);
    }
}

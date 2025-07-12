package com.example.demo.routers;

import org.openqa.selenium.NoSuchElementException;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;

import com.example.demo.messager.classes.Message;
import com.example.demo.messager.Messenger;

@RestController
@RequestMapping("/api/messages")
public class Messages {
    Messenger messenger;
    Messages(){messenger = Messenger.getInstance();}

    record ChatMessage(String chatId, String message) { };

    @GetMapping
    public List<Message> messages(
            @RequestParam String chatId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer limit) {
        messenger.goToTheChat(chatId);
        messenger.waitTheInputField();
        try {
            List<Message> messages = messenger.getAllMessagesInCurrentChat();
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
                    return messages.subList(0, messages.size());
                }
                return messages.subList(0, limit);
            }
            return messages;
        }
        catch (NoSuchElementException ignored){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public String sendMessage(@RequestBody ChatMessage message){
        messenger.goToTheChat(message.chatId());
        try {
            messenger.sendMessage(message.message());
            return "success";
        }
        catch (NoSuchElementException ignored){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "chat not found");
        }
    }
}

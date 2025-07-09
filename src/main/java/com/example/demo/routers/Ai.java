package com.example.demo.routers;

import com.example.demo.messager.Messager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import com.example.demo.messager.classes.SimpleMessage;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
public final class Ai {
    private static final String AiChatId = "gigachat";
    private static Messager messager;

    private void goToAiChat(){
        if (!Objects.equals(messager.getCurrentChatId(), "gigachat"))
            messager.goToTheChat(AiChatId);
    }

    Ai() {
        messager = Messager.getInstance();
    }

    private String waitAi(String userMessage){
        while (true){
            try {
                String lastMsg = messager.getMessageText(messager.getLastMessage());
                if (!Objects.equals(lastMsg, "")
                        && !lastMsg.startsWith("Запрос принят")
                        && !Objects.equals(lastMsg, userMessage)){
                    System.out.println("Вероятно пришло сообщение от ai");
                    System.out.println("Сообщение             : \"" + lastMsg + "\"");
                    System.out.println("Сообщение пользователя: \"" + userMessage + "\"");
                    return lastMsg;
                }
            }
            catch (Exception ignored) {}
        }
    }

    @GetMapping("/pogoda")
    public String weather(@RequestParam String location) {
        goToAiChat();
        messager.waitInputField();
        String prompt = "Напиши актуальную погоду с сайта https://yandex.ru/pogoda/ru/" + location + " без форматирования и выделения текста. " +
                "Ответ должен включать:\n" +
                "1. Ветер\n" +
                "2. Атмосферное давление\n" +
                "3. Влажность воздуха\n" +
                "4. УФ-индекс\n" +
                "Также укажи температуру и осадки по пунктам:\n" +
                "- Утро\n" +
                "- День\n" +
                "- Вечер\n" +
                "- Ночь";

        messager.sendMessage(prompt);
        return waitAi(prompt);
    }

    @PostMapping("/messages")
    public SimpleMessage aiMessages(@RequestBody SimpleMessage userMessage){
        messager.goToTheChat("gigachat");
        if (userMessage.content().length() > 4096){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is too long");
        }
        messager.waitInputField();
        messager.sendMessage(userMessage.content());

        while(true){
            String lastMsg = messager.getMessageText(messager.getLastMessage());
            if (!lastMsg.startsWith("Запрос принят") && userMessage.content().compareTo(lastMsg) != 0){
                return new SimpleMessage(lastMsg);
            }
        }
    }
}


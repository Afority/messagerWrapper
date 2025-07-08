package com.example.demo;

import com.google.common.util.concurrent.AtomicDouble;
import org.openqa.selenium.WebElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@SpringBootApplication
public class DemoApplication {
    static Messager messager = null;
    private static int messageReadCounter = 0;

    record Message(Integer id, String message) {}

    @GetMapping("/messages")
    public String messages(@RequestParam(required = false) Integer id) {
//        if (id != null) {
//            if (id == -1) return messager.getMessageText(messager.getLastMessage());
//            return messager.getMessageText(messager.getMessages().get(id));
//        }
//
//        List<WebElement> messages = messager.getMessages();
//        List<>
//        for  (WebElement message : messages) {
//            messagesBuilder.append(messager.getMessageText(message));
//        }
        return "";
    }

    @GetMapping("/getMessages")
    public String index() {
        StringBuilder builder = new StringBuilder();
        for (int i = messageReadCounter; i < messager.getMessages().size(); ++i) {
            try {
                builder.append(messager.getMessageText(i) + "\n");
                messageReadCounter = ++i;
            }
            catch (Exception e) {continue;}
        }
        return builder.toString();
    }

    public record SimpleMessage(String content) { }

    @PostMapping("/sendMessage")
    public String sendMessage(@RequestBody SimpleMessage message){
        messager.sendMessage(message.content);
        return "success";
    }

    private void waitInputField(){
        while (true) {
            try{
                messager.getInputField();
                return;
            }
            catch (Exception e) {}
        }
    }

    @PostMapping("/api/ai/messages")
    public SimpleMessage aiMessages(@RequestBody SimpleMessage userMessage){
        messager.goToTheChat("gigachat");
//        todo
//        while (message.content.length() > 4096){
//
//        }
        waitInputField();
        messager.sendMessage(userMessage.content);
        while(true){
            String lastMsg = messager.getMessageText(messager.getLastMessage());
            if (!lastMsg.startsWith("Запрос принят") && userMessage.content.compareTo(lastMsg) != 0){
                return new SimpleMessage(lastMsg);
            }
        }
    }

    @GetMapping("/api/ai/pogoda")
    public SimpleMessage pogodaMessages(@RequestBody SimpleMessage userMessage){
        if (!messager.getCurrentUrl().endsWith("gigachat"))
            messager.goToTheChat("gigachat");

        waitInputField();
        String message = "Напиши погоду в " + userMessage.content + " ТОЛЬКО через сервис yandex.ru/pogoda без форматирования текста. " +
                "Ответ должен включать:" +
                "Ветер, Атмосферное давление, Влажность воздуха, УФ-индекс, все это по пунктам. " +
                "Утром, днем, вечером и ночью температура и осадки";
        messager.sendMessage(message);

        while (true){
            try {
                String lastMsg = messager.getMessageText(messager.getLastMessage());
                if (!lastMsg.startsWith("Запрос принят") && !lastMsg.startsWith("Напиши погоду в")){
                    // удаление строк с "Для ответа использовал актуальные интернет-источники"
                    int idxSources = lastMsg.indexOf("\n\n\n");
                    if (idxSources != -1){
                        return new SimpleMessage(lastMsg.substring(0, idxSources));
                    }
                    return new SimpleMessage(lastMsg);
                }
            }
            catch (Exception e) {continue;}
        }
    }

    public static void main(String[] args) {
        messager = new Messager();
        SpringApplication.run(DemoApplication.class, args);
    }
}

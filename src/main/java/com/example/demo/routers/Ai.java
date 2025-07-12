package com.example.demo.routers;

import com.example.demo.messager.Messenger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import com.example.demo.messager.classes.SimpleMessage;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
public final class Ai {
    private static final String AiChatId = "gigachat";
    private static Messenger messager;
    private static final String promptSuffix = "\nПиши ВСЕГДА без форматирования и выделения текста";

    private void goToAiChat(){
        if (!Objects.equals(messager.getCurrentChatId(), AiChatId))
            messager.goToTheChat(AiChatId);
    }

    Ai() {
        messager = Messenger.getInstance();
    }

    private String waitAi(String userMessage){
        while (true){
            try {
                String lastMsg = messager.getMessageText(messager.getLastMessage());
                if (!lastMsg.isEmpty()
                        && !lastMsg.startsWith("Запрос принят")
                        && !Objects.equals(lastMsg, userMessage)){
                    return lastMsg;
                }
            }
            catch (Exception ignored) {}
        }
    }

    @GetMapping("/pogoda")
    public String weather(@RequestParam String location) {
        goToAiChat();
        messager.waitTheInputField();
        String prompt = "Напиши подробный прогноз погоды на сегодня " +
                "используя данные с сайта https://yandex.ru/pogoda/ru/" + location + "/details/today\n" +
                "Формат строго следующий:\n" +
                "\nСегодня, [дата], в [название города]:\n" +
                "Сейчас [температура]°C, ощущается как +[ощущается]°C, ветер [скорость] м/с [направление], погода [описание].\n" +
                "\nУтро: температура [температура]°C, ветер [скорость] м/с, [направление], [описание].\n" +
                "\nДень: температура [температура]°C, ветер [скорость] м/с, [направление], [описание].\n" +
                "\nВечер: температура [температура]°C, ветер [скорость] м/с, [направление], [описание].\n" +
                "\nНочь: температура [температура]°C, ветер [скорость] м/с, [направление], [описание].\n" +
                "\nДополнительно:\n" +
                "\nВетер: [мин]-[макс] м/с, преимущественно [направление]." +
                "\nДавление: [число] мм рт. ст." +
                "\nВлажность: [мин]-[макс]%." +
                "\nУФ-индекс: [число], [уровень]." +
                "\nНе используй HTML, эмодзи, заглавные буквы в описаниях погоды. Не добавляй лишний текст. Только прогноз.";

        messager.sendMessage(prompt);
        return waitAi(prompt);
    }

    @PostMapping("/messages")
    public SimpleMessage aiMessages(@RequestBody SimpleMessage userMessage){
        goToAiChat();
        if (userMessage.content().length() > 4096 - promptSuffix.length()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is too long");
        }
        messager.waitTheInputField();
        messager.sendMessage(userMessage.content() + promptSuffix);

        return new SimpleMessage(waitAi(userMessage.content() + promptSuffix));
    }
}


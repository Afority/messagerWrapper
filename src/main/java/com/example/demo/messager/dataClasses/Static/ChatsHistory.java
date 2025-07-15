package com.example.demo.messager;

import com.example.demo.messager.dataClasses.ChatHistory;
import com.example.demo.messager.dataClasses.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatsHistory {
    static private final List<ChatHistory> chatHistoryList = new ArrayList<>();

    static void addChat(ChatHistory chatHistory){
        chatHistoryList.add(chatHistory);
    }

    static void updateMessage(int chatId, Message message){
        try {
            ChatHistory chatHistory = chatHistoryList.stream()
                    .filter((chat)-> chat.getChatId() == chatId)
                    .findFirst()
                    .orElse(null);

            if (chatHistory == null) {
                ChatHistory newChat = new ChatHistory(chatId);
                newChat.updateMessage(message);
                addChat(newChat);
            }
            else{
                chatHistory.updateMessage(message);
            }
        }
        catch (IndexOutOfBoundsException e){
            System.err.println(e.getMessage());
        }
    }
}

package com.example.demo;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.types.Pair;
import org.openqa.selenium.devtools.DevTools;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.events.CdpEventTypes;
import org.openqa.selenium.devtools.v123.network.Network;
import org.openqa.selenium.devtools.v123.network.model.WebSocketFrameReceived;
import org.openqa.selenium.devtools.DevTools;

import java.util.Optional;

public class Messager {
    private final ChromeDriver driver;

    public Messager(){
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=/home/user/.config/google-chrome/Default/");
        driver = new ChromeDriver(options);
        driver.get("https://web.max.ru/gigachat");

        DevTools devTools = driver.getDevTools();
        devTools.createSession();

        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        File file = new File("log.txt");
        file.delete();

        devTools.addListener(Network.webSocketFrameReceived(), (WebSocketFrameReceived event) -> {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            try(FileWriter writer = new FileWriter(file, true)) {
                writer.write(event.getResponse().getPayloadData());
                writer.write("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void quit(){
        driver.quit();
    }
    public String getCurrentUrl(){
        return driver.getCurrentUrl();
    }
    /*
     элемент с конкретным атрибутом
     xpath //div[@элемент='атрибут']
     css div[элемент='атрибут']

     Если любое значение
     xpath //div[@элемент]
     css div[элемент]

     Подстрока в атрибуте
     xpath //div[contains(@элемент, 'атрибут')]

     div с классом
     xpath //div[contains(@class, 'className')]

     */


    WebElement getInputField(){
        return driver.findElement(By.xpath("//div[@placeholder='Сообщение']"));
    }

    List<Pair<String, Integer>> getRecipients(){
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        return null;
    }

    void goToTheChat(String chatId){
        driver.get("https://web.max.ru/" + chatId);
    }

    void sendMessage(String message) {
        var inputField = getInputField();
        inputField.click();
        inputField.sendKeys(message);
        inputField.sendKeys(Keys.ENTER);
    }

    WebElement getHistory(){
        return driver.findElement(By.xpath("//div[contains(@class, 'history')]"));
    }

    List<WebElement> getMessages(){
        var messages = getHistory().findElement(By.xpath(".//div[contains(@class, 'content')]"));
        return messages.findElements(By.xpath("./div"));
    }

    WebElement getMessage(int messageId) {
        /*
        xpath to history  /html/body/div[4]/div[1]/div[3]/main/div[3]/div/div[2]
        xpath to messages /html/body/div[4]/div[1]/div[3]/main/div[3]/div/div[2]/div[2]/div[1]/div[1]/div
        xpath to message  /html/body/div[4]/div[1]/div[3]/main/div[3]/div/div[2]/div[2]/div[1]/div[1]/div/div[59]
        */
        return getHistory().findElement(By.xpath(".//div[contains(@class, 'item') and @data-index='" + messageId + "']"));
    }

    WebElement getLastMessage(){
        return getHistory().findElement(By.xpath(".//div[contains(@class, 'item')][last()]"));
    }

    String getMessageText(int messageId){
        return getMessageText(getMessage(messageId));
    }

    String getMessageText(WebElement message){
        return message
                .findElement(By.xpath(".//div[contains(@class, 'bubble')]"))
                .findElement(By.xpath("./span[contains(@class, 'text')]"))
                .getText();
    }

    String getTime(int messageId){
        return null;
        //return getMessage(messageId).findElement(By.xpath("./div/div/div[1]/div/div/div/div/span/div/span")).getText();
    }

    void scrollUp(){
        // todo
//        var history = driver.findElement(By.xpath("//div[contains(@class, 'history')]"));
//        var scrollableBox = history.findElement(By.xpath(".//div[contains(@class, 'scrollable')]"));
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        js.executeScript("arguments[0].scrollTop = 0;", scrollableBox);
    }
}

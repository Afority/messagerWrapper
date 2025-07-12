package com.example.demo.messager;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;

import java.util.ArrayList;
import java.time.Duration;
import java.util.Objects;
import java.util.List;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.demo.messager.classes.Message;

//import org.openqa.selenium.devtools.events.CdpEventTypes;
//import org.openqa.selenium.devtools.v123.network.Network;
//import org.openqa.selenium.devtools.v123.network.model.WebSocketFrameReceived;

// import java.util.Optional;

public final class Messenger {
    private static final String siteUrl = "https://web.max.ru";
    private static final String pathToGoogleChromeConfig = "user-data-dir=/home/user/.config/google-chrome/Default/";

    private static final Messenger INSTANCE = new Messenger();

    private final ChromeDriver driver;
    private DevTools devTools;

    public static Messenger getInstance() {
        return INSTANCE;
    }
    private Messenger() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(pathToGoogleChromeConfig);
        driver = new ChromeDriver(options);
        driver.get(siteUrl);

        devTools = driver.getDevTools();
        devTools.createSession();

//        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
//        File file = new File("log.txt");
//        file.delete();
//
//        devTools.addListener(Network.webSocketFrameReceived(), (WebSocketFrameReceived event) -> {
//            try {
//                file.createNewFile();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//                System.exit(1);
//            }
//            try(FileWriter writer = new FileWriter(file, true)) {
//                writer.write(event.getResponse().getPayloadData());
//                writer.write("\n");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
    }
    public void quit(){
        driver.quit();
    }

    public String getCurrentChatId(){
        try {
            String currUrl = driver.getCurrentUrl();
            int posToSlash = currUrl.lastIndexOf("/");
            return currUrl.substring(posToSlash+1);
        }
        catch (WebDriverException e){
            System.err.println("Ошибка получения url");
        }
        catch (Exception e){
            System.err.println("Ошибка получения chat id");
        }
        return null;
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


    public WebElement getInputField(){
        return driver.findElement(By.xpath("//div[@placeholder='Сообщение']"));
    }

//    public List<Pair<String, Integer>> getRecipients(){
//        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
//        return null;
//    }

    public void goToTheChat(String chatId){
        if (Objects.equals(getCurrentChatId(), chatId)) return;
        driver.get(siteUrl + "/" + chatId);
    }

    public void sendMessage(String message) {
        String[] lines = message.split("\n");
        var inputField = getInputField();

        inputField.click();

        for (String line : lines) {
            inputField.sendKeys(line, Keys.chord(Keys.SHIFT, Keys.ENTER));
        }
        inputField.sendKeys(Keys.ENTER);
    }

    public WebElement getChatHistory(){
        return driver.findElement(By.xpath("//div[contains(@class, 'history')]"));
    }

//    private List<WebElement> getMessages(){
//        var messages = getChatHistory().findElement(By.xpath(".//div[contains(@class, 'content')]"));
//        return messages.findElements(By.xpath("./div"));
//    }

    public List<Message> getAllMessagesInCurrentChat(){
        chatScrollUp();
        return getMessagesInCurrentChat();
    }

    public List<Message> getMessagesInCurrentChat(){
        List<WebElement> messages = getChatHistory()
                .findElement(By.xpath(".//div[contains(@class, 'content')]"))
                .findElements(By.xpath("./div"));

        List<Message> result = new ArrayList<>();
        for (int id = 0; id < messages.size(); ++id) {
            result.add(new Message(
                    id,
                    getMessageSender(messages.get(id)),
                    getMessageText(messages.get(id)),
                    getMessageTime(messages.get(id))
            ));
        }
        return result;
    }

    public WebElement getMessage(int messageId) {
        /*
        xpath to history  /html/body/div[4]/div[1]/div[3]/main/div[3]/div/div[2]
        xpath to messages /html/body/div[4]/div[1]/div[3]/main/div[3]/div/div[2]/div[2]/div[1]/div[1]/div
        xpath to message  /html/body/div[4]/div[1]/div[3]/main/div[3]/div/div[2]/div[2]/div[1]/div[1]/div/div[59]
        */
        return getChatHistory().findElement(By.xpath(".//div[contains(@class, 'item') and @data-index='" + messageId + "']"));
    }

    public WebElement getLastMessage(){
        return getChatHistory().findElement(By.xpath(".//div[contains(@class, 'item')][last()]"));
    }

    public String getMessageSender(WebElement message){
        try{
            message.findElement(By.xpath(".//div[contains(@class, 'bordersWrapper----left')]"));
            return "not me";
        }
        catch (NoSuchElementException ignored){}
        return "me";
    }

    public String getMessageText(int messageId){
        return getMessageText(getMessage(messageId));
    }

    public String getMessageTime(WebElement message){
        return message
                .findElement(By.xpath(".//div[contains(@class, 'bubble')]"))
                .findElement(By.xpath("./span[contains(@class, 'meta')]"))
                .getText();
        //return getMessage(messageId).findElement(By.xpath("./div/div/div[1]/div/div/div/div/span/div/span")).getText();
    }

    public String getMessageTime(int messageId){
        return null;
        //return getMessage(messageId).findElement(By.xpath("./div/div/div[1]/div/div/div/div/span/div/span")).getText();
    }

    public String getMessageText(WebElement message){
        return message
                .findElement(By.xpath(".//div[contains(@class, 'bubble')]"))
                .findElement(By.xpath("./span[contains(@class, 'text')]"))
                .getText();
    }

    public void chatScrollUp(){
        var history = getChatHistory();
        var scrollableBox = history
                .findElement(By.xpath(".//div[contains(@class, 'scrollable')]"));
        int messageCount = -1;
        while (messageCount != 0) {
            var messages = getMessagesInCurrentChat();
            if (messageCount != -1 && messageCount == messages.size()) {
                break;
            }
            messageCount = messages.size();
            driver.executeScript("arguments[0].scrollTop = 0;", scrollableBox);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

            try {
                wait.until(
                        ExpectedConditions.presenceOfNestedElementLocatedBy(
                                history,
                                By.xpath(".//div[contains(@class, 'item') and @data-index='" + (messageCount + 1) + "']")
                        )
                );
            }
            catch (TimeoutException e) {
                return;
            }
        }
    }
    public void waitTheInputField(){
        while (true) {
            try{
                getInputField();
                return;
            }
            catch (Exception e) {}
        }
    }

}

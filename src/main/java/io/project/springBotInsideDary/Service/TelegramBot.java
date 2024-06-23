package io.project.springBotInsideDary.Service;

import com.vdurmont.emoji.EmojiParser;
import io.project.springBotInsideDary.config.BotConfig;
import io.project.springBotInsideDary.model.User;
import io.project.springBotInsideDary.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    final BotConfig config;

    private static class UserInfo {
        boolean waitingForContactInfo;
        String lessonType;

        UserInfo(boolean waitingForContactInfo, String lessonType) {
            this.waitingForContactInfo = waitingForContactInfo;
            this.lessonType = lessonType;
        }
    }

    private Map<Long, UserInfo> userWaitingForContactInfo = new HashMap<>();

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand(Texts.START, Texts.START_COMMAND));
        listOfCommands.add(new BotCommand(Texts.MYDATA, Texts.GET_DATA));
        listOfCommands.add(new BotCommand(Texts.DELETEDATA, Texts.DELETE_MY_DATA));
        listOfCommands.add(new BotCommand(Texts.HELP, Texts.HELP_BUTTON));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error settings bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            Message message = update.getMessage();

            if (isWaitingForContactInfo(chatId)) {
                handleContactInfoInput(chatId, messageText, message);
            } else if (isAdminCommand(messageText, chatId)) {
                handleAdminCommand(messageText);
            } else {
                handleUserCommand(chatId, messageText, message);
            }
        }
    }

    private boolean isWaitingForContactInfo(long chatId) {
        return userWaitingForContactInfo.containsKey(chatId) && userWaitingForContactInfo.get(chatId).waitingForContactInfo;
    }

    private void handleContactInfoInput(long chatId, String messageText, Message message) {
        if (isCommandButton(messageText)) {
            updateLessonType(chatId, messageText);
        } else if (messageText.startsWith("/")) {
            sendMessage(chatId, "Пожалуйста, введи свои контактные данные");
        } else {
            String lessonType = userWaitingForContactInfo.get(chatId).lessonType;
            sendToMyself("Контактные данные от " + message.getFrom().getUserName() + " (" + chatId + ")\n" + lessonType + ":\n" + messageText);
            userWaitingForContactInfo.remove(chatId);
            sendMessage(chatId, Texts.COMMUNICATE_ALERT);
        }
    }

    private boolean isCommandButton(String messageText) {
        return messageText.equals(Texts.BOARD_JOIN_GROUP) || messageText.equals(Texts.BOARD_INDIVIDUAL_LESSONS) ||
                messageText.equals(Texts.BOARD_PRICE) || messageText.equals(Texts.BOARD_FREE_LESSONS) ||
                messageText.equals(Texts.TRAINING);
    }

    private void updateLessonType(long chatId, String messageText) {
        userWaitingForContactInfo.remove(chatId); // Удаляем старую запись, так как она больше не актуальна

        switch (messageText) {
            case Texts.BOARD_JOIN_GROUP:
                userWaitingForContactInfo.put(chatId, new UserInfo(true, "⚠\uFE0FГрупповые занятия"));
                sendMessage(chatId, Texts.INFO_GROUP_LESSONS);
                break;
            case Texts.BOARD_INDIVIDUAL_LESSONS:
                userWaitingForContactInfo.put(chatId, new UserInfo(true, "❗\uFE0FИндивидуальные уроки"));
                sendMessage(chatId, Texts.INFO_INDIVIDUAL_LESSONS);
                break;
            case Texts.BOARD_PRICE:
                sendMessage(chatId, Texts.INFO_PRICE);
                break;
            case Texts.BOARD_FREE_LESSONS:
                sendMessage(chatId, Texts.SEND_LINK1);
                linkForSocial(chatId, Texts.SEND_BUTTON, Texts.SEND_LINK_PAGE);
                break;
            case Texts.TRAINING:
                sendMessage(chatId, Texts.TRAINING_BOX);
                sendDocumentMessage(chatId);
                break;
        }
    }

    private boolean isAdminCommand(String messageText, long chatId) {
        return messageText.contains("/send") && config.getOwnerID() == chatId;
    }

    private void handleAdminCommand(String messageText) {
        var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
        var users = userRepository.findAll();
        for (User user : users) {
            sendMessage(user.getChatId(), textToSend);
        }
    }

    private void handleUserCommand(long chatId, String messageText, Message message) {
        switch (messageText) {
            case Texts.START:
                registerUser(message);
                startCommandReceived(chatId, message.getChat().getFirstName());
                break;
            case Texts.DELETEDATA:
                deleteUser(chatId);
                sendMessage(chatId, Texts.INFO_DELETE_DATA);
                break;
            case Texts.MYDATA:
                checkDataUser(chatId);
                break;
            case Texts.HELP:
                sendMessage(chatId, Texts.HELP_COMMAND);
                break;
            case Texts.BOARD_PRICE:
                sendMessage(chatId, Texts.INFO_PRICE);
                break;
            case Texts.BOARD_JOIN_GROUP:
                userWaitingForContactInfo.put(chatId, new UserInfo(true, "⚠\uFE0FГрупповые занятия"));
                sendMessage(chatId, Texts.INFO_GROUP_LESSONS);
                break;
            case Texts.BOARD_INDIVIDUAL_LESSONS:
                userWaitingForContactInfo.put(chatId, new UserInfo(true, "❗\uFE0FИндивидуальные уроки"));
                sendMessage(chatId, Texts.INFO_INDIVIDUAL_LESSONS);
                break;
            case Texts.BOARD_FREE_LESSONS:
                sendMessage(chatId, Texts.SEND_LINK1);
                linkForSocial(chatId, Texts.SEND_BUTTON, Texts.SEND_LINK_PAGE);
                break;
            case Texts.TRAINING:
                sendMessage(chatId, Texts.TRAINING_BOX);
                sendDocumentMessage(chatId);
                break;
            default:
                sendMessage(chatId, Texts.ERROR_COMMAND);
        }
    }

    private void sendToMyself(String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(config.getOwnerID()));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void linkForSocial(long chatId, String buttonText, String URL) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(Texts.SEND_TEXT_LINK);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setUrl(URL);
        rowInline.add(button);

        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred " + e.getMessage());
        }
    }

    private void sendDocumentMessage(long chatId) {
        // Указываем путь к PDF файлу
        String pdfFile = "http://dance-inside.ru/training.pdf";

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));
        sendDocument.setDocument(new InputFile(pdfFile));
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }

    private void deleteUser(long chatId) {
        if (userRepository.existsById(chatId)) {
            userRepository.deleteById(chatId);
            log.info("User with chatId " + chatId + " has been deleted.");
        } else {
            log.info("User with chatId " + chatId + " not found in the database.");
        }
    }

    private void checkDataUser(long chatId) {
        User user = userRepository.findById(chatId).orElse(null);
        if (user != null) {
            String data = String.format(Texts.CHECK_DATA_USER,
                    user.getFirstName(), user.getLastName(), user.getUserName(), user.getRegisteredAt());
            sendMessage(chatId, data);
        } else {
            sendMessage(chatId, Texts.INFO_MYDATA_USER);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет! " + name + Texts.INTRODUCTION_MESSAGE;
        log.info("Replied to user: " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(Texts.BOARD_PRICE);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(Texts.BOARD_JOIN_GROUP);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(Texts.BOARD_INDIVIDUAL_LESSONS);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(Texts.BOARD_FREE_LESSONS);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(Texts.TRAINING);
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred " + e.getMessage());
        }
    }
}
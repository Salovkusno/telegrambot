package io.project.springBotInsideDary.Service;

public class Texts {
    public static final String START = "/start";
    public static final String MYDATA = "/mydata";
    public static final String DELETEDATA = "/deletedata";
    public static final String HELP = "/help";
    public static final String START_COMMAND = "Перезапуск бота";
    public static final String GET_DATA = "Ваши данные";
    public static final String DELETE_MY_DATA = "Удалить мои данные";
    public static final String INTRODUCTION_MESSAGE = ", рада тебя видеть!\n" +
            "Этот бот поможет тебе быстро записаться на мои занятия,\n" +
            "а так же здесь ты сможешь найти полезные видео-уроки\n" +
            "с разбором моих связок";
    public static final String HELP_COMMAND = "Привет! Я рада помочь вам использовать этого бота. Вот список доступных команд и их назначение:\n\n" +
            "/start - Начать работу с ботом и зарегистрироваться.\n" +
            "/mydata - Посмотреть ваши сохраненные данные.\n" +
            "/deletedata - Удалить ваши данные из нашей базы данных.\n" +
            "/help - Показать это сообщение с информацией о командах.\n\n" +
            "Вы также можете использовать кнопки на клавиатуре для выполнения различных действий:\n" +
            "- \"Запись в группу\" - Записаться на групповые занятия.\n" +
            "- \"Индивидуальные уроки\" - Записаться на индивидуальные уроки.\n" +
            "- \"Бесплатные видео-уроки\" - Получить доступ к бесплатным видео-урокам.\n\n" +
            "Если у вас возникнут вопросы или проблемы, не стесняйтесь обращаться за помощью!\n\n" +
            "Спасибо, что используете нашего бота!";

    public static final String CHECK_DATA_USER = "Твои данные: \nИмя: %s\nФамилия: %s\nИмя пользователя: %s\nДата регистрации: %s";
    public static final String COMMUNICATE_ALERT = "Отлично!\nСовсем скоро я свяжусь с тобой!";
    public static final String HELP_BUTTON = "Как пользоваться ботом";
    public static final String ERROR_COMMAND = "Команда пока не поддерживается.";

    public static final String SEND_LINK1 = "https://www.youtube.com/watch?v=o5Ou1xeIpWk";
    public static final String SEND_LINK_PAGE = "https://www.youtube.com/@Insidedary";
    public static final String SEND_BUTTON = "Видео-Уроки";
    public static final String SEND_TEXT_LINK = "Еще больше видео смотри на моем канале";

    public static final String BOARD_PRICE = "Стомость занятий";
    public static final String BOARD_JOIN_GROUP = "Записаться в группу";
    public static final String BOARD_INDIVIDUAL_LESSONS = "Запись на индивиудальное занятие";
    public static final String BOARD_FREE_LESSONS = "Бесплатные видео-уроки";
    public static final String TRAINING = "ТРЕНАЖЕР\uD83D\uDC8E";
    public static final String TRAINING_BOX = "А вот и тренажер \uD83E\uDD29 \n\n" +
            "Я очень рада, что ты начинаешь свой танцевальный путь со мной, прочитай до конца, ведь после прочтения тебя ждёт щедрый подарок \uD83C\uDF81";
    public static final String INFO_PRICE ="\uD83D\uDC8EСтоимость абонементов на групповые тренировки:\n" +
            "Разовое занятие 650р\n" +
            "Абонемент х2 - 1300р\n" +
            "Абонемент х4 - 2400р\n" +
            "Абонемент х6 - 3500р\n" +
            "Абонемент х8 - 4500р\n" +
            "\n" +
            "\uD83D\uDC8EСтоимость индивидуального занятия - 2000р/час";
    public static final String INFO_INDIVIDUAL_LESSONS = "Для записи на индивидуальное занятие:\n"
            + "1) Фамилия Имя\n"
            + "2) Контактный номер телефона\n"
            + "3) Есть ли танцевальный опыт\n\n\n"
            + "* Плюсы индивидулаьной тренировки: \n"
            + "⚡\uFE0F Удобное время тренировки\n"
            + "⚡\uFE0F Возможность выбора любимой музыки\n"
            + "⚡\uFE0F Индивидуальный подход\n"
            + "⚡\uFE0F Стопроцентное внимание тренера";
    public static final String INFO_GROUP_LESSONS = "Для записи в группу укажи:\n"
            + "1) Фамилия Имя\n"
            + "2) Контактный номер телефона";

    public static final String INFO_DELETE_DATA = "Твои данные были удалены";
    public static final String INFO_MYDATA_USER = "Твои данные не найдены в базе данных";

}


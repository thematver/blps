import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bot {
    private final TelegramBot bot;
    private final Logger logger;

    public Bot(String token) {
        this.bot = new TelegramBot(token);
        this.logger = Logger.getLogger("Main");
        new Thread(this::startRabbitConsumer).start();

        bot.setUpdatesListener(updates -> {
            for (Update update:
                 updates) {
                handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            if (e.response() != null) {
                e.response().errorCode();
                e.response().description();
            } else {
                e.printStackTrace();
            }
        });
    }

    private void handleUpdate(Update update) {
        logger.log(Level.INFO, update.toString());
        long chatId = update.message().chat().id();
        UserStateResult userStateResult = SQLHelper.checkUser(String.valueOf(chatId));
        String message = "";
        switch (userStateResult.getUserState()) {

            case INITIAL -> {
                String uuid = SQLHelper.addUser(String.valueOf(chatId));
                String link = "http://127.0.0.1:80/api/users/link?hash=" + uuid;
                message = "Перейдите [по ссылке]("+link+") для авторизации.";
            }
            case HAS_UUID -> {
                String link = "http://127.0.0.1:80/api/users/link?hash=" + userStateResult.getUuid();
                message = "Перейдите [по ссылке]("+link+") для авторизации.";
            }
            case AUTHORIZED -> {
                message = "Больше ничего не нужно писать. Я сам отправлю тебе новости, когда они появятся.";
            }
            case BLOCKED -> {
                message = "Вы уволены.";
            }
        }

       bot.execute(new SendMessage(chatId, message).parseMode(ParseMode.Markdown));
    }

    private void startRabbitConsumer() {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri("..");
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare("linking", false, false, false, null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String receivedMessage = new String(delivery.getBody(), "UTF-8");

                // Process the received message
                handleReceivedRabbitMessage(receivedMessage);
            };

            channel.basicConsume("linking", true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error starting RabbitMQ consumer", e);
        }
    }

    private void handleReceivedRabbitMessage(String receivedMessage) {
        // Split the message based on ": "
        String[] parts = receivedMessage.split(": ", 2); // Limit to 2 parts

        if (parts.length != 2) {
            System.err.println("Received invalid message format from RabbitMQ: " + receivedMessage);
            return;
        }

        // Extract springId and uuid
        String springId = parts[0];
        String uuid = parts[1];

        // Use the addUser method or equivalent to link the user
       String chatId = SQLHelper.addUser(springId, uuid); // Assuming this method adds the user to the database and returns a success status


        if (chatId != null) {


            // Send a message to the user
            String message = "Успешно подключили аккаунт!";
            bot.execute(new SendMessage(chatId, message));
        } else {
            System.err.println("Failed to link the user with springId: " + springId);
        }
    }


}

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bot {
    private final TelegramBot telegramBot;
    private final Logger logger;

    private final String brokerUrl = System.getenv("MQTT_BROKER_URL");
    private final String clientId = System.getenv("MQTT_CLIENT_ID");
    private final String mqttUsername = System.getenv("MQTT_USERNAME");
    private final String mqttPassword = System.getenv("MQTT_PASSWORD");

    public Bot(String token) {
        this.telegramBot = new TelegramBot(token);
        this.logger = Logger.getLogger(Bot.class.getName());
        new Thread(this::startRabbitConsumer).start();

        telegramBot.setUpdatesListener(this::handleUpdates, this::handleException);
    }

    private int handleUpdates(List<Update> updates) {
        updates.forEach(this::handleUpdate);
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleUpdate(Update update) {
        Preconditions.checkNotNull(update, "Update should not be null");
        logger.log(Level.INFO, "Got update with id: {}", update.updateId());
        long chatId = update.message().chat().id();
        UserStateResult userStateResult = SQLHelper.checkUser(String.valueOf(chatId));
        String message = getMessageByUserState(userStateResult);
        telegramBot.execute(new SendMessage(chatId, message).parseMode(ParseMode.Markdown));
    }

    private String getMessageByUserState(UserStateResult userStateResult) {
        return switch (userStateResult.getUserState()) {
            case INITIAL ->
                    String.format("Вставьте код в текстовое поле: `%s`", SQLHelper.addUser(userStateResult.getUuid()));
            case HAS_UUID -> String.format("Вставьте код в текстовое поле: `%s`", userStateResult.getUuid());
            case AUTHORIZED -> "Больше ничего не нужно писать. Я сам отправлю тебе новости, когда они появятся.";
            case BLOCKED -> "Вы уволены.";
        };
    }

    private void startRabbitConsumer() {
        try (MqttClient mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence())){

            MqttConnectOptions connOpts = configureMqttConnection();

            mqttClient.connect(connOpts);
            subscribeToTopics(mqttClient);

        } catch (MqttException e) {
            handleMqttException(e);
        }
        logger.log(Level.INFO, "RabbitMQ готов.");
    }

    private MqttConnectOptions configureMqttConnection() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(mqttUsername);
        connOpts.setPassword(mqttPassword.toCharArray());
        connOpts.setCleanSession(true);
        return connOpts;
    }

    private void subscribeToTopics(MqttClient mqttClient) throws MqttException {
        mqttClient.subscribe("linking", 1, (topic, message) -> handleReceivedLinkingMessage(new String(message.getPayload(), StandardCharsets.UTF_8)));
        mqttClient.subscribe("notification", 1, (topic, message) -> handleReceivedNotificationMessage(new String(message.getPayload(), StandardCharsets.UTF_8)));
    }

    private void handleReceivedLinkingMessage(String receivedMessage) {
        String[] parts = receivedMessage.split(": ", 2);

        if (parts.length != 2) {
            logger.log(Level.WARNING, "Received invalid message format from RabbitMQ: {}", receivedMessage);
            return;
        }

        String springId = parts[0];
        String uuid = parts[1];

        String chatId = SQLHelper.addUser(springId, uuid);

        if (chatId != null) {
            telegramBot.execute(new SendMessage(chatId, "Успешно подключили аккаунт!"));
        } else {
            logger.log(Level.WARNING, "Failed to link the user with springId: {}", springId);
        }
    }

    private void handleReceivedNotificationMessage(String receivedMessage) {
        List<String> ids = SQLHelper.getUserIds();

        for (String id : ids) {
            if (id != null) {
                String message = String.format("Вчера мы получили %s комментариев. Поревьюим?", receivedMessage);
                telegramBot.execute(new SendMessage(id, message));
            } else {
                logger.log(Level.WARNING, "Failed to send message to user. No telegram id");
            }
        }
    }

    private void handleException(Exception e) {
        if (e instanceof MqttException mqttException) {
            handleMqttException(mqttException);
        } else {
            logger.log(Level.SEVERE, "Unhandled exception occurred: {} ",  e.getMessage());
        }
    }

    private void handleMqttException(MqttException e) {
        logger.log(Level.SEVERE, "Ошибка при запуске слушателя RabbitMQ", e);
    }
}
package xyz.anomatver.blps.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
@Service
public class MessageSenderService {

    @Value("${BROKER_URL}")
    String brokerUrl;

    String clientId = "BLPS_Spring";

    @Value("${MQTT_USERNAME}")
    String mqttUsername;

    @Value("${MQTT_PASSWORD}")
    String mqttPassword;

    @Value("${mqtt.linking.topic}")
    String linkingTopic;

    @Value("${mqtt.notification.topic}")
    String notificationTopic;

    private void sendMessage(String topic, String payload) {

        try {
            MqttClient sampleClient = new MqttClient(brokerUrl, clientId);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(mqttUsername);
            connOpts.setPassword(mqttPassword.toCharArray());

            connOpts.setCleanSession(true);

            sampleClient.connect(connOpts);

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(2);
            sampleClient.publish(topic, message);

            sampleClient.disconnect();
        } catch (MqttException me) {
            throw new RuntimeException(me);
        }
    }

    public void sendLinkingMessage(String userId, String uuid) {
        sendMessage(linkingTopic, userId + ": "  + uuid);
    }

    public void sendYesterdayNotificationMessage(String payload) {
        sendMessage(notificationTopic, payload);
    }


}
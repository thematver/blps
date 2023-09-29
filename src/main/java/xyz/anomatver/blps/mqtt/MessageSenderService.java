package xyz.anomatver.blps.mqtt;

import org.springframework.stereotype.Service;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
@Service
public class MessageSenderService {

    String broker = "tcp://92.63.176.162:1883";
    String clientId = "BLPS_Spring";

    private void sendMessage(String topic, String payload) {

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("rmuser");
            connOpts.setPassword("rmpassword".toCharArray());

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
        sendMessage("linking", userId + ": "  + uuid);
    }

    public void sendYesterdayNotificationMessage(String payload) {
        sendMessage("notification", payload);
    }


}
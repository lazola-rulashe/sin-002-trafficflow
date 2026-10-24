package co.wethinkcode.trafficflow.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import javax.jms.*;

public class HeartbeatPublisher {
    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    public HeartbeatPublisher() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        this.connection = factory.createConnection();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(MqConfig.HEARTBEAT_QUEUE);
        this.producer = session.createProducer(queue);
        this.connection.start();
    }

    public void publishHeartbeat() throws JMSException {
        JSONObject message = new JSONObject();
        message.put("timestamp", System.currentTimeMillis());
        message.put("status", "alive");
        TextMessage textMessage = session.createTextMessage(message.toString());
        producer.send(textMessage);
    }

    public void close() throws JMSException {
        producer.close();
        session.close();
        connection.close();
    }
}

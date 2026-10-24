package co.wethinkcode.trafficflow.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import javax.jms.*;

public class CongestionPublisher {
    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    public CongestionPublisher() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        this.connection = factory.createConnection();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(MqConfig.TOPIC);
        this.producer = session.createProducer(topic);
        this.connection.start();
    }

    public void publishCongestionLevel(int level) throws JMSException {
        JSONObject message = new JSONObject();
        message.put("level", level);
        TextMessage textMessage = session.createTextMessage(message.toString());
        producer.send(textMessage);
    }

    public void close() throws JMSException {
        producer.close();
        session.close();
        connection.close();
    }
}

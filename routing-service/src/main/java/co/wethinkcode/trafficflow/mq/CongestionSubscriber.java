package co.wethinkcode.trafficflow.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import javax.jms.*;

public class CongestionSubscriber {
    private final Connection connection;
    private final Session session;
    private final MessageConsumer consumer;

    public CongestionSubscriber(MessageListener listener) throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        this.connection = factory.createConnection();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(MqConfig.TOPIC);
        this.consumer = session.createConsumer(topic);
        this.consumer.setMessageListener(listener);
        this.connection.start();
    }

    public void close() throws JMSException {
        consumer.close();
        session.close();
        connection.close();
    }
}

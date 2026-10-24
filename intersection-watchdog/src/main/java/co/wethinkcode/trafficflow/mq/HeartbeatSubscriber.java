package co.wethinkcode.trafficflow.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import javax.jms.*;

public class HeartbeatSubscriber {
    private final Connection connection;
    private final Session session;
    private final MessageConsumer consumer;

    public HeartbeatSubscriber(MessageListener listener) throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        this.connection = factory.createConnection();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(MqConfig.HEARTBEAT_QUEUE);
        this.consumer = session.createConsumer(queue);
        this.consumer.setMessageListener(listener);
        this.connection.start();
    }

    public void close() throws JMSException {
        consumer.close();
        session.close();
        connection.close();
    }
}

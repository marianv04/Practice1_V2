package dacd.navarro;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class AMQTopicHotelSubscriber implements Subscriber, MessageListener {
    private static String url;
    private static String topicName;

    private static Connection connection;
    private static Session session;
    private static String subscriberName;
    private static String path;

    public AMQTopicHotelSubscriber(String subscriberName, String topicName, String path) throws JMSException {
        this.subscriberName = subscriberName;
        this.topicName = topicName;
        this.path = path;

        url = ActiveMQConnection.DEFAULT_BROKER_URL;

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connection = connectionFactory.createConnection();
        connection.setClientID(subscriberName);
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void subscribeToTopic() throws JMSException {
        Topic topic = session.createTopic(topicName);

        MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberName);
        consumer.setMessageListener(this);
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String response = ((TextMessage) message).getText();
                System.out.println("Received = " + response);
                FileEventStore eventStore = new FileEventStore();
                eventStore.consume(response, topicName, path);

                if (response.equalsIgnoreCase("Quit")) {
                    connection.close();
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

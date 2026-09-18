package keywords

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

import java.time.Duration
import java.util.Properties

class KafkaHelper {

    static final String BOOTSTRAP_SERVER = 'localhost:9092'

    /**
     * Create Kafka Producer
     */
    static KafkaProducer<String, String> createProducer() {

        Properties props = new Properties()

        props.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            BOOTSTRAP_SERVER
        )

        props.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName()
        )

        props.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName()
        )

        return new KafkaProducer<String, String>(props)
    }


    /**
     * Send message to Kafka topic
     */
    static void sendMessage(
        KafkaProducer<String, String> producer,
        String topic,
        String message
    ) {

        ProducerRecord<String, String> record =
            new ProducerRecord<String, String>(
                topic,
                message
            )

        producer.send(record).get()

        println("Message sent to topic: ${topic}")
        println("Message: ${message}")
    }


    /**
     * Create Kafka Consumer
     */
    static KafkaConsumer<String, String> createConsumer(
        String groupId
    ) {

        Properties props = new Properties()

        props.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            BOOTSTRAP_SERVER
        )

        props.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            groupId
        )

        props.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName()
        )

        props.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName()
        )

        props.put(
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
            'earliest'
        )

        return new KafkaConsumer<String, String>(props)
    }


    /**
     * Consume message from Kafka topic
     */
    static String consumeMessage(
        KafkaConsumer<String, String> consumer,
        String topic
    ) {

        consumer.subscribe([topic])

        long timeout = System.currentTimeMillis() + 10000

        while (System.currentTimeMillis() < timeout) {

            ConsumerRecords<String, String> records =
                consumer.poll(Duration.ofMillis(1000))

            if (!records.isEmpty()) {

                String message =
                    records.iterator().next().value()

                println("Message received from topic: ${topic}")
                println("Message: ${message}")

                return message
            }
        }

        println("No message received within timeout.")

        return null
    }
}
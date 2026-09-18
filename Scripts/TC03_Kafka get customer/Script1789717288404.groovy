import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import keywords.KafkaHelper


// ==================================================
// 1. Get customer data from Reqres
// ==================================================

def response = WS.sendRequest(
    findTestObject(
        'Object Repository/GET USER',
        [('page') : page]
    )
)

WS.verifyResponseStatusCode(response, 200)

def responseJson =
    new JsonSlurper().parseText(
        response.getResponseText()
    )

def users = responseJson.data

assert users != null
assert users.size() > 0

println("Total users retrieved from Reqres: ${users.size()}")


// ==================================================
// 2. Prepare batch customer data
// ==================================================

def batchUsers = users.take(3)

assert batchUsers.size() == 3

println("Batch size: ${batchUsers.size()}")


// ==================================================
// 3. Create Kafka Producer
// ==================================================

def producer = KafkaHelper.createProducer()

String topic = 'customer-topic'


// ==================================================
// 4. Send customer batch to Kafka
// ==================================================

batchUsers.each { user ->

    def customerMessage = [
        id        : user.id,
        first_name: user.first_name,
        last_name : user.last_name,
        email     : user.email
    ]

    String message = JsonOutput.toJson(customerMessage)

    println("Sending customer: ${message}")

    KafkaHelper.sendMessage(
        producer,
        topic,
        message
    )
}

producer.close()


// ==================================================
// 5. Create Kafka Consumer
// ==================================================

def consumer = KafkaHelper.createConsumer(
    'katalon-customer-test'
)


// ==================================================
// 6. Consume messages
// ==================================================

def receivedMessages = []

for (int i = 0; i < batchUsers.size(); i++) {

    String receivedMessage =
        KafkaHelper.consumeMessage(
            consumer,
            topic
        )

    assert receivedMessage != null

    receivedMessages.add(receivedMessage)
}

consumer.close()


// ==================================================
// 7. Validate received messages
// ==================================================

assert receivedMessages.size() == batchUsers.size()

batchUsers.each { user ->

    boolean messageFound =
        receivedMessages.any { message ->

            message.contains("\"id\":${user.id}") &&
            message.contains(user.first_name) &&
            message.contains(user.last_name) &&
            message.contains(user.email)
        }

    assert messageFound

    println(
        "Customer ${user.id} validation PASSED"
    )
}


// ==================================================
// 8. Final result
// ==================================================

println(
    "TC02_KAFKA_BATCH_CUSTOMER PASSED"
)
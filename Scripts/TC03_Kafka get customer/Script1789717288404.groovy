import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonSlurper
import keywords.KafkaHelper // Mengimpor Helper Kafka yang sudah Anda buat

// ========================================================
// TAHAP 1: EKSEKUSI API REQRES UNTUK MENDAPATKAN DATA USER
// ========================================================
KeywordUtil.logInfo("Tahap 1: Mengirim request POST ke ReqRes API...")
def response = WS.sendRequest(findTestObject('POST CREATE USER'))

// Memastikan status code sukses 201 Created (Sesuai objek verifikasi Anda)
WS.verifyResponseStatusCode(response, 201)

// Ekstrak nama user dari response JSON (Hasilnya: "Test Rere")
def jsonResponse = new JsonSlurper().parseText(response.getResponseBodyContent())
String createdName = jsonResponse.name
KeywordUtil.logInfo("User berhasil dibuat via API dengan nama: " + createdName)

// ========================================================
// TAHAP 2: SIMULASI PENGIRIMAN DATA KE KAFKA TOPIC (PRODUCER)
// ========================================================
KeywordUtil.logInfo("Tahap 2: Mengirim payload data user baru ke Kafka Topic...")
String topicName = "ifg-customer-topic"

// Membuat producer dan mengirim pesan menggunakan KafkaHelper Anda
def myProducer = KafkaHelper.createProducer()
KafkaHelper.sendMessage(myProducer, topicName, "Payload sync untuk customer baru: " + createdName)

// ========================================================
// TAHAP 3: AMBIL DAN VALIDASI DATA DARI KAFKA (CONSUMER)
// ========================================================
KeywordUtil.logInfo("Tahap 3: Membaca data stream dari Kafka Consumer...")

// Membuat consumer dan menarik pesan menggunakan KafkaHelper Anda
def myConsumer = KafkaHelper.createConsumer("ifg-qa-automation-group")
String receivedMessage = KafkaHelper.consumeMessage(myConsumer, topicName)

// DEEP ASSERTION: Memastikan pesan dari Kafka mengandung nama user dari API ReqRes
if (receivedMessage != null && receivedMessage.contains(createdName)) {
    KeywordUtil.logInfo("PENGUJIAN BERHASIL: Integrasi REST API dan Apache Kafka berjalan sempurna!")
} else {
    KeywordUtil.markFailed("PENGUJIAN GAGAL: Pesan yang diterima dari Kafka tidak sesuai atau kosong.")
}

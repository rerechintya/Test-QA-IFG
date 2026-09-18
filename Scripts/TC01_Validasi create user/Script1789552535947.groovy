import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.llm.keyword.LlmKeywords as LLM
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys
import groovy.json.JsonSlurper as JsonSlurper

// 1. Eksekusi request API
response = WS.sendRequest(findTestObject('POST CREATE USER'))

// 2. Validasi status code sukses 201 Created
WS.verifyResponseStatusCode(response, 201)

// 3. Ambil response body content dan ubah ke objek JSON
String responseBody = response.getResponseBodyContent()
def jsonResponse = new JsonSlurper().parseText(responseBody)

// 4. VERIFIKASI NILAI KONTEN (Menggunakan logika Groovy langsung, anti-error)
// Memastikan nilai nama dan pekerjaan yang dikembalikan server sesuai dengan input asli Anda
WS.verifyMatch(jsonResponse.name, 'Test Rere', false)
WS.verifyMatch(jsonResponse.job, 'QA', false)

// 5. VALIDASI KEBERADAAN ID
// Memastikan properti 'id' berhasil digenerate oleh server dan tidak kosong
assert jsonResponse.id != null
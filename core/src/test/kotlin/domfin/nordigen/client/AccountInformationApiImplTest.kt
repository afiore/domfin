package domfin.nordigen.client

import domfin.nordigen.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

class AccountInformationApiImplTest {
    val testToken = "test-token"
    private val jsonElementLens: BiDiBodyLens<JsonElement> = Body.auto<JsonElement>().toLens()

    @Test
    fun `get institutions by country code`() {
        val jsonResponses = mapOf(
            "gb" to Json.parseToJsonElement(
                """[
               {"id": "X", "name": "bank-X", "transaction_total_days": 13},
               {"id": "Y", "name": "bank-Y", "transaction_total_days": 205},
               {"id": "Z", "name": "bank-Z", "transaction_total_days": 50}
            ]
            """.trimIndent(),
            ),
            "es" to Json.parseToJsonElement(
                """[
               {"id": "A", "name": "bank-A", "transaction_total_days": 2}
            ]
                """.trimIndent()
            )
        )

        val handler: HttpHandler = { request ->
            assertEquals(request.method, Method.GET)
            assertEquals(request.uri.path, "/api/v2/institutions/")

            val country = request.query("country")
            val institutions = jsonResponses.get(country)

            institutions?.let {
                jsonElementLens.inject(it, Response(Status.OK))
            } ?: Response(Status.NOT_FOUND)
        }


        val api = AccountInformationApiImpl(ServerFilters.BearerAuth(testToken).then(handler), testToken)

        runBlocking {
            assertEquals(
                listOf(
                    Institution("X", "bank-X", 13),
                    Institution("Y", "bank-Y", 205),
                    Institution("Z", "bank-Z", 50)
                ), api.getInstitutions("gb")
            )
            assertEquals(
                listOf(
                    Institution("A", "bank-A", 2),
                ), api.getInstitutions("es")
            )
        }
    }

    @Test
    fun `get a single requisition`() {
        val requisitionId = "test-requisition-id"
        val testResponse = Json.parseToJsonElement(
            """
            {"id":"$requisitionId", "accounts": ["account-1", "account-2"], "status": "LN"}
        """.trimIndent()
        )

        val handler: HttpHandler = { request ->
            assertEquals(request.method, Method.GET)
            assertEquals("/api/v2/requisitions/$requisitionId/", request.uri.path)
            jsonElementLens.inject(testResponse, Response(Status.OK))
        }

        val api = AccountInformationApiImpl(handler, testToken)

        runBlocking {
            assertEquals(
                Requisition(requisitionId, listOf("account-1", "account-2"), "LN"),
                api.getRequisition(requisitionId)
            )
        }
    }

    @Test
    fun `create a requisition`() {
        val institutionId = "some-bank-id"
        val requisitionId = UUID.randomUUID().toString()
        val redirectUrl = "http://example.com/page"
        val link = "https://ob.nordigen.com/psd2/start/3fa85f64-5717-4562-b3fc-2c963f66afa6/{$institutionId}"
        val testResponse = Json.parseToJsonElement(
            """
            {"id": "$requisitionId", "link": "$link"}
        """.trimIndent()
        )


        val handler: HttpHandler = { request ->
            assertEquals(request.method, Method.POST)
            assertEquals("/api/v2/requisitions/", request.uri.path)

            val requisitionRequest = RequisitionRequest.lens.get(request)
            assertEquals(institutionId, requisitionRequest.institutionId)
            assertEquals(redirectUrl, requisitionRequest.redirect)

            jsonElementLens.inject(testResponse, Response(Status.OK))
        }

        val api = AccountInformationApiImpl(handler, testToken)

        runBlocking {
            assertEquals(
                RequisitionResponse(
                    requisitionId,
                    link
                ),
                api.createRequisition(RequisitionRequest(redirectUrl, institutionId))
            )
        }
    }

    @Test
    fun `get all the requisitions`() {
        fun pagedResponse(offset: Int): JsonElement {
            val next =
                if (offset < 3)
                    "http://example/some-endpoint?offset=${offset + 1}"
                else
                    null

            return Json.parseToJsonElement(
                """
                {"count": 3, "next": "$next", "results": [
                  {"id": "id-$offset", "accounts":["account-00$offset"], "status": "GA"}
                ]}
            """.trimIndent()
            )
        }

        val handler: HttpHandler = { request ->
            assertEquals("/api/v2/requisitions/", request.uri.path)
            val offset = request.query("offset")?.toInt() ?: 0
            jsonElementLens.inject(pagedResponse(offset), Response(Status.OK))
        }

        val api = AccountInformationApiImpl(handler, testToken)
        runBlocking {
            assertEquals(
                listOf(
                    Requisition("id-0", listOf("account-000"), "GA"),
                    Requisition("id-1", listOf("account-001"), "GA"),
                    Requisition("id-2", listOf("account-002"), "GA"),
                    Requisition("id-3", listOf("account-003"), "GA")
                ),
                api.getAllRequisitions()
            )
        }
    }

    @Test
    fun `get all transactions since a given date`() {
        val transactionsResponse = Json.parseToJsonElement(
            """
            {"transactions":{"booked": [
               
              {
                "transactionId": "82fe8c2a-65dd-36f1-b87a-14943618b253",
                "bookingDate": "2022-11-19",
                "valueDate": "2022-11-19",
                "transactionAmount": {
                  "amount": "-1.99",
                  "currency": "EUR"
                },
                "creditorName": "GOOGLE*GOOGLE STORAGE",
                "remittanceInformationUnstructured": "-",
                "remittanceInformationUnstructuredArray": [
                  "-"
                ],
                "additionalInformation": "5507eadd-e390-4f73-9e6d-89b186709303",
                "bankTransactionCode": "PMNT-CCRD-POSD",
                "internalTransactionId": "91dead06e5afb3bb22a9de9c3b70e268"
              }
            ], "pending": [
              {
                "transactionId": "cb695202-845f-39df-807a-46145f0b144f",
                "bookingDate": "2023-01-28",
                "valueDate": "2023-01-28",
                "transactionAmount": {
                  "amount": "10.0",
                  "currency": "EUR"
                },
                "debtorName": "RENFE CERCANIAS - 7170",
                "remittanceInformationUnstructured": "-",
                "remittanceInformationUnstructuredArray": [
                  "-"
                ],
                "additionalInformation": "eeda27db-b7b6-44c6-bd96-df6ce94a457b",
                "bankTransactionCode": "PMNT-MCRD-OTHR",
                "internalTransactionId": "5493871a34ba7564c0f87a9fe71a52d8"
              }
            ]}}
        """.trimIndent()
        )

        val accountId = "test-account"

        val handler: HttpHandler = { request ->
            assertEquals("/api/v2/accounts/$accountId/transactions/", request.uri.path)
            assertEquals("2022-11-19", request.query("date_from"))
            jsonElementLens.inject(transactionsResponse, Response(Status.OK))
        }


        runBlocking {
            val api = AccountInformationApiImpl(handler, testToken)
            val results = api.getTransactions(accountId, LocalDate.of(2022, 11, 19))

            assertEquals(
                TransactionResults(
                    TransactionsByStatus(
                        listOf(
                            Debit(
                                "82fe8c2a-65dd-36f1-b87a-14943618b253",
                                "GOOGLE*GOOGLE STORAGE",
                                TransactionAmount("EUR", -1.99),
                                "PMNT-CCRD-POSD",
                                LocalDate.of(2022, 11, 19),
                                LocalDate.of(2022, 11, 19),
                                "-",
                                "91dead06e5afb3bb22a9de9c3b70e268"
                            )
                        ), listOf(
                            Credit(
                                "cb695202-845f-39df-807a-46145f0b144f",
                                "RENFE CERCANIAS - 7170",
                                null,
                                TransactionAmount("EUR", 10.0),
                                "PMNT-MCRD-OTHR",
                                LocalDate.of(2023, 1, 28),
                                LocalDate.of(2023, 1, 28),
                                "-",
                                "5493871a34ba7564c0f87a9fe71a52d8"
                            )
                        )
                    )
                ), results
            )

        }


    }


}

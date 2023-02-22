package domfin.nordigen.client

import domfin.nordigen.*
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import java.time.LocalDate

private fun Request.withJsonHeaders(): Request =
    this.header("Content-Type", "application/json")
        .header("Accept", "application/json")

class AccountInformationApiImpl internal constructor(private val client: HttpHandler, accessToken: String) :
    AccountInformationApi() {

    private val withAccessToken = ClientFilters.BearerAuth(accessToken)

    class SuccessfulStatusCodeExpected(val response: Response) : Throwable() {
        override val message: String?
            get() = "Expected a 2xx status code, got instead ${response.status}, response payload: ${response.body}"
    }


    override suspend fun getTransactions(accountId: String, dateFrom: LocalDate?): TransactionResults {
        val url = baseUri
            .appendToPath("/accounts/$accountId/transactions/")
            .let {
                if (dateFrom != null)
                    it.query("date_from", dateFrom.toString())
                else
                    it
            }

        val request = Request(
            Method.GET,
            url
        ).withJsonHeaders()

        val response = withAccessToken(expectSuccess(client))(request)
        return TransactionResults.lens.get(response)
    }

    override suspend fun getInstitutions(countryCode: String): List<Institution> {
        val request = Request(
            Method.GET, baseUri
                .appendToPath("/institutions/")
                .query("country", countryCode)
        ).withJsonHeaders()

        val response = withAccessToken(expectSuccess(client))(request)
        return Institution.listLens(response)
    }

    override suspend fun createRequisition(requisitionRequest: RequisitionRequest): RequisitionResponse {
        val request = RequisitionRequest.lens.inject(
            requisitionRequest, Request(
                Method.POST, baseUri
                    .appendToPath("/requisitions/")
            ).withJsonHeaders()
        )

        val response = withAccessToken(expectSuccess(client))(request)
        return RequisitionResponse.lens.get(response)
    }

    override suspend fun getRequisition(requisitionId: String): Requisition {
        val request = Request(Method.GET, baseUri.appendToPath("/requisitions/$requisitionId/")).withJsonHeaders()
        val response = withAccessToken(expectSuccess(client))(request)
        return Requisition.lens.get(response)
    }

    override suspend fun getAllRequisitions(): List<Requisition> {
        val requisitions = mutableListOf<Requisition>()
        var offset: Int? = null

        while (true) {
            val response = getRequisitionsPage(offset)
            requisitions.addAll(response.results)
            offset = response.nextOffset()
            if (offset == null) break
        }
        return requisitions
    }

    suspend private fun getRequisitionsPage(offset: Int?): RequisitionResults {
        val url = baseUri.appendToPath("/requisitions/")
        val urlWithLimit = offset?.let { url.query("offset", it.toString()) } ?: url
        val request = Request(Method.GET, urlWithLimit).withJsonHeaders()
        val response = withAccessToken(expectSuccess(client))(request)
        return RequisitionResults.lens.get(response)
    }


    companion object Factory {
        private val baseUri = Uri.of("https://ob.nordigen.com/api/v2/")
        private fun expectSuccess(f: HttpHandler): HttpHandler = { request ->
            val response = f(request)
            if (!response.status.successful)
                throw SuccessfulStatusCodeExpected(response)
            else
                response
        }

        suspend fun withFreshToken(secretId: String, secretKey: String): AccountInformationApi {
            val request0 = Request(Method.POST, baseUri.appendToPath("/token/new/")).withJsonHeaders()

            val payload = SecretIdAndKey(secretId, secretKey)
            val request = SecretIdAndKey.lens.inject(payload, request0)
            val client = JavaHttpClient()
            val response = expectSuccess(client)(request)
            val apiResponse = AccessTokenResponse.lens.get(response)
            return AccountInformationApiImpl(client, apiResponse.access)
        }
    }
}
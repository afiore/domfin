package domfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import domfin.nordigen.Institution
import domfin.nordigen.RequisitionRequest
import domfin.nordigen.client.AccountInformationApiImpl
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

class SetupAccounts : CliktCommand() {
    private val countryCode by option(
        "-c",
        "--country-code",
        help = "The ISO 3166 two-character country code to filter banking institutions by."
    ).required()

    private val secrets: Secrets by requireObject<Secrets>()

    override fun run() {
        runBlocking {
            val api = AccountInformationApiImpl.withFreshToken(secrets.id, secrets.key)
            val institutions = api.getInstitutions(countryCode)

            //This should not happen
            if (institutions.isEmpty()) {
                echo("No institutions found for this country code")
                exitProcess(0)
            } else {
                //TODO: format as a table and include BIC
                institutions.withIndex()
                    .forEach { println("${it.index + 1}. ${it.value.name}, transaction total days #${it.value.transactionTotalDays}") }

                val institutionId = promptForBankChoice(institutions)
                val requisition = api.createRequisition(RequisitionRequest("http://example.com/", institutionId))
                echo("Please authenticate with your financial institution now: ${requisition.link}")
                pleaseConfirm("Please confirm when you complete the authentication: y/yes")

                echo("requisition: $requisition")
            }
        }
    }

    private fun pleaseConfirm(msg: String) {
        echo("$msg")

        if (setOf("yes", "y").contains(readTrimLn()))
            return
        else
            pleaseConfirm(msg)
    }

    private fun promptForBankChoice(institutions: List<Institution>): String {
        echo("Please pick an institution by selecting a value between 1 and ${institutions.size + 1}:")

        val idx = readln().trim().toIntOrNull()

        if (idx != null && institutions.indices.contains(idx - 1)) {
            val institution = institutions.get(idx - 1)
            echo("Ok, you have picked: ${institution.name}")
            echo("Please confirm: y/n")

            if (setOf("y", "yes").contains(readTrimLn()))
                return institution.id
            else
                return promptForBankChoice(institutions)
        } else
            return promptForBankChoice(institutions)
    }

    private fun readTrimLn() = readln().trim()
}
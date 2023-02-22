package domfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import domfin.nordigen.client.AccountInformationApiImpl
import kotlinx.coroutines.runBlocking

class ListAccounts : CliktCommand() {

    private val secrets: Secrets by requireObject<Secrets>()
    override fun run() {
        runBlocking {
            val api = AccountInformationApiImpl.withFreshToken(secrets.id, secrets.key)
            api.getAllRequisitions().forEach {
                echo("- requisition id ${it.id}, status: ${it.status}. Accounts: ${it.accounts.joinToString(", ")}")
            }
        }

    }

}

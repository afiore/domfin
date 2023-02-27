package domfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt

data class Secrets(val id: String, val key: String)

class Cli : CliktCommand() {
    val nordigenSecretId by option(
        envvar = "NORDIGEN_SECRET_ID",
        help = "The Nordigen secret id. Will be automatically read from the NORDIGEN_SECRET_ID env variable if present."
    ).prompt(requireConfirmation = true, hideInput = true)

    val nordigenSecretKey by option(
        envvar = "NORDIGEN_SECRET_KEY",
        help = "The Nordigen secret key. Will use NORDIGEN_SECRET_KEY env variable if set."
    ).prompt(requireConfirmation = true, hideInput = true)


    override fun run() {
        currentContext.obj = Secrets(nordigenSecretId, nordigenSecretKey)
    }
}


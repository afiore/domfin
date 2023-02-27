import com.github.ajalt.clikt.core.subcommands
import domfin.cli.Cli
import domfin.cli.ListAccounts
import domfin.cli.SetupAccounts
import domfin.cli.SyncTransactions

fun main(args: Array<String>) = Cli().subcommands(SetupAccounts(), ListAccounts(), SyncTransactions()).main(args)

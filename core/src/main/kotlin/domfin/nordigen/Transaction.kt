package domfin.nordigen

import domfin.serde.LocalDateSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.time.LocalDate


@Serializable(with = TransactionSerializer::class)
sealed class Transaction {
    abstract val transactionId: String
    abstract val transactionAmount: TransactionAmount
    abstract val bankTransactionCode: String

    @Serializable(with = LocalDateSerializer::class)
    abstract val bookingDate: LocalDate

    @Serializable(with = LocalDateSerializer::class)
    abstract val valueDate: LocalDate
    abstract val remittanceInformationUnstructured: String?
    abstract val internalTransactionId: String?
}

@Serializable
data class Credit(
    override val transactionId: String,
    val debtorName: String,
    val debtorAccount: DebtorAccount? = null,
    override val transactionAmount: TransactionAmount,
    override val bankTransactionCode: String,

    @Serializable(with = LocalDateSerializer::class) override val bookingDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class) override val valueDate: LocalDate,
    override val remittanceInformationUnstructured: String? = null,
    override val internalTransactionId: String? = null
) : Transaction()

@Serializable
data class Debit(
    override val transactionId: String,
    val creditorName: String,
    override val transactionAmount: TransactionAmount,
    override val bankTransactionCode: String,
    @Serializable(with = LocalDateSerializer::class) override val bookingDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class) override val valueDate: LocalDate,
    override val remittanceInformationUnstructured: String? = null,
    override val internalTransactionId: String? = null
) : Transaction()

object TransactionSerializer : JsonContentPolymorphicSerializer<Transaction>(Transaction::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Transaction> = when {
        "creditorName" in element.jsonObject -> Debit.serializer()
        else -> Credit.serializer()
    }
}


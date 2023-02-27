package domfin.config

typealias Env = Map<String, String>


enum class ErrorKind {
    VarMissing,
    ParsingError,
    ValidationError
}

sealed class EnvResult<out T> {

    abstract val envVarName: String

    companion object {

        data class Ok<out T>(override val envVarName: String, val value: T) : EnvResult<T>()
        data class Err(override val envVarName: String, val kind: ErrorKind, val clue: String? = null) :
            EnvResult<Nothing>()

    }


    fun asError(): Err = when (this) {
        is Err -> this
        is Ok -> throw ClassCastException("asErr() called on Ok value ${this.value}")
    }

    fun valueOrThrow(): T =
        when (this) {
            is Ok -> this.value
            is Err -> throw ClassCastException("ok() called on ${this}")
        }
}


interface FromEnv<T> {

    fun fromEnv(env: Env): EnvResult<T>
    fun fromEnvOrNull(env: Env): T? {
        return when (val result = fromEnv(env)) {
            is EnvResult.Companion.Err -> null
            is EnvResult.Companion.Ok -> result.value
        }
    }
}

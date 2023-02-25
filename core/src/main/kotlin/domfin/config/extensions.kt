package domfin.config


fun Env.getVar(varName: String): EnvResult<String> {
    val value = this.get(varName)
    return if (value == null)
        EnvResult.Companion.Err(varName, ErrorKind.VarMissing)
    else
        EnvResult.Companion.Ok(varName, value)
}


fun Env.getSecret(varName: String): EnvResult<Secret> =
    getVar(varName).asSecret()


fun EnvResult<String>.asSecret(): EnvResult<Secret> =
    map { Secret(it) }

fun <T> EnvResult<String>.parseOrError(parse: (String) -> T?): EnvResult<T> =
    this.flatMap {
        val parsed = parse(it)
        if (parsed == null)
            EnvResult.Companion.Err(this.envVarName, ErrorKind.ParsingError)
        else
            EnvResult.Companion.Ok(this.envVarName, parsed)
    }

fun <T> EnvResult<T>.default(value: T): EnvResult<T> =
    when (this) {
        is EnvResult.Companion.Ok -> this
        is EnvResult.Companion.Err ->
            if (this.kind == ErrorKind.VarMissing)
                EnvResult.Companion.Ok(this.envVarName, value)
            else
                this
    }


fun <T, S> EnvResult<T>.flatMap(f: (T) -> EnvResult<S>): EnvResult<S> =
    when (this) {
        is EnvResult.Companion.Err -> this
        is EnvResult.Companion.Ok -> f(this.value)
    }

fun <T, S> EnvResult<T>.map(f: (T) -> S): EnvResult<S> =
    when (this) {
        is EnvResult.Companion.Err -> this
        is EnvResult.Companion.Ok ->
            EnvResult.Companion.Ok(this.envVarName, f(this.value))
    }

fun <T> EnvResult<T>.validating(clue: String? = null, pred: (T) -> Boolean): EnvResult<T> =
    this.flatMap {
        if (pred(it))
            this
        else
            EnvResult.Companion.Err(this.envVarName, ErrorKind.ValidationError, clue)
    }

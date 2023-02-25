package domfin.config

@JvmInline
value class Secret(val raw: String) {
    override fun toString(): String =
        "REDACTED"
}

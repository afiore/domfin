package domfin.config

data class GrpcServerConfig(val serverPort: UInt) {
    companion object : FromEnv<GrpcServerConfig> {
        val varName = "GRPC_SERVER_PORT"
        val defaultPort: UInt = 8980u
        override fun fromEnv(env: Env): EnvResult<GrpcServerConfig> =
            env.getVar(varName).parseOrError {
                it.toUIntOrNull()
            }.default(defaultPort).map { GrpcServerConfig(it) }
    }
}
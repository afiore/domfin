package domfin.config

data class AppConfig(val db: DBConfig, val grpc: GrpcServerConfig, val nordigen: NordigenConfig) {
    companion object : FromEnv<AppConfig> {
        override fun fromEnv(env: Env): EnvResult<AppConfig> =
            DBConfig.fromEnv(env).flatMap { db ->
                GrpcServerConfig.fromEnv(env).flatMap { grpc ->
                    NordigenConfig.fromEnv(env).map { nordigen ->
                        AppConfig(db, grpc, nordigen)
                    }
                }
            }

    }
}

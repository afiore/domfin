{ pkgs ? import <nixpkgs> { } }:

pkgs.mkShell {
  buildInputs = [
    pkgs.gradle_7
    pkgs.protobuf
    pkgs.grpc-gateway
    pkgs.protoc-gen-go
    pkgs.protoc-gen-go-grpc
    pkgs.go
    pkgs.buf
    pkgs.postman
    pkgs.envoy
  ];
  "DB_PATH" = "/home/a.fiore/.domfin.sqlite";
  "GRPC_SERVER_PORT" = "9999";
  "DOCKER_GATEWAY_HOST" = "172.17.0.1";
  "TRANSACTION_SYNC_INTERVAL_MILLIS" = "300000";
  "JAVA_HOME" = "${pkgs.jdk17}"; #Set to the same JDK used by gradle_7, so that ./gradlew script can be used
}

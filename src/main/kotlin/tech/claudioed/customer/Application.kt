package tech.claudioed.customer

import io.vertx.core.Vertx

fun main(args: Array<String> = arrayOf()) {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle::class.java.name)
}

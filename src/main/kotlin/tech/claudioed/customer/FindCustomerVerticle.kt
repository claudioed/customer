package tech.claudioed.customer

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class FindCustomerVerticle: AbstractVerticle() {

  override fun start() {
    val mongoClient = MongoClient.createShared(this.vertx, JsonObject(), "CUSTOMER")
  }


}

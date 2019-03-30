package tech.claudioed.customer

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.grpc.VertxServerBuilder
import tech.claudioed.customer.grpc.CustomerCreateRequest
import tech.claudioed.customer.grpc.CustomerFindRequest
import tech.claudioed.customer.grpc.CustomerFindResponse
import tech.claudioed.customer.grpc.CustomerServiceGrpc
import java.util.*

class MainVerticle : AbstractVerticle() {

  private val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)

  override fun start(startFuture: Future<Void>) {

    val mongoHost = System.getenv("MONGO_HOST")
    LOGGER.info(" MONGO HOST $mongoHost")
    val mongoClient = MongoClient.createShared(this.vertx, JsonObject()
      .put("connection_string", mongoHost)
      .put("db_name", "CUSTOMERS"))


    val service = object : CustomerServiceGrpc.CustomerServiceImplBase() {
      override fun findCustomer(request: CustomerFindRequest,
                                responseObserver: io.grpc.stub.StreamObserver<CustomerFindResponse>) {
        LOGGER.info("Receiving request to find customer...")
        val query = JsonObject().put("_id", request.id)
        mongoClient.findOne("customers", query, null) {
          if (it.succeeded()) {
            LOGGER.info("Query executed successfully")
            val json = it.result()
            LOGGER.info("Converting Data")
            val customerData = CustomerFindResponse.newBuilder().setId(json.getString("_id")).setName(json.getString("_name"))
              .setAddress(json.getString("address")).setDocument(json.getString("document"))
              .setCountry(json.getString("country")).setCity(json.getString("city"))
              .setEmail(json.getString("email")).setLastName(json.getString("lastName"))
              .build()
            responseObserver.onNext(customerData)
            responseObserver.onCompleted()
          } else {
            responseObserver.onError(RuntimeException("Customer Not Found"))
          }
        }
      }

      override fun createCustomer(request: CustomerCreateRequest,
                                  responseObserver: io.grpc.stub.StreamObserver<CustomerFindResponse>) {
        LOGGER.info("Receiving request to create customer...")
        val customer = Customer(id = UUID.randomUUID().toString(), name = request.name, lastName = request.lastName, city = request.city, country = request.country, address = request.address, document = request.document, email = request.email)
        val data = JsonObject().put("id",customer.id).put("name",customer.name).put("lastName",customer.lastName).put("city",customer.city).put("country",customer.country)
          .put("address",customer.address).put("document",customer.document).put("email",customer.email)
        mongoClient.insert("customers",data) {
          if(it.succeeded()){
            LOGGER.info("Customer saved successfully")
            val response = CustomerFindResponse.newBuilder().setId(customer.id).setName(customer.name)
              .setAddress(customer.address).setDocument(customer.document)
              .setCountry(customer.country).setCity(customer.city)
              .setEmail(customer.email).setLastName(customer.lastName)
              .build()
            LOGGER.info("New customer was created.")
            responseObserver.onNext(response)
            responseObserver.onCompleted()
          }else{
            responseObserver.onError(RuntimeException("Error to insert customer"))
          }
        }
      }
    }

    LOGGER.info("Starting gRPC Server...")

    val rpcServer = VertxServerBuilder
      .forAddress(vertx, "localhost", 50051)
      .addService(service)
      .build()

    rpcServer.start()

    LOGGER.info("gRPC Server started successfully")

  }

}

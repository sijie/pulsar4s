package com.sksamuel.pulsar4s

import java.util.UUID

import org.scalatest.{FunSuite, Matchers}

import scala.util.{Success, Try}

class MessageWriterTest extends FunSuite with Matchers {

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Person(name: String, location: String)

  implicit object PersonWriter extends MessageWriter[Person] {
    override def write(p: Person): Try[Message] = Success(Message(p.name + "/" + p.location))
  }

  test("message writer should be used to create a message") {

    val client = PulsarClient("pulsar://localhost:6650", "sample/standalone/ns1")
    val topic = Topic("persistent://sample/standalone/ns1/" + UUID.randomUUID())

    val producer = client.producer(topic)
    producer.send(Person("jon snow", "the wall"))
    producer.close()

    val consumer = client.consumer(topic, Subscription("wibble"))
    consumer.seek(MessageId.earliest)
    val msg = consumer.receive
    msg.data shouldBe "jon snow/the wall".getBytes
    consumer.close()
    client.close()
  }
}

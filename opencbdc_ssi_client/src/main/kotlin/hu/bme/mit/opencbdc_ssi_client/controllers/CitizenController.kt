package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext


class CitizenController {


    val name: String

    var ariesClient: AriesClient
    var ariesWebSocketClient : AriesWebSocketClient

    constructor(name: String) {
        this.name = name
        val context : AbstractApplicationContext = ClassPathXmlApplicationContext("factorybean-spring-ctx.xml")

        ariesWebSocketClient = context.getBean(name + "WebsocketClient", AriesWebSocketClient::class.java)
        ariesClient = context.getBean(name + "Client", AriesClient::class.java)
    }

    fun pay() {
        println("Paying")
    }

}
package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.api.AcaPyRequestFilter.log
import org.hyperledger.aries.webhook.EventHandler

abstract class Controller(val name: String, val url: String) : EventHandler() {

    protected val ariesClient: AriesClient = AriesClient
        .builder()
        .url(url) // optional - defaults to ws://localhost:8031/w
        .build()

    protected val ariesWebSocketClient: AriesWebSocketClient = AriesWebSocketClient
        .builder()
        .url("$url/ws") // optional - defaults to ws://localhost:8031/ws
        .handler(this) // optional - your handler implementation
        // .bearerToken(bearer) // optional - jwt token - only when running in multi tenant mode
        .build()
    init {
        log.info("Controller created: $name : $url : $url/ws")
    }
}
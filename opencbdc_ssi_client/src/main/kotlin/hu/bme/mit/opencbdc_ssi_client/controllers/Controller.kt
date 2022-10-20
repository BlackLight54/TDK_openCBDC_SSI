package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.acy_py.generated.model.SendMessage
import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.api.connection.ConnectionRecord
import org.hyperledger.aries.api.connection.CreateInvitationRequest
import org.hyperledger.aries.api.connection.CreateInvitationResponse
import org.hyperledger.aries.api.message.BasicMessage
import org.slf4j.LoggerFactory
import org.hyperledger.aries.webhook.EventHandler as EventHandler1

abstract class Controller(val name: String, val url: String) : EventHandler1() {

    var log = LoggerFactory.getLogger(name + "-controller")

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
        log.info("Controller created: $url : $url/ws")
    }

    fun createInvitaion(): CreateInvitationResponse {
        return ariesClient.connectionsCreateInvitation(CreateInvitationRequest()).get()
    }

    override fun handleBasicMessage(message: BasicMessage?) {
        super.handleBasicMessage(message)
        log.info("Basic message ${message?.messageId} received from connection/${message?.connectionId}: ${message?.content}")
    }

    fun sendBasicMessage(message: String, connectionAlias: String) {
        findConnectionByAlias(connectionAlias).let { ariesClient.connectionsSendMessage(it.connectionId, SendMessage(message)) }
    }


    private fun findConnectionByAlias(alias: String): ConnectionRecord {
        val connections = ariesClient.connections()
        return connections.get().stream()
            .filter { connection -> connection.alias == alias }
            .findFirst().get()
    }


}
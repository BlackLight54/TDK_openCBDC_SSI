package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.acy_py.generated.model.ConnectionInvitation
import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.api.AcaPyRequestFilter.log
import org.hyperledger.aries.api.connection.ConnectionReceiveInvitationFilter
import org.hyperledger.aries.api.connection.ReceiveInvitationRequest
import org.hyperledger.aries.webhook.EventHandler
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

class CitizenController(_name : String, _url : String): Controller(_name, _url) {



    fun acceptInvitation(invitation: ConnectionInvitation, alias: String) {
        ariesClient.connectionsReceiveInvitation(
            ReceiveInvitationRequest.builder()
                .did(invitation.did)
                .label(invitation.label)
                .recipientKeys(invitation.recipientKeys)
                .serviceEndpoint(invitation.serviceEndpoint)
                .build(),
            ConnectionReceiveInvitationFilter
                .builder()
                .alias(alias)
                .build()
        )
            .ifPresent { connection -> log.debug("{}", connection.getConnectionId()) }
    }
    fun pay() {
        println("Paying")
    }


}
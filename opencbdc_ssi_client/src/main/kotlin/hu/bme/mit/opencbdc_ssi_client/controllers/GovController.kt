package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.api.connection.CreateInvitationRequest
import org.hyperledger.aries.api.connection.CreateInvitationResponse
import org.hyperledger.aries.webhook.EventHandler
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.Resource


class GovController(_name :String , _url : String) : Controller(_name,_url) {

    fun createInvitaion(): CreateInvitationResponse {
        return ariesClient.connectionsCreateInvitation(CreateInvitationRequest()).get()
    }
}
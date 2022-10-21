package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.api.message.BasicMessage
import org.hyperledger.aries.webhook.EventHandler
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.Resource


class SentinelController(_name :String , _url : String) : Controller(_name,_url) {
    // cbdc transactions travel in SSI messages in "cbdc:from_address:to_address:amount" format
    override fun handleBasicMessage(message: BasicMessage?) {
        super.handleBasicMessage(message)
        if (message != null) {
            log.info(message.content)
        }
    }

    fun openCBDCTransact(from_address :String , to_addresss : String, amount : Int ){
        // put into the same docker network(SSI_client -> host?)
        // call through ssh
        log.info("opencbdc transaction : $from_address -> $to_addresss : $amount")
    }
}
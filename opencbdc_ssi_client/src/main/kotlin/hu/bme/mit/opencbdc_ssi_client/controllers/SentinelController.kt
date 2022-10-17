package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.webhook.EventHandler
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.Resource


class SentinelController(_name :String , _url : String) : Controller(_name,_url) {



}
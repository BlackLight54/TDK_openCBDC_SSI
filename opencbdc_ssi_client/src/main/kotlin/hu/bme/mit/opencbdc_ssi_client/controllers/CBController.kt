package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.webhook.EventHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.Resource


class CBController(_name :String , _url : String) : Controller(_name,_url) {


//    @PostConstruct
//    fun init() {
//        val context: AbstractApplicationContext = ClassPathXmlApplicationContext("factorybean-spring-ctx.xml")
//
//        ariesWebSocketClient = context.getBean(name + "WebsocketClient", AriesWebSocketClient::class.java)
//        ariesClient = context.getBean(name + "Client", AriesClient::class.java)
//
//    }

    fun pay() {
        println("Paying")
    }

}
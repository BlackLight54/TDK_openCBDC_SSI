package hu.bme.mit.opencbdc_ssi_client.aries

import org.hyperledger.aries.AriesClient
import org.hyperledger.aries.AriesClient.AriesClientBuilder
import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.webhook.EventHandler
import org.springframework.beans.factory.FactoryBean

class AriesClientFactory(var url: String) : FactoryBean<AriesClient>{
    override fun getObject(): AriesClient {
        return AriesClient
            .builder()
            .url(url) // optional - defaults to ws://localhost:8031/w
            .build();
    }

    override fun getObjectType(): Class<*> {
        return AriesClient::class.java
    }

    override fun isSingleton(): Boolean {
        return false
    }
}

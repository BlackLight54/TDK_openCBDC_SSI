package hu.bme.mit.opencbdc_ssi_client.aries

import org.hyperledger.aries.AriesWebSocketClient
import org.hyperledger.aries.webhook.EventHandler
import org.springframework.beans.factory.FactoryBean


@lombok.NoArgsConstructor
class AriesSocketFactory(var url: String) : FactoryBean<AriesWebSocketClient> {

    override fun getObject(): AriesWebSocketClient {
        return AriesWebSocketClient
            .builder()
            .url(url) // optional - defaults to ws://localhost:8031/ws
            .handler(EventHandler.DefaultEventHandler()) // optional - your handler implementation
            // .bearerToken(bearer) // optional - jwt token - only when running in multi tenant mode
            .build();
    }

    override fun getObjectType(): Class<*> {
        return AriesWebSocketClient::class.java
    }

    override fun isSingleton(): Boolean {
        return false
    }
}

package hu.bme.mit.opencbdc_ssi_client.config

import hu.bme.mit.opencbdc_ssi_client.controllers.CBController
import hu.bme.mit.opencbdc_ssi_client.controllers.CitizenController
import hu.bme.mit.opencbdc_ssi_client.controllers.GovController
import hu.bme.mit.opencbdc_ssi_client.controllers.SentinelController
import org.hyperledger.aries.AriesClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ComponentConfig {
    val aliceUrl: String = "http://localhost:8001"
    val bobUrl: String = "http://localhost:8011"
    val sentinelUrl : String = "http://localhost:8021"
    val govUrl : String = "http://localhost:8031"
    val cbUrl : String = "http://localhost:8041"

    @Bean(name = ["aliceController"])
    fun aliceController(): CitizenController {
        return CitizenController("alice", aliceUrl)
    }

    @Bean(name = ["bobController"])
    fun bobController(): CitizenController {
        return CitizenController("bob", bobUrl)
    }
    @Bean(name = ["sentinelController"])
    fun sentinelController() : SentinelController {
        return SentinelController("sentinel", sentinelUrl)
    }
    @Bean(name = ["govController"])
    fun govController() : GovController {
        return GovController("gov", govUrl)
    }
    @Bean(name = ["cbController"])
    fun cbController() : CBController {
        return CBController("cb", cbUrl)
    }

}
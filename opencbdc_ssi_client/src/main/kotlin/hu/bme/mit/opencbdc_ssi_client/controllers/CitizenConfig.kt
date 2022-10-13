package hu.bme.mit.opencbdc_ssi_client.controllers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CitizenConfig {
    @Bean(name = ["aliceController"])
    fun Alice(): CitizenController {
        return CitizenController("alice")
    }
    @Bean(name = ["bobController"])
    fun Bob(): CitizenController {
        return CitizenController("bob")
    }
}

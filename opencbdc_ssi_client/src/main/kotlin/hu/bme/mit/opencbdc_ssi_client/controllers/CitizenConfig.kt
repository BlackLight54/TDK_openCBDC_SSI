package hu.bme.mit.opencbdc_ssi_client.controllers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CitizenConfig {
    @Bean
    fun Alice(): CitizenController {
        return CitizenController("alice")
    }
    @Bean
    fun Bob(): CitizenController {
        return CitizenController("bob")
    }
}

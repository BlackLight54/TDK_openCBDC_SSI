package hu.bme.mit.opencbdc_ssi_client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class OpencbdcSsiClientApplication
fun main(args: Array<String>) {
    runApplication<OpencbdcSsiClientApplication>(*args)
}
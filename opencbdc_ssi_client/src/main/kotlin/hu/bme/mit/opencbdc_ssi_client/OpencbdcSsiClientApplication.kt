package hu.bme.mit.opencbdc_ssi_client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class OpencbdcSsiClientApplication /*: CommandLineRunner*/{
    /*
    private val LOG: Logger = LoggerFactory
        .getLogger(OpencbdcSsiClientApplication::class.java)

    @Override
    override fun run(vararg args: String?) {
        LOG.info("EXECUTING : command line runner")
        for (i in args.indices) {
            LOG.info("args[{}]: {}", i, args[i])
        }
    }*/
}
fun main(args: Array<String>) {
    runApplication<OpencbdcSsiClientApplication>(*args)
}
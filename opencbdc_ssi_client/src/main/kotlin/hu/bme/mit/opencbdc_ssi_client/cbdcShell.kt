package hu.bme.mit.opencbdc_ssi_client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class cbdcShell {
    var log: Logger = LoggerFactory.getLogger(cbdcShell::class.java.getName())

    constructor(){
        log.info("cbdcShell created")
    }
    @ShellMethod(key = arrayOf("onboard"), value = "Onboard Alice")
    fun onboardAlice() {
        log.info("Onboarding Alice")
    }


}
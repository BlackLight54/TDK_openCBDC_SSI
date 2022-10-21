package hu.bme.mit.opencbdc_ssi_client

import hu.bme.mit.opencbdc_ssi_client.controllers.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct
import kotlin.random.Random
import kotlin.random.nextUInt

@RestController
class Endpoints(
    val aliceController: CitizenController,
    val bobController: CitizenController,
    val sentinelController: SentinelController,
    val cbController: CBController,
    val govController: GovController
) {

    var log = LoggerFactory.getLogger(Endpoints::class.java)

    @GetMapping("/connectToGov")
    fun connectToGov() {
        aliceController.establishConnection(govController)
        bobController.establishConnection(govController)
    }

    @GetMapping("/connectToCB")
    private fun establishConnections() {
        aliceController.establishConnection(cbController)
        bobController.establishConnection(cbController)
    }
    @GetMapping("/auditableTx")
    fun auditableTx(): String {
        aliceController.establishConnection(sentinelController)
        bobController.establishConnection(sentinelController)
        aliceController.sendBasicMessage("cbdc:${aliceController.name}_addr:${bobController.name}_addr:100:${Random.nextUInt()}", sentinelController.name)
        return ""
    }
    @GetMapping("/privateTx")
    fun privateTx(): String {

        return ""
    }







}
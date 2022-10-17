package hu.bme.mit.opencbdc_ssi_client

import hu.bme.mit.opencbdc_ssi_client.controllers.*
import org.hyperledger.aries.api.AcaPyRequestFilter
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@RestController()
class Endpoints(
    val aliceController: CitizenController,
    val bobController: CitizenController,
    val sentinelController: SentinelController,
    val cbController: CBController,
    val govController: GovController
) {

    var log = LoggerFactory.getLogger(Endpoints::class.java)

    @PostConstruct
    fun init() {

    }

    @GetMapping("/connect")
    fun connect() {
        estabilishConnections(aliceController)
        estabilishConnections(bobController)
    }

    @GetMapping("/onboard")
    fun onboard(): String {
        onBoard(aliceController);
        onBoard(bobController);
        return ""
    }

    @GetMapping("/auditableTx")
    fun auditableTx(): String {

        return ""
    }
    @GetMapping("/privateTx")
    fun privateTx(): String {

        return ""
    }

    private fun onBoard(citizen: CitizenController) {
        citizen.sendBasicMessage("onboard", govController.name)
    }

    private fun estabilishConnections(citizen: CitizenController) {
        estabilishConnection(citizen, govController)
        estabilishConnection(citizen, cbController)
        estabilishConnection(citizen, sentinelController)
    }

    private fun estabilishConnection(citizen: CitizenController, entity: Controller) {
        log.info("Establishing connection: ${citizen.name} -> ${entity.name}")
        val invitation = entity.createInvitaion().invitation
        citizen.acceptInvitation(invitation, entity.name)
    }

}
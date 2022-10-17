package hu.bme.mit.opencbdc_ssi_client

import hu.bme.mit.opencbdc_ssi_client.controllers.CBController
import hu.bme.mit.opencbdc_ssi_client.controllers.CitizenController
import hu.bme.mit.opencbdc_ssi_client.controllers.GovController
import hu.bme.mit.opencbdc_ssi_client.controllers.SentinelController
import org.hyperledger.aries.api.AcaPyRequestFilter.log
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


    @PostConstruct
    fun init() {
//        val context = ClassPathXmlApplicationContext("factorybean-spring-ctx.xml")
//        aliceController = context.getBean("aliceController", CitizenController::class.java)
//        bobController = context.getBean("bobController", CitizenController::class.java)
//        // sentinelController = context.getBean("sentinelController", SentinelController::class.java)
//        // cbController = context.getBean("cbController", CBController::class.java)
//        govController = context.getBean("governmentController", GovController::class.java)

    }

    @GetMapping("/onboard")
    fun onboard(): String {
        onBoard(aliceController);
        onBoard(bobController);
        return ""
    }

    private fun onBoard(citizen: CitizenController) {
        estabilishConnections(citizen)

    }

    private fun estabilishConnections(citizen: CitizenController) {
        log.info("Establishing connections for ${citizen.name}")
        log.info("Establishing connection with gov")
        val invitation = govController.createInvitaion().invitation
        citizen.acceptInvitation(invitation, "gov")

    }
}
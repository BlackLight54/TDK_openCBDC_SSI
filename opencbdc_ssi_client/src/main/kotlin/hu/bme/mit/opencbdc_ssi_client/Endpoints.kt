package hu.bme.mit.opencbdc_ssi_client

import hu.bme.mit.opencbdc_ssi_client.controllers.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@RestController
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
        establishConnections(aliceController)
        establishConnections(bobController)
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
    @GetMapping("/defineCreds")
    fun defineCreds(): String {
        govController.prepareForCredientialIssuance()
        return ""
    }
    @GetMapping("/issueGovCreds")
    fun issueCreds(): String {
      govController.issueCredientialToConnections()
        return ""
    }

    @GetMapping("/recCreds")
    fun recCreds(){
        aliceController.storeCredential()
        bobController.storeCredential()
    }
    @GetMapping("/reqProof")
    fun reqProof(){
        cbController.requestProof(govController.citizenCredDefId)
    }
    private fun onBoard(citizen: CitizenController) {
        citizen.sendBasicMessage("onboard", govController.name)
    }

    private fun establishConnections(citizen: CitizenController) {
        citizen.establishConnection(govController)
        citizen.establishConnection(cbController)
        citizen.establishConnection(sentinelController)
    }



}
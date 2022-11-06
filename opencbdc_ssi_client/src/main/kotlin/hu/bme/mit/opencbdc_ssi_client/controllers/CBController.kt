package hu.bme.mit.opencbdc_ssi_client.controllers

import hu.bme.mit.opencbdc_ssi_client.credientials.CBCred
import hu.bme.mit.opencbdc_ssi_client.credientials.CitizenCred
import org.hyperledger.aries.api.connection.ConnectionRecord
import org.hyperledger.aries.api.credential_definition.CredentialDefinition
import org.hyperledger.aries.api.credential_definition.CredentialDefinitionFilter
import org.hyperledger.aries.api.credentials.CredentialAttributes
import org.hyperledger.aries.api.credentials.CredentialPreview
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialProposalRequest
import org.hyperledger.aries.api.present_proof.PresentProofRequest
import org.hyperledger.aries.api.present_proof.PresentProofRequestHelper
import org.hyperledger.aries.api.present_proof_v2.V20PresExRecord
import org.hyperledger.aries.api.present_proof_v2.V20PresSendRequestRequest
import org.hyperledger.aries.api.schema.SchemaSendRequest
import org.hyperledger.aries.api.schema.SchemasCreatedFilter
import org.hyperledger.aries.pojo.PojoProcessor
import org.springframework.beans.factory.annotation.Autowired
import java.io.*
import java.util.Random


class CBController(_name: String, _url: String) : Controller(_name, _url) {
    // TODO: Implement Revocation for CB VCs
    // TODO: Implement revocation check for Gov VCs
    // TODO: check that only one VC is issued to one Citizenship VC
    @Autowired
    lateinit var govController: GovController

    private val schemaName = "CBCard"
    private val schemaVersion = "1." + Random().nextInt().toUInt().toString()


    val addressesToNames = mutableMapOf<String, String>()
    val namesToAddresses = mutableMapOf<String, String>()

    private fun requestProof(connection: ConnectionRecord, credentialDefinitionId: String) {
        log.info("Requesting proof from: ${connection.theirLabel}")
        val proofRequest = PresentProofRequestHelper.buildForAllAttributes(
            connection.connectionId,
            CitizenCred::class.java,
            listOf(
                PresentProofRequest.ProofRequest.ProofRestrictions.builder()
                    .credentialDefinitionId(credentialDefinitionId)
                    .addAttributeValueRestriction("citizenship", "Hungarian")
                    .build()
            )
        )
        ariesClient.presentProofV2SendRequest(
            V20PresSendRequestRequest(
                true,
                "Citizenship proof for onboarding",
                connection.connectionId,
                V20PresSendRequestRequest.V20PresRequestByFormat.builder()
                    .indy(proofRequest.proofRequest)
                    .build(),
                false
            )
        )


    }

    override fun handleProofV2(proof: V20PresExRecord?) {
        super.handleProofV2(proof)
        if (proof != null) {
            log.info(
                "proof for ${
                    ariesClient.connections().get().filter { conn -> conn.connectionId == proof.connectionId }
                        .first().theirLabel
                } state : ${proof.state.name}"
            )
        }
        if (proof != null && proof.isVerified) {
            val connection =
                ariesClient.connections().get().filter { conn -> conn.connectionId == proof.connectionId }.first()
            log.info("recieved and verified proof: ${connection.theirLabel} : ${proof.isVerified}")
            issueCredientialToConnection(connection)
        }
    }

    fun getSchemaId(): String {


        val schemaId: String
        log.debug("Getting CB schema Id")
        val definedSchemas = ariesClient.schemasCreated(
            SchemasCreatedFilter
                .builder()
                .schemaName(schemaName)
                .schemaVersion(schemaVersion)
                .build()
        ).get()
        if (definedSchemas.isEmpty()) {
            log.info("CB schema doesn't exist in wallet -> posting Schema")
            schemaId = ariesClient.schemas(
                SchemaSendRequest
                    .builder()
                    .schemaName(schemaName)
                    .schemaVersion(schemaVersion)
                    .attributes(
                        PojoProcessor.fieldNames(CBCred::class.java).toList()
                    )
                    .build()
            ).get().schemaId
        } else {
            schemaId = definedSchemas.first()
        }
        log.debug("Schema id: $schemaId")
        return schemaId
    }

    fun getCredentialDefinition(): String {
        log.info("Getting credential definition")
        val citizenCredDefWalletResp = ariesClient.credentialDefinitionsCreated(
            CredentialDefinitionFilter
                .builder()
                .schemaId(getSchemaId())
                .build()
        )

        val credDefId: String
        // check if Cred def in wallet
        if (citizenCredDefWalletResp.isPresent && citizenCredDefWalletResp.get().credentialDefinitionIds.isNotEmpty()
        ) {
            log.info("Credential definition already exists in wallet")
            credDefId = citizenCredDefWalletResp.get().credentialDefinitionIds.first()
        }
//        else {
//            // check if cred def on ledger
//            val citizenCredDefGenId = citizenSchemaId.split(":")[0] + ":3:CL:153507:default"
//            val citizenCredDefLedgerResp = ariesClient.credentialDefinitionsGetById(citizenCredDefGenId)
//            if (citizenCredDefLedgerResp.isPresent && citizenCredDefLedgerResp.get().credentialDefinition.id == citizenCredDefGenId) {
//                log.info("Credential definition already exists on ledger")
//                log.info("Getting Cred Def from ledger")
//                ariesClient.credentialDefinitionsWriteRecord(citizenCredDefGenId)
//                citizenCredDefId = citizenCredDefGenId
//            }
        // Create and post cred def to ledger
        else {
            log.info("Creating crediential definition")
            credDefId = ariesClient.credentialDefinitionsCreate(
                CredentialDefinition.CredentialDefinitionRequest
                    .builder()
                    .schemaId(getSchemaId())
//                        .supportRevocation(true)
//                        .revocationRegistrySize(1000)
                    .tag("default")
                    .build()
            ).get().credentialDefinitionId
        }
//        }
        log.info("Credential definition: id: $credDefId")
        return credDefId
    }

    fun createAddress(name: String): String? {


        log.info("Creating openCBDC address for $name")
        var address: String? = null
        val builder = ProcessBuilder();

        builder.command(
            "docker",
            "exec",
            "-i",
            "opencbdc-tx-client",
            "./build/src/uhs/client/client-cli",
            "2pc-compose.cfg",
            "${name}Mempool.dat",
            "${name}Wallet.dat",
            "newaddress",
            ""
        )
//          builder.command("cmd.exe", "/c", "dir")
        builder.directory(File(System.getProperty("user.home")));


        val process = builder.start();
        val exitCode = process.waitFor();
        assert(exitCode == 0);
        val returnString = process.inputStream.readAllBytes().toString(Charsets.UTF_8)
        address = returnString.trim().split("\n").last()


        log.info("openCBDC Address created: $address")
        mintToAddress(name, 10,10)
        return address
    }
    private fun mintToAddress(name :String, num: Int, value : Int) {



        log.info("Minting to $name")
        val builder = ProcessBuilder();

        builder.command(
            "docker",
            "exec",
            "-i",
            "opencbdc-tx-client",
            "./build/src/uhs/client/client-cli",
            "2pc-compose.cfg",
            "${name}Mempool.dat",
            "${name}Wallet.dat",
            "mint",
            num.toString(),
            value.toString()
        )
        builder.directory(File(System.getProperty("user.home")));
        val process = builder.start();
        val exitCode = process.waitFor();
        assert(exitCode == 0);
       log.info("Minted to $name")
    }

    private fun issueCredientialToConnection(connection: ConnectionRecord?) {
        if (connection != null) {
            val address = createAddress(connection.theirLabel) ?: throw Exception("Address creation failed")
            log.info("Issuing credential to connection ${connection.connectionId}: ${connection.theirLabel}")
            ariesClient.issueCredentialV2Send(
                V1CredentialProposalRequest
                    .builder()
                    .issuerDid(getPublicDID())
                    .connectionId(connection.connectionId.toString())
                    .credentialDefinitionId(getCredentialDefinition())
                    .schemaName(getSchemaId().split(":")[2])
                    .schemaIssuerDid(getPublicDID())
                    .schemaVersion(getSchemaId().split(":")[3])
                    .credentialProposal(
                        CredentialPreview(
                            CredentialAttributes.from(CBCred(connection.theirLabel, address))
                        )
                    )
                    .build()
            )
            log.info("Credential Issued to ${connection.theirLabel}: $address")
            addressesToNames[address] = connection.theirLabel
            namesToAddresses[connection.theirLabel] = address
        }
    }

    // Currently CB initiates the issuance of address VCs
    override fun handleConnection(connection: ConnectionRecord?) {
        super.handleConnection(connection)
        if (connection != null) {
//            log.info("${connection.theirLabel}: ${connection.state.name}")
            if (connection.state.name == "ACTIVE") {
                requestProof(connection, govController.getCredentialDefinition())
            }
        }
    }

//    private class StreamGobbler(private val inputStream: InputStream, consumer: Consumer<String>) :
//        Runnable {
//        private val consumer: Consumer<String>
//
//        init {
//            this.consumer = consumer
//        }
//
//        override fun run() {
//            BufferedReader(InputStreamReader(inputStream)).lines()
//                .forEach(consumer)
//        }
//    }
}



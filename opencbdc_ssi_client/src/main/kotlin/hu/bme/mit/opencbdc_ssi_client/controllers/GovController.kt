package hu.bme.mit.opencbdc_ssi_client.controllers

import hu.bme.mit.opencbdc_ssi_client.credientials.CitizenCred
import org.hyperledger.aries.api.connection.*
import org.hyperledger.aries.api.credential_definition.CredentialDefinition
import org.hyperledger.aries.api.credential_definition.CredentialDefinitionFilter
import org.hyperledger.aries.api.credentials.CredentialAttributes
import org.hyperledger.aries.api.credentials.CredentialPreview
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialProposalRequest
import org.hyperledger.aries.api.schema.SchemaSendRequest
import org.hyperledger.aries.api.schema.SchemasCreatedFilter
import org.hyperledger.aries.pojo.PojoProcessor
import kotlin.random.Random
import kotlin.random.nextUInt


class GovController(_name: String, _url: String) : Controller(_name, _url) {
        private val schemaName = "citizen"
        private val schemaVersion = "1." + Random.nextUInt(1000u).toString()

    private fun getSchemaId() : String {
        log.debug("Getting schema Id")
        val definedSchemas = ariesClient.schemasCreated(
            SchemasCreatedFilter
                .builder()
                .schemaName(schemaName)
                .schemaVersion(schemaVersion)
                .build()
        ).get()
        val schemaId : String
        if (definedSchemas.isEmpty()) {
            log.info("Schema doesn't exist in wallet -> posting Schema $schemaName:$schemaVersion")
            schemaId = ariesClient.schemas(
                SchemaSendRequest
                    .builder()
                    .schemaName(schemaName)
                    .schemaVersion(schemaVersion)
                    .attributes(
                        PojoProcessor.fieldNames(CitizenCred::class.java).toList()
                    )
                    .build()
            ).get().schemaId
            log.info("schemaId: $schemaId")
        } else{
            log.debug("Schema already defined")
            schemaId = definedSchemas.first()
        }
        log.debug("Schema id: $schemaId")
        return schemaId
    }



    fun getCredentialDefinition() :String {
        log.info("Getting credential definition")
        val citizenCredDefWalletResp = ariesClient.credentialDefinitionsCreated(
            CredentialDefinitionFilter
                .builder()
                .schemaId(getSchemaId())
                .build()
        )

        val credDefId : String

        // check if Cred def in wallet
        if (citizenCredDefWalletResp.isPresent && citizenCredDefWalletResp.get().credentialDefinitionIds.isNotEmpty()
        ) {
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


    // Currently the Goverment issues a valid citizenship credential to anyone who connects iwth it
    override fun handleConnection(connection: ConnectionRecord?) {
        super.handleConnection(connection)
        if (connection != null) {
//            log.info("${connection.theirLabel}: ${connection.state.name}")
            if (connection.state.name == "ACTIVE" ){
                issueCredientialToConnection(connection)
            }
        }
    }

    fun issueCredientialToConnections() {
        val connections = ariesClient.connections().get()
        for (connection in connections) {
            issueCredientialToConnection(connection)
        }
    }

    private fun issueCredientialToConnection(connection: ConnectionRecord?) {
        if (connection != null) {
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
                            CredentialAttributes.from(CitizenCred(20, connection.theirLabel))
                        )
                    )
                    .build()
            )
            log.info("Credential Issued")
        }
    }

    override fun handleEvent(topic: String?, payload: String?) {
        super.handleEvent(topic, payload)
        log.info("handle event:$topic payload:$payload")
    }
}


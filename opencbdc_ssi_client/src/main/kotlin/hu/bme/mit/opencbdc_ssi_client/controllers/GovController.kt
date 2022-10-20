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


class GovController(_name: String, _url: String) : Controller(_name, _url) {

    lateinit var citizenSchemaId: String
    lateinit var citizenCredDefId: String
    lateinit var issuerDid : String

    fun prepareForCredientialIssuance() {
        getIssuerDid()
        getSchema()
        getCredentialDefinition()
    }

    private fun getIssuerDid() {
        log.info("Getting issuer DID")
        issuerDid = ariesClient.walletDidPublic().get().did
        log.info("Got issuer DID: $issuerDid")
    }

    private fun getCredentialDefinition() {
        log.info("Getting credential definition")
        log.info("Fetching credDefs from wallet")
        val citizenCredDefWalletResp = ariesClient.credentialDefinitionsCreated(
            CredentialDefinitionFilter
                .builder()
                .schemaId(citizenSchemaId)
                .build()
        )


        // check if Cred def in wallet
        if (citizenCredDefWalletResp.isPresent && citizenCredDefWalletResp.get().credentialDefinitionIds.isNotEmpty()
        ) {
            log.info("Credential definition already exists in wallet")
            citizenCredDefId = citizenCredDefWalletResp.get().credentialDefinitionIds.first()
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
                citizenCredDefId = ariesClient.credentialDefinitionsCreate(
                    CredentialDefinition.CredentialDefinitionRequest
                        .builder()
                        .schemaId(citizenSchemaId)
//                        .supportRevocation(true)
//                        .revocationRegistrySize(1000)
                        .tag("default")
                        .build()
                ).get().credentialDefinitionId
            }
//        }
        log.info("Credential definition: id: $citizenCredDefId")
    }

    private fun getSchema() {
        val schemaName = "citizen"
        val schemaVersion = "1.6"
        log.info("Getting schema Id")
        val definedSchemas = ariesClient.schemasCreated(
            SchemasCreatedFilter
                .builder()
                .schemaName(schemaName)
                .schemaVersion(schemaVersion)
                .build()
        ).get()
        if (definedSchemas.isEmpty()) {
            log.info("Schema doesn't exist in wallet -> posting Schema")
            citizenSchemaId = ariesClient.schemas(
                SchemaSendRequest
                    .builder()
                    .schemaName(schemaName)
                    .schemaVersion(schemaVersion)
                    .attributes(
                        PojoProcessor.fieldNames(CitizenCred::class.java).toList()
                    )
                    .build()
            ).get().schemaId
        } else{
            log.info("Schema already defined")
            citizenSchemaId = definedSchemas.first()
        }
        log.info("Schema id: $citizenSchemaId")
    }

    // Currently the Goverment issues a valid citizenship credential to anyone who connects iwth it
    override fun handleConnection(connection: ConnectionRecord?) {
        super.handleConnection(connection)
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
                    .issuerDid(issuerDid)
                    .connectionId(connection.connectionId.toString())
                    .credentialDefinitionId(citizenCredDefId)
                    .schemaName(citizenSchemaId.split(":")[2])
                    .schemaIssuerDid(issuerDid)
                    .schemaVersion(citizenSchemaId.split(":")[3])
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


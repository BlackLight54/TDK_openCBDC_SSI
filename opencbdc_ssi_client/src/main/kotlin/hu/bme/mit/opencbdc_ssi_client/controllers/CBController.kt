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


class CBController(_name :String , _url : String) : Controller(_name,_url) {

    fun requestProof(credentialDefinitionId : String){
        for (conn in ariesClient.connections().get()){
            requestProof(conn, credentialDefinitionId)
        }
    }
    private fun requestProof(connection: ConnectionRecord, credentialDefinitionId: String){
        log.info("Requesting proof from: ${connection.theirLabel}")
        val proofRequest = PresentProofRequestHelper.buildForAllAttributes(
            connection.connectionId,
            CitizenCred::class.java,
            listOf(PresentProofRequest.ProofRequest.ProofRestrictions.builder()
                .credentialDefinitionId(credentialDefinitionId)
                .addAttributeValueRestriction("citizenship", "Hungarian")
                .build())
        )
//        val proofRequest2 = PresentProofRequestHelper.buildProofRequest(
//            connection.connectionId,
//            mapOf("name" to ProofRequestedAttributes.builder()
//                .name("name")
//                .restriction()
//                .build())
//        )
        ariesClient.presentProofV2SendRequest(V20PresSendRequestRequest(
            true,
            "Citizenship proof for onboarding",
            connection.connectionId,
            V20PresSendRequestRequest.V20PresRequestByFormat.builder()
                .indy(proofRequest.proofRequest)
                .build(),
        false))


    }

    override fun handleProofV2(proof: V20PresExRecord?) {
        super.handleProofV2(proof)
        if (proof != null) {
            log.info("proof for ${ariesClient.connections().get().filter {conn -> conn.connectionId == proof.connectionId  }.first().theirLabel} state : ${proof.state.name}")
        }
        if (proof != null && proof.isVerified ) {
            val connection = ariesClient.connections().get().filter {conn -> conn.connectionId == proof.connectionId  }.first()
            log.info("recieved and verified proof: ${connection.theirLabel} : ${proof.isVerified}")
            issueCredientialToConnection(connection)
        }
    }




    private fun issueCbCredential(connection: ConnectionRecord?) {

    }


    fun prepareForCredientialIssuance() {
        getIssuerDid()
        getSchemaId()
        getCredentialDefinition()
    }

    private fun getIssuerDid() : String{
        val issuerDID = ariesClient.walletDidPublic().get().did
        log.info("Got issuer DID: $issuerDID")
        return issuerDID
    }

    private fun getSchemaId() : String{
        val schemaName = "CBCard"
        val schemaVersion = "1.1"
        val schemaId : String
        log.info("Getting CB schema Id")
        val definedSchemas = ariesClient.schemasCreated(
            SchemasCreatedFilter
                .builder()
                .schemaName(schemaName)
                .schemaVersion(schemaVersion)
                .build()
        ).get()
        if (definedSchemas.isEmpty()) {
            log.info("CB schema doesn't exist in wallet -> posting Schema")
            schemaId =  ariesClient.schemas(
                SchemaSendRequest
                    .builder()
                    .schemaName(schemaName)
                    .schemaVersion(schemaVersion)
                    .attributes(
                        PojoProcessor.fieldNames(CBCred::class.java).toList()
                    )
                    .build()
            ).get().schemaId
        } else{
            log.info("Schema already defined")
            schemaId = definedSchemas.first()
        }
        log.info("Schema id: $schemaId")
        return schemaId
    }
    private fun getCredentialDefinition() :String {
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
                    .issuerDid(getIssuerDid())
                    .connectionId(connection.connectionId.toString())
                    .credentialDefinitionId(getCredentialDefinition())
                    .schemaName(getSchemaId().split(":")[2])
                    .schemaIssuerDid(getIssuerDid())
                    .schemaVersion(getSchemaId().split(":")[3])
                    .credentialProposal(
                        CredentialPreview(
                            CredentialAttributes.from(CBCred(connection.theirLabel,connection.theirLabel + "::addr"))
                        )
                    )
                    .build()
            )
            log.info("Credential Issued")
        }
    }
}



package hu.bme.mit.opencbdc_ssi_client.controllers

import hu.bme.mit.opencbdc_ssi_client.credientials.CBCred
import org.hyperledger.acy_py.generated.model.ConnectionInvitation
import org.hyperledger.acy_py.generated.model.V20CredStoreRequest
import org.hyperledger.aries.api.connection.ConnectionReceiveInvitationFilter
import org.hyperledger.aries.api.connection.ReceiveInvitationRequest
import org.hyperledger.aries.api.credentials.CredentialAttributes
import org.hyperledger.aries.api.credentials.CredentialPreview
import org.hyperledger.aries.api.issue_credential_v1.V1CredentialProposalRequest
import org.hyperledger.aries.api.issue_credential_v2.V2IssueCredentialRecordsFilter
import org.hyperledger.aries.api.issue_credential_v2.V2IssueIndyCredentialEvent
import org.springframework.beans.factory.annotation.Autowired

class CitizenController(_name : String, _url : String): Controller(_name, _url) {

    @Autowired
    lateinit var cbController : CBController

    private fun acceptInvitation(invitation: ConnectionInvitation, alias: String) {
        ariesClient.connectionsReceiveInvitation(
            ReceiveInvitationRequest.builder()
                .did(invitation.did)
                .label(invitation.label)
                .recipientKeys(invitation.recipientKeys)
                .serviceEndpoint(invitation.serviceEndpoint)
                .build(),
            ConnectionReceiveInvitationFilter
                .builder()
                .alias(alias)
                .build()
        )
            .ifPresent { connection -> log.debug("{}", connection.getConnectionId()) }
    }
    fun establishConnection(entity: Controller) {
        log.info("Establishing connection: ${this.name} -> ${entity.name}")

        val invitation = entity.createInvitaion().invitation
        this.acceptInvitation(invitation, entity.name)
    }

//    fun requestCBCred(){
//        // send credential proposal to CB
//        // TODO: Implement sending proposal to CB instead of CB sending out
//        ariesClient.issueCredentialV2SendProposal(
//            V1CredentialProposalRequest
//            .builder()
//            .issuerDid(cbController.getIssuerDid())
//            .credentialDefinitionId(getCredentialDefinition())
//            .schemaName(getSchemaId().split(":")[2])
//            .schemaIssuerDid(getIssuerDid())
//            .schemaVersion(getSchemaId().split(":")[3])
//            .credentialProposal(
//                CredentialPreview(
//                    CredentialAttributes.from(CBCred(connection.theirLabel,connection.theirLabel + "::addr"))
//                )
//            )
//            .build())
//    }

}
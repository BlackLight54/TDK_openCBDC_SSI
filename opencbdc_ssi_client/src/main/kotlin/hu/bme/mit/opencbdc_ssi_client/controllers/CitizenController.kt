package hu.bme.mit.opencbdc_ssi_client.controllers

import org.hyperledger.acy_py.generated.model.ConnectionInvitation
import org.hyperledger.acy_py.generated.model.V20CredStoreRequest
import org.hyperledger.aries.api.connection.ConnectionReceiveInvitationFilter
import org.hyperledger.aries.api.connection.ReceiveInvitationRequest
import org.hyperledger.aries.api.issue_credential_v2.V2IssueCredentialRecordsFilter
import org.hyperledger.aries.api.issue_credential_v2.V2IssueIndyCredentialEvent

class CitizenController(_name : String, _url : String): Controller(_name, _url) {

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


    fun storeCredential(){
        val issueCredientialRecordResponse = ariesClient.issueCredentialV2Records(V2IssueCredentialRecordsFilter.builder().build())
        if (issueCredientialRecordResponse.isPresent && issueCredientialRecordResponse.get().isNotEmpty()) {
            for(issueCredientialRecord in issueCredientialRecordResponse.get()){
                log.info("Storing credential record: ex_id: ${issueCredientialRecord.credExRecord.credExId} ;credential_id: ${issueCredientialRecord.credExRecord.credIssue.atId}")
                ariesClient.issueCredentialV2RecordsStore(issueCredientialRecord.credExRecord.credExId, V20CredStoreRequest(issueCredientialRecord.credExRecord.credExId))
            }
        }else {
            log.info("No credential record to store")
        }
    }

}
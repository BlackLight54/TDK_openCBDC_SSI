package hu.bme.mit.opencbdc_ssi_client.controllers

import hu.bme.mit.opencbdc_ssi_client.credientials.CBCred
import hu.bme.mit.opencbdc_ssi_client.credientials.CitizenCred
import org.hyperledger.aries.api.connection.ConnectionRecord
import org.hyperledger.aries.api.message.BasicMessage
import org.hyperledger.aries.api.present_proof.PresentProofRequest
import org.hyperledger.aries.api.present_proof.PresentProofRequestHelper
import org.hyperledger.aries.api.present_proof.PresentationExchangeState
import org.hyperledger.aries.api.present_proof_v2.V20PresExRecord
import org.hyperledger.aries.api.present_proof_v2.V20PresSendRequestRequest
import org.springframework.beans.factory.annotation.Autowired
import java.io.File


class SentinelController(_name: String, _url: String) : Controller(_name, _url) {

    @Autowired
    lateinit var cbController: CBController

    private val txRecords = mutableListOf<TxRecord>()

    enum class TxStatus { PENDING, ACCEPTED, REJECTED }
    class TxRecord(
        val from_name: String , val to_name: String , val from_address: String, val to_address: String, val amount: Int, val nonce: String, var status: TxStatus
    ) {
        override fun toString(): String {
            return "cbdc:$from_name:$to_name:$from_address:$to_address:$amount:$nonce"
        }

        constructor(txData: String) : this(
            txData.split(":")[1],
            txData.split(":")[2],
            txData.split(":")[3],
            txData.split(":")[4],
            txData.split(":")[5].toInt(),
            txData.split(":")[6],
            TxStatus.PENDING
        )
    }


    // cbdc transactions travel in SSI messages in "cbdc:from_address:to_address:amount:nonce" format
    override fun handleBasicMessage(message: BasicMessage?) {
        super.handleBasicMessage(message)
        if (message != null) {
            log.info(message.content)
            if (message.content.contains("cbdc:")) {
                val txRecord = TxRecord(message.content)
                log.info("$name received a transaction from ${
                    ariesClient.connections().get().filter { conn -> conn.connectionId == message.connectionId }
                        .first().theirLabel
                }:${txRecord.from_address} to ${txRecord.to_address} for ${txRecord.amount} with nonce ${txRecord.nonce}")
                startTransaction(txRecord)
            }
        }
    }

    private fun startTransaction(txRecord: TxRecord) {
        log.info("$name is starting a transaction from ${txRecord.from_address} to ${txRecord.to_address} for ${txRecord.amount}")
        if (txRecords.any { tx ->
                tx.nonce == txRecord.nonce && tx.from_address == txRecord.from_address && tx.to_address == txRecord.to_address && tx.amount == txRecord.amount
            }) {
            log.error("Transaction already exists")
            return
        }

        txRecords.add(txRecord)
        log.debug("Transaction added to the txRecords")


        val fromName = txRecord.from_name
        val toName = txRecord.to_name
        log.info("$fromName -> $toName")

        log.debug("Getting connection record for $fromName, $toName")
        val fromConnection = ariesClient.connections().get().first { conn -> conn.theirLabel == fromName }
        val toConnection = ariesClient.connections().get().first { conn -> conn.theirLabel == toName }
        if (fromConnection == null || toConnection == null) {
            log.error("Either sender or receiver is not connected to the Sentinel")
            txRecord.status = TxStatus.REJECTED
            return
        }

        log.info("Requesting proofs for each address: ${fromConnection.theirLabel} -> ${toConnection.theirLabel}")
        requestProof(fromConnection, txRecord)
        requestProof(toConnection, txRecord)

    }

    private fun requestProof(fromConnection: ConnectionRecord, txRecord: TxRecord) {
        log.info("Requesting proof from: ${fromConnection.theirLabel}")
        val proofRequest = PresentProofRequestHelper.buildForAllAttributes(
            fromConnection.connectionId, CBCred::class.java, listOf(
                PresentProofRequest.ProofRequest.ProofRestrictions.builder()
                    .credentialDefinitionId(cbController.getCredentialDefinition()).build()
            )
        )
        ariesClient.presentProofV2SendRequest(
            V20PresSendRequestRequest(
                true,
                txRecord.toString(),
                fromConnection.connectionId,
                V20PresSendRequestRequest.V20PresRequestByFormat.builder().indy(proofRequest.proofRequest).build(),
                true
            )
        )
        log.info("Proof sent")


    }

    fun openCBDCTransact(from_name:String ,to_name: String,  from_address: String, to_addresss: String, amount: Int): String? {
        // put into the same docker network(SSI_client -> host?)
        // call through ssh
        log.info("opencbdc transaction :$from_name-$from_address -> $to_name-$to_addresss : $amount")

        val builder = ProcessBuilder();

        builder.command(
            "docker",
            "exec",
            "-i",
            "opencbdc-tx-client",
            "./build/src/uhs/client/client-cli",
            "2pc-compose.cfg",
            "${from_name}Mempool.dat",
            "${from_name}Wallet.dat",
            "send",
            to_addresss,
            amount.toString()
        )
//          builder.command("cmd.exe", "/c", "dir")
        builder.directory(File(System.getProperty("user.home")));
        val process = builder.start();
        val exitCode = process.waitFor();
        assert(exitCode == 0);
        val returnString = process.inputStream.readAllBytes().toString(Charsets.UTF_8)
        val importinput_data = returnString.split("\n")[3]
        log.info("importinput_data: $importinput_data")
        return importinput_data
    }

    override fun handleProofV2(proof: V20PresExRecord?) {
        super.handleProofV2(proof)
        if (proof != null) {

            // Proof accepted
            if (proof.state == PresentationExchangeState.DONE) {
                log.info("Proof accepted: ${proof.presRequest.comment}")
                val txRecord = getTxRecordFromRecords(TxRecord(proof.presRequest.comment))

                // Only accept if transaction is pending
                if (txRecord != null && txRecord.status == TxStatus.PENDING) {

                    // TODO : check if addresses in VP and transaction record match match
                    if (true) {
                        txRecord.status = TxStatus.ACCEPTED
                        log.info("Transaction accepted: ${txRecord.from_address} -> ${txRecord.to_address} : ${txRecord.amount}")
                        log.info("${proof.pres.presentationsTildeAttach}")
                        val importinput_data = openCBDCTransact(txRecord.from_name, txRecord.to_name, txRecord.from_address, txRecord.to_address, txRecord.amount)

                    }
                }

            } else if (proof.state == PresentationExchangeState.ABANDONED && proof.state == PresentationExchangeState.DECLINED) {
                val recTxRecord = TxRecord(proof.presRequest.comment)
                log.info("$recTxRecord was rejected")
                val txRecord = txRecords.first { txRecord ->
                    txRecord.from_address == recTxRecord.from_address && txRecord.to_address == recTxRecord.to_address && txRecord.amount == recTxRecord.amount && txRecord.nonce == recTxRecord.nonce
                }
                txRecord.status = TxStatus.REJECTED
            }
        }
    }

    private fun getTxRecordFromRecords(recTxRecord: TxRecord): TxRecord? {
        return txRecords.firstOrNull { txRecord ->
            txRecord.from_address == recTxRecord.from_address &&
                    txRecord.to_address == recTxRecord.to_address &&
                    txRecord.amount == recTxRecord.amount &&
                    txRecord.nonce == recTxRecord.nonce
        }
    }
}


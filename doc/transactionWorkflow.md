# Transaction workflow : How do we conduct payments on openCBDC with SSI?

> How does privacy provided by SSI and the features of a CBDC connect ?

> How do we combine SSI and OpenCBDC considering OpenCBDC's design consideretions?

OpenCBDC is designed to be a cash-like retail CBDC with an emphasis on privacy. It requires minimal disclosure to the Central Bank, and requires communication between transaction participants. 
Following these goals, we should use SSI to secure communication between clients, and trust between then and the Central Bank. Also increase the abilities of the Central Bank to audit transactions and comply with regulations, without compromising OpenCBDC's main advantage, performace. All while considering privacy concerns.

> What can we control with SSI in openCBDC?
# First thought: Which keys do we use as encumberance?

Difference between the two following models:
Which keys do we use to sign the transactions?
Ones that the CB knows or ones that only the participants know.

# "Cash-like" model 
> Goal: Performant and private PTP money transfer
## Workflow
The participants use the keys associated with a DID to sign the transactions. This way both of them can be sure of the identity of the parties invoved. 
This way the CB doesn't need to know anything about the participants. This way the CB can be sure that the transactions are valid, but it can't be sure of the identity of the participants. 

## What does this approach solve?
Removes the CB from the trust equation as it relates to SSI. This ensures privacy, while providing the participants a safe and efficient way to verify eachothers identities.

# "Cheque-like" model 
>Goal: Central Bank KYC

## Workflow
1. Alice creates the transaction, signs it with the private key corresponding to the one she used to recieve them, which are associated with a connection to the CB. 
2. She then presents this transaction to the recipient - Bob. 
3. He them signs it with his key associated with the CB, and send it to the sentinel.
4. The Sentinel only accepts transactions which have been signed with a key that the CB recognises, not a foreign key. 
5. The Sentinel then sends the transaction for processing
6. The Sentinel recieves the result of the transaction, and broadcasts it.
7. (If successful, it may provide a receipt VC)

This solution requires Alice and Bob to both have a trusted connection with the CB, because openCBDC needs the recipient to witness the outputs of the transaction, so he can sign them later when he spends them. 

## What does this approach solve?

### Trust between central bank and clients:
Because openCBDC requires private keys to spend funds, trust isn't needed towards the CB for transaction execution, but it helps tremendously with KYC requirements. So if the central bank is compelled to follow KYC, they may choose to follow this framework. This opens privacy concerns, because the CB can associate identities with transactions, if it chooses to store them. OpenCBDC doesn't require the storage of complete transactions,
so we can choose to avoid this problem.

### Trust between clients:
Greatly increases trust between clients, because they can present eachother VCs to prove eachothers identity. This greatly widens the possibilities of trusted payment workflows through the internet, both in terms of speed, and privacy, simply because we use SSI with a performant payment processor.

# Other concerns associated with keys
If Alice loses her private key, she will not be able to spend the funds. 
If she would like to use a different key to spend her UTxOs in the future, she will need to create a transaction to herself, where the input encumberance is the old key, and the output encumberance is the new key. 
? Does Hyperledger indie support this kind of key management?
Also, if her key is published as a DID, part of the workflow would need to refresh that too.

# Second thought: Upgrading Sentinels
Sentinels may provide additional functionality, such as recieving VCs(eg. KYC checks), providing receipts, escrow etc. as they are the outwards facing interface of the CBDC.
They may be more deeply incorporated into public and private workflows.

Because they are horizontally scalable, additional functions implemented in Sentinels would have little impact on overall performace.

## Using the advantages of VCs as gatekeeping
> Goal: Only eligible entites can use the CBDC
### Issuing a "Credit Card"
Upon the cutomers request, the CB issues a "Credit Card" VC, if they can prove they satisfy legal requirements. This VC is associated with a single public key within the CB.
Then if Alice and Bob would like to transact CBDC, shew must present her "Credit Card" as a VP along with their transaction.

### Showing your "ID"
One simpler solution would be to ask for proof of citizenship and age as gatekeeping the CBDC system along with each transaction.

### Effects of gatekeeping
It is important that only legally eligible individuals use the payment processor, which is why we may use cryptographic or VC based gatekeeping.
The CB may to ensure that each citizen only has one CBDC "address" associated with them, which can help in regards to monetary policy.

## VCs as receipts
VCs can be used as receipts, which can be used to prove that a transaction has been executed. This is a great way to increase trust between clients, and also between the CB and clients. Clients may provide VCs to prove that they paid their rent or paid their taxes. Combined with the CB facilitated payment processor, which stores which transactions have been executed, these receipts function as undeniable, government backed proofs of payment. 
A lentire jutottam (draftban), akárhogy tekerem, AnonCred alapon nem működik (valójában ha lenne hidden attributes a master secret-en túl az AnonCred-ben, akkor pofon egyszerű lenne, de úgy látom, hogy nincs). Viszont ez már tényleg működőnek tűnik (modulo a ZKP).

Az eleje viszonylag egyszerű, a végén van egy ránézésre szörnyű, de várhatóan egyszerűen “komponálható” ZKP, azzal kapcsolatban emlékeztess, hogy beszéljek Bertalannal, megnézzük ZoKratesban.

---

Privacy goal: ability to send to and receive from Bitcoin-like addresses in a UTXO setting, without no party being practically able to correlate identities and money flow easier than it is for Bitcoin.

In our model, making digital money as or more “inconvenient” – compared to electronic currency – to use as cash translates to ensuring the following properties:

- No person can hold more than a policy-determined amount at any time.
- No person can send or receive more money in a time frame than allowed. (Amount and time policy determined.)
- No “handover” may occur without physical closeness.
  The following model aims to address “smurfing” issues predominantly across the addresses of a single person. The possibility of smurfing by multiple people is present but deemed a controllable/nonissue aspect due to the nature of the controls we introduce.

We assume that every citizen holds a government-issued VC.

Actors include the user; the CB, which issues time- and amount-limited spend- and receive authorizations to users; and extended openCBDC sentinels, which consume those.

TODO: extend for multiple inputs and outputs.

---

The central bank (CB) maintains the following data structure for every citizen-VC:

- hash(current_balance+salt) – current balance committment
- addresses_merkle_root – the root of a merkle tree for the addresses of a citizen
- last_spend_request_tstp – timestamp of the last approved spend request
- last_receive_request_tstp – timestamp of the last approved receive request
  Citizens register a commitment on their addresses and current balance. Balance is needed for maximum amount control (see later); addresses will be needed to account for the source and target of each performed transfer.

---

Citizens can update their merkle tree root. Currently, there is no defense against sharing addresses between people, but as that would only limit the “spending power” or “receiving power” of an address, there does not seem to be any value in that. Maliciously registering “somebody else’s address” is an attack vector, however, we do not detail defenses against that here. (Options include, e.g., limiting merkle tree size for everybody through ZKP obligation on merkle root change + update frequency limits).

When a citizen wishes to spend money, it gives encrypted(from|sentinelID) to the Sentinel and gets it blindly signed. The key used for signing determines the max_amount (quasi-note) and the intra-day interval (hour, 5 minutes, …) for spend token validity.

Ideally, citizen should also prove that the from address is in its registered address set in a privacy-preserving way, but that’s problematic with current ZKP technology (encryption proving). And this will be checked at the protocol anyhow.

After decryption, citizen has signed(from|sentinelID) spend-token.

Clients are interested in privacy, which at this step translates to making unlinkable the issuance and usage of spend-tokens.

Unlinkability on the Sentinel side – assuming malicious cooperation with the CB – needs that usable_until and max_amount can’t be used for correlation efficiently. (E.g., periodic usable_until’s for everybody and same max_amount).

Then, if the usable_until window is long enough, the system is used by enough clients and the clients all perform a random waiting phase between receiving the spend token and using it, timing-based linking of issued and used credentials should not be practically feasible, either.

The CB is interested in enforcing maximum spend limits (by setting max_amount) for a policy-defined time window by each citizen. Therefore, it will refuse to issue another spend token before a cooloff time (e.g., before the previously issued has reached it usable_until time).

For receiving money, a receive token is created, which contains a to address.

Here, the CB is interested in enforcing maximum holding limits. Therefore, it will issue the token only if the citizen can present a proof that for the hash-committed current_balance, current_balance + max_amount < policy_limit. This is simple in ZoKrates and can be interactive ZKP, too. (ZoKrates supports non-interactive zk-SNARKs.)

The receive-authorization is similarly time managed as the spend authorization.

---

Alice and Bob agree that they want to transact. They already have a spend and a receive token to the same Sentinel, or fetch one at this point. They agree on a random transaction key.

---

Bob as a receiver anonymously turns to the Sentinel, and

- registers the transaction key,
- presents the spend token, which the Sentinel records to avoid double use,
- and declares the amount.
  Alice does similarly for the spend token.

Both prove the ownership of their addresses via digitally signing the request.

At this point, if physical closeness is required for the transfers, mobile infrastructure can be involved which can provide timestamped geolocation attestation for senders as well as receivers.

Alice now can issue a “normal” openCBDC transaction.

---

Upon completion, based on the assumption that the from address was registered for Alice and she had a single spend token for that, the following protocol can be followed.

---

The Sentinel gives Alice a receipt: signed(from|amount|timestamp).

Alice turns to the Sentinel, who knows that she had a spend allowance for a time window, presents hash(signed(from|amount|timestamp)|salt) and hash(newbalance|salt2) and ZKP proves the following.

- It knows signed(from|amount|timestamp) and salt and these lead to the presented hash.
- The signature is valid for the Sentinel on signed(from|amount|timestamp).
- Timestamp is valid for the issued allowance.
- From is in the Merkle tree of the addresses of the citizen.
- It knows salt2, newbalance and salt2 hash to the presented value.
- Newbalance is the original balance – amount.
- The same happens on the receiver side.

---

If the spend or receive tokens are not used after all for any reason, then the Sentinel can be requested, even after the validity passed, to issue a receipt with zero value.

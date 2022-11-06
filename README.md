# Payments in openCBDC with Self-Sovereign Identitites – from the verifiable to the private -- Scientific Students’ Association Report

[Report Tex on Overleaf](https://www.overleaf.com/read/cgdhvvjsddyq)

## OpenCBDC Aries Client
A Spring client application containing components capable of facilitating my Auditable Payment protocol. 

## Prerequisites 
### openCBDC transaction pocessor 
To run the system, you will need to have the openCBDC transaction processor running. You can find the instructions for setting up the transaction processor [here](https://github.com/mit-dci/opencbdc-tx).
One thing to not is that there were feature braking changes introduced in recent releases, so it is advised to use a known stable version. For this reason, 'docker\docker-compose-prebuilt-2pc.yml' uses realase cb0e78d. I advise to use this compose file.
### Aries Cloud Agents 
To run the Aries Cloud Agents, use the following command: 
` docker compose -f ./docker/docker-compose.yml --env-file ./docker/compose.env up`
This will run an agent for each component, on the correct ports.

## Running the applicaiton
The application is a simple Spring application compiled in maven. The most components realise actors within the protocol, while one component acts as the experiment orchestrator. It exposes endpoints for the different steps of the protocol.
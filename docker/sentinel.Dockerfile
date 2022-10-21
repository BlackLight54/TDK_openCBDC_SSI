# Sentinel agent may interact with the openCBDC sentinel through SSH
FROM bcgovimages/aries-cloudagent:py36-1.16-1_0.7.5-rc0
USER root
# Install SSH
RUN apt-get update && apt-get install -y ssh
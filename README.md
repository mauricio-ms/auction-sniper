# Auction Sniper
This project is the code developed from the reading of the book Growing Object-Oriented Software Guided by Tests written by Steve Freeman and Nat Pryce.

## Getting Started
- Download the OpenFire Server https://www.igniterealtime.org/downloads/
- Install the OpenFire Server
- Access http://localhost:9090 to verify that OpenFire server is up
  - User: admin
  - Pass: admin
- Create the following users:
  
    | User               | Password |
    | ------------------ |----------|
    | sniper             | sniper   |
    | auction-item54321 | auction  |
    | auction-item65432 | auction  |
- At `server > system properties`, configure the property `auctionsniper.xmpp.domain=localhost`
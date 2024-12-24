```mermaid
sequenceDiagram
    participant S as Sender
    participant WS as WebSocket Server
    participant H as Message Handler
    participant R as Redis
    participant K as Kafka
    participant M as MongoDB
    participant RC as Receiver

    S->>WS: Connect & Authenticate
    WS->>R: Store Session
    S->>WS: Send Message
    WS->>H: Process Message
    H->>M: Save Message
    H->>K: Publish Message
    
    alt Receiver Online
        K->>RC: Push Message
        RC->>H: Acknowledge
        H->>M: Update Status
    else Receiver Offline
        K->>H: Queue Message
        H->>M: Mark Pending
    end


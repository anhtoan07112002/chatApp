```mermaid
sequenceDiagram
    participant C as Client
    participant WS as WebSocket Manager
    participant H as Message Handler
    participant P as Message Processor
    participant R as Repository
    participant Q as Queue Producer
    
    C->>WS: Send Message
    WS->>H: Process Request
    H->>P: Validate & Enrich
    P->>R: Save Message
    P->>Q: Publish Event
    Q-->>C: Acknowledge
    Q->>WS: Broadcast to Recipients
```mermaid
sequenceDiagram
    participant A as Client A
    participant C as Controller
    participant UC as Use Case
    participant DS as Domain Service
    participant DB as MongoDB
    participant Cache as Redis
    participant WS as WebSocket
    participant KF as Kafka
    participant B as Client B

    A->>C: POST /api/messages
    C->>UC: SendMessageUseCase.execute()
    UC->>Cache: Check user status
    Cache-->>UC: User status
    UC->>DS: createMessage()
    DS->>DB: save message
    DS->>WS: send to recipient
    WS->>B: deliver message
    B-->>WS: message received
    WS-->>DS: delivery confirmation
    DS->>DB: update status
    UC->>KF: publish MessageCreatedEvent
    KF->>Cache: update chat history
    B-->>WS: read receipt
    WS-->>DS: update read status
    DS->>DB: update message status
```mermaid
graph TD
    subgraph Client Layer
        A[Web Client] & B[Mobile Client]
        C[WebSocket Connection]
        D[Connection Manager]
    end

    subgraph Message Service
        E[WebSocket Server]
        F[Connection Pool]
        G[Message Handler]
        H[Message Validator]
        
        subgraph Message Storage
            I[(MongoDB - Messages)]
            J[(Redis - User Sessions)]
        end
        
        subgraph Message Queue
            K[Kafka - Message Topics]
            L[Kafka - Delivery Status]
        end
    end

    A & B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    G --> H
    H --> I
    H --> J
    G --> K
    K --> L
```mermaid
graph TB
    subgraph Presentation Layer
        C[Controller] --> |1. HTTP Request| V[Validator]
        V --> |2. Validated DTO| UC[Use Case]
    end

    subgraph Application Layer
        UC --> |3. Execute| DS[Domain Service]
        UC --> |9. Publish| EV[Event Publisher]
    end

    subgraph Domain Layer
        DS --> |4. Create| M[Message Entity]
        DS --> |5. Save| RI[Repository Interface]
        DS --> |6. Send| MSI[Message Sender Interface]
    end

    subgraph Infrastructure Layer
        RI --> |7a. Persist| MongoDB[(MongoDB)]
        MSI --> |7b. Deliver| WS[WebSocket]
        EV --> |10. Publish| KF[Kafka]
    end

    subgraph External Systems
        WS --> |8. Real-time delivery| Client[Client B WebSocket]
        KF --> |11. Notify| NS[Notification Service]
        KF --> |11. Update| CS[Cache Service]
    end

    subgraph Supporting Services
        Redis[(Redis Cache)]
        ELK[ELK Stack]
        Prometheus[Prometheus]
    end

    %% classDef layer fill:#f9f,stroke:#333,stroke-width:2px
    %% class Presentation Layer,Application Layer,Domain Layer,Infrastructure Layer,External Systems,Supporting Services layer
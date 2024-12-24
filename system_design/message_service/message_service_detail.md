```mermaid
graph TD
    subgraph Message Service
        A[API Layer] --> B[Message Handler]
        B --> C[Message Processor]
        C --> D[Message Repository]
        
        B --> E[WebSocket Manager]
        C --> F[Queue Producer]
        
        G[Event Consumer] --> C
        
        subgraph Domain Layer
            H[Message Entity]
            I[Conversation Entity]
            J[Attachment Handler]
        end
        
        subgraph Infrastructure
            D --> K[(Message DB)]
            D --> L[(Redis Cache)]
            F --> M[Message Queue]
            E --> N[Socket Connections]
        end
    end

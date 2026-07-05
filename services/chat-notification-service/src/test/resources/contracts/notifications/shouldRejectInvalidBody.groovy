package contracts.notifications

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Responde 400 cuando el cuerpo es invalido (type en blanco) pese a token valido"
    priority 50
    request {
        method POST()
        url "/api/v1/chat/internal/notifications"
        headers {
            contentType applicationJson()
            header "X-Service-Token", value(consumer(regex('.+')), producer('internal-token'))
        }
        body(
                recipientId: value(consumer(regex(uuid())), producer('11111111-1111-1111-1111-111111111111')),
                type: '',
                title: 'Notification',
                message: 'New request'
        )
    }
    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
        }
        body(
                status: 400,
                error: 'VALIDATION_ERROR'
        )
    }
}

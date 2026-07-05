package contracts.notifications

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Rechaza con 403 cuando falta la cabecera X-Service-Token"
    priority 50
    request {
        method POST()
        url "/api/v1/chat/internal/notifications"
        headers {
            contentType applicationJson()
        }
        body(
                recipientId: value(consumer(regex(uuid())), producer('11111111-1111-1111-1111-111111111111')),
                type: 'REQUEST_CREATED',
                title: 'Notification',
                message: 'New request'
        )
    }
    response {
        status FORBIDDEN()
        headers {
            contentType applicationJson()
        }
        body(
                status: 403,
                error: 'FORBIDDEN'
        )
    }
}

package contracts.notifications

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Crea una notificacion interna (201) cuando pulls-service envia un X-Service-Token valido"
    priority 1
    request {
        method POST()
        url "/api/v1/chat/internal/notifications"
        headers {
            contentType applicationJson()
            header "X-Service-Token", value(consumer(regex('.+')), producer('internal-token'))
        }
        body(
                recipientId: value(consumer(regex(uuid())), producer('11111111-1111-1111-1111-111111111111')),
                type: value(consumer(regex('.+')), producer('REQUEST_CREATED')),
                title: value(consumer(regex('.+')), producer('Notification')),
                message: value(consumer(regex('.+')), producer('New request'))
        )
    }
    response {
        status CREATED()
        headers {
            contentType applicationJson()
        }
        body(
                id: anyUuid(),
                type: fromRequest().body('$.type'),
                title: fromRequest().body('$.title'),
                message: fromRequest().body('$.message'),
                read: false,
                createdAt: anyNonBlankString()
        )
    }
}

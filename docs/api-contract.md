# GigU API Contract

## Access (`/api/v1/access`)
- `POST /sign-up` public
- `POST /login` public
- `GET /me` bearer
- `GET /freelancer-profiles/{userId}` public
- `PATCH /freelancer-profiles/me` bearer
- `POST /freelancer-profiles/me/portfolio-items` bearer multipart
- `DELETE /freelancer-profiles/me/portfolio-items/{itemId}` bearer

## Marketplace (`/api/v1/marketplace`)
- `GET /services` public
- `GET /services/{id}` public
- `GET /services/mine` bearer
- `POST /services` bearer
- `PATCH /services/{id}` bearer
- `DELETE /services/{id}` bearer
- `POST /services/{id}/media` bearer multipart
- `DELETE /services/{id}/media/{mediaId}` bearer
- `GET /categories` public

## Engagement (`/api/v1/engagement`)
- `POST /requests` bearer
- `GET /requests/incoming` bearer
- `GET /requests/outgoing` bearer
- `PATCH /requests/{id}/decision` bearer
- `GET /projects` bearer
- `GET /projects/{id}` bearer
- `PATCH /projects/{id}/status` bearer
- `POST /projects/{id}/reviews` bearer
- `GET /projects/{id}/reviews` bearer
- `POST /price-suggestions` bearer

## Chat (`/api/v1/chat`)
- `POST /conversations` bearer
- `GET /conversations` bearer
- `GET /conversations/{id}` bearer
- `GET /conversations/{id}/messages` bearer
- `POST /conversations/{id}/messages` bearer
- `GET /notifications` bearer
- `PATCH /notifications/{id}/read` bearer
- `PATCH /notifications/read-all` bearer
- `POST /reports` bearer
- `POST /support-tickets` bearer
- `POST /internal/notifications` service token (`X-Service-Token`)

## Standard Error Response
```json
{
  "timestamp": "2026-05-05T20:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request contains invalid fields.",
  "path": "/api/v1/marketplace/services",
  "fields": {
    "basePrice": "must be greater than 0"
  }
}
```

## Standard Error Codes
- `VALIDATION_ERROR` (400)
- `BAD_REQUEST` (400)
- `UNAUTHORIZED` (401)
- `FORBIDDEN` (403)
- `RESOURCE_NOT_FOUND` (404)
- `EMAIL_ALREADY_EXISTS` (409)
- `INVALID_PROJECT_STATUS_TRANSITION` (409)
- `DUPLICATED_REVIEW` (409)
- `INTERNAL_SERVER_ERROR` (500)
- `EXTERNAL_SERVICE_UNAVAILABLE` (503)

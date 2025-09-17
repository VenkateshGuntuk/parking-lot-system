# Parking Lot Management System

Run:
- Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `ADMIN_EMAILS` env vars (optional for local; defaults provided).
- `./mvnw spring-boot:run`

H2 Console: `/h2-console` (JDBC URL: `jdbc:h2:mem:parkingdb`)

APIs (OAuth2 Google login required):
- `POST /api/user/enter` body: `{ "plateNo":"KA01AB1234", "vehicleType":"CAR", "ownerEmail":"user@example.com" }`
- `GET /api/user/amount/{ticketId}`
- `POST /api/user/pay` body: `{ "ticketId": 1 }`
- Admin:
  - `POST /api/admin/slots?floor=0&number=1&type=CAR`
  - `GET /api/admin/slots`
  - `DELETE /api/admin/slots/{id}`
  - `GET /api/admin/pricing/preview?type=CAR&minutes=180`

Notes:
- Slot allocation uses a strategy interface; default is nearest (lowest floor then number) and uses DB-level pessimistic locking to avoid double allocation.
- Payment is atomic in a transaction: slot freed only on success.
- Duplicate vehicle entry is prevented by checking active tickets for the same plate.
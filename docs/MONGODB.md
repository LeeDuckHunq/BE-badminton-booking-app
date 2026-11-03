# MongoDB setup

The backend now uses Spring Data MongoDB instead of JPA/MySQL.

## Connection

Set the `MONGODB_URI` environment variable before starting the application:

```powershell
$env:MONGODB_URI = "mongodb+srv://<user>:<password>@<cluster>/smashzone?retryWrites=true&w=majority"
.\mvnw.cmd spring-boot:run
```

If `MONGODB_URI` is not set, the application uses:

```text
mongodb://localhost:27017/smashzone
```

## Collections

The API uses these collections while preserving the existing JSON contract:

- `users`
- `CumSan`
- `San`
- `HinhAnhSan`
- `Favorite`
- `PhieuDatSan`
- `HoaDon`
- `KhuyenMai`
- `ChiTietKhuyenMai`
- `QR`
- `PhongChat`
- `ThanhVienPhongChat`
- `TinNhan`
- `_booking_locks` (internal, do not migrate)

Entity identifiers are stored by MongoDB in `_id`. For example, `maSan`,
`maCumSan`, and `username` become `_id` in their respective collections.

## Existing MySQL data

The application does not copy existing MySQL records automatically. Export and
transform the old tables before importing them into MongoDB. In every collection,
rename the old primary-key column to `_id`. Auto-increment identifiers from
`ChiTietKhuyenMai` and `ThanhVienPhongChat` may be imported as strings or omitted
so MongoDB creates them.

Do not import `_booking_locks`; it is runtime coordination data used to serialize
concurrent booking requests for the same court.

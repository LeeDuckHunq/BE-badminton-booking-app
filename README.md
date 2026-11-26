# SmashZone – Backend API

API cho hệ thống đặt sân cầu lông SmashZone: Spring Boot 4 (Java 21) + MongoDB, xác thực bằng JWT.

Dự án gồm 3 repo:

| Repo | Vai trò |
|---|---|
| **BE-badminton-booking-app** (repo này) | API |
| [badminton-booking-app](https://github.com/LeeDuckHunq/badminton-booking-app) | App khách hàng (Flutter) |
| [smashzone-dashboard](https://github.com/LeeDuckHunq/smashzone-dashboard) | Trang quản trị cho chủ sân / admin (React) |

Bản đang chạy thật: `https://be-badminton-booking-app-production.up.railway.app` (Railway tự deploy khi push lên `main`).

> Muốn test thoải mái (tạo, huỷ, sửa dữ liệu) thì chạy BE ở máy mình theo hướng dẫn dưới đây, **đừng thử nghiệm trên bản thật**.

## 1. Cần cài

- **JDK 21** trở lên (`java -version`).
- **Docker Desktop**: để chạy MongoDB (hoặc tự cài MongoDB 7 ở cổng 27017).
- **Node.js 18+**: chỉ cần cho script tạo dữ liệu demo.

Không cần cài Maven, repo có sẵn `mvnw`.

## 2. Chạy MongoDB

```bash
docker compose up -d
```

Mongo chạy ở `localhost:27017`, database `smashzone`. Dừng: `docker compose down`. Xoá sạch dữ liệu: `docker compose down -v`.

## 3. Chạy BE

BE đọc cấu hình từ biến môi trường. Khi chạy local, chỉ cần 3 biến:

| Biến | Bắt buộc | Ý nghĩa |
|---|---|---|
| `JWT_SECRET` | Có | Chuỗi bất kỳ, **tối thiểu 64 ký tự** |
| `ADMIN_USERNAME` | Nên có | Tài khoản admin được tạo tự động khi khởi động |
| `ADMIN_PASSWORD` | Nên có | Mật khẩu admin (tối thiểu 8 ký tự) |

Windows (PowerShell):

```powershell
$env:JWT_SECRET = "local-dev-secret-local-dev-secret-local-dev-secret-local-dev-secret"
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "adminpass123"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8089"
```

macOS / Linux / Git Bash:

```bash
export JWT_SECRET="local-dev-secret-local-dev-secret-local-dev-secret-local-dev-secret"
export ADMIN_USERNAME=admin ADMIN_PASSWORD=adminpass123
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8089
```

Thấy dòng `Started AlopoApiApplication` là xong. Kiểm tra: mở <http://localhost:8089/cumsan/10> sẽ thấy `[]` (hoặc danh sách cụm sân).

> Các ví dụ trong README đều dùng cổng **8089**. Nếu bỏ `--server.port`, BE chạy ở cổng 8080.

Các biến khác (để trống khi chạy local):

| Biến | Mặc định | Ghi chú |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/smashzone` | |
| `CORS_ALLOWED_ORIGINS` | `*` | Bản thật đặt đúng domain dashboard/app |
| `JWT_EXPIRATION_HOURS` | `12` | |
| `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY` | trống | Dùng để upload ảnh bill chuyển khoản. Không có thì chức năng upload bill báo lỗi, các chức năng khác vẫn chạy |

Thông tin thật của bản production (mật khẩu, khoá…) **không nằm trong repo**. Cần thì hỏi trưởng nhóm.

## 4. Dữ liệu demo

Sau khi BE đã chạy (bước 3), trên một database **trống**:

```bash
node scripts/seed-demo.mjs
```

Script tạo 4 cụm sân, 12 sân, voucher, đơn đặt, hoá đơn, tin nhắn, đánh giá và các tài khoản:

| Vai trò | Tài khoản | Mật khẩu | Ghi chú |
|---|---|---|---|
| Admin | `admin` | giá trị `ADMIN_PASSWORD` | Toàn quyền dashboard |
| Chủ sân | `hoanganh` | `owner1234` | Cụm Quận 1 (có giá sáng sớm, cao điểm, cuối tuần), Phú Nhuận |
| Chủ sân | `tanbinh` | `owner1234` | Cụm Tân Bình, Thủ Đức |
| Khách | `tuananh` | `secret12` | Có đơn chờ duyệt, đã xác nhận, hoàn thành (chưa đánh giá), đã huỷ; có voucher, yêu thích, tin nhắn |
| Khách | `minhkhang` | `secret12` | Trưởng nhóm "CLB Cầu Lông Quận 1 Chill", chủ trận đôi tối mai (có 1 người chờ duyệt) |
| Khách | `mailinh` | `secret12` | Trưởng nhóm riêng tư "Hội Smash Tân Bình", chủ trận vãng lai "Tìm 2 bạn đánh đôi TB+" |
| Khách | `quochuy` | `secret12` | Thành viên nhóm Quận 1 (chưa điểm danh trận nhóm) |
| Khách | `thanhtruc` | `secret12` | Đang xin vào trận của minhkhang |
| Khách | `ducminh` | `secret12` | Trưởng nhóm "Team Giao Lưu Thủ Đức", đang xin vào nhóm Quận 1 |

Mã mời của các nhóm được in ra cuối lúc chạy script.

Script chỉ chạy được với BE ở `localhost` và cần container Mongo tên `smashzone-mongo` (của `docker-compose.yml`). Muốn làm lại từ đầu:

```bash
docker compose down -v && docker compose up -d
# khởi động lại BE (bước 3), rồi:
node scripts/seed-demo.mjs
```

## Giá theo khung giờ

Mỗi sân có `gia` (giá mặc định mỗi giờ) và `bangGia`: danh sách khung `{ thu: [1..7], tuGio, denGio, gia, ten }` (1 = Thứ Hai … 7 = Chủ Nhật, giờ theo bước 30 phút, `denGio` có thể là `24:00`). Các khung trong cùng một ngày không được chồng giờ. Tiền một lượt đặt được tính theo từng phút: phút nào thuộc khung nào thì theo giá khung đó, còn lại theo giá mặc định (`PricingService`). Web, app, dashboard tính lại y hệt để hiển thị.

## 5. Chạy test

```bash
./mvnw test        # Windows: .\mvnw.cmd test
```

Unit test không cần MongoDB.

## 6. Phân quyền

| Vai trò | Được làm |
|---|---|
| Khách (`ROLE_USER`) | Đặt sân, upload bill, xem lịch sử, nhận voucher, yêu thích, chat, đánh giá lần đã chơi |
| Chủ sân (`ROLE_OWNER`) | Quản lý cụm sân / sân / đơn đặt **của mình**, đặt lịch hộ khách, xem lịch sân, trả lời đánh giá |
| Admin (`ROLE_ADMIN`) | Toàn bộ quyền chủ sân trên mọi cụm sân, quản lý tài khoản & phân quyền, ẩn đánh giá |

Tài khoản chủ sân / admin chỉ được tạo từ dashboard (mục Tài khoản & phân quyền) bởi admin. Đăng ký trên app luôn ra tài khoản khách.

## 7. Danh sách API chính

Gửi token qua header `Authorization: Bearer <token>` (lấy từ `POST /login`). Lỗi trả về dạng `{ "status": ..., "message": "..." }`.

**Công khai (không cần đăng nhập)**

| Method | Đường dẫn | |
|---|---|---|
| POST | `/login`, `/register` | Đăng nhập / đăng ký khách |
| GET | `/cumsan/{soLuong}`, `/cumsan/cum-san-info/{id}`, `/cumsan/top-10-cum-san` | Cụm sân (kèm giá từ, số sân, điểm đánh giá) |
| GET | `/san/get-san/{maCumSan}`, `/san/san-info/{maSan}` | Sân |
| GET | `/anh-san`, `/anh-san/{maCumSan}` | Ảnh cụm sân |
| GET | `/phieu-dat-san/theo-ngay` | Lịch đã đặt theo ngày (ẩn thông tin khách) |
| GET | `/danh-gia/cum-san/{maCumSan}` | Đánh giá công khai của cụm sân |
| GET | `/tran/tim-kiem?ngay=&trinhDo=&kieuChoi=&maCumSan=&conCho=` | Tìm trận vãng lai |
| GET | `/nhom/kham-pha?q=&trinhDo=` | Nhóm công khai |

**Khách (đã đăng nhập)**

| Method | Đường dẫn | |
|---|---|---|
| POST | `/phieu-dat-san/create` | Đặt sân (giá do server tính) |
| GET | `/phieu-dat-san/get-theo-user/{username}` | Lịch sử đặt của chính mình |
| POST | `/files/payment-proof` | Upload ảnh bill |
| POST / GET | `/hoa-don/tao-hoa-don`, `/hoa-don/get-hoa-don/{maPhieuDat}` | Hoá đơn |
| GET / POST | `/khuyen-mai/...` | Voucher |
| GET / POST / DELETE | `/yeu-thich/...` | Yêu thích |
| GET / POST | `/chat/...` | Tin nhắn |
| POST / GET | `/danh-gia`, `/danh-gia/cua-toi` | Gửi / sửa đánh giá, đánh giá của tôi |
| GET | `/tran/cua-toi`, `/tran/phieu-dat-kha-dung`, `/tran/{id}` | Trận của tôi, lượt đặt có thể mở trận, chi tiết trận |
| POST / PUT | `/tran`, `/tran/{id}`, `/tran/{id}/huy` | Mở trận từ phiếu đặt của mình, sửa, huỷ (chủ trận) |
| POST | `/tran/{id}/tham-gia`, `/tran/{id}/roi` | Tham gia / xin vào, rời trận / báo vắng |
| POST / DELETE | `/tran/{id}/duyet/{username}`, `/tran/{id}/nguoi-choi/{username}` | Chủ trận duyệt, mời ra |
| POST | `/tran/{id}/thanh-toan`, `/tran/{id}/thanh-toan/{username}` | Báo đã chuyển tiền (kèm bill), chủ trận xác nhận |
| GET / POST | `/nhom/cua-toi`, `/nhom`, `/nhom/{id}`, `/nhom/tham-gia-bang-ma` | Nhóm của tôi, tạo nhóm, chi tiết, vào bằng mã mời |
| POST / DELETE | `/nhom/{id}/tham-gia`, `/nhom/{id}/roi`, `/nhom/{id}/duyet/{u}`, `/nhom/{id}/vai-tro/{u}`, `/nhom/{id}/thanh-vien/{u}` | Xin vào, rời, duyệt, phân quyền, mời ra |
| GET / POST | `/nhom/{id}/tin-nhan`, `/nhom/{id}/tran` | Chat nhóm, trận của nhóm |
| POST / DELETE | `/phieu-dat-san/{id}/yeu-cau-huy` | Xin huỷ lượt đã xác nhận (body `{lyDo}`, trước giờ chơi ≥ 30 phút; tự nhắn cho chủ sân, trả về liên hệ Zalo) / rút yêu cầu |
| GET | `/thong-bao`, `/thong-bao/chua-doc` | Thông báo của tôi (60 mới nhất + số chưa đọc) |
| GET | `/thong-bao/chua-gui` | Thông báo chưa đẩy lên điện thoại (app Android gọi khi ở nền; gọi xong coi như đã gửi) |
| POST | `/thong-bao/{id}/da-doc`, `/thong-bao/da-doc-het` | Đánh dấu đã đọc |

**Quản lý (`/management/**`, chủ sân hoặc admin)**

| Method | Đường dẫn | |
|---|---|---|
| GET | `/management/me`, `/management/overview` | Hồ sơ, số liệu tổng quan |
| GET / POST / PUT / DELETE | `/management/venues`, `/management/courts` | Cụm sân, sân (kèm `bangGia`: giá theo khung giờ) |
| POST | `/management/courts/{id}/copy-pricing` | Áp dụng bảng giá của sân cho các sân khác cùng cụm |
| GET / POST | `/management/bookings` | Danh sách đơn, đặt lịch hộ khách |
| GET / PATCH | `/management/bookings/{id}`, `/management/bookings/{id}/status` | Chi tiết, đổi trạng thái đơn |
| PATCH | `/management/bookings/{id}/court` | Đổi sang sân khác cùng cụm còn trống (body `{maSan, lyDo}`; giữ giờ và giá, báo khách + người chơi trong trận) |
| POST | `/management/bookings/{id}/cancel-request` | Xử lý yêu cầu huỷ của khách (body `{accept, ghiChu}`; đồng ý = huỷ đơn, nhả khung giờ) |
| GET | `/management/timeline?date=&venue=` | Lịch sân theo ngày |
| GET | `/management/customers?q=` | Tìm khách để đặt hộ |
| GET / PATCH | `/management/reviews`, `/management/reviews/{id}/reply` | Đánh giá, trả lời |
| GET | `/management/matches?venue=` | Trận ghép tại cụm sân của mình |
| * | `/management/admin/**` | Chỉ admin: tài khoản, phân quyền, ẩn đánh giá, danh sách / khoá nhóm |

### Thông báo

Máy chủ tạo thông báo khi: chủ sân xác nhận / huỷ / hoàn thành đơn, đổi sân, đặt hộ có gắn tài khoản, khách gửi bill (báo chủ sân), khách xin huỷ / rút yêu cầu, chủ sân từ chối huỷ, tin nhắn mới (gộp theo người gửi), ghép trận (xin vào, được duyệt / từ chối, huỷ trận, lên danh sách chờ, báo đã chuyển tiền) và chủ sân trả lời đánh giá. Đơn bị huỷ thì trận ghép đang mở gắn với đơn cũng huỷ theo.

Web và dashboard hỏi `/thong-bao` mỗi 20 giây (kèm thông báo trình duyệt khi tab ẩn). App Android: khi mở app hỏi mỗi 20 giây; khi app ở nền, WorkManager gọi `/thong-bao/chua-gui` (khoảng 1 phút/lần trong 30 phút đầu, sau đó 15 phút/lần) và hiện thông báo hệ thống — không cần Firebase.

## 8. Cấu trúc thư mục

```
src/main/java/com/example/alopoapi/
├── controller/   REST controller
├── service/      Nghiệp vụ (đặt sân, quản lý, đánh giá…)
├── repository/   Spring Data MongoDB
├── entity/       Document MongoDB
├── dto/          Request / response
├── security/     JWT, SecurityConfig, AccessGuard
└── config/       CORS, xử lý lỗi, khởi tạo admin
scripts/seed-demo.mjs   Tạo dữ liệu demo
docs/MONGODB.md         Ghi chú về các collection
```

## 9. Lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| `JWT_SECRET must contain at least 64 characters` | Đặt `JWT_SECRET` dài ≥ 64 ký tự |
| `Port 8089 was already in use` | Tắt chương trình đang dùng cổng, hoặc đổi `--server.port` |
| Treo khi khởi động / lỗi kết nối Mongo | Docker Desktop chưa bật, hoặc chưa `docker compose up -d` |
| Seed báo lỗi ở `POST /register` | Database không trống (đã seed rồi). Chạy `docker compose down -v`, bật lại Mongo và BE rồi seed lại |
| Seed báo lỗi `docker exec` | Docker Desktop chưa bật, hoặc Mongo không chạy bằng `docker-compose.yml` (đặt `MONGO_CONTAINER=<tên container>`) |
| Upload bill lỗi | Chưa cấu hình Supabase (bình thường khi chạy local) |

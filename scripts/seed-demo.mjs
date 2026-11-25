// Tạo dữ liệu demo cho BE chạy LOCAL (xem README, mục "Dữ liệu demo").
//   node scripts/seed-demo.mjs
// Biến môi trường (tuỳ chọn):
//   API_URL          mặc định http://localhost:8089
//   MONGO_CONTAINER  mặc định smashzone-mongo (container trong docker-compose.yml)
//   ADMIN_USERNAME / ADMIN_PASSWORD  phải trùng với lúc chạy BE (mặc định admin / adminpass123)
// Chỉ chạy trên database trống. Muốn làm lại: `docker compose down -v && docker compose up -d`, khởi động lại BE rồi chạy lại script.
import { execFileSync } from 'child_process';

const B = (process.env.API_URL || 'http://localhost:8089').replace(/\/$/, '');
if (!/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(B)) {
  console.error(`Từ chối chạy: API_URL=${B} không phải máy local. Script này chỉ dùng cho môi trường test.`);
  process.exit(1);
}
const CONTAINER = process.env.MONGO_CONTAINER || 'smashzone-mongo';
const ADMIN_USER = process.env.ADMIN_USERNAME || 'admin';
const ADMIN_PASS = process.env.ADMIN_PASSWORD || 'adminpass123';
const mongo = (js) => execFileSync('docker', ['exec', '-i', CONTAINER, 'mongosh', '--quiet', 'smashzone', '--eval', js], { encoding: 'utf8' });

async function call(method, path, { token, body } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;
  const r = await fetch(B + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  const text = await r.text();
  let json; try { json = JSON.parse(text); } catch { json = text; }
  if (!r.ok) throw new Error(`${method} ${path} → ${r.status} ${text.slice(0, 200)}`);
  return json;
}
const login = async (u, p) => (await call('POST', '/login', { body: { username: u, password: p } })).token;

// ── Người dùng ─────────────────────────────────────────────────────────
for (const u of [
  { username: 'tuananh', password: 'secret12', fullName: 'Nguyễn Tuấn Anh', email: 'tuananh.badminton@gmail.com', phoneNumber: '0908 123 456' },
  { username: 'minhkhang', password: 'secret12', fullName: 'Lê Minh Khang', email: 'khang.smash@gmail.com', phoneNumber: '0912 345 678' },
  { username: 'mailinh', password: 'secret12', fullName: 'Trần Mai Linh', email: 'mailinh@gmail.com', phoneNumber: '0933 222 111' },
]) await call('POST', '/register', { body: u });

const admin = await login(ADMIN_USER, ADMIN_PASS);
await call('POST', '/management/admin/users', { token: admin, body: { username: 'hoanganh', password: 'owner1234', fullName: 'Hoàng Văn Anh', email: 'hoang.court@gmail.com', phoneNumber: '0988 777 666', role: 'ROLE_OWNER' } });
await call('POST', '/management/admin/users', { token: admin, body: { username: 'tanbinh', password: 'owner1234', fullName: 'Cụm Sân Tân Bình', email: 'tanbinh@smashzone.vn', phoneNumber: '0283 999 888', role: 'ROLE_OWNER' } });

// ── Cụm sân + sân ───────────────────────────────────────────────────────
const img = (id) => `https://images.unsplash.com/${id}?w=1200&q=70&auto=format&fit=crop`;
const venues = [
  { tenCumSan: 'Cụm Sân Cầu Lông Quận 1', diaChi: '12 Nguyễn Huệ, P. Bến Nghé, Quận 1, TP.HCM', gioMoCua: '05:00', gioDongCua: '23:00', sDT: '0283 123 456', ownerUsername: 'hoanganh', courts: [['Sân 1', 100000], ['Sân 2', 100000], ['Sân 3', 120000], ['Sân 4', 120000]], image: img('photo-1626224583764-f87db24ac4ea') },
  { tenCumSan: 'Sân Cầu Lông Tân Bình Sport', diaChi: '45 Hoàng Hoa Thám, P.13, Tân Bình, TP.HCM', gioMoCua: '06:00', gioDongCua: '22:30', sDT: '0283 999 888', ownerUsername: 'tanbinh', courts: [['Sân A', 120000], ['Sân B', 120000], ['Sân C', 140000]], image: img('photo-1613918431703-aa50889e3be8') },
  { tenCumSan: 'CLB Cầu Lông Phú Nhuận Premium', diaChi: '18 Phan Xích Long, Phú Nhuận, TP.HCM', gioMoCua: '05:30', gioDongCua: '23:00', sDT: '0283 555 444', ownerUsername: 'hoanganh', courts: [['Sân VIP 1', 150000], ['Sân VIP 2', 150000]], image: img('photo-1521537634581-0dced2fee2ef') },
  { tenCumSan: 'Cụm Sân Thủ Đức Center', diaChi: '120 Võ Văn Ngân, Thủ Đức, TP.HCM', gioMoCua: '06:00', gioDongCua: '21:00', sDT: '0283 111 222', ownerUsername: 'tanbinh', courts: [['Sân 1', 90000], ['Sân 2', 90000], ['Sân 3', 90000]], image: img('photo-1599474924187-334a4ae5bd3c') },
];
const ids = {};
for (const v of venues) {
  const created = await call('POST', '/management/venues', { token: admin, body: { tenCumSan: v.tenCumSan, diaChi: v.diaChi, gioMoCua: v.gioMoCua, gioDongCua: v.gioDongCua, sDT: v.sDT, ownerUsername: v.ownerUsername, trangThai: 'ACTIVE' } });
  ids[v.tenCumSan] = { venue: created.maCumSan, courts: [] };
  for (const [tenSan, gia] of v.courts) {
    const c = await call('POST', '/management/courts', { token: admin, body: { tenSan, maCumSan: created.maCumSan, gia, trangThai: true } });
    ids[v.tenCumSan].courts.push(c.maSan);
  }
  mongo(`db.HinhAnhSan.insertOne({_id:'IMG_${created.maCumSan}', duongDanHinhAnh:'${v.image}', maCumSan:'${created.maCumSan}'})`);
}
const q1 = ids['Cụm Sân Cầu Lông Quận 1'];
const tb = ids['Sân Cầu Lông Tân Bình Sport'];
const pn = ids['CLB Cầu Lông Phú Nhuận Premium'];
const td = ids['Cụm Sân Thủ Đức Center'];

// ── Voucher ────────────────────────────────────────────────────────────
const day = 86400000;
const now = Date.now();
mongo(`db.KhuyenMai.insertMany([
  {_id:'SZ10', tenKhuyenMai:'Ưu đãi giờ thấp điểm (05:00 – 09:00)', phanTramGiam:0.10, ngayBatDau:new Date(${now - 3 * day}), ngayKetThuc:new Date(${now + 13 * day})},
  {_id:'WEEKEND20', tenKhuyenMai:'Combo đánh đôi cuối tuần', phanTramGiam:0.20, ngayBatDau:new Date(${now - day}), ngayKetThuc:new Date(${now + 2 * day})},
  {_id:'NEWBIE15', tenKhuyenMai:'Chào mừng thành viên mới SmashZone', phanTramGiam:0.15, ngayBatDau:new Date(${now - 5 * day}), ngayKetThuc:new Date(${now + 40 * day})},
  {_id:'BWF25', tenKhuyenMai:'Gói luyện tập tiêu chuẩn BWF', phanTramGiam:0.25, ngayBatDau:new Date(${now - day}), ngayKetThuc:new Date(${now + 25 * day})}
])`);

const ta = await login('tuananh', 'secret12');
await call('POST', '/khuyen-mai/nhan', { token: ta, body: { maKhuyenMai: 'SZ10' } });

// ── Đặt sân (tương lai qua API) ─────────────────────────────────────────
const d = (offsetDays, hh, mm = 0) => {
  const t = new Date(now + offsetDays * day);
  const pad = (n) => String(n).padStart(2, '0');
  return `${t.getFullYear()}-${pad(t.getMonth() + 1)}-${pad(t.getDate())}T${pad(hh)}:${pad(mm)}:00`;
};
const mk = await login('minhkhang', 'secret12');
const ml = await login('mailinh', 'secret12');
const pending = await call('POST', '/phieu-dat-san/create', { token: ta, body: { maSan: q1.courts[1], batDau: d(1, 8), ketThuc: d(1, 9, 30) } });
const confirmed = await call('POST', '/phieu-dat-san/create', { token: ta, body: { maSan: pn.courts[0], batDau: d(4, 18), ketThuc: d(4, 20) } });
await call('POST', '/phieu-dat-san/create', { token: mk, body: { maSan: q1.courts[0], batDau: d(1, 7), ketThuc: d(1, 8) } });
const mkEvening = await call('POST', '/phieu-dat-san/create', { token: mk, body: { maSan: q1.courts[0], batDau: d(1, 17), ketThuc: d(1, 19) } });
await call('POST', '/phieu-dat-san/create', { token: ml, body: { maSan: q1.courts[3], batDau: d(1, 9), ketThuc: d(1, 10, 30) } });
const mlTanBinh = await call('POST', '/phieu-dat-san/create', { token: ml, body: { maSan: tb.courts[0], batDau: d(2, 19), ketThuc: d(2, 21) } });
await call('PATCH', `/management/bookings/${confirmed.maPhieuDat}/status`, { token: admin, body: { status: 'DA_XAC_NHAN' } });

const bill = img('photo-1556742049-0cfed4f6a45d');
mongo(`db.HoaDon.insertMany([
  {_id:'HD_${pending.maPhieuDat}', maPhieuDat:'${pending.maPhieuDat}', maKhuyenMai:'NEWBIE15', tongTien:127500, ngayLap:new Date(), billThanhToan:'${bill}'},
  {_id:'HD_${confirmed.maPhieuDat}', maPhieuDat:'${confirmed.maPhieuDat}', maKhuyenMai:null, tongTien:300000, ngayLap:new Date(${now - day}), billThanhToan:'${bill}'}
])`);

// ── Đơn quá khứ (chèn thẳng) ───────────────────────────────────────────
const past = (offsetDays, hh) => {
  const t = new Date(now);
  return `new Date(${new Date(t.getFullYear(), t.getMonth(), t.getDate() + offsetDays, hh, 0, 0).getTime()})`;
};
mongo(`db.PhieuDatSan.insertMany([
  {_id:'PD_8102a7c1-0000-4000-8000-000000000001', maNguoiDung:'tuananh', maSan:'${tb.courts[2]}', batDau:${past(-2, 6)}, ketThuc:${past(-2, 8)}, tongTien:280000, ngayLap:${past(-3, 10)}, trangThai:'DA_HOAN_THANH'},
  {_id:'PD_7950c3d2-0000-4000-8000-000000000002', maNguoiDung:'tuananh', maSan:'${q1.courts[3]}', batDau:${past(-5, 19)}, ketThuc:${past(-5, 20)}, tongTien:180000, ngayLap:${past(-6, 9)}, trangThai:'DA_HUY'},
  {_id:'PD_8020aaaa-0000-4000-8000-000000000003', maNguoiDung:'minhkhang', maSan:'${q1.courts[0]}', batDau:${past(-1, 18)}, ketThuc:${past(-1, 20)}, tongTien:200000, ngayLap:${past(-2, 8)}, trangThai:'DA_HOAN_THANH'},
  {_id:'PD_8030bbbb-0000-4000-8000-000000000004', maNguoiDung:'mailinh', maSan:'${pn.courts[1]}', batDau:${past(-4, 7)}, ketThuc:${past(-4, 9)}, tongTien:300000, ngayLap:${past(-4, 6)}, trangThai:'DA_HOAN_THANH'},
  {_id:'PD_8040cccc-0000-4000-8000-000000000005', maNguoiDung:'minhkhang', maSan:'${tb.courts[1]}', batDau:${past(-3, 20)}, ketThuc:${past(-3, 21)}, tongTien:120000, ngayLap:${past(-5, 15)}, trangThai:'DA_HOAN_THANH'}
])`);

// ── Yêu thích ──────────────────────────────────────────────────────────
await call('POST', '/yeu-thich/them', { token: ta, body: { maCumSan: q1.venue } });
await call('POST', '/yeu-thich/them', { token: ta, body: { maCumSan: pn.venue } });

// ── Chat ───────────────────────────────────────────────────────────────
const owner = await login('hoanganh', 'owner1234');
const room = await call('POST', '/chat/get-or-create-room', { token: ta, body: { username1: 'tuananh', username2: 'hoanganh' } });
const say = async (tok, noiDung) => { await call('POST', '/chat/send-message', { token: tok, body: { maPhongChat: room, noiDung } }); await new Promise((r) => setTimeout(r, 30)); };
await say(owner, 'Chào Tuấn Anh! Sân 2 ngày mai khung 08:00 bên mình đã chuẩn bị lưới mới rồi nhé.');
await say(ta, 'Dạ em cảm ơn anh Hoàng ạ! Em vừa tải bill chuyển khoản lên hệ thống, anh duyệt đơn giúp em nhé.');
await say(owner, 'Ok bạn, mình thấy hoá đơn rồi. Mình duyệt ngay đây!');
await say(ta, 'Vâng em cảm ơn anh nhiều! Hẹn gặp anh ngày mai ạ 🏸');
const room2 = await call('POST', '/chat/get-or-create-room', { token: ta, body: { username1: 'tuananh', username2: 'minhkhang' } });
await call('POST', '/chat/send-message', { token: mk, body: { maPhongChat: room2, noiDung: 'Tối thứ 5 này đánh đôi không ông?' } });

// ── Đánh giá (đơn đã hoàn thành) ───────────────────────────────────────
const r1 = await call('POST', '/danh-gia', { token: mk, body: { maPhieuDat: 'PD_8020aaaa-0000-4000-8000-000000000003', soSao: 4, noiDung: 'Thảm tốt, đèn không chói. Giờ cao điểm hơi đông, nên đặt sớm.' } });
await call('PATCH', `/management/reviews/${r1.maDanhGia}/reply`, { token: owner, body: { phanHoi: 'Cảm ơn Khang! Sân đã mở thêm khung 21h–23h cho giờ cao điểm.' } });
await call('POST', '/danh-gia', { token: ml, body: { maPhieuDat: 'PD_8030bbbb-0000-4000-8000-000000000004', soSao: 3, noiDung: 'Sân ổn nhưng máy lạnh hơi yếu buổi chiều.' } });
await call('POST', '/danh-gia', { token: mk, body: { maPhieuDat: 'PD_8040cccc-0000-4000-8000-000000000005', soSao: 5, noiDung: 'Giá hợp lý, sân thoáng, sẽ quay lại!' } });
// Đơn PD_8102a7c1-...0001 của tuananh (Hoàn thành) để trống, dùng để test viết đánh giá.

// ── Hội nhóm & ghép trận ───────────────────────────────────────────────
for (const u of [
  { username: 'quochuy', password: 'secret12', fullName: 'Phạm Quốc Huy', email: 'quochuy@gmail.com', phoneNumber: '0909 456 789' },
  { username: 'thanhtruc', password: 'secret12', fullName: 'Võ Thanh Trúc', email: 'thanhtruc@gmail.com', phoneNumber: '0937 111 222' },
  { username: 'ducminh', password: 'secret12', fullName: 'Đỗ Đức Minh', email: 'ducminh@gmail.com', phoneNumber: '0918 333 444' },
]) await call('POST', '/register', { body: u });
const qh = await login('quochuy', 'secret12');
const tt = await login('thanhtruc', 'secret12');
const dm = await login('ducminh', 'secret12');
const pause = () => new Promise((r) => setTimeout(r, 30));

const g1 = await call('POST', '/nhom', { token: mk, body: { tenNhom: 'CLB Cầu Lông Quận 1 Chill', moTa: 'Đánh đôi giao lưu tối thứ 3, 5, 7 ở Quận 1. Vui vẻ là chính, không cay cú 😄', khuVuc: 'Quận 1, TP.HCM', trinhDo: 'TB', congKhai: true, mau: 0 } });
for (const t of [ta, ml, qh]) await call('POST', '/nhom/tham-gia-bang-ma', { token: t, body: { maMoi: g1.maMoi } });
await call('POST', `/nhom/${g1.maNhom}/vai-tro/mailinh`, { token: mk, body: { vaiTro: 'PHO_NHOM' } });
await call('POST', `/nhom/${g1.maNhom}/tham-gia`, { token: dm, body: { loiNhan: 'Mình ở Thủ Đức nhưng hay qua Q1 chơi, cho mình vào với nhé!' } });

const g2 = await call('POST', '/nhom', { token: ml, body: { tenNhom: 'Hội Smash Tân Bình', moTa: 'Nhóm kín cho anh em đánh TB+ trở lên, đánh nhanh – smash mạnh.', khuVuc: 'Tân Bình, TP.HCM', trinhDo: 'TB_KHA', congKhai: false, mau: 3 } });
await call('POST', '/nhom/tham-gia-bang-ma', { token: tt, body: { maMoi: g2.maMoi } });

const g3 = await call('POST', '/nhom', { token: dm, body: { tenNhom: 'Team Giao Lưu Thủ Đức', moTa: 'Dành cho người mới và trình yếu – TB. Có người hướng dẫn kỹ thuật cơ bản.', khuVuc: 'Thủ Đức, TP.HCM', trinhDo: 'YEU', congKhai: true, mau: 4 } });
await call('POST', `/nhom/${g3.maNhom}/tham-gia`, { token: ta, body: { loiNhan: 'Cho em vào học hỏi với ạ' } });

for (const [tok, text] of [
  [mk, 'Tối mai 17h sân 1 Quận 1 nha mọi người, mình đặt sân rồi!'],
  [ta, 'Ok anh, em vào điểm danh rồi 🙌'],
  [ml, 'Nhớ mang cầu nha, lần trước hết cầu giữa chừng 😂'],
  [qh, 'Mình mang 1 ống Thành Công nhé'],
]) { await call('POST', `/nhom/${g1.maNhom}/tin-nhan`, { token: tok, body: { noiDung: text } }); await pause(); }
await call('POST', `/nhom/${g2.maNhom}/tin-nhan`, { token: tt, body: { noiDung: 'Thứ 6 này ai rảnh đánh không?' } });

const bank = { nganHang: 'MB', soTaiKhoan: '0901234567', chuTaiKhoan: 'LE MINH KHANG' };
const m1 = await call('POST', '/tran', { token: mk, body: { maPhieuDat: mkEvening.maPhieuDat, maNhom: g1.maNhom, tieuDe: 'Kèo đôi tối mai – Quận 1', ghiChu: 'Ưu tiên thành viên nhóm, còn chỗ thì mời bạn vãng lai. Cầu nhóm lo.', kieuChoi: 'DOI', soNguoiToiDa: 4, trinhDoTu: 'TB', trinhDoDen: 'TB_KHA', congKhai: true, ...bank } });
await call('POST', `/tran/${m1.maTran}/tham-gia`, { token: ta });
await call('POST', `/tran/${m1.maTran}/tham-gia`, { token: ml });
await call('POST', `/tran/${m1.maTran}/thanh-toan`, { token: ml, body: { billUrl: bill } });
await call('POST', `/tran/${m1.maTran}/thanh-toan/mailinh`, { token: mk, body: { daTra: true } });
await call('POST', `/tran/${m1.maTran}/thanh-toan`, { token: ta, body: { billUrl: bill } });
await call('POST', `/tran/${m1.maTran}/tham-gia`, { token: tt, body: { loiNhan: 'Mình đánh TB+, tối mai rảnh, cho mình 1 slot nhé!', trinhDo: 'TB_KHA' } });

const m2 = await call('POST', '/tran', { token: ml, body: { maPhieuDat: mlTanBinh.maPhieuDat, tieuDe: 'Tìm 2 bạn đánh đôi TB+', ghiChu: 'Sân A Tân Bình, có máy lạnh. Đánh vui, không tính điểm.', kieuChoi: 'DOI', soNguoiToiDa: 4, trinhDoTu: 'TB_KHA', trinhDoDen: 'KHA', nganHang: 'VCB', soTaiKhoan: '1012345678', chuTaiKhoan: 'TRAN MAI LINH' } });
await call('POST', `/tran/${m2.maTran}/tham-gia`, { token: qh, body: { loiNhan: 'Mình TB+, đánh đôi nam nữ được', trinhDo: 'TB_KHA' } });
await call('POST', `/tran/${m2.maTran}/duyet/quochuy`, { token: ml, body: { dongY: true } });

const dmBooking = await call('POST', '/phieu-dat-san/create', { token: dm, body: { maSan: td.courts[0], batDau: d(3, 18), ketThuc: d(3, 20) } });
await call('POST', '/tran', { token: dm, body: { maPhieuDat: dmBooking.maPhieuDat, maNhom: g3.maNhom, tieuDe: 'Giao lưu xoay vòng cho người mới', ghiChu: 'Xoay vòng đánh đôi, có hướng dẫn cầm vợt và di chuyển cơ bản.', kieuChoi: 'GIAO_LUU', soNguoiToiDa: 8, trinhDoTu: 'YEU', trinhDoDen: 'TB', congKhai: true, chiPhiMoiNguoi: 25000 } });

console.log(`
Đã tạo dữ liệu demo.
  Admin   : ${ADMIN_USER} / (mật khẩu ADMIN_PASSWORD bạn đặt khi chạy BE)
  Chủ sân : hoanganh / owner1234  (Quận 1, Phú Nhuận)
            tanbinh  / owner1234  (Tân Bình, Thủ Đức)
  Khách   : tuananh / secret12, minhkhang / secret12, mailinh / secret12
            quochuy / secret12, thanhtruc / secret12, ducminh / secret12
  Nhóm    : "${g1.tenNhom}" (mã mời ${g1.maMoi}), "${g2.tenNhom}" (riêng tư, mã ${g2.maMoi}), "${g3.tenNhom}"
`);

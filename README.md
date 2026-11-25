# MiniERP

Mini ERP sederhana berbasis **Spring Boot 4** (REST API): master data, siklus pembelian
(PO → GRN), siklus penjualan (SO → Delivery), dan stok multi-gudang dengan alur approval.

## Stack

| Komponen   | Teknologi |
|------------|-----------|
| Runtime    | Java 21 (build dengan JDK 21/27) |
| Framework  | Spring Boot 4.2 (Spring Framework 7, Jakarta EE) |
| Security   | Spring Security OAuth2 Resource Server — JWT stateless (HS256) |
| Persistence| Spring Data JPA + Flyway + PostgreSQL 17 |
| Test       | JUnit 5 + Mockito + Testcontainers (PostgreSQL) |
| Build      | Maven Wrapper (`./mvnw`) |

## Menjalankan

```bash
# 1. Jalankan database
docker compose up -d

# 2. Build & test (butuh Docker untuk Testcontainers)
./mvnw verify

# 3. Jalankan aplikasi
./mvnw spring-boot:run
```

Aplikasi berjalan di `http://localhost:8080`. Konfigurasi via environment variable:
`MINIERP_DB_URL`, `MINIERP_DB_USER`, `MINIERP_DB_PASSWORD`, `MINIERP_JWT_SECRET`
(wajib di-override untuk produksi, minimal 32 byte).

Data seed (saat `minierp.seed.enabled=true`):

| Username | Password    | Role    |
|----------|-------------|---------|
| admin    | `Admin123!` | ADMIN   |
| manager  | `Manager123!` | MANAGER |
| staff    | `Staff123!` | STAFF   |

## Autentikasi & Role

Login untuk mendapatkan JWT:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"staff","password":"Staff123!"}'
```

Kirim token sebagai `Authorization: Bearer <token>` pada semua request berikutnya.

| Akses | ADMIN / MANAGER | STAFF |
|-------|-----------------|-------|
| Master data (Item, Supplier, Customer, Warehouse) — tulis | ✅ | ❌ |
| Master data & stok — baca | ✅ | ✅ |
| Input transaksi (PO, GRN, SO, Delivery) | ✅ | ✅ |
| Approval dokumen < Rp 10.000.000 | ✅ | ✅ |
| Approval dokumen ≥ Rp 10.000.000 | ✅ | ❌ (422) |

## API

### Auth
- `POST /api/auth/login` — login, mengembalikan JWT (publik)
- `GET /api/auth/me` — identitas user terautentikasi

### Master data (write: ADMIN/MANAGER only)
- `GET|POST /api/items`, `GET|PUT|DELETE /api/items/{id}`
- `GET|POST /api/suppliers`, `GET|PUT|DELETE /api/suppliers/{id}`
- `GET|POST /api/customers`, `GET|PUT|DELETE /api/customers/{id}`
- `GET|POST /api/warehouses`, `GET|PUT|DELETE /api/warehouses/{id}`
- `DELETE` bersifat *soft delete* (menonaktifkan data, histori transaksi tetap utuh)

### Stok
- `GET /api/stock` — saldo stok per item per gudang (filter `itemId`, `warehouseId`)
- `GET /api/stock/{itemId}` — saldo stok satu item di semua gudang
- `GET /api/stock/movements` — stock card (riwayat keluar-masuk)

### Pembelian
- `POST /api/purchase-orders` — buat PO (status PENDING)
- `GET /api/purchase-orders`, `GET /api/purchase-orders/{id}`
- `POST /api/purchase-orders/{id}/approve` | `/reject`
- `POST /api/goods-receipts` — buat GRN dari PO yang sudah APPROVED
- `GET /api/goods-receipts`, `GET /api/goods-receipts/{id}`
- `POST /api/goods-receipts/{id}/approve` | `/reject` — approve menambah stok

### Penjualan
- `POST /api/sales-orders` — buat SO (status PENDING)
- `GET /api/sales-orders`, `GET /api/sales-orders/{id}`
- `POST /api/sales-orders/{id}/approve` | `/reject`
- `POST /api/deliveries` — buat Delivery dari SO yang sudah APPROVED
- `GET /api/deliveries`, `GET /api/deliveries/{id}`
- `POST /api/deliveries/{id}/approve` | `/reject` — approve mengurangi stok

Endpoint `approve` dan `reject` menerima aturan approval yang sama;
`reject` membutuhkan body `{"reason": "..."}`.

## Alur bisnis

```
Purchase:  PO (PENDING) ──approve──▶ GRN (PENDING) ──approve──▶ stok BERTAMBAH
                │ reject                    │ reject
                ▼                           ▼
            tidak bisa diproses       PO tetap APPROVED, GRN baru bisa dibuat ulang

Sales:     SO (PENDING) ──approve──▶ Delivery (PENDING) ──approve──▶ stok BERKURANG
                │ reject                    │ reject
                ▼                           ▼
            tidak bisa diproses       SO tetap APPROVED, Delivery baru bisa dibuat ulang
```

Aturan utama:

1. **Approval threshold** — dokumen dengan nilai ≥ Rp 10.000.000 hanya boleh
   disetujui/ditolak oleh ADMIN/MANAGER. Di bawah itu STAFF boleh menyetujui.
2. **GRN hanya dari PO APPROVED** — PO yang ditolak tidak bisa diterima (stok tidak masuk).
3. **Delivery hanya dari SO APPROVED** — SO yang ditolak tidak bisa dikirim.
4. **Validasi stok** — stok gudang tidak boleh lebih kecil dari qty pesanan saat
   Delivery dibuat maupun saat disetujui (stok tidak bisa minus).
5. **Qty kumulatif** — total GRN tidak boleh melebihi qty PO; total Delivery tidak
   boleh melebihi qty SO (GRN/Delivery yang ditolak membebaskan qty untuk dibuat ulang).
6. **Multi-gudang** — stok dihitung per item per gudang; GRN menambah dan Delivery
   mengurangi stok gudang yang dipilih.

## Struktur project

Arsitektur **hexagonal (ports & adapters)**: domain murni tanpa framework, use case
di application, dan semua detail teknis (JPA, HTTP, security) di infrastructure.

```
src/main/java/com/minierp/
├── MinierpApplication.java   # bootstrap saja
├── domain/                   # PURE Java — tanpa Spring/JPA/Jakarta
│   ├── model/                # aggregate, value object, enum
│   │   ├── item/ customer/ supplier/ warehouse/ inventory/
│   │   ├── purchase/ sales/ user/
│   │   └── shared/           # BaseApprovalDocument, DocumentStatus, AuthenticatedUser,
│   │                         # PageQuery/PageResult, exception domain
│   ├── port/in/              # interface use case (ditawarkan aplikasi)
│   ├── port/out/             # port driven (repository, StockPort, DocumentNumberPort, …)
│   └── service/              # ApprovalPolicy (threshold)
├── application/usecase/      # @Service @Transactional, implementasi port/in
└── infrastructure/
    ├── persistence/          # entity JPA, mapper domain⇄entity, adapter repository
    ├── web/                  # controller + dto per modul, error/ (RFC 9457), support/
    ├── security/             # JwtService, CurrentUser, JpaUserDetailsService, filter
    ├── config/               # MinierpProperties, Jackson, SecurityConfig, DataSeeder
    └── docs/                 # katalog Problem Detail
src/test/java/com/minierp/
├── application/usecase/      # unit test use case (mock port)
├── architecture/             # ArchitectureTest (ArchUnit) — batas layer dijaga otomatis
└── MinierpFlowIntegrationTest  # end-to-end Testcontainers
src/main/resources/db/migration/   # skema Flyway (V1..V7)
```

Batas layer divalidasi ArchUnit setiap `./mvnw verify`: domain bebas framework,
application tidak boleh menyentuh infrastructure, dan web tidak boleh menyentuh persistence.

## Response envelope

Semua response JSON (sukses maupun error) dibungkus dengan format yang sama
(`application/json`), dipasang otomatis oleh `ApiResponseBodyAdvice` — controller
cukup mengembalikan DTO biasa:

```json
{
  "success": true,
  "data": { "id": 1, "sku": "SKU-1" },
  "meta": {
    "timestamp": "2026-11-25T10:00:00Z",
    "correlationId": "0b6c…",
    "pagination": { "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }
  }
}
```

- `data` — payload; untuk list berupa array, `null` saat error.
- `meta.pagination` — hanya ada di endpoint list yang ber-pagination.
- `204 No Content` (DELETE) tetap tanpa body.

## Error contract

Error memakai envelope yang sama dengan `success: false` dan field `error` berisi
member RFC 9457 Problem Detail (`type`, `title`, `status`, `detail`, `instance`,
plus ekstensi seperti `errors` untuk validasi):

```json
{
  "success": false,
  "data": null,
  "meta": { "timestamp": "2026-11-25T10:00:00Z", "correlationId": "0b6c…" },
  "error": {
    "type": "https://minierp.local/problems/not-found",
    "title": "Resource tidak ditemukan",
    "status": 404,
    "detail": "Item dengan id 99 tidak ditemukan",
    "instance": "/api/items/99"
  }
}
```

| Status | Kondisi |
|--------|---------|
| 400 | Validasi request gagal |
| 401 | Token tidak valid / login gagal |
| 403 | Role tidak punya hak akses |
| 404 | Data tidak ditemukan |
| 409 | Status dokumen tidak memungkinkan / data duplikat |
| 422 | Aturan bisnis dilanggar (stok, threshold approval, qty) |

## Rencana berikutnya

Laporan (stok, stock card, top 5 produk, aging stock), export Excel/PDF,
notifikasi stok minimum di dashboard, dan pagination list endpoint.

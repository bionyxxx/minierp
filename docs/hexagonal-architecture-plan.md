# Plan: Restrukturisasi MiniERP ke Hexagonal Architecture

> Status: **SELESAI — fase 1–6 terimplementasi.** Batas layer dijaga oleh
> `src/test/java/com/minierp/architecture/ArchitectureTest` (ArchUnit) di setiap `./mvnw verify`.
> Referensi skill: `.opencode/skills/hexagonal-architecture`
> Scope: restrukturisasi package murni. Kontrak eksternal (endpoint, JSON, error RFC 9457,
> pagination, security rules, skema Flyway) **tidak berubah**.

---

## 1. Latar belakang

Kondisi saat ini:

- Layout *package-by-feature* di `com.minierp` (`item`, `customer`, `supplier`, `warehouse`,
  `inventory`, `purchase`, `sales`, `auth`, `user`, `approval`, `docs`, `config`, `common`).
- Concern teknis/cross-cutting bercampur dengan modul bisnis di root package:
  `config/` (Security, Jackson, Web, DataSeeder), `common/` (exception, pagination,
  correlation-id), `auth/JwtService`, `user/JpaUserDetailsService`.
- Entity langsung memakai anotasi JPA (`jakarta.persistence`) dan repository langsung
  extends `JpaRepository` — business logic dan detail persistensi menyatu.
- Total 118 file main + 13 file test.

Tujuan: pemisahan tegas antara **domain** (bisnis murni), **application** (orkestrasi use
case), dan **infrastructure** (framework, DB, HTTP, security), mengikuti pola
*ports & adapters*.

---

## 2. Struktur target

```
com.minierp/
├── MinierpApplication.java              ← bootstrap saja (@SpringBootApplication)
├── domain/                              ← PURE Java, nol dependensi framework
│   ├── model/                           ← aggregate, entity, value object, enum
│   │   ├── item/  customer/  supplier/  warehouse/
│   │   ├── inventory/                   (Stock, StockMovement, StockMovementRef, StockMovementType)
│   │   ├── purchase/                    (PurchaseOrder, PurchaseOrderLine, GoodsReceipt, GoodsReceiptLine)
│   │   ├── sales/                       (SalesOrder, SalesOrderLine, Delivery, DeliveryLine)
│   │   ├── user/                        (User, Role)
│   │   └── shared/                      (DocumentStatus, BaseApprovalDocument tanpa JPA,
│   │                                     AuthenticatedUser, BusinessRuleException,
│   │                                     NotFoundException, DuplicateResourceException,
│   │                                     InvalidStateException, InvalidSortException)
│   ├── port/
│   │   ├── in/                          ← interface use case (ItemUseCase,
│   │   │                                  PurchaseOrderUseCase, StockQueryUseCase, …)
│   │   └── out/                         ← interface yang dibutuhkan aplikasi
│   │                                      (ItemRepositoryPort, StockPort,
│   │                                       DocumentNumberPort, ClockPort, …)
│   └── service/                         ← logic bisnis murni
│       (ApprovalPolicy — threshold di-inject sebagai BigDecimal,
│        kalkulasi line total, validasi transisi status)
├── application/                         ← orkestrasi use case; Spring boleh di sini
│   └── usecase/                         ← @Service + @Transactional,
│                                          implementasi port/in, per modul:
│       (ItemService, CustomerService, SupplierService, WarehouseService,
│        StockService, StockQueryService, PurchaseOrderService, GoodsReceiptService,
│        SalesOrderService, DeliveryService, AuthService, …)
└── infrastructure/                      ← semua detail framework/DB/HTTP
    ├── persistence/
    │   ├── entity/                      (ItemJpaEntity, BaseApprovalDocumentJpa, …)
    │   ├── mapper/                      (ItemMapper, PurchaseMapper, SalesOrderMapper,
    │   │                                 DeliveryMapper, …) — mapping domain ⇄ JPA entity
    │   └── repository/                  (JpaItemRepository implements ItemRepositoryPort,
    │                                     SpringDataItemRepository extends JpaRepository, …)
    ├── web/                             ← driving adapter
    │   ├── (per modul: ItemController, PurchaseOrderController, …, dto/)
    │   ├── auth/                        (AuthController)
    │   ├── error/                       (GlobalExceptionHandler, ProblemTypes, ProblemTitles,
    │   │                                 ProblemMessages, ProblemTypeUris, FieldViolation,
    │   │                                 SecurityProblemDetailHandler)
    │   └── support/                     (Pagination, PageResponse, PaginationDefaults)
    ├── security/                        (SecurityConfig, JwtService,
    │                                     JpaUserDetailsService, CurrentUser,
    │                                     CorrelationIdFilter)
    ├── config/                          (MinierpProperties, JacksonConfig, WebConfig,
    │                                     DataSeeder)
    └── docs/                            (ProblemCatalogController, ProblemCatalog,
                                          ProblemDescriptor)
```

---

## 3. Pemetaan file (118 file)

| Lokasi sekarang | Tujuan | Catatan |
|---|---|---|
| `*/Item.java` dkk (12 entity JPA) | `domain/model/*` (model murni) **+** `infrastructure/persistence/entity/*JpaEntity` **+** mapper | Lihat §5 keputusan desain |
| `*/XxxRepository.java` (extends `JpaRepository`) | Split: interface port di `domain/port/out/`, adapter `JpaXxxRepository` + `SpringDataXxxRepository` di `infrastructure/persistence/repository/` | Query `@Query` JPQL pindah ke Spring Data repo (infrastruktur) |
| `*/XxxService.java` | `application/usecase/` (implementasi `port/in`); logic validasi murni naik ke `domain/service/` | `@Transactional` hanya di application |
| `*/XxxController.java`, `*/dto/` | `infrastructure/web/` | DTO tetap; kontrak API tidak berubah |
| `approval/ApprovalPolicy` | `domain/service/` | Threshold di-inject sebagai `BigDecimal`, bukan `MinierpProperties` (domain bebas config) |
| `common/BaseApprovalDocument` | `domain/model/shared/` (tanpa anotasi) + `infrastructure/persistence/entity/BaseApprovalDocumentJpa` | `@MappedSuperclass` menjadi detail persistensi |
| `common/DocumentNumbers` | `domain/port/out/DocumentNumberPort` + adapter di `infrastructure/persistence/` | Pembangkit nomor dokumen = driven port |
| `common/AuthenticatedUser` | `domain/model/shared/` | Record murni — sudah bersih |
| `common/CurrentUser` | `infrastructure/security/` | Adapter pembaca SecurityContext |
| `common/*Exception`, `DocumentStatus` | `domain/model/shared/` | Error domain = bagian domain |
| `common/GlobalExceptionHandler`, `Problem*`, `FieldViolation` | `infrastructure/web/error/` | |
| `common/Pagination*`, `PageResponse` | `infrastructure/web/support/` | |
| `common/CorrelationIdFilter` | `infrastructure/security/` | Filter servlet = infrastruktur |
| `config/*` | `infrastructure/config/` | |
| `docs/*` | `infrastructure/docs/` | |
| `auth/JwtService` | `infrastructure/security/` | |
| `auth/AuthController` | `infrastructure/web/auth/` | |
| `auth/AuthService` | `application/usecase/` | |
| `user/JpaUserDetailsService` | `infrastructure/security/` | |

### Test (13 file)

| Test | Tujuan |
|---|---|
| `ItemServiceTest`, `StockQueryServiceTest`, `PurchaseOrderServiceTest`, `GoodsReceiptServiceTest`, `SalesOrderServiceTest`, `DeliveryServiceTest` | `src/test/java/com/minierp/application/usecase/` (Mockito, mock port) |
| `GlobalExceptionHandlerTest`, `PaginationTest`, `JacksonConfigTest` | `src/test/java/com/minierp/infrastructure/...` (sesuai lokasi kelasnya) |
| `MinierpApplicationTests`, `MinierpFlowIntegrationTest`, `TestcontainersConfiguration`, `TestMinierpApplication` | tetap di root test package (integration test) |
| **Baru**: `ArchitectureTest` (ArchUnit) | Memvalidasi batas layer otomatis di `./mvnw verify` |

---

## 4. Batas yang harus dijaga (gotchas dari skill)

- `domain/` **tanpa** `jakarta.persistence`, `org.springframework.*`, atau framework apa pun.
- Use case meng-inject **port interface**, bukan `JpaRepository` / repository Spring Data.
- `@Transactional` hanya di `application/`, tidak di domain.
- `port/in` = apa yang aplikasi tawarkan; `port/out` = apa yang aplikasi butuhkan. Jangan dicampur.
- Domain jangan anemic (getter/setter saja) — perilaku (validasi, transisi status, kalkulasi)
  hidup di objek domain.
- Test pakai `@MockitoBean`/`@MockitoSpyBean` (Boot 4), bukan `@MockBean`.
- Aturan bisnis tetap di service/policy (lihat `AGENTS.md`), bukan di controller atau
  security rules.
- Hibernate snapshot state saat persist: setiap field (termasuk nomor dokumen) harus sudah
  final **sebelum** `save()`. Nomor dokumen tetap dari `DocumentNumberPort` (penerus
  `*NumberRepository#nextNumberValue()`), bukan update setelah save.
- Mapping JPA yang menyimpan domain model ke entity harus mempertahankan perilaku
  `@PrePersist`/`@PreUpdate` (createdAt dst.) di sisi entity infrastruktur.

---

## 5. Keputusan desain

### 5.1 Domain murni (strict) vs pragmatis — **PERLU KONFIRMASI**

| | Strict (mengikuti skill) | Pragmatis |
|---|---|---|
| Anotasi JPA di domain | Tidak — domain pure Java | Ya, tetap di model domain |
| Per entity | Model domain + `XxxJpaEntity` + mapper (±3 file) | 1 file (seperti sekarang) |
| Estimasi tambahan | ±24 file tambahan, relasi (`PurchaseOrder → lines`) di-map dua arah | Hemat ±24 file |
| Kepatuhan skill | Penuh | Skill menyebutnya *gotcha* |

**Default plan ini: strict.** Jika memilih pragmatis, fase 2 dan 3 menjadi jauh lebih ringan.

### 5.2 Keputusan lain (mengikuti default, bisa berubah saat implementasi)

- `ApprovalPolicy` jadi `domain/service` dengan constructor `BigDecimal threshold`;
  wiring bean dari `MinierpProperties` dilakukan di `infrastructure/config`.
- `StockService` menjadi `application/usecase` yang mengimplementasi
  `domain/port/out/StockPort` — dipakai lintas modul (purchase → IN, sales → OUT).
  Aturan "stock card selalu mencerminkan sejarah in/out" tetap di satu tempat ini.
- `AuthenticatedUser` + `Role` di `domain/model/shared`; `CurrentUser` (infrastructure)
  me-resolve lalu controller menyertakannya di command — service bebas tipe security.
- `MinierpApplication` tetap di root `com.minierp` agar component scan menjangkau
  `domain`, `application`, dan `infrastructure`.

---

## 6. Fase implementasi

1. **Fondasi**
   - Buat skeleton `domain/` `application/` `infrastructure/`.
   - Pindahkan concern teknis: `config`, `security`, `docs`, `common` infra
     (handler, pagination, correlation-id).
   - Pecah `AuthenticatedUser`, `DocumentStatus`, `*Exception` ke `domain/model/shared`.
   - Kompilasi harus hijau sebelum lanjut.
2. **Modul CRUD sederhana** (`item`, `customer`, `supplier`, `warehouse`) sebagai pola:
   model domain + port + use case + JPA adapter + web adapter.
   - **Review pola ini dulu** sebelum lanjut ke fase 3.
3. **Modul dokumen** (`inventory`, `purchase`, `sales`, `approval`)
   - `BaseApprovalDocument` dipecah (domain vs `@MappedSuperclass`).
   - `StockService` → implementasi `StockPort`.
   - `DocumentNumberPort`; `ApprovalPolicy` → `domain/service`.
   - Mapping relasi antar-aggregate (PO ↔ GRN, SO ↔ Delivery) dua arah.
4. **Auth & user** — `JwtService`, `JpaUserDetailsService` → `infrastructure/security`;
   `AuthController` → `infrastructure/web/auth`; `AuthService` → `application/usecase`.
5. **Test** — pindah & sesuaikan 13 test; tambah `ArchitectureTest` (ArchUnit):
   - `domain` tidak boleh import Spring/JPA/Jakarta.
   - `application` tidak boleh import `infrastructure`.
   - `web` tidak boleh import `persistence`.
6. **Verifikasi**
   - `JAVA_HOME=~/.jdks/openjdk-27 ./mvnw verify` penuh (unit + slice + Testcontainers).
   - Bandingkan hasil `MinierpFlowIntegrationTest` — perilaku harus identik.
   - Tidak ada perubahan pada `src/main/resources/db/migration` (skema tidak berubah).

---

## 7. Risiko

- **Mapping relasi dua arah** (PO ↔ lines, SO ↔ lines) di mode strict rawan kehilangan
  state bila mapper tidak lengkap. Mitigasi: test integrasi flow end-to-end tetap dijalankan.
- **Query JPQL `@Query`** pindah ke `SpringDataXxxRepository`; pastikan tidak ada perubahan
  semantik query saat dipindah.
- **`@PrePersist`/`@PreUpdate`** harus tetap bekerja setelah entity dipecah.
- **Konvensi `AGENTS.md`** (set field sebelum save, nomor dokumen dari number sequence,
  aturan bisnis di service) harus tetap dipatuhi selama refactor.
- Perubahan import sangat luas (±118 file); lakukan per fase + kompilasi per fase, jangan
  sekaligus.

---

## 8. Checklist penyelesaian

- [x] Keputusan §5.1 (strict vs pragmatis) dikonfirmasi — strict.
- [x] Fase 1–4 selesai, kompilasi hijau per fase.
- [x] Semua test lama dipindah dan hijau.
- [x] `ArchitectureTest` (ArchUnit) lulus.
- [x] `./mvnw verify` penuh lulus.
- [x] `MinierpFlowIntegrationTest` menunjukkan perilaku identik (27/27).
- [x] README.md / AGENTS.md diperbarui dengan struktur baru.

# Huong Dan Su Dung BCTTG API

Tai lieu nay huong dan van hanh, su dung va khai thac API BCTTG theo cach thuc te de ban co the:
- chay he thong nhanh bang Docker Compose,
- dang nhap bang `phone + password`,
- test API bang Swagger/cURL,
- quan ly du lieu (content, song, profile, note, media, user),
- xu ly cac loi van hanh thuong gap.

---

## 1) Tong quan he thong

Ung dung gom 2 thanh phan chinh:

1. `api` (Spring Boot 4, Java 17)
2. `mysql` (MySQL 8.3)

Chuc nang chinh:
- Quan ly tai khoan nguoi dung (role, trang thai, cap lai mat khau).
- Quan ly danh muc/noi dung truyen thong, net tieu bieu va so do lich su.
- Quan ly danh muc/bai hat.
- Quan ly ho so du lieu (`THU_TRUONG`, `CHIEN_SI`, `ANH_HUNG`).
- Quan ly ghi chu ca nhan theo tung tai khoan.
- Dashboard admin tong hop so lieu va trang thai he thong.
- Upload va quan ly media.
- Xac thuc JWT bang so dien thoai.
- Tai lieu API qua Swagger.

Cong nghe:
- Spring Boot, Spring Security, Spring Data JPA
- Flyway migration
- MySQL
- springdoc-openapi
- Docker Compose

---

## 2) URL truy cap mac dinh

Sau khi chay thanh cong:
- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- MySQL host port: `localhost:3306`

Luu y:
- Swagger co the redirect sang `http://localhost:8080/swagger-ui/index.html`.

---

## 3) Chay nhanh bang Docker Compose (khuyen nghi)

### 3.1. Dieu kien
- Docker Engine
- Docker Compose

### 3.2. Chay lan dau

```bash
docker-compose down -v
docker-compose up -d --build
```

Kiem tra trang thai:

```bash
docker-compose ps
docker-compose logs --tail=200 api
```

Dieu kien thanh cong:
- `bcttg-api` o trang thai `Up`
- log co dong: `Started BcttgApplication`

### 3.3. Kiem tra DB da tao bang

```bash
docker exec -it bcttg-mysql mysql -ubcttg -pBcttg@2026 -D bcttg -e "SHOW TABLES;"
```

Ky vong co cac bang:
- `content_categories`
- `content_items`
- `home_modules`
- `data_profiles`
- `media_assets`
- `personal_notes`
- `song_categories`
- `songs`
- `system_settings`
- `system_audit_logs`
- `user_accounts`
- `user_profiles`
- `flyway_schema_history`

### 3.4. Kiem tra migration Flyway

```bash
docker exec -it bcttg-mysql mysql -ubcttg -pBcttg@2026 -D bcttg -e "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Luu y quan trong:
- Bang `flyway_schema_history` la bang bat buoc cua Flyway.
- Neu bang nay khong ton tai, migration chua chay.

---

## 4) Bien moi truong quan trong

File: `docker-compose.yml`

### MySQL
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

### API
- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=none`
- `SPRING_FLYWAY_ENABLED=true`
- `BCTTG_DB_NAME`
- `BCTTG_DB_USER`
- `BCTTG_DB_PASS`
- `BCTTG_JWT_SECRET`
- `BCTTG_MEDIA_ROOT`
- `BCTTG_MEDIA_BASE_URL`

Khuyen nghi production:
- doi toan bo password/secret.
- khong de gia tri mac dinh.

---

## 5) Co so du lieu va migration

Thu muc migration:
- `src/main/resources/db/migration/V1__init.sql`
- `src/main/resources/db/migration/V2__seed.sql`
- `src/main/resources/db/migration/V3__seed_update.sql`
- `src/main/resources/db/migration/V4__user_schema.sql`
- `src/main/resources/db/migration/V5__user_seed.sql`
- `src/main/resources/db/migration/V6__seed_real_data.sql`
- `src/main/resources/db/migration/V7__profile_note_dashboard_schema.sql`
- `src/main/resources/db/migration/V8__profile_note_dashboard_seed.sql`
- `src/main/resources/db/migration/V9__settings_home_modules_schema.sql`
- `src/main/resources/db/migration/V10__settings_home_modules_seed.sql`
- `src/main/resources/db/migration/V11__history_diagram_content.sql`

Nguyen tac lam viec voi migration:
1. Khong sua file migration da chay tren moi truong dung.
2. Muon bo sung du lieu/schema thi tao file moi: `V12__...sql`, `V13__...sql`.
3. Sau khi tao file moi, restart `api` (hoac `docker-compose up -d --build`) de Flyway ap dung.

Luu y:
- `V11__history_diagram_content.sql` khong tao bang moi, ma bo sung type `SO_DO_LICH_SU` vao module content dung chung.

Reset du lieu local:

```bash
docker-compose down -v
docker-compose up -d --build
```

---

## 6) Tai khoan dang nhap mac dinh (seed)

Du lieu seed trong `V5__user_seed.sql`:

| Role    | Phone       | Password      |
|---------|-------------|---------------|
| ADMIN   | 0900000001  | Admin@2026    |
| MANAGER | 0900000002  | Manager@2026  |
| USER    | 0900000003  | User@2026     |
| USER    | 0900000004  | User@2026     |
| USER    | 0900000005  | User@2026     |
| USER    | 0900000006  | User@2026     |

Luu y:
- He thong dang nhap theo `phone`, khong phai `username`.
- Mat khau seed dang de dang test, can doi truoc khi dua production.

---

## 7) Co che xac thuc va phan quyen

### 7.1. Dang nhap
Endpoint:
- `POST /api/v1/auth/login`

Request:

```json
{
  "phone": "0900000001",
  "password": "Admin@2026"
}
```

Response mau:

```json
{
  "success": true,
  "data": {
    "accessToken": "<jwt>",
    "tokenType": "Bearer",
    "expiresInMinutes": 120,
    "roles": ["ROLE_ADMIN"]
  },
  "meta": null,
  "error": null
}
```

### 7.2. Gui token cho API can auth

Header:

```http
Authorization: Bearer <jwt_token>
```

### 7.3. Nhom endpoint khong can token
- `/api/v1/auth/**`
- `/files/**`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/api-docs/**`
- `/v3/api-docs/**`

### 7.4. Role thuc te
- `ADMIN`, `MANAGER`, `USER`
- Toan bo endpoint nghiep vu, ke ca namespace `/api/v1/public/**`, deu can Bearer token hop le.
- Namespace `/api/v1/public/**`:
  - `GET`: cho phep `ADMIN`, `MANAGER`, `USER`.
- Module `admin/content-*`, `admin/song-*`, `admin/data-profiles`, `admin/media`:
  - `GET` list/detail: `ADMIN`, `MANAGER`
  - `POST/PATCH/DELETE`: `ADMIN`, `MANAGER`
- Module `admin/users`:
  - `GET` list/detail: `ADMIN`, `MANAGER`
  - `POST/PATCH/DELETE`: chi `ADMIN`
- Module `admin/dashboard`: `ADMIN`, `MANAGER`
- Module `admin/settings`: chi `ADMIN`
- Module `admin/home-modules`:
  - `GET`: `ADMIN`, `MANAGER`
  - `PATCH`: `ADMIN`
- Module `notes`: `ADMIN`, `MANAGER`, `USER` tren du lieu cua chinh tai khoan dang nhap

---

## 8) Cau truc response chung

API tra ve format thong nhat:

```json
{
  "success": true,
  "data": {},
  "meta": {},
  "error": null
}
```

Khi loi:

```json
{
  "success": false,
  "data": null,
  "meta": null,
  "error": {
    "code": "BAD_REQUEST",
    "message": "Noi dung loi",
    "details": []
  }
}
```

Pagination (`meta`) khi list:
- `page`
- `page_size`
- `total_elements`
- `total_pages`

---

## 9) Danh sach endpoint theo nhom

## 9.1 Auth

| Method | Path | Auth | Mo ta |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Public | Dang nhap bang phone/password, nhan JWT |

## 9.2 Public - Content

Luu y:
- Module content dung chung cho 3 nhom du lieu: `TRUYEN_THONG`, `NET_TIEU_BIEU`, `SO_DO_LICH_SU`.
- "So do lich su" khong co module rieng; dung chung category/item CRUD cua content va phan biet bang field `type`.

| Method | Path | Auth | Mo ta |
|---|---|---|---|
| GET | `/api/v1/public/content-categories` | Bearer (ADMIN/MANAGER/USER) | Danh sach danh muc noi dung cong khai |
| GET | `/api/v1/public/content-categories/{id}` | Bearer (ADMIN/MANAGER/USER) | Chi tiet danh muc cong khai |
| GET | `/api/v1/public/content-items` | Bearer (ADMIN/MANAGER/USER) | Danh sach bai viet cong khai |
| GET | `/api/v1/public/content-items/{id}` | Bearer (ADMIN/MANAGER/USER) | Chi tiet bai viet cong khai (tu tang view_count) |

## 9.3 Public - Song

| Method | Path | Auth | Mo ta |
|---|---|---|---|
| GET | `/api/v1/public/song-categories` | Bearer (ADMIN/MANAGER/USER) | Danh sach danh muc bai hat cong khai |
| GET | `/api/v1/public/song-categories/{id}` | Bearer (ADMIN/MANAGER/USER) | Chi tiet danh muc bai hat cong khai |
| GET | `/api/v1/public/songs` | Bearer (ADMIN/MANAGER/USER) | Danh sach bai hat cong khai |
| GET | `/api/v1/public/songs/{id}` | Bearer (ADMIN/MANAGER/USER) | Chi tiet bai hat cong khai |

## 9.4 Public - Data profile

| Method | Path | Auth | Mo ta |
|---|---|---|---|
| GET | `/api/v1/public/data-profiles` | Bearer (ADMIN/MANAGER/USER) | Liet ke ho so du lieu dang hien thi |
| GET | `/api/v1/public/data-profiles/{id}` | Bearer (ADMIN/MANAGER/USER) | Chi tiet ho so du lieu dang hien thi |

## 9.5 Public - Media file

| Method | Path | Auth | Mo ta |
|---|---|---|---|
| GET | `/files/**` | Public | Tai file media theo storage key |

## 9.6 Admin - Content category

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/content-categories` | ADMIN/MANAGER | Liet ke danh muc |
| POST | `/api/v1/admin/content-categories` | ADMIN/MANAGER | Tao danh muc |
| GET | `/api/v1/admin/content-categories/{id}` | ADMIN/MANAGER | Chi tiet danh muc |
| PATCH | `/api/v1/admin/content-categories/{id}` | ADMIN/MANAGER | Cap nhat mot phan danh muc, field bo trong duoc giu nguyen |
| DELETE | `/api/v1/admin/content-categories/{id}` | ADMIN/MANAGER | Xoa danh muc |
| PATCH | `/api/v1/admin/content-categories/{id}/visibility` | ADMIN/MANAGER | Bat/tat hien thi |
| PATCH | `/api/v1/admin/content-categories/reorder` | ADMIN/MANAGER | Sap xep thu tu |

## 9.7 Admin - Content item

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/content-items` | ADMIN/MANAGER | Liet ke noi dung |
| POST | `/api/v1/admin/content-items` | ADMIN/MANAGER | Tao noi dung |
| GET | `/api/v1/admin/content-items/{id}` | ADMIN/MANAGER | Chi tiet noi dung |
| PATCH | `/api/v1/admin/content-items/{id}` | ADMIN/MANAGER | Cap nhat mot phan noi dung, field bo trong duoc giu nguyen |
| DELETE | `/api/v1/admin/content-items/{id}` | ADMIN/MANAGER | Xoa noi dung |
| PATCH | `/api/v1/admin/content-items/{id}/visibility` | ADMIN/MANAGER | Bat/tat hien thi |
| PATCH | `/api/v1/admin/content-items/reorder` | ADMIN/MANAGER | Sap xep thu tu |
| POST | `/api/v1/admin/content-items/{id}/media` | ADMIN/MANAGER | Upload media lien quan |

## 9.8 Admin - Song category

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/song-categories` | ADMIN/MANAGER | Liet ke danh muc bai hat |
| POST | `/api/v1/admin/song-categories` | ADMIN/MANAGER | Tao danh muc bai hat |
| GET | `/api/v1/admin/song-categories/{id}` | ADMIN/MANAGER | Chi tiet danh muc |
| PATCH | `/api/v1/admin/song-categories/{id}` | ADMIN/MANAGER | Cap nhat mot phan danh muc, field bo trong duoc giu nguyen |
| DELETE | `/api/v1/admin/song-categories/{id}` | ADMIN/MANAGER | Xoa danh muc |
| PATCH | `/api/v1/admin/song-categories/{id}/visibility` | ADMIN/MANAGER | Bat/tat hien thi |
| PATCH | `/api/v1/admin/song-categories/reorder` | ADMIN/MANAGER | Sap xep thu tu |

## 9.9 Admin - Song

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/songs` | ADMIN/MANAGER | Liet ke bai hat |
| POST | `/api/v1/admin/songs` | ADMIN/MANAGER | Tao bai hat |
| GET | `/api/v1/admin/songs/{id}` | ADMIN/MANAGER | Chi tiet bai hat |
| PATCH | `/api/v1/admin/songs/{id}` | ADMIN/MANAGER | Cap nhat mot phan bai hat, field bo trong duoc giu nguyen |
| DELETE | `/api/v1/admin/songs/{id}` | ADMIN/MANAGER | Xoa bai hat |
| PATCH | `/api/v1/admin/songs/{id}/visibility` | ADMIN/MANAGER | Bat/tat hien thi |
| PATCH | `/api/v1/admin/songs/reorder` | ADMIN/MANAGER | Sap xep thu tu |

## 9.10 Admin - Media

| Method | Path | Role | Mo ta |
|---|---|---|---|
| POST | `/api/v1/admin/media` | ADMIN/MANAGER | Upload file media |
| GET | `/api/v1/admin/media/{id}` | ADMIN/MANAGER | Lay thong tin media |
| DELETE | `/api/v1/admin/media/{id}` | ADMIN/MANAGER | Xoa media |

## 9.11 Admin - Data profile

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/data-profiles` | ADMIN/MANAGER | Liet ke ho so du lieu |
| POST | `/api/v1/admin/data-profiles` | ADMIN/MANAGER | Tao ho so du lieu |
| GET | `/api/v1/admin/data-profiles/{id}` | ADMIN/MANAGER | Chi tiet ho so |
| PATCH | `/api/v1/admin/data-profiles/{id}` | ADMIN/MANAGER | Cap nhat mot phan ho so, field bo trong duoc giu nguyen |
| DELETE | `/api/v1/admin/data-profiles/{id}` | ADMIN/MANAGER | Xoa ho so |
| PATCH | `/api/v1/admin/data-profiles/{id}/visibility` | ADMIN/MANAGER | Bat/tat hien thi ho so |
| PATCH | `/api/v1/admin/data-profiles/reorder` | ADMIN/MANAGER | Sap xep thu tu ho so theo tung `profileType` |

## 9.12 Personal Notes

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/notes` | ADMIN/MANAGER/USER | Liet ke ghi chu cua chinh user dang dang nhap |
| POST | `/api/v1/notes` | ADMIN/MANAGER/USER | Tao ghi chu ca nhan |
| GET | `/api/v1/notes/{id}` | ADMIN/MANAGER/USER | Chi tiet ghi chu cua minh |
| PUT | `/api/v1/notes/{id}` | ADMIN/MANAGER/USER | Cap nhat ghi chu cua minh |
| DELETE | `/api/v1/notes/{id}` | ADMIN/MANAGER/USER | Xoa ghi chu cua minh |
| PATCH | `/api/v1/notes/{id}/pin` | ADMIN/MANAGER/USER | Ghim/bo ghim ghi chu |
| PATCH | `/api/v1/notes/{id}/archive` | ADMIN/MANAGER/USER | Luu tru/bo luu tru ghi chu |

## 9.13 Admin - Dashboard

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/dashboard/overview` | ADMIN/MANAGER | Tong hop so lieu dashboard admin |

## 9.14 Admin - Users

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/users` | ADMIN/MANAGER | Liet ke tai khoan nguoi dung |
| GET | `/api/v1/admin/users/{id}` | ADMIN/MANAGER | Chi tiet tai khoan |
| POST | `/api/v1/admin/users` | ADMIN | Tao tai khoan moi |
| PATCH | `/api/v1/admin/users/{id}` | ADMIN | Cap nhat mot phan thong tin tai khoan + profile, field bo trong duoc giu nguyen |
| PATCH | `/api/v1/admin/users/{id}/active` | ADMIN | Khoa/mo khoa tai khoan |
| PATCH | `/api/v1/admin/users/{id}/role` | ADMIN | Doi role tai khoan |
| PATCH | `/api/v1/admin/users/{id}/reset-password` | ADMIN | Cap lai mat khau |
| DELETE | `/api/v1/admin/users/{id}` | ADMIN | Xoa mem tai khoan |

## 9.15 Public - Home modules

| Method | Path | Auth | Mo ta |
|---|---|---|---|
| GET | `/api/v1/public/home-modules` | Bearer (ADMIN/MANAGER/USER) | Danh sach module trang chu dang bat, da sap xep |

## 9.16 Admin - Settings

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/settings` | ADMIN | Lay cau hinh he thong hien tai |
| PATCH | `/api/v1/admin/settings` | ADMIN | Cap nhat mot phan cau hinh he thong, field bo trong duoc giu nguyen |
| POST | `/api/v1/admin/settings/reset` | ADMIN | Reset cau hinh ve gia tri mac dinh |
| GET | `/api/v1/admin/settings/status` | ADMIN | Lay cac the trang thai he thong |
| GET | `/api/v1/admin/settings/version` | ADMIN | Lay thong tin version/runtime hien tai |

## 9.17 Admin - Home modules

| Method | Path | Role | Mo ta |
|---|---|---|---|
| GET | `/api/v1/admin/home-modules` | ADMIN/MANAGER | Danh sach cau hinh module trang chu cho admin |
| PATCH | `/api/v1/admin/home-modules` | ADMIN | Cap nhat full danh sach module trang chu sau khi reorder/edit/toggle |

---

## 10) Query param dung chung cho list API

Nhieu endpoint list dung cung co che:
- `page` (bat dau tu 1, mac dinh 1)
- `page_size` (mac dinh 20, toi da 100)
- `sort` (ten field)
- `order` (`asc` | `desc`)

Query rieng theo module:
- Content/Song list: `q`, `is_visible`, `category_id`, `type` (`TRUYEN_THONG|NET_TIEU_BIEU|SO_DO_LICH_SU`, tuy endpoint)
- Data profile list: `profileType` (`THU_TRUONG|CHIEN_SI|ANH_HUNG`), `q`, `is_visible`
- Personal notes list: `q`, `is_archived`
- User list: `q`, `role` (`ADMIN|MANAGER|USER`), `is_active`

Luu y:
- Cac endpoint update dung `PATCH` se chi ghi de cac field duoc gui len.
- Field nao khong gui trong request se duoc giu nguyen gia tri cu o DB.

Vi du:

```bash
curl "http://localhost:8080/api/v1/public/songs?page=1&page_size=10&sort=createdAt&order=desc" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 11) Mau payload quan trong

### 11.1 Tao content category

```json
{
  "type": "TRUYEN_THONG",
  "parentId": null,
  "name": "Tin noi bo",
  "slug": "tin-noi-bo",
  "description": "Thong bao noi bo",
  "isVisible": true,
  "sortOrder": 1
}
```

Rule:
- Toi da 2 tang category.
- `type` bat buoc thuoc 1 trong 3 gia tri: `TRUYEN_THONG`, `NET_TIEU_BIEU`, `SO_DO_LICH_SU`.
- `slug` unique trong cung scope (`type + parent`).

### 11.2 Tao content item

```json
{
  "categoryId": 3,
  "title": "Thong bao so 01",
  "summary": "Noi dung tom tat",
  "bodyHtml": "<p>Noi dung day du</p>",
  "coverMediaId": 1,
  "isVisible": true,
  "sortOrder": 1,
  "publishedAt": "2026-02-06T07:00:00Z"
}
```

Rule:
- `categoryId` phai la category cap con (khong duoc la root).
- `type` cua content item duoc suy ra tu category, khong can gui rieng trong request.

Response content item hien tra them:
- `type`

### 11.3 Tao song

```json
{
  "categoryId": 2,
  "title": "Bai hat moi",
  "lyric": "Noi dung loi bai hat...",
  "audioMediaId": 4,
  "audioUrl": null,
  "durationSec": 180,
  "isVisible": true,
  "sortOrder": 1
}
```

Rule bat buoc:
- Chi duoc cung cap dung 1 trong 2 truong:
  - `audioMediaId`
  - `audioUrl`

Neu gui ca 2 hoac khong gui cai nao -> `BAD_REQUEST`.

### 11.4 Reorder (vi du song)

```json
{
  "categoryId": 2,
  "orders": [
    { "id": 10, "sortOrder": 1 },
    { "id": 12, "sortOrder": 2 }
  ]
}
```

### 11.5 Bat/tat hien thi

```json
{
  "isVisible": false
}
```

Neu tat category, he thong se tat hien thi cac muc con lien quan theo rule service.

### 11.6 Tao data profile

```json
{
  "profileType": "THU_TRUONG",
  "fullName": "Nguyen Van A",
  "position": "Chi huy truong",
  "unitName": "Bo chi huy trung doan",
  "rankName": "Thieu tuong",
  "heroTitle": null,
  "contactPhone": "0901222333",
  "birthDate": "1975-03-10",
  "hometown": "Ha Noi",
  "summary": "Thong tin tom tat",
  "biography": "<p>Tieu su day du</p>",
  "achievements": "<ul><li>Bang khen...</li></ul>",
  "avatarMediaId": 1,
  "isVisible": true,
  "sortOrder": 1
}
```

Rule:
- `profileType` bat buoc thuoc 1 trong 3 gia tri: `THU_TRUONG`, `CHIEN_SI`, `ANH_HUNG`.
- Neu khong truyen `sortOrder`, he thong tu gan max+1 theo `profileType`.

### 11.7 Reorder data profile

```json
{
  "profileType": "CHIEN_SI",
  "orders": [
    { "id": 3, "sortOrder": 1 },
    { "id": 4, "sortOrder": 2 }
  ]
}
```

Rule:
- Tat ca `id` trong `orders` phai cung `profileType`.

### 11.8 Tao personal note

```json
{
  "title": "Viec can lam trong tuan",
  "content": "Ra soat noi dung cho duyet.",
  "colorCode": "#FDE68A",
  "reminderAt": "2026-03-10T01:00:00Z",
  "isPinned": true
}
```

### 11.9 Patch pin/archive personal note

```json
{
  "value": true
}
```

### 11.10 Mau response dashboard overview (rut gon)

```json
{
  "summary": {
    "totalPosts": 156,
    "totalProfiles": 89,
    "totalSongs": 24,
    "totalAccounts": 42,
    "viewsToday": 1247,
    "editsToday": 18
  },
  "monthlyContent": [
    { "label": "T1", "value": 45 },
    { "label": "T2", "value": 52 }
  ],
  "contentDistribution": [
    { "label": "Truyen thong", "value": 31 },
    { "label": "Net tieu bieu", "value": 22 },
    { "label": "So do lich su", "value": 11 },
    { "label": "Ho so thu truong", "value": 12 },
    { "label": "Ho so chien si", "value": 37 },
    { "label": "Ho so anh hung", "value": 8 },
    { "label": "Ca khuc", "value": 24 }
  ],
  "weeklyVisits": [
    { "label": "T2", "value": 120 },
    { "label": "T3", "value": 145 }
  ],
  "recentActivities": [],
  "systemStatuses": [],
  "pendingItems": []
}
```

### 11.11 Tao user (admin)

```json
{
  "phone": "0900000010",
  "password": "User@2026",
  "role": "USER",
  "isActive": true,
  "profile": {
    "fullName": "Nguyen Van Moi",
    "position": "Chuyen vien",
    "unitName": "Phong Tong hop",
    "rankName": "Trung uy",
    "email": "moi.nguyen@bcttg.local",
    "address": "Ha Noi",
    "birthDate": "1995-10-20"
  }
}
```

Rule:
- `phone`: chi gom so, do dai 8-15.
- `password`: toi thieu 8 ky tu, phai co chu hoa + chu thuong + so.
- `profile.fullName` bat buoc.

### 11.12 Khoa/mo khoa user

```json
{
  "value": false
}
```

### 11.13 Doi role user

```json
{
  "role": "MANAGER"
}
```

### 11.14 Cap lai mat khau user

```json
{
  "newPassword": "Reset@2026"
}
```

Rule:
- Password moi cung dung policy nhu luc tao tai khoan.

### 11.15 Vi du so do lich su dung chung module content

Tao category root cho so do lich su:

```json
{
  "type": "SO_DO_LICH_SU",
  "parentId": null,
  "name": "So do lich su",
  "slug": "so-do-lich-su",
  "description": "Danh muc tong cho cac so do lich su",
  "isVisible": true,
  "sortOrder": 1
}
```

Tao bai viet trong so do lich su:

```json
{
  "categoryId": 9,
  "title": "So do vong tron don vi",
  "summary": "Mo ta tong quan so do",
  "bodyHtml": "<p>Noi dung chi tiet theo don vi</p>",
  "coverMediaId": 1,
  "isVisible": true,
  "sortOrder": 1,
  "publishedAt": "2026-03-07T07:00:00Z"
}
```

Luu y:
- Tim kiem, loc, sap xep va CRUD cua "So do lich su" dung chung endpoint content.
- Khong co bang rieng va khong co controller rieng cho module nay.

### 11.16 PATCH settings (admin)

```json
{
  "systemName": "BCTTG",
  "timezone": "Asia/Bangkok",
  "recordsPerPage": 20,
  "sessionTimeout": 120
}
```

Luu y:
- Chi can gui cac field can doi.
- `smtpPass` khong tra plaintext; response chi tra trang thai da cau hinh va gia tri masked.

### 11.17 PATCH home modules (admin)

```json
{
  "modules": [
    {
      "id": "banner",
      "name": "Banner Chinh",
      "description": "Hinh anh banner xoay vong tren dau trang chu",
      "enabled": true,
      "sortOrder": 1
    },
    {
      "id": "truyen-thong",
      "name": "Truyen thong",
      "description": "Noi dung noi bat",
      "enabled": true,
      "sortOrder": 2
    }
  ]
}
```

Luu y:
- Request phai gui day du danh sach module dang cau hinh.
- `itemCount` la field read-only, backend tu tinh tu du lieu thuc te.

---

## 12) Upload va phuc vu media

### 12.1 Upload media

```bash
curl -X POST "http://localhost:8080/api/v1/admin/media" \
  -H "Authorization: Bearer <TOKEN>" \
  -F "file=@/path/to/file.jpg"
```

Response tra ve `id`, `url`, `storageKey`, `mimeType`, `sizeBytes`.

### 12.2 Truy cap file

File tra qua endpoint:
- `GET /files/{storageKey}`

Vi du:
- `http://localhost:8080/files/2026/02/06/uuid_file.jpg`

---

## 13) Su dung Swagger hieu qua

1. Mo `http://localhost:8080/swagger-ui`
2. Goi `POST /api/v1/auth/login` de lay token.
3. Bam `Authorize`.
4. Nhap JWT token vao truong bearer.
5. Thu cac endpoint public va admin. Namespace `/api/v1/public/**` hien cung can JWT.

Cac nhom tag hien thi:
- Auth
- Content Categories (Public/Admin)
- Content Items (Public/Admin)
- Data Profiles (Public/Admin)
- Song Categories (Public/Admin)
- Songs (Public/Admin)
- Media (Public/Admin)
- Personal Notes
- Dashboard (Admin)
- Settings (Admin)
- Home Modules (Public/Admin)
- Users (Admin)

---

## 14) Kiem tra nhanh bang cURL

### 14.1 Dang nhap

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"0900000001\",\"password\":\"Admin@2026\"}"
```

### 14.2 Lay danh sach bai hat public

```bash
curl -i http://localhost:8080/api/v1/public/songs \
  -H "Authorization: Bearer <TOKEN>"
```

### 14.3 Kiem tra user seed

```bash
docker exec -it bcttg-mysql mysql -ubcttg -pBcttg@2026 -D bcttg -e "SELECT phone, role, is_active FROM user_accounts;"
```

### 14.4 Lay danh sach data profile public

```bash
curl -i "http://localhost:8080/api/v1/public/data-profiles?profileType=THU_TRUONG&page=1&page_size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

### 14.5 Tao personal note (can token)

```bash
curl -i -X POST http://localhost:8080/api/v1/notes \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Viec can lam\",\"content\":\"Ra soat dashboard\",\"colorCode\":\"#BFDBFE\",\"isPinned\":true}"
```

### 14.6 Lay dashboard admin overview

```bash
curl -i http://localhost:8080/api/v1/admin/dashboard/overview \
  -H "Authorization: Bearer <TOKEN>"
```

### 14.7 Liet ke user (admin/manager)

```bash
curl -i "http://localhost:8080/api/v1/admin/users?page=1&page_size=20&role=USER&is_active=true" \
  -H "Authorization: Bearer <TOKEN>"
```

### 14.8 Tao user (admin)

```bash
curl -i -X POST http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"0900000010\",\"password\":\"User@2026\",\"role\":\"USER\",\"isActive\":true,\"profile\":{\"fullName\":\"Nguyen Van Moi\"}}"
```

### 14.9 Cap lai mat khau user (admin)

```bash
curl -i -X PATCH http://localhost:8080/api/v1/admin/users/10/reset-password \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"newPassword\":\"Reset@2026\"}"
```

### 14.10 Lay home modules public

```bash
curl -i http://localhost:8080/api/v1/public/home-modules \
  -H "Authorization: Bearer <TOKEN>"
```

### 14.11 Cap nhat settings (admin, partial)

```bash
curl -i -X PATCH http://localhost:8080/api/v1/admin/settings \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"systemName\":\"BCTTG\",\"sessionTimeout\":120}"
```

### 14.12 Cap nhat home modules (admin)

```bash
curl -i -X PATCH http://localhost:8080/api/v1/admin/home-modules \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"modules\":[{\"id\":\"banner\",\"name\":\"Banner Chinh\",\"description\":\"Hinh anh banner xoay vong tren dau trang chu\",\"enabled\":true,\"sortOrder\":1},{\"id\":\"truyen-thong\",\"name\":\"Truyen thong\",\"description\":\"Noi dung noi bat\",\"enabled\":true,\"sortOrder\":2},{\"id\":\"net-tieu-bieu\",\"name\":\"Net tieu bieu\",\"description\":\"Guong dien hinh tieu bieu\",\"enabled\":true,\"sortOrder\":3},{\"id\":\"thu-truong\",\"name\":\"Thu truong\",\"description\":\"Ho so thu truong\",\"enabled\":true,\"sortOrder\":4},{\"id\":\"anh-hung\",\"name\":\"Anh hung\",\"description\":\"Ho so anh hung\",\"enabled\":true,\"sortOrder\":5},{\"id\":\"ca-khuc\",\"name\":\"Ca khuc\",\"description\":\"Thu vien bai hat\",\"enabled\":true,\"sortOrder\":6},{\"id\":\"tin-tuc\",\"name\":\"Tin tuc\",\"description\":\"Cac ban tin tong hop\",\"enabled\":false,\"sortOrder\":7}]}"
```

---

## 15) Troubleshooting (cac loi thuong gap)

## 15.1 Loi `Schema validation: missing table ...`

Dau hieu:
- API `Exit 1`
- log co `missing table [content_categories]`

Nguyen nhan thuong gap:
- API validate schema truoc khi migration.
- DB volume cu/khong dong bo.

Xu ly:
1. Dam bao profile docker dang dung `ddl-auto: none` trong `application-docker.yml`.
2. Dam bao `SPRING_FLYWAY_ENABLED=true`.
3. Reset local:

```bash
docker-compose down -v
docker-compose up -d --build
```

## 15.2 Loi `Can't connect to MySQL server ...`

Kiem tra:
```bash
docker-compose ps
docker-compose logs --tail=200 mysql
```

Cho MySQL khoi dong xong roi moi query.

## 15.3 Loi login `User not found`

Kiem tra du lieu seed:

```bash
docker exec -it bcttg-mysql mysql -ubcttg -pBcttg@2026 -D bcttg -e "SELECT phone, role FROM user_accounts;"
```

Neu rong -> migration chua chay, quay lai muc 15.1.

## 15.4 Loi `Invalid character found in method name`

Nguyen nhan:
- Gui request HTTPS/TLS vao cong HTTP (8080).

Xu ly:
- dung `http://...:8080` khi goi truc tiep.
- neu dung HTTPS thi dat reverse proxy (Nginx) o truoc.

## 15.5 Loi compile `illegal character: '\ufeff'`

Nguyen nhan:
- File Java duoc luu dang UTF-8 BOM.

Xu ly:
- Luu lai file dang UTF-8 khong BOM.

## 15.6 Swagger tra 500 `/api-docs`

Kiem tra:
- log `api` de xem stack trace that.
- dam bao dependency springdoc tuong thich trong `pom.xml`.
- dam bao app khoi dong hoan toan truoc khi mo swagger.

## 15.7 Loi `Access denied for user 'root'@'localhost'`

Nguyen nhan:
- Mat khau root truyen vao lenh khong dung.
- Dang ket noi bang user `root` trong khi user app la `bcttg`.

Xu ly nhanh:
```bash
docker exec -it bcttg-mysql mysql -ubcttg -pBcttg@2026 -D bcttg -e "SHOW TABLES;"
```

Neu can dung root:
```bash
docker exec -it bcttg-mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> -e "SHOW DATABASES;"
```

## 15.8 Loi module Users (`403` / `400`)

Truong hop thuong gap:
- `MANAGER` goi API ghi (`POST/PATCH/DELETE`) cua `/api/v1/admin/users` -> `403`.
- `ADMIN` tu khoa/xoa/chuyen role chinh minh -> `403`.
- Password khong dat policy (thieu chu hoa/thuong/so, < 8 ky tu) -> `400`.
- `phone` khong hop le (khong phai so hoac do dai khong nam trong 8-15) -> `400`.

## 15.9 Loi `401` tren namespace `/api/v1/public/**`

Nguyen nhan:
- Namespace public cua du an hien chi public theo nghiep vu, khong public theo security.
- Request thieu header `Authorization: Bearer <TOKEN>` hoac token het han.

Xu ly:
1. Goi `POST /api/v1/auth/login` de lay JWT.
2. Gui lai request voi header bearer.
3. Neu dung Swagger/Postman/frontend, dam bao khong gui token cu het han.

---

## 16) Van hanh production (khuyen nghi)

1. Dat JWT secret manh, khong commit secret that.
2. Doi toan bo password DB/seed user.
3. Chi mo cong cong khai can thiet:
   - mo `80/443` (neu dung Nginx),
   - han che/moi truong noi bo cho `3306`,
   - co the khong can mo `8080` ra internet neu da reverse proxy.
4. Bat HTTPS (Nginx + Let's Encrypt).
5. Backup dinh ky volume MySQL.
6. Theo doi log va canh bao tai nguyen container.

---

## 17) Lenh van hanh nhanh (cheat sheet)

```bash
# Build + start
docker-compose up -d --build

# Xem trang thai
docker-compose ps

# Xem log API
docker-compose logs --tail=200 api

# Xem log MySQL
docker-compose logs --tail=200 mysql

# Kiem tra bang DB
docker exec -it bcttg-mysql mysql -ubcttg -pBcttg@2026 -D bcttg -e "SHOW TABLES;"

# Reset sach du lieu local
docker-compose down -v
docker-compose up -d --build
```

---

## 18) Ke hoach len back-end cho cac man dang dung mock

Ngay cap nhat: `2026-03-07`

### 18.1 Hien trang

| Man hinh | Duong dan | Service hien tai | Hien trang |
|---|---|---|---|
| Cau hinh | `/cau-hinh` | `SettingsService` | Dang tra mock cho `getSettings`, `getSystemStatusCards`, `getVersion` |
| Module trang chu | `/module-trang-chu` | `HomeModulesService` | Dang dung store mock trong RAM cho `getAll`, `saveAll`, `update` |
| Nhat ky | `/nhat-ky` | `LogsService` | Dang tra mock cho `getLoginLogs`, `getSystemLogs` |
| Bao cao | `/bao-cao` | `ReportsService` | Dang tra mock cho `getMonthlyData`, `getUserActivityData`, `getTopContentData`, `getSummaryCards` |

### 18.2 Nguyen tac chot truoc khi lam back-end

1. Tat ca admin endpoint di theo namespace dang dung trong du an: `/api/v1/admin/...`.
2. Response theo format san co: `ApiResponse<T>`, co `success`, `data`, `meta`, `error`.
3. Danh sach co filter/pagination phai lam server-side, khong de frontend loc tren mock.
4. Moc thoi gian tra ve theo ISO 8601, frontend tu format hien thi.
5. Khong dua React-specific field vao contract API. Dac biet, `HomeModule.icon` khong the di tu backend; backend chi tra `id` hoac `iconKey`, frontend map icon.
6. Export file phai co endpoint rieng, khong dung `alert`.
7. Phan quyen toi thieu:
   - `settings`: chi `ADMIN`
   - `home-modules`: `ADMIN`, `MANAGER`
   - `logs`: `ADMIN`, `MANAGER`
   - `reports`: `ADMIN`, `MANAGER`

### 18.3 `/cau-hinh`

#### Muc tieu

- Doc va luu cau hinh he thong that.
- Lay duoc trang thai he thong thay cho mock card.
- Tach ro phan cau hinh nghiep vu va phan van hanh.

#### Du lieu can co

- Cau hinh chung:
  - `systemName`
  - `systemDescription`
  - `timezone`
  - `language`
  - `recordsPerPage`
  - `showAvatar`
  - `compactMode`
- Cau hinh bao mat:
  - `passwordMinLength`
  - `requireUppercase`
  - `requireNumber`
  - `requireSpecialChar`
  - `sessionTimeout`
  - `maxLoginAttempts`
  - `require2fa`
- Cau hinh email:
  - `smtpHost`
  - `smtpPort`
  - `smtpUser`
  - `smtpPass`
  - `emailFrom`
  - `notifyNewLogin`
  - `notifyPendingContent`
  - `notifySecurityAlerts`
  - `notifyPeriodicReports`
- Cau hinh backup:
  - `autoBackupEnabled`
  - `backupFrequency`
  - `backupRetention`
- Operational metadata:
  - `version`
  - `statusCards`
  - `updatedAt`
  - `updatedBy`

#### Endpoint toi thieu de thay mock hien tai

- `GET /api/v1/admin/settings`
  - Tra ve toan bo form data.
- `PATCH /api/v1/admin/settings`
  - Luu toan bo cau hinh.
- `GET /api/v1/admin/settings/status`
  - Tra ve card suc khoe he thong.
- `GET /api/v1/admin/settings/version`
  - Tra ve phien ban he thong.

#### Endpoint nen co de hoan thien man

- `POST /api/v1/admin/settings/test-email`
- `GET /api/v1/admin/settings/backups`
- `POST /api/v1/admin/settings/backups/{id}/restore`
- `GET /api/v1/admin/settings/backups/{id}/download`
- `POST /api/v1/admin/settings/cache/clear`
- `POST /api/v1/admin/settings/reset`

#### Cong viec back-end

1. Tao bang `system_settings` hoac co che luu 1 ban ghi config toan cuc.
2. Ma hoa hoac luu an toan cho `smtpPass`, khong tra plaintext neu khong can thiet.
3. Tao service tinh `statusCards` tu health check app, DB, storage, uptime.
4. Ghi audit log khi thay doi cau hinh.
5. Neu backup la tinh nang that, can bang `backup_jobs` va thao tac restore/download ro rang.

#### Cong viec frontend di kem

1. Bo sung `SettingsService.update`.
2. Xu ly state save/loading/error.
3. Noi cac button `Gui email thu`, `Tai xuong`, `Khoi phuc`, `Xoa cache`, `Reset cau hinh`.
4. Neu backend khong tra `smtpPass` plaintext, can dung co che masked field hoac update tung phan.

#### Dieu kien nghiem thu

1. Reload trang van thay cau hinh vua luu.
2. Card he thong lay du lieu that, khong phu thuoc mock.
3. Version den tu backend/build metadata.
4. Moi thao tac nguy hiem deu co audit log.

### 18.4 `/module-trang-chu`

#### Muc tieu

- Luu cau hinh hien thi module trang chu vao DB thay vi store trong RAM.
- Dam bao thu tu, trang thai bat/tat, ten, mo ta duoc luu ben vung.
- Co the tai su dung cho public homepage sau nay.

#### Van de can chot truoc

1. `HomeModule.icon` dang la `React.ElementType`, backend khong the tra kieu nay.
2. `itemCount` nen la field read-only, tinh tu du lieu thuc te cua tung module.
3. `id` can la slug on dinh, dung de map icon va logic module.

#### Contract de xuat

```json
{
  "id": "banner",
  "name": "Banner Chinh",
  "description": "Hinh anh banner xoay vong tren dau trang chu",
  "enabled": true,
  "sortOrder": 1,
  "itemCount": 5,
  "updatedAt": "2026-03-07T07:00:00Z"
}
```

#### Endpoint de xuat

- `GET /api/v1/admin/home-modules`
  - Tra ve danh sach module da sap xep.
- `PATCH /api/v1/admin/home-modules`
  - Nhan full list sau khi admin reorder/edit/toggle.
- `GET /api/v1/public/home-modules`
  - Hien tai backend dang yeu cau Bearer token cho namespace public nay.

#### Cong viec back-end

1. Tao bang `home_modules` gom `id`, `name`, `description`, `enabled`, `sort_order`, `updated_at`, `updated_by`.
2. Seed du lieu ban dau cho cac module co san:
   - `banner`
   - `truyen-thong`
   - `net-tieu-bieu`
   - `thu-truong`
   - `anh-hung`
   - `ca-khuc`
   - `tin-tuc`
3. Tinh `itemCount` tu bang noi dung/ho so/ca khuc lien quan, khong luu tay neu khong bat buoc.
4. Validate khong trung `sort_order`, khong mat module khi save full list.
5. Ghi audit log khi reorder, bat/tat, sua thong tin module.

#### Cong viec frontend di kem

1. Doi `HomeModule` API model thanh kieu khong chua `icon`.
2. Map icon bang `id` o frontend.
3. Doi field `order` sang `sortOrder` de thong nhat voi backend hien co.
4. Can nhac bo `update(id, data)` neu luong hien tai chi save bulk.

#### Dieu kien nghiem thu

1. Reorder, edit, toggle luu xong refresh van dung thu tu.
2. Khong bi mat cau hinh khi deploy lai app frontend.
3. Neu homepage public tich hop, thu tu module public khop admin.

### 18.5 `/nhat-ky`

#### Muc tieu

- Lay du lieu nhat ky dang nhap va nhat ky he thong tu backend.
- Filter, tim kiem, phan trang, export thuc hien server-side.
- Tao duoc audit trail that, khong chi thay mock.

#### Du lieu can co

- Login logs:
  - `id`
  - `userId`
  - `userName`
  - `unitName`
  - `action`
  - `ipAddress`
  - `device`
  - `status`
  - `failureReason`
  - `createdAt`
- System logs:
  - `id`
  - `actorId`
  - `actorName`
  - `module`
  - `action`
  - `description`
  - `level`
  - `metadata`
  - `createdAt`

#### Endpoint de xuat

- `GET /api/v1/admin/logs/login`
  - Query: `page`, `page_size`, `q`, `status`, `action`, `period`, `from`, `to`, `sort`, `order`
- `GET /api/v1/admin/logs/system`
  - Query: `page`, `page_size`, `q`, `level`, `module`, `action`, `period`, `from`, `to`, `sort`, `order`
- `GET /api/v1/admin/logs/summary`
  - Tra ve 4 so lieu card dau trang.
- `GET /api/v1/admin/logs/export`
  - Query: `type=login|system`, `format=xlsx|csv`, filter giong list

#### Cong viec back-end

1. Tao bang `login_logs` va `audit_logs` hoac co che log tuong duong.
2. Hook vao luong dang nhap/dang xuat de ghi `login_logs`.
3. Hook vao cac thao tac CRUD quan trong de ghi `audit_logs`.
4. Co luat retention, vi du giu 90 ngay tren bang online va archive neu can.
5. Toi uu index cho `created_at`, `user_id`, `level`, `module`, `action`.

#### Cong viec frontend di kem

1. Chuyen sang fetch co filter va pagination that.
2. Tach pagination cho login log va system log neu hai tab khac nhau.
3. Dung `meta.total_elements`, `meta.total_pages` tu backend.
4. Noi nut `Xuat nhat ky` sang endpoint download file.

#### Dieu kien nghiem thu

1. Login/logout that phat sinh log moi.
2. Sua/xoa/them du lieu tren admin sinh system log dung module va action.
3. Search/filter/pagination khop tong so ban ghi tu backend.
4. Export file phan anh dung bo loc dang chon.

### 18.6 `/bao-cao`

#### Muc tieu

- Thay toan bo chart va summary card bang du lieu tong hop that.
- Ho tro filter theo `week`, `month`, `quarter`, `year`.
- Export Excel that cho tung loai bao cao.

#### Van de can chot truoc

1. `timePeriod` dang la state local, chua truyen vao service.
2. `handleExportExcel` hien tai chi `alert`.
3. `MonthlyData.month` khong phu hop voi tat ca period; backend nen tra field tong quat hon, vi du `label`.
4. Bao cao phu thuoc vao nguon du lieu thuc:
   - page views
   - login logs
   - thao tac admin
   - user active
   - top content

#### Endpoint de xuat

- `GET /api/v1/admin/reports/overview`
  - Query: `period=week|month|quarter|year`
  - Nen tra mot goi du lieu thong nhat:
    - `summaryCards`
    - `trendSeries`
    - `userActivity`
    - `topContent`
- `GET /api/v1/admin/reports/export`
  - Query: `type`, `period`, `format=xlsx`

#### Contract overview de xuat

```json
{
  "summaryCards": [
    {
      "id": "views",
      "title": "Tong luot xem",
      "value": "77400",
      "change": 18.5,
      "period": "so voi ky truoc",
      "iconKey": "views"
    }
  ],
  "trendSeries": [
    {
      "label": "T1",
      "views": 4500,
      "edits": 120,
      "logins": 850
    }
  ],
  "userActivity": [
    {
      "name": "Quan tri vien",
      "value": 5,
      "activity": 89
    }
  ],
  "topContent": [
    {
      "title": "Lich su hinh thanh Binh chung",
      "views": 2156,
      "trend": 15
    }
  ]
}
```

#### Cong viec back-end

1. Chot nguon tinh metric:
   - `views`: tu bang view tracking hoac analytics table
   - `edits` va `actions`: tu `audit_logs`
   - `logins`: tu `login_logs`
   - `active users`: tu user co phat sinh hoat dong trong ky
   - `avgTime`: tu session analytics, neu chua co thi phai bo hoac danh dau chua kha dung
2. Tao aggregate query hoac bang tong hop theo ngay de tranh scan raw log qua lon.
3. Dinh nghia ro cach tinh `change` so voi ky truoc.
4. Export bao cao theo `type` va `period`.
5. Dam bao cung mot `period` thi tat ca widget dung cung snapshot du lieu.

#### Cong viec frontend di kem

1. Doi `ReportsService` thanh mot API `getOverview(period)`.
2. Doi `MonthlyData.month` thanh `label` de dung cho moi period.
3. Goi lai data moi khi `timePeriod` thay doi.
4. Download file that thay cho `alert`.

#### Dieu kien nghiem thu

1. Doi period thi chart, card, top content doi theo cung mot bo du lieu.
2. So lieu tong hop khop voi log va du lieu goc trong DB.
3. Export tao duoc file xlsx cho moi loai dang co tren UI.

### 18.7 Thu tu trien khai de xuat

#### Pha 1: Chot contract va data model

1. Chot endpoint, request, response cho ca 4 man.
2. Chot quy uoc datetime, pagination, export.
3. Chot role duoc phep truy cap tung endpoint.

#### Pha 2: Lam nhung phan doc lap, de ra ket qua nhanh

1. `settings`
2. `home-modules`

Ly do: hai phan nay it phu thuoc, du lieu don gian, co the bo mock som.

#### Pha 3: Lam logging that

1. `login_logs`
2. `audit_logs`
3. `logs summary`
4. `logs export`

Ly do: day la nen tang du lieu cho bao cao.

#### Pha 4: Lam reports

1. Tao aggregate query/bang tong hop
2. `reports/overview`
3. `reports/export`

Ly do: bao cao phu thuoc vao log va analytics da co that.

### 18.8 Cac quyet dinh can chot som

1. `smtpPass` co duoc tra nguoc ra frontend hay chi cho phep overwrite?
2. Backup/download/restore tren man cau hinh la tinh nang that hay chi la UI placeholder?
3. `avgTime` trong bao cao lay tu dau neu he thong chua co session tracking?
4. Homepage public co se dung cung cau hinh module tu backend ngay trong dot nay khong?
5. Retention cua logs la bao lau va co can archive file/DB rieng hay khong?

### 18.9 Checklist hoan tat bo mock

1. Khong con import du lieu tu `lib/data/mock/settings`.
2. Khong con import du lieu tu `lib/data/mock/home-modules`.
3. Khong con import du lieu tu `lib/data/mock/logs`.
4. Khong con import du lieu tu `lib/data/mock/reports`.
5. `SettingsService`, `HomeModulesService`, `LogsService`, `ReportsService` deu goi `ApiClient`.
6. Export tren `/nhat-ky` va `/bao-cao` khong con dung `alert`.
7. Moi man reload lai van hien du lieu duoc luu tu backend.

---

Neu ban muon, toi co the viet them:
- bo tai lieu rieng cho Frontend (map endpoint + payload theo man hinh),
- bo Postman Collection,
- checklist release (dev -> staging -> production) theo quy trinh CI/CD.

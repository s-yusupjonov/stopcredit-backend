# StopCredit: 2026-10-07 audit va tuzatishlar

Asos: `2026-10-06` auditi va joriy kodni qayta to‘liq ko‘rib chiqish (backend `0188189`, frontend `60cda52`).
O‘zgarishlar commit qilinmagan — ikkala repoda ishchi nusxada turibdi.

## Tekshiruv natijasi

| Tekshiruv | Natija |
| --- | --- |
| Backend `mvn verify` | 33 ta test, 0 xato (yangi: 15 ta) |
| Backend haqiqiy ishga tushirish | Vaqtinchalik Postgres 16 + MinIO bilan: Flyway V1–V6 o‘tdi, curl smoke-test (login, validatsiya, 401/403/404/409, upload, advance, unblock, Excel) |
| Frontend | `tsc -b`, `eslint --max-warnings 0`, `vite build` — toza |
| Brauzer | Login → kredit yaratish → PDF yuklash → bosqichga yuborish → karta blokdan ochish → admin sahifasi; 1024px va 375px kenglikda |

## Backend tuzatishlari

**Xavfsizlik**
- `application.yml` dan LDAP xizmat paroli va DN default qiymati olib tashlandi; `changeme` admin paroli o‘rniga bo‘sh bo‘lsa tasodifiy bir martalik parol generatsiya qilinadi. LDAP sozlanmagan yoki `ldap://` bo‘lsa startupda ogohlantirish chiqadi.
- Rol bo‘yicha rad etilgan so‘rov **403 o‘rniga 401** qaytarardi (`/error` dispatch JWT’siz qayta tekshirilardi) — frontend buni “sessiya tugadi” deb foydalanuvchini chiqarib yuborardi. Endi 401/403 to‘g‘ridan-to‘g‘ri ProblemDetail sifatida yoziladi.
- Spring Boot’ning keraksiz in-memory `user` (logga yoziladigan parol bilan) o‘chirildi.
- Saralash faqat ruxsat etilgan maydonlar bo‘yicha (`createdBy.passwordHash` kabi nested sort yopildi).
- Qidiruvdagi `%` va `_` endi wildcard emas, oddiy belgi.
- `PUT /api/credits/{id}` faqat ANTI_FRAUD uchun (UI va README’dagi qoidaga moslandi).
- docker-compose: Postgres va MinIO portlari faqat `127.0.0.1` ga bog‘landi.

**Biznes qoidalar va ma’lumot izchilligi**
- Kredit va kartaga `version` (V6 migratsiya): eskirgan ekrandan saqlash 409 bilan rad etiladi, jimgina ustiga yozilmaydi.
- Umumiy kredit `PUT` statusni o‘zgartirishga urinsa 400; bir xil statusni qayta qo‘yish audit yozuvini yaratmaydi.
- Blokdan ochish buyrug‘i (UNBLOCK hujjat) o‘chirilmaydi — dalil sifatida saqlanadi.
- Faol bo‘lmagan, lekin parolni to‘g‘ri kiritgan foydalanuvchi endi “Login yoki parol noto‘g‘ri” emas, tushunarli 403 oladi va bu urinish limitga sanalmaydi.
- Admin bootstrap login boshqa rolda band bo‘lsa ilova yiqilmaydi, aniq xato logga yoziladi.

**Xabarlar va eksport**
- Barcha API xabarlari (ApiException, Bean Validation, Spring framework xatolari, 401/403) o‘zbekchada.
- Nullable maydonlar endi `null` sifatida keladi (avval umuman tushib qolardi).
- Kredit Excel eksporti o‘zbekcha sarlavha va qiymatlar bilan (karta eksporti bilan bir xil).

## Frontend tuzatishlari

**Komponentlarni to‘g‘ri ishlatish**
- antd static `message/notification/Modal.confirm` (theme va locale’ni ko‘rmaydi) → `App` konteksti orqali `shared/ui/feedback.ts`.
- antd `uz_UZ` locale va dayjs `uz-latn`: pagination, DatePicker, bo‘sh holat, tasdiq tugmalari o‘zbekcha.
- Eskirgan prop’lar: `destroyOnClose` → `destroyOnHidden`, `Empty imageStyle` → `styles.image`.
- Formalar: label’lar inputlarga bog‘landi (`FormField` + `htmlFor`), Enter bilan yuborish modallarda ham ishlaydi, `Space.Compact`, javob beruvchi `Row/Col`.
- `tsc -b` `vite.config.js` ni chiqarib qo‘yar va Vite `.ts` o‘rniga eskirgan `.js` ni o‘qir edi — build artefaktlari `node_modules/.tmp` ga yo‘naltirildi.

**UX xatolari**
- Muvaffaqiyatsiz login sessiyani tozalab yubormaydi; login’dan keyin asl so‘ralgan sahifaga qaytadi; login holatida `/login` ochilsa bosh sahifaga o‘tadi.
- Ro‘yxatlar: har 60 soniyadagi fon yangilanishida jadval “miltillamaydi”, gorizontal skroll, ariza/karta raqami havola (klaviatura va yangi tab uchun), URL’dagi noto‘g‘ri filtrlar e’tiborsiz qoldiriladi, sahifa chegaradan oshsa oxirgisiga qaytadi, filtrga mos bo‘sh holat matni, “Tozalash” tugmasi.
- Kredit sahifasi: status o‘zgartirishdan oldin tasdiqlash, “Orqaga” tugmasi (to‘g‘ridan-to‘g‘ri ochilganda ro‘yxatga), keyingi bo‘lim nomi ko‘rsatiladi; hujjatlar bosqich tartibida.
- Tahrirlash: kredit Anti-fraud bosqichidan o‘tgan bo‘lsa forma o‘rniga tushuntirish; karta statusi tahrirda bloklangan (faqat “Blokdan ochish”); o‘zgarish bo‘lmasa “Saqlash” o‘chiq; muvaffaqiyat xabarlari.
- PINFL/karta raqamiga bo‘shliq yoki harf bilan joylashtirilganda raqamlar kesilib qolmaydi.
- Pul maydoni ro‘yxatdagi format bilan bir xil (`1 234 567,50`), vergul ham nuqta ham qabul qilinadi, tahrirda 2 kasr xona ko‘rsatiladi.
- PDF tanlash MIME turi bo‘sh bo‘lsa ham `.pdf` kengaytmasi bo‘yicha qabul qilinadi; bo‘sh fayl rad etiladi.
- Karta hujjatlarida turi (cheklov/blokdan ochish) ko‘rsatiladi, buyruq o‘chirilmaydi (qulf belgisi).
- Foydalanuvchilar: qidiruv va holat filtri, o‘chirilgan `Switch` o‘rniga holat yorlig‘i, ikonka tugmalarga `aria-label`.
- 992px dan tor ekranda sidebar ikonkalarga yig‘iladi, 768px dan torda sarlavha/padding moslashadi; detal sahifasidagi `Descriptions` 3 ustunga siqilmaydi.
- Fayl yuklab olishda object URL darhol bekor qilinmaydi (ba’zi brauzerlarda yuklashni uzib qo‘yardi); fayl nomidagi sana mahalliy vaqt bo‘yicha.

**Infratuzilma**
- nginx: `client_max_body_size` 25m → 100m (backend bilan bir xil), hash’li `assets` uchun uzoq kesh, `index.html` uchun `no-cache`, xavfsizlik sarlavhalari.
- Vendor bo‘laklari (`react`, `antd`, `vendor`) alohida — release’lar orasida keshda qoladi.
- Ikkala repoga GitHub Actions CI (`mvn verify`; `npm ci`, lint, build).

## Hal qilinmagan — qaror yoki infra kerak

1. **LDAP paroli `init commit` dan beri git tarixida.** Konfiguratsiyadan olindi, lekin xizmat akkaunti paroli darhol almashtirilishi (rotate) kerak; tarixni qayta yozish alohida kelishiladi.
2. Default `LDAP_URL` hali `ldap://`; LDAPS uchun sertifikat/truststore infra bilan sozlanishi kerak.
3. PAN va PINFL ro‘yxat/eksportda to‘liq — maskalash rollar bo‘yicha biznes qarori talab qiladi.
4. Token `localStorage` da; HttpOnly cookie/CSP — alohida arxitektura ishi.
5. Postgres/MinIO bilan avtomatik integratsion testlar (Testcontainers) va browser E2E hali yo‘q; bu safar qo‘lda tekshirildi.
6. Login limiter bitta instans xotirasida; bir nechta instansda umumiy saqlash kerak bo‘ladi.
7. Hujjatni tiklash (soft delete), maker/checker, ish kuni bo‘yicha muddat, SLA ogohlantirishlari — biznes talablari.

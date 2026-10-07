# StopCredit backend va frontend texnik auditi

2026-10-06. Dasturchilar va texnik review uchun.

Ko‘rib chiqilgan ikki revisionda autentifikatsiya, ruxsatlar, kredit jarayoni, hujjatlar va frontend ishlashiga ta’sir qiladigan jiddiy muammolar bor. Quyidagi P0 va P1 bandlari bartaraf etilib, integratsion qabul testlari bajarilmaguncha ushbu revisionlarni production uchun tasdiqlamaslik tavsiya etiladi. Bu xulosa lokal kodga tegishli; production’da aynan shu revision ishlayotgani tekshirilmagan.

Backend: `stopcredit-backend`, commit `4c359e31a35f00097d063bce22c840331d44e84b`.

Frontend: `stopcredit-frontend`, commit `ae7b4027d1e0fd5a0ac00b9dc9378c1856860927`.

Hisobotda **33 ta topilma** bor: **1 P0, 17 P1, 15 P2**. Ular orasida qayta hosil qilingan xatolar, koddan tasdiqlangan nuqsonlar va production uchun yetishmayotgan himoyalar bor. Har bir bandning dalil darajasi ko‘rsatilgan. P0 — darhol ko‘riladigan sir bilan bog‘liq masala; P1 — release oldidan tuzatiladigan jiddiy xato yoki nazorat yetishmasligi; P2 — keyingi rejalashtirilgan tuzatishdagi muhim kamchilik.

## Tekshiruv doirasi va chegaralari

BE qisqartmasi `/Users/admin/AGRO/stopcredit-backend`, FE qisqartmasi `/Users/admin/AGRO/stopcredit-frontend` degani. Java fayllariga qisqa havolalardagi `java/` prefiksi BE dagi `src/main/java/uz/agrobank/stopcredit/` katalogini bildiradi. Raqamlar tekshirilgan revisiondagi satrlar.

Graphify har bir repoda alohida `extract --code-only --no-cluster` va vazifaga mos `query --budget 1500` bilan ishlatildi. Backendda 87 ta, frontendda 74 ta code-classified fayl indekslandi. Graphify tasniflamagan Dockerfile, env namunasi va CSS fayllari tegishli joylarda bevosita o‘qildi. Graphify xatosi kuzatilmadi; frontend so‘rovining 1500-token javobi qisqargani sababli xulosalar graphdan emas, tegishli source fayllaridan tekshirildi.

Tekshirilgan qatlamlar: controllerlar, service va mapperlar, JWT/LDAP, barcha role/stage qoidalari, entity/repository/SQL migratsiyalar, DTO validatsiyasi, PDF saqlash/yuklash/o‘chirish, Excel eksporti, frontend route/auth/cache/hooks/formalar/jadvallar, env/CORS/Docker/nginx, mavjud testlar va CI fayllari.

| Tekshiruv | Natija va chegarasi |
| --- | --- |
| Backend Java 21 kompilyatsiyasi | 82 ta asosiy Java source va mavjud test class kompilyatsiya bo‘ldi. |
| Standart `mvn test` | Yakunlanmadi. Surefire uchun `junit-platform-launcher:1.10.5` keshda yo‘q; Maven Central DNS tashqi ruxsatli urinishda ham ishlamadi. Bu test assertion failure emas. |
| Mavjud 6 ta `CreditStageTest` metodi | Kompilyatsiya qilingan classdan lokal reflection runner orqali chaqirildi va o‘tdi. Bu Surefire yoki application integration test o‘rnini bosmaydi. |
| Qo‘shimcha backend tekshiruvi | 8 ta muammoga yo‘naltirilgan probe o‘tdi. Haqiqiy service/mapper/validator/JWT/Spring transaction interceptor ishlatildi; repository, LDAP va storage o‘rnida test double bor. |
| Frontend funksional probe | 6 ta tekshiruv: pagination, bo‘sh balans, 200-record statistika, logout cache tozalamasligi va ikkala upload handlerning takroriy yuborishi. Browser E2E emas. |
| TypeScript 5.9.3 minimal reproducer | Kredit upload predicate uchun `TS2677` va `TS2345` qayta hosil qilindi; dependency tipi AntD 5.29.3 manbasi bilan tekshirildi. |
| Frontend to‘liq build/lint | Bajarilmadi: `npm ci` DNS xatosi, offline urinishda `zustand@4.5.7` keshda yo‘q. TypeScript probe to‘liq build natijasi sifatida ko‘rsatilmaydi. |
| Real DB/MinIO/AD va production | Ulanilmadi. Real foydalanuvchi, PINFL yoki karta ma’lumoti bilan sinov qilinmadi. Migratsiya/startup/load/restore/real browser ekranlari tekshirilmagan. |
| Dependency CVE tekshiruvi | To‘liq bajarilmadi. Faqat versiya eski ko‘ringani uchun CVE yoki zaiflik bor deb da’vo qilinmaydi. |

Mahsulot source kodi o‘zgartirilmadi. Audit fayllari, Graphify indekslari va build output yaratildi; frontenddagi avvaldan mavjud `.idea/` o‘zgarishlariga tegilmadi. Commit, push yoki deploy bajarilmadi. Hisobotda topilgan sirning qiymati keltirilmagan.

## Ustuvor tuzatishlar ro‘yxati

| ID | Daraja | Muammo | Egasi |
| --- | --- | --- | --- |
| SC01 | P0 | LDAP xizmat paroli repoda ochiq fallback sifatida turibdi | BE, DevOps, security |
| SC02 | P1 | LDAP default ulanishi TLSsiz | BE, DevOps |
| SC03 | P1 | Birinchi AD login yangi userni rollback qiladi | BE |
| SC04 | P1 | Frontenddagi AD lookup/create backendda qo‘llanmagan | BE, FE |
| SC05 | P1 | Kredit statusi uchun rol cheklovi umumiy PUT orqali chetlab o‘tiladi | BE |
| SC06 | P1 | Parallel amallar uchun version/lock yo‘q | BE |
| SC07 | P1 | Logoutda umumiy query cache qoladi | FE |
| SC08 | P1 | Balanssiz karta frontendni yiqitadi | BE, FE |
| SC09 | P1 | Kredit pagination ishlamaydi | FE |
| SC10 | P1 | PDFlar takroran upload qilinadi | FE |
| SC11 | P1 | Kredit upload kodida TypeScript predicate xatosi | FE |
| SC12 | P1 | MinIO va DB o‘zgarishlari izchil emas | BE |
| SC13 | P1 | Xavfsiz bo‘lmagan bootstrap va infratuzilma defaultlari | BE, DevOps |
| SC14 | P1 | Parol resetidan keyin eski JWT amal qilishda davom etadi | BE |
| SC15 | P1 | Oxirgi administratorni o‘chirib boshqaruvni bloklash mumkin | BE, FE |
| SC16 | P1 | Status va bosqichlar uchun o‘zgarmas audit tarixi yo‘q | BE, product |
| SC17 | P2 | Parol yangilashda minimal uzunlik tekshirilmaydi | BE |
| SC18 | P2 | AD identifikatori katta-kichik harfga bog‘liq lokal lookupga tayanadi | BE |
| SC19 | P2 | Login/LDAP uchun limit va timeoutlar belgilanmagan | BE, DevOps |
| SC20 | P2 | Besh baytli prefiks soxta PDFni o‘tkazadi | BE |
| SC21 | P2 | User loginini tahrirlash UI’da bor, backend uni saqlamaydi | BE, FE |
| SC22 | P2 | GET xatolari cheksiz skeleton yoki bo‘sh ro‘yxat sifatida chiqadi | FE |
| SC23 | P2 | Upload/delete/download/export xatolari foydalanuvchiga aytilmaydi | FE |
| SC24 | P2 | Kredit statistikasi faqat oxirgi 200 yozuvdan hisoblanadi | BE, FE |
| SC25 | P2 | CORS namunasi Vite portiga mos emas | BE, FE |
| SC26 | P1 | Toza frontend Docker build uchun API konfiguratsiyasi kafolatlanmagan | FE, DevOps |
| SC27 | P2 | Pul summasining aniqligi qatlamlar orasida yo‘qolishi mumkin | BE, FE |
| SC28 | P2 | Excel eksporti barcha yozuvlarni RAMda yig‘adi | BE |
| SC29 | P2 | Parallel duplicate create tushunarli 409 o‘rniga 500 beradi | BE |
| SC30 | P2 | Filter tozalanganda inputda eski qiymat qoladi | FE |
| SC31 | P2 | Deadline va rol ma’lumotlari ekranda eskirib qoladi | FE, BE |
| SC32 | P2 | PAN/PINFL bo‘yicha minimallashtirish va ko‘rish/eksport nazorati yetishmaydi | BE, FE, security |
| SC33 | P1 | Asosiy jarayonlar testlari va repodagi CI gate yo‘q | BE, FE, QA, DevOps |

## Xavfsizlik va autentifikatsiya topilmalari

### SC01 LDAP xizmat paroli repoda ochiq turibdi

**Dalil:** BE `src/main/resources/application.yml:40–46`. `manager-password` muhit o‘zgaruvchisi berilmasa, source ichidagi aniq paroldan foydalanadi; xizmat akkaunti DN va ichki LDAP manzili ham shu konfiguratsiyada. Parol qiymati bu yerda ataylab chiqarilmagan. Uning hozir faol ekanligi tekshirilmagan.

**Ta’sir:** repository, build JAR yoki imagega kirish huquqi bor shaxs credentialni ko‘rishi mumkin. `LDAP_MANAGER_PASSWORD`ni production’da almashtirib qo‘yish oldingi sirni repository/artifactdan yo‘qotmaydi.

**Tuzatish:** ushbu qiymat real credential bo‘lgan bo‘lsa zudlik bilan rotate/revoke qilish; configdan secret fallbackni olib tashlash; secret manager/environment orqali majburiy berish; Git tarixi va yaratilgan artefaktlarni tashkilotning secret incident tartibida tekshirish. Tarixni qayta yozish alohida kelishilgan operatsiya bo‘lishi kerak.

**Qabul testi:** sir berilmasa production profil ishga tushmasin; secret scan repository va image bo‘yicha o‘tsin; avvalgi credential bekor qilingani infra egasi tomonidan tasdiqlansin.

### SC02 LDAP default ulanishi TLSsiz

**Dalil:** BE `src/main/resources/application.yml:41`, `.env.example:29`, `java/config/LdapConfig.java:20–35`. Default `ldap://...:389`; `LdapContextSource`da StartTLS strategy o‘rnatilmagan.

**Ta’sir va shart:** shu default bilan ishlaganda manager bind va foydalanuvchi bind ma’lumotlari LDAP transportida TLS bilan himoyalanmaydi. Tashqi tunnel yoki production override bor-yo‘qligi noma’lum.

**Tuzatish:** sertifikati tekshiriladigan LDAPS yoki StartTLS; prod profilida oddiy LDAP URLni rad etish; ishonchli truststore. Sertifikat tekshiruvini o‘chirish tuzatish hisoblanmaydi.

**Qabul testi:** noto‘g‘ri/ishonchsiz sertifikatda bind ishlamasin; tasdiqlangan serverga shifrlangan bind muvaffaqiyatli bo‘lsin.

### SC03 Birinchi AD login yangi userni rollback qiladi

**Dalil:** BE `java/service/AuthService.java:29–38,59–65`, `LdapUserProvisioningService.java:18–31`, `java/exception/ApiException.java:8`. Login `@Transactional`; yangi AD user `active=false` qilib shu transactionda saqlanadi. Keyin login unchecked `ApiException` tashlaydi.

**Takrorlash:** DBda yo‘q AD akkaunt bilan to‘g‘ri kirish → user save → active tekshiruvi 401 → transaction rollback. Admin tasdiqlashi kerak bo‘lgan user DBda qolmaydi. Spring transaction interceptor bilan lokal probe rollbackni tasdiqladi; real AD/DBga ulanilmadi. Standart rollback semantikasi [Spring hujjatida](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html) ham bayon qilingan.

**Tuzatish:** pending user provisioningni alohida commit qilinadigan xavfsiz transactionga ajratish yoki administrator boshqaradigan onboarding oqimini yakunlash. Login rad etilgani uchun butun auth service’da barcha xatolar rollbackini o‘chirish kerak emas.

**Qabul testi:** birinchi to‘g‘ri AD login 401/pending qaytarsa ham aynan bitta inactive AD yozuv saqlansin; admin aktivlashtirgandan keyin keyingi login ishlasin; parallel login duplicate user yaratmasin.

### SC04 AD lookup va create API shartnomasi mos emas

**Dalil:** FE `src/shared/api/endpoints.ts:28–35`, `src/features/users/UserFormModal.tsx:80–120`; BE `java/controller/UserController.java:20–43`, `java/dto/UserCreateRequest.java:10–14`, `java/service/UserService.java:26–37`.

Frontend `GET /api/users/ad-lookup` chaqiradi, lekin backendda bunday mapping yo‘q. Frontend AD yaratishda `authSource:'AD'` yuborib parolni yubormaydi. Backend DTO esa `authSource`ni qabul qilmaydi, parolni majburiy qiladi va service lokal user yaratadi. Parolsiz request validatsiyadan o‘tmasligi lokal probe bilan tasdiqlandi.

**Tuzatish:** AD lookup response contractini va AD uchun alohida yaratish/provisioning yo‘lini joriy qilish; server AD identifikatori va mavjudligini o‘zi tekshirsin. LOCAL va AD yaratishni chalkashtirmaslik.

**Qabul testi:** topilgan/topilmagan/allaqachon ro‘yxatdan o‘tgan AD akkauntlari; AD uchun local password talab qilinmasligi; saqlangan `auth_source=AD`; LOCAL oqim regressiyasi yo‘qligi.

### SC07 Bir foydalanuvchining cache ma’lumoti keyingisiga ko‘rinishi mumkin

**Dalil:** FE `src/features/auth/useAuth.ts:19–22`, `authStore.ts:32–35`, `src/shared/api/queryKeys.ts:3–21`, `src/app/App.tsx:7–14`.

**Takrorlash:** ANTI_FRAUD user kreditlarni ko‘radi → logout → shu tabda boshqa rol bilan login. Logout faqat sessionni tozalaydi. QueryClient yashashda davom etadi; keylar user/role bilan ajratilmagan, 30 soniya fresh cache qayta ishlatilishi mumkin. Keyin refetch bo‘lsa ham avvalgi data vaqtincha render qilinishi mumkin. Logout funksiyasining cache tozalamasligi probe bilan tasdiqlandi; browserdagi to‘liq ikki-user ssenariysi hali bajarilmagan.

**Tuzatish:** session chegarasida querylarni cancel qilib cache’ni clear qilish; kechikib keladigan eski requestlar yangi sessionni ifloslamasligi; sensitive query keylarini session/user bilan scope qilish.

**Qabul testi:** bir tabda ANTI_FRAUD → LEGAL va ADMIN → boshqa user almashinuvida eski foydalanuvchining ko‘rishi mumkin bo‘lgan data hatto bir kadr ham chiqmasin; sekin request bilan ham tekshirilsin.

### SC13 Xavfsiz bo‘lmagan defaultlar bootstrapda ishlatiladi

**Dalil:** BE `src/main/resources/application.yml:5–7,27–34`, `java/component/AdminInitializer.java:24–34`, `docker-compose.yml:8–12,30–35`.

Admin, DB va MinIO uchun ma’lum default credentiallar bor. `AdminInitializer` env majburiyligini tekshirmaydi, mavjud ADMIN bo‘lmasa default akkaunt yaratadi. Compose DB va MinIO portlarini host interfeyslariga chiqaradi. Internetga ochiqlik isbotlanmagan; xavf shu konfiguratsiya himoyasiz tarmoqqa chiqarilganda yuzaga keladi.

**Tuzatish:** development va production profillarini ajratish; production’da default credentiallarni rad etish; bir martalik bootstrap va parol almashtirish jarayoni; DB/MinIO portlarini kerakli interfeys yoki yopiq container network bilan cheklash.

**Qabul testi:** production konfiguratsiyasida default yoki yo‘q credential bilan startup fail bo‘lsin; DB va storage uchun faqat tasdiqlangan network yo‘li ochiq bo‘lsin.

### SC14 Parol almashtirish eski tokenni bekor qilmaydi

**Dalil:** BE `java/service/UserService.java:46–55`, `java/security/JwtService.java:28–45`, `JwtAuthenticationFilter.java:33–36`; token muddati default 480 daqiqa.

JWT faqat user ID va muddati bilan tekshiriladi. Password hash yangilanganda token version yoki `passwordChangedAt` bilan solishtirish yo‘q. Lokal probe parol hashidan keyin ham oldingi token parse bo‘lishini tasdiqladi; filter aktyor userni yana qabul qiladi. Userni inactive qilish alohida himoya sifatida ishlaydi, lekin uni qayta active qilganda muddati tugamagan eski token yana yaroqli bo‘ladi.

**Tuzatish:** credential/session version yoki `tokensValidAfter` tekshiruvi; password reset va revoke’da qiymat yangilanishi; logout/revoke siyosatini aniq belgilash.

**Qabul testi:** password resetdan keyin oldingi token har qanday API’da 401 olsin, yangi login tokeni ishlasin.

### SC15 Oxirgi administratorni bloklash mumkin

**Dalil:** BE `java/service/UserService.java:46–55`, `java/component/AdminInitializer.java:25`; FE `src/features/users/UserFormModal.tsx:233–274`.

Admin o‘zini inactive qilishi yoki oxirgi ADMIN rolini almashtirishi mumkin. Active admin qolishi tekshirilmaydi. Inactive ADMIN mavjud bo‘lsa initializer ham yangi admin yaratmaydi. Rol almashtirilgan bo‘lsa restartda bootstrap username unique konflikti ham mumkin.

**Tuzatish:** kamida bir active ADMIN saqlanishini serverda transactional kafolatlash; self-demotion/deactivation uchun aniq qoida va UI tasdiqlovi; favqulodda tiklash runbooki.

**Qabul testi:** bitta adminni inactive/demote qilish tushunarli 409 bilan rad etilsin; ikki admin parallel ravishda bir-birini o‘chirsa ham kamida bittasi active qolsin.

### SC17 Password update bir belgili parolni qabul qiladi

**Dalil:** BE `java/dto/UserUpdateRequest.java:8–12`, `java/service/UserService.java:52–53`. Create’da min 6 bor; update’da faqat max 72. `password:"x"` validatsiyadan o‘tadi — lokal validator probe bilan tasdiqlandi. UI’dagi min 6 server himoyasi emas.

**Tuzatish:** bo‘sh qiymat “o‘zgartirmaslik” bo‘lsa, nonblank yangi parolga bir xil minimal/maximal va tashkilot siyosati qo‘llansin; AD akkaunt uchun local password yozish rad etilsin. BCryptning UTF-8 bayt chegarasi ham mos ravishda tekshirilsin.

**Qabul testi:** bir belgili yangi parol API’dan 400 olsin; bo‘sh/yo‘q password mavjud hashni saqlasin; Unicode chegara qiymatlari tekshirilsin.

### SC18 AD user lookup canonical identifikatorsiz

**Dalil:** BE `java/service/AuthService.java:31,43`, `LdapUserProvisioningService.java:18–30`, `java/repository/UserRepository.java:12`; V1 migratsiyadagi oddiy `username VARCHAR UNIQUE`.

Username trim qilinadi, lekin canonical case yoki AD object ID bilan bog‘lanmaydi. Odatdagi AD case-insensitive bindda `User` va `user` bir akkauntga tegishli bo‘lsa, lokal lookup ularni alohida deb ko‘rishi mumkin. Natija: aktiv user varianti topilmaydi, pending/duplicate jarayoni yuz beradi. Joriy AD konfiguratsiyasida live qayta hosil qilinmagan; SC03 tuzatilgach yanada ko‘rinadi.

**Tuzatish:** AD’dan canonical login va barqaror ID olish; username normalization/unique constraintni kelishish; mavjud kolliziyalarni tekshirib migratsiya qilish.

**Qabul testi:** bir AD akkauntning katta-kichik harf variantlari bitta lokal userga bog‘lansin va rol/active holati bir xil bo‘lsin.

### SC19 Login limitlari va LDAP timeoutlari yo‘q

**Dalil:** BE `java/controller/AuthController.java:20–23`, `java/dto/LoginRequest.java:5`, `java/service/AuthService.java:29–32`, `java/config/LdapConfig.java:20–35`.

Repo ichida login throttling/rate limit va login DTO uzunlik limitlari topilmadi. LDAP connect/read timeoutlari ham berilmagan. Tashqi gateway himoyasi mavjudligi noma’lum. Ko‘p login yoki sekin LDAP servlet threadlarini va login transactionini band qilishi mumkin.

**Tuzatish:** user/IP bo‘yicha boshqariladigan limit, kuzatuv va lockout siyosati; LDAP connect/read timeout; tarmoq bindini keraksiz uzoq DB transactiondan tashqariga chiqarish; LDAP outage’ni oddiy noto‘g‘ri paroldan monitoringda ajratish.

**Qabul testi:** limitdan keyin boshqariladigan javob; LDAP javob bermasa belgilangan vaqtda yakunlanish; sog‘lom boshqa so‘rovlar davom etishi.

### SC32 PAN va PINFL uchun minimallashtirish yetishmaydi

**Dalil:** BE `java/domain/Card.java:22–23`, `java/mapper/CardMapper.java:36–53`, `java/component/CardExcelExporter.java:66–67`; FE `src/shared/ui/formatters.ts:57–59`, `src/features/credits/CreditsListPage.tsx:89`.

Karta raqami entity/DB ustunida to‘liq saqlanadi, list/detail/exportda to‘liq qaytadi. `formatCardNumber` maskalamaydi, faqat bo‘sh joy qo‘yadi. PINFL ham ro‘yxat va eksportda to‘liq. Disk/DB darajasidagi encryption va korporativ siyosat tekshirilmagan; bu band PCI yoki qonunchilikka nomuvofiqlik hukmi emas.

**Tuzatish:** rollar uchun qaysi ekranda qancha ma’lumot kerakligini tasdiqlash; standart listda masking; to‘liq ko‘rish/eksport uchun alohida ruxsat va audit; saqlash hamda backup shifrlashini infra bilan isbotlash; token va to‘liq identifikatorlarni logga chiqarmaslik.

**Qabul testi:** read-only rolda minimal ko‘rinish; full reveal/export faqat ruxsatli rol bilan, audit hodisasi bilan; backup va loglarda nazorat tekshiruvi.

## Kredit jarayoni va ma’lumotlar izchilligi

### SC05 Umumiy kredit PUT status cheklovini chetlab o‘tadi

**Dalil:** BE `java/config/SecurityConfig.java:64–67`, `java/service/CreditService.java:51–58`, `java/service/CreditAccess.java:23–28`, `java/mapper/CreditMapper.java:16–26`.

`PATCH /credits/{id}/status` faqat CREDIT_MANAGEMENT uchun. Biroq `PUT /credits/{id}` o‘z stage’idagi LEGAL/UNDERWRITING/ANTI_FRAUD uchun ham `CreditRequest.status`ni mapper orqali qo‘llaydi. LEGAL bosqichidagi STOPPED kreditni LEGAL user umumiy update bilan ACTIVE qilishi lokal service probe’da tasdiqlandi. Frontend edit tugmasini yashirishi bu API yo‘lini himoya qilmaydi.

**Tuzatish:** create/edit/status DTOlarini ajratish; umumiy update’dan statusni olib tashlash yoki status o‘zgarganda serverda alohida ruxsat tekshirish; qaysi rol qaysi kredit maydonini o‘zgartirishi mumkinligini aniq jadvalga keltirish.

**Qabul testi:** har bir rol va stage uchun PUT/PATCH matrix; LEGAL/UNDERWRITING umumiy PUT orqali statusni o‘zgartira olmasin; vakolatli CREDIT_MANAGEMENT status operatsiyasi ishlasin.

### SC06 Parallel edit advance va delete himoyalanmagan

**Dalil:** BE `java/domain/BaseEntity.java:16–29`, `java/repository/CreditRepository.java:16–28`, `java/service/CreditService.java:51–79`, `CreditDocumentService.java:64–83`, `CardService.java:42–46`.

Entitylarda `@Version`, repositoryda parent row lock yoki stage shartli update yo‘q. Ikki request bir xil eski stage/statusni o‘qishi mumkin. Masalan, T1 eski kreditni tahrirlash uchun o‘qiydi; T2 keyingi stagega o‘tkazib commit qiladi; T1 eski entity state bilan flush qiladi. Lost update yoki stage/deadline ortga yozilishi xavfi bor. Advance “hujjat bor” deb tekshirayotgan vaqtda boshqa transaction oxirgi hujjatni o‘chirishi ham mumkin. Bu ssenariylar source’dan aniqlangan, real PostgreSQL concurrency testi bajarilmagan.

**Tuzatish:** version/ETag yoki shartli update bilan lost update’ni rad etish; advance/upload/delete o‘rtasidagi invariantlar uchun bir xil parent locking strategiyasi; conflictga 409 va qayta yuklash yo‘riqnomasi. Faqat frontend tugmasini disabled qilish yetmaydi.

**Qabul testi:** barrier bilan parallel edit/advance, status/edit, delete/advance testlari; stage ortga ketmasin, bir o‘zgarish ikkinchisini indamay yo‘qotmasin, docsiz stage advance bo‘lmasin.

### SC12 DB transaction MinIO o‘zgarishini qaytara olmaydi

**Dalil:** BE `java/service/CreditDocumentService.java:42–52,64–71,86–101`, `CardDocumentService.java:41–53,63–67,77–93`.

Upload avval MinIOga yozadi, keyin DBga metadata saqlaydi. DB insert/commit yoki keyingi fayl xatosida oldingi objectlar qoladi. Delete esa avval MinIO objectni yo‘q qiladi, keyin DB yozuvini o‘chiradi; DB xatosida metadata qolib fayl yo‘qoladi. Delete tartibi va kompensatsiya yo‘qligi lokal storage/repository test double bilan tasdiqlandi. Fayl nomi 255 belgidan oshishi ham DB xatosiga olib kelishi mumkin; upload oldidan mos uzunlik validatsiyasi yo‘q.

**Tuzatish:** staging va finalize, outbox/delete-pending yoki kompensatsiya hamda retry/reconciliation jarayoni; fayl nomi/metadata validatsiyasini storage’dan oldin bajarish. `@Transactional`ning o‘zi ikki tizimni atomik qilmaydi.

**Qabul testi:** storage write’dan keyin DB failure; uch faylning ikkinchisida xato; delete’dan keyin DB failure; retry/commit failure — barchasida object va metadata holati izchil tiklansin.

### SC16 Qaror va o‘zgarishlarning audit tarixi yo‘q

**Dalil:** BE `java/service/CreditService.java:62–79`, `java/service/CardService.java:42–46`, `java/domain/BaseEntity.java:22–29`, barcha V1–V3 migratsiyalar.

Status va stage joyida yangilanadi. Kim, qachon, qaysi oldingi qiymatdan qaysi keyingi qiymatga o‘tkazgani, sabab va operatsiya identifikatori alohida history’da yo‘q. `updatedAt` hamda hujjat uploaderi bu savollarga yetarli javob bermaydi. Card sender/uploadedBy faqat ism matni bilan saqlanadi, immutable actor ID emas. Bu production kuzatuvchanligi kamchiligi; mavjud tashqi audit collector tekshirilmagan.

**Tuzatish:** append-only audit/event jadvali: entity, actor ID, action, old/new value, vaqt, sabab, correlation ID; muvaffaqiyatsiz ruxsat va eksport/reveal hodisalari uchun nazorat. Token/parol yoki keraksiz to‘liq PAN auditga yozilmasin.

**Qabul testi:** bir kreditning barcha bosqichlari, statuslari, hujjat upload/delete amallari va karta o‘zgarishlari actor bilan tiklab ko‘rsatilishi; oddiy application roli audit yozuvini o‘zgartira olmasligi.

### SC20 Soxta PDF haqiqiy deb qabul qilinadi

**Dalil:** BE `java/service/CreditDocumentService.java:109–117`, `CardDocumentService.java:103–111`.

Tekshiruv extension va dastlabki `%PDF-` baytlari bilan cheklangan. Shu prefiksli, lekin PDF bo‘lmagan sintetik fayl probe’da saqlandi. Kreditda stage hujjati mavjudligi keyingi bosqichga o‘tish uchun asos bo‘ladi, shuning uchun oddiy buzilgan fayl ham jarayon shartini bajargandek ko‘rinadi. Zararli PDF ekspluatatsiyasi namoyish qilinmagan.

**Tuzatish:** resurs limitlari bilan PDF parsing/strukturaviy tekshiruv; malware/quarantine siyosati; faqat muvaffaqiyatli tekshirilgan hujjat advance shartiga hisoblansin. Content type yoki extensionning o‘zi yetarli emas.

**Qabul testi:** faqat prefiks, truncated PDF, katta/buzilgan struktura rad etilsin; haqiqiy tasdiqlangan PDF saqlansin; karantindagi hujjat advance’ga ruxsat bermasin.

### SC27 Pul qiymatlarining kontrakti aniqlikni saqlamaydi

**Dalil:** BE `java/dto/CreditRequest.java:22`, `CardRequest.java:19`, `java/component/CreditExcelExporter.java:62`, `CardExcelExporter.java:72`; FE `src/shared/api/types.ts:72,160`, `src/shared/ui/MoneyInput.tsx:6`, `formatters.ts:3–5`.

Backend 17 ta integer + 2 kasr xonali BigDecimalni qabul qiladi; frontend JSON number/JavaScript number bilan ishlaydi; Excel exporter `doubleValue()` ishlatadi. Bu diapazondagi katta qiymatlarda tiyinigacha aniqlik kafolatlanmaydi. Frontend input chegarasi ham backend maksimumiga teng emas, shuning uchun API’dan yaratilgan yozuv UI’dan to‘liq qayta tahrirlanmasligi mumkin.

**Tuzatish:** bitta biznes maksimumi va aniq decimal kontrakt; zarur bo‘lsa decimal string/decimal library yoki belgilangan minor-unit modeli; Excel’da yuqori aniqlikni yo‘qotmaydigan taqdimot.

**Qabul testi:** kichik kasrlar, maksimal summa va API → UI edit → API → Excel round-tripda qiymat o‘zgarmasin. Faqat ekrandagi format emas, yuborilgan payload ham tekshirilsin.

### SC28 Export hajmi cheklanmagan va RAMga bog‘liq

**Dalil:** BE `java/service/CreditService.java:95–101`, `CardService.java:61–66`, `java/component/CreditExcelExporter.java:30–31`, `CardExcelExporter.java:42–43`, controllerlardagi `byte[]` response.

`findAll` barcha mos entity/DTOlarni oladi; `XSSFWorkbook` butun workbookni, `ByteArrayOutputStream` esa tayyor faylni xotirada saqlaydi. Katta yoki parallel eksport heapni to‘ldirishi va boshqa so‘rovlarni sekinlashtirishi mumkin. Load/OOM testi bajarilmagan.

**Tuzatish:** paged/streaming DB o‘qish, SXSSF yoki async export job, maksimal qator/hajm va parallel job limiti, bekor qilish va timeout. Excel qator chegarasi ham hisobga olinsin.

**Qabul testi:** kutiladigan katta dataset va parallel eksportlarda belgilangan memory/latency chegaralari; limit oshganda tushunarli javob.

### SC29 Parallel duplicate create noto‘g‘ri 500 qaytaradi

**Dalil:** BE `java/service/CreditService.java:40–48`, `UserService.java:27–37`, `ExecutorDirectoryService.java:29–38`, `java/exception/GlobalExceptionHandler.java:32–35`; unique constraintlar V1/V3’da bor.

Ikki parallel request `exists` tekshiruvidan birga o‘tishi mumkin. DB unique constraint ma’lumotni himoya qiladi, lekin `DataIntegrityViolationException` maxsus conflictga o‘girilmagan; umumiy 500 ishlaydi. Bu duplicate DBga kiradi degani emas — muammo API javobi va xavfsiz retry’da.

**Tuzatish:** DB constraintlarni saqlab, ma’lum unique conflictlarni 409 va fieldga tegishli xabarga map qilish; tarmoqdan keyingi noaniq create uchun idempotency qo‘llashni ko‘rib chiqish.

**Qabul testi:** bir xil application number/username/executor bilan parallel request — bittasi muvaffaqiyatli, ikkinchisi 409; logda keraksiz maxfiy request qiymatlari chiqmasin.

## Frontend va API integratsiyasi

### SC08 Balans bo‘sh bo‘lsa karta sahifasi xatoga tushadi

**Dalil:** BE `src/main/resources/application.yml:23–24`, `java/dto/CardResponse.java:20`; FE `src/features/cards/CardsListPage.tsx:128–133`, `CardDetailPage.tsx:73–75`, `src/shared/ui/formatters.ts:3–4`.

Backend `NON_NULL` sabab `balance:null`ni umuman yubormaydi. Frontend `balance === null`ni tekshiradi, lekin `undefined` uchun `formatMoney(undefined)` chaqiradi va `.toFixed`da yiqiladi. Backend serialization hamda frontend funksiya probe’lari bilan ikki tomoni tasdiqlandi.

**Tuzatish:** JSON nullability contractini moslashtirish; nullable maydonlar bo‘sh bo‘lsa explicit null qaytarish yoki FE’da optional sifatida model va nullish guard; barcha nullable fieldlarni tekshirish.

**Qabul testi:** balanssiz karta yaratish → ikkala ro‘yxat/detail/edit ishlasin; `0` balans “bo‘sh” deb chiqmasin; null va omitted property alohida test qilinsin.

### SC09 Kredit pagination tanlangan sahifani o‘chiradi

**Dalil:** FE `src/features/credits/CreditsListPage.tsx:70–78,275–280`.

`onChange` `updateParam('page',...)` chaqiradi. Funksiya `page`ni set qilganidan so‘ng shartsiz `next.delete('page')` qiladi. Natija doim 0-sahifa. Funksiyaning o‘zi lokal probe’da bajarildi: sahifa 2 tanlanganda URL’da page qolmadi.

**Tuzatish:** `page` faqat boshqa filter o‘zgarganda reset qilinsin; CardsListPage’dagi shunga o‘xshash shartdan foydalanish mumkin.

**Qabul testi:** 25+ kredit bilan 1 → 2 → 3 sahifalar turli yozuvlarni ko‘rsatsin; filter o‘zgarsa 1-sahifaga qaytsin; browser back/forward ishlasin.

### SC10 Hujjat upload handlerlari fayllarni qayta yuboradi

**Dalil:** FE `src/features/credits/DocumentsSection.tsx:37–61`, `src/features/cards/CardDocumentsSection.tsx:25–48`. `onChange` har safar butun `info.fileList`ni upload qiladi; ro‘yxat controlled emas va muvaffaqiyatdan keyin tozalanmaydi.

Lockfile’dagi AntD 5.29.3 `onBatchStart` har tanlangan fayl uchun `onChange`ni butun yangi ro‘yxat bilan chaqiradi. Masalan, A+B → ikkita request, har ikkisida A+B; keyin C tanlansa oldingi A/B ham yana ketishi mumkin. Ikkala haqiqiy handler sintetik callback ketma-ketligida duplicate request chiqardi. Kutubxona semantikasi [pinned AntD source](https://github.com/ant-design/ant-design/blob/5.29.3/components/upload/Upload.tsx#L181-L228) bilan tekshirildi.

**Tuzatish:** controlled queue va alohida batch submit yoki har faylga yagona upload; muvaffaqiyatdan keyin queue’ni tozalash; pending paytida qayta yuborishni boshqarish; backend idempotency qo‘shimcha himoya bo‘lishi mumkin.

**Qabul testi:** 2 fayl tanlansa DB/MinIO’da aynan 2 hujjat; keyin uchinchi fayl bilan aynan 3; bir request fail bo‘lib retry qilinganda duplicate bo‘lmasin.

### SC11 Kredit upload type predicate TypeScriptga mos emas

**Dalil:** FE `src/features/credits/DocumentsSection.tsx:55–59`. `.originFileObj` tipi `RcFile | undefined`, lekin predicate `f is File` deb yozilgan. `File` tipi `RcFile`ning majburiy propertylarini kafolatlamaydi. [AntD 5.29.3 interface](https://github.com/ant-design/ant-design/blob/5.29.3/components/upload/interface.ts#L9-L30) tekshirildi.

Lockfile’dagi TypeScript 5.9.3 bilan minimal reproducer `TS2677` va natijadagi massiv uchun `TS2345` chiqardi. To‘liq repository build dependency yetishmagani sabab yakunlanmagan; bu band aynan shu type xatosining isbotidir.

**Tuzatish:** `NonNullable<typeof f>` yoki to‘g‘ri `RcFile` type guard ishlatish; keyingi `mutate` argumenti `File[]`ga mos bo‘lsin. CardDocumentsSection’da allaqachon NonNullable yondashuvi bor.

**Qabul testi:** toza `npm ci && npm run build && npm run lint`; ushbu predicate minimal testi xatosiz o‘tsin.

### SC21 Foydalanuvchi loginini tahrirlash saqlanmaydi

**Dalil:** FE `src/features/users/UserFormModal.tsx:103–111,164–188`, `src/shared/api/types.ts:45–51`; BE `java/dto/UserUpdateRequest.java:8–12`, `java/service/UserService.java:46–55`.

Edit modal loginni tahrirlashga beradi va payloadga username qo‘shadi. Backend update DTO/service bu fieldni qo‘llamaydi. Odatdagi Jackson unknown-property ignore rejimida update muvaffaqiyatli ko‘rinsa ham login o‘zgarmaydi.

**Tuzatish:** username immutable bo‘lsa edit’da read-only va payload’dan chiqarish; o‘zgartirish kerak bo‘lsa unique/AD identity oqimi bilan serverda qo‘llash.

**Qabul testi:** saqlashdan keyin modal va ro‘yxat serverdagi haqiqiy loginni ko‘rsatsin; qo‘llanmaydigan maydonni tahrirlashga yo‘l qo‘yilmasin.

### SC22 So‘rov xatosi bo‘sh ma’lumotdek ko‘rsatiladi

**Dalil:** FE `src/features/credits/CreditDetailPage.tsx:39–49`, `src/features/cards/CardDetailPage.tsx:22–29`, `CreditsListPage.tsx:59,257,265–273`, `CardsListPage.tsx:92,281,287`, `src/features/users/UsersPage.tsx:15,104–108`; ikkala edit formda faqat `isLoading` tekshiriladi.

404/403/500/network failure’da detail `!data` sabab doimiy skeleton chiqaradi. Listlar `data ?? []` bilan “topilmadi” deydi, hatto “muammoli kreditlar topilmadi” success holati chiqishi mumkin. Edit request fail bo‘lsa bo‘sh form render bo‘lib qoladi. Token 401 yo‘li redirect qiladi, lekin qolgan xatolarni bu hal qilmaydi.

**Tuzatish:** loading/error/empty/successni alohida render qilish; retry, not-found, forbidden holatlari; edit formni data muvaffaqiyatli kelmaguncha ochmaslik; route render xatosi uchun foydalanuvchiga tushunarli error boundary.

**Qabul testi:** har sahifa uchun 403, 404, 500, offline va normal empty javob — ularning bir-biridan farqli to‘g‘ri ko‘rinishi.

### SC23 Hujjat va eksport xatolari jim qoladi

**Dalil:** FE `src/features/credits/hooks/useUploadDocuments.ts:12–22`, `useDeleteDocument.ts:7–14`, `useDownloadDocument.ts:5–20`, `useExportCredits.ts:6–21`; cards’dagi shu hooklar va ikkala DocumentsSection.

`.mutate()` ishlatiladi, lekin xatoni ko‘rsatadigan onError/UI yo‘q. Upload onError faqat progressni reset qiladi. Foydalanuvchi server uploadni rad etganini, fayl download bo‘lmaganini yoki delete ishlamaganini tushunmaydi. Blob response’dagi ProblemDetail ham oddiy JSON handlerga to‘g‘ri kelmaydi.

**Tuzatish:** hook yoki umumiy mutation error handler; blob xatosini xavfsiz parse qilish; retry va natija statusi. Delete uchun qaytarib bo‘lmaydigan amal tasdiqlovi yoki recovery yo‘li ham kerak.

**Qabul testi:** 413, storage 500, 403 hamda tarmoq uzilishida foydalanuvchiga aniq xabar; muvaffaqiyat haqida yolg‘on xabar bo‘lmasin; retry duplicate chiqarmasin.

### SC24 Dashboard sonlari 200 yozuv bilan cheklangan

**Dalil:** FE `src/features/dashboard/useDashboardSummary.ts:4–9`, `src/features/credits/CreditsListPage.tsx:63–68,175–200`.

Jami son `totalElements`dan, qolgan sonlar esa faqat birinchi 200 recorddan hisoblanadi. Masalan, 201-yozuv overdue bo‘lsa “muddati o‘tganlar” 0 bo‘ladi. 201-record fixture bilan hisoblash xatosi tasdiqlandi. README bu cheklovni aytadi, lekin UI’da sonlar to‘liq statistikadek berilgan.

**Tuzatish:** backend aggregate summary API, ayni ruxsat/filter doirasi bilan; vaqtinchalik variantda preview ekanini ochiq yozish, biroq operatsion sonlar uchun to‘liq aggregate zarur.

**Qabul testi:** 201+ va ko‘p minglik datasetda UI hisoblagichlari DB aggregate va list filter totaliga mos bo‘lsin.

### SC25 Default CORS Vite portiga mos emas

**Dalil:** BE `.env.example:22`, `src/main/resources/application.yml:52`; FE `vite.config.ts:13–14`, `.env.example:1`.

Frontend `localhost:5173`da ishga tushadi, backend namunasi esa faqat `localhost:3000`ni ruxsat qiladi. README bo‘yicha ishga tushirilganda browser API chaqiruvlarini CORS bloklaydi.

**Tuzatish:** development originlarni moslashtirish yoki Vite proxy; production originlarini alohida va qat’iy belgilash. `*` bilan umumiy ochish yechim emas.

**Qabul testi:** yangi clone’da README buyruqlari bilan login va API ishlasin; begona origin rad etilsin.

### SC26 Frontend Docker image uchun env shartnomasi yetishmaydi

**Dalil:** FE `Dockerfile:1–8`, `src/shared/config/env.ts:1–6`, `nginx.conf:7–9`, `.env.example`.

Toza clone’da faqat `.env.example` bor. Dockerfile VITE envni build argument sifatida olmaydi va runtime config yaratmaydi. `.env`ni oldindan yaratmasdan build qilinganda `apiBaseUrl` undefined bo‘ladi; requestlar frontend originiga ketadi, nginx esa `/api` proxy qilmaydi. `maxUploadSizeMb` ham NaN bo‘lib, FE hajm tekshiruvini amalda ishlatmaydi. README’dagi `.env` tayyorlashga qat’iy rioya qilinsa ba’zi konfiguratsiyalar ishlashi mumkin, lekin image bu shartni tekshirmaydi. Dockerga run paytida env berish allaqachon build qilingan Vite bundle’ni o‘zgartirmaydi.

**Tuzatish:** majburiy env validation, aniq build-time ARG yoki runtime config; same-origin `/api` proxy bo‘lsa nginx mapping; development localhost URL production bundle’ga tasodifan kirmasin. Docker build context uchun `.dockerignore` ham qo‘shilsin; hozir `.git` va lokal keraksiz fayllar build stagega yuboriladi.

**Qabul testi:** clean CI checkoutdan image yaratish; ikki environmentda to‘g‘ri API; env yo‘q bo‘lsa aniq fail; browser Network’da API request HTML SPA fallback emas, JSON olsin.

### SC30 Filterlarni tozalash inputdagi matnni tozalamaydi

**Dalil:** FE `src/features/cards/CardsListPage.tsx:209–225,268–270`, `src/features/credits/CreditsListPage.tsx:207–214`.

Qidiruv va MFO `defaultValue` bilan uncontrolled. “Tozalash” URLni tozalaydi, ammo mounted inputdagi eski qidiruv/MFO qoladi. Browser back/forwardda ham ko‘rinayotgan input bilan real query ajralishi mumkin.

**Tuzatish:** URL bilan sinxron controlled input yoki aniq draft/applied filter modeli; clear va back/forward uchun sinxronizatsiya.

**Qabul testi:** qidiruv → tozalash → browser back/forward; input, URL, request va natija doim bir xil filterni bildirishi.

### SC31 Deadline va session roli jonli yangilanmaydi

**Dalil:** FE `src/app/App.tsx:9–13`, `src/shared/ui/StageTracker.tsx:33–78`, `src/shared/ui/formatters.ts:25–54`, `src/features/auth/authStore.ts:14–29`; BE `java/security/JwtAuthenticationFilter.java:33–45`.

Qolgan vaqt faqat render vaqtida hisoblanadi. Interval/polling yo‘q, window focus refetch o‘chirilgan. `staleTime` avtomatik refresh timer emas. Ochiq ekran deadline o‘tgach eski danger/statusni ushlab turishi mumkin. UI roli localStorage snapshotidan olinadi; admin rolni serverda o‘zgartirganda backend yangi rolni tekshiradi, UI eski menyu va actionlarni saqlaydi.

**Tuzatish:** vaqt hisoblagichi uchun timer, data uchun mos refetch/push; `/auth/me` yoki session refresh bilan serverdagi user/role sinxronlash; ruxsat o‘zgarganda cache scope/tozalash ham SC07 bilan birga bajarilsin.

**Qabul testi:** sahifani ochiq qoldirib deadline chegarasidan o‘tish; boshqa admin rolni o‘zgartirishi; refreshsiz to‘g‘ri holat, action va menyu chiqishi.

## Test va release tayyorligi

### SC33 Asosiy jarayonlar uchun testlar va CI gate yetishmaydi

**Dalil:** BE’da yagona test fayli `src/test/java/uz/agrobank/stopcredit/domain/CreditStageTest.java` — 6 ta enum testi. FE `package.json:6–10`da test script yo‘q, frontend test fayllari topilmadi. Har ikkala repositoryda tekshirilgan tracked fayllar orasida GitHub/GitLab CI konfiguratsiyasi yo‘q. BE Dockerfile package’da `-DskipTests` ishlatadi. Tashqi CI mavjudligi noma’lum.

Enum testlari auth, API contract, transaction, parallel amallar va UI muammolarini ushlamaydi. Auditda yozilgan probelar product regression suite o‘rnini bosmaydi.

**Tuzatish:** clean install/build/lint/unit/integration gate; PostgreSQL + MinIO bilan izchil local/test environment; LDAP test double yoki alohida sandbox; contract test va asosiy browser E2E. Dependency va secret scan alohida gate bo‘lsin. Maven wrapper va Node/toolchain versiyasini pin qilish qayta hosil qilinadigan buildni yaxshilaydi.

**Qabul testi:** aynan fix commit SHA uchun CI link va natija; failure bo‘lsa merge/release bloklanishi; testlarni o‘tkazib yuborib “green” deb hisoblamaslik.

## Talab yoki production dalili bilan aniqlashtiriladigan qo‘shimcha kamchiliklar

Quyidagilar yuqoridagi 33 ta topilmaga qo‘shilgan tasdiqlangan buglar sifatida hisoblanmaydi. Ularni talab egasi yoki infra bilan kelishish kerak.

- **Karta yozuvining unikal kaliti.** V3’da `card_number` oddiy index, unique emas; CardService duplicate tekshirmaydi. Bir PAN uchun ACTIVE va BLOCKED yozuvlar birga bo‘lishi mumkin. Bu event tarixi bo‘lsa tartib/aktual holat modeli, yagona reyestr bo‘lsa unique yoki active-record qoidasi kerak.
- **Haqiqiy bank integratsiyasi.** Tekshirilgan kod statuslarni lokal DBda o‘zgartiradi. Karta processingi/core bankingga block/unblock yoki kreditni real to‘xtatish chaqiruvi topilmadi. Mahsulot faqat ichki reyestr bo‘lsa bu normal; real bank operatsiyasi talab qilinsa hozirgi “BLOCKED/STOPPED” status bajarilgan operatsiya dalili emas.
- **Tasdiqlash vakolatlari.** Bitta ANTI_FRAUD karta statusi va rekvizitlarini o‘zgartira oladi; barcha bo‘lim egalari kreditning umumiy rekvizitlarini PUT orqali o‘zgartira oladi. Maker/checker, branch/MFO bo‘yicha ajratish va qaysi maydon qachon muzlatilishi kerakligi tasdiqlansin.
- **Ish kuni yoki kalendar kuni.** Deadline `Duration.ofDays(3)` — 72 soat; dam olish/bayram kuni hisobga olinmaydi. “3 ish kuni” talabi bo‘lsa alohida calendar kerak.
- **Orqaga qaytarish va rad etish.** Workflow faqat oldinga. Reject/return/reopen, noto‘g‘ri yuborilgan kreditni tuzatish jarayoni yo‘q. Bu oqimlar kerakmi, kim bajaradi — biznes bilan belgilansin.
- **Hujjat recovery va retention.** UI’da delete bir bosishda, storage’dan qattiq o‘chiriladi. Soft delete/versioning, retention va tasodifiy o‘chirishdan tiklash siyosati kelishilsin.
- **Storage/startup readiness.** `MinioFileStorage.ensureBucketExists()` startupda MinIOga bog‘liq va get-then-create ishlatadi. Ko‘p instansiya bir vaqtda birinchi ishga tushsa bucket-create race holatini tekshirish kerak. Tayyorlik/liveness, DB/MinIO backup restore, TLS proxy va monitoring uchun dalil talab qilinadi.
- **UI moslashuvchanligi va klaviatura.** Sidebar `min-width:240px`, asosiy shell’da mobile collapse yo‘q; kredit jadvalida kartalardagidek explicit horizontal scroll yo‘q. Row click va icon-only edit/delete’lar uchun keyboard/accessible name auditi kerak. Real 1366px/768px/390px va keyboard/screen-reader sinovi bajarilmagan.
- **SLA ogohlantirishlari.** `danger` javob vaqtida hisoblanadi; scheduler, email/Telegram yoki boshqa eskalatsiya yo‘li ko‘rinmadi. Operatsion talab bo‘lsa alohida joriy qilinadi.
- **Qo‘shimcha input/kontrakt aniqligi.** MFO formati, restrictionDate va BLOCKED/ACTIVEga bog‘liq majburiy maydonlar, OTHER uchun asos izohi, `dateFrom <= dateTo`, `danger=false` ma’nosi aniqlashtirilsin. Hozir `danger=false` alohida “faqat kechikmagan” filtri emas. Frontend whitespace-only matnni o‘tkazishi, backend esa rad etishi ham izchil UX uchun tuzatilsin.
- **Dependency va container lifecycle.** Lockfile bo‘yicha haqiqiy versiyalarni, image digestlarni, qo‘llab-quvvatlash muddatini va CVE scan natijasini tekshirish kerak. `npm install` o‘rniga CI/Dockerda `npm ci` ishlatish va image pin qilish maqsadga muvofiq. Hozirgi audit biror CVE ekspluatatsiyasini tasdiqlamaydi.
- **Session saqlash siyosati.** Bearer token localStorage’da; XSS yuz bersa script uni o‘qishi mumkin. Bu auditda XSS sink ekspluatatsiyasi topilmadi. CSP va zarur bo‘lsa HttpOnly cookie/BFF kabi session modeli alohida loyihalansin; cookiega o‘tilsa CSRF himoyasi ham birga ko‘rilsin.

## Tuzatishlardan keyingi majburiy qabul ssenariylari

1. LOCAL va AD login: noto‘g‘ri parol, inactive user, birinchi AD login, aktivlashtirish, parallel provisioning, katta-kichik harf, parol reset va token revoke.
2. ADMIN, ANTI_FRAUD, CREDIT_MANAGEMENT, LEGAL, UNDERWRITING, MANAGEMENT rollarining barcha GET/POST/PUT/PATCH/DELETE endpointlari bo‘yicha ruxsat matrixi. Yashirilgan UI emas, HTTP request bilan tekshirilsin.
3. Kreditni ANTI_FRAUD → CREDIT_MANAGEMENT → LEGAL → UNDERWRITING → COMPLETED o‘tkazish; har bosqichdagi PDF talabi; oldingi stage hujjatini o‘chirishni rad etish; ruxsatsiz status update.
4. Parallel edit/advance/status/delete uchun real PostgreSQL transaction testlari; version conflict va invariantlar.
5. PDF: bitta/ko‘p fayl, duplicate callback/retry, katta fayl, soxta/truncated PDF, uzun filename, DB/storage failure, hujjat recovery.
6. Karta minimal maydonlar va bo‘sh balans bilan; nol/katta balans; ACTIVE/BLOCKED listlar; pagination/filter/back/forward.
7. Kamida 201 kredit: barcha pagination sahifalari, overdue/own-stage/completed hisoblagichlari va aggregate mosligi.
8. Logout va boshqa user login: eski cache, kechikkan request, o‘zgargan rol, token muddati tugashi.
9. 400/401/403/404/409/413/500/offline holatlari: list/detail/form/upload/download/delete/exportdagi aniq xabarlar.
10. Toza CI build, yangi DBga V1–V3/fix migratsiyasi, productionga o‘xshash env, CORS/TLS, katta export, restart, backupdan tiklash va rollback rehearsal.

Dasturchining yakuniy topshirig‘ida har bir SC ID uchun fix commit, regression testi, CI natijasi va kerak bo‘lsa sandbox E2E dalili berilsin. “Kod yozildi”, “test o‘tdi”, “deploy bo‘ldi” va “biznes qabul qildi” alohida holatlar sifatida yuritilsin.

## Saqlangan tekshiruv dalillari

- `evidence/backend-maven-test.log` — standart Maven testning dependency sabab to‘xtashi.
- `evidence/backend-probes.log` — mavjud 6 test va 8 qo‘shimcha backend probe natijalari.
- `evidence/frontend-probes.log` — 6 ta frontend funksiya/source-model tekshiruvi.
- `evidence/frontend-type-probe.log` — `TS2677` va `TS2345` chiqishi.
- `evidence/AuditProbe.java.txt`, `evidence/frontend-probes.cjs.txt`, `evidence/upload-type-probe.ts.txt` — sintetik tekshiruv manbalari. Ular product source yoki doimiy test suite’ga qo‘shilmagan.

Web manbalar faqat kutubxona semantikasini tasdiqlash uchun ishlatildi; loyihaning holati haqidagi xulosalar yuqorida ko‘rsatilgan ikki commit source’iga tayangan. Production konfiguratsiyasi va biznesning tasdiqlangan talablari bilan qayta solishtirish qabul jarayonining alohida qismi bo‘lib qoladi.

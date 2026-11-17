# EkoLagja

Web aplikacion për raportimin dhe menaxhimin e mbeturinave në lagjet e Prishtinës.

Qytetarët raportojnë probleme me mbeturinat (deponi ilegale, kontejnerë të mbushur ose të dëmtuar, djegie mbeturinash) me foto dhe lokacion GPS. Kompania e pastrimit i shqyrton raportet, ua cakton ekipeve në terren dhe i përcjell deri në zgjidhje. Banorët marrin njoftime për statusin e raporteve dhe për orarin e grumbullimit, ndërsa menaxhmenti dhe Komuna i përdorin statistikat sipas lagjeve.

Projekt grupor për lëndën Dizajni i Sistemit të Softuerit, Kolegji UBT, 2026.

## Anëtarët e grupit

- Edin Kamberi (menaxher i projektit)
- Ediar Halili
- Erjon Demiri
- Endrit Abdullahu

## Tabela e përgjegjësive

Çdo anëtar është përgjegjës për një modul: e përshkruan në SAD, e dizajnon në DDD dhe e implementon në të gjitha shtresat e kodit.

| Moduli | Anëtari | Përgjegjësia | Paketat / klasat |
|---|---|---|---|
| Raportimi | Edin Kamberi | Dërgimi i raportit me kategori, foto, lokacion GPS dhe përshkrim; raportimi anonim; konfirmimi „edhe unë e kam parë këtë” | Caktohen në DDD |
| Menaxhimi i rasteve | Ediar Halili | Paneli i dispeçerit, caktimi i raporteve te ekipet, statusi i raportit, pamja e ekipit në terren | Caktohen në DDD |
| Orari dhe njoftimet | Erjon Demiri | Orari i grumbullimit sipas lagjes, ndjekja e lagjeve, preferencat dhe dërgimi i njoftimeve me email ose SMS | Caktohen në DDD |
| Analitika dhe harta | Endrit Abdullahu | Harta interaktive, harta e nxehtësisë, statistikat sipas lagjeve dhe kategorive, eksporti | Caktohen në DDD |
| Puna e përbashkët | I gjithë grupi | Modeli konceptual, skeleti i arkitekturës, autentifikimi dhe konfigurimi | Caktohen në DDD |

## Dokumentimi

| Dokumenti | Statusi | Vendndodhja |
|---|---|---|
| Modeli konceptual | Përfunduar | [diagrami](docs/Modeli_Konceptual_EkoLagja.drawio), [figura](docs/Modeli_Konceptual_EkoLagja.png), [përshkrimi](docs/Modeli_Konceptual_EkoLagja.md) |
| Dokumenti i arkitekturës (SAD) | Në punë | `docs/` |
| Dokumenti i dizajnit të detajuar (DDD) | Pa filluar | `docs/` |

## Teknologjitë

- **Backend:** Java 17, Spring Boot, Maven
- **Frontend:** React (Vite)
- **Baza e të dhënave:** H2 gjatë zhvillimit, MySQL në fazën e implementimit
- **Hartat:** Leaflet dhe OpenStreetMap

## Struktura e repozitorit

```
.
├── backend/     Spring Boot (Java, Maven)
├── frontend/    React (Vite)
└── docs/        Modeli konceptual, SAD dhe DDD
```

Kodi aktual është skelet fillestar. Struktura e paketave dhe klasat do të ndryshojnë sipas DDD-së.

## Nisja gjatë zhvillimit

Backend (http://localhost:8080, me konsolën e H2 te `/h2-console`):

```bash
cd backend
./mvnw spring-boot:run
```

Frontend (http://localhost:5173, i dërgon kërkesat `/api` te backend-i):

```bash
cd frontend
npm install
npm run dev
```

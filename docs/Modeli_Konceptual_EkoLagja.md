# Modeli konceptual – EkoLagja

Diagrami: [Modeli_Konceptual_EkoLagja.drawio](Modeli_Konceptual_EkoLagja.drawio) (pamja: [PNG](Modeli_Konceptual_EkoLagja.png))

## Përmbledhje

EkoLagja është web aplikacion për raportimin dhe menaxhimin e mbeturinave në lagjet e Prishtinës. Modeli konceptual paraqet konceptet e fushës së problemit dhe lidhjet mes tyre: kush raporton, çfarë raportohet, ku ndodhet problemi, kush e trajton dhe si njoftohen banorët. Modeli nuk përshkruan klasat e programit, prandaj klasat kanë vetëm atribute dhe nuk kanë operacione.

Koncepti qendror është `Raporti`. Rreth tij lidhen qytetari që e dërgon, lagjja ku ndodhet, dispeçeri që e shqyrton dhe ekipi në terren që e zgjidh.

## Konceptet

| Koncepti | Përshkrimi | Atributet |
|---|---|---|
| `Qytetari` | Banori që raporton probleme dhe merr njoftime. | emri, mbiemri, email, telefoni |
| `Raporti` | Problemi i raportuar me mbeturina, me lokacion GPS dhe status. | kategoria, përshkrimi, gjerësiaGjeografike, gjatësiaGjeografike, dataRaportimit, statusi, ështëAnonim |
| `Fotografia` | Fotoja e bashkëngjitur raportit, para ose pas pastrimit. | lloji, dataNgarkimit |
| `Lagjja` | Zona e qytetit ku ndodhen raportet, kontejnerët dhe orari. | emri, ështëPilot |
| `Kontejneri` | Kontejneri i mbeturinave në një lagje. | kodi, lloji, adresa, gjendja |
| `OrariGrumbullimit` | Dita dhe ora e grumbullimit të mbeturinave në një lagje. | dita, ora, shënim |
| `Ndjekja` | Abonimi i qytetarit në një lagje, me preferencat e njoftimeve. | kanali, njoftoPërOrarin, njoftoPërNdryshime, njoftoPërRaporte |
| `Njoftimi` | Mesazhi që i dërgohet qytetarit me email ose SMS. | lloji, kanali, përmbajtja, dataDërgimit |
| `Dispeçeri` | Punonjësi i kompanisë që shqyrton raportet dhe ua cakton ekipeve. | emri, mbiemri |
| `EkipiNëTerren` | Ekipi që ndërhyn në terren dhe e zgjidh raportin. | emri, numriAnëtarëve |
| `Menaxheri` | Përfaqësuesi i kompanisë ose i Komunës që përcjell statistikat. | emri, mbiemri, institucioni |

Vlerat e lejuara të dy atributeve të `Raporti`:

- `kategoria` (tipi `Kategoria`): deponi ilegale, kontejner i mbushur, kontejner i dëmtuar, djegie mbeturinash.
- `statusi` (tipi `Statusi`): Pranuar, Në proces, Zgjidhur. Raporti i ri fillon me vlerën Pranuar.

## Asociacionet

| Asociacioni | Multipliciteti | Shpjegimi |
|---|---|---|
| `Qytetari` dërgon `Raporti` | 0..1 – * | Raporti mund të jetë anonim, prandaj qytetari është opsional. |
| `Qytetari` konfirmon `Raporti` | * – * | Votimi „edhe unë e kam parë këtë”, që shërben për prioritizim. |
| `Qytetari` merr `Njoftimi` | 1 – * | Çdo njoftim i dërgohet një qytetari. |
| `Njoftimi` lidhet me `Raporti` | * – 0..1 | Njoftimet për ndryshim statusi lidhen me një raport; njoftimet për orarin jo. |
| `Dispeçeri` shqyrton dhe cakton `Raporti` | 0..1 – * | Raporti i ri ende nuk ka dispeçer. |
| `EkipiNëTerren` zgjidh `Raporti` | 0..1 – * | Raporti i pacaktuar ende nuk ka ekip. |
| `Raporti` ndodhet në `Lagjja` | * – 1 | Çdo raport i takon një lagjeje. |
| `Raporti` ka të bëjë me `Kontejneri` | * – 0..1 | Vetëm raportet për kontejnerë lidhen me një kontejner. |
| `Qytetari` ka `Ndjekja` | 1 – * | Qytetari mund të ndjekë më shumë se një lagje. |
| `Ndjekja` për `Lagjja` | * – 1 | Çdo ndjekje vlen për një lagje. |
| `Lagjja` ka `OrariGrumbullimit` | 1 – * | Lagjja mund të ketë disa termine grumbullimi. |
| `Menaxheri` monitoron `Lagjja` | * – * | Statistikat shikohen sipas lagjeve. |

## Agregimet (pjesë–tërësi)

- **`Raporti` përmban `Fotografia`** (1 – 1..*): fotot janë pjesë e raportit. Çdo raport ka të paktën një foto, dhe pas pastrimit i shtohet fotoja „pas”.
- **`Kontejneri` është pjesë e `Lagjja`** (* – 1): lagjja përbëhet, ndër të tjera, nga kontejnerët e saj.

## Lidhja me modulet (propozim)

Grupi ka 4 anëtarë, prandaj sistemi ka 4 module. Kjo ndarje është propozim dhe duhet të miratohet nga grupi:

| Moduli | Konceptet në model |
|---|---|
| Raportimi | `Raporti`, `Fotografia`, asociacioni „konfirmon” |
| Menaxhimi i rasteve | `Dispeçeri`, `EkipiNëTerren`, atributi `statusi` i raportit |
| Orari dhe njoftimet | `OrariGrumbullimit`, `Ndjekja`, `Njoftimi` |
| Analitika dhe harta | `Menaxheri`, `Lagjja`, `Kontejneri` |

`Qytetari` përdoret nga disa module dhe i takon pjesës së përbashkët të grupit.

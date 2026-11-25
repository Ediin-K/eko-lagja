# Fragment i pemës së dobisë — Ndihma fqinjësore

Këta dy skenarë të modulit **Ndihma fqinjësore** integrohen në pemën e dobisë së projektit nga përgjegjësi i arkitekturës së grupit. Vlerësimet janë të propozuara dhe kërkojnë pajtim në grup.

| Atributi | ID | Skenari i matshëm | Rëndësia (H/M/L) | Vështirësia (H/M/L) |
|---|---|---|---|---|
| Performanca | `QS-NF-01` | Me 100 banorë aktivë dhe ≥1,000 shpallje testuese/lagje, listimi i faqosur përgjigjet me `p95 ≤ 400 ms` dhe kufizohet te lagjja e autorizuar | **H** | **M** |
| Besueshmëria | `QS-NF-02` | Dy banorë kërkojnë njëkohësisht të njëjtën shpallje `OPEN`; në 100 prova vetëm një caktim pranohet dhe tjetri kthen `409` | **H** | **H** |

```mermaid
flowchart TD
    U[Utility / Dobishmëria]
    U --> P[Performance]
    U --> R[Reliability]
    P --> Q1[QS-NF-01: p95 <= 400 ms | H/M]
    R --> Q2[QS-NF-02: Një responder | H/H]
```

Për përshkrimet e plota me burim stimulimi, mjedis, artefakt, përgjigje dhe matje, shih [`ndihma-fqinjesore.md`](ndihma-fqinjesore.md#2-dy-skenarët-e-cilësisë-për-pemën-e-dobisë-së-grupit).

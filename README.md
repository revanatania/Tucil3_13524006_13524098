# IcePath Solver - IF2211 Tucil 3

## Penjelasan Singkat Program
IcePath Solver adalah program untuk mencari solusi puzzle lintasan es (ice sliding puzzle).  
Pemain bergerak meluncur sampai menabrak penghalang, dengan tujuan mencapai `goal` setelah melewati checkpoint berurutan jika ada.

Program menyediakan:
- Antarmuka GUI (JavaFX) untuk memuat map, menjalankan solver, playback langkah, dan melihat log pencarian.
- Implementasi algoritma pencarian: UCS, GBFS, dan A*.
- Pilihan heuristik: Manhattan (H1), Euclidean (H2), dan Chebyshev (H3).

## Requirement dan Instalasi
Kebutuhan utama:
- Java Development Kit (JDK) 17 atau lebih baru
- Maven 3.8 atau lebih baru

Catatan penting:
- `pom.xml` saat ini menggunakan `javafx.platform=win`, sehingga konfigurasi default ditujukan untuk Windows.
- Jika ingin menjalankan di Linux/macOS, ubah nilai `javafx.platform` di `pom.xml` sesuai OS.

Cara instalasi singkat:
1. Install JDK 17+.
2. Install Maven 3.8+.
3. Pastikan perintah `java -version` dan `mvn -version` berhasil di terminal.

## Format Input
File input menggunakan `.txt` dengan format:

```text
N M
<N baris grid berisi karakter>
<N baris matriks cost, tiap baris berisi M bilangan bulat>
```

Keterangan simbol grid:
- `*` : lantai es
- `X` : obstacle/dinding
- `L` : lava
- `Z` : posisi awal pemain
- `O` : goal
- `0` - `9` : checkpoint (harus dikunjungi berurutan)

Contoh file tersedia pada folder `test/`.

## Cara Mengompilasi Program
Di root project, jalankan:

```bash
mvn clean compile
```

Untuk membuat file JAR:

```bash
mvn package
```

## Cara Menjalankan dan Menggunakan Program

### Menjalankan GUI (disarankan)
```bash
mvn javafx:run
```

Langkah penggunaan GUI:
1. Buka halaman Solver.
2. Klik `Browse File` lalu pilih file map `.txt`.
3. Pilih algoritma (UCS/GBFS/A*).
4. Pilih heuristik (untuk GBFS/A*).
5. Klik `Run Solver`.
6. Lihat hasil pada panel kanan, dan gunakan playback/log pada panel bawah.
7. Jika solusi ditemukan, simpan hasil dengan `Save Solution` atau log dengan `Save Log`.

### Menjalankan versi CLI (opsional)
Setelah compile:

```bash
java -cp target/classes Main
```

Lalu ikuti instruksi interaktif di terminal (input path file, algoritma, heuristik, playback, simpan output).

### Menjalankan JAR hasil package
Setelah `mvn package`:

```bash
java -jar target/icepath-solver-1.0.0.jar
```

## Struktur Folder Utama
```text
src/
  backend/    -> logika solver dan algoritma
  frontend/   -> GUI JavaFX
test/         -> kumpulan testcase
pom.xml       -> konfigurasi build Maven
```

## Author / Identitas Pembuat
| Nama              | NIM                                      |
|------------------|-------------------------------------------|
| Gabriella Botimada Lubis   | 13524006   |
| Reva Natania Sitohang  | 13524098   |

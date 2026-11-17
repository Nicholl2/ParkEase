# Eparking — Aplikasi Manajemen Parkir

Tugas besar PBO: aplikasi desktop manajemen parkir dengan **Java Swing** dan
**SQLite**. Repositori ini dibangun bertahap; tahap yang sudah selesai adalah
lapisan **model, DAO, dan service** beserta pengujian konsol. Lapisan GUI
menyusul pada tahap berikutnya.

## Kebutuhan

- JDK 22 (proyek di-set ke JavaSE-22)
- Eclipse IDE (opsional, untuk pengembangan)
- Driver `lib/sqlite-jdbc-3.47.1.0.jar` (sudah disertakan)

## Menjalankan

### Lewat terminal

```bash
./scripts/compile.sh        # kompilasi ke folder out/
./scripts/run-console.sh    # kompilasi + jalankan 95 pemeriksaan
```

### Lewat Eclipse

1. `File > Import > General > Existing Projects into Workspace`, pilih folder
   `Eparking`.
2. Pastikan *JRE System Library* menunjuk ke **JavaSE-22** dan
   `lib/sqlite-jdbc-3.47.1.0.jar` ada di *Build Path*.
3. Jalankan `src/parkir/app/MainUji.java` dengan **Run As > Java Application**.

## Akun demo

| Username | Password   | Peran     |
|----------|------------|-----------|
| `admin`  | `admin123` | Petugas   |

Password disimpan sebagai hash PBKDF2-HMAC-SHA256 (120.000 iterasi + salt acak
per pengguna), bukan teks biasa.

## Struktur paket

```
src/parkir/
├── app/          MainUji — harness uji konsol (GUI menyusul)
├── dao/          Database, SlotDAO, PetugasDAO, PengaturanTarifDAO,
│                 TiketDAO, PembayaranDAO, AuditLogDAO
├── exception/    ValidasiException, SlotPenuhException,
│                 AutentikasiGagalException, DataAccessException
├── model/        Kendaraan (abstrak) ← Motor, Mobil
│                 MetodePembayaran ← PembayaranTunai, PembayaranQris
│                 Tiket, RincianBiaya, SlotParkir, Petugas,
│                 PengaturanTarif, AuditLog, Pembayaran, enum status
├── service/      AuthService, ParkirService, SlotService,
│                 PetugasService, TarifService, AuditService
└── util/         Money, Jam, Hash, NomorTiket
```

## Ketentuan tugas yang sudah terpenuhi

| Ketentuan | Implementasi |
|---|---|
| ≥ 8 kelas model/logika | 20+ kelas di `model/` dan `service/` |
| Atribut privat + getter/setter tervalidasi | `Petugas`, `SlotParkir`, `PengaturanTarif`, `Tiket` |
| 1 induk + 2 anak memakai `super()` | `Kendaraan` ← `Motor`, `Mobil` |
| Kelas abstrak + metode abstrak | `Kendaraan.getJenis()`, `getDendaOverstay()`, `getDendaTiketHilang()` |
| Interface dipakai > 1 kelas | `MetodePembayaran` ← `PembayaranTunai`, `PembayaranQris` |
| Overriding lewat referensi induk | `Kendaraan.buat(...)` lalu `hitungTarifParkir(...)`; diuji di `MainUji` |
| Overloading | konstruktor `Tiket`/`Motor`/`Mobil`, `hitungRincian()`, `hitungTotal()` |
| `List`/`Map` bergenerik | `List<Tiket>`, `Map<JenisKendaraan, int[]>` |
| try-catch operasi DB/berkas | seluruh DAO, `ParkirService.unduhBukti` |
| Exception buatan sendiri | 4 kelas di `exception/` |
| CRUD lengkap 2 entitas | `SlotService` dan `PetugasService` (+ tabel nanti di GUI) |

## Aturan bisnis penting

- **Tarif di-snapshot saat kendaraan masuk.** Perubahan tarif hanya berlaku
  untuk tiket baru, sehingga tagihan tiket aktif tidak berubah di tengah jalan.
- **Overstay adalah flag**, bukan status tiket. Durasi di atas
  `batas_jam_reguler` dikenai denda overstay, dan tarif dasar berhenti bertambah.
- **Tiket hilang** memakai jalur pembayaran tersendiri dan menambah denda tiket
  hilang sesuai snapshot.
- **Pembatalan bersifat soft delete**: tiket berstatus `DIBATALKAN`, tidak
  dihapus, agar jejak audit tetap utuh.
- **Slot terisi tidak bisa diubah jenisnya atau dihapus** sebelum dikosongkan.
- **Petugas dengan riwayat transaksi tidak bisa dihapus**, tetapi audit log
  tetap menyimpan nama pelakunya (`petugas_nama`).
- **Satu transaksi = satu koneksi** dengan `autoCommit(false)`, `commit` hanya
  bila semua langkah berhasil, dan `rollback` bila ada kegagalan.

## Basis data dan simulasi waktu

- Berkas basis data default: `data/parkir.db` (dibuat otomatis saat pertama
  jalan, lengkap dengan 120 slot motor, 30 slot mobil, dan 2 pengaturan tarif).
- Folder `data/` diabaikan Git. Bila skema berubah saat pengembangan, hapus
  `data/parkir.db` lalu jalankan ulang aplikasi.
- Semua waktu diambil dari `parkir.util.Jam.sekarang()`. Untuk mendemokan
  overstay tanpa menunggu 8 jam, gunakan `Jam.setOffsetJam(n)` dari panel Mode
  Demo, atau `Jam.setWaktuTetap(...)` pada pengujian.

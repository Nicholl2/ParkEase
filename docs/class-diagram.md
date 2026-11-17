# Class Diagram — Eparking (ParkEase)

Diagram dibuat dengan [Mermaid](https://mermaid.js.org/) agar mudah dirawat:
GitHub merender blok di bawah secara otomatis. Untuk laporan Word/PDF, buka
[mermaid.live](https://mermaid.live), tempel satu blok, lalu ekspor sebagai
PNG atau SVG.

## 1. Model inti (pilar OOP)

Memuat hierarki `Kendaraan`, interface `MetodePembayaran`, serta entitas utama
beserta relasinya.

```mermaid
classDiagram
    direction LR

    class Kendaraan {
        <<abstract>>
        -long tarifJamPertama
        -long tarifJamBerikutnya
        #Kendaraan(long, long)
        +getJenis() JenisKendaraan
        +getDendaOverstay() long
        +getDendaTiketHilang() long
        +getTarifJamPertama() long
        +getTarifJamBerikutnya() long
        +hitungTarifParkir(int, int) long
        +isOverstay(int, int) boolean
        +buat(JenisKendaraan, long, long) Kendaraan
        +buat(JenisKendaraan) Kendaraan
    }

    class Motor {
        +long TARIF_JAM_PERTAMA
        +long TARIF_JAM_BERIKUTNYA
        +long DENDA_OVERSTAY
        +long DENDA_TIKET_HILANG
        +Motor()
        +Motor(long, long)
        +getJenis() JenisKendaraan
        +getDendaOverstay() long
        +getDendaTiketHilang() long
    }

    class Mobil {
        +long TARIF_JAM_PERTAMA
        +long TARIF_JAM_BERIKUTNYA
        +long DENDA_OVERSTAY
        +long DENDA_TIKET_HILANG
        +Mobil()
        +Mobil(long, long)
        +getJenis() JenisKendaraan
        +getDendaOverstay() long
        +getDendaTiketHilang() long
    }

    class MetodePembayaran {
        <<interface>>
        +getKode() String
        +getLabel() String
        +proses(long) String
        +dariKode(String) MetodePembayaran
        +semua() MetodePembayaran
        +validasiJumlah(long) long
    }

    class PembayaranTunai {
        +getKode() String
        +getLabel() String
        +proses(long) String
    }

    class PembayaranQris {
        +getKode() String
        +getLabel() String
        +proses(long) String
    }

    class Petugas {
        -int id
        -String username
        -String nama
        -String passwordHash
        -String salt
        +baru(String, String, String) Petugas
        +setUsername(String)
        +setNama(String)
        +gantiPassword(String)
        +verifikasiPassword(String) boolean
        +getRole() String
    }

    class SlotParkir {
        -int id
        -String nomor
        -JenisKendaraan jenis
        -StatusSlot status
        +tempati()
        +lepaskan()
        +isKosong() boolean
    }

    class Tiket {
        -int id
        -String nomorTiket
        -SlotParkir slot
        -JenisKendaraan jenis
        -LocalDateTime waktuMasuk
        -LocalDateTime waktuKeluar
        -int durasiJam
        -long dendaOverstay
        -long dendaTiketHilang
        -long total
        -StatusTiket status
        -String alasanPembatalan
        -long tarifJamPertamaSnapshot
        -long tarifJamBerikutnyaSnapshot
        -int batasJamRegulerSnapshot
        +hitungRincian() RincianBiaya
        +hitungRincian(boolean) RincianBiaya
        +selesaikan(Petugas) RincianBiaya
        +batalkan(String, Petugas)
        +tandaiTiketHilang()
    }

    class RincianBiaya {
        -int durasiJam
        -long tarifDasar
        -long dendaOverstay
        -long dendaTiketHilang
        -boolean overstay
        -boolean tiketHilang
        +getTotal() long
        +isOverstay() boolean
        +isTiketHilang() boolean
        +ringkasan() String
    }

    class PengaturanTarif {
        -JenisKendaraan jenis
        -long tarifJamPertama
        -long tarifJamBerikutnya
        -int batasJamReguler
        -long dendaOverstay
        -long dendaTiketHilang
        -LocalDateTime berlakuSejak
        +setTarifJamPertama(long)
        +setTarifJamBerikutnya(long)
        +setBatasJamReguler(int)
        +setDendaOverstay(long)
        +setDendaTiketHilang(long)
        +keKendaraan() Kendaraan
    }

    class Pembayaran {
        -int id
        -int tiketId
        -String metode
        -long jumlah
        -LocalDateTime waktu
        -int petugasId
        -String petugasNama
    }

    class AuditLog {
        -int id
        -LocalDateTime waktu
        -int petugasId
        -String petugasNama
        -String aksi
        -String entitas
        -String entitasId
        -String detail
    }

    class JenisKendaraan {
        <<enumeration>>
        MOTOR
        MOBIL
    }

    class StatusTiket {
        <<enumeration>>
        AKTIF
        SELESAI
        DIBATALKAN
    }

    class StatusSlot {
        <<enumeration>>
        KOSONG
        TERISI
    }

    Kendaraan <|-- Motor
    Kendaraan <|-- Mobil
    MetodePembayaran <|.. PembayaranTunai
    MetodePembayaran <|.. PembayaranQris

    PengaturanTarif ..> Kendaraan : keKendaraan()
    Tiket --> SlotParkir
    Tiket --> JenisKendaraan
    Tiket --> StatusTiket
    Tiket ..> RincianBiaya : hitungRincian()
    Tiket --> Petugas : petugasMasuk / petugasSelesai
    Tiket "1" --> "0..1" Pembayaran
    Pembayaran --> Petugas
    SlotParkir --> JenisKendaraan
    SlotParkir --> StatusSlot
    PengaturanTarif --> JenisKendaraan
    Petugas "1" --> "*" AuditLog : pelaku
```

## 2. Arsitektur aplikasi (UI → Service → DAO → SQLite)

Menunjukkan pemisahan tampilan, logika bisnis, dan akses data, serta navigasi
`CardLayout` pada `MainFrame`.

```mermaid
classDiagram
    direction LR

    class LoginFrame {
        -AuthService auth
        +LoginFrame()
        -masuk()
    }

    class MainFrame {
        -Petugas petugas
        -JPanel isi
        +tampilkan(String)
        +getPetugas() Petugas
        +segarkanWaktu()
    }

    class MuatUlang {
        <<interface>>
        +muatUlang()
    }

    class DashboardPanel
    class MasukPanel
    class KeluarPanel
    class RiwayatPanel
    class SlotPanel
    class PetugasPanel
    class TarifPanel
    class ModeDemoPanel

    class AuthService {
        +login(String, String) Petugas
    }

    class ParkirService {
        +masuk(JenisKendaraan, String, Petugas) Tiket
        +keluar(String, MetodePembayaran, Petugas) Pembayaran
        +tiketHilang(String, MetodePembayaran, Petugas) Pembayaran
        +batalkan(String, String, Petugas) Tiket
        +hitungTotal(String, boolean) RincianBiaya
        +daftarAktif() List~Tiket~
        +cariRiwayat(String, StatusTiket, JenisKendaraan) List~Tiket~
        +kapasitas() Map
        +unduhBukti(Tiket, Path) Path
    }

    class SlotService {
        +semua() List~SlotParkir~
        +kosong(JenisKendaraan) List~SlotParkir~
        +tambah(String, JenisKendaraan, Petugas) SlotParkir
        +ubah(int, String, JenisKendaraan, Petugas)
        +hapus(int, Petugas)
    }

    class PetugasService {
        +semua() List~Petugas~
        +tambah(String, String, String, Petugas) Petugas
        +ubah(int, String, String, Petugas)
        +resetPassword(int, String, Petugas)
        +hapus(int, Petugas)
    }

    class TarifService {
        +ambil(JenisKendaraan) PengaturanTarif
        +simpan(PengaturanTarif, Petugas)
    }

    class AuditService {
        +catat(Connection, Petugas, String, String, String, String)
        +terbaru(int) List~AuditLog~
    }

    class SlotDAO
    class PetugasDAO
    class TiketDAO
    class PembayaranDAO
    class PengaturanTarifDAO
    class AuditLogDAO

    class Database {
        +koneksi() Connection
        +inisialisasi()
    }

    class Jam {
        +sekarang() LocalDateTime
        +setOffsetJam(int)
        +getOffsetJam() int
    }

    LoginFrame ..> MainFrame : setelah login
    MainFrame *-- DashboardPanel
    MainFrame *-- MasukPanel
    MainFrame *-- KeluarPanel
    MainFrame *-- RiwayatPanel
    MainFrame *-- SlotPanel
    MainFrame *-- PetugasPanel
    MainFrame *-- TarifPanel
    MainFrame *-- ModeDemoPanel

    MuatUlang <|.. DashboardPanel
    MuatUlang <|.. MasukPanel
    MuatUlang <|.. KeluarPanel
    MuatUlang <|.. RiwayatPanel
    MuatUlang <|.. SlotPanel
    MuatUlang <|.. PetugasPanel
    MuatUlang <|.. TarifPanel
    MuatUlang <|.. ModeDemoPanel

    LoginFrame --> AuthService
    MainFrame --> ParkirService
    MainFrame --> SlotService
    MainFrame --> PetugasService
    MainFrame --> TarifService
    MainFrame --> AuditService

    DashboardPanel ..> ParkirService
    MasukPanel ..> ParkirService
    KeluarPanel ..> ParkirService
    RiwayatPanel ..> ParkirService
    SlotPanel ..> SlotService
    PetugasPanel ..> PetugasService
    TarifPanel ..> TarifService
    ModeDemoPanel ..> Jam

    AuthService --> PetugasDAO
    ParkirService --> TiketDAO
    ParkirService --> SlotDAO
    ParkirService --> PembayaranDAO
    ParkirService --> PengaturanTarifDAO
    ParkirService --> AuditLogDAO
    SlotService --> SlotDAO
    PetugasService --> PetugasDAO
    TarifService --> PengaturanTarifDAO
    AuditService --> AuditLogDAO

    SlotDAO ..> Database
    PetugasDAO ..> Database
    TiketDAO ..> Database
    PembayaranDAO ..> Database
    PengaturanTarifDAO ..> Database
    AuditLogDAO ..> Database
```

## 3. Exception handling

```mermaid
classDiagram
    Exception <|-- ValidasiException
    Exception <|-- AutentikasiGagalException
    RuntimeException <|-- DataAccessException
    ValidasiException <|-- SlotPenuhException
```

## Catatan

- Diagram 1 memuat kelas model/logika (di luar kelas tampilan) dan menonjolkan
  pewarisan, abstract class, interface, serta relasi antarentitas.
- Diagram 2 menunjukkan alur dependensi dari tampilan ke service, DAO, sampai
  basis data SQLite lokal (`data/parkir.db`).
- Getter/setter rutin dan konstanta kecil tidak semuanya ditampilkan agar
  diagram tetap terbaca; method penting dan relasi antarkelas tetap lengkap.

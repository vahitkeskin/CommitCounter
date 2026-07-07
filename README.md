# CommitCounter - IntelliJ Platform Plugin

CommitCounter, geliştiricilerin günlük GitHub commit sayılarını IDE durum çubuğunda (StatusBar) anlık ve şık bir şekilde takip etmelerini sağlayan modern bir IntelliJ platform eklentisidir. Clean Architecture (Temiz Mimari) prensiplerine ve SOLID standartlarına uygun olarak Kotlin dilinde geliştirilmiştir.

---

## ✨ Özellikler

- 🟢 **Durum Çubuğu Entegrasyonu (StatusBarWidget):** IDE'nin sağ alt köşesinde eklentinin durumunu canlı olarak gösterir:
  - Giriş yapılmadıysa: `GitHub: Click to Login`
  - Veri güncellenirken: `GitHub: Fetching...`
  - Giriş yapıldıysa: `Commits Today: [Sayı]`
  - Hata durumunda: `GitHub: Error`
- 🔑 **GitHub OAuth Device Flow:** Tarayıcı üzerinden güvenli ve şifresiz giriş yapma imkanı. Eklenti, otomatik olarak doğrulama kodunu panoya kopyalar ve tarayıcıyı açar.
- 🔒 **Güvenli Veri Saklama (PasswordSafe):** Kullanıcının GitHub OAuth Access Token'ı JetBrains'in yerleşik güvenli şifre kasası `PasswordSafe` (CredentialStore) API'si kullanılarak şifrelenip saklanır.
- 🕒 **Akıllı Senkronizasyon (Scheduler):** `ScheduledExecutorService` ile her 15 dakikada bir otomatik senkronizasyon yaparak commit sayılarını arka planda günceller.
- ⚡ **Asenkron İşlemler:** Tüm ağ istekleri ve dosya yazma/okuma işlemleri UI thread'ini kilitlemeyecek şekilde arka plan thread'lerinde (Background Workers) çalışır.
- 🖱️ **Etkileşimli Menü:** Durum çubuğuna tıklandığında açılan menü üzerinden manuel yenileme (Refresh), güvenli çıkış yapma (Logout) ya da işlemi iptal etme seçenekleri sunar.

---

## 🏗️ Mimari Yapı (Clean Architecture)

Proje, Clean Architecture prensiplerine sadık kalınarak üç temel katmana ayrılmıştır:

```
src/main/kotlin/com/vahitkeskin/commitcounter/
├── data/
│   └── repository/
│       ├── GitHubRepository.kt       # GitHub API istekleri (Device Flow OAuth & Search API)
│       └── PasswordSafeStorage.kt    # Token ve kullanıcı verilerinin güvenli saklanması
├── domain/
│   ├── model/
│   │   └── CommitState.kt            # Eklentinin UI durum modelleri (LoggedOut, Fetching, vb.)
│   └── usecase/
│       └── CommitCounterService.kt   # Durum yönetimi ve arka plan zamanlayıcısı (App Service)
└── presentation/
    └── widget/
        ├── CommitCounterWidget.kt    # Durum çubuğu widget arayüzü ve olay yönetimi
        ├── CommitCounterWidgetFactory.kt # Widget'ın IntelliJ platformuna kayıt fabrikası
        └── VerificationDialog.kt     # OAuth cihaz doğrulama kodunu gösteren diyalog
```

---

## 🛠️ Kurulum ve Çalıştırma

### Gereksinimler
- **JDK:** Version 21
- **Gradle:** Sürüm uyumluluğu proje içinde otomatik yapılandırılmıştır (`gradlew`).

### 1. Klonlama ve Çalıştırma
Projeyi klonladıktan sonra terminalden aşağıdaki komutla test ortamını (sandbox IDE) başlatabilirsiniz:
```bash
./gradlew runIde
```

### 2. GitHub OAuth Yapılandırması
Eklentinin çalışabilmesi için bir **GitHub OAuth App** tanımlanmış olmalıdır:
1. GitHub hesabınızda **Settings > Developer Settings > OAuth Apps** menüsüne gidin.
2. Yeni bir uygulama oluşturun (Device Flow desteği aktif olmalıdır).
3. Uygulamanın **Client ID** değerini [GitHubRepository.kt](src/main/kotlin/com/vahitkeskin/commitcounter/data/repository/GitHubRepository.kt) dosyasındaki `CLIENT_ID` sabiti ile değiştirin.

---

## ⚙️ Lisans ve Katkıda Bulunma

Bu proje öğrenme ve geliştirme amaçlı açık kaynaklı olarak paylaşılmıştır. Her türlü hata bildirimi ve özellik önerisi için Pull Request gönderebilir veya Issue açabilirsiniz.

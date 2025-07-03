# 🏢 System Zarządzania Magazynem

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template%20Engine-green)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-purple)

Kompleksowe rozwiązanie do zarządzania magazynem, umożliwiające śledzenie produktów, zarządzanie zapasami, obsługę rezerwacji oraz generowanie raportów.

## 📋 Spis Treści

- [Przegląd](#przegląd)
- [Funkcje](#funkcje)
- [Wymagania Systemowe](#wymagania-systemowe)
- [Technologie](#technologie)
- [Instalacja i Konfiguracja](#instalacja-i-konfiguracja)
- [Struktura Projektu](#struktura-projektu)
- [Role Użytkowników i Uprawnienia](#role-użytkowników-i-uprawnienia)
- [Kluczowe Funkcjonalności](#kluczowe-funkcjonalności)
- [Dokumentacja API](#dokumentacja-api)
- [Struktura Bazy Danych](#struktura-bazy-danych)
- [Raporty](#raporty)
- [Zrzuty Ekranu](#zrzuty-ekranu)
- [Współpraca](#współpraca)
- [Licencja](#licencja)

## 🔍 Przegląd

System Zarządzania Magazynem został zaprojektowany, aby usprawnić operacje magazynowe poprzez dostarczenie przyjaznego dla użytkownika interfejsu do zarządzania produktami, strefami, rezerwacjami oraz generowania szczegółowych raportów. System implementuje kontrolę dostępu opartą na rolach, aby zapewnić, że użytkownicy mają dostęp tylko do funkcjonalności odpowiednich dla ich stanowiska.

## ✨ Funkcje

- **Zarządzanie Produktami**: Dodawanie, edycja, usuwanie i przeglądanie produktów ze szczegółowymi informacjami
- **Zarządzanie Strefami**: Tworzenie stref magazynowych z monitorowaniem pojemności
- **System Rezerwacji**: Rezerwacja produktów dla klientów lub użytku wewnętrznego
- **Zarządzanie Użytkownikami**: Kontrola dostępu oparta na rolach z różnymi poziomami uprawnień
- **Śledzenie Historii**: Rejestrowanie wszystkich działań magazynowych do celów audytu
- **Raportowanie**: Generowanie raportów PDF z rezerwacji i stanu zapasów
- **Responsywny Interfejs**: Nowoczesny interfejs zbudowany z wykorzystaniem Bootstrap, działający na wszystkich urządzeniach

## 💻 Wymagania Systemowe

- Java 17 lub nowsza
- Baza danych MySQL
- Maven do zarządzania zależnościami
- Minimum 2GB RAM
- 500MB przestrzeni dyskowej

## 🛠️ Technologie

### Backend
- **Język**: Java 17
- **Framework**: Spring Boot 3.3.4
- **Baza danych**: MySQL
- **Bezpieczeństwo**: Spring Security
- **ORM**: Spring Data JPA / Hibernate
- **Generowanie PDF**: iText 7

### Frontend
- **Silnik szablonów**: Thymeleaf
- **Framework UI**: Bootstrap 5
- **JavaScript**: Vanilla JS
- **Ikony**: Bootstrap Icons

## 🚀 Instalacja i Konfiguracja

### Konfiguracja bazy danych
1. Utwórz bazę danych MySQL o nazwie `magazyn`
2. Skonfiguruj połączenie z bazą danych w pliku `application.properties`
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/magazyn
   spring.datasource.username=twoja_nazwa_użytkownika
   spring.datasource.password=twoje_hasło
   ```

### Uruchamianie aplikacji
1. Sklonuj repozytorium
   ```bash
   git clone "link do repozytorium"
   ```
2. Przejdź do katalogu projektu
   ```bash
   cd warehouse-management
   ```
3. Zbuduj projekt za pomocą Mavena
   ```bash
   mvn clean install
   ```
4. Uruchom aplikację
   ```bash
   mvn spring-boot:run
   ```
5. Dostęp do aplikacji pod adresem http://localhost:8080

### Domyślne dane logowania administratora
Użytkownik stworzony jako pierwszy, automatycznie jest administratorem.

## 🗂️ Struktura Projektu

```
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       └── example/
│   │           └── magazyn/
│   │               ├── config/         # Konfiguracja aplikacji
│   │               ├── controller/     # Kontrolery MVC
│   │               ├── dto/            # Obiekty transferu danych
│   │               ├── entity/         # Encje JPA
│   │               ├── repository/     # Repozytoria danych
│   │               ├── security/       # Konfiguracje bezpieczeństwa
│   │               ├── service/        # Logika biznesowa
│   │               └── MagazynApplication.java
│   └── resources/
│       ├── static/          # Zasoby statyczne (CSS, JS)
│       ├── templates/       # Szablony Thymeleaf
│       └── application.properties
```

## 👥 Role Użytkowników i Uprawnienia

- **USER**: Podstawowy dostęp do przeglądania produktów
- **WAREHOUSEMAN** (Magazynier): Zarządzanie produktami, strefami i obsługa rezerwacji
- **MANAGER** (Kierownik): Dodatkowy dostęp do raportów i zarządzania użytkownikami
- **ADMIN** (Administrator): Pełny dostęp do systemu, w tym zarządzanie rolami

## 🔑 Kluczowe Funkcjonalności

### Zarządzanie Produktami
- Tworzenie, aktualizacja i usuwanie produktów
- Przypisywanie produktów do stref magazynowych
- Przesyłanie zdjęć produktów
- Śledzenie ilości i ceny produktów

### Zarządzanie Strefami
- Tworzenie stref magazynowych z limitami wagi
- Monitorowanie aktualnej wagi i pozostałej pojemności
- Przypisywanie/usuwanie produktów ze stref

### System Rezerwacji
- Tworzenie rezerwacji produktów
- Zmiana statusu rezerwacji (AKTYWNA, ZAKOŃCZONA, ANULOWANA)
- Generowanie raportów rezerwacji

### Raportowanie
- Generowanie raportów PDF dla rezerwacji
- Eksport danych historycznych

## 📊 Struktura Bazy Danych

### Główne Encje:
- **Product** (Produkt): Przechowuje informacje o produkcie
- **Zone** (Strefa): Reprezentuje obszary magazynowe
- **User** (Użytkownik): Informacje o kontach użytkowników
- **Role** (Rola): Role użytkowników do kontroli dostępu
- **Reservation** (Rezerwacja): Szczegóły rezerwacji produktów
- **History** (Historia): Logi aktywności

### Relacje między Encjami:
- Produkty są przechowywane w Strefach (wiele do jednego)
- Produkty mogą być rezerwowane (jeden do wielu)
- Użytkownicy mogą tworzyć Rezerwacje (jeden do wielu)
- Użytkownicy mogą mieć wiele Ról (wiele do wielu)

## 📄 Raporty

System może generować różne raporty PDF:
- Raporty rezerwacji ze szczegółami dotyczącymi produktów i klientów
- Raporty zajętości magazynu
- Historia ruchu produktów

Raporty są automatycznie zapisywane w katalogu `reports`.

## 🖼️ Zrzuty Ekranu



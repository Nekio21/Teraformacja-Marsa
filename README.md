# 🚀 Teraformacja Marsa: Strategiczna Gra Online

Fanowska implementacja inspirowana popularną grą planszową *Terraformacja Marsa* — stworzona jako aplikacja webowa w Spring Boot. Gra pozwala na logowanie, rejestrację, dołączanie do rozgrywek i wspólną grę w przeglądarce.

---

## 🎮 O grze

Gracze wcielają się w korporacje terraformujące Marsa, zwiększając temperaturę, poziom tlenu i budując oceany. Zagrywają karty projektów, rywalizując o punkty zwycięstwa.

Ta wersja została zaprogramowana od zera, łącznie z własnym backendem, frontendem i logiką gry. To w pełni działająca, przeglądarkowa gra z potencjałem do dalszego rozwoju.

---

## 🛠 Technologie

- **Java 17 / Spring Boot**
- **PostgreSQL** (baza danych)
- **RabbitMQ** (komunikacja między graczami)
- **HTML + CSS + JavaScript**
- **Docker + Docker Compose**

### ▶️ Uruchomienie krok po kroku

1. **Pobierz projekt**:

    Skopiuj repozytorium na swoje lokalne środowisko:

    ```bash
    git clone https://github.com/Nekio21/Teraformacja-Marsa.git
    cd Teraformacja-Marsa
    ```

2. **Uruchom Docker Compose**:

    W katalogu głównym projektu, uruchom poniższą komendę, aby zbudować i uruchomić kontenery:

    ```bash
    docker-compose up --build
    ```

3. **Dostęp do aplikacji**:

    Po uruchomieniu aplikacji, będzie dostępna pod adresem:

    ```
    http://localhost:8080
    ```

4. **Zatrzymywanie kontenerów**:

    Aby zatrzymać kontenery, użyj:

    ```bash
    docker-compose down
    ```

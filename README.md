# ReadMe

An Android library app where members browse the catalog, search it, check out available books and return them. It is a fully simulated demo: no backend, no payment gateway, no email or SMS and no real push notifications.

**Core flow:** Sign up / Log in → Browse or search the catalog → Book details → Check out → Return from Profile

## About

ReadMe is a practice project based on the STI College Balayan Codefest Training AY 2023-2024, Problem #3: Library App. The whole app works offline: the catalog and book details are seeded into SQLite and never fetched.

## Features

**Registration and authentication**
- Sign up with name, email, password and confirm password. Email must be unique and valid, password is 6+ characters
- Log in by email and password, and reset a forgotten password by email and a new password (no email is sent)
- Errors show inline on the field that is wrong

**Book catalog**
- 15 seeded books across 5 genres: Fiction, Fantasy, Science, History and Technology
- Each row shows title, author, genre and whether the book is Available or Borrowed, sorted by title
- Tap a row to open Book details

**Search**
- A search field above the list re-queries on every keystroke, with no search button
- Matches title, author and genre, ignoring case. No matches shows an empty view

**Book details**
- Title, author, genre, summary, publication date and availability
- One button in the bottom bar: **Check out** for an available book, a disabled **Unavailable** when someone else has it, and **Return** when you hold it

**Checkout and return**
- The status is re-read from the database when you tap Check out. If the book was taken meanwhile you get a message and the screen refreshes
- A confirm dialog shows the due date (14 days from today) before the loan is created
- Checkout and return each update the book and its loan in one database transaction
- No fines, no renewals and no borrowing limit
- Return from Book details, or from the Borrowed tab on Profile

**Profile**
- Shows your name and email, with Edit name (in a bottom sheet) and Log out
- **Borrowed** tab: books not yet returned, with due date. Tap a row to return it
- **Returned** tab: history with borrowed and returned dates

## Tech specs

| | |
|---|---|
| Language | Java 11 |
| UI | XML layouts, Material Design 3 (Material Components 1.14), view binding |
| Min / target SDK | 24 (Android 7.0) / 36 |
| Build | Android Gradle Plugin 9.2.1, Gradle 9.4.1 |
| Storage | Local SQLite for users, books and loans |
| Theme | Material Theme Builder palette with light and dark mode |
| Navigation | One Activity per screen, no fragments |
| Libraries | AndroidX AppCompat, Activity, ConstraintLayout and Material Components only |

## Requirements

- Android Studio (a recent version that supports AGP 9.x) and JDK 11 or newer
- An Android emulator or device running Android 7.0 (API 24) or higher

## Getting started

```bash
git clone https://github.com/mowtiie/ReadMe.git
cd ReadMe
./gradlew assembleDebug
```

Then open the project in Android Studio and press **Run** on an emulator. The debug APK is created at `app/build/outputs/apk/debug/`.

## Demo accounts

Both accounts use the password `demo1234`.

| User | Email | Books |
|---|---|---|
| Demo Member | `member@demo.com` | None borrowed |
| Alex Reyes | `alex@demo.com` | The Hobbit, A Brief History of Time, Clean Code |

Log in as the Demo Member and open a book to check it out, then return it from Details or from Profile. Alex's three books show as Borrowed in the catalog and cannot be checked out. The demo data is seeded when the database is first created, so no screen starts out empty.

## What is simulated

- Sign up, login, logout, profile edit and password reset are fake but validated.
- Due dates are shown for information only. Nothing happens when a loan is overdue.
- Messages are Toasts, Snackbars and lists inside the app. There are no real Android notifications.
- Data is saved in local SQLite and persists across restarts. Only the logged-in user is kept in memory, so closing the app logs you out.

## Release build

```bash
./gradlew assembleRelease
```

Release signing is not configured in the project yet. Keystore files and `keystore.properties` must never be committed.

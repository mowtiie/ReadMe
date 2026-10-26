# CLAUDE.md

## Project

ReadMe: an Android library app for browsing a library's book catalog, searching it, checking out available books and returning them, for library members.

Core flow: **Sign up / Login → Browse or search catalog → Book details → Check out → Return from Profile**

This is a **simulation**. There is no backend, no payment gateway, no email/SMS, no real push notifications. Everything must look and behave correctly on screen during a demo. All app data is stored in local SQLite so it persists across launches. The whole app works offline by design: the catalog and book details are seeded into SQLite and never fetched.

Source of requirements: the spec the user provides (STI College Balayan Codefest Training AY 2023-2024, Problem #3: Library App; the app itself is named ReadMe). Read it fully before building. Where the spec is vague, pick the simplest simulated behavior and note it here.

Status: build steps 1-5 done; the core flow was run on the Pixel_10 emulator (login, catalog, details, checkout, return, edit, logout). Last verified build: `./gradlew assembleDebug` succeeded.

## Prime directive: build the fastest way possible

- Speed beats elegance. Pick the shortest path that works in a live demo.
- **No structures.** No architecture patterns (MVVM, MVP, repositories, use cases, DI), no layers, no feature packages, no interfaces or abstract classes, no custom model/POJO classes. Exception: a single shared `GenericAdapter<T>` with a nested `Binder` interface.
- One Activity per screen, one shared `Data.java` holding the constants, the `USER_*` fields and static helper methods that read and write the database.
- Read rows from a `Cursor` into `String[]` or `ArrayList<String>`. No custom model/POJO classes.
- Hardcode seed data and insert it once, when the database is first created. Never fetch or sync anything. All records live in the SQLite tables listed under Database and persist across launches. Only the logged-in user (`Data.USER_*`) lives in memory.
- Duplicate code freely if it is faster than abstracting it.
- No tests, no logging frameworks, no error handling beyond field validation.
- Do not add libraries or touch `build.gradle` dependencies. Enabling `buildFeatures { viewBinding = true }` is allowed.

## Build order

Build in this order. The core flow must work end to end before anything is polished.

1. `DbHelper` with tables and seed, `Data.java`, `GenericAdapter`, Sign up, Login, Forgot password.
2. Catalog with live search, Book details.
3. Checkout, Profile with borrowed and returned lists, Return.
4. Profile edit, logout, empty states.
5. UI polish, then run the whole flow on an emulator and fix what turns up.

## Stack

- Java, XML layouts, AppCompat and Material 3, single `app` module. minSdk 24, targetSdk 36.
- No Fragments, no Navigation component, no Jetpack Compose. No permissions, no internet.
- Views via view binding. Use the generated `*Binding` classes, never `findViewById`.
- Lists: `RecyclerView` with the one shared `GenericAdapter<T>` and shared row layout `item_row.xml`. Never write a per-screen adapter.
- Database: pure SQLite (`SQLiteOpenHelper`, `ContentValues`, `Cursor`). No Room, no ORM, no DAO layer.
- Dropdowns: `TextInputLayout` (ExposedDropdownMenu style) with `MaterialAutoCompleteTextView`. Never `Spinner`.
- Messages: `Snackbar` for validation and errors on a screen that stays open. `Toast` only for simulated notifications and messages shown as a screen closes. `MaterialAlertDialogBuilder` (never plain `AlertDialog.Builder`) for dialogs, `BottomSheetDialog` for add/edit sheets.

## Code layout

Package `com.mowtiie.readme`, three flat folders. Do not nest them or add per-feature packages.

- `activities/`: every Activity.
- `data/`: `Data.java` (constants, `USER_*`, database helper methods) and `DbHelper.java` (tables and seed).
- `adapters/`: `GenericAdapter.java`.

Navigation is `startActivity(new Intent(...))` with `putExtra`. After a flow finishes, start the final screen with `NEW_TASK | CLEAR_TASK`. Logout clears the task and returns to Login.

## Database

- Every record the app creates or edits is a row in a SQLite table, written immediately and reloaded from the database in `onResume`.
- `DbHelper` has a `DATABASE_VERSION` constant. `onCreate` creates every table and then calls `seedDemo`. `onUpgrade` drops all tables and calls `onCreate`. Bump the version whenever a table changes during development.
- Tables: `users` (id, email, password, name), `books` (id, title, author, genre, summary, published, status), `loans` (id, user_id, book_id, borrowed_on, due_on, returned_on).
- Enum-like values are stored as the text of their `ALL_CAPS` constants (`STATUS_AVAILABLE`, `STATUS_BORROWED`). Dates are stored as the display text from the picker or formatter. `returned_on` is an empty string while the book is still borrowed.
- A book's availability is its `status` column. Checkout and return update `books.status` and the `loans` row in one transaction.

## UI rules

- Material 3 components, not plain framework widgets: `MaterialToolbar`, `MaterialTextView`, `MaterialButton`, `MaterialCardView`, `MaterialDivider`, `TextInputLayout` with `TextInputEditText`, `MaterialSwitch`, `TabLayout`, `Chip`/`ChipGroup`, progress indicators.
- Every screen except Login has a `MaterialToolbar` with a title and a back arrow (content description set), except root screens.
- Toolbar actions use menu resources via `app:menu` and `setOnMenuItemClickListener` (no `setSupportActionBar`).
- Empty lists use a shared `view_empty.xml` through `<include>`.
- Theme: generated by Material Theme Builder (`themes.xml`, `colors.xml`, `theme_overlays.xml`). Do not hand-edit. Colors come only from `?attr/color*` and `?attr/textAppearance*`. No hex values or custom colors.
- Page background and toolbars `colorSurfaceContainer`, sticky bars `colorSurfaceContainerHigh`, brand accents `colorPrimary`, secondary actions tonal, one filled button per screen.
- Insets: `EdgeToEdge.enable(this)` right after `super.onCreate` in every Activity, `fitsSystemWindows="true"` on every layout root. Auth screens sit in a `ScrollView` with `fillViewport="true"`.
- Spacing: screen padding 16dp (24dp on auth), gaps 8dp between fields and cards, 16dp between sections. Lists use `clipToPadding="false"` with 8dp bottom padding.
- Dividers only above a sticky bottom bar or between distinct sections, never under a toolbar or between a heading and a list.
- Chips are for interactive items. Static facts go in text. Availability on a book row or detail is plain text, not a chip.
- Destructive actions offer a Snackbar Undo.
- Accessibility: content descriptions on icons, 48dp touch targets, decorative emoji hidden from screen readers.

## Forms

- Every `TextInputLayout` has helper text with the format when it matters.
- Validate with inline `setError` on the wrong field, not a generic Snackbar. Clear the error on the next attempt.
- Password fields use the toggle end icon, 6+ characters. Emails use `Patterns.EMAIL_ADDRESS`.
- Dates are picked with `MaterialDatePicker` / `MaterialTimePicker` into a read-only field.

## Naming and style

- Constants `ALL_CAPS_WITH_UNDERSCORES`.
- View ID prefixes: `til`, `et`, `dd`, `tv`, `btn`, `rv`, `chip`, `sw`, `card`, `ll`.
- 4-space indent, `binding.xxx` access, lambdas for click listeners. Match surrounding code.
- Comments only when the reason is not obvious.

## Account and notifications defaults

- Sign up, login, logout, profile edit and password reset are fake but validated. Accounts live in the SQLite `users` table, and the logged-in user is copied into `Data.USER_*` (`USER_ID`, `USER_EMAIL`, `USER_NAME`).
- One role only (library member), so there is no `role` column, no role tabs on Sign up and a single home screen (`CatalogActivity`). Logout is only on Profile. The user must have an account to use the app: Login is the launcher and nothing else opens without it.
- Create one demo account automatically (`member@demo.com`, password `demo1234`), plus a second seeded user (`Alex Reyes`) who holds the books that are already borrowed.
- Seed demo data in one `seedDemo(SQLiteDatabase)` method called from `DbHelper.onCreate`, so it runs once on first install and no screen is empty in a demo. Never reseed on login or launch.
- Light and dark mode are both supported through the generated theme. Do not hardcode colors.
- Notifications are in-app only (Toast, Snackbar, list). Never use real Android notifications or channels.

## App-specific rules

**1. User registration and login**
- Sign up asks for name, email, password and confirm password. Duplicate email shows an inline error on the email field.
- Login checks email and password against the `users` table. Wrong credentials show an inline error.
- Forgot password asks for the email and a new password and updates the row if the email exists.

**2. Book catalog**
- Seed about 15 books across at least 5 genres (for example Fiction, Fantasy, Science, History, Technology). Two or three are seeded as `STATUS_BORROWED`, loaned to the second seeded user, so unavailable books show up in a demo.
- Each row shows title, author, genre and availability text ("Available" / "Borrowed").
- Sorted by title. Tapping a row opens Book details.

**3. Search**
- A `TextInputLayout` search field above the list. A `TextWatcher` re-queries on every keystroke with `LIKE` on title, author and genre (case-insensitive) and refreshes the list. No search button.
- No matches shows the empty view.

**4. Book details**
- Shows title, author, genre, summary, publication date and availability status.
- The bottom bar has one filled button: **Check out** when the book is available, disabled and labelled **Unavailable** when borrowed. If the logged-in user holds the book, the button is **Return** instead.

**5. Checkout**
- Check out is only possible for `STATUS_AVAILABLE`. Re-read the status from the database at tap time, not from the screen, and if the book is no longer available show a Snackbar and refresh the screen.
- A confirm `MaterialAlertDialogBuilder` shows the due date (14 days from today, display only), then inserts the `loans` row, sets the book to `STATUS_BORROWED` and shows a Snackbar.
- No fines, no renewals, no borrowing limit.

**6. Return**
- Return is available from Book details and from the Borrowed tab on Profile (row tap opens a confirm dialog).
- Returning sets `returned_on` to today, sets the book back to `STATUS_AVAILABLE` and refreshes the list.

**7. User profile**
- Shows name and email, with Edit (name only, in a `BottomSheetDialog`) and Logout.
- A `TabLayout` with **Borrowed** (books not yet returned, with due date) and **Returned** (history, with borrowed and returned dates). Both read from `loans` joined with `books`.

**8. Offline functionality**
- The app has no network code and no permissions. Catalog, search and details come from SQLite, so they work with no connection. Do not add any internet permission or fetch.

## Screens (one Activity each)

- `LoginActivity` (launcher)
- `SignUpActivity`
- `ForgotPasswordActivity`
- `CatalogActivity` (root: search field, book list, Profile toolbar action)
- `BookDetailActivity`
- `ProfileActivity`

## Manifest

- Only `LoginActivity` (launcher) is `exported="true"`. Every other Activity is `exported="false"`. Register every new Activity.

## Release

- `release` uses `isMinifyEnabled = true` and `isShrinkResources = true`. Add keep rules to `proguard-rules.pro` only when a release crash needs one.
- Signing reads `keystore.properties` in the project root (see `keystore.properties.example`). Never commit `keystore.properties`, `*.jks` or `*.keystore`.
- Verify with `apksigner verify --print-certs --verbose <apk>`.

## Commands

- Build: `./gradlew assembleDebug`
- Release: `./gradlew assembleRelease`
- Lint: `./gradlew lintDebug`
- Run: Android Studio's Run button on an emulator.

## Commit rules

- **Never add Claude Code (or Claude) as an author or co-author of any git commit.** No `Co-Authored-By` trailer mentioning Claude or Anthropic, no "Generated with Claude Code" footer in commits or PR descriptions, and no author or committer override that names Claude. Commits are authored only by the configured git user.
- Do not change `git config user.name` or `user.email`.
- Short, plain, imperative messages, for example `Add catalog screen`.
- Do not commit unless asked.

## Working agreements

- Keep changes small and in scope. Do not refactor or restyle code you were not asked to touch.
- After a change, run the build and report failures with the output.
- Update this file when a rule changes or a screen is added.

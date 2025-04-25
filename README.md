# News Explorer App

A modern Android news reader application built entirely with Jetpack Compose, following MVVM architecture. It allows users to browse news headlines by category, search for specific topics, read article details, save articles for later, and customize their experience.

## Features

*   **Dynamic Home Feed:** Displays the latest news articles based on the selected category.
*   **Category Browsing:** Easily switch between news categories (General, Business, Technology, Sports, Entertainment, Science, Health) using a top selector or the navigation drawer.
*   **Article Detail View:** Read the title, description, and content (if available) of an article. Provides options to:
    *   Open the original article source in a browser.
    *   Share the article link with other apps.
    *   Save the article date/details to the device calendar (via Intent).
*   **Search Functionality:** Search for articles across various sources based on keywords.
*   **Bookmarking:** Save articles locally for offline reading and easy access via the "Saved Articles" screen. Bookmark status syncs across lists and detail views.
*   **Settings:**
    *   **Theme Selection:** Choose between Light, Dark, or System Default themes.
    *   **Font Size Adjustment:** Select Small, Medium, or Large font sizes for article content (applied via Theme).
    *   (Planned: Category/Source preferences, Notifications)
*   **Material Design 3:** Clean, modern UI using Material 3 components.
*   **Adaptive Layout:** Basic support for different screen sizes and orientations:
    *   Portrait (Phone): Single-column list.
    *   Landscape (Phone): Two-column grid.
    *   Tablet: Two or Three-column grid depending on orientation.
*   **Offline Caching:** Articles viewed are cached locally using Room database, allowing for limited offline reading.

## Tech Stack & Libraries

*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (UI Toolkit, Material 3, Compose Navigation)
*   **Architecture:** MVVM (ViewModel, Repository, Model)
*   **Asynchronous Programming:** Kotlin Coroutines & Flow
*   **Networking:** [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) (for fetching data from NewsAPI)
*   **JSON Parsing:** [Gson](https://github.com/google/gson) (via Retrofit converter)
*   **Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) (for caching articles)
*   **Dependency Injection:** Manual (using Application class and ViewModel Factories - **TODO:** Migrate to Hilt or Koin)
*   **Preferences:** [Jetpack DataStore (Preferences)](https://developer.android.com/topic/libraries/architecture/datastore)
*   **Image Loading:** [Coil](https://coil-kt.github.io/coil/) (Compose integration)
*   **Build System:** Gradle with Kotlin DSL (`.kts`) & Version Catalogs (`libs.versions.toml`)

## Setup and Running

Follow these steps to get the project running:

**1. Prerequisites:**

*   [Android Studio](https://developer.android.com/studio) (Latest stable version recommended)
*   JDK 11 or higher (Usually bundled with Android Studio)

**2. Clone the Repository:**

bash
git clone https://github.com/YOUR_USERNAME/NewsExplorer.git # Replace with your repo URL
cd NewsExplorer

**3. Get Your NewsAPI Key:**
* This project uses the NewsAPI to fetch news articles. You need a free API key.
* Go to https://newsapi.org/.
* Click on "Get API Key" or "Register".
* Sign up for a free account.
* Once registered and logged in, find your API key on your account dashboard.

**4. Add API Key to Project:**
* Important: Your API key is sensitive and should not be committed to Git. This project uses local.properties to store it securely.
* In the root directory of the cloned project, find or create a file named local.properties.
* Add the following line to local.properties, replacing "YOUR_ACTUAL_API_KEY_HERE" with the key you copied from NewsAPI:

NEWS_API_KEY="YOUR_ACTUAL_API_KEY_HERE"

* Verify .gitignore: Ensure the root .gitignore file contains the line /local.properties to prevent accidental commits of your key.

**5. Build and Run:**
* Open the project in Android Studio.
* Let Gradle sync the project dependencies (this might take a moment the first time).
* If the sync fails, try File -> Sync Project with Gradle Files.
* If you encounter build errors after adding the API key, try cleaning and rebuilding:
        Build -> Clean Project
        Build -> Rebuild Project
* Select a target device (emulator or physical device).
* Click the "Run 'app'" button (green play icon) in Android Studio.

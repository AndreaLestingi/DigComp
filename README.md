# DigComp

Application for studying and testing DigComp skills

## Key Features & Benefits

This application is designed to help users study and test their DigComp competencies. Core functionalities include:

*   Study materials and resources covering the DigComp framework.
*   Interactive quizzes and assessments to evaluate skill levels.
*   Personalized feedback and progress tracking.
*   User-friendly interface for easy navigation and learning.

## Prerequisites & Dependencies

Before you begin, ensure you have met the following requirements:

*   **Java Development Kit (JDK):** Version 8 or higher is recommended.
*   **Android Studio:** Latest version is recommended for development and testing.
*   **Android SDK:** API level 21 or higher.
*   **Gradle:** Version 7.0 or higher. (Usually bundled with Android Studio)

## Installation & Setup Instructions

Follow these steps to set up the project on your local machine:

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/AndreaLestingi/DigComp.git
    ```

2.  **Open the project in Android Studio:**

    *   Launch Android Studio.
    *   Select "Open an Existing Project."
    *   Navigate to the directory where you cloned the repository and select the `DigComp` folder.

3.  **Configure the SDK:**

    *   If prompted, ensure your Android SDK is correctly configured within Android Studio.
    *   You may need to download missing SDK components using the SDK Manager in Android Studio.

4.  **Build the project:**

    *   Click on "Build" > "Make Project" in the Android Studio menu.
    *   Resolve any dependency issues or build errors that may arise.

5.  **Run the application:**

    *   Connect an Android device or use an emulator.
    *   Click on "Run" > "Run 'app'" in the Android Studio menu.
    *   Select your connected device or emulator as the deployment target.

## Usage Examples & API Documentation

This application provides a user-friendly interface, and no external API documentation is currently available.  Refer to the in-app instructions for specific usage guidelines.

## Configuration Options

The application's behavior can be customized through the `build.gradle.kts` file.

*   **`minSdkVersion`:**  Minimum Android API level supported by the app.
*   **`targetSdkVersion`:** Target Android API level that the app is optimized for.
*   **`versionCode`:** Internal version number of the app.
*   **`versionName`:** User-visible version name of the app.

To modify these settings, open the `app/build.gradle.kts` file and adjust the values accordingly.

```kotlin
android {
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
}
```

## Contributing Guidelines

We welcome contributions to the DigComp project! To contribute:

1.  **Fork the repository.**
2.  **Create a new branch** for your feature or bug fix.
3.  **Make your changes** and ensure they adhere to the project's coding standards.
4.  **Test your changes thoroughly.**
5.  **Submit a pull request** with a clear description of your changes.

## License Information

This project is licensed under the [GNU General Public License v3.0](LICENSE).

## Acknowledgments

We would like to thank the creators of the DigComp framework for providing valuable resources in the area of digital competence.
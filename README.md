# SequenceBox 🔢📐

**SequenceBox** is a modern, feature-rich Android application built with **Jetpack Compose** and **Material 3** designed for students, educators, and math enthusiasts to calculate, analyze, and explore mathematical sequences (Arithmetic and Geometric progressions).

---

## ✨ Features

- **Arithmetic Sequences (AP):**
  - Calculate the $n$-th term ($a_n$).
  - Find the common difference ($d$).
  - Compute the sum of the first $n$ terms ($S_n$).
  - Generate full sequence terms with custom limits.

- **Geometric Sequences (GP):**
  - Calculate the $n$-th term ($a_n$).
  - Find the common ratio ($r$).
  - Compute the sum of finite or infinite geometric series.
  - Handle precision, large numbers, and output limits gracefully.

- **Interactive UI & Tools:**
  - **Step-by-Step Breakdown:** Detailed calculation steps explaining formulas and intermediate values.
  - **History Tracking:** Save, view, copy, and clear past calculations.
  - **Settings & Customization:** Dark/Light theme support, vibration/haptic feedback toggles, and formatting options.
  - **Export & Share:** Easily share calculation results and formulas via clipboard or other apps.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **Navigation:** Jetpack Navigation Compose
- **Concurrency:** Kotlin Coroutines & Flow
- **Language:** Kotlin 100%
- **Testing:** JUnit 4, Kotlin Coroutines Test, Compose UI Testing

---

## 📱 Screenshots & UI

*(Add screenshots here)*

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Narwhal / Koala or newer recommended)
- JDK 11 or higher
- Android SDK (Compile SDK 37, Min SDK 26)

### Clone & Build
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/SequenceBox.git
   cd SequenceBox
   ```
2. Open the project in Android Studio.
3. Run the app on an emulator or physical device using:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Running Tests

SequenceBox includes a robust unit test suite covering sequence calculators, formatting, validation, and ViewModel state management.

To run unit tests:
```bash
./gradlew test
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

---

Developed by [Abdullah Ateeq](https://github.com/abdullah-ateeq).

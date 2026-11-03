# Contributing to the Movie Streaming Demo App

Thanks for contributing to our **Interactive Multimedia Group Assignment**.
This document explains how we work together, what the assignment rules are, and how to
get your changes merged without breaking the build.

Please read this **before** you start coding or opening a pull request.

---

## 1. About the Project

A JavaFX desktop application that mimics a Netflix-style movie streaming service.
It displays movies in at least **3 genres**, each with **8+ titles**, and plays
YouTube trailers directly inside the app.

**Stack**

| Tool | Version |
|---|---|
| Java (JDK) | 17+ |
| JavaFX | 17+ (include `javafx-web`) |
| Build tool | Maven (or Gradle — see `pom.xml`) |
| IDE | IntelliJ IDEA / VS Code / Eclipse |
| Version control | Git + GitHub |

---

## 2. Non-Negotiable Assignment Rules

These come straight from the brief. **A PR that breaks any of these will be rejected**, even
if the code is otherwise good.

1. **Do NOT use `javafx.scene.control.Button`.** Anywhere. Ever.
   Use our custom `CustomButton` (built from `Label` / `StackPane` / `ImageView`).
2. **Do NOT add video files** (`.mp4`, `.mkv`, `.avi`, etc.) to the repository.
   Trailers are loaded from **YouTube** via `WebView` only.
3. Every category must have **at least 8 movies**.
4. Each category row must show **exactly 5 thumbnails** at a time, with horizontal scrolling.
5. Every movie needs a **thumbnail image** and a **visible title**.
6. Every thumbnail needs a **hover effect**.
7. The trailer player must support **play and pause**.
8. The loading animation for the YouTube player must be **our own custom JavaFX animation**
   (not a GIF, not a spinner library).
9. Sound effects are required where appropriate (hover, click, open trailer).
10. Category labels must be clearly visible.

If you're unsure whether something is allowed, **ask in the group chat before coding it.**

---

## 3. Getting Started

### 3.1 Clone the repository

```bash
git clone https://github.com/Tebello-Rankuatsana/JavaFx-Movie-Streaming-Demo-App.git
cd JavaFx-Movie-Streaming-Demo-App

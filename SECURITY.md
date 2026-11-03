# Security Policy

This document explains how we handle security for the **Interactive
Multimedia Group Assignment** (Movie Streaming Demo App). 

Please read this before reporting an issue, adding a dependency, or embedding external
content.

---

## 1. Scope

This project is a **local desktop JavaFX application**. It does not run a server, does not
store user accounts, and does not process payments. As a result, the attack surface is
small.

In scope:

- JavaFX application code (UI, models, utilities)
- The embedded YouTube player loaded through `WebView`
- Build files (`pom.xml` / `build.gradle`) and dependencies
- Repository configuration (`.gitignore`, GitHub settings)
- Resources in `src/main/resources/` (images, sounds, CSS, HTML)

Out of scope:

- YouTube itself, or any vulnerability in YouTube's player
- The operating system or JVM installation used to run the app
- Third-party IDEs (IntelliJ, VS Code, Eclipse)
- The network the app runs on

---

## 2. Supported Versions

Only the latest commit on the `main` branch is actively maintained. Security fixes will be
applied there.

| Version | Supported |
|---|---|
| `main` (latest commit) | Yes |
| Feature branches | No — will be fixed when merged to `main` |
| Past submissions / tags | No |

---

## 3. Reporting a Vulnerability

**Do not open a public GitHub issue for a security problem.**

If you believe you have found a security issue, report it privately to the group first:

1. Send a **private message** to the project lead (or the designated security contact
   listed in the group chat pinned message).
2. Include:
   - A short description of the issue
   - Steps to reproduce (or a minimal proof-of-concept)
   - Which file, class, or dependency is affected
   - The likely impact
   - Any suggested fix (optional)
3. **Do not** commit a fix directly — wait for confirmation first.

We aim to acknowledge a report within **48 hours** and agree on a fix and timeline within
**5 working days**. Because this is a student project with a hard deadline, timelines are
best-effort.

If the issue affects a **dependency** (e.g. a JavaFX CVE), also report it to the upstream
project.

---

## 4. What Counts as a Security Issue Here?

Examples relevant to this project:

- A dependency with a **known CVE** that affects how the app runs
- A hard-coded secret or API key committed to the repository
- Arbitrary **JavaScript execution** via the `WebView` that could escape the local
  application context
- Loading content from an **untrusted URL** based on user input
- Path traversal when loading images or sounds from disk
- A malicious image or sound file causing a crash or memory issue
- Committing personal data (student numbers, emails, home addresses) to the repo

Not security issues for this project:

- Broken UI layout
- A trailer that fails to load
- Styling or hover-effect bugs
- Missing sound effects

Please report those as normal bugs (see `CONTRIBUTING.md`, section 10).

---

## 5. Rules for Contributors

These rules keep the repository safe. **A PR that breaks any of them will be rejected.**

### 5.1 Secrets and Credentials

- **Never commit** API keys, tokens, passwords, OAuth secrets, or `.env` files.
- If you think you need a secret for a feature, **stop and ask the group first**.
  This project should not require any secrets to run.
- If a secret is accidentally committed, notify the group lead immediately. Treat it as
  compromised and rotate it — removing it from a later commit is not enough, since Git
  history still contains it.

### 5.2 Dependencies

- Only use dependencies that are **necessary**. Do not add a library for a feature we can
  write in a few lines.
- Prefer **well-maintained** libraries with recent releases.
- After adding or updating a dependency, run a vulnerability scan:

  ```bash
  # Maven
  mvn org.owasp:dependency-check-maven:check

  # Gradle (with the dependency-check plugin)
  ./gradlew dependencyCheckAnalyze

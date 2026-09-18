# ContextKey Privacy & Security Policy

## Core Privacy Principles

ContextKey AI is engineered with a strict **Privacy-First** architecture.

### 1. Zero Logging of User Keystrokes
- ContextKey never stores, buffers, or writes raw text, passwords, or conversations to local log files, shared preferences, or databases.
- Keystrokes are directly committed to the active application via Android's `InputConnection` API.

### 2. Sensitive Field Isolation
`InputSecurityPolicy` enforces strict hard-coded rules for sensitive input fields:
- `TYPE_TEXT_VARIATION_PASSWORD`
- `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD`
- `TYPE_TEXT_VARIATION_WEB_PASSWORD`
- `TYPE_NUMBER_VARIATION_PASSWORD` (PINs, OTPs)
- `IME_FLAG_NO_PERSONALIZED_LEARNING` (Incognito / private browsing modes)

When any of the above conditions are met:
- AI functionality is unconditionally prohibited.
- Context extraction is completely disabled.
- No network requests or background processing will be performed.

### 3. No Accessibility Abuse or Screen Scraping
- ContextKey does **NOT** use Android Accessibility Services.
- ContextKey does **NOT** perform screen scraping, OCR, or overlay snooping.
- Text interaction is strictly bounded by the legitimate `InputConnection` provided to the IME by the operating system.

### 4. Phase 1 Offline Guarantee
- The Phase 1 Android Keyboard requires **zero network permissions** (`INTERNET` permission is absent from `AndroidManifest.xml`).
- 100% offline and standalone operation.

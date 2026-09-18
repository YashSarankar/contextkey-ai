# ContextKey AI - Development Plan

## Roadmap Overview

```
Phase 1: Native Android Keyboard MVP (Current)
├── Native Android IME (InputMethodService)
├── Clean QWERTY + Symbol layouts
├── Shift / Caps lock handling
├── InputConnection operations (insert, delete, space, enter)
├── InputSecurityPolicy & password / sensitive field detection
├── SafeContextCollector abstraction (zero data leakage)
├── Comprehensive Unit Tests
└── Setup & Testing Onboarding Activity

Phase 2: Local Intelligence & Offline Processing (Upcoming)
├── Local tokenization and intent detection
├── On-device phrase suggestions
└── Sensitive field hard blocking verification

Phase 3: Hybrid AI Context Engine (Future)
├── ContextKey Context Engine
├── Opt-in secure sync
└── Real-time rewriting, tone adjustment, smart completions
```

## Phase 1 Deliverables

| Component | Status | Details |
|---|---|---|
| **Android Module** | Completed | Native Android project in `android_keyboard/` with Kotlin DSL (`.gradle.kts`) |
| **Package** | Completed | `com.contextkey.ai.keyboard` |
| **App Name** | Completed | `ContextKey Keyboard` |
| **IME Service** | Completed | `ContextKeyInputMethodService` implementing Android IME lifecycle |
| **Input Controller** | Completed | Text mutation via `InputConnection`, backspace, space, enter action handling |
| **Keyboard UI** | Completed | Responsive dark theme, key popups, haptics, QWERTY, Numeric Symbols (`?123`), Extended Symbols (`=\<`) |
| **Security Policy** | Completed | `InputSecurityPolicy` detecting password variants and `IME_FLAG_NO_PERSONALIZED_LEARNING` |
| **Privacy / Context** | Completed | `SafeContextCollector` guaranteeing zero logging and zero network calls |
| **Placeholder AI** | Completed | Small non-functional `[ AI ]` pill button in keyboard header |
| **Unit Tests** | Completed | Security policy, layout mapping, shift state cycling, input controller, and context collector tests |
| **Documentation** | Completed | Architecture, Privacy, Decisions, Development Plan |

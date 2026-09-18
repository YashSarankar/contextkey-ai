# Architecture Decision Records (ADR)

## ADR-001: Native Android IME vs Flutter for Keyboard
- **Status**: Accepted
- **Context**: An input method on Android must respond with near-zero latency, minimal memory footprint, and tight OS lifecycle integration. Flutter is not architecturally designed to host a system IME service.
- **Decision**: Implement the system keyboard as a pure native Kotlin Android IME using `InputMethodService` and native views. The Flutter application is reserved for companion management, settings, and dashboard features.

## ADR-002: Modular Input Security Engine
- **Status**: Accepted
- **Context**: Ensuring user privacy and preventing accidental context extraction in sensitive fields (such as passwords, credit cards, and private browser tabs) must be enforced from the ground up rather than retrofitted.
- **Decision**: Created `InputSecurityPolicy` as a core architectural gatekeeper. Any context collection or AI suggestions must pass through this security policy.

## ADR-003: Pure Custom View Layout for Keyboard Rendering
- **Status**: Accepted
- **Context**: The deprecated `android.inputmethodservice.KeyboardView` is legacy and lacks flexible touch/state manipulation.
- **Decision**: Created a lightweight, responsive View hierarchy (`KeyboardView`, `KeyView`, `KeyboardHeaderView`) with customizable themes, haptics, and instant state switching.

## ADR-004: Phase 1 Offline & No-Network Boundary
- **Status**: Accepted
- **Context**: In Phase 1, the objective is a rock-solid, functional keyboard MVP with normal typing and familiar layout.
- **Decision**: Omit `android.permission.INTERNET` from the keyboard manifest entirely. Place a non-functional `[ AI ]` placeholder pill in the header to establish UX visual hierarchy for future phases.

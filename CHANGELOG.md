# KDoc-er Changelog

## [2026.1.1]

### Added

- **`@throws` detection** — scans function bodies for `throw` expressions and emits `@throws` tags with the exception type. Enabled by default.
- **`@since` version stamp** — appends `@since <version>` to every generated KDoc. Configure the version string per project via settings or `sinceVersion` in `.kdocer.yaml`. Disabled by default.
- **`@see` cross-references** — links override methods to their super declaration (`@see SuperClass.method`), sealed subtypes to the parent sealed type, and object singletons to their implemented interface/superclass. Disabled by default; enable via settings or `seeReferences: true` in `.kdocer.yaml`.
- **Two inspection levels** — "Missing KDoc comment (suggestion)" (enabled by default, Alt+Enter only) and "Missing KDoc comment (warning)" (disabled by default, subtle underline). Both offer a quick-fix to generate the KDoc.
- **Non-destructive merge** — existing KDocs are preserved by default. New three-way policy: Merge (keeps your text, adds missing tags), Keep (leave untouched), or Replace (regenerate). Default is Merge.
- **Inferred return types** — expression-body functions (`fun f() = …`) now resolve the return type via the K2 Analysis API and emit a meaningful `@return` tag.
- **Smart `@return` descriptions** — return tags now carry type-derived text (e.g. `@return the user`, `@return the users`, `@return \`true\` if the condition holds`) instead of being empty.
- **`@constructor` toggle** — new setting to include or omit the `@constructor` line for classes.
- **Usage example generation** — optional fenced code sample appended to function KDocs (e.g. `val user = getUser(id)`), configurable on/off.
- **Folder / project batch generation** — right-click a folder in the Project view to generate KDocs for every Kotlin file underneath, with background progress and cancel support.
- **Generate `.kdocer.yaml` action** — Tools menu and Project-view action to scaffold a fully-commented config file at the project root.
- **Missing KDoc inspection** — optional `LocalInspectionTool` (disabled by default, WEAK WARNING) with Alt+Enter quick-fix to generate KDoc.
- **Unified settings page** — single scrollable Kotlin UI DSL page under Settings > Tools > KDoc-er with grouped sections: element types, visibility, description, framework awareness, usage example, existing KDoc policy, templates, notifications, and support links.

### Changed

- Bumped target platform to IntelliJ 2025.1 (since-build 241, open-ended).
- All actions now use `ActionUpdateThread.BGT` — eliminates `OLD_EDT` SEVERE exceptions on 2024.1+.
- Modernised notification API to use `NotificationGroupManager` with registered `<notificationGroup>`.
- Notification throttling changed from time-based to count-based (shows after every 20 generations, then resets).
- Redundant `@param` descriptions eliminated — `@param key the key` now collapses to `@param key`.
- Empty `@return` tags are no longer emitted; only generated when there is meaningful content.
- Consolidated two settings pages (Configuration + Templates & AI) into one clean page.
- Remove actions (single + all) fully rewritten and working correctly.
- Generate actions no longer gate on `SMART_INDENT_ON_ENTER`.
- Removed `com.intellij.modules.java` dependency — plugin now works on any JetBrains IDE with the Kotlin plugin, not just Java-capable IDEs.

### Removed

- AI/MCP backend layer — stripped entirely; the plugin focuses on the rule-based NLP engine.
- Old Java Swing settings panel (`.form` + `.java`).
- `ServiceManager` usage (replaced with `ApplicationManager.getApplication().getService()`).

## [2.0.0]

### Added

- Template-driven, NLP-aware description engine: verb mapping, word splitting and pluralisation produce natural sentences instead of hardcoded strings.
- Coroutine and stream awareness: `suspend` functions and `Flow`/`StateFlow`/`SharedFlow`/`Deferred`/`Channel` return types are documented automatically.
- Pluggable framework-aware aspect layer: detects Jetpack Compose, `ViewModel`, `LiveData`, `data`/`sealed`/`enum`/`object`/utility declarations and adds tailored notes.
- Optional `.kdocer.yaml` style sheet (verb maps, templates, aspect note overrides), resolved as a single source of truth alongside the settings panel.
- Unit and IntelliJ PSI fixture tests.

### Changed

- Modernised the build to Gradle 9.5.1, Kotlin 2.4.0, Java 17 and the IntelliJ Platform Gradle Plugin 2.16.0, targeting IntelliJ 2024.1+.
- Plugin metadata, description and change notes are now generated from `gradle.properties`, `README.md` and this changelog.

## [1.6.0]

### Fixed

- Underscore-separated function name issue.
- Various bug fixes.

## [1.5.0]

### Fixed

- Various bug fixes.

## [1.4.0]

### Added

- Plugin support for IDE builds newer than 182/2018.2.

## [1.3.0]

### Added

- Configuration page.
- Field and object support.

### Fixed

- Various bug fixes.

## [1.0.0]

### Added

- Support for Kotlin files, classes, functions, extension functions and receivers.
- Generate/remove KDoc for all elements in a file or a single element.
- Type `/**` and press Enter to generate KDoc for an element.
- Access from the Generate menu (Alt+Insert) and the Code menu.

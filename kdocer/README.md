# KDoc-er

[![Version](https://img.shields.io/jetbrains/plugin/v/14778-kdoc-er--kotlin-doc-generator.svg)](https://plugins.jetbrains.com/plugin/14778-kdoc-er--kotlin-doc-generator)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/14778-kdoc-er--kotlin-doc-generator.svg)](https://plugins.jetbrains.com/plugin/14778-kdoc-er--kotlin-doc-generator)

[Install from JetBrains Marketplace](https://plugins.jetbrains.com/plugin/14778-kdoc-er--kotlin-doc-generator)

## Requirements

- IntelliJ IDEA 2025.1 or later (any edition)
- Kotlin plugin installed

## Installation

1. Open **Settings > Plugins > Marketplace**
2. Search for **KDoc-er**
3. Click **Install** and restart

Or install directly: [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/14778-kdoc-er--kotlin-doc-generator)

<!-- Plugin description -->
**KDoc-er** is a smart, lightweight KDoc generator for Kotlin in IntelliJ IDEA.

It produces natural, readable documentation from your code — not boilerplate. A template-driven NLP engine builds descriptions from declaration names, understands coroutines and `Flow` streams, resolves inferred return types, and recognises frameworks like Jetpack Compose, `ViewModel`, and `LiveData`.

**Non-destructive by default** — regenerating never overwrites your hand-written documentation. The merge engine preserves your summaries and tag text while filling in missing `@param`, `@return`, and other tags.

Customise everything per project via a `.kdocer.yaml` style sheet, or use the settings panel.


## Features

### Smart KDoc Generation

- **NLP-aware descriptions** — verb maps, word splitting, and pluralisation produce natural sentences (`getUserById` → *"Gets the user by id."*) instead of generic placeholders.
- **Inferred return types** — expression-body functions (`fun f() = expr`) resolve the return type via the K2 Analysis API and emit a meaningful `@return`.
- **Smart `@return` descriptions** — type-derived text like `@return the user`, `@return the users` (collections), or `` @return `true` if the condition holds `` (booleans).
- **Coroutine & stream awareness** — `suspend` functions and `Flow`/`StateFlow`/`SharedFlow`/`Deferred`/`Channel` return types are documented automatically.
- **Framework detection** — recognises Jetpack Compose, `ViewModel`, `LiveData`, `data`/`sealed`/`enum`/`object`/utility classes and adds tailored notes.
- **`@throws` detection** — scans function bodies for `throw` expressions and emits `@throws` tags with the exception type.
- **`@since` version stamp** — optionally appends `@since <version>` to every generated KDoc, with the version configurable per project.
- **`@see` cross-references** — links override methods to their super declaration, and sealed subtypes to the parent type.
- **Usage examples** — optionally appends a fenced code sample to function KDocs (e.g. `val user = getUser(id)`).

### Non-Destructive Merge

Three policies for handling existing KDocs:

| Policy | Behaviour |
|---|---|
| **Merge** (default) | Keeps your summary and tag text, adds only missing tags, never drops user-only tags like `@throws` |
| **Keep** | Leaves existing KDocs completely untouched |
| **Replace** | Regenerates the entire comment from scratch |

### Multiple Ways to Generate

| Action | Where | What it does |
|---|---|---|
| **Type `/**` + Enter** | Editor | Generates KDoc for the element at the cursor |
| **Create KDoc for Element** | Generate menu (Alt+Insert) / Code menu | Single-element generation |
| **Create KDocs for All Elements** | Generate menu / Code menu | Documents every qualifying element in the file |
| **Create KDocs in Folder** | Right-click folder in Project view | Batch-generates across all `.kt` files with progress bar |
| **Remove KDoc for Element** | Code menu | Removes the KDoc from the element at the cursor |
| **Remove KDocs for All Elements** | Code menu / right-click | Removes every KDoc in the file |

### Missing KDoc Inspection

An inspection (enabled by default) gently suggests adding KDoc to undocumented declarations. Use **Alt+Enter** to generate the missing KDoc via the quick-fix.

Configure or disable it in *Settings > Editor > Inspections > KDoc-er > Missing KDoc comment*.

### Project-Level Configuration

Create a `.kdocer.yaml` at the project root to override settings per project. Generate a starter file with every option commented out:

**Tools > Create .kdocer.yaml Config** (or right-click in the Project view > KDoc-er > Create .kdocer.yaml Config)

```yaml
style:
  appendName: true            # prepend a generated description sentence
  splitNames: true            # split camelCase/snake_case into readable phrases
  frameworkAware: true        # detect Compose / ViewModel / LiveData / data / sealed
  usageExample: false         # append a fenced sample call to function KDocs
  includeConstructor: true    # emit the @constructor line for classes
  existingKDoc: merge         # merge | keep | replace
  throwsDetection: false      # scan function bodies for throw and emit @throws
  sinceTag: false             # stamp KDocs with @since <version>
  sinceVersion: "1.0.0"       # version string for @since
  seeReferences: false        # add @see for overrides and sealed subtypes
  verbMapping:                # extend/override the verb -> phrase map
    fetch: "Fetches the {noun} from the remote source"
    sync: "Synchronises the {noun}"

aspects:
  notes:                      # override framework note wording
    compose: "Jetpack Compose UI for {name}."
    viewmodel: "Holds and exposes the UI state for the {name} screen."

templates:
  function:
    description: "{description}"
    param: "* @param {name} the {noun}"
    return: "* @return {description}"
  class:
    description: "{description}"
    constructor: "* @constructor {description}"
  property:
    description: "{description}"
```

### Settings

All options are available under **Settings > Tools > KDoc-er**:

- **Generate KDoc For** — classes & objects, functions, properties
- **Visibility** — public, protected, internal, private, overridden members
- **Description** — prepend element name, split camelCase names, `@constructor` toggle
- **Framework Awareness** — detect and annotate frameworks
- **@throws Detection** — scan for throw expressions
- **@since Tag** — stamp with a version string
- **@see Cross-References** — link overrides and sealed subtypes
- **Usage Example** — append sample call to function KDocs
- **Existing KDoc** — Merge / Keep / Replace policy
- **Templates** — override description and tag templates with `{placeholder}` tokens
- **Notifications** — disable the occasional plugin notification

## Customization Guide

Every part of the generated KDoc can be customized — from the description sentence to framework notes to tag templates. Configuration is resolved in order: `.kdocer.yaml` > IDE settings > built-in defaults.

### Controlling Descriptions

By default, KDoc-er builds a description from the element name using NLP:

```kotlin
// Generated with defaults:
/**
 * Http method
 */
enum class HttpMethod { ... }
```

| Option | Effect | YAML | Settings UI |
|---|---|---|---|
| **Disable descriptions** | Removes the auto-generated description line entirely | `appendName: false` | Uncheck *"Prepend element name"* |
| **Keep camelCase as-is** | `HttpMethod` stays as `HttpMethod` instead of `Http method` | `splitNames: false` | Uncheck *"Split class names"* |

### How the NLP Description Engine Works

KDoc-er generates descriptions by splitting an identifier into words, matching the leading word against a **verb map**, and filling in the remaining words as the **noun**:

```
fetchUserProfile
  ↓ split
[fetch, user, profile]
  ↓ verb map lookup: "fetch" → "Returns the {noun}"
  ↓ noun = remaining words joined: "user profile"
Returns the user profile
```

For classes and properties (no verb prefix), the name itself becomes the description:
```
UserRepository → "User repository"
sessionToken   → "Session token"
```

**Pluralisation** is automatic for collection return types — if a function returns `List<User>`, the noun `user` becomes `users`:
```kotlin
fun getUsers(): List<User>
// → "Returns the users"
```

**Redundancy elimination** — when a `@param` name already says it all, the noun is dropped:
```kotlin
// @param key the key  →  collapsed to just:
// @param key
```

### Built-in Verb Map Reference

Every function name prefix below is recognised out of the box. The `{noun}` placeholder is replaced with the remaining words of the function name.

| Verb prefix(es) | Generated phrase |
|---|---|
| `get`, `fetch`, `load`, `retrieve`, `find`, `read`, `obtain`, `of`, `from` | Returns the {noun} |
| `set`, `update`, `save`, `store`, `write`, `put`, `assign` | Sets the {noun} |
| `is`, `has`, `can`, `should`, `are`, `was`, `will`, `must` | Returns \`true\` if {noun} |
| `check`, `contains`, `equals`, `matches`, `supports`, `allows` | Returns \`true\` if {noun} |
| `create`, `build`, `make`, `new`, `generate`, `produce`, `construct` | Creates a new {noun} |
| `delete`, `remove`, `clear`, `drop`, `destroy`, `discard`, `erase` | Removes the {noun} |
| `add`, `append`, `insert`, `push`, `register`, `attach` | Adds the {noun} |
| `init`, `initialize`, `initialise`, `setup`, `configure`, `prepare` | Initialises the {noun} |
| `convert`, `parse`, `map`, `transform`, `serialize`, `deserialize`, `to`, `as` | Converts the {noun} |
| `validate`, `verify`, `ensure`, `assert`, `require` | Validates the {noun} |
| `calculate`, `compute`, `count`, `sum`, `measure` | Calculates the {noun} |
| `handle`, `process`, `execute`, `run`, `perform`, `apply`, `invoke`, `call` | Handles the {noun} |
| `send`, `emit`, `publish`, `dispatch`, `post`, `submit`, `notify` | Sends the {noun} |
| `receive`, `collect`, `consume`, `observe`, `subscribe`, `listen` | Receives the {noun} |
| `start`, `begin`, `launch`, `open`, `connect` | Starts the {noun} |
| `stop`, `end`, `close`, `cancel`, `finish`, `disconnect`, `shutdown` | Stops the {noun} |
| `toggle`, `switch` | Toggles the {noun} |
| `reset`, `refresh`, `reload`, `sync` | Resets the {noun} |
| `show`, `display`, `render`, `draw`, `print` | Displays the {noun} |
| `hide`, `dismiss` | Hides the {noun} |
| `filter`, `select`, `query`, `search` | Filters the {noun} |
| `sort`, `order`, `arrange` | Sorts the {noun} |
| `copy`, `clone`, `duplicate` | Copies the {noun} |
| `merge`, `combine`, `join`, `concat` | Merges the {noun} |
| `enable` | Enables the {noun} |
| `disable` | Disables the {noun} |

### Custom Verb Mappings

You can **override** any built-in verb or **add new ones** for verbs specific to your project or library. Custom mappings take priority over the built-in table:

```yaml
# .kdocer.yaml
style:
  verbMapping:
    # Override built-in verbs with your preferred wording
    fetch: "Fetches the {noun} from the remote source"
    delete: "Permanently deletes the {noun}"

    # Add verbs for your own library / domain
    sync: "Synchronises the {noun} with the server"
    enqueue: "Enqueues the {noun} for background processing"
    schedule: "Schedules the {noun} for deferred execution"
    broadcast: "Broadcasts the {noun} to all registered listeners"
    cache: "Caches the {noun} for faster subsequent access"
    migrate: "Migrates the {noun} to the latest schema"
    archive: "Archives the {noun}"
    restore: "Restores the {noun} from the archive"
    retry: "Retries the {noun}"
    throttle: "Throttles the {noun}"
    debounce: "Debounces the {noun}"
    encrypt: "Encrypts the {noun}"
    decrypt: "Decrypts the {noun}"
    log: "Logs the {noun}"
    audit: "Audits the {noun}"
    bind: "Binds the {noun} to the lifecycle"
    inject: "Injects the {noun} dependency"
    provide: "Provides the {noun} to the dependency graph"
    emit: "Emits the {noun} to downstream collectors"
    navigate: "Navigates to the {noun} screen"
    render: "Renders the {noun} component"
```

**Example** — with `navigate: "Navigates to the {noun} screen"` configured:

```kotlin
fun navigateUserProfile(userId: Long) { ... }

// Generated KDoc:
/**
 * Navigates to the user profile screen
 *
 * @param userId
 */
```

### Controlling Framework Notes

When **framework awareness** is enabled (the default), KDoc-er detects patterns like `data class`, `sealed`, `enum`, `@Composable`, `ViewModel`, etc. and appends a contextual note:

```kotlin
// Generated with frameworkAware: true (default):
/**
 * Show dialog
 * Data class — a value holder; `equals`, `hashCode`, `toString` and `copy` are generated.
 *
 * @constructor Creates a new ShowDialog
 * @property title
 * @property message
 */
data class ShowDialog(val title: String, val message: String)
```

#### Disable all framework notes

```yaml
# .kdocer.yaml
style:
  frameworkAware: false
```

Or uncheck **"Framework-aware notes"** in the settings UI. Result:

```kotlin
/**
 * Show dialog
 *
 * @constructor Creates a new ShowDialog
 * @property title
 * @property message
 */
data class ShowDialog(val title: String, val message: String)
```

#### Customize wording for specific aspects

Override any aspect's note text using its ID under `aspects.notes`. Use `{name}` as a placeholder for the humanized element name:

```yaml
# .kdocer.yaml
aspects:
  notes:
    data-class: "Immutable value type."
    compose: "Jetpack Compose UI for {name}."
    viewmodel: "Manages UI state for {name}."
    enum: "Defines the set of valid {name} values."
```

#### Suppress a single aspect note

Set its note to an empty string to remove just that one while keeping the others:

```yaml
aspects:
  notes:
    data-class: ""    # no note for data classes
    # all other aspects still produce their default notes
```

#### Available aspect IDs and their default notes

| ID | Matches | Default note |
|---|---|---|
| `compose` | `@Composable` functions | Composable function — emits UI and is re-invoked (recomposed) when its inputs change. |
| `compose-preview` | `@Preview` composable functions | Design-time Compose preview — not used at runtime. |
| `viewmodel` | Classes extending `ViewModel` / `AndroidViewModel` | Android \[ViewModel\] — survives configuration changes and exposes observable UI state. |
| `data-class` | `data class` declarations | Data class — a value holder; \`equals\`, \`hashCode\`, \`toString\` and \`copy\` are generated. |
| `sealed` | `sealed class` / `sealed interface` declarations | Sealed hierarchy — all subtypes are known at compile time, ideal for exhaustive \`when\`. |
| `enum` | `enum class` declarations | Enum — a fixed set of named constants. |
| `companion` | `companion object` blocks | Companion object — holds factory methods and constants for the enclosing class. |
| `object` | Top-level `object` singletons (not companions) | Singleton object — a single shared instance. |
| `util` | Classes named `*Util`, `*Utils`, or `*Helper` | Utility holder — stateless helper functions; not meant to be instantiated. |
| `livedata` | Properties typed `LiveData` / `MutableLiveData` | Observable \[LiveData\] state — observe it from the UI to receive updates. |
| `flow-state` | Properties typed `StateFlow` / `SharedFlow` / `Flow` | Observable stream — collect it from a coroutine to receive updates. |

#### Customizing aspects for your own libraries

The `aspects.notes` section lets you tailor the wording to match your project's libraries and conventions. For example, if your team uses Koin for DI, Room for database, or a custom event bus:

```yaml
# .kdocer.yaml — tailored for a project using Koin + Room + custom EventBus
aspects:
  notes:
    # Make ViewModel notes reference your DI setup
    viewmodel: "Koin-injected ViewModel — use `by viewModel()` to obtain an instance."

    # Reference your database layer for data classes
    data-class: "Room entity — mapped to a database table."

    # Tailor companion notes for your factory pattern
    companion: "Factory and constants — prefer `create()` over the constructor."

    # Custom wording for observable properties in your architecture
    flow-state: "Collected by the UI layer via `collectAsState()` in Compose."
    livedata: "Observed by the Fragment via `observe(viewLifecycleOwner)`."
```

> **Note:** The built-in aspects detect patterns by code structure (annotations, superclasses, naming conventions), not by import paths. If your custom library uses patterns that don't match any built-in aspect (e.g. a custom `@Injectable` annotation), the aspect system won't detect it — but you can still use **verb mappings** and **template overrides** to control the generated descriptions for those elements.

### @throws Detection

When enabled, KDoc-er scans function bodies for `throw` expressions and emits `@throws` tags:

```yaml
# .kdocer.yaml
style:
  throwsDetection: true
```

```kotlin
fun requireActive(userId: Long): User {
    val user = findById(userId) ?: throw NoSuchElementException("User not found")
    if (!user.active) throw IllegalStateException("User is not active")
    return user
}

// Generated KDoc:
/**
 * Requires the active
 *
 * @param userId
 * @return the user
 * @throws IllegalStateException
 * @throws NoSuchElementException
 */
```

### @since Version Stamp

Appends a `@since` tag to every generated KDoc with the configured version string:

```yaml
# .kdocer.yaml
style:
  sinceTag: true
  sinceVersion: "2.0.0"
```

```kotlin
// Generated KDoc:
/**
 * Gets the user by id
 *
 * @param id
 * @return the user
 * @since 2.0.0
 */
```

### @see Cross-References

When enabled, KDoc-er adds `@see` links for:
- **Override methods** → links to the super class method
- **Sealed subtypes** → links to the parent sealed type
- **Object singletons** → links to the implemented interface / superclass

```yaml
# .kdocer.yaml
style:
  seeReferences: true
```

```kotlin
sealed class NavigationEvent {
    data class NavigateTo(val route: String) : NavigationEvent()
    // Generated KDoc for NavigateTo includes: @see NavigationEvent
}

class UserService : BaseService() {
    override fun validate(input: String): Boolean { ... }
    // Generated KDoc for validate includes: @see BaseService.validate
}
```

### Template Overrides

Override the structure of generated KDoc lines using `{placeholder}` tokens:

```yaml
templates:
  function:
    description: "{description}"
    param: "* @param {name} the {noun}"
    return: "* @return {description}"
  class:
    description: "{description}"
    constructor: "* @constructor {description}"
  property:
    description: "{description}"
```

These can also be set per-field in the settings UI under the **Templates** section. Leave a template blank to use the built-in default.

### Full Configuration Example

Here's a complete `.kdocer.yaml` showing all options together:

```yaml
# .kdocer.yaml
style:
  appendName: true
  splitNames: true
  frameworkAware: true
  usageExample: false
  includeConstructor: true
  existingKDoc: merge          # merge | keep | replace
  throwsDetection: true
  sinceTag: true
  sinceVersion: "2.0.0"
  seeReferences: true
  verbMapping:
    sync: "Synchronises the {noun} with the backend"
    navigate: "Navigates to the {noun} screen"
    inject: "Injects the {noun} dependency"

aspects:
  notes:
    viewmodel: "Koin-injected ViewModel for {name}."
    data-class: ""             # suppress data class notes
    compose: "Compose UI — recomposes when inputs change."

templates:
  function:
    description: "{description}"
    param: "* @param {name} the {noun}"
    return: "* @return {description}"
  class:
    description: "{description}"
    constructor: "* @constructor Creates a new {description}"
  property:
    description: "{description}"
```
<!-- Plugin description end -->

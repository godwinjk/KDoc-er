# RustDoc-er

A smart, lightweight RustDoc generator for Rust in IntelliJ IDEA (with the Rust plugin).

## Requirements

- IntelliJ IDEA 2025.1 or later (any edition)
- Rust plugin (`com.jetbrains.rust`) installed

## Installation

1. Open **Settings > Plugins > Marketplace**
2. Search for **RustDoc-er**
3. Click **Install** and restart

<!-- Plugin description -->
**RustDoc-er** is a smart, lightweight RustDoc generator for Rust in IntelliJ IDEA (with the Rust plugin).

It produces natural, readable documentation from your code -- not boilerplate. A template-driven NLP engine builds descriptions from declaration names, detects `unsafe` patterns, analyses `Result<T, E>` and `Option<T>` return types, and recognises common crate patterns like `serde`, `async_trait`, and derive macros.

**Non-destructive by default** -- regenerating never overwrites your hand-written documentation. The merge engine preserves your summaries and section content while filling in missing `# Arguments`, `# Returns`, and other sections.

Customise everything per project via a `.rustdocer.yaml` style sheet, or use the settings panel.

## Features

### Smart RustDoc Generation

RustDoc uses `///` outer doc comments with Markdown formatting. RustDoc-er generates idiomatic Rust documentation following the official [Rust API Guidelines](https://rust-lang.github.io/api-guidelines/documentation.html).

- **NLP-aware descriptions** -- verb maps, word splitting, and pluralisation produce natural sentences (`get_user_by_id` -> *"Returns the user by id."*) instead of generic placeholders.
- **Section-based format** -- RustDoc uses named sections (`# Arguments`, `# Returns`, `# Panics`, `# Safety`, `# Errors`, `# Examples`) rather than tag-based annotations. RustDoc-er generates the correct sections automatically.
- **Parameter documentation** -- parameters are documented as bullet points under `# Arguments`: `` * `name` - description ``.
- **Smart `# Returns` sections** -- type-derived descriptions like "Returns the user", "Returns the users" (for `Vec<T>`), or "Returns `true` if the condition holds" (for `bool`).
- **Panic detection** -- scans function bodies for `panic!()`, `.unwrap()`, `.expect()`, `unreachable!()`, and `todo!()` and emits a `# Panics` section describing when the function panics.
- **`# Safety` section** -- automatically generated for `unsafe fn` declarations, documenting the safety invariants the caller must uphold.
- **`# Errors` section** -- generated for functions returning `Result<T, E>`, documenting the error conditions.
- **`# Examples` section** -- optionally appends a fenced code block with a usage example.
- **Crate awareness** -- recognises `serde` derive macros (`Serialize`/`Deserialize`), `async_trait`, `thiserror`, `Clone`, `Debug`, `Default` derives, and adds tailored notes.
- **Async/Future awareness** -- `async fn` and functions returning `impl Future<Output = T>` are documented with appropriate context.
- **Cross-references** -- links trait implementations to their trait definition using `[TraitName::method]` intra-doc link syntax.

### Rust-Idiomatic Output

RustDoc-er generates documentation that follows Rust conventions:

```rust
/// Returns the user by id.
///
/// # Arguments
///
/// * `id` - The user identifier.
///
/// # Returns
///
/// The matching [`User`], or `None` if not found.
///
/// # Errors
///
/// Returns [`DatabaseError`] if the connection fails.
///
/// # Panics
///
/// Panics if `id` is empty.
pub async fn get_user_by_id(id: &str) -> Result<Option<User>, DatabaseError> { ... }
```

Compare with KDoc (Kotlin) style -- RustDoc-er uses the correct format for each language:

| Aspect | KDoc (Kotlin) | RustDoc (Rust) |
|---|---|---|
| Comment style | `/** ... */` | `///` outer doc comments |
| Parameters | `@param name description` | `# Arguments` section with `` * `name` `` bullets |
| Returns | `@return description` | `# Returns` section |
| Throws/Panics | `@throws Type description` | `# Panics` / `# Errors` sections |
| Safety | N/A | `# Safety` section for `unsafe fn` |
| Examples | Optional fenced block | `# Examples` section with code block |
| References | `[ClassName]` | `[`ClassName`]` intra-doc links |

### Non-Destructive Merge

Three policies for handling existing doc comments:

| Policy | Behaviour |
|---|---|
| **Merge** (default) | Keeps your summary and section content, adds only missing sections and argument entries |
| **Keep** | Leaves existing doc comments completely untouched |
| **Replace** | Regenerates the entire comment from scratch |

### Multiple Ways to Generate

| Action | Where | What it does |
|---|---|---|
| **Type `///` + Enter** | Editor | Generates RustDoc for the item at the cursor |
| **Create RustDoc for Item** | Generate menu (Alt+Insert) / Code menu | Single-item generation |
| **Create RustDocs for All Items** | Generate menu / Code menu | Documents every qualifying item in the file |
| **Create RustDocs in Folder** | Right-click folder in Project view | Batch-generates across all `.rs` files with progress bar |
| **Remove RustDoc for Item** | Code menu | Removes the doc comment from the item at the cursor |
| **Remove RustDocs for All Items** | Code menu / right-click | Removes every doc comment in the file |

### Missing RustDoc Inspection

An inspection (enabled by default) suggests adding documentation to undocumented public items. Use **Alt+Enter** to generate the missing RustDoc via the quick-fix.

Configure or disable it in *Settings > Editor > Inspections > RustDoc-er > Missing RustDoc comment*.

### Project-Level Configuration

Create a `.rustdocer.yaml` at the project root to override settings per project. Generate a starter file with every option commented out:

**Tools > Create .rustdocer.yaml Config**

```yaml
style:
  appendName: true            # prepend a generated description sentence
  splitNames: true            # split snake_case/CamelCase into readable phrases
  crateAware: true            # detect serde / async_trait / thiserror / derives
  usageExample: false         # append a # Examples section with sample code
  existingDoc: merge          # merge | keep | replace
  panicDetection: true        # scan for panic!() / unwrap() / expect() and emit # Panics
  safetySection: true         # emit # Safety for unsafe fn
  errorsSection: true         # emit # Errors for Result<T, E> returns
  sinceTag: false             # stamp docs with a version string
  sinceVersion: "1.0.0"       # version string for the since stamp
  seeReferences: false        # add cross-references for trait impls
  verbMapping:                # extend/override the verb -> phrase map
    fetch: "Fetches the {noun} from the remote source"
    sync: "Synchronises the {noun}"

aspects:
  notes:                      # override crate/pattern note wording
    serde: "Serializable via serde."
    async-trait: "Async trait method -- requires an async runtime."

templates:
  function:
    description: "{description}"
    argument: "* `{name}` - {description}"
    returns: "{description}"
  struct:
    description: "{description}"
  enum:
    description: "{description}"
  trait:
    description: "{description}"
```

### Settings

All options are available under **Settings > Tools > RustDoc-er** (independent from KDoc-er):

- **Generate RustDoc For** -- structs, enums, traits, functions, methods, constants, type aliases
- **Visibility** -- `pub`, `pub(crate)`, `pub(super)`, private items
- **Description** -- prepend item name, split snake_case names
- **Crate Awareness** -- detect and annotate common crate patterns
- **Panic Detection** -- scan for panic/unwrap/expect
- **Safety Section** -- emit `# Safety` for unsafe functions
- **Errors Section** -- emit `# Errors` for Result returns
- **@since Tag** -- stamp with a version string
- **Cross-References** -- link trait implementations
- **Usage Example** -- append `# Examples` section
- **Existing Doc** -- Merge / Keep / Replace policy
- **Templates** -- override section templates with `{placeholder}` tokens

## Customization Guide

Every part of the generated RustDoc can be customized. Configuration is resolved in order: `.rustdocer.yaml` > IDE settings > built-in defaults.

### How the NLP Description Engine Works

RustDoc-er uses the same NLP engine as KDoc-er. It splits an identifier into words (handling both `snake_case` and `CamelCase`), matches the leading word against a **verb map**, and fills in the remaining words as the **noun**:

```
fetch_user_profile
  -> split
[fetch, user, profile]
  -> verb map lookup: "fetch" -> "Returns the {noun}"
  -> noun = remaining words joined: "user profile"
Returns the user profile
```

For structs and constants (no verb prefix), the name itself becomes the description:
```
UserRepository -> "User repository"
session_token  -> "Session token"
```

**Pluralisation** is automatic for collection return types -- if a function returns `Vec<User>`, the noun `user` becomes `users`:
```rust
fn get_users() -> Vec<User> { ... }
/// Returns the users.
```

### Built-in Verb Map Reference

Every function name prefix below is recognised out of the box. The `{noun}` placeholder is replaced with the remaining words of the function name.

| Verb prefix(es) | Generated phrase |
|---|---|
| `get`, `fetch`, `load`, `retrieve`, `find`, `read`, `obtain`, `of`, `from` | Returns the {noun} |
| `set`, `update`, `save`, `store`, `write`, `put`, `assign` | Sets the {noun} |
| `is`, `has`, `can`, `should`, `are`, `was`, `will`, `must` | Returns `true` if {noun} |
| `check`, `contains`, `equals`, `matches`, `supports`, `allows` | Returns `true` if {noun} |
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

You can **override** any built-in verb or **add new ones** for verbs specific to your project:

```yaml
# .rustdocer.yaml
style:
  verbMapping:
    fetch: "Fetches the {noun} from the remote source"
    spawn: "Spawns the {noun} as an async task"
    register: "Registers the {noun} with the event loop"
    drain: "Drains the {noun}, consuming all pending items"
```

### Crate Pattern Notes

When **crate awareness** is enabled (the default), RustDoc-er detects common patterns and appends contextual notes:

```rust
/// User data.
/// Serializable -- derives `Serialize` and `Deserialize` for serde compatibility.
#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct User { ... }
```

#### Available aspect IDs and their default notes

| ID | Matches | Default note |
|---|---|---|
| `serde` | `#[derive(Serialize)]` / `#[derive(Deserialize)]` | Serializable -- derives `Serialize` and `Deserialize` for serde compatibility. |
| `async-trait` | `#[async_trait]` trait/impl blocks | Async trait -- requires an async runtime (e.g. tokio or async-std). |
| `thiserror` | `#[derive(Error)]` enums | Error type -- derives `std::error::Error` via thiserror. |
| `derive-debug` | `#[derive(Debug)]` | Implements `Debug` for formatted output. |
| `derive-clone` | `#[derive(Clone)]` | Implements `Clone` for value duplication. |
| `derive-default` | `#[derive(Default)]` | Implements `Default` for zero-value construction. |
| `unsafe-fn` | `unsafe fn` declarations | Unsafe function -- caller must uphold the safety invariants documented in `# Safety`. |
| `unsafe-trait` | `unsafe trait` declarations | Unsafe trait -- implementations must uphold the documented safety invariants. |
| `builder` | Types named `*Builder` | Builder -- constructs the target type step by step; call `.build()` to finish. |

### Controlling Crate Notes

```yaml
# .rustdocer.yaml
aspects:
  notes:
    serde: "JSON-serializable via serde."
    thiserror: ""    # suppress thiserror notes
    unsafe-fn: "SAFETY: review the # Safety section before calling."
```

### Panic Detection

When enabled (on by default), RustDoc-er scans function bodies for panic-inducing patterns:

```yaml
# .rustdocer.yaml
style:
  panicDetection: true
```

Detected patterns: `panic!()`, `.unwrap()`, `.expect()`, `unreachable!()`, `todo!()`, `unimplemented!()`, `assert!()`, `assert_eq!()`, `assert_ne!()`.

```rust
/// Returns the active user.
///
/// # Arguments
///
/// * `id` - The user identifier.
///
/// # Returns
///
/// The active [`User`].
///
/// # Panics
///
/// Panics if the user is not found (`.unwrap()`) or if the user is not active (`.expect()`).
pub fn get_active_user(id: u64) -> User {
    let user = find_user(id).unwrap();
    assert!(user.is_active, "user must be active");
    user
}
```

### Safety Section

Automatically generated for `unsafe fn` declarations:

```rust
/// # Safety
///
/// Caller must ensure that `ptr` is a valid, aligned, non-null pointer
/// to an initialised `T` and that no other references to the same memory exist.
pub unsafe fn read_raw<T>(ptr: *const T) -> T { ... }
```

### Errors Section

Generated for functions returning `Result<T, E>`:

```rust
/// # Errors
///
/// Returns [`DatabaseError`] if the query fails.
pub fn get_user(id: u64) -> Result<User, DatabaseError> { ... }
```

### Template Overrides

Override the structure of generated documentation using `{placeholder}` tokens:

```yaml
templates:
  function:
    description: "{description}"
    argument: "* `{name}` - {description}"
    returns: "{description}"
  struct:
    description: "{description}"
  enum:
    description: "{description}"
  trait:
    description: "{description}"
```

### Full Configuration Example

```yaml
# .rustdocer.yaml
style:
  appendName: true
  splitNames: true
  crateAware: true
  usageExample: true
  existingDoc: merge           # merge | keep | replace
  panicDetection: true
  safetySection: true
  errorsSection: true
  sinceTag: true
  sinceVersion: "2.0.0"
  seeReferences: true
  verbMapping:
    spawn: "Spawns the {noun} as an async task"
    drain: "Drains the {noun}, consuming all pending items"
    register: "Registers the {noun} with the event loop"

aspects:
  notes:
    serde: "JSON-serializable via serde."
    thiserror: ""              # suppress thiserror notes
    unsafe-fn: "SAFETY: review the # Safety section before calling."

templates:
  function:
    description: "{description}"
    argument: "* `{name}` - {description}"
    returns: "{description}"
  struct:
    description: "{description}"
  enum:
    description: "{description}"
  trait:
    description: "{description}"
```
<!-- Plugin description end -->

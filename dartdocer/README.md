# DartDoc-er

A smart, lightweight DartDoc generator for Dart in IntelliJ IDEA and Android Studio.

## Requirements

- IntelliJ IDEA 2025.1 or later (any edition), or Android Studio
- Dart plugin installed

## Installation

1. Open **Settings > Plugins > Marketplace**
2. Search for **DartDoc-er**
3. Click **Install** and restart

<!-- Plugin description -->
**DartDoc-er** is a smart, lightweight DartDoc generator for Dart in IntelliJ IDEA and Android Studio.

It produces natural, readable documentation from your code -- not boilerplate. A template-driven NLP engine builds descriptions from declaration names, understands `async`/`Future`/`Stream` patterns, and recognises Flutter frameworks like `StatelessWidget`, `StatefulWidget`, `ChangeNotifier`, and `@freezed`.

**Non-destructive by default** -- regenerating never overwrites your hand-written documentation. The merge engine preserves your summaries and inline references while filling in missing parameter and return documentation.

Customise everything per project via a `.dartdocer.yaml` style sheet, or use the settings panel.

## Features

### Smart DartDoc Generation

DartDoc uses `///` triple-slash comments -- not `/** */` block comments. DartDoc-er generates idiomatic Dart documentation following the official [Effective Dart: Documentation](https://dart.dev/effective-dart/documentation) guidelines.

- **NLP-aware descriptions** -- verb maps, word splitting, and pluralisation produce natural sentences (`getUserById` -> *"Returns the user by id."*) instead of generic placeholders.
- **No `@param`/`@return` tags** -- Dart documents parameters using `[paramName]` inline bracket references in prose, not tag-based annotations. DartDoc-er follows this convention.
- **Prose return descriptions** -- returns are documented naturally: "Returns the..." rather than a separate `@return` tag.
- **Prose throws descriptions** -- exceptions are documented as "Throws [ExceptionType] if..." following Dart conventions.
- **Async/Future/Stream awareness** -- `async` functions, `Future<T>` and `Stream<T>` return types are documented automatically with appropriate context.
- **Flutter framework detection** -- recognises `StatelessWidget`, `StatefulWidget`, `State<T>`, `ChangeNotifier`, `ValueNotifier`, `InheritedWidget`, `@freezed`, `@immutable`, mixins, and extensions, and adds tailored notes.
- **Throws detection** -- scans function bodies for `throw` expressions and emits "Throws [ExceptionType]" documentation.
- **`@since` version stamp** -- optionally appends a version tag to every generated doc comment.
- **Cross-references** -- links override methods to their super declaration using `[SuperClass.method]` bracket syntax.
- **Usage examples** -- optionally appends a fenced code sample to function docs.

### Dart-Idiomatic Output

DartDoc-er generates documentation that follows Dart conventions:

```dart
/// Returns the user by id.
///
/// The [id] parameter identifies the user to retrieve.
///
/// Returns the matching [User], or `null` if not found.
///
/// Throws [NotFoundException] if the user does not exist.
Future<User?> getUserById(String id) async { ... }
```

Compare with KDoc (Kotlin) style -- DartDoc-er uses the correct format for each language:

| Aspect | KDoc (Kotlin) | DartDoc (Dart) |
|---|---|---|
| Comment style | `/** ... */` | `///` triple-slash |
| Parameters | `@param name description` | `[name]` inline in prose |
| Returns | `@return description` | "Returns the..." in prose |
| Throws | `@throws Type description` | "Throws [Type] if..." in prose |
| References | `[ClassName]` | `[ClassName]` |

### Non-Destructive Merge

Three policies for handling existing doc comments:

| Policy | Behaviour |
|---|---|
| **Merge** (default) | Keeps your summary and prose, adds only missing parameter references and return documentation |
| **Keep** | Leaves existing doc comments completely untouched |
| **Replace** | Regenerates the entire comment from scratch |

### Multiple Ways to Generate

| Action | Where | What it does |
|---|---|---|
| **Type `///` + Enter** | Editor | Generates DartDoc for the element at the cursor |
| **Create DartDoc for Element** | Generate menu (Alt+Insert) / Code menu | Single-element generation |
| **Create DartDocs for All Elements** | Generate menu / Code menu | Documents every qualifying element in the file |
| **Create DartDocs in Folder** | Right-click folder in Project view | Batch-generates across all `.dart` files with progress bar |
| **Remove DartDoc for Element** | Code menu | Removes the doc comment from the element at the cursor |
| **Remove DartDocs for All Elements** | Code menu / right-click | Removes every doc comment in the file |

### Missing DartDoc Inspection

An inspection (enabled by default) suggests adding documentation to undocumented declarations. Use **Alt+Enter** to generate the missing DartDoc via the quick-fix.

Configure or disable it in *Settings > Editor > Inspections > DartDoc-er > Missing DartDoc comment*.

### Project-Level Configuration

Create a `.dartdocer.yaml` at the project root to override settings per project. Generate a starter file with every option commented out:

**Tools > Create .dartdocer.yaml Config**

```yaml
style:
  appendName: true            # prepend a generated description sentence
  splitNames: true            # split camelCase/snake_case into readable phrases
  frameworkAware: true        # detect Flutter widgets / ChangeNotifier / freezed
  usageExample: false         # append a fenced sample call to function docs
  existingDoc: merge          # merge | keep | replace
  throwsDetection: false      # scan function bodies for throw and emit Throws docs
  sinceTag: false             # stamp docs with a version string
  sinceVersion: "1.0.0"       # version string for the since stamp
  seeReferences: false        # add cross-references for overrides
  verbMapping:                # extend/override the verb -> phrase map
    fetch: "Fetches the {noun} from the remote source"
    sync: "Synchronises the {noun}"

aspects:
  notes:                      # override framework note wording
    stateless-widget: "A stateless Flutter widget for {name}."
    stateful-widget: "A stateful Flutter widget for {name}."
    change-notifier: "Notifies listeners when {name} state changes."

templates:
  function:
    description: "{description}"
    param: "[{name}]"
    return: "Returns {description}"
  class:
    description: "{description}"
  property:
    description: "{description}"
```

### Settings

All options are available under **Settings > Tools > DartDoc-er** (independent from KDoc-er):

- **Generate DartDoc For** -- classes, functions, properties, extensions, mixins
- **Visibility** -- public, private (underscore-prefixed), overridden members
- **Description** -- prepend element name, split camelCase names
- **Framework Awareness** -- detect and annotate Flutter/Dart patterns
- **Throws Detection** -- scan for throw expressions
- **@since Tag** -- stamp with a version string
- **Cross-References** -- link overrides
- **Usage Example** -- append sample call to function docs
- **Existing Doc** -- Merge / Keep / Replace policy
- **Templates** -- override description and documentation templates with `{placeholder}` tokens

## Customization Guide

Every part of the generated DartDoc can be customized. Configuration is resolved in order: `.dartdocer.yaml` > IDE settings > built-in defaults.

### How the NLP Description Engine Works

DartDoc-er uses the same NLP engine as KDoc-er. It splits an identifier into words, matches the leading word against a **verb map**, and fills in the remaining words as the **noun**:

```
fetchUserProfile
  -> split
[fetch, user, profile]
  -> verb map lookup: "fetch" -> "Returns the {noun}"
  -> noun = remaining words joined: "user profile"
Returns the user profile
```

For classes and properties (no verb prefix), the name itself becomes the description:
```
UserRepository -> "User repository"
sessionToken   -> "Session token"
```

**Pluralisation** is automatic for collection return types -- if a function returns `List<User>`, the noun `user` becomes `users`:
```dart
List<User> getUsers() => ...;
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
# .dartdocer.yaml
style:
  verbMapping:
    fetch: "Fetches the {noun} from the remote source"
    navigate: "Navigates to the {noun} screen"
    inject: "Injects the {noun} dependency"
    dispose: "Disposes the {noun} and releases resources"
```

### Flutter Framework Notes

When **framework awareness** is enabled (the default), DartDoc-er detects Flutter patterns and appends contextual notes:

```dart
/// User profile screen.
/// Stateless widget -- renders UI based on its configuration without mutable state.
class UserProfileScreen extends StatelessWidget { ... }
```

#### Available aspect IDs and their default notes

| ID | Matches | Default note |
|---|---|---|
| `stateless-widget` | `StatelessWidget` subclasses | Stateless widget -- renders UI based on its configuration without mutable state. |
| `stateful-widget` | `StatefulWidget` subclasses | Stateful widget -- maintains mutable state via its companion `State` object. |
| `state` | `State<T>` subclasses | State object -- holds the mutable state for its associated `StatefulWidget`. |
| `change-notifier` | `ChangeNotifier` / `ValueNotifier` subclasses | Change notifier -- call `notifyListeners()` to update observers. |
| `inherited-widget` | `InheritedWidget` subclasses | Inherited widget -- provides data down the widget tree via `of(context)`. |
| `freezed` | Classes annotated with `@freezed` / `@unfreezed` | Freezed union/data class -- immutable with generated `copyWith`, `==`, and pattern matching. |
| `immutable` | Classes annotated with `@immutable` | Immutable class -- all fields must be final. |
| `mixin` | `mixin` declarations | Mixin -- provides reusable members to classes via `with`. |
| `extension` | `extension` declarations | Extension -- adds methods to an existing type without modifying it. |

### Controlling Framework Notes

```yaml
# .dartdocer.yaml
aspects:
  notes:
    stateless-widget: "Flutter UI component for {name}."
    freezed: ""    # suppress freezed notes
```

### Throws Detection

When enabled, DartDoc-er scans function bodies for `throw` expressions and documents them:

```yaml
# .dartdocer.yaml
style:
  throwsDetection: true
```

```dart
/// Requires the active user.
///
/// The [userId] identifies the user.
///
/// Returns the active [User].
///
/// Throws [NotFoundException] if the user does not exist.
/// Throws [StateError] if the user is not active.
User requireActive(String userId) { ... }
```

### Template Overrides

Override the structure of generated documentation using `{placeholder}` tokens:

```yaml
templates:
  function:
    description: "{description}"
    param: "[{name}]"
    return: "Returns {description}"
  class:
    description: "{description}"
  property:
    description: "{description}"
```

### Full Configuration Example

```yaml
# .dartdocer.yaml
style:
  appendName: true
  splitNames: true
  frameworkAware: true
  usageExample: false
  existingDoc: merge           # merge | keep | replace
  throwsDetection: true
  sinceTag: true
  sinceVersion: "2.0.0"
  seeReferences: true
  verbMapping:
    sync: "Synchronises the {noun} with the backend"
    navigate: "Navigates to the {noun} route"
    dispose: "Disposes the {noun} and releases resources"

aspects:
  notes:
    stateful-widget: "Manages mutable state for {name}."
    freezed: ""                # suppress freezed notes
    change-notifier: "Reactive state holder for {name}."

templates:
  function:
    description: "{description}"
    param: "[{name}]"
    return: "Returns {description}"
  class:
    description: "{description}"
  property:
    description: "{description}"
```
<!-- Plugin description end -->

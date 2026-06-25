# Doc-er — Documentation Generator Suite

A monorepo shipping **three IntelliJ plugins** that automatically generate documentation comments for Kotlin, Dart, and Rust. All three share a common NLP engine that builds natural, readable descriptions from your code — not boilerplate.

| Plugin | Language | Doc Style | Status |
|--------|----------|-----------|--------|
| [**KDoc-er**](kdocer/README.md) | Kotlin | `/** ... */` KDoc | [![Version](https://img.shields.io/jetbrains/plugin/v/14778-kdoc-er--kotlin-doc-generator.svg)](https://plugins.jetbrains.com/plugin/14778-kdoc-er--kotlin-doc-generator) |
| [**DartDoc-er**](dartdocer/README.md) | Dart | `///` DartDoc | Coming soon |
| [**RustDoc-er**](rustdocer/README.md) | Rust | `///` RustDoc | Coming soon |

## Shared Engine

All three plugins are powered by a shared `engine` module providing:

- **NLP-aware descriptions** — verb maps, word splitting, and pluralisation produce natural sentences (`getUserById` -> *"Returns the user by id"*) instead of generic placeholders.
- **Non-destructive merge** — regenerating never overwrites your hand-written documentation. Three policies: Merge (default), Keep, Replace.
- **Template engine** — customise every line of generated output with `{placeholder}` tokens.
- **Project-level YAML config** — override settings per project via `.kdocer.yaml`, `.dartdocer.yaml`, or `.rustdocer.yaml`.

Each plugin adds its own language-specific generators, framework/crate detection, async analysis, and an independent settings page under **Tools**.

## Current Version

**2026.1.1** — targets IntelliJ 2025.1+ (build 251, open-ended).

## Project Structure

```
engine/       Shared NLP, template, merge, and style modules
kdocer/       Kotlin KDoc generator plugin
dartdocer/    Dart DartDoc generator plugin
rustdocer/    Rust RustDoc generator plugin
```

## Build & Run

```bash
# Build all plugins (distributable ZIPs)
./gradlew buildAllPlugins

# Build a single plugin
./gradlew :kdocer:buildPlugin
./gradlew :dartdocer:buildPlugin
./gradlew :rustdocer:buildPlugin

# Run sandboxed IDE with a plugin loaded
./gradlew runKdocer
./gradlew runDartdocer
./gradlew runRustdocer

# Run all tests
./gradlew testAll

# Verify plugin structure
./gradlew verifyAllPlugins
```

## Requirements

- IntelliJ IDEA 2025.1 or later (any edition)
- **KDoc-er**: Kotlin plugin installed
- **DartDoc-er**: Dart plugin installed
- **RustDoc-er**: Rust plugin (com.jetbrains.rust) installed

## Author

<a href="https://github.com/godwinjk">
  <img src="https://github.com/godwinjk.png" width="80" height="80" style="border-radius:50%;" alt="godwinjk"/>
  <br/>
  <sub><b>Godwin Joseph</b></sub>
</a>

## Contributors

<a href="https://github.com/isaacy2012">
  <img src="https://github.com/isaacy2012.png" width="60" height="60" style="border-radius:50%;" alt="isaacy2012"/>
</a>
&nbsp;
<a href="https://github.com/CJCrafter">
  <img src="https://github.com/CJCrafter.png" width="60" height="60" style="border-radius:50%;" alt="CJCrafter"/>
</a>

## Contributing

All contributions are welcome — whether it's a bug fix, a new feature, a typo correction, or even a comment improving code readability. No contribution is too small.

1. Fork the repository
2. Create your branch (`git checkout -b my-change`)
3. Make your changes
4. Run `./gradlew buildAllPlugins` to make sure everything compiles
5. Open a pull request

## Donate

If you find these plugins useful, consider supporting their development:

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://paypal.me/godwinj)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/S6S0176OVQ)

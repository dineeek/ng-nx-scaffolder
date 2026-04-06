# Next Version

## Playwright E2E Test Generator
- Scaffold Playwright test files with Page Object Model pattern
- Generate locator files, page classes, and test case functions
- Dialog for feature name, test case name, and fixture type
- Live templates: `tstep` (test.step block), `tcase` (test case function with fixtures)
- Action code and templates are implemented but commented out — re-enable in `plugin.xml` and `PluginSettingsConfigurable.kt`

## Additional Live Templates
- `ngrxstore` — full SignalStore skeleton (withState, withMethods, withComputed)
- `httpget` / `httppost` — HttpClient method with return type and URL
- `cmp` — standalone component decorator skeleton
- `effect` — Angular effect() with cleanup

## Release Automation
- GitHub Actions workflow to publish to JetBrains Marketplace on tag push
- Dependabot config for Gradle and plugin dependency updates
- CHANGELOG.md for tracking releases

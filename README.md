# ng-nx-scaffolder

IntelliJ / WebStorm plugin for scaffolding Angular and Nx libraries. Generates complete file structures matching standard Nx workspace generator conventions.

## Features

### Nx Library Generators

Access via **Right-click > New > Angular/Nx** or **File > New > Angular/Nx**.

#### Feature Library

Generates a full feature lib with container component and optional layers:

| Option | Default | What it generates |
|-|-|-|
| Store | on | `store/{name}.store.ts`, `{name}.state.ts`, `{name}.store.spec.ts` |
| Facade | off | `facade/{name}-facade.service.ts`, spec |
| Form | off | `form/{name}-form.service.ts`, `{name}-form.model.ts`, spec |
| Routing | off | `{name}-routing.routes.ts` |
| Dialog | off | Dialog variant of container with `MAT_DIALOG_DATA`, dialog model interfaces |

Always generates: container component (`.ts`, `.html`, `.scss`, `.spec.ts`), mapper, models folder, barrel `index.ts`.

```
src/
├── index.ts
└── lib/
    ├── container/
    │   ├── {name}-container.component.ts
    │   ├── {name}-container.component.html
    │   ├── {name}-container.component.scss
    │   └── {name}-container.component.spec.ts
    ├── store/                          # if store enabled
    │   ├── {name}.store.ts
    │   ├── {name}.state.ts
    │   └── {name}.store.spec.ts
    ├── facade/                         # if facade enabled
    │   ├── {name}-facade.service.ts
    │   └── {name}-facade.service.spec.ts
    ├── form/                           # if form enabled
    │   ├── {name}-form.service.ts
    │   ├── {name}-form.model.ts
    │   └── {name}-form.service.spec.ts
    ├── mapper/
    │   ├── {name}.mapper.ts
    │   └── {name}.mapper.spec.ts
    ├── models/
    │   └── example.model.ts
    └── {name}-routing.routes.ts        # if routing enabled
```

#### Data-Access Library

```
src/
├── index.ts
└── lib/services/
    ├── {name}.service.ts               # @Injectable + HttpClient
    └── {name}.service.spec.ts
```

#### Model Library

```
src/
├── index.ts
└── lib/models/
    └── {name}.model.ts                 # empty I{Name} interface
```

#### UI Library

```
src/
├── index.ts
└── lib/example/
    ├── example.component.ts            # standalone, OnPush
    ├── example.component.html
    └── example.component.scss
```

#### Util Library

```
src/
├── index.ts
└── lib/{name}/
    ├── {name}.util.ts
    └── {name}.util.spec.ts
```

### Playwright E2E Test Generator

Generates a complete feature test folder with Page Object Model:

```
feature-{name}/
├── cycles/
│   └── all.spec.ts
├── tests/
│   ├── {test-name}.ts                  # async function with test.step()
│   └── index.ts
└── pages/
    ├── {name}.locators.ts
    ├── {name}.page.ts
    └── index.ts
```

### Live Templates

Type the abbreviation in any `.ts` file and press Tab:

| Abbreviation | Expansion |
|-|-|
| `rxm` | `rxMethod<T>(pipe(switchMap(...)))` |
| `sig` | `signal<T>(initialValue)` |
| `comp` | `computed(() => ...)` |
| `ps` | `patchState(store, { ... })` |
| `inj` | `private readonly name = inject(Service)` |
| `wstate` | `withState<IState>(INITIAL_STATE)` |
| `tstep` | `await test.step('...', async () => { })` |
| `tcase` | Playwright test case function with Fixtures |

## Settings

**Settings > Tools > Angular/Nx Scaffolder**

| Setting | Default | Description |
|-|-|-|
| Selector prefix | `app` | Component selector prefix (e.g. `sp-feature`, `ef`) |
| Default domain | _(empty)_ | Pre-fills the domain field in dialogs |
| Playwright fixture type | `Fixtures` | Type name for E2E test fixtures |
| Playwright domain prefix | _(empty)_ | Tag prefix in test describe blocks |

## Requirements

- IntelliJ IDEA Ultimate 2024.1+ or WebStorm 2024.1+
- Java 17+

## Development

### Prerequisites

- JDK 17+ (the project uses Gradle wrapper, no Gradle install needed)

### Build

```bash
./gradlew build
```

### Run in sandbox IDE

```bash
./gradlew runIde
```

This launches a sandboxed IntelliJ instance with the plugin loaded. Use it to test all generators manually.

### Run tests

```bash
./gradlew test
```

### Build distributable

```bash
./gradlew buildPlugin
```

Produces `build/distributions/ng-nx-scaffolder-{version}.zip`.

## Installation

### From disk

1. Build the plugin: `./gradlew buildPlugin`
2. In IntelliJ/WebStorm: **Settings > Plugins > Gear icon > Install Plugin from Disk**
3. Select `build/distributions/ng-nx-scaffolder-{version}.zip`
4. Restart the IDE

### From JetBrains Marketplace (future)

```bash
./gradlew publishPlugin
```

Requires `ORG_GRADLE_PROJECT_intellijPublishToken` environment variable with a valid [JetBrains Marketplace](https://plugins.jetbrains.com/) token.

## Project Structure

```
src/main/
├── kotlin/com/ngscaffolder/
│   ├── actions/          # Menu actions (6)
│   ├── dialogs/          # Input dialogs (3)
│   ├── generators/       # File generation logic (6)
│   ├── settings/         # Plugin configuration persistence
│   └── util/             # NamingUtils (kebab/Pascal/camel case)
└── resources/
    ├── META-INF/plugin.xml
    ├── fileTemplates/internal/   # 28 Velocity templates
    └── liveTemplates/AngularNx.xml
```

## License

Private repository. All rights reserved.

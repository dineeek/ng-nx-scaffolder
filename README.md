# ng-nx-scaffolder

IntelliJ / WebStorm plugin for scaffolding Angular and Nx libraries. Generates complete file structures following standard Angular and Nx conventions.

## Features

### Nx Library Generators

Access via **Right-click > New > Angular/Nx** or **File > New > Angular/Nx**.

#### Feature Library

Generates a full feature lib with a container component and optional architectural layers:

| Option | Default | What it generates |
|-|-|-|
| Store | on | `store/{name}.store.ts`, `{name}.state.ts`, `{name}.store.spec.ts` — NgRx SignalStore |
| Facade | off | `facade/{name}-facade.service.ts` + spec — abstraction layer over store |
| Form | off | `form/{name}-form.service.ts`, `{name}-form.model.ts` + spec — typed reactive form |
| Routing | off | `{name}.routes.ts` — Angular route config |
| Dialog | off | Dialog variant of container with `MAT_DIALOG_DATA` + response/data interfaces |

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
    └── {name}.routes.ts                # if routing enabled
```

#### Data-Access Library

Generates an Angular service with `HttpClient` injection:

```
src/
├── index.ts
└── lib/services/
    ├── {name}.service.ts
    └── {name}.service.spec.ts
```

#### Model Library

Generates a TypeScript interface file:

```
src/
├── index.ts
└── lib/models/
    └── {name}.model.ts
```

#### UI Library

Generates a standalone Angular component:

```
src/
├── index.ts
└── lib/example/
    ├── example.component.ts
    ├── example.component.html
    └── example.component.scss
```

#### Util Library

Generates a utility file with spec:

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
│   ├── {test-name}.ts
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
| Selector prefix | `app` | Component selector prefix |
| Playwright fixture type | `Fixtures` | Type name for E2E test fixtures |
| Playwright domain prefix | _(empty)_ | Tag prefix in test describe blocks |

## Requirements

- IntelliJ IDEA Ultimate 2024.1+ or WebStorm 2024.1+
- Java 17+

## Development

### Build

```bash
./gradlew build
```

### Run in sandbox IDE

```bash
./gradlew runIde
```

Launches a sandboxed IntelliJ instance with the plugin loaded for manual testing.

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

1. `./gradlew buildPlugin`
2. **Settings > Plugins > Gear icon > Install Plugin from Disk**
3. Select `build/distributions/ng-nx-scaffolder-{version}.zip`
4. Restart the IDE

### From JetBrains Marketplace

```bash
./gradlew publishPlugin
```

Requires `ORG_GRADLE_PROJECT_intellijPublishToken` environment variable.

## Project Structure

```
src/main/
├── kotlin/com/ngscaffolder/
│   ├── actions/          # Menu actions
│   ├── dialogs/          # Input dialogs
│   ├── generators/       # File generation logic
│   ├── settings/         # Plugin configuration
│   └── util/             # NamingUtils (kebab/Pascal/camel case)
└── resources/
    ├── META-INF/plugin.xml
    ├── fileTemplates/internal/   # Velocity templates
    └── liveTemplates/AngularNx.xml
```

## License

MIT

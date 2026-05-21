# South Neuhof Client Application Template

This is a South Neuhof client application monorepo template. It starts with one web app and one client-owned shared package.

## Structure

- `apps/web` - Vue web client app
- `apps/api` - reserved for a future backend app
- `apps/mobile` - reserved for a future mobile app
- `packages/data-model` - client-owned shared data model package

Framework packages are installed from GitHub Packages:

- `@southneuhof/is-data-model`
- `@southneuhof/apostle`
- `@southneuhof/is-vue-framework`

## GitHub Packages Auth

Create a GitHub token with package read access, then install dependencies:

```bash
export GITHUB_TOKEN=ghp_xxx
pnpm install
```

The root `.npmrc` points the `@southneuhof` scope at GitHub Packages.

## Commands

```bash
pnpm install
pnpm dev
pnpm type-check
pnpm test
pnpm build
```

## Framework Updates

Update framework packages:

```bash
pnpm add \
  @southneuhof/is-data-model@NEW_VERSION \
  @southneuhof/apostle@NEW_VERSION \
  @southneuhof/is-vue-framework@NEW_VERSION \
  --filter @client/web

pnpm add \
  @southneuhof/is-data-model@NEW_VERSION \
  --filter @client/data-model
```

Rollback a framework package:

```bash
pnpm add @southneuhof/is-vue-framework@PREVIOUS_VERSION --filter @client/web
```

List published versions:

```bash
npm view @southneuhof/is-vue-framework versions --registry=https://npm.pkg.github.com
```

## Customization Checklist

- Update app branding, routes, navigation, and theme tokens in `apps/web`.
- Replace placeholder environment values in `apps/web/.env`.
- Add or edit client models in `packages/data-model/src/models`.
- Pin framework packages to the versions approved for the client project.
- Add future apps under `apps/api` or `apps/mobile` when needed.

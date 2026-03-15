# Tencent CVM UI

A modern Web UI for Tencent Cloud CVM management, including instance purchase, instance operations, and billing overview.

![Tencent CVM UI](docs/images/ui-screenshot-2026-03-15.png)

## Features

- Instance purchase workflow
- Region / zone / instance type / image selection
- Instance lifecycle management
  - Start
  - Stop
  - Reboot
  - Destroy
  - Renew
  - Rename
- Billing and balance overview
- Import / export templates
- Frontend + backend integrated project structure

## Tech Stack

- **Frontend:** Vue 3, Vite, Element Plus
- **Backend:** Spring Boot, Maven
- **Language:** Java, JavaScript
- **Cloud Integration:** Tencent Cloud CVM / Billing related APIs

## Project Structure

```text
frontend-elementplus/   Frontend application
src/main/java/          Backend source code
src/main/resources/     Config and static resources
docs/                   Project documentation
scripts/                Helper scripts
```

## Quick Start

### Frontend

```bash
cd frontend-elementplus
npm install
npm run dev
```

### Backend

```bash
mvn spring-boot:run
```

## Deployment

For deployment details, see:

- [Deployment Guide](docs/deployment.md)

## Releases

You can publish a GitHub release in two common ways.

### Option 1: Create a tag and push it

```bash
git tag v1.0.0
git push origin v1.0.0
```

Then create a Release in GitHub based on that tag.

### Option 2: Use GitHub CLI

```bash
gh release create v1.0.0 --title "v1.0.0" --notes "First public release"
```

If the tag does not exist yet, `gh` can create it for you.

## Notes

This repository is a GitHub-ready sanitized edition.
Environment-specific deployment details, private infrastructure data, and sensitive internal configuration have been removed.

## License

Private / Internal Use

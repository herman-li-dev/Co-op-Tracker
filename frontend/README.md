# Co-op Application Tracker Frontend

React frontend for the Co-op Application Tracker. It lets users register and sign in, manage job applications, track application-status history and deadlines, and view progress analytics. Administrators can also manage user accounts.

## Technology

- React 17 and TypeScript
- Umi 3
- Ant Design and Ant Design Pro
- Ant Design Charts
- Jest / Umi Test and Playwright

## Prerequisites

- Node.js 20 or later
- npm
- The backend service running at `http://localhost:8080` for local development

## Local Development

Install dependencies:

```bash
npm ci
```

Start the development server:

```bash
npm start
```

Open `http://localhost:8000`. The development proxy forwards `/api/*` requests to the local backend.

## Quality Checks

Run unit tests:

```bash
npm test -- --runInBand
```

Run the browser end-to-end test against a disposable test database:

```bash
npm run playwright:install
npm run test:e2e
```

The Playwright configuration starts the backend and frontend automatically. Maven must be available in `PATH`, and the database environment variables must point to an initialized test database.

Run linting and type checks:

```bash
npm run lint
```

Create a production build:

```bash
npm run build
```

The generated files are written to `dist/`.

## Related Documentation

See the repository-level [README](../README.md) for full-stack setup, database configuration, deployment, backup, and restore instructions.

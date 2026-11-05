# Postnaut frontend

## Run it

You need Node 22 or newer, and the backend running on `localhost:8080`.

```bash
npm install
npm run dev
```

Open http://localhost:5173. To use a different backend address, copy `.env.example` to `.env` and change `BACKEND_URL`.

`npm run build` checks the types and creates a production build in `dist/`.

## Where things live

| Path | What's there |
| - | - |
| `src/App.tsx` | The list of pages and their URLs |
| `src/pages/` | One file per page |
| `src/components/` | Pieces shared between pages, like `CloudCard` |
| `src/hooks/` | Reusable logic for the session and the feed |
| `src/api/` | Backend calls and their TypeScript types |
| `src/index.css` | Colors, fonts, and the cloud and button styles |

## Common changes

- **New endpoint:** add its types and function to a file in `src/api/`.
- **New page:** add a file to `src/pages/`, add its route to `src/App.tsx`, and add it to `LINKS` in `src/components/AppLayout.tsx` if it needs a nav item.
- **New colors:** change the values under `@theme` in `src/index.css`.
- **Backend error text:** add the error code to `messages` in `src/api/client.ts`.

import { Navigate, Outlet } from "react-router";
import { useSession } from "../hooks/useSession";
import { errorMessage } from "../api/client";

function SessionStatus({ error, retry }: { error: unknown; retry: () => void }) {
  return (
    <main className="grid min-h-dvh place-items-center p-4 text-center">
      {error ? (
        <div className="panel grid justify-items-center gap-4 p-6">
          <p role="alert">{errorMessage(error)}</p>
          <button className="button" onClick={retry}>Try again</button>
        </div>
      ) : (
        <p role="status" className="fade-in-late text-haze">Loading…</p>
      )}
    </main>
  );
}

// Pages inside this gate need a logged-in user.
export function RequireSession() {
  const session = useSession();
  if (session.data === undefined) return <SessionStatus error={session.error} retry={session.refetch} />;
  return session.data ? <Outlet /> : <Navigate to="/login" replace />;
}

// Login and register send logged-in users straight to the feed.
export function RequireNoSession() {
  const session = useSession();
  if (session.data === undefined) return <SessionStatus error={session.error} retry={session.refetch} />;
  return session.data ? <Navigate to="/" replace /> : <Outlet />;
}

import type { ReactNode } from "react";

// The two-sided layout shared by the login and register pages.
export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <main className="mx-auto grid min-h-dvh max-w-5xl items-center gap-8 px-4 py-10 md:grid-cols-2 md:gap-16">
      <div className="grid justify-items-center gap-5 text-center">
        <img src="/logo.png" alt="Postnaut" width={560} height={613} className="h-auto w-48 md:w-80" />
        <p className="max-w-sm text-lg text-balance text-mist md:text-xl">Say the thing nobody asked for. Let it float away.</p>
      </div>
      <div className="panel p-6 md:p-8">{children}</div>
    </main>
  );
}

export function TextField({
  label,
  error,
  ...input
}: { label: string; error?: string } & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className="grid gap-1 text-sm font-semibold">
      {label}
      <input className="field font-normal text-mist" aria-invalid={Boolean(error)} {...input} />
      {error && <span className="text-sm font-normal text-blush">{error}</span>}
    </label>
  );
}

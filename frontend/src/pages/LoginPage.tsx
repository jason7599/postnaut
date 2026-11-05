import { useMutation } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { Link } from "react-router";
import { login } from "../api/auth";
import { errorMessage } from "../api/client";
import { AuthShell, TextField } from "../components/AuthShell";
import { markLoggedIn } from "../hooks/useSession";

export function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const mutation = useMutation({ mutationFn: login, onSuccess: markLoggedIn });

  function submit(event: FormEvent) {
    event.preventDefault();
    mutation.mutate({ username, password });
  }

  return (
    <AuthShell>
      <form onSubmit={submit} className="grid gap-4">
        <h1 className="text-2xl font-bold">Log in</h1>
        <TextField label="Username" autoComplete="username" required value={username} onChange={(e) => setUsername(e.target.value)} />
        <TextField label="Password" type="password" autoComplete="current-password" required value={password} onChange={(e) => setPassword(e.target.value)} />
        {mutation.isError && <p role="alert" className="text-blush">{errorMessage(mutation.error)}</p>}
        <button className="button mt-1" disabled={mutation.isPending}>
          {mutation.isPending ? "Logging in…" : "Log in"}
        </button>
        <p className="text-center text-sm text-haze">
          New here? <Link to="/register" className="font-semibold text-lilac underline">Create an account</Link>
        </p>
      </form>
    </AuthShell>
  );
}

import { useMutation } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { Link } from "react-router";
import { register } from "../api/auth";
import { errorMessage } from "../api/client";
import { AuthShell, TextField } from "../components/AuthShell";
import { markLoggedIn } from "../hooks/useSession";

// These match the rules in the Auth API doc.
function validate(username: string, password: string, displayName: string) {
  return {
    username: /^[a-zA-Z0-9]{3,32}$/.test(username) ? undefined : "Use 3 to 32 letters and digits.",
    password: password.length >= 6 && password.length <= 64 ? undefined : "Use 6 to 64 characters.",
    displayName: displayName.length >= 3 && displayName.length <= 32 ? undefined : "Use 3 to 32 characters.",
  };
}

export function RegisterPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [showErrors, setShowErrors] = useState(false);
  const mutation = useMutation({ mutationFn: register, onSuccess: markLoggedIn });

  const errors = validate(username, password, displayName);
  const visibleErrors: Partial<typeof errors> = showErrors ? errors : {};

  function submit(event: FormEvent) {
    event.preventDefault();
    setShowErrors(true);
    if (Object.values(errors).some(Boolean)) return;
    mutation.mutate({ username, password, displayName });
  }

  return (
    <AuthShell>
      <form onSubmit={submit} noValidate className="grid gap-4">
        <h1 className="text-2xl font-bold">Create an account</h1>
        <TextField label="Username" autoComplete="username" value={username} error={visibleErrors.username} onChange={(e) => setUsername(e.target.value)} />
        <TextField label="Display name" autoComplete="nickname" value={displayName} error={visibleErrors.displayName} onChange={(e) => setDisplayName(e.target.value)} />
        <TextField label="Password" type="password" autoComplete="new-password" value={password} error={visibleErrors.password} onChange={(e) => setPassword(e.target.value)} />
        {mutation.isError && <p role="alert" className="text-blush">{errorMessage(mutation.error)}</p>}
        <button className="button mt-1" disabled={mutation.isPending}>
          {mutation.isPending ? "Creating account…" : "Create account"}
        </button>
        <p className="text-center text-sm text-haze">
          Already have one? <Link to="/login" className="font-semibold text-lilac underline">Log in</Link>
        </p>
      </form>
    </AuthShell>
  );
}

import { Link } from "react-router";

export function Wordmark() {
  return (
    <Link to="/" className="rounded-lg">
      <img src="/wordmark.png" alt="Postnaut" width={640} height={154} className="h-9 w-auto" />
    </Link>
  );
}

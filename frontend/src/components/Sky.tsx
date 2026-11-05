// The fixed background behind every page.
function DistantCloud({ className }: { className: string }) {
  return (
    <svg viewBox="0 0 200 80" className={`absolute fill-horizon blur-sm ${className}`} aria-hidden>
      <circle cx="52" cy="52" r="28" />
      <circle cx="96" cy="36" r="36" />
      <circle cx="144" cy="52" r="28" />
      <rect x="52" y="52" width="92" height="28" />
    </svg>
  );
}

export function Sky() {
  return (
    <div className="fixed inset-0 -z-10 overflow-hidden bg-linear-to-b from-night via-dusk via-65% to-horizon">
      <DistantCloud className="-left-16 top-[30%] w-56 opacity-20 md:w-96" />
      <DistantCloud className="-right-20 top-[52%] w-64 opacity-25 md:w-[30rem]" />
      <DistantCloud className="-left-24 bottom-[4%] w-72 opacity-50 md:w-[36rem]" />
      <DistantCloud className="right-[12%] -bottom-8 w-72 opacity-60" />
    </div>
  );
}

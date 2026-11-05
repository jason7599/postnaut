import type { ReactNode } from "react";

// Position and size of each bump, as a percentage of the card's width.
const PUFFS = [
  { left: "10%", top: "-1.1rem", width: "20%" },
  { left: "27%", top: "-2rem", width: "27%" },
  { left: "51%", top: "-1.25rem", width: "22%" },
  { left: "70%", top: "-0.6rem", width: "16%" },
];

const LOW_PUFFS = [
  { left: "20%", bottom: "-1rem", width: "24%" },
  { left: "48%", bottom: "-1.4rem", width: "28%" },
];

export function CloudCard({ children, className = "" }: { children: ReactNode; className?: string }) {
  return (
    <div className="cloud">
      {PUFFS.map((puff, index) => (
        <span key={index} className="puff" style={puff} aria-hidden />
      ))}
      {LOW_PUFFS.map((puff, index) => (
        <span key={index} className="puff puff-low" style={puff} aria-hidden />
      ))}
      <div className={`cloud-body ${className}`}>{children}</div>
    </div>
  );
}

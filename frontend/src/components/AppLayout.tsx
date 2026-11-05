import { Cloud, Feather, Inbox, type LucideIcon } from "lucide-react";
import { NavLink, Outlet } from "react-router";
import { Wordmark } from "./Wordmark";

const LINKS: { to: string; label: string; icon: LucideIcon }[] = [
  { to: "/", label: "Feed", icon: Cloud },
  { to: "/write", label: "Write", icon: Feather },
  { to: "/mine", label: "My posts", icon: Inbox },
];

function NavLinks({ iconSize }: { iconSize: number }) {
  return LINKS.map(({ to, label, icon: Icon }) => (
    <NavLink
      key={to}
      to={to}
      end
      className={({ isActive }) =>
        `flex min-h-11 flex-1 items-center justify-center gap-2 rounded-full px-4 font-semibold max-md:flex-col max-md:gap-0 max-md:text-xs md:flex-none ${
          isActive ? "bg-lilac text-ink" : "text-haze hover:text-mist"
        }`
      }
    >
      <Icon size={iconSize} aria-hidden />
      {label}
    </NavLink>
  ));
}

export function AppLayout() {
  return (
    <div className="flex min-h-dvh flex-col overflow-x-clip">
      <header className="mx-auto flex w-full max-w-5xl items-center justify-between px-4 pt-4 md:pt-6">
        <Wordmark />
        <nav aria-label="Main" className="panel hidden gap-1 rounded-full! p-1 md:flex">
          <NavLinks iconSize={18} />
        </nav>
      </header>

      <div className="mx-auto flex w-full max-w-5xl flex-1 flex-col px-4 pt-6 pb-28 md:pb-10">
        <Outlet />
      </div>

      <nav
        aria-label="Main"
        className="panel fixed inset-x-3 bottom-[max(0.75rem,env(safe-area-inset-bottom))] z-10 flex gap-1 rounded-full! p-1 md:hidden"
      >
        <NavLinks iconSize={20} />
      </nav>
    </div>
  );
}

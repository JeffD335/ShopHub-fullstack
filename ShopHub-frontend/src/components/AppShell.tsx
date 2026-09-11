import {
  Home,
  Newspaper,
  PlusSquare,
  Search,
  Store,
  UserRound
} from "lucide-react";
import { NavLink, useLocation } from "react-router-dom";
import type { ReactNode } from "react";

const navItems = [
  { to: "/", label: "Home", icon: Home },
  { to: "/discover", label: "Shops", icon: Store },
  { to: "/feed", label: "Feed", icon: Newspaper },
  { to: "/posts/new", label: "Post", icon: PlusSquare },
  { to: "/profile", label: "Me", icon: UserRound }
];

const titles: Record<string, string> = {
  "/": "ShopHub",
  "/discover": "Discover",
  "/feed": "Community Feed",
  "/posts/new": "Create Post",
  "/profile": "Profile"
};

export default function AppShell({ children }: { children: ReactNode }) {
  const location = useLocation();
  const title = titles[location.pathname] || "ShopHub";

  return (
    <div className="app-shell">
      <aside className="side-rail">
        <div className="brand-lockup">
          <span className="brand-mark">S</span>
          <span>ShopHub</span>
        </div>
        <nav className="side-nav" aria-label="Primary">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === "/"}>
              <item.icon size={18} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="app-main">
        <header className="top-bar">
          <div>
            <p className="eyebrow">Local deals and reviews</p>
            <h1>{title}</h1>
          </div>
          <NavLink className="search-shortcut" to="/discover" title="Search shops">
            <Search size={19} />
          </NavLink>
        </header>

        <main>{children}</main>

        <nav className="bottom-nav" aria-label="Primary">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === "/"}>
              <item.icon size={20} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>
    </div>
  );
}

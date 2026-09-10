import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  Dumbbell,
  GlassWater,
  HeartHandshake,
  MapPinned,
  PartyPopper,
  Scissors,
  Search,
  Sparkles,
  Store,
  Utensils,
  Waves
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { Link } from "react-router-dom";
import { shophubApi } from "../api/shophub";
import type { Shop, ShopType } from "../api/types";
import ShopCard from "../components/ShopCard";
import { EmptyState, InlineError, LoadingState } from "../components/Status";
import { categoryMeta } from "../data/localization";

const categoryIcons: Record<number, LucideIcon> = {
  1: Utensils,
  2: Sparkles,
  3: Scissors,
  4: Dumbbell,
  5: HeartHandshake,
  6: Waves,
  7: PartyPopper,
  8: GlassWater,
  9: Store,
  10: Sparkles
};

export default function HomePage({ discoverMode = false }: { discoverMode?: boolean }) {
  const [types, setTypes] = useState<ShopType[]>([]);
  const [activeType, setActiveType] = useState<number | null>(null);
  const [shops, setShops] = useState<Shop[]>([]);
  const [query, setQuery] = useState("");
  const [nearby, setNearby] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    async function loadTypes() {
      setLoading(true);
      setError(null);
      try {
        const data = await shophubApi.shopTypes();
        if (!mounted) return;
        setTypes(data);
        setActiveType(data[0]?.id ?? 1);
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : "Could not load categories");
        }
      } finally {
        if (mounted) setLoading(false);
      }
    }
    void loadTypes();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    if (!activeType || query) return;
    const typeId = activeType;
    let mounted = true;
    async function loadShops() {
      setLoading(true);
      setError(null);
      try {
        const data = await shophubApi.shopsByType(typeId, 1, nearby);
        if (mounted) setShops(data);
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : "Could not load shops");
        }
      } finally {
        if (mounted) setLoading(false);
      }
    }
    void loadShops();
    return () => {
      mounted = false;
    };
  }, [activeType, nearby, query]);

  const featured = useMemo(() => shops.slice(0, 3), [shops]);

  const submitSearch = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!query.trim()) {
      const typeId = activeType;
      if (typeId) {
        setShops(await shophubApi.shopsByType(typeId, 1, nearby));
      }
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setShops(await shophubApi.shopsByName(query.trim()));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Search failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-stack">
      {!discoverMode ? (
        <section className="home-hero">
          <div>
            <p className="eyebrow">Interview-ready full-stack demo</p>
            <h2>Browse local merchants, social reviews, and Redis-backed deals.</h2>
          </div>
          <Link className="primary-button compact" to="/feed">
            Open feed
          </Link>
        </section>
      ) : null}

      <form className="search-panel" onSubmit={submitSearch}>
        <div className="input-with-icon">
          <Search size={18} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search shops"
          />
        </div>
        <button className="secondary-button" type="submit">
          Search
        </button>
      </form>

      <section className="category-strip" aria-label="Shop categories">
        {types.map((type) => {
          const meta = categoryMeta[type.id] || {
            label: type.name,
            accent: "accent-teal"
          };
          const Icon = categoryIcons[type.id] || Store;
          return (
            <button
              className={
                activeType === type.id
                  ? `category-chip selected ${meta.accent}`
                  : `category-chip ${meta.accent}`
              }
              key={type.id}
              type="button"
              onClick={() => {
                setQuery("");
                setActiveType(type.id);
              }}
            >
              <Icon size={18} />
              <span>{meta.label}</span>
            </button>
          );
        })}
      </section>

      <section className="section-heading">
        <div>
          <p className="eyebrow">{nearby ? "Geo query" : "Merchant catalog"}</p>
          <h2>{query ? "Search results" : "Explore shops"}</h2>
        </div>
        <button
          className={nearby ? "chip active" : "chip"}
          type="button"
          onClick={() => setNearby((value) => !value)}
        >
          <MapPinned size={16} />
          Nearby sample
        </button>
      </section>

      <InlineError message={error} />
      {loading ? <LoadingState label="Loading shops" /> : null}
      {!loading && shops.length === 0 ? (
        <EmptyState
          title="No shops found"
          detail="Try another category or clear the search."
        />
      ) : null}

      {!loading && featured.length > 0 && !discoverMode ? (
        <section className="featured-grid">
          {featured.map((shop) => (
            <ShopCard key={shop.id} shop={shop} />
          ))}
        </section>
      ) : null}

      {!loading ? (
        <section className="list-stack">
          {shops.map((shop) => (
            <ShopCard key={shop.id} shop={shop} />
          ))}
        </section>
      ) : null}
    </div>
  );
}

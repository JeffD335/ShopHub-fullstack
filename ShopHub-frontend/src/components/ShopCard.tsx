import { MapPin, Star } from "lucide-react";
import { Link } from "react-router-dom";
import type { Shop } from "../api/types";
import { displayShop, firstImage, score } from "../utils/format";

export default function ShopCard({ shop }: { shop: Shop }) {
  const copy = displayShop(shop);

  return (
    <Link className="shop-card" to={`/shops/${shop.id}`}>
      <img src={firstImage(shop.images)} alt={copy.name} />
      <div className="shop-card-body">
        <div className="shop-title-row">
          <h3>{copy.name}</h3>
          <span className="rating-pill">
            <Star size={14} fill="currentColor" />
            {score(shop.score)}
          </span>
        </div>
        <p>{copy.tagline}</p>
        <div className="meta-row">
          <span>
            <MapPin size={14} />
            {shop.area || "Local area"}
          </span>
          <span>{shop.avgPrice ? `$${shop.avgPrice} avg` : "Deal friendly"}</span>
          {shop.distance ? <span>{shop.distance.toFixed(1)} km</span> : null}
        </div>
      </div>
    </Link>
  );
}

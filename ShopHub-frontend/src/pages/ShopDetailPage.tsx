import { useEffect, useState } from "react";
import { Clock, MapPin, MessageCircle, Star } from "lucide-react";
import { useParams } from "react-router-dom";
import { shophubApi } from "../api/shophub";
import type { Shop, Voucher } from "../api/types";
import PageHeader from "../components/PageHeader";
import { EmptyState, InlineError, LoadingState } from "../components/Status";
import VoucherCard from "../components/VoucherCard";
import { displayShop, firstImage, score, splitImages } from "../utils/format";

export default function ShopDetailPage() {
  const { id } = useParams();
  const shopId = Number(id);
  const [shop, setShop] = useState<Shop | null>(null);
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [loading, setLoading] = useState(true);
  const [ordering, setOrdering] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const [shopData, voucherData] = await Promise.all([
          shophubApi.shop(shopId),
          shophubApi.vouchers(shopId)
        ]);
        if (!mounted) return;
        setShop(shopData);
        setVouchers(voucherData);
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : "Could not load shop");
        }
      } finally {
        if (mounted) setLoading(false);
      }
    }
    void load();
    return () => {
      mounted = false;
    };
  }, [shopId]);

  const claimVoucher = async (voucher: Voucher) => {
    if (voucher.type !== 1) return;
    setOrdering(voucher.id);
    setMessage(null);
    setError(null);
    try {
      const orderId = await shophubApi.seckill(voucher.id);
      setMessage(`Flash-sale request accepted. Order #${orderId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not place order");
    } finally {
      setOrdering(null);
    }
  };

  if (loading) return <LoadingState label="Loading shop" />;
  if (!shop) {
    return <EmptyState title="Shop not found" detail="The backend returned no shop." />;
  }

  const copy = displayShop(shop);
  const gallery = splitImages(shop.images).slice(0, 4);

  return (
    <div className="page-stack">
      <PageHeader eyebrow="Merchant detail" title={copy.name} back />

      <section className="detail-hero">
        <img src={firstImage(shop.images)} alt={copy.name} />
        <div className="detail-hero-content">
          <h2>{copy.name}</h2>
          <p>{copy.tagline}</p>
          <div className="meta-row">
            <span>
              <Star size={15} fill="currentColor" />
              {score(shop.score)}
            </span>
            <span>
              <MessageCircle size={15} />
              {shop.comments || 0} reviews
            </span>
            <span>{shop.sold || 0} sold</span>
          </div>
        </div>
      </section>

      {gallery.length > 1 ? (
        <section className="media-strip" aria-label="Shop photos">
          {gallery.map((url) => (
            <img key={url} src={url.replace("http://", "https://")} alt={copy.name} />
          ))}
        </section>
      ) : null}

      <section className="info-band">
        <div>
          <MapPin size={18} />
          <span>{shop.address || "Address unavailable"}</span>
        </div>
        <div>
          <Clock size={18} />
          <span>{shop.openHours || "Hours unavailable"}</span>
        </div>
      </section>

      <section className="section-heading">
        <div>
          <p className="eyebrow">Deals</p>
          <h2>Vouchers</h2>
        </div>
      </section>

      {message ? <div className="success-banner">{message}</div> : null}
      <InlineError message={error} />

      <section className="list-stack">
        {vouchers.length > 0 ? (
          vouchers.map((voucher) => (
            <VoucherCard
              key={voucher.id}
              voucher={voucher}
              busy={ordering === voucher.id}
              onClaim={claimVoucher}
            />
          ))
        ) : (
          <EmptyState title="No vouchers" detail="This merchant has no live deals." />
        )}
      </section>
    </div>
  );
}

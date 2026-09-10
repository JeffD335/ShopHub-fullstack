import { BadgePercent, Bolt } from "lucide-react";
import type { Voucher } from "../api/types";
import { cents } from "../utils/format";

export default function VoucherCard({
  voucher,
  onClaim,
  busy
}: {
  voucher: Voucher;
  onClaim: (voucher: Voucher) => void;
  busy?: boolean;
}) {
  const isFlashSale = voucher.type === 1;

  return (
    <article className="voucher-card">
      <div className="voucher-value">
        <BadgePercent size={20} />
        <strong>{cents(voucher.actualValue)}</strong>
        <span>value</span>
      </div>
      <div className="voucher-body">
        <h3>{voucher.title || "Shop voucher"}</h3>
        <p>{voucher.subTitle || "Local deal available for this merchant."}</p>
        <div className="meta-row">
          <span>Pay {cents(voucher.payValue)}</span>
          {voucher.stock != null ? <span>{voucher.stock} left</span> : null}
        </div>
      </div>
      <button
        className={isFlashSale ? "primary-button compact" : "secondary-button compact"}
        type="button"
        onClick={() => onClaim(voucher)}
        disabled={!isFlashSale || busy}
        title={isFlashSale ? "Join flash sale" : "Regular voucher"}
      >
        <Bolt size={16} />
        {isFlashSale ? "Flash sale" : "Regular"}
      </button>
    </article>
  );
}

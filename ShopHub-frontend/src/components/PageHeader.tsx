import { ArrowLeft } from "lucide-react";
import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";

export default function PageHeader({
  eyebrow,
  title,
  action,
  back = false
}: {
  eyebrow?: string;
  title: string;
  action?: ReactNode;
  back?: boolean;
}) {
  const navigate = useNavigate();

  return (
    <section className="page-header">
      <div className="page-title-row">
        {back ? (
          <button
            className="icon-button"
            type="button"
            title="Go back"
            onClick={() => navigate(-1)}
          >
            <ArrowLeft size={18} />
          </button>
        ) : null}
        <div>
          {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
          <h2>{title}</h2>
        </div>
      </div>
      {action ? <div className="page-action">{action}</div> : null}
    </section>
  );
}

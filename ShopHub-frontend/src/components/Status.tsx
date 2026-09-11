import { AlertCircle, Loader2 } from "lucide-react";

export function LoadingState({ label = "Loading" }: { label?: string }) {
  return (
    <div className="state-block">
      <Loader2 className="spin" size={22} />
      <span>{label}</span>
    </div>
  );
}

export function EmptyState({
  title,
  detail
}: {
  title: string;
  detail?: string;
}) {
  return (
    <div className="state-block">
      <AlertCircle size={22} />
      <strong>{title}</strong>
      {detail ? <span>{detail}</span> : null}
    </div>
  );
}

export function InlineError({ message }: { message: string | null }) {
  if (!message) return null;
  return (
    <div className="inline-error" role="alert">
      <AlertCircle size={16} />
      <span>{message}</span>
    </div>
  );
}

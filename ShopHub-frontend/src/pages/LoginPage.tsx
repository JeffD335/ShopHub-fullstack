import { FormEvent, useState } from "react";
import { LogIn, MessageSquareText, Smartphone } from "lucide-react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { setToken } from "../api/client";
import { shophubApi } from "../api/shophub";
import { useAuth } from "../auth";
import { InlineError } from "../components/Status";

export default function LoginPage() {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [phone, setPhone] = useState("13686869696");
  const [code, setCode] = useState("");
  const [sent, setSent] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (user) {
    return <Navigate to="/" replace />;
  }

  const sendCode = async () => {
    setBusy(true);
    setError(null);
    try {
      await shophubApi.sendCode(phone);
      setSent(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not send code");
    } finally {
      setBusy(false);
    }
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const token = await shophubApi.login({ phone, code });
      setToken(token);
      await refreshUser();
      const target =
        typeof location.state === "object" &&
        location.state &&
        "from" in location.state
          ? String(location.state.from)
          : "/";
      navigate(target, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="login-screen">
      <section className="login-panel">
        <div className="brand-lockup large">
          <span className="brand-mark">S</span>
          <span>ShopHub</span>
        </div>
        <div>
          <p className="eyebrow">Full-stack local marketplace</p>
          <h1>Sign in to explore deals, reviews, follows, and flash sales.</h1>
        </div>

        <form className="login-form" onSubmit={submit}>
          <label>
            <span>Phone number</span>
            <div className="input-with-icon">
              <Smartphone size={18} />
              <input
                value={phone}
                onChange={(event) => setPhone(event.target.value)}
                inputMode="tel"
                placeholder="13686869696"
              />
            </div>
          </label>

          <label>
            <span>Verification code</span>
            <div className="code-row">
              <div className="input-with-icon">
                <MessageSquareText size={18} />
                <input
                  value={code}
                  onChange={(event) => setCode(event.target.value)}
                  inputMode="numeric"
                  placeholder="Backend log code"
                />
              </div>
              <button
                className="secondary-button"
                type="button"
                onClick={sendCode}
                disabled={busy || phone.length < 11}
              >
                Send
              </button>
            </div>
          </label>

          {sent ? (
            <p className="form-note">
              Local demo mode writes the verification code to the backend log.
            </p>
          ) : null}
          <InlineError message={error} />

          <button
            className="primary-button full-width"
            type="submit"
            disabled={busy || !phone || !code}
          >
            <LogIn size={18} />
            Sign in
          </button>
        </form>
      </section>
    </main>
  );
}

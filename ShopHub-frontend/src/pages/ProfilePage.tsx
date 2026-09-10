import { useEffect, useState } from "react";
import { LogOut, Medal, PenLine, RefreshCcw, TicketCheck } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { shophubApi } from "../api/shophub";
import type { Blog, UserInfo } from "../api/types";
import { useAuth } from "../auth";
import BlogCard from "../components/BlogCard";
import PageHeader from "../components/PageHeader";
import { EmptyState, InlineError, LoadingState } from "../components/Status";
import { avatarText } from "../utils/format";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const [info, setInfo] = useState<UserInfo | null>(null);
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [streak, setStreak] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const load = async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    try {
      const [profileData, postData, count] = await Promise.all([
        shophubApi.userInfo(user.id),
        shophubApi.myBlogs(1),
        shophubApi.signCount()
      ]);
      setInfo(profileData);
      setBlogs(postData || []);
      setStreak(count || 0);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load profile");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [user?.id]);

  const signToday = async () => {
    setError(null);
    setMessage(null);
    try {
      await shophubApi.sign();
      const count = await shophubApi.signCount();
      setStreak(count || 0);
      setMessage("Daily check-in saved.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not check in");
    }
  };

  const logout = async () => {
    await signOut();
    navigate("/login", { replace: true });
  };

  if (!user) return null;

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Account"
        title="Profile"
        action={
          <button className="secondary-button compact" type="button" onClick={logout}>
            <LogOut size={16} />
            Logout
          </button>
        }
      />

      <section className="profile-band">
        <div className="avatar large">{avatarText(user.nickName)}</div>
        <div>
          <h2>{user.nickName}</h2>
          <p>Member #{user.id}</p>
        </div>
      </section>

      <section className="metric-grid">
        <div className="metric-tile">
          <Medal size={20} />
          <strong>{info?.level ?? 0}</strong>
          <span>Level</span>
        </div>
        <div className="metric-tile">
          <TicketCheck size={20} />
          <strong>{streak ?? 0}</strong>
          <span>Check-in streak</span>
        </div>
        <div className="metric-tile">
          <PenLine size={20} />
          <strong>{blogs.length}</strong>
          <span>Posts</span>
        </div>
      </section>

      <div className="action-row">
        <button className="primary-button compact" type="button" onClick={signToday}>
          <TicketCheck size={16} />
          Check in
        </button>
        <button className="secondary-button compact" type="button" onClick={load}>
          <RefreshCcw size={16} />
          Refresh
        </button>
        <Link className="secondary-button compact" to="/posts/new">
          <PenLine size={16} />
          New post
        </Link>
      </div>

      {message ? <div className="success-banner">{message}</div> : null}
      <InlineError message={error} />
      {loading ? <LoadingState label="Loading profile" /> : null}

      <section className="section-heading">
        <div>
          <p className="eyebrow">Your content</p>
          <h2>My posts</h2>
        </div>
      </section>

      {!loading ? (
        <section className="feed-grid">
          {blogs.length > 0 ? (
            blogs.map((blog) => <BlogCard key={blog.id} blog={blog} />)
          ) : (
            <EmptyState title="No posts yet" detail="Create a review from the Post tab." />
          )}
        </section>
      ) : null}
    </div>
  );
}

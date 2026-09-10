import { useCallback, useEffect, useState } from "react";
import { PlusSquare, RefreshCcw } from "lucide-react";
import { Link } from "react-router-dom";
import { shophubApi } from "../api/shophub";
import type { Blog } from "../api/types";
import BlogCard from "../components/BlogCard";
import PageHeader from "../components/PageHeader";
import { EmptyState, InlineError, LoadingState } from "../components/Status";

type FeedMode = "hot" | "following";

export default function FeedPage() {
  const [mode, setMode] = useState<FeedMode>("hot");
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      if (mode === "hot") {
        setBlogs(await shophubApi.hotBlogs(1));
      } else {
        const result = await shophubApi.followedBlogs(Date.now(), 0);
        setBlogs(result.list || []);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load feed");
    } finally {
      setLoading(false);
    }
  }, [mode]);

  useEffect(() => {
    void load();
  }, [load]);

  const likeBlog = async (blog: Blog) => {
    try {
      await shophubApi.likeBlog(blog.id);
      setBlogs((current) =>
        current.map((item) =>
          item.id === blog.id
            ? {
                ...item,
                isLike: !item.isLike,
                liked: (item.liked || 0) + (item.isLike ? -1 : 1)
              }
            : item
        )
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not update like");
    }
  };

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Community"
        title="Review Feed"
        action={
          <Link className="primary-button compact" to="/posts/new">
            <PlusSquare size={16} />
            New post
          </Link>
        }
      />

      <section className="segmented-control" aria-label="Feed type">
        <button
          className={mode === "hot" ? "selected" : ""}
          type="button"
          onClick={() => setMode("hot")}
        >
          Hot
        </button>
        <button
          className={mode === "following" ? "selected" : ""}
          type="button"
          onClick={() => setMode("following")}
        >
          Following
        </button>
        <button className="icon-segment" type="button" title="Refresh" onClick={load}>
          <RefreshCcw size={17} />
        </button>
      </section>

      <InlineError message={error} />
      {loading ? <LoadingState label="Loading feed" /> : null}
      {!loading && blogs.length === 0 ? (
        <EmptyState
          title={mode === "hot" ? "No posts yet" : "Your follow feed is empty"}
          detail={
            mode === "hot"
              ? "Create the first review post."
              : "Follow users from blog detail pages to populate this view."
          }
        />
      ) : null}

      {!loading ? (
        <section className="feed-grid">
          {blogs.map((blog) => (
            <BlogCard key={blog.id} blog={blog} onLike={likeBlog} />
          ))}
        </section>
      ) : null}
    </div>
  );
}

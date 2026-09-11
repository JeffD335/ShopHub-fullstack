import { FormEvent, useEffect, useState } from "react";
import { Heart, MessageCircle, Send, UserPlus, UserX } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { shophubApi } from "../api/shophub";
import type { Blog, BlogComment, User } from "../api/types";
import { useAuth } from "../auth";
import PageHeader from "../components/PageHeader";
import { EmptyState, InlineError, LoadingState } from "../components/Status";
import {
  avatarText,
  dateLabel,
  displayBlog,
  firstImage,
  shortText,
  stripHtml
} from "../utils/format";

export default function BlogDetailPage() {
  const { id } = useParams();
  const blogId = Number(id);
  const { user } = useAuth();
  const [blog, setBlog] = useState<Blog | null>(null);
  const [comments, setComments] = useState<BlogComment[]>([]);
  const [likedUsers, setLikedUsers] = useState<User[]>([]);
  const [followed, setFollowed] = useState(false);
  const [comment, setComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [savingComment, setSavingComment] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const blogData = await shophubApi.blog(blogId);
      setBlog(blogData);
      const [commentData, likeData] = await Promise.all([
        shophubApi.comments(blogId),
        shophubApi.blogLikes(blogId)
      ]);
      setComments(commentData || []);
      setLikedUsers(likeData || []);
      if (blogData && blogData.userId !== user?.id) {
        setFollowed(await shophubApi.followStatus(blogData.userId));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load post");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [blogId]);

  const like = async () => {
    if (!blog) return;
    try {
      await shophubApi.likeBlog(blog.id);
      setBlog({
        ...blog,
        isLike: !blog.isLike,
        liked: (blog.liked || 0) + (blog.isLike ? -1 : 1)
      });
      setLikedUsers(await shophubApi.blogLikes(blog.id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not update like");
    }
  };

  const toggleFollow = async () => {
    if (!blog) return;
    try {
      await shophubApi.follow(blog.userId, !followed);
      setFollowed((value) => !value);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not update follow");
    }
  };

  const submitComment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!comment.trim()) return;
    setSavingComment(true);
    setError(null);
    try {
      await shophubApi.addComment({ blogId, content: comment.trim() });
      setComment("");
      setComments(await shophubApi.comments(blogId));
      if (blog) {
        setBlog({ ...blog, comments: (blog.comments || 0) + 1 });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not add comment");
    } finally {
      setSavingComment(false);
    }
  };

  if (loading) return <LoadingState label="Loading post" />;
  if (!blog) return <EmptyState title="Post not found" />;

  const copy = displayBlog(blog);
  const canFollow = user && user.id !== blog.userId;

  return (
    <div className="page-stack">
      <PageHeader eyebrow="Review detail" title={copy.title} back />

      <article className="post-detail">
        <img src={firstImage(blog.images)} alt={copy.title} />
        <div className="author-row roomy">
          <Link className="avatar" to={`/users/${blog.userId}`}>
            {avatarText(blog.name)}
          </Link>
          <div>
            <Link className="author-name" to={`/users/${blog.userId}`}>
              {blog.name || `User ${blog.userId}`}
            </Link>
            <span>{dateLabel(blog.createTime)}</span>
          </div>
          {canFollow ? (
            <button className="secondary-button compact" type="button" onClick={toggleFollow}>
              {followed ? <UserX size={16} /> : <UserPlus size={16} />}
              {followed ? "Following" : "Follow"}
            </button>
          ) : null}
        </div>
        <h2>{copy.title}</h2>
        <p className="lead-copy">{copy.summary}</p>
        <p>{shortText(stripHtml(blog.content || ""), 900)}</p>

        <div className="card-actions">
          <button className={blog.isLike ? "chip active" : "chip"} type="button" onClick={like}>
            <Heart size={16} fill={blog.isLike ? "currentColor" : "none"} />
            {blog.liked || 0}
          </button>
          <span className="chip muted">
            <MessageCircle size={16} />
            {blog.comments || 0}
          </span>
        </div>
      </article>

      <InlineError message={error} />

      {likedUsers.length > 0 ? (
        <section className="liked-row">
          <span>Recent likes</span>
          {likedUsers.map((item) => (
            <Link className="avatar small" to={`/users/${item.id}`} key={item.id}>
              {avatarText(item.nickName)}
            </Link>
          ))}
        </section>
      ) : null}

      <section className="comment-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Discussion</p>
            <h2>Comments</h2>
          </div>
        </div>

        <form className="comment-form" onSubmit={submitComment}>
          <input
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            placeholder="Add a comment"
          />
          <button className="primary-button compact" type="submit" disabled={savingComment}>
            <Send size={16} />
            Send
          </button>
        </form>

        <div className="list-stack">
          {comments.length > 0 ? (
            comments.map((item) => (
              <article className="comment-item" key={item.id || item.content}>
                <div className="avatar small">{avatarText(`U${item.userId || ""}`)}</div>
                <div>
                  <strong>User {item.userId || "Unknown"}</strong>
                  <p>{item.content}</p>
                </div>
              </article>
            ))
          ) : (
            <EmptyState title="No comments yet" detail="Start the discussion." />
          )}
        </div>
      </section>
    </div>
  );
}

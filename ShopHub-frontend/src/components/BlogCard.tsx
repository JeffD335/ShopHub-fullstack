import { Heart, MessageCircle } from "lucide-react";
import { Link } from "react-router-dom";
import type { Blog } from "../api/types";
import {
  avatarText,
  dateLabel,
  displayBlog,
  firstImage,
  shortText
} from "../utils/format";

export default function BlogCard({
  blog,
  onLike
}: {
  blog: Blog;
  onLike?: (blog: Blog) => void;
}) {
  const copy = displayBlog(blog);

  return (
    <article className="blog-card">
      <Link to={`/blogs/${blog.id}`}>
        <img src={firstImage(blog.images)} alt={copy.title} />
      </Link>
      <div className="blog-card-body">
        <div className="author-row">
          <Link className="avatar" to={`/users/${blog.userId}`}>
            {avatarText(blog.name)}
          </Link>
          <div>
            <Link className="author-name" to={`/users/${blog.userId}`}>
              {blog.name || `User ${blog.userId}`}
            </Link>
            <span>{dateLabel(blog.createTime)}</span>
          </div>
        </div>
        <Link to={`/blogs/${blog.id}`}>
          <h3>{copy.title}</h3>
          <p>{shortText(copy.summary, 150)}</p>
        </Link>
        <div className="card-actions">
          <button
            className={blog.isLike ? "chip active" : "chip"}
            type="button"
            onClick={() => onLike?.(blog)}
          >
            <Heart size={16} fill={blog.isLike ? "currentColor" : "none"} />
            {blog.liked || 0}
          </button>
          <span className="chip muted">
            <MessageCircle size={16} />
            {blog.comments || 0}
          </span>
        </div>
      </div>
    </article>
  );
}

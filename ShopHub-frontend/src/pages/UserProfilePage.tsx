import { useEffect, useState } from "react";
import { RefreshCcw, UserPlus, UserRoundCheck, UserX } from "lucide-react";
import { useParams } from "react-router-dom";
import { shophubApi } from "../api/shophub";
import type { Blog, User, UserInfo } from "../api/types";
import { useAuth } from "../auth";
import BlogCard from "../components/BlogCard";
import PageHeader from "../components/PageHeader";
import { EmptyState, InlineError, LoadingState } from "../components/Status";
import { avatarText } from "../utils/format";

export default function UserProfilePage() {
  const { id } = useParams();
  const userId = Number(id);
  const { user: currentUser } = useAuth();
  const [profile, setProfile] = useState<User | null>(null);
  const [info, setInfo] = useState<UserInfo | null>(null);
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [common, setCommon] = useState<User[]>([]);
  const [followed, setFollowed] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isSelf = currentUser?.id === userId;

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const [profileData, infoData, blogData] = await Promise.all([
        shophubApi.user(userId),
        shophubApi.userInfo(userId),
        shophubApi.userBlogs(userId)
      ]);
      setProfile(profileData);
      setInfo(infoData);
      setBlogs(blogData || []);
      if (!isSelf) {
        const [followStatus, commonFollows] = await Promise.all([
          shophubApi.followStatus(userId),
          shophubApi.commonFollows(userId)
        ]);
        setFollowed(followStatus);
        setCommon(commonFollows || []);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load user");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [userId, isSelf]);

  const toggleFollow = async () => {
    setError(null);
    try {
      await shophubApi.follow(userId, !followed);
      setFollowed((value) => !value);
      setCommon(await shophubApi.commonFollows(userId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not update follow");
    }
  };

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Community member"
        title={profile?.nickName || `User ${userId}`}
        back
        action={
          <button className="secondary-button compact" type="button" onClick={load}>
            <RefreshCcw size={16} />
            Refresh
          </button>
        }
      />

      {loading ? <LoadingState label="Loading user" /> : null}
      <InlineError message={error} />

      {!loading && profile ? (
        <>
          <section className="profile-band">
            <div className="avatar large">{avatarText(profile.nickName)}</div>
            <div>
              <h2>{profile.nickName}</h2>
              <p>{info?.city || "Local member"}</p>
            </div>
            {!isSelf ? (
              <button className="primary-button compact" type="button" onClick={toggleFollow}>
                {followed ? <UserX size={16} /> : <UserPlus size={16} />}
                {followed ? "Unfollow" : "Follow"}
              </button>
            ) : null}
          </section>

          <section className="metric-grid">
            <div className="metric-tile">
              <UserRoundCheck size={20} />
              <strong>{info?.fans ?? 0}</strong>
              <span>Followers</span>
            </div>
            <div className="metric-tile">
              <UserPlus size={20} />
              <strong>{info?.followee ?? 0}</strong>
              <span>Following</span>
            </div>
            <div className="metric-tile">
              <RefreshCcw size={20} />
              <strong>{common.length}</strong>
              <span>Common</span>
            </div>
          </section>

          {common.length > 0 ? (
            <section className="liked-row">
              <span>Common follows</span>
              {common.map((item) => (
                <div className="avatar small" key={item.id}>
                  {avatarText(item.nickName)}
                </div>
              ))}
            </section>
          ) : null}

          <section className="section-heading">
            <div>
              <p className="eyebrow">Posts</p>
              <h2>Reviews by this user</h2>
            </div>
          </section>

          <section className="feed-grid">
            {blogs.length > 0 ? (
              blogs.map((blog) => <BlogCard key={blog.id} blog={blog} />)
            ) : (
              <EmptyState title="No posts" detail="This user has not posted reviews yet." />
            )}
          </section>
        </>
      ) : null}
    </div>
  );
}

import { FormEvent, useEffect, useState } from "react";
import { ImagePlus, Send } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { shophubApi } from "../api/shophub";
import type { Shop, ShopType } from "../api/types";
import PageHeader from "../components/PageHeader";
import { InlineError, LoadingState } from "../components/Status";
import { categoryMeta } from "../data/localization";

export default function CreatePostPage() {
  const navigate = useNavigate();
  const [types, setTypes] = useState<ShopType[]>([]);
  const [shops, setShops] = useState<Shop[]>([]);
  const [typeId, setTypeId] = useState(1);
  const [shopId, setShopId] = useState<number | "">("");
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [imagePath, setImagePath] = useState("");
  const [uploading, setUploading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    async function loadTypes() {
      try {
        const data = await shophubApi.shopTypes();
        if (mounted) setTypes(data);
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : "Could not load categories");
        }
      }
    }
    void loadTypes();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    let mounted = true;
    async function loadShops() {
      setLoading(true);
      try {
        const data = await shophubApi.shopsByType(typeId, 1);
        if (!mounted) return;
        setShops(data);
        setShopId(data[0]?.id || "");
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : "Could not load shops");
        }
      } finally {
        if (mounted) setLoading(false);
      }
    }
    void loadShops();
    return () => {
      mounted = false;
    };
  }, [typeId]);

  const upload = async (file?: File) => {
    if (!file) return;
    setUploading(true);
    setError(null);
    try {
      setImagePath(await shophubApi.uploadBlogImage(file));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not upload image");
    } finally {
      setUploading(false);
    }
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!shopId) return;
    setSaving(true);
    setError(null);
    try {
      const id = await shophubApi.saveBlog({
        shopId: Number(shopId),
        title: title.trim(),
        content: content.trim(),
        images: imagePath
      });
      navigate(`/blogs/${id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not create post");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-stack">
      <PageHeader eyebrow="Community" title="Create Review Post" />

      <form className="editor-form" onSubmit={submit}>
        <label>
          <span>Category</span>
          <select value={typeId} onChange={(event) => setTypeId(Number(event.target.value))}>
            {types.map((type) => (
              <option value={type.id} key={type.id}>
                {categoryMeta[type.id]?.label || type.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>Shop</span>
          {loading ? (
            <LoadingState label="Loading shops" />
          ) : (
            <select value={shopId} onChange={(event) => setShopId(Number(event.target.value))}>
              {shops.map((shop) => (
                <option value={shop.id} key={shop.id}>
                  #{shop.id} {shop.name}
                </option>
              ))}
            </select>
          )}
        </label>

        <label>
          <span>Title</span>
          <input
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="What makes this place worth visiting?"
          />
        </label>

        <label>
          <span>Review</span>
          <textarea
            value={content}
            onChange={(event) => setContent(event.target.value)}
            placeholder="Write atmosphere, service, pricing, and deal notes."
            rows={8}
          />
        </label>

        <label className="upload-box">
          <ImagePlus size={20} />
          <span>{imagePath || "Upload a blog photo"}</span>
          <input type="file" accept="image/*" onChange={(event) => upload(event.target.files?.[0])} />
        </label>

        <InlineError message={error} />

        <button
          className="primary-button full-width"
          type="submit"
          disabled={saving || uploading || !title.trim() || !content.trim() || !shopId}
        >
          <Send size={18} />
          Publish post
        </button>
      </form>
    </div>
  );
}

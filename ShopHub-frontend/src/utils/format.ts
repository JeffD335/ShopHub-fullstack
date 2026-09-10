import { blogCopy, fallbackImage, shopCopy } from "../data/localization";
import type { Blog, Shop } from "../api/types";

export function cents(value?: number) {
  if (value == null) return "$0.00";
  return `$${(value / 100).toFixed(2)}`;
}

export function score(value?: number) {
  if (value == null) return "New";
  return (value / 10).toFixed(1);
}

export function splitImages(images?: string) {
  if (!images) return [];
  return images
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

export function firstImage(images?: string) {
  const [first] = splitImages(images);
  if (!first) return fallbackImage;
  if (first.startsWith("http://")) {
    return first.replace("http://", "https://");
  }
  if (first.startsWith("https://")) {
    return first;
  }
  return fallbackImage;
}

export function displayShop(shop: Shop) {
  return {
    name: shopCopy[shop.id]?.name || shop.name,
    tagline:
      shopCopy[shop.id]?.tagline ||
      "Local merchant listing powered by MySQL records and Redis cache reads."
  };
}

export function displayBlog(blog: Blog) {
  return {
    title: blogCopy[blog.id]?.title || blog.title,
    summary:
      blogCopy[blog.id]?.summary ||
      stripHtml(blog.content || "Community review post from the live backend.")
  };
}

export function stripHtml(value: string) {
  return value
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

export function shortText(value: string, max = 140) {
  if (value.length <= max) return value;
  return `${value.slice(0, max).trim()}...`;
}

export function avatarText(name?: string) {
  if (!name) return "SH";
  const clean = name.replace(/^user_/, "");
  return clean.slice(0, 2).toUpperCase();
}

export function dateLabel(value?: string) {
  if (!value) return "Recently";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Recently";
  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    year: "numeric"
  }).format(date);
}

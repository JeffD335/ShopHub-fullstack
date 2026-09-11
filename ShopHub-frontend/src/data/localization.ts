export const categoryMeta: Record<number, { label: string; accent: string }> = {
  1: { label: "Restaurants", accent: "accent-red" },
  2: { label: "Karaoke", accent: "accent-indigo" },
  3: { label: "Beauty & Hair", accent: "accent-pink" },
  4: { label: "Fitness", accent: "accent-green" },
  5: { label: "Massage", accent: "accent-gold" },
  6: { label: "Spa", accent: "accent-teal" },
  7: { label: "Family Fun", accent: "accent-blue" },
  8: { label: "Bars", accent: "accent-orange" },
  9: { label: "Party Rooms", accent: "accent-purple" },
  10: { label: "Nails & Lashes", accent: "accent-rose" }
};

export const shopCopy: Record<number, { name: string; tagline: string }> = {
  1: {
    name: "103 Tea Restaurant",
    tagline: "Casual Cantonese plates with strong local traffic."
  },
  2: {
    name: "Cai Ma Hongtao Grill",
    tagline: "Hot-pot and grilled meat with dinner-time demand spikes."
  },
  3: {
    name: "New White Deer",
    tagline: "High-volume restaurant listing used for cache and search demos."
  },
  4: {
    name: "Mamala Garden Dining",
    tagline: "Premium restaurant detail page with vouchers and blog content."
  },
  5: {
    name: "Haidilao Hot Pot",
    tagline: "Popular shop record for ratings, comments, and deal browsing."
  },
  10: {
    name: "Kaile Di KTV",
    tagline: "Entertainment listing for category and nearby-shop queries."
  }
};

export const blogCopy: Record<number, { title: string; summary: string }> = {
  4: {
    title: "A romantic garden-dining night with steak and wine",
    summary:
      "A user review post about restaurant atmosphere, service, signature dishes, and photo-friendly dining details."
  },
  5: {
    title: "Affordable Cantonese cafe worth revisiting",
    summary:
      "A local-food post covering value, nostalgic decor, dish highlights, and user engagement metrics."
  },
  6: {
    title: "Weekend horseback-riding idea near Hangzhou",
    summary:
      "A lifestyle post used to demonstrate the social feed and like workflow."
  },
  7: {
    title: "Outdoor weekend activity recommendation",
    summary:
      "A second activity post useful for feed pagination and author-profile navigation."
  }
};

export const fallbackImage =
  "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80";

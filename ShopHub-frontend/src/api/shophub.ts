import { http } from "./client";
import type {
  Blog,
  BlogComment,
  LoginPayload,
  ScrollResult,
  Shop,
  ShopType,
  User,
  UserInfo,
  Voucher
} from "./types";

export const shophubApi = {
  sendCode: (phone: string) =>
    http.post<void>(`/user/code?phone=${encodeURIComponent(phone)}`),
  login: (payload: LoginPayload) => http.post<string>("/user/login", payload),
  logout: () => http.post<void>("/user/logout"),
  me: () => http.get<User>("/user/me"),
  user: (id: number) => http.get<User | null>(`/user/${id}`),
  userInfo: (id: number) => http.get<UserInfo | null>(`/user/info/${id}`),
  updateUserInfo: (payload: Partial<UserInfo>) =>
    http.put<void>("/user/info", payload),
  sign: () => http.put<void>("/user/sign"),
  signCount: () => http.get<number | null>("/user/sign/count"),

  shopTypes: () => http.get<ShopType[]>("/shop-type/list"),
  shopsByType: (typeId: number, current = 1, nearby = false) => {
    const params = new URLSearchParams({
      typeId: String(typeId),
      current: String(current)
    });
    if (nearby) {
      params.set("x", "120.149192");
      params.set("y", "30.316078");
    }
    return http.get<Shop[]>(`/shop/of/type?${params}`);
  },
  shopsByName: (name: string, current = 1) =>
    http.get<Shop[]>(
      `/shop/of/name?name=${encodeURIComponent(name)}&current=${current}`
    ),
  shop: (id: number) => http.get<Shop | null>(`/shop/${id}`),

  vouchers: (shopId: number) => http.get<Voucher[]>(`/voucher/list/${shopId}`),
  seckill: (voucherId: number) =>
    http.post<number>(`/voucher-order/seckill/${voucherId}`),

  hotBlogs: (current = 1) => http.get<Blog[]>(`/blog/hot?current=${current}`),
  followedBlogs: (lastId = Date.now(), offset = 0) =>
    http.get<ScrollResult<Blog>>(
      `/blog/of/follow?lastId=${lastId}&offset=${offset}`
    ),
  blog: (id: number) => http.get<Blog | null>(`/blog/${id}`),
  myBlogs: (current = 1) => http.get<Blog[]>(`/blog/of/me?current=${current}`),
  userBlogs: (userId: number, current = 1) =>
    http.get<Blog[]>(`/blog/of/user?id=${userId}&current=${current}`),
  saveBlog: (payload: Partial<Blog>) => http.post<number>("/blog", payload),
  likeBlog: (id: number) => http.put<void>(`/blog/like/${id}`),
  blogLikes: (id: number) => http.get<User[]>(`/blog/likes/${id}`),
  comments: (blogId: number) =>
    http.get<BlogComment[]>(`/blog-comments/of/blog/${blogId}`),
  addComment: (payload: BlogComment) =>
    http.post<number>("/blog-comments", payload),
  uploadBlogImage: (file: File) => {
    const form = new FormData();
    form.append("file", file);
    return http.upload<string>("/upload/blog", form);
  },

  follow: (userId: number, isFollow: boolean) =>
    http.put<void>(`/follow/${userId}/${isFollow}`),
  followStatus: (userId: number) => http.get<boolean>(`/follow/status/${userId}`),
  commonFollows: (userId: number) => http.get<User[]>(`/follow/common/${userId}`)
};

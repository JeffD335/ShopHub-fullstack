export interface ApiResult<T> {
  success: boolean;
  errorMsg: string | null;
  data: T;
  total: number | null;
}

export interface LoginPayload {
  phone: string;
  code: string;
}

export interface User {
  id: number;
  nickName: string;
  icon?: string;
}

export interface UserInfo {
  userId: number;
  city?: string;
  introduce?: string;
  fans?: number;
  followee?: number;
  gender?: number;
  birthday?: string;
  credits?: number;
  level?: number;
}

export interface ShopType {
  id: number;
  name: string;
  icon?: string;
  sort?: number;
}

export interface Shop {
  id: number;
  name: string;
  typeId: number;
  images?: string;
  area?: string;
  address?: string;
  x?: number;
  y?: number;
  avgPrice?: number;
  sold?: number;
  comments?: number;
  score?: number;
  openHours?: string;
  distance?: number;
}

export interface Voucher {
  id: number;
  shopId: number;
  title: string;
  subTitle?: string;
  rules?: string;
  payValue: number;
  actualValue: number;
  type: number;
  status: number;
  stock?: number;
  beginTime?: string;
  endTime?: string;
}

export interface Blog {
  id: number;
  shopId?: number;
  userId: number;
  icon?: string;
  name?: string;
  isLike?: boolean;
  title: string;
  images?: string;
  content?: string;
  liked?: number;
  comments?: number;
  createTime?: string;
}

export interface BlogComment {
  id?: number;
  userId?: number;
  blogId: number;
  parentId?: number;
  answerId?: number;
  content: string;
  liked?: number;
  status?: number;
  createTime?: string;
}

export interface ScrollResult<T> {
  list: T[];
  minTime: number;
  offset: number;
}

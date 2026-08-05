import { platformFetch } from "@/lib/platform-auth";

export type InboxNotification = { notificationId:string; tenantKey:string; siteKey?:string|null; type:string; severity:string; title:string; body?:string|null; deepLink?:string|null; sourceService?:string|null; sourceKey?:string|null; createdAt:string; readAt?:string|null; version:number };
export type InboxPage = { content:InboxNotification[]; page:number; size:number; totalElements:number; totalPages:number };

async function call<T>(path:string, init:RequestInit = {}):Promise<T>{
  const response=await platformFetch(`/api/platform/service/notification-service${path}`,{...init,headers:{"Content-Type":"application/json",...(init.headers??{})},cache:"no-store"});
  if(!response.ok) throw new Error((await response.text().catch(()=>""))||`Notification request failed (${response.status})`);
  return response.json() as Promise<T>;
}
export const listInbox=(page=0,size=20)=>call<InboxPage>(`/endpoint/notifications/inbox?page=${page}&size=${size}`);
export const getUnreadCount=()=>call<{unreadCount:number;updatedAt:string}>("/endpoint/notifications/inbox/unread-count");
export const markNotificationRead=(id:string)=>call<InboxNotification>(`/endpoint/notifications/inbox/${encodeURIComponent(id)}/read`,{method:"PATCH"});
export const markAllNotificationsRead=()=>call<{unreadCount:number;updatedAt:string}>("/endpoint/notifications/inbox/read-all",{method:"POST"});

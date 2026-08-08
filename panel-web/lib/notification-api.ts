import { platformFetch } from "@/lib/platform-auth";

export type InboxNotification = { notificationId:string; tenantKey:string; siteKey?:string|null; type:string; severity:string; title:string; body?:string|null; deepLink?:string|null; sourceService?:string|null; sourceKey?:string|null; createdAt:string; readAt?:string|null; version:number };
export type InboxPage = { content:InboxNotification[]; page:number; size:number; totalElements:number; totalPages:number };
export type NotificationMessage={recordKey:string;data:Record<string,unknown>;createdAt?:string;updatedAt?:string};
export type NotificationProvider={channel:string;provider:string;status:string};
export type NotificationDraft={messageKey?:string;channel?:string;templateKey?:string;provider?:string;dispatchMode?:string;recipient?:string;subject?:string;body?:string;model?:Record<string,unknown>;relatedRef?:Record<string,unknown>};

async function call<T>(path:string, init:RequestInit = {}):Promise<T>{
  const response=await platformFetch(`/api/platform/service/notification-service${path}`,{...init,headers:{"Content-Type":"application/json",...(init.headers??{})},cache:"no-store"});
  if(!response.ok) throw new Error((await response.text().catch(()=>""))||`Notification request failed (${response.status})`);
  return response.json() as Promise<T>;
}
export const listInbox=(page=0,size=20)=>call<InboxPage>(`/endpoint/notifications/inbox?page=${page}&size=${size}`);
export const getUnreadCount=()=>call<{unreadCount:number;updatedAt:string}>("/endpoint/notifications/inbox/unread-count");
export const markNotificationRead=(id:string)=>call<InboxNotification>(`/endpoint/notifications/inbox/${encodeURIComponent(id)}/read`,{method:"PATCH"});
export const markAllNotificationsRead=()=>call<{unreadCount:number;updatedAt:string}>("/endpoint/notifications/inbox/read-all",{method:"POST"});
export const listNotificationMessages=()=>call<NotificationMessage[]>("/endpoint/notifications/messages");
export const listNotificationProviders=()=>call<NotificationProvider[]>("/endpoint/notifications/providers");
export const previewNotification=(draft:NotificationDraft)=>call<{channel:string;provider:string;subject:string;body:string}>("/endpoint/notifications/preview",{method:"POST",body:JSON.stringify(draft)});
export const sendNotification=(draft:NotificationDraft)=>call<{messageKey:string;status:string;channel:string;recipient:string}>("/endpoint/notifications/send",{method:"POST",body:JSON.stringify(draft)});
export const retryNotification=(key:string)=>call<{messageKey:string;status:string}>(`/endpoint/notifications/messages/${encodeURIComponent(key)}/retry`,{method:"POST",body:"{}"});

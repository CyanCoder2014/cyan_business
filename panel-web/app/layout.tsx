import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import { AppProviders } from "@/components/app-providers";
import "./globals.css";
import "./phase1-shell.css";

export const metadata: Metadata = {
  title: "Cyan Panel",
  description: "Multilingual control panel for AI-native business apps, workflows, data, bots, and storefront operations.",
  manifest: "/manifest.json",
  icons: {
    icon: "/icon.svg",
    shortcut: "/icon.svg",
    apple: "/icon.svg"
  },
  appleWebApp: {
    capable: true,
    title: "Cyan Panel",
    statusBarStyle: "black-translucent"
  }
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#f7f8fe" },
    { media: "(prefers-color-scheme: dark)", color: "#07101e" }
  ]
};

export default function RootLayout({
  children
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head><script dangerouslySetInnerHTML={{ __html: `(function(){try{var t=localStorage.getItem('cyan.panel.theme')||'system';var d=t==='dark'||(t==='system'&&matchMedia('(prefers-color-scheme: dark)').matches);document.documentElement.dataset.theme=d?'dark':'light';document.documentElement.dataset.themePreference=t;var l=localStorage.getItem('cyan.panel.locale')||'en';document.documentElement.lang=l==='fa'?'fa':'en';document.documentElement.dir=l==='fa'?'rtl':'ltr'}catch(e){}})()` }} /></head>
      <body>
        <AppProviders>{children}</AppProviders>
      </body>
    </html>
  );
}

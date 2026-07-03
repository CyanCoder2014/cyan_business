import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import { PanelProvider } from "@/components/panel-provider";
import "./globals.css";

export const metadata: Metadata = {
  title: "Cyan Panel",
  description: "Multilingual control panel for AI-native business apps, workflows, data, bots, and storefront operations.",
  manifest: "/manifest.json",
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
  themeColor: "#f7f8fe"
};

export default function RootLayout({
  children
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <PanelProvider>{children}</PanelProvider>
      </body>
    </html>
  );
}

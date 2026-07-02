import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import { Roboto } from "next/font/google";
import { PanelProvider } from "@/components/panel-provider";
import "./globals.css";

const roboto = Roboto({
  subsets: ["latin"],
  weight: ["300", "400", "500", "700"],
  variable: "--font-roboto",
  display: "swap"
});

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
    <html lang="en" className={roboto.variable}>
      <body>
        <PanelProvider>{children}</PanelProvider>
      </body>
    </html>
  );
}

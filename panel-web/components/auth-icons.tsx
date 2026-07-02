type IconProps = {
  className?: string;
  size?: number;
};

export function SparkleIcon({ className, size = 16 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 2l1.2 4.2L17.5 7.5 13.2 8.7 12 13l-1.2-4.3L6.5 7.5l4.3-1.3L12 2z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      <path d="M19 14l.8 2.7L22.5 17.5l-2.7.8L19 21l-.8-2.7L15.5 17.5l2.7-.8L19 14z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
    </svg>
  );
}

export function GlobeIcon({ className, size = 22 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.5" />
      <path d="M3 12h18M12 3c2.5 2.8 2.5 15.2 0 18M12 3c-2.5 2.8-2.5 15.2 0 18" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

export function ShopIcon({ className, size = 22 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M6 8h15l-1.5 11H7.5L6 8z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      <path d="M9 8V6a3 3 0 0 1 6 0v2" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

export function UsersIcon({ className, size = 22 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="9" cy="8" r="3" stroke="currentColor" strokeWidth="1.5" />
      <path d="M3.5 19c.6-2.5 2.7-4 5.5-4s4.9 1.5 5.5 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <path d="M16 8.5a2.5 2.5 0 1 1 0 5M14.5 19c.4-1.6 1.7-2.8 3.5-2.8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export function DocumentIcon({ className, size = 22 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M8 4h8l4 4v12a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" stroke="currentColor" strokeWidth="1.5" />
      <path d="M16 4v4h4M10 12h6M10 16h6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export function LightningIcon({ className, size = 22 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M13 2L4 14h7l-1 8 9-12h-7l1-8z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
    </svg>
  );
}

export function PlaneIcon({ className, size = 22 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3 12l18-7-4 7 4 7-18-7 4-2-4-2z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
    </svg>
  );
}

export function ShieldIcon({ className, size = 20 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 3l8 3v6c0 5-3.5 8.5-8 9-4.5-.5-8-4-8-9V6l8-3z" stroke="currentColor" strokeWidth="1.5" />
      <path d="M9.5 12.5l1.8 1.8 3.7-3.7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export function LayersIcon({ className, size = 20 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 3l9 5-9 5-9-5 9-5z" stroke="currentColor" strokeWidth="1.5" />
      <path d="M3 12l9 5 9-5M3 17l9 5 9-5" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

export function PhoneDeviceIcon({ className, size = 20 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="7" y="2.5" width="10" height="19" rx="2.5" stroke="currentColor" strokeWidth="1.5" />
      <path d="M11 18.5h2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export function MailIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="3" y="5" width="18" height="14" rx="2" stroke="currentColor" strokeWidth="1.5" />
      <path d="M3 7l9 6 9-6" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

export function LockIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="5" y="10" width="14" height="10" rx="2" stroke="currentColor" strokeWidth="1.5" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

export function BuildingIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 20V6l8-3 8 3v14H4z" stroke="currentColor" strokeWidth="1.5" />
      <path d="M9 10h2M9 14h2M13 10h2M13 14h2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export function PhoneIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M8.5 4.5c.5 2.5 1.5 4.8 3 6.8M15.5 19.5c-2 .9-4.2 1.4-6.5 1.4-1 0-2-.1-3-.3l-1 3 3-1c-5.5-1.5-9.5-6-9.5-11.5 0-1 .1-2 .3-3l3 1c-.2 1-.3 2-.3 3 0 2.3.5 4.5 1.4 6.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export function EyeIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M2.5 12C4.5 7.5 8 5 12 5s7.5 2.5 9.5 7c-2 4.5-5.5 7-9.5 7s-7.5-2.5-9.5-7z" stroke="currentColor" strokeWidth="1.5" />
      <circle cx="12" cy="12" r="2.5" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

export function EyeOffIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3 3l18 18M10.6 10.6a2.5 2.5 0 0 0 3.5 3.5M7.2 7.8C5.4 9.2 4 10.8 2.5 12c2 4.5 5.5 7 9.5 7 1.4 0 2.7-.3 3.9-.8M12 5c4 0 7.5 2.5 9.5 7-1 2.2-2.6 4-4.4 5.2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export function ArrowRightIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function GoogleIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" aria-hidden="true">
      <path fill="#4285F4" d="M22 12.24c0-.74-.07-1.45-.2-2.14H12v4.05h5.62c-.24 1.28-.97 2.36-2.07 3.08v2.55h3.35c1.96-1.8 3.1-4.45 3.1-7.54z" />
      <path fill="#34A853" d="M12 22c2.78 0 5.12-.92 6.82-2.5l-3.35-2.55c-.92.62-2.1.98-3.47.98-2.67 0-4.93-1.8-5.74-4.22H2.9v2.64C4.6 19.98 8.04 22 12 22z" />
      <path fill="#FBBC05" d="M6.26 13.71A5.98 5.98 0 0 1 5.98 12c0-.59.1-1.17.28-1.71V7.65H2.9A10 10 0 0 0 2 12c0 1.61.39 3.13 1.08 4.47l3.18-2.76z" />
      <path fill="#EA4335" d="M12 5.38c1.52 0 2.88.52 3.95 1.54l2.96-2.96C17.11 2.09 14.77 1 12 1 8.04 1 4.6 3.02 2.9 7.65l3.36 2.64C7.07 7.18 9.33 5.38 12 5.38z" />
    </svg>
  );
}

export function GitHubIcon({ className, size = 18 }: IconProps) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M12 1.5C6.2 1.5 1.5 6.35 1.5 12.24c0 4.74 3.08 8.75 7.35 10.17.54.1.74-.24.74-.53 0-.26-.01-1.14-.02-2.07-2.99.65-3.62-1.28-3.62-1.28-.49-1.24-1.2-1.57-1.2-1.57-.98-.67.07-.66.07-.66 1.08.08 1.65 1.12 1.65 1.12.96 1.66 2.52 1.18 3.13.9.1-.71.38-1.18.69-1.45-2.39-.27-4.9-1.2-4.9-5.34 0-1.18.42-2.14 1.11-2.9-.11-.27-.48-1.36.11-2.83 0 0 .91-.29 2.98 1.1.86-.24 1.78-.36 2.7-.36.92 0 1.84.12 2.7.36 2.07-1.39 2.98-1.1 2.98-1.1.59 1.47.22 2.56.11 2.83.69.76 1.11 1.72 1.11 2.9 0 4.15-2.52 5.07-4.92 5.34.39.34.73 1 .73 2.02 0 1.46-.01 2.63-.01 2.98 0 .29.2.64.75.53 4.26-1.42 7.34-5.43 7.34-10.17C22.5 6.35 17.8 1.5 12 1.5z" />
    </svg>
  );
}

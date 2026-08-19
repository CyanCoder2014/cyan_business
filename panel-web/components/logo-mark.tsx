export function LogoMark({ size = 44 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <defs>
        <linearGradient id="cyanLogoGradient" x1="0%" y1="100%" x2="100%" y2="0%">
          <stop offset="0%" stopColor="#0EA5A6" />
          <stop offset="100%" stopColor="#22D3EE" />
        </linearGradient>
      </defs>
      <g fill="none" strokeLinecap="round">
        <path d="M330.2,309.3 A170,170 0 1 1 330.2,90.7" stroke="url(#cyanLogoGradient)" strokeWidth="26" />
        <path d="M104.3,119.6 A125,125 0 1 1 104.3,280.4" stroke="currentColor" strokeWidth="22" />
        <path d="M261.3,251.4 A80,80 0 1 1 261.3,148.6" stroke="url(#cyanLogoGradient)" strokeWidth="18" />
        <path d="M165.4,180 A40,40 0 1 1 165.4,220" stroke="currentColor" strokeWidth="14" />
      </g>
    </svg>
  );
}

import type { ReactNode } from 'react';
export function PageHeader({
  eyebrow,
  title,
  children,
}: {
  eyebrow: string;
  title: string;
  children?: ReactNode;
}) {
  return (
    <header className="flex flex-col gap-4 border-b border-atelier-stone bg-white px-5 py-6 md:flex-row md:items-end md:justify-between md:px-9">
      <div>
        <p className="text-xs font-semibold uppercase tracking-[.2em] text-atelier-accent">{eyebrow}</p>
        <h1 className="mt-1 text-3xl font-semibold">{title}</h1>
      </div>
      {children}
    </header>
  );
}

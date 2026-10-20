import type { InputHTMLAttributes } from 'react';
export function FormField({
  label,
  error,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string }) {
  return (
    <label className="grid gap-1 text-sm font-medium">
      {label}
      <input
        className="rounded-md border border-atelier-stone bg-white px-3 py-2.5 shadow-sm"
        aria-invalid={Boolean(error)}
        {...props}
      />
      {error && <span className="text-sm text-red-700">{error}</span>}
    </label>
  );
}

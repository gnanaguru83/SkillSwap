export default function LoadingSpinner({ label = 'Loading...' }) {
  return <div className="flex min-h-[240px] flex-col items-center justify-center gap-3 text-gray-500"><div className="h-10 w-10 animate-spin rounded-full border-4 border-purple-100 border-t-purple-600" /><span className="text-sm font-medium">{label}</span></div>;
}
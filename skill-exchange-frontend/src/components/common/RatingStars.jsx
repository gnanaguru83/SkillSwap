import { Star } from 'lucide-react';
export default function RatingStars({ rating = 0, count, size = 18, interactive = false, onChange }) {
  const rounded = Math.round(Number(rating) || 0);
  return <div className="flex items-center gap-1">{[1,2,3,4,5].map((n) => <button key={n} type="button" disabled={!interactive} onClick={() => onChange?.(n)} className={`transition ${interactive ? 'hover:scale-125' : ''}`}><Star size={size} className={n <= rounded ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'} /></button>)}{count !== undefined && <span className="ml-2 text-sm text-gray-500">({count})</span>}</div>;
}
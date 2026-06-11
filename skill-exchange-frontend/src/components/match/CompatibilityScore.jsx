export default function CompatibilityScore({ score = 0 }) {
  const band =
    score >= 80
      ? { color: 'bg-emerald-500', text: 'Excellent match' }
      : score >= 60
      ? { color: 'bg-green-500', text: 'Strong match' }
      : score >= 40
      ? { color: 'bg-amber-500', text: 'Promising match' }
      : score >= 21
      ? { color: 'bg-orange-400', text: 'Potential match' }
      : { color: 'bg-gray-400', text: 'Add skills to match' };
  return (
    <div>
      <div className="mb-2 flex items-center justify-between text-sm">
        <span className="font-bold text-gray-900">{score}% match</span>
        <span className="text-gray-500">{band.text}</span>
      </div>
      <div className="h-2.5 overflow-hidden rounded-full bg-gray-100">
        <div className={`h-full rounded-full ${band.color}`} style={{ width: `${Math.min(score, 100)}%` }} />
      </div>
    </div>
  );
}

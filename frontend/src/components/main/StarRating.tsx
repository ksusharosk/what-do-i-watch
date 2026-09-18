import { Star } from "lucide-react";

// Shows a 0–5 star rating with partial fill (e.g. 4.2 = 4 full + 1 star 20% filled).
export function StarRating({ value }: { value: number }) {
  return (
    <div className="flex gap-0.5">
      {[0, 1, 2, 3, 4].map((i) => {
        // How much of THIS star is filled: full (1), empty (0), or partial.
        const fill = Math.max(0, Math.min(1, value - i));
        return (
          <div key={i} className="relative size-4">
            {/* Empty star underneath */}
            <Star className="absolute size-4 text-muted-foreground" />
            {/* Filled star on top, clipped to the fill fraction */}
            <div
              className="absolute overflow-hidden"
              style={{ width: `${fill * 100}%` }}
            >
              <Star className="size-4 fill-yellow-400 text-yellow-400" />
            </div>
          </div>
        );
      })}
    </div>
  );
}
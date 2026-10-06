#!/bin/sh
# Prints line/branch coverage per module from the JaCoCo CSV reports (target/site/jacoco/jacoco.csv).
printf '%-16s %-18s %-18s\n' MODULE LINES BRANCHES
for m in domain application infrastructure web; do
  f="$m/target/site/jacoco/jacoco.csv"
  [ -f "$f" ] || { printf '%-16s (no report)\n' "$m"; continue; }
  awk -F, -v m="$m" 'NR>1 { bm+=$6; bc+=$7; lm+=$8; lc+=$9 }
    END { tl=lm+lc; tb=bm+bc;
      printf "%-16s %d/%d = %.1f%%   %s\n", m, lc, tl, (tl? 100*lc/tl : 100),
        (tb? sprintf("%d/%d = %.1f%%", bc, tb, 100*bc/tb) : "- (no branches)") }' "$f"
done

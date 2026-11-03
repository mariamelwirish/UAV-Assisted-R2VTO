#!/usr/bin/env bash
# Usage: ./run.sh [pc|muV|cmax|lambda|muVload|all]   (default: all)
set -e
cd "$(dirname "$0")"
mkdir -p out Results Plots
javac -d out *.java
java -cp out Driver "${1:-all}"
MATLAB=$(command -v matlab || ls -d /Applications/MATLAB_*.app/bin/matlab /usr/local/MATLAB/*/bin/matlab 2>/dev/null | tail -1)
if [ -z "$MATLAB" ]; then
  echo "MATLAB not found: results are in Results/, run plot_all in MATLAB manually."
  exit 0
fi
"$MATLAB" -nodesktop -r "plot_all"
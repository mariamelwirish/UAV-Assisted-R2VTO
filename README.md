# UAV-Assisted RSU-to-Vehicle Task Offloading.

A Java discrete-event simulator of **RSU-to-Vehicle Task Offloading (R2VTO)** in Vehicular Fog Computing, following the system model of [Khabbaz et al](https://doi.org/10.1109/TVT.2026.3690457). An RSU buffers deadline-constrained tasks in a finite FCFS queue and releases the Head-of-Line (HoL) task to an incoming vehicle only if the vehicle is cooperative and can complete the task before leaving the RSU's coverage range.

This version extends the model with bypassing UAVs that relay tasks to approaching vehicles before they enter the RSU's coverage range. Every experiment runs a baseline (no UAV) and a UAV-assisted scenario on identical task and vehicle realizations.

## Requirements
- Java JDK 8+
- MATLAB R2019b+ (for plots; found automatically on macOS/Linux default installs)

## Run
macOS / Linux / Windows (Git Bash):
```bash
./run.sh            # all experiments + all plots
./run.sh pc         # one experiment
```
Windows (Command Prompt):
```
javac -d out *.java
java -cp out Driver all
matlab -batch "plot_all"
```
If MATLAB is not found, results are still written to `Results/`; run `plot_all` in MATLAB manually.

## Experiments
| Name | Varies | Paper figure | Output CSV |
|---|---|---|---|
| `pc` | p_c | 3(a) | `Results/1_pr_vs_pc.csv` |
| `muV` | μ_V | 3(b) | `Results/1_pr_vs_muV.csv` |
| `cmax` | C_max | 3(c) | `Results/1_pr_vs_cmax.csv` |
| `lambda` | λ_t (3 C_max panels) | 5(a,b) | `Results/1_lambda_C{3,5,8}.csv` |
| `muVload` | μ_V under load | 4(c), 6 | `Results/2_muV_C{3,5,8}.csv` |

Figures are written to `Plots/<experiment>/`.

## Snapshot
`v1`: first version of the results.

## Reference
M. Khabbaz, H. Hijazi, R. Atallah, and C. Assi, "Impact of Vehicular Processing Capacity, Mobility Dynamics and Cooperativeness on R2V Task Offloading Performance in Vehicular Fog Networks," *IEEE Transactions on Vehicular Technology*, vol. 75, no. 9, pp. 21444–21458, Sep. 2026. doi: [10.1109/TVT.2026.3690457](https://doi.org/10.1109/TVT.2026.3690457)
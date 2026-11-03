function compare(experiment, xLabelText)

% ---------- Read the results ----------
file = fullfile('Results', [experiment '.csv']);
if ~isfile(file)
    error('File not found: %s', file);
end
data = readtable(file, 'TextType', 'string');

% ---------- What to plot ----------
metrics = {'pr', 'Ts', 'pb', 'pe', 'Nt', 'Qt', 'uavShare', 'attemptsPerVehicle'};
yLabels = {'p_r', 'T_S (s)', 'p_b', 'p_e', 'N_t (tasks)', 'Q_t (s)', ...
           'UAV share of releases', 'Offers per vehicle'};
scenarios  = ["base", "uav"];
legendText = {'Baseline (no UAV)', 'With UAV'};
markers    = {'-o', '-s'};

% ---------- Output folder ----------
outDir = fullfile('Plots', experiment);
if ~exist(outDir, 'dir'); mkdir(outDir); end

% ---------- One window, one tab per metric ----------
mainFig  = figure('Name', ['Baseline vs UAV: ' experiment], 'Position', [100 100 1100 700]);
tabGroup = uitabgroup(mainFig);

for m = 1:numel(metrics)
    name = metrics{m};
    tab  = uitab(tabGroup, 'Title', name);
    ax   = axes('Parent', tab);
    hold(ax, 'on');

    for s = 1:numel(scenarios)
        rows = data(data.scenario == scenarios(s), :);   % only base, or only uav
        xs = unique(rows.x);
        mu = zeros(size(xs));
        ci = zeros(size(xs));

        for k = 1:numel(xs)
            y = rows.(name)(rows.x == xs(k));             % all reps at this x
            n = numel(y);
            mu(k) = mean(y);
            if n > 1
                ci(k) = tCritical(n - 1) * std(y) / sqrt(n);   % 95% CI
            end
        end

        errorbar(ax, xs, mu, ci, markers{s}, 'LineWidth', 1.8, ...
                 'MarkerSize', 7, 'CapSize', 6);
    end

    hold(ax, 'off'); grid(ax, 'on'); box(ax, 'on');
    xlabel(ax, xLabelText, 'FontSize', 12);
    ylabel(ax, yLabels{m}, 'FontSize', 12);
    title(ax, sprintf('%s vs %s', yLabels{m}, xLabelText), 'FontSize', 13);
    legend(ax, legendText, 'Location', 'best');

    exportgraphics(ax, fullfile(outDir, [name '.png']), 'Resolution', 300);
end

fprintf('Saved %d plots to %s\n', numel(metrics), outDir);

% ---------- Helper: t-value for a 95% CI (no toolbox needed) ----------
function t = tCritical(df)
    tbl = [12.706 4.303 3.182 2.776 2.571 2.447 2.365 2.306 2.262 2.228 ...
           2.201 2.179 2.160 2.145 2.131 2.120 2.110 2.101 2.093 2.086];
    if df <= numel(tbl)
        t = tbl(df);
    else
        t = 1.96;
    end
end

fprintf('Saved %d plots to %s\n', numel(metrics), outDir);
end                                   % <-- closes compare

% ---------- Helper: t-value for a 95% CI (no toolbox needed) ----------
function t = tCritical(df)
    ...
end
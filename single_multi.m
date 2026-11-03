clear; clc; close all;

% ================== SETTINGS ==================
xLabelText = '\mu_V (veh/s)';
outDir     = fullfile('Plots', '2_muV');
rescuedCol = 'rescuedShare';     % <-- change if your CSV column has a different name

% Part 1: T_S, three panels (Fig. 4(c))
tsFiles  = {'2_muV_C3', '2_muV_C5', '2_muV_C8'};
tsPanels = {'C_{max} = 3\times10^8', 'C_{max} = 5\times10^8', 'C_{max} = 8\times10^8'};

% Part 2: single plots from C_max = 8e8 (Fig. 6)
singleFile    = '2_muV_C8';
singleMetrics = {'pa', 'pe', 'Qt', rescuedCol};
singleLabels  = {'p_a', 'p_e', 'Q_t (s)', 'Rescued by UAV (share of releases)'};
% ==============================================

if ~exist(outDir, 'dir'); mkdir(outDir); end
scen    = ["base", "uav"];
styles  = {'-o', '-s'};
legText = {'Baseline (no UAV)', 'With UAV'};

% ---------- Part 1: T_S in three panels ----------
fig = figure('Name', 'T_S vs mu_V', 'Position', [100 100 1500 450]);
tl  = tiledlayout(fig, 1, numel(tsFiles), 'TileSpacing', 'compact');
axs = gobjects(1, numel(tsFiles));
for f = 1:numel(tsFiles)
    data = readtable(fullfile('Results', [tsFiles{f} '.csv']), 'TextType', 'string');
    ax = nexttile(tl); axs(f) = ax; hold(ax, 'on');
    for s = 1:2
        rows = data(data.scenario == scen(s), :);
        xs = unique(rows.x);
        mu = arrayfun(@(xv) mean(rows.Ts(rows.x == xv)), xs);
        plot(ax, xs, mu, styles{s}, 'LineWidth', 1.8, 'MarkerSize', 6);
    end
    hold(ax, 'off'); grid(ax, 'on'); box(ax, 'on');
    title(ax, tsPanels{f}, 'FontSize', 12);
    xlabel(ax, xLabelText, 'FontSize', 11);
    if f == 1
        ylabel(ax, 'T_S (s)', 'FontSize', 11);
        legend(ax, legText, 'Location', 'best');
    end
end
linkaxes(axs, 'y');
title(tl, ['T_S vs ' xLabelText], 'FontSize', 14);
exportgraphics(fig, fullfile(outDir, 'Ts_panels.png'), 'Resolution', 300);

% ---------- Part 2: single plots (C_max = 8e8), one tab each ----------
data = readtable(fullfile('Results', [singleFile '.csv']), 'TextType', 'string');
mainFig  = figure('Name', 'S2: C_max = 8e8', 'Position', [100 100 1100 700]);
tabGroup = uitabgroup(mainFig);
for m = 1:numel(singleMetrics)
    name = singleMetrics{m};
    tab  = uitab(tabGroup, 'Title', name);
    ax   = axes('Parent', tab); hold(ax, 'on');
    for s = 1:2
        rows = data(data.scenario == scen(s), :);
        xs = unique(rows.x);
        mu = arrayfun(@(xv) mean(rows.(name)(rows.x == xv)), xs);
        plot(ax, xs, mu, styles{s}, 'LineWidth', 1.8, 'MarkerSize', 7);
    end
    hold(ax, 'off'); grid(ax, 'on'); box(ax, 'on');
    xlabel(ax, xLabelText, 'FontSize', 12);
    ylabel(ax, singleLabels{m}, 'FontSize', 12);
    title(ax, sprintf('%s vs %s  (C_{max} = 8\\times10^8)', singleLabels{m}, xLabelText), 'FontSize', 13);
    legend(ax, legText, 'Location', 'best');
    exportgraphics(ax, fullfile(outDir, [name '_C8.png']), 'Resolution', 300);
end

fprintf('Saved plots to %s\n', outDir);
clear; clc; close all;

% ================== SETTINGS ==================
files      = {'1_lambda_C3', '1_lambda_C5', '1_lambda_C8'};
panelNames = {'C_{max} = 3\times10^8', 'C_{max} = 5\times10^8', 'C_{max} = 8\times10^8'};
xLabelText = '\lambda_t (tasks/s)';
outName    = 'S1_lambda';
metrics    = {'Nt', 'Qt', 'pe', 'pa', 'uavShare'};
yLabels    = {'N_t (tasks)', 'Q_t (s)', 'p_e', 'p_a', 'UAV share of releases'};
% ==============================================

outDir = fullfile('Plots', outName);
if ~exist(outDir, 'dir'); mkdir(outDir); end
data = cellfun(@(f) readtable(fullfile('Results', [f '.csv']), 'TextType', 'string'), ...
               files, 'UniformOutput', false);

for m = 1:numel(metrics)
    name = metrics{m};
    fig = figure('Name', yLabels{m}, 'Position', [100 100 1500 450]);
    tl  = tiledlayout(fig, 1, numel(files), 'TileSpacing', 'compact');
    axs = gobjects(1, numel(files));

    for f = 1:numel(files)
        ax = nexttile(tl); axs(f) = ax; hold(ax, 'on');
        scen = ["base", "uav"]; styles = {'-o', '-s'};
        for s = 1:2
            rows = data{f}(data{f}.scenario == scen(s), :);
            xs = unique(rows.x);
            mu = arrayfun(@(xv) mean(rows.(name)(rows.x == xv)), xs);
            plot(ax, xs, mu, styles{s}, 'LineWidth', 1.8, 'MarkerSize', 6);
        end
        hold(ax, 'off'); grid(ax, 'on'); box(ax, 'on');
        title(ax, panelNames{f}, 'FontSize', 12);
        xlabel(ax, xLabelText, 'FontSize', 11);
        if f == 1; ylabel(ax, yLabels{m}, 'FontSize', 11); end
        if f == 1; legend(ax, {'Baseline (no UAV)', 'With UAV'}, 'Location', 'best'); end
    end

    linkaxes(axs, 'y');            % same y-scale on all panels, so they're comparable
    title(tl, sprintf('%s vs %s', yLabels{m}, xLabelText), 'FontSize', 14);
    exportgraphics(fig, fullfile(outDir, [name '.png']), 'Resolution', 300);
end
fprintf('Saved plots to %s\n', outDir);
% Plots UAV arrival-rate sweep from Results/uav_rate.txt
% Column 1 = UAV arrival rate (0 = baseline, no UAV), each other column = a metric.
clear; clc;

filePath = fullfile('Results', 'uav_rate.txt');
if ~isfile(filePath)
    error('Results/uav_rate.txt not found.');
end

data = readtable(filePath, 'VariableNamingRule', 'preserve');

% --- sort by UAV rate so the baseline (0) is leftmost and points are ordered ---
data = sortrows(data, 1);

outputDir = 'Plots';
if ~exist(outputDir, 'dir')
    mkdir(outputDir);
end
oldPlots = dir(fullfile(outputDir, '*.png'));
for k = 1:numel(oldPlots)
    delete(fullfile(oldPlots(k).folder, oldPlots(k).name));
end

mainFig = figure('Name', 'UAV Arrival Rate Sweep', 'Position', [100, 100, 1100, 700]);
tabGroup = uitabgroup(mainFig);

varNames = data.Properties.VariableNames;
xVar   = data.(varNames{1});             % UAV arrival rate
xLabel = prettifyName(varNames{1});

for c = 2:width(data)
    yVar = data.(varNames{c});
    if ~isnumeric(xVar) || ~isnumeric(yVar)
        warning('Skipping non-numeric column %s.', varNames{c});
        continue;
    end

    yLabel   = prettifyName(varNames{c});
    tabTitle = sprintf('%s VS %s', yLabel, xLabel);
    tab = uitab(tabGroup, 'Title', tabTitle);
    ax  = axes('Parent', tab);

    plot(ax, xVar, yVar, '-o', 'LineWidth', 2, 'MarkerSize', 7);
    hold(ax, 'on');
    % highlight the baseline point (UAV rate = 0) in a different marker
    if xVar(1) == 0
        plot(ax, xVar(1), yVar(1), 'sk', 'MarkerSize', 11, 'LineWidth', 2, ...
             'MarkerFaceColor', [0.85 0.85 0.85]);
    end
    hold(ax, 'off');
    grid(ax, 'on');

    xlabel(ax, xLabel, 'FontSize', 12);
    ylabel(ax, yLabel, 'FontSize', 12);
    title(ax, tabTitle, 'FontSize', 14);
    set(ax, 'FontSize', 11);

    if xVar(1) == 0
        legend(ax, {'With UAV', 'Baseline (no UAV)'}, 'Location', 'best');
    else
        legend(ax, 'With UAV', 'Location', 'best');
    end

    outputFile = fullfile(outputDir, sprintf('%s VS %s.png', ...
        sanitizeFileName(yLabel), sanitizeFileName(xLabel)));
    exportgraphics(ax, outputFile, 'Resolution', 300);
end

function label = prettifyName(rawName)
    name = regexprep(rawName, '([a-z0-9])([A-Z])', '$1 $2');
    name = strrep(name, '_', ' ');
    label = regexprep(name, '\s+', ' ');
    label = strtrim(label);
end

function cleaned = sanitizeFileName(name)
    cleaned = regexprep(name, '[\\/:*?"<>|]', '_');
    cleaned = strrep(cleaned, ' ', '_');
    cleaned = regexprep(cleaned, '_+', '_');
    cleaned = strtrim(cleaned);
end
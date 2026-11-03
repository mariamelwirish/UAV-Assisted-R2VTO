% Compares baseline vs UAV results: overlays mu_base.txt and mu_uav.txt
% on the same axes, one tab per metric column.
clear; clc;

% --- CHANGED: load the two files explicitly instead of globbing ---
baseFile = fullfile('Results', 'mu_base.txt');
uavFile  = fullfile('Results', 'mu_uav.txt');

if ~isfile(baseFile) || ~isfile(uavFile)
    error('Need both Results/mu_base.txt and Results/mu_uav.txt.');
end

baseData = readtable(baseFile, 'VariableNamingRule', 'preserve');
uavData  = readtable(uavFile,  'VariableNamingRule', 'preserve');

outputDir = 'Plots';
if ~exist(outputDir, 'dir')
    mkdir(outputDir);
end
oldPlots = dir(fullfile(outputDir, '*.png'));
for k = 1:numel(oldPlots)
    delete(fullfile(oldPlots(k).folder, oldPlots(k).name));
end

mainFig = figure('Name', 'Baseline vs UAV', 'Position', [100, 100, 1100, 700]);
tabGroup = uitabgroup(mainFig);

varNames = baseData.Properties.VariableNames;
xVar   = baseData.(varNames{1});          % column 1 = x-axis (mu_v), same for both
xLabel = prettifyName(varNames{1});

% --- CHANGED: one tab per metric, two lines each ---
for c = 2:width(baseData)
    yBase = baseData.(varNames{c});
    yUav  = uavData.(varNames{c});         % same column in the UAV file

    if ~isnumeric(xVar) || ~isnumeric(yBase) || ~isnumeric(yUav)
        warning('Skipping non-numeric column %s.', varNames{c});
        continue;
    end

    yLabel   = prettifyName(varNames{c});
    tabTitle = sprintf('%s VS %s', yLabel, xLabel);
    tab = uitab(tabGroup, 'Title', tabTitle);
    ax  = axes('Parent', tab);

    plot(ax, xVar, yBase, '-o', 'LineWidth', 2, 'MarkerSize', 7); % baseline
    hold(ax, 'on');
    plot(ax, xVar, yUav,  '-s', 'LineWidth', 2, 'MarkerSize', 7); % with UAV
    hold(ax, 'off');
    grid(ax, 'on');

    xlabel(ax, xLabel, 'FontSize', 12);
    ylabel(ax, yLabel, 'FontSize', 12);
    title(ax, tabTitle, 'FontSize', 14);
    set(ax, 'FontSize', 11);
    legend(ax, {'Baseline (no UAV)', 'With UAV'}, 'Location', 'best');  % CHANGED

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
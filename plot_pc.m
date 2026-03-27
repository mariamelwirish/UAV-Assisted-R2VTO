% Generic plotting utility for simulation result files.
% For every file in Results/*.txt:
%   - Column 1 is used as x-axis.
%   - Each remaining column is plotted as a separate y-vs-x tab in one window.

clear; clc;

resultFiles = dir(fullfile('Results', '*.txt'));

if isempty(resultFiles)
	error('No .txt files found in Results folder.');
end

outputDir = 'Plots';
if ~exist(outputDir, 'dir')
	mkdir(outputDir);
end
oldPlots = dir(fullfile(outputDir, '*.png'));
for k = 1:numel(oldPlots)
	delete(fullfile(oldPlots(k).folder, oldPlots(k).name));
end

mainFig = figure('Name', 'Simulation Plots', 'Position', [100, 100, 1100, 700]);
tabGroup = uitabgroup(mainFig);

for f = 1:numel(resultFiles)
	filePath = fullfile(resultFiles(f).folder, resultFiles(f).name);
	data = readtable(filePath, 'VariableNamingRule', 'preserve');

	if width(data) < 2
		warning('Skipping %s: file must contain at least 2 columns.', resultFiles(f).name);
		continue;
	end

	varNames = data.Properties.VariableNames;
	xVar = data.(varNames{1});
	xLabel = prettifyName(varNames{1});

	[~, fileStem, ~] = fileparts(resultFiles(f).name);

	for c = 2:width(data)
		yVar = data.(varNames{c});

		if ~isnumeric(xVar) || ~isnumeric(yVar)
			warning('Skipping %s in %s: non-numeric x or y column.', varNames{c}, resultFiles(f).name);
			continue;
		end

		yLabel = prettifyName(varNames{c});

		tabTitle = sprintf('%s VS %s', yLabel, xLabel);
		tab = uitab(tabGroup, 'Title', sprintf('%s', tabTitle));
		ax = axes('Parent', tab);
		plot(ax, xVar, yVar, '-o', 'LineWidth', 2, 'MarkerSize', 7);
		grid(ax, 'on');

		xlabel(ax, xLabel, 'FontSize', 12);
		ylabel(ax, yLabel, 'FontSize', 12);
		title(ax, tabTitle, 'FontSize', 14);

		set(ax, 'FontSize', 11);
		legend(ax, 'Simulation', 'Location', 'best');

		outputFile = fullfile(outputDir, sprintf('%s VS %s.png', ...
			sanitizeFileName(yLabel), sanitizeFileName(xLabel)));
		exportgraphics(ax, outputFile, 'Resolution', 300);
	end
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

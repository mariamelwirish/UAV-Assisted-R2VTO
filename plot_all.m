if isfile('Results/1_pr_vs_pc.csv'),   compare('1_pr_vs_pc',   'p_c'); end
if isfile('Results/1_pr_vs_muV.csv'),  compare('1_pr_vs_muV',  '\mu_V (veh/s)'); end
if isfile('Results/1_pr_vs_cmax.csv'), compare('1_pr_vs_cmax', 'C_{max} (\times10^8 inst/s)'); end
if isfile('Results/1_lambda_C3.csv'),  multi;        end
if isfile('Results/2_muV_C3.csv'),     single_multi; end
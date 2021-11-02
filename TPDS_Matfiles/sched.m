
%%%%%% Fixed parameters %%%%%%
path_results = "../result_multi_daivd1/";
path_figs = '../TPDS_figures/';

wid = 800;
len = 300;

colors=[[0.8500 0.3250 0.0980]; [0 0.4470 0.7410];  [0.9290 0.6940 0.1250]; [0.4660, 0.6740, 0.1880]; [0.3010, 0.7450, 0.9330]; [0.6350, 0.0780, 0.1840]];
markers=['o', '*','^'];
systemNo = 1000;
legends = ["our","He2019","Zhao2020"];
xlabels = ["System Utilisation", "Parallel Degree of DAGs", "Number of Cores", "Number of Tasks"];

file_pre = ["util","paral","nopF","not"];
file_end = "sched";

startingVar = [1,2,2,1];
endingVar = [12,10,32,10];
space = [1,1,2,1];

for k = 1:length(xlabels)
    coloums = 1: (endingVar(k))/space(k);
    variable_range = startingVar(k):space(k):endingVar(k);
    data = zeros(endingVar(k)/space(k),length(legends));
    
    xticklabel = strings(1,endingVar(k)/space(k));
    
    count = 1;
    for i = startingVar(k):space(k):endingVar(k)
        file = file_pre(k) + "_" + i + "_" + file_end+".txt";
        data_per_file = readmatrix(strcat(path_results, file));
        data(count,:) = data_per_file;
        if(k==1)
            xticklabel(count) = strcat(string( 5  *i) + "%");
        else
            xticklabel(count) = string(variable_range(count));
        end
        
        count=count+1;
    end
    
    data = data / systemNo * 100;
    
    f=figure('Position', [100, 100, wid, len]);
    set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);

    for i = 1: length(legends)
        plot(coloums, data(:,i), 'color', colors(i,:), 'marker',markers(i), 'LineWidth', 2, 'MarkerSize', 10);
        hold on;
    end

    if(k==2)
        xlim([1, length(coloums)-1]);
    else
         xlim([1, length(coloums)]);
    end
    set(gca,'xtick',coloums);
    set(gca,'xticklabel',xticklabel,'FontSize',12);
    xlabel({xlabels(k)},'FontSize', 14)
    ylabel('System Schedulability (%)','FontSize', 14)

    h=legend(legends,'location','northeast','Orientation','vertical');
    if(k==2)
        h=legend(legends,'location','northeast','Orientation','horizontal');
    end
    if(k==3)
        h=legend(legends,'location','southeast','Orientation','vertical');
    end
    set(h,'FontSize',14);

    saveas(gcf,strcat(path_figs, strcat("ep_sched_", file_pre(k), ".eps")),'epsc');

end













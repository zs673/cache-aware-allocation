
%%%%%% Fixed parameters %%%%%%
path_results = "../result_multi_daivd1/";
path_figs = '../TPDS_figures/';

wid = 800;
len = 300;

colors=[[0.8500 0.3250 0.0980]; [0 0.4470 0.7410];  [0.9290 0.6940 0.1250]; [0.4660, 0.6740, 0.1880]; [0.3010, 0.7450, 0.9330]; [0.6350, 0.0780, 0.1840]];


file_name = ["our", "he"];
file_pre = "nopF";
file_end = ["intra","inter"];

startingVar = 8;
endingVar = 32;
space = 2;




for j = 1: length(file_end)
    f=figure('Position', [100, 100, wid, len]);
    set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);
    pos = 1;
    for x = startingVar:space:endingVar
        intra_our = [];
        inter_our = [];
        for i = 1: length(file_name)
            file_method = file_name(i); 

            file = file_pre + "_" + x + "_" + file_end(j) +"_" + file_method + ".txt";
            data_per_file = readmatrix(strcat(path_results, file));

            mean = reshape(data_per_file.',1,[]); %median(data_per_file);
            if i==1
                boxplot(mean, 'position', pos, 'widths', 0.65, 'symbol','.', 'color', colors(i,:));
                hold on;
            else
                boxplot(mean, 'position', pos+1, 'widths', 0.65, 'symbol','.', 'color', colors(i,:));
                hold on;
            end
        end 
        pos = pos + 2;
    end
    
    xlim([0.5, 26.5]);
    if(j==1)
        ylim([-500, 100000]);
    else
        ylim([-1000, 200000]);
    end
    
    legends = ["our","He2019"];

    coloums = 1.5: 2 : 32;
    
    xticklabel = strings(1,endingVar/space);
    count=1;
    for n = 8:2:32
        xticklabel(count) = n;
        count = count+1;
    end
    
    set(gca,'xtick',coloums);
    set(gca,'xticklabel',xticklabel,'FontSize',12);
    
    xlabel("Number of Cores m",'FontSize', 14)
    if(j==1)
       ylabel("Intra-task Interference",'FontSize', 14)
    else
       ylabel("Inter-task Interference",'FontSize', 14)
    end
    
    c = findall(gca,'Tag','Box');
    hleg1 = legend(c(1:2),legends,'location','northeast','Orientation','vertical');
    set(hleg1,'FontSize',14);
    
    if(j==1)
       saveas(gcf,strcat(path_figs, strcat("ep_intra_", file_pre, ".eps")),'epsc');
    else
       saveas(gcf,strcat(path_figs, strcat("ep_inter_", file_pre, ".eps")),'epsc');
    end
end


 
 
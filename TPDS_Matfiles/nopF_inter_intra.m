
wid = 800;
len = 300;

colors=[[0.8500 0.3250 0.0980]; [0 0.4470 0.7410];  [0.4660, 0.6740, 0.1880]; [0.3010, 0.7450, 0.9330]; [0.6350, 0.0780, 0.1840]];


file_name = ["our", "he","fed"];
file_pre = "nopF";
file_end = ["intra","inter"];

startingVar = 8;
endingVar = 32;
space = 4;




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
            
%             if(j==1 && x==12)
%                 p = prctile(mean, [0 94]);
%                 mean(mean >= p(2)) = []
%             elseif(j==1 && x>12)
%                 p = prctile(mean, [0 90]);
%                 mean(mean >= p(2)) = []
%             end
            
            if i==1
                boxplot(mean, 'position', pos, 'widths', 0.65, 'symbol','.', 'color', colors(i,:),'symbol', '');
                hold on;
            elseif i == 2
                boxplot(mean, 'position', pos+1, 'widths', 0.65, 'symbol','.', 'color', colors(i,:),'symbol', '');
                hold on;
            else
                boxplot(mean, 'position', pos+2, 'widths', 0.65, 'symbol','.', 'color', colors(i,:),'symbol', '');
                hold on;
            end
        end
        pos = pos + 3;
    end
    
    xlim([0.5, 21.5]);
    if(j==1)
        ylim([-1000, 70000]);
    else
        ylim([-1000, 70000]);
    end
    
    legends = ["mDAG-CA","He2019","Baseline"];
    
    coloums = 2: 3:32;
    
    xticklabel = strings(1,endingVar/space);
    count=1;
    for n = startingVar:space:endingVar
        xticklabel(count) = n;
        count = count+1;
    end
    
    set(gca,'xtick',coloums);
    set(gca,'xticklabel',xticklabel,'FontSize',12);
    
    xlabel("Number of Cores",'FontSize', 14)
    if(j==1)
        ylabel("Intra-task Interference",'FontSize', 14)
    else
        ylabel("Inter-task Interference",'FontSize', 14)
    end
    
    if(j==1)
        c = findall(gca,'Tag','Box');
        hleg1 = legend([c(3),c(2),c(1)],legends,'location','northwest','Orientation','horizontal');
        set(hleg1,'FontSize',14);
        rect = [0.139583333333333 0.797777780161964 0.474999991543591 0.0933333309491475];
        set(hleg1, 'Position', rect)
    else
        c = findall(gca,'Tag','Box');
        hleg1 = legend([c(3),c(2),c(1)],legends,'location','northeast','Orientation','horizontal');
        set(hleg1,'FontSize',14);
    end
    
    
    if(j==1)
        saveas(gcf,strcat(path_figs, strcat("ep_intra_", file_pre, "1.eps")),'epsc');
    else
        saveas(gcf,strcat(path_figs, strcat("ep_inter_", file_pre, "1.eps")),'epsc');
    end
end




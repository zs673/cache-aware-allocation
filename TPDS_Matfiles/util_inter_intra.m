
%%%%%% Fixed parameters %%%%%%
path_results = "../result_multi_daivd2/";
path_figs = '../TPDS_figures/';

wid = 800;
len = 300;

colors=[[0.8500 0.3250 0.0980]; [0 0.4470 0.7410];  [0.9290 0.6940 0.1250]; [0.4660, 0.6740, 0.1880]; [0.3010, 0.7450, 0.9330]; [0.6350, 0.0780, 0.1840]];


file_name = ["our", "he"];
file_pre = "util";
file_end = ["intra","inter"];

startingVar = 1;
endingVar = 6;
space = 1;




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
                boxplot(mean, 'position', pos, 'widths', 0.65, 'symbol','.', 'color', colors(i,:),'symbol', '');
                hold on;
            else
                boxplot(mean, 'position', pos+1, 'widths', 0.65, 'symbol','.', 'color', colors(i,:),'symbol', '');
                hold on;
            end
        end 
        pos = pos + 2;
    end
    
    xlim([0.5, 12.5]);
%     if(j==1)
%         ylim([-500, 100000]);
%     else
        ylim([-3000, 60000]);
%     end
    
    legends = ["mDAG-CA","He2019"];

    coloums = 1.5: 2:20;
    
    xticklabel = strings(1,endingVar/space);
    for n = startingVar:space:endingVar
        xticklabel(n) = strcat(string( 5  * n) + "%");
    end
    
    set(gca,'xtick',coloums);
    set(gca,'xticklabel',xticklabel,'FontSize',12);
    
    xlabel("System Utilisation",'FontSize', 14)
    if(j==1)
       ylabel("Intra-task Interference",'FontSize', 14)
    else
       ylabel("Inter-task Interference",'FontSize', 14)
    end
    
    c = findall(gca,'Tag','Box');
    hleg1 = legend(c(2:3),legends,'location','northwest','Orientation','vertical');
    set(hleg1,'FontSize',14);
    if(j==1)
       saveas(gcf,strcat(path_figs, strcat("ep_intra_", file_pre, ".eps")),'epsc');
    else
       saveas(gcf,strcat(path_figs, strcat("ep_inter_", file_pre, ".eps")),'epsc');
    end
end


 
 
 
 
% 
% for i = 1: length(file_end)
%     
% count = 1;
%     for x = startingVar(k):space(k):endingVar(k)
%         file_method = file_name(2); 
% 
%         file = file_pre(1) + "_" + x + "_" + file_end(i) +"_" + file_method + ".txt";
%         data_per_file = readmatrix(strcat(path_results, file));
% 
%         mean = median(data_per_file);
%         if i==1
%             intra_he(count,:) = mean;
%         else
%             inter_he(count,:) = mean;
%         end
%         count = count + 1;
%     end
% end



    


% xticklabel = strings(1,endingVar(k)/space(k));



    
% ylim([0, 200000]);
    
   

%     for i = 1: length(legends)
%         plot(coloums, data(:,i), 'color', colors(i,:), 'marker',markers(i), 'LineWidth', 2, 'MarkerSize', 10);
%         hold on;
%     end
% 
%     if(k==2)
%        
%     else
%          xlim([1, length(coloums)]);
%     end
%     set(gca,'xtick',coloums);
%     set(gca,'xticklabel',xticklabel,'FontSize',12);
%     xlabel({xlabels(k)},'FontSize', 14)
%     ylabel('System Schedulability (%)','FontSize', 14)
% 
%     h=legend(legends,'location','northeast','Orientation','vertical');
%     if(k==2)
%         h=legend(legends,'location','northeast','Orientation','horizontal');
%     end
%     if(k==3)
%         h=legend(legends,'location','southeast','Orientation','vertical');
%     end
%     set(h,'FontSize',14);
% 
%     saveas(gcf,strcat(path_figs, strcat("ep_sched_", file_pre(k), ".eps")));
% end













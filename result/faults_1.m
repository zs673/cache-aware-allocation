close all

cores = [4,8];
percents = [0.1,0.2,0.3,0.4,0.5];
effects = [0.1,0.2,0.3,0.4,0.5];
instanceNums = [1,3,5,10];

for core = cores
    data = readmatrix(strcat('faults/out_',num2str(core),'_',num2str(0.5),'_',num2str(0.5),'_',num2str(1),'.txt'));
colors=[[0.8500 0.3250 0.0980]; [0 0.4470 0.7410];  [0.9290 0.6940 0.1250]; [0.4660, 0.6740, 0.1880]; [0.3010, 0.7450, 0.9330]; [0.6350, 0.0780, 0.1840]];

f=figure('Position', [100, 100, 1200, 350]);

[noRows, noColumn] = size(data);

for i = 1: noColumn
	boxplot(data(:,i), 'position', i, 'widths', 0.65, 'symbol','.');
    hold on;
end

% set(gca,'XTick',["no error", "all error"], 'YTick', [])

line([2.5 2.5], [0 100000],'LineStyle',':','color','k','LineWidth',1);
line([4.5 4.5], [0 100000],'LineStyle',':','color','k','LineWidth',1);
line([6.5 6.5], [0 100000],'LineStyle',':','color','k','LineWidth',1);
line([8.5 8.5], [0 100000],'LineStyle',':','color','k','LineWidth',1);

xticks = 1 : noColumn;

row1 = {'no error' 'all error' 'non-critical error' 'critical error' 'low path number' 'high path number' 'low ET' 'high ET' 'no sensitive' 'all sensitive'};


set(gca,'xtick',xticks );
set(gca,'xticklabel',row1,'fontsize',14);

ylabel('makespan','FontSize', 14)

% saveas(gcf,strcat('faults/faults_1.eps'), 'epsc');
% saveas(gcf,strcat('faults/out_',num2str(core),'_',num2str(0.5),'_',num2str(0.5),'_',num2str(1),'.png'));
saveas(gcf,strcat('faults/out_',num2str(core),'.png'));
end


f=figure('Position', [100, 100, 1200, 350]);
count =1;
for percent = percents
    data = readmatrix(strcat('faults/out_',num2str(4),'_',num2str(percent),'_',num2str(0.5),'_',num2str(1),'.txt'));
    
    count= count + 1;
end




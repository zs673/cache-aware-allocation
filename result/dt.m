close all

absolute = 0;

f=figure('Position', [100, 100, 800, 350]);
set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);

data = readmatrix(strcat('real/cores.txt'));

col = size(data,2);

if absolute == 0
    for i = 1 : 3 : col
        normalizer = max(max(data(:,i:i+2)));
        
        boxplot(data(:,i)/normalizer, 'position',i, 'widths', 0.65, 'symbol','.', 'color', 'b');
        hold on
        
        boxplot(data(:,i+1)/normalizer, 'position',i+1, 'widths', 0.65, 'symbol','.', 'color', 'r');
        hold on
        
        boxplot(data(:,i+2)/normalizer, 'position',i+2, 'widths', 0.65, 'symbol','.', 'color', 'k');
        hold on
    end
    ylabel('Normalised Makespan','FontSize', 16)
else
    for i = 1 : 3 : col
        boxplot(data(:,i), 'position',i, 'widths', 0.65, 'symbol','.', 'color', 'b');
        hold on
        
        boxplot(data(:,i+1), 'position',i+1, 'widths', 0.65, 'symbol','.', 'color', 'r');
        hold on
        
        boxplot(data(:,i+2), 'position',i+2, 'widths', 0.65, 'symbol','.', 'color', 'k');
        hold on
    end
    
    ylabel('Makespan','FontSize', 16)
end

xticklables= 3 : col;
set(gca,'xticklabel',xticklables,'FontSize', 14);
xticks = 2 : 3 : col;
set(gca,'xtick',xticks );

c = findobj(gca,'Tag','Box');
legend([c(3),c(2),c(1)],"AJLR", "WF", "FIFO",'Orientation','horizontal','location','northoutside','FontSize', 14);

% ylim([0.65 1])
xlabel('Number of Cores','FontSize', 16)

set(gcf, 'PaperSize', [25 25])
saveas(gcf,strcat('figs/simu_cores.png'));
saveas(gcf,strcat('figs/simu_cores.eps'), 'epsc');

%% number of tasks

startTaskNum = 3;

f=figure('Position', [100, 100, 800, 350]);
set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);

data = readmatrix(strcat('real/tasks.txt'));

col = size(data,2);

if absolute == 0
    for i = 1 :3 : col
        normalizer = max(max(data(:,i:i+2)));
        
        boxplot(data(:,i)/normalizer, 'position',i, 'widths', 0.65, 'symbol','.', 'color', 'b');
        hold on
        
        boxplot(data(:,i+1)/normalizer, 'position',i+1, 'widths', 0.65, 'symbol','.', 'color', 'r');
        hold on
        
        boxplot(data(:,i+2)/normalizer, 'position',i+2, 'widths', 0.65, 'symbol','.', 'color', 'k');
        hold on
    end
    ylabel('Normalised Makespan','FontSize', 16)
else
    for i = 1 : 3: col
        boxplot(data(:,i), 'position',i, 'widths', 0.65, 'symbol','.', 'color', 'b');
        hold on
        
        boxplot(data(:,i+1), 'position',i+1, 'widths', 0.65, 'symbol','.', 'color', 'r');
        hold on
        
        boxplot(data(:,i+2), 'position',i+2, 'widths', 0.65, 'symbol','.', 'color', 'k');
        hold on
    end
    ylabel('Makespan','FontSize', 16)
end

xticklables= startTaskNum : col;
set(gca,'xticklabel',xticklables,'FontSize', 14);
xticks = 2 : 3 : col;
set(gca,'xtick',xticks );

c = findobj(gca,'Tag','Box');
legend([c(3),c(2),c(1)],"AJLR", "WFD", "FIFO",'Orientation','horizontal','location','northoutside','FontSize', 14);

xlabel('Number of Tasks','FontSize', 16)

set(gcf, 'PaperSize', [25 25])
saveas(gcf,strcat('figs/simu_tasks.png'));
saveas(gcf,strcat('figs/simu_tasks.eps'), 'epsc');

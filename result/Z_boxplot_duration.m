close all;

folder = 'duration_';
taskNum = 8;
systemPerMethod = 1000;
colors=['r','b','k'];

comparingMethod=['Worst-fit','Cache-aware online', 'Cache-aware offline'];

for duration = 1 : taskNum
    f=figure('Position', [100, 100, 1200, 400]);
    set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);
    
    data = readmatrix(strcat('taskNum/',folder ,num2str(duration), '.txt'));
    instanceNo = readmatrix(strcat('taskNum/','instanceNum_',num2str(duration), '.txt'));
    
    colsNum = size(data,2) - 1;
    rowsNum = size(data,1);
    
    methodNum = rowsNum/systemPerMethod;
    dataByMethod = cell(1,methodNum);
    
    % get data by each method
    for m = 1: methodNum
        startIndex = 1 + (m-1) * systemPerMethod;
        endIndex = m * systemPerMethod;
        dataByMethod{m} = data(startIndex :endIndex,:);
    end
    
    % plot all data by boxplot
    for m=1:methodNum
        datam = dataByMethod{m};
        
        pos = zeros(1);
        for col = 1:colsNum
            pos(col) = (col-1) * methodNum + m;
        end
        
        h1 = boxplot(datam(:,1:colsNum), 'position', pos, 'widths', 0.65, 'symbol','', 'color', colors(m));
        hold on;
    end
    
    
    xTick_start = sum(1:methodNum) /methodNum ;
    xTick_space = methodNum;
    xticks = zeros(1);
    xticklables = zeros(1);
    
    % add xticks, xticklables.
    counter = 1;
    for task = 1 : length(instanceNo)
        instanceNumber = instanceNo(task);
        
        
        for col = 1:instanceNumber
            xticks(counter) = xTick_start + xTick_space * (counter-1);
            xticklables(counter) = col;
            counter=counter+1;
        end
        
    end
    
    xlim([0 colsNum*methodNum+1]);
    xlabel({'Instances of DAGs'})
    set(gca,'xtick',xticks );
    set(gca,'xticklabel',xticklables);
    
    ylim([0, 1.0]);
    ylabel('Normalised makespan','FontSize', 12)
    
%     c = findobj(gca,'Tag','Box');
%     legend([c(3),c(4)],'LNF','Cache-aware','FontAngle','italic','location','northeast');
    
    set(gcf, 'PaperSize', [25 25])
    saveas(gcf,strcat('taskNum/','Z_',folder,num2str(duration),'.pdf'));
end

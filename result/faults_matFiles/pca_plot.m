clc
close all

colors=["b"; "r"];
types = ["nodeET","pathET","in\_degree","out\_degree","in\_out\_degree","pathNum"];
% judgement = ["0.1", "0.2", "0.3", "0.4", "0.5"];
judgements = ["0.1"];
threads = 0:3;

% effects = ["0.5","0.8","1.0","2.0","4.0","5.0","10.0"];
effects = ["1.0"];

for core = cores
    for judgement = judgements
        for effect = effects
            
            allData = zeros(0);
            for thread = threads
                data = readmatrix(strcat('../faults_new/random_',num2str(core),'_',judgement,'_',effect,'_',num2str(thread),'.txt'));
                allData = [allData; data(:,1:2:13)];
            end
            
            figure;
            
            allY= zeros(0);
            for i = 1:6
                y = allData(allData(:,i)==1,:);
                y1 = y(:,7);
                boxplot(y1, "Position", i, 'Whisker',1)
                hold on
                %             allY = [allY y1];
            end
            
            figure
            for i = 1:6
                subplot(6,1,i)
                y = allData(allData(:,i)==1,:);
                y1 = y(:,7);
             
                histogram(y1,100);
                xlim([-0.3 0.3])
                ylim([0 8000])
                
                ylabel('frequency', 'FontSize', 12);
                xlabel('impact on makespan', 'FontSize', 12);
                legend(types(i), 'FontSize', 10)
                
                
                %             allY = [allY y1];
            end
            
           
            
            
           [r,p] = corrcoef(allData);
           [coeff,score,latent,tsquared,explained,mu] = pca(allData(:,1:6));
           
           anova1(allData);
           explained
        end
    end
end
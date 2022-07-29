clc
close all

cores = [4];
colors=["b"; "r"];
types = ["$C_j$","$\hat{L}(v_j)$","$D^{in}_j$","$D^{out}_j$","$D_j$","$||G(v_j)||$"];
types_names = ["nodeET","pathET","in-degree","out-degree","in_out_degree","pathNum"];

% judgement = ["0.1", "0.2", "0.3", "0.4", "0.5"];
judgements = ["0.1"];
threads = 0;

% effects = ["0.5","0.8","1.0","2.0","4.0","5.0","10.0"];
effects = ["1.0"];

for core = cores
    for judgement = judgements
        for effect = effects
            
            allData = zeros(0);
            for thread = threads
                data = readmatrix(strcat('../faults_new/random_',num2str(core),'_',judgement,'_',effect,'_',num2str(thread),'.txt'));
                allData = [allData; data];
            end
            
            dataDis1 = allData(:,1:2:13);
            dataDis2 = allData(:,[2:2:12,13]);
            for i = 1:6
                %%%%%%%%%%%%%%%% Scatter %%%%%%%%%%%%%%%%
                figure('Position', [100, 100, 400, 200]);
                scatter(dataDis1(1:50000,i),dataDis1(1:50000,7));
                
                ylabel('$\Delta{R_i}(\%)$', 'FontSize', 12 ,'Interpreter','latex');
                xlabel('Normalised Ranking', 'FontSize', 12);
                
                set(gcf, 'PaperSize', [25 25])
                saveas(gcf,strcat('../CARVB_figs/carvb_cc_scatter_',num2str(core),'_','0.1','_',types_names(i),'.eps'), 'epsc');
                saveas(gcf,strcat('../CARVB_figs/carvb_cc_scatter_',num2str(core),'_','0.1','_',types_names(i),'.png'));
                
                
                %%%%%%%%%%%%%%%% histogram %%%%%%%%%%%%%%%%
                f=figure('Position', [100, 100, 400, 200]);
                
                y = dataDis2(dataDis2(:,i)==1,:);
    
                y1 = y(:,7);
                
                histogram(y1,100);
                
                xlim([-0.05 0.3])
                ylim([0 2400])
                
                ylabel('Frequency', 'FontSize', 12);
                xlabel(strcat('$\Delta{R_i}(\%)$'), 'FontSize', 12,'Interpreter','latex');
                
                set(gcf, 'PaperSize', [25 25])
                saveas(gcf,strcat('../CARVB_figs/carvb_cc_hist_',num2str(core),'_','0.1','_',types_names(i),'.eps'), 'epsc');
                saveas(gcf,strcat('../CARVB_figs/carvb_cc_hist_',num2str(core),'_','0.1','_',types_names(i),'.png'));
                
            end
            
            
            
            
            [r,p] = corrcoef(dataDis1);
            r
%             p
            %             p
            
            [r,p] = corrcoef(dataDis2);
            r
%             p
            %             p
            
            %            [coeff,score,latent,tsquared,explained,mu] = pca(allData(:,1:6));
            %
            %            anova1(allData);
            %            explained
        end
    end
end
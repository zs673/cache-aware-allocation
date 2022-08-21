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
                data = readmatrix(strcat('../faults_new/before_after_ajlr_',num2str(core),'_',judgement,'_',effect,'_',num2str(thread),'.txt'));
                allData = [allData; data];
            end
            
            dataDis1 = allData(:,[1:2:12,14]);
            dataDis2 = allData(:,[2:2:12,14]);
            
            
            [r,p] = corrcoef(dataDis1);
            r(:,7)'
            
            [r,p] = corrcoef(dataDis2);
            r(:,7)'
            
            allData = zeros(0);
            for thread = threads
                data = readmatrix(strcat('../faults_new/before_after_carvb_',num2str(core),'_',judgement,'_',effect,'_',num2str(thread),'.txt'));
                allData = [allData; data];
            end
            
            dataDis1 = allData(:,[1:2:12,14]);
            dataDis2 = allData(:,[2:2:12,14]);
            
            
            [r,p] = corrcoef(dataDis1);
            r(:,7)'
            
            [r,p] = corrcoef(dataDis2);
            r(:,7)'
        end
    end
end

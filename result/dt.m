close all

f=figure('Position', [100, 100, 800, 350]);
set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);

boxplot(input)

xticklables = ["AJLR","WF","AJLR","WF","AJLR","WF" ];


set(gca,'xticklabel',xticklables,'FontSize', 14);

ylabel('Makespan','FontSize', 18)
xlabel('3 Cores                              4 Cores                           5 Cores','FontSize', 18)
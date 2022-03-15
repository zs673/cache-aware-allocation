close all

f=figure('Position', [100, 100, 400, 350]);
set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);

data = inputCore;

boxplot(data);

xticklables = ["AJLR","WFD"];
set(gca,'xticklabel',xticklables,'FontSize', 12);



ylabel('Makespan','FontSize', 14)

c = findobj(gca,'Tag','Box');

set(gcf, 'PaperSize', [25 25])
saveas(gcf,strcat('figs/simu.png'));
saveas(gcf,strcat('figs/simu.eps'), 'epsc');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

f=figure('Position', [100, 100, 400, 350]);
set(f,'defaultAxesColorOrder',[[0,60/255,255/255];[1,51/255,51/255]]);

col = 2;

AJLR = load("AJLR.out");
WFD = load("WFD.out");

boxplot([AJLR(:,col), WFD(:,col)],  'symbol' , '-'); % , 'Whisker', 5

xticklables = ["AJLR","WFD"];
set(gca,'xticklabel',xticklables,'FontSize', 12);

ylim([3.8*1e4 4.6*1e4])
ylabel('Makespan','FontSize', 14)

set(gcf, 'PaperSize', [25 25])
saveas(gcf,strcat('figs/makespan_diagrams.png'));
saveas(gcf,strcat('figs/makespan_diagrams.eps'), 'epsc');

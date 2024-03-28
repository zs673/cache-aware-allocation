import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

NOS_NUM = 500
PATH = "E:/Code/Java/cache-aware-allocation-main/result/41/"
data = pd.read_table(PATH+'/makespan_1_2.0.txt', sep=',', header=None)
data = data.iloc[NOS_NUM:, 0:10]
x = data.values
AJLR = x[NOS_NUM:, :].mean(axis=0)
OUR = x[0:NOS_NUM, :].mean(axis=0)

bar_width=0.2                                                  #定义柱宽为0.2
x_labels = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']   #定义X轴标签名称
x_size=np.arange(len(x_labels))                                   #X轴标签分布有8个
 
#绘制并列柱状图
plt.bar(x_size,AJLR, bar_width, color='r', align='center', label='AJLR')  #柱宽为0.2
plt.bar(x_size + bar_width, OUR, bar_width, color='y', align='center', label='OUR')#柱宽为0.2，标签在柱子中间
 
plt.ylabel('Normalized makespan')
plt.xlabel('Instance index of DAG')
plt.xticks(x_size + bar_width, x_labels)     #定义X轴标签位置
 
plt.legend()                           #显示图例
# plt.show()                             #显示柱状图
plt.savefig(PATH + "bar.png", dpi=300)
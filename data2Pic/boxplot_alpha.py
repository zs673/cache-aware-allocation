import os
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def draw_line(data, PATH1):
    y = np.zeros(TRIAL_NUM)
    x = np.linspace(1, TRIAL_NUM, TRIAL_NUM)
    for i in range(0, TRIAL_NUM):
        data_tmp = data[i*TOT_NOS:(i+1)*TOT_NOS, :]
        mean_data = data_tmp.mean(axis=0)
        med_point = np.median(mean_data, axis=0)
        y[i] = np.around(med_point, 3)
        # y[i] = med_point
        # y[i] = mean_data
    
    plt.plot(x, y, color='r')
    plt.ylabel('Normalized makespan')
    plt.xlabel('value of alpha')
    plt.savefig(PATH1 + "alpha.png", dpi=300)

def draw_bar(x, PATH):
    OUR = x[2*TOT_NOS:, :].mean(axis=0)
    AJLR = x[TOT_NOS:2*TOT_NOS, :].mean(axis=0)
    EOWF = x[0:TOT_NOS, :].mean(axis=0)

    bar_width=0.1                                                  #定义柱宽为0.2
    # x_labels = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']   #定义X轴标签名称
    # x_labels = np.arange(TASK_NUM*INSTANCE_NUM)
    # x_size=np.arange(len(x_labels))                                 
    x_labels = ['0', '1', '2', '9']
    x_size = np.arange(4)
    color = ['b', 'r']
    #绘制并列柱状图
    for i in range(0, TRIAL_NUM):
        data_tmp = x[i*TOT_NOS:(i+1)*TOT_NOS, :]
        mean_data = data_tmp.mean(axis=0)
        slice = np.zeros(4)
        slice[0:3] = mean_data[0:3]
        slice[3] = mean_data[9]
        plt.bar(x_size + (i-1.5)*bar_width, slice, bar_width, color=color[i%2], align='center')#柱宽为0.2，标签在柱子中间
    # plt.bar(x_size + bar_width, AJLR, bar_width, color='y', align='center', label='AJLR')  #柱宽为0.2
    # plt.bar(x_size + 2 * bar_width , OUR, bar_width, color='r', align='center', label='OUR')#柱宽为0.2，标签在柱子中间

    plt.ylabel('Normalized makespan')
    plt.xlabel('Instance index of DAG')
    plt.xticks(x_size + bar_width, x_labels)     #定义X轴标签位置
    
    plt.legend()                           #显示图例
    # plt.show()                             #显示柱状图
    plt.savefig(PATH + "alpha.png", dpi=300)

TOT_NOS = 1000
NOS_NUM = 500
TASK_NUM = 1
INSTANCE_NUM = 10
METHOD_NUM = 3
TRIAL_NUM = 9

def main():
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/alpha1/"
    PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/alpha2/"
    PATH3 = "E:/Code/Java/cache-aware-allocation-main/result/alpha3/"
    data1 = pd.read_table(PATH1 + '/makespan_1_2.0.txt', sep=',', header=None).iloc[:, 0:TASK_NUM*INSTANCE_NUM]
    # data = data.iloc[NOS_NUM:, 0:10]
    data2 = pd.read_table(PATH2 + '/makespan_1_2.0.txt', sep=',', header=None).iloc[:, 0:TASK_NUM*INSTANCE_NUM]
    x = np.concatenate((data1.values, data2.values), axis=0)
    data2 = pd.read_table(PATH3 + '/makespan_1_2.0.txt', sep=',', header=None).iloc[:, 0:TASK_NUM*INSTANCE_NUM]
    x = np.concatenate((x, data2.values), axis=0)
    draw_line(x, PATH1)
    # draw_bar(x, PATH1)

if __name__ == '__main__':
    main()



     



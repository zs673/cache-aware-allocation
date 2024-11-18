import os
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def draw_box(x, NOS_NUM, PATH):
    fig = plt.figure()
    ax = fig.add_subplot(facecolor='white')
    # 每个刻度标签下有几个group就有几个箱子
    # group_data = [x[0:TOT_NOS, :], x[TOT_NOS:2*TOT_NOS, :], x[2*TOT_NOS:, :]]

    color_list = ['b', 'y', 'r']
    x_labels = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
    length = TASK_NUM * len(x_labels)
    x_loc = np.arange(length)

    boxplot_data = [x[0:TOT_NOS, :], x[TOT_NOS:2*TOT_NOS, :], x[2*TOT_NOS:, :]]
    ax.boxplot(boxplot_data[2], patch_artist=True,
            medianprops={'lw': 1, 'color': 'r'},
            boxprops={'lw':2, 'facecolor': 'None', 'edgecolor': 'r'},
            capprops={'lw': 1, 'color': 'r'},
            whiskerprops={'ls': '--', 'lw': 1, 'color': 'r'},
            flierprops = {'marker':'o','markerfacecolor':'red','color':'black'},
            showfliers=True, zorder=1)

    ax.grid(True, ls=':', color='b', alpha=0.3)
    ax.set_xticks(x_loc)
    ax.set_xticklabels(x_labels, rotation=0)
    ax.set_ylabel('Maximum Number of Node Deferment')
    ax.set_xlabel('Instance index of DAG')
    fig.tight_layout()
    # plt.show()
    plt.savefig(PATH + "box.png", dpi=300)


def draw_bar(x, NOS_NUM, PATH):
    OUR = x[2*TOT_NOS:, :].mean(axis=0)
    AJLR = x[TOT_NOS:2*TOT_NOS, :].mean(axis=0)
    EOWF = x[0:TOT_NOS, :].mean(axis=0)

    bar_width=0.2                                                  #定义柱宽为0.2
    # x_labels = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']   #定义X轴标签名称
    x_labels = np.arange(TASK_NUM*INSTANCE_NUM)
    x_size=np.arange(len(x_labels))                                   #X轴标签分布有8个
    
    #绘制并列柱状图
    plt.bar(x_size, EOWF, bar_width, color='b', align='center', label='EO+WF')#柱宽为0.2，标签在柱子中间
    plt.bar(x_size + bar_width, AJLR, bar_width, color='y', align='center', label='AJLR')  #柱宽为0.2
    plt.bar(x_size + 2 * bar_width , OUR, bar_width, color='r', align='center', label='OUR')#柱宽为0.2，标签在柱子中间

    plt.ylabel('Normalized makespan')
    plt.xlabel('Instance index of DAG')
    plt.xticks(x_size + bar_width, x_labels)     #定义X轴标签位置
    
    plt.legend()                           #显示图例
    # plt.show()                             #显示柱状图
    plt.savefig(PATH + "bar.png", dpi=300)

def numpy_to_str(array):
    return ','.join(str(i) for i in array)

def analyse(x, PATH1):
    if os.path.exists(PATH1) is False:
        os.makedirs(PATH1)
    
    path = PATH1 + "analyse.txt"

    f = open(path, 'w', encoding='utf-8')
    for i in range(0, METHOD_NUM):
        method_data = x[i*TOT_NOS:(i+1)*TOT_NOS, :]
        method_data_inv = method_data.T

        tot_mean = np.around(method_data.mean(), 3)
        f.write(str(tot_mean) + "\n")
        mean_data = np.around(method_data.mean(axis=0), 3)
        f.write(numpy_to_str(mean_data) + "\n")
        med_data = np.around(np.median(method_data, axis=0), 3)
        f.write(numpy_to_str(med_data) + "\n")
        max_data = np.around(method_data.max(axis=0), 3)
        f.write(numpy_to_str(max_data) + "\n")
        min_data = np.around(method_data.min(axis=0), 3)
        f.write(numpy_to_str(min_data) + "\n\n")

    f.close()


TOT_NOS = 1000
NOS_NUM = 500
TASK_NUM = 1
INSTANCE_NUM = 10
METHOD_NUM = 1
def main():
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/8/"
    data1 = pd.read_table(PATH1 + '/max_defer_1_2.0.txt', sep=',', header=None)
    x = data1.iloc[:, 0:TASK_NUM*INSTANCE_NUM].values
    draw_box(x, NOS_NUM, PATH1)
    # plt.clf()
    # draw_bar(x, NOS_NUM, PATH1)

if __name__ == '__main__':
    main()



     



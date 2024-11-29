import os
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def draw_box(x, NOS_NUM, PATH, NAME):
    fig = plt.figure(figsize=(6.4, 2.8))
    ax = fig.add_subplot(facecolor='white')
    # 每个刻度标签下有几个group就有几个箱子
    # group_data = [x[0:TOT_NOS, :], x[TOT_NOS:2*TOT_NOS, :], x[2*TOT_NOS:, :]]

    # color_list = ['#1f77b4', '#ff7f0e', '#EEB937']
    # color_list = ['#C0D6EA', '#A6C7E2', '#86B7DB']
    color_list = ['#AABCDB', '#7698C3', '#487DB2']
    x_labels = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
    length = TASK_NUM * len(x_labels)
    x_loc = np.arange(length)

    group_number = METHOD_NUM
    total_width = 0.6
    box_total_width = total_width * 0.65
    interval_total_width = total_width * 0.35
    box_width = box_total_width / group_number

    if group_number == 1:
        interval_width = interval_total_width
    else:
        interval_width = interval_total_width / (group_number - 1)

    if group_number % 2 == 0:
        x1_box = x_loc - (group_number / 2 - 1) * box_width - box_width / 2 - (group_number / 2 - 1) * interval_width - interval_width / 2
    else:
        x1_box = x_loc - ((group_number - 1) / 2) * box_width - ((group_number - 1) / 2) * interval_width
    x_list_box = [x1_box + box_width * i + interval_width * i for i in range(group_number)]

    boxplot_data = [x[0:TOT_NOS, :], x[TOT_NOS:2*TOT_NOS, :], x[2*TOT_NOS:, :]]
    for i in range(len(boxplot_data)):
        ax.boxplot(boxplot_data[i], positions=x_list_box[i], widths=box_width, patch_artist=True,
                medianprops={'lw': 1, 'color': color_list[i]},
                boxprops={'lw':2, 'facecolor': 'None', 'edgecolor': color_list[i]},
                capprops={'lw': 1, 'color': color_list[i]},
                whiskerprops={'ls': '--', 'lw': 1, 'color': color_list[i]},
                showfliers=False, zorder=1)

    ax.grid(True, ls=':', color='b', alpha=0.3)
    ax.set_xticks(x_loc)
    ax.set_xticklabels(x_labels, rotation=0)
    ax.set_ylabel('Normalized makespan', fontsize=20)
    ax.set_xlabel('Job index of the DAG', fontsize=20)
    legend_elements = [plt.Line2D([0], [0], color=color_list[0], label='WF+EO'),
                    plt.Line2D([0], [0], color=color_list[1], label='AJLR'),
                    plt.Line2D([0], [0], color=color_list[2], label='CAPA+CADE')]
    plt.legend(handles=legend_elements, loc='lower left', prop={'size':7}, ncol=3)
    fig.tight_layout()
    # plt.show()
    plt.savefig(PATH + NAME, dpi=300)


def draw_bar(x, NOS_NUM, PATH, NAME):
    # fig = plt.figure(figsize=(6, 2.5))
    fig = plt.figure(figsize=(7, 4.5))
    OUR = x[2*TOT_NOS:, :].mean(axis=0)
    AJLR = x[TOT_NOS:2*TOT_NOS, :].mean(axis=0)
    EOWF = x[0:TOT_NOS, :].mean(axis=0)

    bar_width=0.2                                                  #定义柱宽为0.2
    # x_labels = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']   #定义X轴标签名称
    x_labels = np.arange(TASK_NUM*INSTANCE_NUM)
    x_size=np.arange(len(x_labels))                                   #X轴标签分布有8个
    
    #绘制并列柱状图
    # plt.bar(x_size, EOWF, bar_width, color='b', align='center', label='WF+EO')#柱宽为0.2，标签在柱子中间
    # plt.bar(x_size + bar_width, AJLR, bar_width, color='y', align='center', label='AJLR')  #柱宽为0.2
    # plt.bar(x_size + 2 * bar_width , OUR, bar_width, color='r', align='center', label='OUR')#柱宽为0.2，标签在柱子中间
    # color_list = ['#AABCDB', '#7698C3', '#487DB2']
    color_list = ['#999999', '#30617F', '#AED9E6']

    plt.bar(x_size, EOWF, bar_width, color=color_list[0], edgecolor='black', align='center', label='WF+EO')  # 蓝色
    plt.bar(x_size + bar_width, AJLR, bar_width, color=color_list[1], edgecolor='black', align='center', label='AJLR')  # 橙色
    plt.bar(x_size + 2 * bar_width, OUR, bar_width, color=color_list[2], edgecolor='black', align='center', label='CAPA+CADE')  # 绿色
    # 3D93CD E07647 EEB937
    plt.ylabel('Normalized makespan', fontsize=12)
    plt.xlabel('Job index of the DAG', fontsize=12)
    plt.xticks(x_size + bar_width, x_labels)     #定义X轴标签位置
    
    # plt.legend(prop={'size':9}, loc='upper left', ncol=3, framealpha=0.3, bbox_to_anchor=(0, 1.18), columnspacing=8.34, frameon=True)
    # plt.legend(prop={'size':12}, loc='lower center', ncol=3, framealpha=0.9, bbox_to_anchor=(0.5, -0), columnspacing=3.6, frameon=True)
    plt.legend(prop={'size':11}, loc='lower center', ncol=3, framealpha=0.9, bbox_to_anchor=(0.5, -0), columnspacing=5, frameon=True)
    plt.tight_layout()
    # plt.show()                             #显示柱状图
    plt.savefig(PATH + NAME, dpi=300, format="pdf")

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
METHOD_NUM = 3
def main():
    # PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/core_32_1/"
    # PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/core_32_2/"
    # data1 = pd.read_table(PATH1 + '/makespan_1_4.0.txt', sep=',', header=None)
    # # data = data.iloc[NOS_NUM:, 0:10]
    # data2 = pd.read_table(PATH2 + '/makespan_1_4.0.txt', sep=',', header=None)
    # x = np.zeros((METHOD_NUM*TOT_NOS, TASK_NUM*INSTANCE_NUM))
    # for i in range(0, METHOD_NUM):
    #     x[i*TOT_NOS:(i+1)*TOT_NOS, :] = np.concatenate((data1.iloc[i*NOS_NUM:(i+1)*NOS_NUM, 0:TASK_NUM*INSTANCE_NUM].values, data2.iloc[i*NOS_NUM:(i+1)*NOS_NUM, 0:TASK_NUM*INSTANCE_NUM].values), axis=0)
    # draw_box(x, NOS_NUM, PATH1)
    # plt.clf()
    # draw_bar(x, NOS_NUM, PATH1)
    # analyse(x, PATH1)

    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/1017_noupdateS/16_varyU_0-40_varyT/"
    NAME = "ep_16core.pdf"
    data1 = pd.read_table(PATH1 + '/makespan_1_2.1056973431632953.txt', sep=',', header=None)
    x = data1.iloc[:, 0:TASK_NUM*INSTANCE_NUM].values
    draw_box(x, NOS_NUM, PATH1, NAME)
    plt.clf()
    draw_bar(x, NOS_NUM, PATH1, NAME)
  
    # PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/predict/"
    # data1 = pd.read_table(PATH1 + '/total_defer_num_1_2.0.txt', sep=',', header=None)
    # x = data1.iloc[:, 0:TASK_NUM*INSTANCE_NUM].values
    # test2 = x[TOT_NOS:2*TOT_NOS, :]
    # test2_sum = test2.sum()
    # print (test2_sum)
    # compare = x[2*TOT_NOS:, :]
    # compare_sum = compare.sum()
    # print (compare_sum)

if __name__ == '__main__':
    main()



     



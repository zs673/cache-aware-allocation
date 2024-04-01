import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def draw_box(x, NOS_NUM, PATH):
    fig = plt.figure()
    ax = fig.add_subplot(facecolor='white')
    # 每个刻度标签下有几个group就有几个箱子
    group_data = [x[NOS_NUM:, :], x[0:NOS_NUM, :]]

    # 橙绿蓝
    # color_list = ['#FF8C00', '#00FF00', '#0000FF']
    color_list = ['r', 'y']
    legend_labels = ['AJLR', 'OUR']
    x_labels = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
    length = len(x_labels)
    x_loc = np.arange(length)

    group_number = len(group_data)
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

    boxplot_data = [x[NOS_NUM:, :], x[0:NOS_NUM, :]]
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
    ax.set_ylabel('Normalized makespan')
    ax.set_xlabel('Instance index of DAG')
    legend_elements = [plt.Line2D([0], [0], color=color_list[0], label='AJLR'),
                    plt.Line2D([0], [0], color=color_list[1], label='OUR')]
    plt.legend(handles=legend_elements, loc='upper right')
    fig.tight_layout()
    # plt.show()
    plt.savefig(PATH + "box.png", dpi=300)


def draw_bar(x, NOS_NUM, PATH):
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


def main():
    NOS_NUM = 500
    PATH = "E:/Code/Java/cache-aware-allocation-main/result/12.1/"
    data = pd.read_table(PATH+'/makespan_1_2.0.txt', sep=',', header=None)
    # data = data.iloc[NOS_NUM:, 0:10]
    data1 = data.iloc[0:NOS_NUM, 0:10]
    x1 = data1.values
    data2 = data.iloc[2*NOS_NUM:, 0:10]
    x2 = data2.values
    x = np.concatenate((x1, x2), axis=0)
    draw_box(x, NOS_NUM, PATH)
    plt.clf()
    draw_bar(x, NOS_NUM, PATH)

if __name__ == '__main__':
    main()



     



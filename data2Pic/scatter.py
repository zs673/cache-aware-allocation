import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def handle_y(y, y2, y3, y4):
    num_per_method = 1500

    combined_data = np.concatenate((y, y2, y3, y4), axis=0)
    i=0
    for k in range(4):
    # 提取每个方法的数据
        method1_data = combined_data[i * num_per_method:(i+1)*num_per_method]
        method2_data = combined_data[(i+1)*num_per_method:num_per_method*(i+2)]
        method3_data = combined_data[num_per_method*(i+2):num_per_method*(i+3)]

        # 聚集每个方法的数据
        if k == 0:
            method1_combined = method1_data
            method2_combined = method2_data
            method3_combined = method3_data
        else:
            method1_combined = np.concatenate((method1_combined, method1_data), axis=0)
            method2_combined = np.concatenate((method2_combined, method2_data), axis=0)
            method3_combined = np.concatenate((method3_combined, method3_data), axis=0)

        i+=3

    # 最后将三个方法的数据聚集在一起
    new_y = np.concatenate((method1_combined, method2_combined, method3_combined), axis=0)

    # 打印结果以验证
    return new_y

# todo:
# 修改点的大小
# cache分布（miss L1和L2）分开吧 done
# 截到350
def scatter(x, data, PATH):
    plt.figure(figsize=(8, 3))
    color = ['#1f77b4', '#ff7f0e', '#EEB937']
    # 78AC31
    # x = np.linspace(0.576, 576, 1000)  
    # x_ = np.linspace(START, END, STEP_NUM) 
    # x = np.repeat(x_, 5)
    labels = ["WF+EO", "AJLR", "CAP+CARMIP"]
    for i in range(0, METHOD_NUM):
        # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, :]
        method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, :]
        # mean_data = np.around(method_data.mean(axis=0), 3)
        med_data = np.around(np.median(method_data, axis=1), 3)
        plt.scatter(x, med_data, s=3, c=color[i], marker = 'o', label=labels[i])
    plt.xlim(START, END)
    plt.ylabel('Normalized makespan')
    plt.xlabel('Workload of the DAG task')
    plt.legend(prop={'size':12}, loc='lower right')  
    plt.tight_layout()
    # plt.show()
    plt.savefig(PATH + "scatter.png", dpi=300)

def scatter_cache(x, data, PATH, flag):
    plt.figure(figsize=(8, 3))
    # color = ['b', 'y', 'r']
    color = ['#1f77b4', '#ff7f0e', '#EEB937']
    # x = np.linspace(0.576, 576, 1000)  
    # x_ = np.linspace(START, END, STEP_NUM) 
    # x = np.repeat(x_, 5)
    labels = ["WF+EO", "AJLR", "CAP+CARMIP"]
    for i in range(0, METHOD_NUM):
        if flag == "L1":
            # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 0]
            method_data = data[i*TOT_NOS:(i+1)*TOT_NOS]
            local = 'lower right'
            print(np.mean(method_data))
        elif flag == "L2":
            # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 1]
            method_data = data[i*TOT_NOS:(i+1)*TOT_NOS]
            local = 'upper left'
            print(np.mean(method_data))

        plt.scatter(x, method_data, s=3, c=color[i], marker = 'o', label=labels[i])
    
    if flag == "L1":
        plt.ylabel('Recency miss rate')
    elif flag == "L2":
        plt.ylabel('Recency miss rate')
    plt.xlabel('Workload of the DAG task')
    plt.legend(prop={'size':12}, loc=local)  
    plt.tight_layout()
    # plt.show()
    plt.savefig(PATH + "cache_" + flag + ".png", dpi=300)

NUM = 1500
TOT_NOS = 6000
TASK_NUM = 1
INSTANCE_NUM = 10
METHOD_NUM = 3
# START = 0.1152#0.144
START = 0.576#0.144
END = 250.56#300.096 #576
STEP_NUM = (int)((END - START) / START + 1)
def main():
    # PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/util.txt"
    # PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/exp_L2cluster/cache/"
    # # PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/exp1_2/"
    # x = pd.read_table(PATH, sep=',', header=None).iloc[:, 0:TOT_NOS].values
    # x = x * 144
    # data = pd.read_table(PATH1 + '/makespan_1_1.6406510688216152.txt', sep=',', header=None)
    # data = data.iloc[:, 0:10]
    # y = data.values
    # scatter(x, y, PATH1)
    # plt.clf()
    # miss_rate = np.zeros((METHOD_NUM*TOT_NOS, 2))
    # cache_data = pd.read_table(PATH1 + '/cache_1_1.6406510688216152.txt', sep=',', header=None)
    # cache_data = cache_data.iloc[0:METHOD_NUM*TOT_NOS, 0:4]
    # y = cache_data.values
    # miss_rate[:, 0] = np.sum(y, axis=1) - y[:, 0]
    # miss_rate[:, 1] = np.sum(y, axis=1) - y[:, 0] - y[:, 1]
    # scatter_cache(x, miss_rate, PATH1, "L1")
    # scatter_cache(x, miss_rate, PATH1, "L2")

    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache1/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache1/"
    # x = pd.read_table(PATH, sep=',', header=None).iloc[:, 0:TOT_NOS].values
    # x = x[0, :NUM] * 144
    data = pd.read_table(PATH1 + '/makespan_1_1.286409325309998.txt', sep=',', header=None)
    data = data.iloc[:, 0:10]
    y = data.values

    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache2/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache2/"
    # PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/exp1_2/"
    x2 = pd.read_table(PATH, sep=',', header=None).iloc[:, 0:TOT_NOS].values
    new_x = x2[0, :TOT_NOS] * 144
    # x = np.append(x, x2, axis=0)
    data2 = pd.read_table(PATH1 + '/makespan_1_1.164458775607931.txt', sep=',', header=None)
    data2 = data2.iloc[:, 0:10]
    y2 = data2.values
    # y = np.append(y, y2, axis=0)

    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache3/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache3/"
    # PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/exp1_2/"
    # x3 = pd.read_table(PATH, sep=',', header=None).iloc[:, 0:TOT_NOS].values
    # x3 = x3[0, :NUM] * 144
    data3 = pd.read_table(PATH1 + '/makespan_1_0.2191014804318998.txt', sep=',', header=None)
    data3 = data3.iloc[:, 0:10]
    y3 = data3.values

    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache4/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache4/"
    # PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/exp1_2/"
    # x4 = pd.read_table(PATH, sep=',', header=None).iloc[:, 0:TOT_NOS].values
    # x4 = x4[0, :NUM] * 144
    data4 = pd.read_table(PATH1 + '/makespan_1_0.5729632765207335.txt', sep=',', header=None)
    data4 = data4.iloc[:, 0:10]
    y4 = data4.values

    # new_x = np.concatenate((x, x2, x3, x4), axis=0)
    new_y = handle_y(y, y2, y3, y4)
    # print(new_y[1500, :])
    # print(new_y[3000, :])
    # print(new_y[4500, :])
    scatter(new_x, new_y, PATH1)

    plt.clf()

    miss_rate1 = np.zeros((METHOD_NUM*NUM, 2))
    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache1/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache1/"
    cache_data = pd.read_table(PATH1 + '/cache_1_1.286409325309998.txt', sep=',', header=None)
    cache_data = cache_data.iloc[0:METHOD_NUM*NUM, 0:4]
    y = cache_data.values
    miss_rate1[:, 0] = np.sum(y, axis=1) - y[:, 0]
    miss_rate1[:, 1] = np.sum(y, axis=1) - y[:, 0] - y[:, 1]

    miss_rate2 = np.zeros((METHOD_NUM*NUM, 2))
    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache2/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache2/"
    cache_data = pd.read_table(PATH1 + '/cache_1_1.164458775607931.txt', sep=',', header=None)
    cache_data = cache_data.iloc[0:METHOD_NUM*NUM, 0:4]
    y = cache_data.values
    miss_rate2[:, 0] = np.sum(y, axis=1) - y[:, 0]
    miss_rate2[:, 1] = np.sum(y, axis=1) - y[:, 0] - y[:, 1]

    miss_rate3 = np.zeros((METHOD_NUM*NUM, 2))
    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache3/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache3/"
    cache_data = pd.read_table(PATH1 + '/cache_1_0.2191014804318998.txt', sep=',', header=None)
    cache_data = cache_data.iloc[0:METHOD_NUM*NUM, 0:4]
    y = cache_data.values
    miss_rate3[:, 0] = np.sum(y, axis=1) - y[:, 0]
    miss_rate3[:, 1] = np.sum(y, axis=1) - y[:, 0] - y[:, 1]

    miss_rate4 = np.zeros((METHOD_NUM*NUM, 2))
    PATH = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache4/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/exp0924/cache4/"
    cache_data = pd.read_table(PATH1 + '/cache_1_0.5729632765207335.txt', sep=',', header=None)
    cache_data = cache_data.iloc[0:METHOD_NUM*NUM, 0:4]
    y = cache_data.values
    miss_rate4[:, 0] = np.sum(y, axis=1) - y[:, 0]
    miss_rate4[:, 1] = np.sum(y, axis=1) - y[:, 0] - y[:, 1]

    miss_L1 = handle_y(miss_rate1[:, 0], miss_rate2[:, 0], miss_rate3[:, 0], miss_rate4[:, 0])
    miss_L2 = handle_y(miss_rate1[:, 1], miss_rate2[:, 1], miss_rate3[:, 1], miss_rate4[:, 1])
    scatter_cache(new_x, miss_L1, PATH1, "L1")
    scatter_cache(new_x, miss_L2, PATH1, "L2")


    # 定义区间的边界，以50为区间大小
    bins = np.arange(0, new_x.max() + 50, 50)

    # 使用 numpy 的 histogram 函数统计每个区间内的数的个数
    counts, bin_edges = np.histogram(new_x, bins=bins)

    # 输出每个区间及其对应的数的个数
    for i in range(len(counts)):
        print(f"Range {bin_edges[i]} to {bin_edges[i+1]}: {counts[i]}")



if __name__ == '__main__':
    main()


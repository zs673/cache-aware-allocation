import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


# todo:
# 修改点的大小
# cache分布（miss L1和L2）分开吧 done
# 截到350
def scatter(x, data, PATH):
    plt.figure()
    color = ['b', 'y', 'r']
    # x = np.linspace(0.576, 576, 1000)  
    # x_ = np.linspace(START, END, STEP_NUM) 
    # x = np.repeat(x_, 5)
    labels = ["EO+WF", "AJLR", "OUR"]
    for i in range(0, METHOD_NUM):
        # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, :]
        method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, :]
        # mean_data = np.around(method_data.mean(axis=0), 3)
        med_data = np.around(np.median(method_data, axis=1), 3)
        plt.scatter(x, med_data, s=3, c=color[i], marker = 'o', label=labels[i])
    plt.xlim(START, END)
    plt.ylabel('Normalized makespan')
    plt.xlabel('Workload of the DAG task')
    plt.legend()  
    # plt.show()
    plt.savefig(PATH + "scatter.png", dpi=300)

def scatter_cache(x, data, PATH, flag):
    plt.figure()
    color = ['b', 'y', 'r']
    # x = np.linspace(0.576, 576, 1000)  
    # x_ = np.linspace(START, END, STEP_NUM) 
    # x = np.repeat(x_, 5)
    labels = ["EO+WF", "AJLR", "OUR"]
    for i in range(0, METHOD_NUM):
        if flag == "L1":
            # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 0]
            method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 0]
        elif flag == "L2":
            # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 1]
            method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 1]

        plt.scatter(x, method_data, s=3, c=color[i], marker = 'o', label=labels[i])
    
    if flag == "L1":
        plt.ylabel('Recency miss rate at the core levels')
    elif flag == "L2":
        plt.ylabel('Recency miss rate at the cluster levels')
    plt.xlim(START, END)
    plt.xlabel('Workload of the DAG task')
    plt.legend()  
    # plt.show()
    plt.savefig(PATH + "cache_" + flag + ".png", dpi=300)

TOT_NOS = 5000
TASK_NUM = 1
INSTANCE_NUM = 10
METHOD_NUM = 3
# START = 0.1152#0.144
START = 0.576#0.144
END = 250.56#300.096 #576
STEP_NUM = (int)((END - START) / START + 1)
def main():
    PATH = "E:/Code/Java/cache-aware-allocation-main/result/util.txt"
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/predict/"
    # PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/exp1_2/"
    x = pd.read_table(PATH, sep=',', header=None).iloc[:, 0:TOT_NOS].values
    x = x * 144
    data = pd.read_table(PATH1 + '/makespan_1_1.6406510688216152.txt', sep=',', header=None)
    data = data.iloc[:, 0:10]
    y = data.values
    scatter(x, y, PATH1)
    plt.clf()
    miss_rate = np.zeros((METHOD_NUM*TOT_NOS, 2))
    cache_data = pd.read_table(PATH1 + '/cache_1_1.6406510688216152.txt', sep=',', header=None)
    cache_data = cache_data.iloc[0:METHOD_NUM*TOT_NOS, 0:4]
    y = cache_data.values
    miss_rate[:, 0] = np.sum(y, axis=1) - y[:, 0]
    miss_rate[:, 1] = np.sum(y, axis=1) - y[:, 0] - y[:, 1]
    scatter_cache(x, miss_rate, PATH1, "L1")
    scatter_cache(x, miss_rate, PATH1, "L2")

if __name__ == '__main__':
    main()


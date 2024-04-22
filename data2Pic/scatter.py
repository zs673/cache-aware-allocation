import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


# todo:
# 修改点的大小
# cache分布（miss L1和L2）分开吧 done
# 截到350
def scatter(data, PATH):
    plt.figure()
    color = ['b', 'y', 'r']
    # x = np.linspace(0.576, 576, 1000)  
    x = np.linspace(START, END, STEP_NUM) 
    labels = ["EO+WF", "AJLR", "OUR"]
    for i in range(0, METHOD_NUM):
        # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, :]
        method_data = data[i*TOT_NOS:i*TOT_NOS+STEP_NUM, :]
        # mean_data = np.around(method_data.mean(axis=0), 3)
        med_data = np.around(np.median(method_data, axis=1), 3)
        plt.scatter(x, med_data, s=3, c=color[i], marker = 'o', label=labels[i])
    plt.ylabel('Normalized makespan')
    plt.xlabel('Workload of the DAG task')
    plt.legend()  
    # plt.show()
    plt.savefig(PATH + "scatter.png", dpi=300)

def scatter_cache(data, PATH, flag):
    plt.figure()
    color = ['b', 'y', 'r']
    # x = np.linspace(0.576, 576, 1000)  
    x = np.linspace(START, END, STEP_NUM) 
    labels = ["EO+WF", "AJLR", "OUR"]
    for i in range(0, METHOD_NUM):
        if flag == "L1":
            # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 0]
            method_data = data[i*TOT_NOS:i*TOT_NOS+STEP_NUM, 0]
        elif flag == "L2":
            # method_data = data[i*TOT_NOS:(i+1)*TOT_NOS, 1]
            method_data = data[i*TOT_NOS:i*TOT_NOS+STEP_NUM, 1]

        plt.scatter(x, method_data, s=3, c=color[i], marker = 'o', label=labels[i])
    
    if flag == "L1":
        plt.ylabel('Recency miss rate at the core levels')
    elif flag == "L2":
        plt.ylabel('Recency miss rate at the cluster levels')
    plt.xlabel('Workload of the DAG task')
    plt.legend()  
    # plt.show()
    plt.savefig(PATH + "cache_" + flag + ".png", dpi=300)

TOT_NOS = 5000
TASK_NUM = 1
INSTANCE_NUM = 10
METHOD_NUM = 3
START = 0.1152#0.144
END = 300.096 #576
STEP_NUM = (int)((END - START) / START + 1)
def main():
    PATH1 = "E:/Code/Java/cache-aware-allocation-main/result/cache_5000/"
    # PATH2 = "E:/Code/Java/cache-aware-allocation-main/result/exp1_2/"
    data = pd.read_table(PATH1 + '/makespan_1_4.0.txt', sep=',', header=None)
    data = data.iloc[:, 0:10]
    x = data.values
    scatter(x, PATH1)
    plt.clf()
    miss_rate = np.zeros((METHOD_NUM*TOT_NOS, 2))
    cache_data = pd.read_table(PATH1 + '/cache_1_4.0.txt', sep=',', header=None)
    cache_data = cache_data.iloc[0:METHOD_NUM*TOT_NOS, 0:4]
    x = cache_data.values
    miss_rate[:, 0] = np.sum(x, axis=1) - x[:, 0]
    miss_rate[:, 1] = np.sum(x, axis=1) - x[:, 0] - x[:, 1]
    scatter_cache(miss_rate, PATH1, "L1")
    scatter_cache(miss_rate, PATH1, "L2")

if __name__ == '__main__':
    main()


import numpy as np
import pandas as pd

# data = np.loadtxt('E:/Code/Java/cache-aware-allocation-main/result/45/makespan_1_2.0.txt', delimiter=",")

data = pd.read_table('E:/Code/Java/cache-aware-allocation-main/result/45/makespan_1_2.0.txt', sep=',', header=None)
data = data.iloc[500:, 0:10]
x = data.values
print(data)

print(x)
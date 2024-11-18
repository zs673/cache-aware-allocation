import numpy as np

# Data for the three methods across 10 instances
# method1 = np.array([0.995, 0.85, 0.788, 0.762, 0.748, 0.739, 0.737, 0.733, 0.731, 0.729])
# method2 = np.array([0.984, 0.739, 0.693, 0.672, 0.661, 0.655, 0.65, 0.648, 0.645, 0.644])
# # method3 = np.array([0.983, 0.614, 0.499, 0.479, 0.474, 0.474, 0.474, 0.473, 0.474, 0.473])
# # method3 = np.array([0.984, 0.609, 0.564, 0.552, 0.545, 0.542, 0.537, 0.537, 0.535, 0.534])
# method3 = np.array([0.984, 0.555, 0.495, 0.482, 0.477, 0.474, 0.474, 0.473, 0.473, 0.471])
# # # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1 = np.mean((method1 - method3) / method1) * 100
# improvement_over_method2 = np.mean((method2 - method3) / method2) * 100

# print(improvement_over_method1)
# print(improvement_over_method2)


# # Data for the two methods across 10 instances
# method2 = np.array([0.984, 0.739, 0.693, 0.672, 0.661, 0.655, 0.65, 0.648, 0.645, 0.644])
# method3_new = np.array([0.984, 0.576, 0.517, 0.503, 0.499, 0.495, 0.494, 0.494, 0.493, 0.492])

# # Calculate the average improvement of the lower method over the upper method
# improvement = np.mean((method2 - method3_new) / method2) * 100

# print(improvement)

method1 = np.array([0.984, 0.739, 0.693, 0.672, 0.661, 0.655, 0.65, 0.648, 0.645, 0.644])
method2 = np.array([0.984, 0.609, 0.564, 0.552, 0.545, 0.542, 0.537, 0.537, 0.535, 0.534])
method4 = np.array([0.984, 0.576, 0.517, 0.503, 0.499, 0.495, 0.494, 0.494, 0.493, 0.492])
method3 = np.array([0.984, 0.555, 0.495, 0.482, 0.477, 0.474, 0.474, 0.473, 0.473, 0.471])
# # Calculate the average improvement of method3 over method1 and method2
improvement_over_method1 = np.mean((method1 - method2) / method1) * 100
improvement_over_method2 = np.mean((method4 - method3) / method4) * 100

print(improvement_over_method1)
print(improvement_over_method2)


# # Data for the two methods across 10 instances
# method3 = np.array([0.983, 0.64, 0.528, 0.508, 0.504, 0.502, 0.502, 0.501, 0.501, 0.501])
# method4 = np.array([0.983, 0.613, 0.498, 0.479, 0.474, 0.473, 0.471, 0.472, 0.472, 0.472])

# # Calculate the average improvement of the lower method over the upper method
# improvement = np.mean((method3 - method4) / method3) * 100

# print(improvement)

# # Data for the three methods across 10 instances
# method1 = np.array([0.995, 0.87, 0.832, 0.822, 0.818, 0.816, 0.816, 0.816, 0.816, 0.817])
# method2 = np.array([0.983, 0.785, 0.735, 0.72, 0.714, 0.711, 0.714, 0.713, 0.712, 0.712])
# method3 = np.array([0.983, 0.614, 0.499, 0.479, 0.474, 0.474, 0.474, 0.473, 0.474, 0.473])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1 = np.mean((method1 - method3) / method1) * 100
# improvement_over_method2 = np.mean((method2 - method3) / method2) * 100

# print(improvement_over_method1, improvement_over_method2)

# # core 8 16 24 32
# # Data for the three methods across 10 instances
# method1 = np.array([0.995, 0.85, 0.788, 0.762, 0.748, 0.739, 0.737, 0.733, 0.731, 0.729])
# method2 = np.array([0.984, 0.739, 0.693, 0.672, 0.661, 0.655, 0.65, 0.648, 0.645, 0.644])
# method3 = np.array([0.984, 0.555, 0.495, 0.482, 0.477, 0.474, 0.474, 0.473, 0.473, 0.471])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1 = np.mean((method1 - method3) / method1) * 100
# improvement_over_method2 = np.mean((method2 - method3) / method2) * 100

# print(improvement_over_method1, improvement_over_method2)
# # 36.55% 29.00%
# 36.43115106179723 28.857970042249935
##### 1101 32.36697166857769 24.482706519904927

# # Data for the three methods across 10 instances for the second image
# method1_img2 = np.array([0.998, 0.942, 0.91, 0.891, 0.878, 0.867, 0.858, 0.855, 0.851, 0.848])
# method2_img2 = np.array([0.963, 0.643, 0.605, 0.589, 0.582, 0.577, 0.575, 0.572, 0.571, 0.57])
# method3_img2 = np.array([0.963, 0.517, 0.456, 0.441, 0.435, 0.432, 0.431, 0.429, 0.429, 0.427])


# # Calculate the average improvement of method3 over method1 and method2 for each image
# improvement_over_method1_img2 = np.mean((method1_img2 - method3_img2) / method1_img2) * 100
# improvement_over_method2_img2 = np.mean((method2_img2 - method3_img2) / method2_img2) * 100


# print(improvement_over_method1_img2, improvement_over_method2_img2)
# # 46.52 24.57
# 46.40817820280168 24.403923383590204
### 1101 44.84739452326617 21.97386427263516


# # Data for the three methods across 10 instances for the fourth image
# method1_img4 = np.array([0.999, 0.968, 0.949, 0.937, 0.928, 0.922, 0.915, 0.912, 0.908, 0.903])
# method2_img4 = np.array([0.99, 0.56, 0.507, 0.491, 0.481, 0.476, 0.47, 0.467, 0.466, 0.464])
# method3_img4 = np.array([0.99, 0.5, 0.439, 0.424, 0.417, 0.415, 0.413, 0.412, 0.411, 0.411])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1_img4 = np.mean((method1_img4 - method3_img4) / method1_img4) * 100
# improvement_over_method2_img4 = np.mean((method2_img4 - method3_img4) / method2_img4) * 100

# print(improvement_over_method1_img4, improvement_over_method2_img4)
# # 49.58% 13.60%
# 49.5507593225868 13.550760694544724
######1101  48.670047890055024 11.102282545048867

# # Data for the three methods across 10 instances for the fifth image
# method1_img5 = np.array([1, 0.978, 0.966, 0.956, 0.95, 0.944, 0.939, 0.937, 0.934, 0.929])
# method2_img5 = np.array([0.999, 0.506, 0.442, 0.422, 0.414, 0.41, 0.409, 0.405, 0.404, 0.402])
# method3_img5 = np.array([0.999, 0.485, 0.426, 0.412, 0.407, 0.405, 0.404, 0.403, 0.402, 0.402])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1_img5 = np.mean((method1_img5 - method3_img5) / method1_img5) * 100
# improvement_over_method2_img5 = np.mean((method2_img5 - method3_img5) / method2_img5) * 100

# print(improvement_over_method1_img5, improvement_over_method2_img5)
# #  50.98%  3.18%
# 50.94891142080438 3.123987069058885
#####1101 50.522161679208885 1.526147938130191

# # Provided improvement values
# improvement_row1 = np.array([32.37, 44.85, 48.67, 50.52])
# improvement_row2 = np.array([1.53, 11.10, 21.97, 24.48])
# # Calculate the average for each row
# average_row1 = np.mean(improvement_row1)
# average_row2 = np.mean(improvement_row2)

# print(average_row1, average_row2)

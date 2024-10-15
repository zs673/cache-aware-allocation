import numpy as np

# Data for the three methods across 10 instances
# method1 = np.array([0.995, 0.87, 0.832, 0.822, 0.818, 0.816, 0.816, 0.816, 0.816, 0.817])
# method2 = np.array([0.983, 0.785, 0.735, 0.72, 0.711, 0.714, 0.714, 0.713, 0.712, 0.712])
# method3 = np.array([0.983, 0.64, 0.528, 0.508, 0.504, 0.502, 0.502, 0.501, 0.501, 0.501])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1 = np.mean((method1 - method3) / method1) * 100
# improvement_over_method2 = np.mean((method2 - method3) / method2) * 100

# print(improvement_over_method1)
# print(improvement_over_method2)

# Data for the two methods across 10 instances
# method2 = np.array([0.983, 0.785, 0.735, 0.72, 0.714, 0.711, 0.714, 0.713, 0.712, 0.712])
# method3_new = np.array([0.983, 0.659, 0.574, 0.559, 0.555, 0.553, 0.553, 0.552, 0.552, 0.55])

# # Calculate the average improvement of the lower method over the upper method
# improvement = np.mean((method2 - method3_new) / method2) * 100

# print(improvement)


# # Data for the two methods across 10 instances
# method3 = np.array([0.983, 0.64, 0.528, 0.508, 0.504, 0.502, 0.502, 0.501, 0.501, 0.501])
# method4 = np.array([0.983, 0.613, 0.498, 0.479, 0.474, 0.473, 0.471, 0.472, 0.472, 0.472])

# # Calculate the average improvement of the lower method over the upper method
# improvement = np.mean((method3 - method4) / method3) * 100

# print(improvement)

# # Data for the three methods across 10 instances
# method1 = np.array([0.995, 0.87, 0.832, 0.822, 0.818, 0.816, 0.816, 0.816, 0.816, 0.817])
# method2 = np.array([0.983, 0.785, 0.735, 0.72, 0.714, 0.711, 0.714, 0.713, 0.712, 0.712])
# method3 = np.array([0.983, 0.613, 0.498, 0.479, 0.474, 0.473, 0.471, 0.472, 0.472, 0.472])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1 = np.mean((method1 - method3) / method1) * 100
# improvement_over_method2 = np.mean((method2 - method3) / method2) * 100

# print(improvement_over_method1, improvement_over_method2)

# # core 8 16 24 32
# # Data for the three methods across 10 instances
# method1 = np.array([0.995, 0.87, 0.832, 0.822, 0.818, 0.816, 0.816, 0.816, 0.816, 0.817])
# method2 = np.array([0.983, 0.785, 0.735, 0.72, 0.714, 0.711, 0.714, 0.713, 0.712, 0.712])
# method3 = np.array([0.983, 0.613, 0.498, 0.477, 0.474, 0.473, 0.472, 0.471, 0.471, 0.472])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1 = np.mean((method1 - method3) / method1) * 100
# improvement_over_method2 = np.mean((method2 - method3) / method2) * 100

# improvement_over_method1, improvement_over_method2
# # 36.55% 29.00%

# # Data for the three methods across 10 instances for the second image
# method1_img2 = np.array([0.998, 0.947, 0.93, 0.926, 0.925, 0.924, 0.926, 0.924, 0.924, 0.925])
# method2_img2 = np.array([0.963, 0.681, 0.63, 0.617, 0.613, 0.611, 0.609, 0.611, 0.608, 0.607])
# method3_img2 = np.array([0.963, 0.571, 0.462, 0.44, 0.436, 0.434, 0.433, 0.432, 0.431, 0.431])


# # Calculate the average improvement of method3 over method1 and method2 for each image
# improvement_over_method1_img2 = np.mean((method1_img2 - method3_img2) / method1_img2) * 100
# improvement_over_method2_img2 = np.mean((method2_img2 - method3_img2) / method2_img2) * 100


# print(improvement_over_method1_img2, improvement_over_method2_img2)
# # 46.52 24.57


# # Data for the three methods across 10 instances for the fourth image
# method1_img4 = np.array([0.999, 0.97, 0.959, 0.959, 0.957, 0.956, 0.958, 0.957, 0.959, 0.958])
# method2_img4 = np.array([0.99, 0.592, 0.522, 0.506, 0.5, 0.494, 0.491, 0.489, 0.489, 0.489])
# method3_img4 = np.array([0.99, 0.543, 0.438, 0.419, 0.416, 0.415, 0.414, 0.414, 0.414, 0.414])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1_img4 = np.mean((method1_img4 - method3_img4) / method1_img4) * 100
# improvement_over_method2_img4 = np.mean((method2_img4 - method3_img4) / method2_img4) * 100

# print(improvement_over_method1_img4, improvement_over_method2_img4)
# # 49.58% 13.60%

# # Data for the three methods across 10 instances for the fifth image
# method1_img5 = np.array([1, 0.982, 0.973, 0.972, 0.972, 0.97, 0.971, 0.971, 0.971, 0.971])
# method2_img5 = np.array([0.999, 0.537, 0.454, 0.435, 0.428, 0.421, 0.419, 0.416, 0.415, 0.412])
# method3_img5 = np.array([0.999, 0.522, 0.426, 0.410, 0.407, 0.407, 0.407, 0.406, 0.406, 0.406])

# # Calculate the average improvement of method3 over method1 and method2
# improvement_over_method1_img5 = np.mean((method1_img5 - method3_img5) / method1_img5) * 100
# improvement_over_method2_img5 = np.mean((method2_img5 - method3_img5) / method2_img5) * 100

# print(improvement_over_method1_img5, improvement_over_method2_img5)
# #  50.98%  3.18%

# # Provided improvement values
improvement_row1 = np.array([36.55, 46.52, 49.58, 50.98])
improvement_row2 = np.array([3.18, 13.60, 24.57, 29.00])

# Calculate the average for each row
average_row1 = np.mean(improvement_row1)
average_row2 = np.mean(improvement_row2)

print(average_row1, average_row2)

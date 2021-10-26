from drs import drs_module as drs
import sys
import ast
import math
import numpy as np
from scipy.stats import dirichlet
import random

# test code:
if __name__ == "__main__":
	# print('Number of arguments:', len(sys.argv), 'arguments.')
	# print('Argument List:', str(sys.argv))
	
	num = ast.literal_eval(sys.argv[1])
	num_sum = ast.literal_eval(sys.argv[2])

	res = drs.drs(num, num_sum)
	print(res)
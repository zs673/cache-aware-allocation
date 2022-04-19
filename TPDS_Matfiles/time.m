t1 = readmatrix("../time 1.txt");
t2 = readmatrix("../time 2.txt");
t3 = readmatrix("../time 3.txt");
t4 = readmatrix("../time 4.txt");
t5 = readmatrix("../time 5.txt");

t1_nonzero = nonzeros(t1)./1000;
t2_nonzero = nonzeros(t2)./1000;
t3_nonzero = nonzeros(t3)./1000000;
t4_nonzero = nonzeros(t4)./1000000;
t5_nonzero = nonzeros(t5)./1000000;

t1_avg = sum(t1_nonzero) / length(t1_nonzero);
t2_avg = sum(t2_nonzero) / length(t2_nonzero);
t3_avg = sum(t3_nonzero) / length(t3_nonzero);
t4_avg = sum(t4_nonzero) / length(t4_nonzero);
t5_avg = sum(t5_nonzero) / length(t5_nonzero);

t1_med = median(t1_nonzero);
t2_med = median(t2_nonzero);
t3_med = median(t3_nonzero);
t4_med = median(t4_nonzero);
t5_med = median(t5_nonzero);

t1_std = std(t1_nonzero);
t2_std = std(t2_nonzero);
t3_std = std(t3_nonzero);
t4_std = std(t4_nonzero);
t5_std = std(t5_nonzero);
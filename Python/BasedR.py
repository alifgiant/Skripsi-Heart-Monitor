import matplotlib.pyplot as plt
file = open('data2.csv')

data = []
for i in file:
    data.append(float(i))

raw = data[5000:10000]  # [start inclusive:stop exclusive]

# Portion pertaining to Pan-Tompkins QRS detection

M = 5
N = 30
winSize = 250
HP_CONSTANT = 1. / M

# resolution of RNG
RAND_RES = 100000000

# interval at which to take samples and iterate algorithm (microseconds)
PERIOD = 1000000. / winSize

tmp = 0

# circular buffer for input ecg signal
# we need to keep a history of M + 1 samples for HP filter
ecg_buff = [0]*(M + 1)
ecg_buff_WR_idx = 0
ecg_buff_RD_idx = 0

# circular buffer for input ecg signal
# we need to keep a history of N+1 samples for LP filter
hp_buff = [0]*(N + 1)
hp_buff_WR_idx = 0
hp_buff_RD_idx = 0

# LP filter outputs a single point for every input point
# This goes straight to adaptive filtering for eval
next_eval_pt = 0.

# running sums for HP and LP filters, values shifted in FILO
hp_sum = 0.
lp_sum = 0.

# working variables for adaptive thresholding
treshold = 0
triggered = False
trig_time = 0
win_max = 0
win_idx = 0

# numebr of starting iterations, used determine when moving windows are filled
number_iter = 0


def process(new_ecg_pt):
    # copy new point into circular buffer, increment index
    global ecg_buff_WR_idx
    global ecg_buff

    # print ecg_buff, len(ecg_buff), ecg_buff[ecg_buff_WR_idx]
    # print ecg_buff_WR_idx

    ecg_buff[ecg_buff_WR_idx] = new_ecg_pt
    ecg_buff_WR_idx += 1
    ecg_buff_WR_idx %= (M + 1)

    global number_iter
    global hp_sum
    global ecg_buff_RD_idx
    global hp_buff_WR_idx
    global tmp

    # High pass filtering
    if number_iter < M:
        # first fill buffer with enough points for HP filter
        hp_sum += ecg_buff[ecg_buff_RD_idx]
        hp_buff[hp_buff_WR_idx] = 0
    else:
        hp_sum += ecg_buff[ecg_buff_RD_idx]
        tmp = ecg_buff_RD_idx - M
        if tmp < 0:
            tmp += M + 1

        hp_sum -= ecg_buff[tmp]

        y1 = 0
        y2 = 0

        tmp = ecg_buff_RD_idx - ((M+1) / 2)
        if tmp < 0:
            tmp += M + 1

        y2 = ecg_buff[tmp]
        y1 = HP_CONSTANT * hp_sum

        hp_buff[hp_buff_WR_idx] = y2 - y1

    holder = hp_buff[hp_buff_WR_idx]

    # done reading ECG buffer, increment position
    ecg_buff_RD_idx += 1
    ecg_buff_RD_idx %= (M + 1)

    # done writing to HP buffer, increment position
    hp_buff_WR_idx += 1
    hp_buff_WR_idx %= (N + 1)

    # Low pass filtering
    global lp_sum
    global hp_buff_RD_idx
    global next_eval_pt
    # shift in new sample from high pass filter
    lp_sum += hp_buff[hp_buff_RD_idx] * hp_buff[hp_buff_RD_idx]

    if number_iter < N:
        # first fill buffer with enough points for LP filter
        next_eval_pt = 0
    else:
        # shift out oldest data point
        tmp = hp_buff_RD_idx - N
        if tmp < 0:
            tmp += (N+1)

        lp_sum -= hp_buff[tmp] * hp_buff[tmp]

        next_eval_pt = lp_sum

    # done reading HP buffer, increment position
    hp_buff_RD_idx += 1
    hp_buff_RD_idx %= (N + 1)

    global treshold
    # Adapative thresholding beat detection
    # set initial threshold
    if number_iter < winSize:
        if next_eval_pt > treshold:
            treshold = next_eval_pt

        # only increment number_iter iff it is less than winSize
        # if it is bigger, then the counter serves no further purpose
        number_iter += 1

    return holder, next_eval_pt

# print raw
lowed = []
highed = []
for i in raw:
    high, low = process(i)
    lowed.append(low)
    highed.append(high)
    # break

# plt.plot(lowed)
# plt.ylabel('jantung')
# plt.show()
# #
#
# fig, (ax_orig, ax_lowed, ax_highed, ax_derr, ax_square, ax_6, resss) = plt.subplots(7, 1, sharex=True)
fig, (ax_orig, ax_lowed, ax_highed) = plt.subplots(3, 1, sharex=True)
ax_orig.plot(raw)
ax_lowed.plot(lowed)
ax_highed.plot(highed)

plt.tight_layout()
plt.show()

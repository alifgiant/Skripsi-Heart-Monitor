# import matplotlib.pyplot as plt
# # from scipy.signal import butter, filtfilt
#
# import numpy as np
#
# fc = 0.1  # Cutoff frequency as a fraction of the sampling rate (in (0, 0.5)).
# b = 0.08  # Transition band, as a fraction of the sampling rate (in (0, 0.5)).
# N = int(np.ceil((4 / b)))
# if not N % 2: N += 1  # Make sure that N is odd.
# n = np.arange(N)
#
# # Compute a low-pass filter.
# h = np.sinc(2 * fc * (n - (N - 1) / 2.))
# w = np.blackman(N)
# h = h * w
# h = h / np.sum(h)
#
# # Create a high-pass filter from the low-pass filter through spectral inversion.
# h = -h
# h[(N - 1) / 2] += 1
#
# s = np.convolve(data_source[1:200], h)
#
# has = []
#
# for da in data_source[1:200]:
#     # has.append(da)
#     has.append(np.convolve(da, h))
#
# # ax_win = plt.subplots(3, 1, sharex=True)
# # # plt.plot(data_source)
# # ax_win.plot(has)
# # plt.plot(s)
# # plt.ylabel('some numbers')
# # plt.show()
#
# fig, (ax_orig, ax_win, ax_filt) = plt.subplots(3, 1, sharex=True)
# ax_orig.plot(data_source[1:200])
# ax_orig.set_title('Original pulse')
# # ax_orig.margins(0, 0.1)
# ax_win.plot(np.append(np.convolve(data_source[1:100], h)[:-24],np.convolve(data_source[100:200], h)[24:]))
# ax_win.set_title('Filter impulse response')
# # ax_win.margins(0, 0.1)
# ax_filt.plot(s)
# ax_filt.set_title('Filtered signal')
# # ax_filt.margins(0, 0.1)
# fig.tight_layout()
# # fig.show()
#
# plt.show()
#
# fs = 350.
#
# f1 = 5  # cuttoff low frequency to get rid of baseline wander
# f2 = 15  # cuttoff frequency to discard high frequency noise
# Wn = [y/fs for y in [ x*2. for x in[f1, f2]]]  # cutt off based on fs
# N = 3  # order of 3 less processing
# [b, a] = butter(N, Wn, 'bandpass')  # bandpass filtering
# # print b, a
# ecg_h = filtfilt(b, a, data_source)
# abss = [abs(x) for x in ecg_h]
# ecg_h = [ecg/max(abss) for ecg in ecg_h]
# # for ecg in ecg_h:
# #     print ecg
#
# # # ax(3) = subplot(323)
# # fig, (ax_orig, ax_filter) = plt.subplots(2, 1, sharex=True)
# # ax_orig.plot(data_source)
# # ax_orig.set_title('Original pulse')
# # ax_filter.plot(ecg_h)
# # ax_filter.set_title('Band Pass Filtered')
# # fig.tight_layout()
# # plt.show()

import matplotlib.pyplot as plt
import CsvLoader
from PanTom import *

detector = Detector()
source = CsvLoader.load()

for data in source:
    dat = detector.add_data(data, True)
    if dat[0]!='filling':
        print dat[0], dat[1]


# fig, (ax_orig, ax_filter) = plt.subplots(2, 1, sharex=True)
# ax_orig.plot(data_source[:200])
# ax_filter.plot(res)
# plt.tight_layout()
# plt.show()

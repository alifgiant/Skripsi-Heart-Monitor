import operator
import itertools

PEAK = 0.5

BEAT_CLASS = {
    'Normal': 1,
    'PVC': 2,
    'VF': 3,
    'BII': 4
}


class BeatDetector(object):
    def __init__(self, sampling_freq, window_duration=8, val_by_mean=1, idx_by_r=0.5):
        # experiment variable
        self.window_duration = window_duration  # in second
        self.freq = sampling_freq  # total sample in a second
        self.val_by_mean = val_by_mean  # val threshold by mean coefficient
        self.idx_by_r_avg = idx_by_r  # r-dis threshold by r-dis average coefficient

        # data holder
        self.sample = []
        self.rr_holder = []                

        # flags
        self.r_distance = 0

    def execute_buffer(self):
        # calculate threshold
        mean = sum(self.sample) / len(self.sample)
        threshold = self.val_by_mean * mean

        # peak flags
        is_peak_area = False
        temp_peak_val = -1

        # peak and threshold holder
        threshold_feed = [threshold] * len(self.sample)
        peak_feed = [0] * len(self.sample)
        peaks = []

        # find peaks in windows
        for idx, val in enumerate(self.sample):
            # for idx in range(len(self.sample)):
            # val = self.sample[idx]
            if val > threshold:
                if not is_peak_area:  # peak area just begin
                    is_peak_area = True
                    temp_peak_val = val
                    peaks.append(idx)
                elif val > temp_peak_val:  # is in peak area, and current val higher than last
                    # set peak to current val
                    temp_peak_val = val
                    # update last peak position
                    peaks[-1] = idx
            else:
                is_peak_area = False

        if len(peaks) > 0:
            # calc r-dis average,
            # distance from last window r + idx of last peak
            r_avg = (self.r_distance + peaks[-1]) / len(peaks)
            r_threshold = r_avg * self.idx_by_r_avg

            # add last peak position of last window
            peaks.insert(0, -self.r_distance)

            # remove false peak
            last_picked = 0
            peaks_removed = []
            for i in range(len(peaks)-1):
                current = peaks[i+1]
                last = peaks[last_picked]
                if current - last >= r_threshold:
                    peaks_removed.append(current)
                    last_picked = i+1  # set to current

            # safe this window last r distance to end of window
            self.r_distance = len(self.sample) - (peaks_removed[-1] + 1)  # idx+1, because idx start 0

            # get set peak positions
            for idx in peaks_removed:
                peak_feed[idx] = PEAK


        # clear buffer
        self.sample.clear()
        return peak_feed, threshold_feed

    def detect(self, data):
        # load data to window
        self.sample.append(data)

        # buffer period is (n * freq) of incoming signal.
        # find peaks every buffer period (seconds).
        # if len(self.sample) == (self.buffer_duration * self.freq):
        if len(self.sample) % (self.window_duration * self.freq) == 0:            
            return self.execute_buffer()


class ArrhythmiaDetector(object):
    """
    Based on An arrhythmia classification system based on the RR-interval signal
    by M.G. Tsipouras, D.I. Fotiadis, D. Sideris
    """

    def __init__(self, sampling_freq):
        # experiment variable
        self.freq = sampling_freq  # total sample in a second

        # holder
        self.rr_holder = []
        self.beat_class = []        

    def __get_r_window(self, i):
        return self.rr_holder[i-1], self.rr_holder[i], self.rr_holder[i-+1]

    def __duration(self, x):
        return x * self.freq

    def __is_exceed_holder(self, i):
        return i + 1 >= len(self.rr_holder)

    def __check1(self, window):
        rr1, rr2, rr3 = window
        return rr2 < self.__duration(0.6) and 1.8 * rr2 < rr1

    def __check2(self, window):
        rr1, rr2, rr3 = window
        return (rr1 < self.__duration(0.7) and rr2 < self.__duration(0.7) and rr3 < self.__duration(0.7)) or \
               (rr1 + rr2 + rr3 < self.__duration(1.7))

    @staticmethod
    def __check3(window):
        rr1, rr2, rr3 = window
        return 1.15*rr2 < rr1 and 1.15 * rr2 < rr3

    @staticmethod
    def __mean(x1, x2):
        return (x1 + x2) / 2

    def __check4(self, window):
        rr1, rr2, rr3 = window
        return (abs(rr1 - rr2) < self.__duration(0.3)) and \
               (rr1 < self.__duration(0.8) or rr2 < self.__duration(0.8)) and \
               (rr3 > 1.2 * self.__mean(rr1, rr2))

    def __check5(self, window):
        rr1, rr2, rr3 = window
        return (abs(rr2 - rr3) < self.__duration(0.3)) and \
               (rr2 < self.__duration(0.8) or rr3 < self.__duration(0.8)) and \
               (rr1 > 1.2 * self.__mean(rr2, rr3))

    def __check6(self, window):
        rr1, rr2, rr3 = window
        return (self.__duration(2.2) < rr2 < self.__duration(3)) and \
               (abs(rr1 - rr2) < self.__duration(0.2) or abs(rr2 - rr3) < self.__duration(0.2))

    def detect(self, rr_window):
        # add new window to process holder
        self.rr_holder += rr_window
        # self.beat_class = [0] * len(self.rr_holder)  # category 0, no category
        self.beat_class = [1] * len(self.rr_holder)  # category 1, assume all normal

        should_stop = False
        i = 1
        pulse = 0
        while not self.__is_exceed_holder(i) and not should_stop:  # window iterator
            # VF area
            if self.__check1(self.__get_r_window(i)):
                self.beat_class[i] = 3
                pulse += 1
                i += 1
                while not self.__is_exceed_holder(i) and self.__check2(self.__get_r_window(i)):
                    self.beat_class[i] = 3
                    pulse += 1
                    i += 1
                if self.__is_exceed_holder(i):
                    # stop and return to i - pulse, re check after new window arrived
                    should_stop = True
                    i -= pulse                    
                elif pulse < 4:
                    while pulse > 0:
                        i -= 1
                        pulse -= 1
                        self.beat_class[i] = 1  # set to category 1

            # PVC area
            window = self.__get_r_window(i)
            if not should_stop and (self.__check3(window) or self.__check4(window) or self.__check5(window)):
                self.beat_class[i] = 2  # set to category 2

            # Heart Block area
            if not should_stop and self.__check6(window):
                self.beat_class[i] = 4  # set to category 4

            i += 1

        self.rr_holder = self.rr_holder[i-1:]  # slice from i - 1 to last

        # remove first class, because un calculated or has calculated on last window
        # remove from i to last, because un calculated, wait for next window
        return self.beat_class[1:i]


class ArrhythmiaEpisodeDetector(object):
    def __init__(self):
        self.position = 1

    def detect(self, beat_class):
        if self.position == 1:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 1
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 2
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 7
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 8
        elif self.position == 2:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 3
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 5
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 1
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 1
        elif self.position == 3:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 4
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 2
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 1
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 1
        elif self.position == 4:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 1
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 2
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 1
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 1
        elif self.position == 5:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 1
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 6
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 1
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 1
        elif self.position == 6:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 1
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 6
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 7
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 8
        elif self.position == 7:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 1
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 2
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 7
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 8
        elif self.position == 8:
            if beat_class == BEAT_CLASS['Normal']:
                self.position = 1
            elif beat_class == BEAT_CLASS['PVC']:
                self.position = 2
            elif beat_class == BEAT_CLASS['VF']:
                self.position = 7
            elif beat_class == BEAT_CLASS['BII']:
                self.position = 8

        return self.position


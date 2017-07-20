import operator
import itertools

PEAK = 0.5


class Detector(object):
    def __init__(self, sampling_freq, chunk_size_by_freq=0.2, init_duration=8, threshold_by_mean=1):
        # experiment variable
        self.init_duration = init_duration  # in second
        self.freq = sampling_freq  # sample count in a second
        self.chunk_size = int(sampling_freq * chunk_size_by_freq)  # detection chunks

        # data holder
        self.sample = []
        self.search_back_sample = []
        self.search_back_peak = []
        self.rr_holder = []
        self.init_sample = []
        self.init_rr = []

        # THRESHOLD vars
        self.SPKI = 0
        self.NPKI = 0
        self.THRESHOLD1 = 0
        self.THRESHOLD2 = 0
        self.RR_AVERAGE1 = 0
        self.RR_AVERAGE2 = 0
        self.RR_HIGH_LIMIT = 0
        self.RR_LOW_LIMIT = 0

        # flags
        self.r_distance = 0
        self.is_initiation_period = True

    def __initialization_threshold(self):
        # Divide to 8 part (1s chunk), sized of sampling freq
        chunks = [self.sample[i:i + self.freq] for i in range(0, len(self.sample), self.freq)]
        # find temp peaks to use as base threshold
        max_chunks = [max(enumerate(chunk), key=operator.itemgetter(1)) for chunk in chunks]

        for max_chunk, pos in zip(max_chunks, range(len(max_chunks))):
            idx, val = max_chunk
            self.SPKI = 0.125 * val + 0.875 * self.SPKI
            self.THRESHOLD1 = self.NPKI + 0.25 * (self.SPKI - self.NPKI)
            self.THRESHOLD2 = self.THRESHOLD1 * 0.5

        last_max_idx, last_max_val = max_chunks[-1]
        last_max_idx += 1  # +1 because idx start 0

        '''
        if peak always found on last sample in a second. the average is must equal to sample count in second/
        Whenever a peak found, as long the last peak is found on last sample of chunk, the total RR distance
        is equal to all chunk length. So init RR average only is equal to
        avr = (((chunk count - 1) * chunk length) + last peak idx) / chunk count 
        '''
        self.RR_AVERAGE1 = (((self.init_duration - 1) * self.freq) + last_max_idx) / len(max_chunks)
        self.RR_AVERAGE2 = self.RR_AVERAGE1

        self.RR_LOW_LIMIT = self.RR_AVERAGE2 * 0.92
        self.RR_HIGH_LIMIT = self.RR_AVERAGE2 * 1.16

        self.is_initiation_period = False

    def __update_r_threshold(self, dist):
        """"""
        """
        -----DISTANCE THRESHOLD-----
        Updates dist thresholds based on last 8 RR intervals.
        This method is normally used after initialization stage of algorithm.
        """
        self.rr_holder.append(dist)
        if len(self.rr_holder) >= 8:
            self.RR_AVERAGE1 = sum(self.rr_holder) / 8.
            self.RR_AVERAGE2 = self.RR_AVERAGE1

            self.RR_LOW_LIMIT = 0.92 * self.RR_AVERAGE2
            self.RR_HIGH_LIMIT = 1.16 * self.RR_AVERAGE2

            self.rr_holder.pop(-1)

    def __update_val_threshold(self, peak_val, is_search_back=False, is_noise_peak=False):
        # VAL THRESHOLD
        if is_noise_peak:
            self.NPKI = 0.125 * peak_val + 0.875 * self.NPKI
        elif is_search_back:
            self.SPKI = 0.25 * peak_val + 0.75 * self.SPKI
        else:
            self.SPKI = 0.125 * peak_val + 0.875 * self.SPKI

        self.THRESHOLD1 = self.NPKI + 0.25 * (self.SPKI - self.NPKI)
        self.THRESHOLD2 = 0.5 * self.THRESHOLD1

    def __search_back(self):
        idx, val = next(itertools.chain(iter((idx, val) for idx, val in enumerate(self.search_back_sample)
                                             if val > self.THRESHOLD2), [(-1, -1)]))

        t_search_back_peak = [0] * len(self.search_back_sample)
        if idx > -1:
            self.__update_val_threshold(val, is_search_back=True)
            self.__update_r_threshold(idx)

            # reset r distance and clear search back sample
            self.r_distance = len(self.search_back_sample) - idx

            t_search_back_peak[idx] = PEAK
            self.search_back_peak += t_search_back_peak
        else:
            self.search_back_peak += t_search_back_peak
        return t_search_back_peak  # search_back_result

    def __execute_chunk(self, chunk):
        peak = [0]*len(chunk)
        # check if its distance is enough
        if self.r_distance + len(chunk) < self.RR_LOW_LIMIT:
            self.r_distance += len(chunk)
            self.search_back_sample += chunk
            return [], []

            # impossible area to find peak
            # self.search_back_peak += [0] * (len(self.search_back_sample) + len(chunk))
            # self.search_back_sample.clear()
            # return peak, [self.THRESHOLD1] * len(chunk)  # impossible peak, so 0
        else:
            remains = int(self.RR_LOW_LIMIT - self.r_distance)
            qrs_margin_distance = int(self.RR_HIGH_LIMIT - self.RR_LOW_LIMIT + remains)
            # qrs_margin_distance = 0
            remains = remains if remains > 0 else 0
            qrs_margin_distance = qrs_margin_distance if qrs_margin_distance > 0 else len(chunk)

            idx, val = max(enumerate(chunk[remains:qrs_margin_distance]), key=operator.itemgetter(1))
            idx += remains  # real idx in chunk

            if val >= self.THRESHOLD1:
                self.__update_val_threshold(val)
                self.__update_r_threshold(self.r_distance + remains)
                self.r_distance = 0

                search_back_peak_t = [0] * len(self.search_back_sample)
                self.search_back_peak += search_back_peak_t + ([0] * len(chunk))
                self.search_back_sample.clear()

                peak[idx] = PEAK  # temp number
                return search_back_peak_t+peak, [self.THRESHOLD1] * (len(search_back_peak_t) + len(chunk))

            elif self.r_distance + remains > self.RR_HIGH_LIMIT:
                self.r_distance += len(chunk)

                self.search_back_sample += chunk
                res = self.__search_back()
                self.search_back_sample.clear()

                return res, [self.THRESHOLD2] * len(res)
            else:
                self.r_distance += len(chunk)
                self.search_back_sample += chunk

                # update threshold by noise
                self.__update_val_threshold(val, is_noise_peak=True)

                return [], []
                # return peak, [self.THRESHOLD1] * len(chunk)

    def detect(self, data):
        # load data to chunk
        self.sample.append(data)

        # Initialization period is first 8 seconds of incoming signal.
        # Samples are aggregated and for every 1s period the highest peak is assumed to be R peak.
        # Initial thresholds are set after all 8s are acquired.
        if self.is_initiation_period and len(self.sample) == self.init_duration * self.freq:
            self.__initialization_threshold()

            # re-calculate the 8s signal using threshold, but cut back into chunk size
            chunks = [self.sample[i:i + self.chunk_size] for i in range(0, len(self.sample), self.chunk_size)]

            temp_p = []
            temp_t = []
            for chunk in chunks:
                # execute pan tompkins
                peak, thr = self.__execute_chunk(chunk)
                temp_p += peak
                temp_t += thr

            self.sample.clear()
            return temp_p, temp_t

        elif not self.is_initiation_period and len(self.sample) >= self.chunk_size:
            # execute pan tompkins
            peak, thr = self.__execute_chunk(self.sample)

            self.sample.clear()
            return peak, thr

    def detect_test(self, data):
        # load data to chunk
        self.sample.append(data)

        # Initialization period is first 8 seconds of incoming signal.
        # Samples are aggregated and for every 1s period the highest peak is assumed to be R peak.
        # Initial thresholds are set after all 8s are acquired.
        if self.is_initiation_period and len(self.sample) == self.init_duration * self.freq:
            # Divide to 8 part (1s chunk), sized of sampling freq
            # chunks = [self.sample[i:i + self.freq] for i in range(0, len(self.sample), self.freq)]
            chunks = [self.sample[i:i + self.freq] for i in range(0, len(self.sample), self.freq)]
            # find temp peaks to use as base threshold
            max_chunks = [max(enumerate(chunk), key=operator.itemgetter(1)) for chunk in chunks]

            idx_temp = []
            for max_chunk, pos in zip(max_chunks, range(len(max_chunks))):
                idx, val = max_chunk
                idx_temp.append(idx + (pos * self.freq))
                self.SPKI = 0.125 * val + 0.875 * self.SPKI
                self.THRESHOLD1 = self.NPKI + 0.25 * (self.SPKI - self.NPKI)
                self.THRESHOLD2 = self.THRESHOLD1 * 0.5

            last_max_idx, last_max_val = max_chunks[-1]
            last_max_idx += 1  # +1 because idx start 0

            '''
            if peak always found on last sample in a second. the average is must equal to sample count in second/
            Whenever a peak found, as long the last peak is found on last sample of chunk, the total RR distance
            is equal to all chunk length. So init RR average only is equal to
            avr = (((chunk count - 1) * chunk length) + last peak idx) / chunk count 
            '''
            self.RR_AVERAGE1 = (((self.init_duration - 1) * self.freq) + last_max_idx) / self.init_duration
            self.RR_AVERAGE2 = self.RR_AVERAGE1

            self.RR_LOW_LIMIT = self.RR_AVERAGE2 * 0.92
            self.RR_HIGH_LIMIT = self.RR_AVERAGE2 * 1.16

            # self.is_initiation_period = False
            print(len(self.sample))
            peak = [0] * len(self.sample)
            for x in idx_temp:
                # print(x)
                peak[x] = 1
            return peak, [self.THRESHOLD1] * len(self.sample)

        # elif not self.is_initiation_period and len(self.sample) >= self.chunk_size:
        #     # execute pan tompkins
        #     peak, thr = self.__execute_chunk(self.sample)
        #
        #     self.sample.clear()
        #     return peak, thr

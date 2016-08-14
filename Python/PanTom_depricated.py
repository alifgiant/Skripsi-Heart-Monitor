ECG_BUFF_SIZE = 12
LP_BUFF_SIZE = 32

class PanTom(object):
    number_iter = 0

    # ECG VAR
    ecg_idx = 0
    ecg_buffer = []  # keep it's length equal to ECG_BUFF_SIZE

    # LOW PASS VAR
    lp_idx = 0
    lp_buffer = []  # keep it's length equal to LP_BUFF_SIZE

    # HIGH PASS VAR
    last_HP_val = 0

    def __init__(self):

        pass

    def add_data(self, raw):
        # count data delay
        if self.number_iter < ECG_BUFF_SIZE or self.number_iter < LP_BUFF_SIZE:
            self.number_iter += 1

        # add data to ecg buffer, and maintain the length
        # print self.ecg_idx
        self.ecg_buffer.append(raw)
        if self.ecg_idx <= ECG_BUFF_SIZE:  # only add on first run
            self.ecg_idx += 1
        if self.ecg_idx > ECG_BUFF_SIZE:
            self.ecg_buffer.pop(0)
        # print len(self.ecg_buffer)

        low_filterred = self.low_pass_filter()
        # # high_filterred = self.high_pass_filter(low_filterred)
        # # differentiated = self.differentiate(high_filterred)
        # # squared = self.squaring(differentiated)
        #
        return low_filterred

    def __get_ecg_data(self, position):
        if position > 0:
            # print len(self.ecg_buffer), position, position-1
            return self.ecg_buffer[position-2]
        return 0

    def __get_lp_filter_data(self, position):
        if position > 0:
            return self.lp_buffer[position-2]
        return 0

    def low_pass_filter(self):
        if self.number_iter >= ECG_BUFF_SIZE:
            result = 2*self.__get_lp_filter_data(self.lp_idx-1) - self.__get_lp_filter_data(self.lp_idx-2) \
                + self.__get_ecg_data(self.ecg_idx) - 2*self.__get_ecg_data(self.ecg_idx-6) \
                + self.__get_ecg_data(self.ecg_idx-12)

            # print self.lp_idx
            self.lp_buffer.append(result)
            if self.lp_idx <= LP_BUFF_SIZE:  # only add on first run
                self.lp_idx += 1
            if self.lp_idx > LP_BUFF_SIZE:
                self.lp_buffer.pop(0)
            # print len(self.lp_buffer)

            # print result
            if result in [float('inf'), float('-inf')]:
                print 'log', self.__get_lp_filter_data(self.lp_idx-1), self.__get_ecg_data(self.ecg_idx)
                return result
            return result
        return 0

    def high_pass_filter(self, signal):
        self.hp_buffer[self.hp_idx] = 2*self.hp_buffer[self.hp_idx-1] - (1/32.0)*self.ecg_buffer[self.ecg_idx] \
                                      + self.ecg_buffer[self.ecg_idx-16] - self.ecg_buffer[self.ecg_idx-17] \
                                      + (1/32.0)*self.ecg_buffer[self.ecg_idx-32]
        return self.hp_buffer[self.hp_idx]

    def differentiate(self, signal):
        return (1/8)*(2*self.ecg_buffer[self.ecg_idx]+self.ecg_buffer[self.ecg_idx-1]-self.ecg_buffer[self.ecg_idx-3]
                      + 2*self.ecg_buffer[self.ecg_idx-4])

    def squaring(self, signal):
        return (self.ecg_buffer[self.ecg_idx])**2


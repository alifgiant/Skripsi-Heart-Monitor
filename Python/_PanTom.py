import matplotlib.pyplot as plt
import numpy as np
from scipy.signal import lfilter, convolve

CONST_FILTER_WINDOW = 2400


class Detector(object):
    data_sources = []

    def __init__(self):
        pass

    def add_data(self, raw, visible_plot=False):
        if len(self.data_sources) == 0:
            self.data_sources.append([])
        container = self.data_sources[0]
        if len(container) < CONST_FILTER_WINDOW:
            container.append(raw)
        else:
            self.data_sources.append([raw])
            return self.__process(self.data_sources.pop(0), visible_plot)
        return ['filling']

    @staticmethod
    def __process(data_source, visible_plot=False):
        '''
        LOW PASS FILTERING
        '''
        b = [1, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, 0, 1]
        a = [1, -2, 1]
        lowed = lfilter(b,a, data_source)

        '''
        HIGH PASS FILTERING
        '''
        b = [-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, -32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]
        a = [1, -1]
        highed = lfilter(b,a, lowed)

        '''
        DERRIVIATE
        '''
        h = [x/8. for x in [-1, -2, 0, 2, 1]]
        derr = convolve(highed, h)[2:]  # cancel delay
        abss = [abs(x) for x in derr]
        max_abs = max(abss)
        derr = [x/max_abs for x in derr]

        '''
        SQUARING
        '''
        squared = [x**2 for x in derr]
        abss = [abs(x) for x in squared]
        max_abs = max(abss)
        squared = [x/max_abs for x in squared]

        '''
        MOVING WINDOW INTEGRATION
        '''
        h = np.ones(31)/31
        # Delay = 15
        x6 = convolve(squared, h)[15:]
        # x6 = [x+15 for x in x6]
        abss = [abs(x) for x in x6]
        max_abs = max(abss)
        x6 = [x/max_abs for x in x6]

        '''
        QRS EXTRACTION
        '''
        max_h = max(x6)
        thresh = np.mean(x6)
        poss_reg = [int(x > thresh*max_h) for x in x6]
        # print 'treae', len(poss_reg)


        def indices(datas, func):
            return [i for (i, val) in enumerate(datas) if func(val)]

        left = indices(np.diff([0] + poss_reg), lambda y: y == 1)
        right = indices(np.diff(poss_reg + [0]), lambda y: y == -1)
        # print 'left ', left
        # print 'right ', right

        left = [x-(6+16) for x in left]  # cancel delay because of LP and HP
        right = [x-(6+16) for x in right]  # cancel delay because of LP and HP

        Q_loc, R_loc, S_loc = [], [], []

        for i in xrange(len(left)):
            try:
                sliced_temp = derr[left[i]:right[i]]
                r_loc = sliced_temp.index(max(sliced_temp))
                r_loc = r_loc - 1 + left[i]  # add offset
                R_loc.append(r_loc)

                sliced_temp = derr[left[i]:r_loc]
                q_loc = sliced_temp.index(min(sliced_temp))
                q_loc = q_loc - 1 + left[i]  # add offset
                Q_loc.append(q_loc)

                sliced_temp = derr[left[i]:right[i]]
                s_loc = sliced_temp.index(min(sliced_temp))
                s_loc = s_loc - 1 + left[i]  # add offset
                S_loc.append(s_loc)
            except:
                # print i, left[i], right[i]
                pass  # look for next peak
                # break

        '''
        SHOWING DATA
        '''
        if visible_plot:
            fig, (ax_orig, ax_lowed, ax_highed, ax_derr, ax_square, ax_6, resss) = plt.subplots(7, 1, sharex=True)
            ax_orig.plot(data_source)
            ax_lowed.plot(lowed)
            ax_highed.plot(highed)
            ax_derr.plot(derr)
            ax_square.plot(squared)
            ax_6.plot(x6)
            resss.plot(derr)
            # print R_loc
            resss.plot([i for i in R_loc if i < len(derr)], [derr[i] for i in R_loc if i < len(derr)], 'r')
            # dataata = [data_source[i] for i in R_loc if i<len(data_source)]
            # print dataata
            # plt.plot([i for i in R_loc if i < len(data_source)], [data_source[i] for i in R_loc if i < len(data_source)], 'r')
            plt.tight_layout()
            plt.show()

        '''
        DECISION
        '''
        RR_interval = np.diff(R_loc)
        QRS_width = [S_loc[i]-Q_loc[i] for i in xrange(len(Q_loc))]

        # print 'len', len(RR_interval), len(QRS_width), len(R_loc), len(Q_loc), len(S_loc)

        bpm = [round(60*(x*5)/1000.) for x in RR_interval]
        # print bpm

        """Beat Classification"""
        Last4_Window = zip(range(0, len(RR_interval)-4), range(4, len(RR_interval)))
        PVC = [RR_interval[y-1] <= 0.8 * np.mean(RR_interval[x:y]) and QRS_width[y-1]*5 >= 120 for x, y in Last4_Window]
        print PVC
        PAC = [RR_interval[y-1] <= 0.8 * np.mean(RR_interval[x:y]) and QRS_width[y-1]*5 < 120 for x, y in Last4_Window]
        print PAC
        BUNDLE_BRANCH = [0.8*np.mean(RR_interval[x:y]) <= RR_interval[y-1] <= 1.2 * np.mean(RR_interval[x:y])
                         and QRS_width[y-1]*5 >= 120 for x, y in Last4_Window]
        # print BUNDLE_BRANCH

        """Cardiac Rhythms"""
        AtrialTachycardia = [PAC[x] and PAC[x-1] and PAC[x-2] for x in range(len(PAC))[2:]]
        # print AtrialTachycardia

        VentricularTachycardia = [PVC[x] and PVC[x-1] and PVC[x-2] for x in range(len(PVC))[2:]]
        # print VentricularTachycardia

        BundleBranchBlock = [BUNDLE_BRANCH[x] and BUNDLE_BRANCH[x-1]
                             and BUNDLE_BRANCH[x-2] for x in range(len(BUNDLE_BRANCH))[2:]]
        # print BundleBranchBlock

        return derr,  PVC, PAC, BUNDLE_BRANCH, AtrialTachycardia, VentricularTachycardia, BundleBranchBlock

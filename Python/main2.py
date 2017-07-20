from time import time
import json
import CsvLoader
import sys
import matplotlib.pyplot as plt

from filter import Filter
from detector import BeatDetector

# numbers = [228]  #
# numbers = [215]  #
# numbers = [203]  # 67
# numbers = [119]  #
# numbers = [113]  # 111
numbers = [100]  #

# numbers = [100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 118, 119, 121, 122, 123,
#            124, 200, 201, 202, 203, 205, 207, 208, 209, 210, 212, 213, 214, 215, 217, 219, 220, 221, 222, 223, 228, 230,
#            231, 232, 233, 234]

if __name__ == '__main__':
    for number in numbers:
        number = 'MIT_BIH/' + str(number)

        # beat = json.load(open(number + '/beat.json'))
        # beat = json.load(open(number + '/beat.json'))[:2935]  # 8 second
        # beat = json.load(open(number + '/beat.json'))[:7000]
        # beat = json.load(open(number + '/beat.json'))[10000:20000]
        beat = json.load(open(number + '/beat.json'))[20000:40000]

        # raw = CsvLoader.load_dummy(1)
        # raw = CsvLoader.load(number + '/record.csv')
        # raw = CsvLoader.load(number + '/record.csv')[:2935]  # 8 second
        # raw = CsvLoader.load(number + '/record.csv')[:7000]
        # raw = CsvLoader.load(number + '/record.csv')[10000:20000]
        raw = CsvLoader.load(number + '/record.csv')[20000:40000]

        start_filtering = time()

        # filtering
        low_high_filter = Filter('coef/coef-filter.json')
        filtered = []
        for x in raw:
            filtered.append(low_high_filter.execute(x))

        # derivative
        der_filter = Filter('coef/coef-derr.json')
        der = []
        for x in filtered:
            der.append(der_filter.execute(x))

        squared = []
        for x in der:
            squared.append(x**2)

        # MWI
        mwi_filter = Filter('coef/coef-mwi.json')
        mwi = []
        for x in squared:
            mwi.append(mwi_filter.execute(x))

        stop_filtering = time()
        # print('filtering duration', stop_filtering-start_filtering)

        # REMOVED DELAY
        # print('delay', low_high_filter.get_delay()+der_filter.get_delay()+mwi_filter.get_delay())
        removed = mwi[int(low_high_filter.get_delay()+der_filter.get_delay()+mwi_filter.get_delay()):]

        # detector = Detector(360, window_duration=5, val_by_mean=1.1, idx_by_r=0.5)
        detector = BeatDetector(360, window_duration=1.2, val_by_mean=1.1, idx_by_r=0.975)
        # detector = Detector(360, 0.1, 8, 1)
        peaks = []
        thrs = []
        # for x in mwi:
        for x in removed:
            # for x in mwi:
            res = detector.detect(x)
            # res = detector.detect(x)
            if res and len(res) > 1:
                peak, thr = res
                peaks += peak
                thrs += thr

        res = detector.execute_buffer(last=True)
        if res and len(res) > 1:
            peak, thr = res
            peaks += peak
            thrs += thr

        # print('removed', len(removed))
        # print('beat', len(beat))
        # print('peak', len(peaks))
        # print()

        stop_detection = time()
        print('detection duration', stop_detection - stop_filtering)

        real = beat.count(1)
        detect = peaks.count(0.5)

        # output = open(number + '/result.txt', 'w')
        # print('beat in real', real)
        # print('beat in detected', detect)
        # print('beat missed |x|', abs(real-detect))
        # print('accuracy', number, 100 * (detect / real), '%')
        # print('missed', 100 * (abs(detect-real) / real), '%')
        # print('finished:', number, '|', 'accuracy:', 100 * (detect / real), '%')
        print('finished:', number, '|', 'real:', real, 'detected:', detect, 'accuracy:', 100 * (detect / real), '%')
        # output.close()

        fig, (ax_raw, ax_peaks) = plt.subplots(2, 1)

        ax_raw.plot(raw)
        ax_raw.plot(beat)
        # ax_peaks.plot(raw)
        ax_peaks.plot(removed)
        ax_peaks.plot(peaks)
        # ax_peaks.plot(beat)
        ax_peaks.plot(thrs)
        # ax_peaks.plot(search_back_peak)

        plt.tight_layout()
        plt.show()

        # fig, (ax_raw, ax_filtered, ax_der, ax_squared, ax_mwi, ax_peaks) = plt.subplots(6, 1)
        #
        # ax_raw.plot(raw)
        # # ax_raw.plot(beat)
        # ax_filtered.plot(filtered)
        # ax_der.plot(der)
        # ax_squared.plot(squared)
        # ax_mwi.plot(mwi)
        # # ax_peaks.plot(raw)
        # ax_peaks.plot(removed)
        # ax_peaks.plot(beat)
        # ax_peaks.plot(peaks)
        # ax_peaks.plot(thrs)
        # # ax_peaks.plot(search_back_peak)
        #
        # plt.tight_layout()
        # plt.show()

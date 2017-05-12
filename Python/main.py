from filter import Filter
from detector import Detector
import json
import CsvLoader
import matplotlib.pyplot as plt

if __name__ == '__main__':
    beat = json.load(open('106/beat.json'))
    # beat = json.load(open('106/beat.json'))[:7000]
    # beat = json.load(open('106/beat.json'))[10000:20000]

    # raw = CsvLoader.load_dummy(200)
    raw = CsvLoader.load_single('106/106-MLII.csv')
    # raw = CsvLoader.load_single('106/106-MLII.csv')[:7000]
    # raw = CsvLoader.load_single('106/106-MLII.csv')[10000:20000]

    # filtering
    low_high_filter = Filter('coef-filter.json')
    filtered = []
    for x in raw:
        filtered.append(low_high_filter.execute(x))

    # derivative
    der_filter = Filter('coef-derr.json')
    der = []
    for x in filtered:
        der.append(der_filter.execute(x))

    squared = []
    for x in der:
        squared.append(x**2)

    # MWI
    mwi_filter = Filter('coef-mwi.json')
    mwi = []
    for x in squared:
        mwi.append(mwi_filter.execute(x))

    # REMOVED DELAY
    # print('delay', low_high_filter.get_delay()+der_filter.get_delay()+mwi_filter.get_delay())
    removed = mwi[int(low_high_filter.get_delay()+der_filter.get_delay()+mwi_filter.get_delay()):]

    detector = Detector(360, 0.2)
    peaks = []
    thrs = []
    for x in removed:
        # for x in mwi:
        res = detector.detect(x)
        # res = detector.detect_test(x)
        if res and len(res) > 1:
            peak, thr = res
            peaks += peak
            thrs += thr
    # search_back_peak = detector.search_back_peak
    # print('peak', len(peaks))
    # print('peak s', len(search_back_peak))

    # clean search back peak
    peaks += [0] * len(detector.search_back_sample)

    real = beat.count(1)
    detect = peaks.count(0.5)

    print('beat in real', real)
    print('beat in detect', detect)
    print('simpangan', 100 * (abs(detect-real) / real))
    print('accuracy', 100 * (detect / real), '%')

    # fig, (ax_raw, ax_filtered, ax_der, ax_squared, ax_mwi, ax_peaks) = plt.subplots(6, 1)
    #
    # ax_raw.plot(raw)
    # ax_raw.plot(beat)
    # ax_filtered.plot(filtered)
    # ax_der.plot(der)
    # ax_squared.plot(squared)
    # ax_mwi.plot(mwi)
    # # ax_peaks.plot(raw)
    # ax_peaks.plot(removed)
    # # ax_peaks.plot(beat)
    # ax_peaks.plot(peaks)
    # # ax_peaks.plot(search_back_peak)
    #
    # plt.tight_layout()
    # plt.show()

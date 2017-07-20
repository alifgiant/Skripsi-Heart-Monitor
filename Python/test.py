import os
from collections import Counter
import json
from detector import ArrhythmiaDetector

# all samples
numbers = [100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 112, 113, 114, 115, 116, 117, 118, 119, 121, 122,
           123, 124, 200, 201, 202, 203, 205, 207, 208, 209, 210, 212, 213, 214, 215, 217, 219, 220, 221, 222, 223,
           228, 230, 231, 232, 233, 234]

# good samples
# numbers = [100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 111, 114, 115, 117, 118, 119, 121, 122, 123, 124, 200,
#            201, 202, 205, 208, 209, 210, 212, 213, 214, 215, 217, 219, 220, 221, 222, 223, 230, 231, 233, 234]

# special handling samples
# numbers = [112, 113, 116, 203, 207, 228, 232]

# test
# numbers = [106]


def chunks(l, n):
    """Yield successive n-sized chunks from l."""
    for i in range(0, len(l), n):
        yield l[i:i + n]


def get_r_distance(peaks):
    last_idx = 0
    for idx, val in enumerate(peaks):
        if val == 1:
            yield (idx - last_idx)
            last_idx = idx

if __name__ == '__main__':
    for number in numbers:
        number = 'MIT_BIH/' + str(number)

        beat = json.load(open(number + '/beat.json'))
        # beat = json.load(open(number + '/beat.json'))[:2935]  # 8 second
        # beat = json.load(open(number + '/beat.json'))[:7000]
        # beat = json.load(open(number + '/beat.json'))[10000:20000]

        detector = ArrhythmiaDetector(360)
        r_distance = list(get_r_distance(beat))
        beat_classes = list()
        for chunk in chunks(r_distance, 10):  # let say in a window there will be 10 r-dis
            beat_classes.extend(detector.detect(chunk))

        # assume first and last is normal
        beat_classes = [1] + beat_classes + [1]

        count = Counter(beat_classes)
        print(number, count)

        # json.dump(beat_classes, open(number + '/beat_class/raw.json'))
        # json.dump(beat_classes, open(number + '/beat_class/detect_raw.json', 'w'))
        directory = os.path.dirname(number + '/beat_class/detect_processed.json')
        if not os.path.exists(directory):
            print('creating', directory)
            os.makedirs(directory)
        json.dump(beat_classes, open(number + '/beat_class/detect_processed.json', 'w'))

import sys
import wfdb
import json
from collections import Counter
import annotations

if __name__ == '__main__':
    number = sys.argv[1]

    recordname = number + '/' + number
    annotator = 'atr'
    sampfrom = 0
    sampto = None
    # record = wfdb.rdsamp(recordname, sampfrom=sampfrom, sampto=sampto)
    record = wfdb.srdsamp(recordname, sampfrom=sampfrom, sampto=sampto)
    annotation = wfdb.rdann(recordname, annotator, sampfrom=sampfrom, sampto=sampto)

    print(record)

    # wfdb.plotrec(record, annotation=annotation, title='Record 106 from MIT-BIH Arrhythmia Database',
    #              timeunits='seconds')

    beat_only = [(x, y) for x, y in zip(annotation.annsamp, annotation.anntype) if y in annotations.BEAT_ANNOTATION]
    beat_only_pos = [x for x, y in beat_only]
    beat_only_ann = [y for x, y in beat_only]

    # count = Counter(annotation.anntype)
    # count_beat_ann = Counter(beat_only_ann)

    # print(len(record[0]))
    # print(count_beat_ann)
    # print(beat_only_pos)
    # print(beat_only_ann)
    # beat_loc = [0] * len(record[0])
    # for x in beat_only_pos:
    #     beat_loc[x] = 1
    #
    # file_output_add = sys.argv[2]
    # json.dump(beat_loc, open(number+'/'+file_output_add+'.json', 'w'))

import CsvLoader
source = CsvLoader.load()[0:5000]

"""
Delay data: 12 second (2400 sample)
Sample Rate: 200/second (5ms)

Using PanTom
1. import PanTom.py
2. instantiate Detector object
3. call function add_data(raw)

return:
check the returned[0] if
['filling']:
    function return 'filling', detection step is skipped, algorithm still filling buffer
[Filtered Data, PVC, PAC, BUNDLE_BRANCH, AtrialTachycardia, VentricularTachycardia, BundleBranchBlock] :
    [0] is filtered data, send to chart via MQTT
    [1..6] is classification
"""

from PanTom import *
detector = Detector()
for raw in source:
    # detector.add_data(raw, True)  # view plot
    detector.add_data(raw)  # deployment

    # dat = detector.add_data(raw)  # print test, return
    # if dat[0] != 'filling':
    #     print dat[0], dat[1]

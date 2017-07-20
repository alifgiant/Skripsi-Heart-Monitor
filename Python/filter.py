import json


class Filter(object):
    def __init__(self, file_coef_address):
        self.coefficient = json.load(open(file_coef_address))
        self.samples = [0] * len(self.coefficient)

    def execute(self, data):
        self.samples.insert(0, data)  # insert data
        if len(self.samples) > len(self.coefficient):
            self.samples.pop(-1)  # remove last data, if more than 61

        result = 0
        for sample, coefficient in zip(self.samples, self.coefficient):
            result += sample * coefficient

        return result

    def get_delay(self):
        return (len(self.coefficient) - 1) / 2

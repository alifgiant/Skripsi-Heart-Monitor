import json


class Filter(object):
    def __init__(self, file_coef_address):
        self.coefficient = json.load(open(file_coef_address))
        self.len_coefficient = len(self.coefficient)
        self.samples = [0] * self.len_coefficient

    def execute(self, data):
        self.samples.insert(0, data)  # insert data
        if len(self.samples) > self.len_coefficient:
            self.samples.pop(-1)  # remove last data, if more than 61

        result = 0
        for i in range(self.len_coefficient):
            result += self.samples[i] * self.coefficient[i]

        return result

    def get_delay(self):
        return (self.len_coefficient - 1) / 2

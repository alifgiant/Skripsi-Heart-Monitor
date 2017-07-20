def load(address='106/106.csv', column=1):
    """
    Load csv that have multi column data
    :param address: csv location
    :param column: csv location
    :return: read data in a list
    """
    source = open(address)
    data_source = []
    i = 0
    data_count = 0
    for data in source:
        i += 1
        if i > 2:  # first column is table headerx
            data_count += 1
            y = data.split(',')[column]
            data_source.append(float(y))
    # print ('banyak sample', dataCount, 'Lama rekam', dataCount/200./60., 'menit')
    return data_source


def load_single(address='106/106-MLII.csv'):
    """
    Load csv that have single column data
    :param address: csv location
    :return: read data in a list
    """
    source = open(address)
    holder = []
    for data in source:
        holder.append(float(data))
    return holder


def load_dummy(length=100):
    """
    return dummy data consist only 1 with length of l(param)
    :param length:
    :return:
    """
    return [float(1)]*length


if __name__ == '__main__':
    # print(load())
    # print(load_single())
    print(load_dummy())

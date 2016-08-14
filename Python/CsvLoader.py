def load(address='../Data/116/samples.csv'):
    source = open(address)
    data_source = []
    i = 0
    dataCount = 0
    for data in source:
        i += 1
        if (i > 2):
            dataCount += 1
            y = data.split(',')[2]
            data_source.append(float(y))

    print 'banyak sample', dataCount, 'Lama rekam', dataCount/200./60., 'menit'
    return data_source

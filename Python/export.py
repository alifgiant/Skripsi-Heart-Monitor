import re
data = open('data.csv')

for i in data:
    columns = re.split(',', i)
    try:
        print float(columns[-1])
    except Exception as ex:
        pass

datas = open('arrhythmia.data', 'r')
length = 0
for i in datas:
	# print i	
	# data = [float(x) for x in i.split(',')]

	data = i.split(',')
	last = data[0:2], data[4], data[15:18], data[21], data[99:102], data[230:233]

	# print data, len(data)
	print last

	length += 1	
	pass
	# break

print length
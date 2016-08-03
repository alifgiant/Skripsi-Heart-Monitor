import urllib
import os
import urllib2

counter = 103
filetype = [".atr",".dat",".hea"]
testfile = urllib.URLopener()

for x in range(counter, 124)+range(200, 235):
	directory = str(x)
	try:
	    urllib2.urlopen("https://www.physionet.org/physiobank/database/mitdb/"+directory+".dat")
	except urllib2.HTTPError, e:
	    print e.code, 'here'
	    continue
	except urllib2.URLError, e:
	    print e.args, 'there'
	    continue
	
	if not os.path.exists(directory):
	    os.makedirs(directory)

	for tipe in filetype:
		testfile.retrieve("https://www.physionet.org/physiobank/database/mitdb/"+directory+tipe, directory+"/"+directory+tipe)	

	print directory, "done"
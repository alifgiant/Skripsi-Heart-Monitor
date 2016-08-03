var csv = require("fast-csv");

dataInterval = 0.00277778;
DataCollection = [];

onParseCompleteListener = function (){
    console.log("done");
    DataCollection.splice(0, 2);
    console.log(DataCollection[0]);
};

csv.fromPath("Data/106/samples.csv", {headers : ["sample", "MLII", "V1"]})
	.on("data", function(data){
        //console.log(data);
        DataCollection.push(data);
	})
	.on("end", onParseCompleteListener);


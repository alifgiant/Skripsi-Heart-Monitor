/**
 * Created by maakbar on 8/3/2016.
 * 116, 119, 221, and 228. data
 */
var Detection = require('./Detection.js');


var detector = new Detection();

/* 	will return [signal, volt, isQrs, bpm, predictions];
	predictions is prediction[6], [false, false, false, false, false, true];
	prediction[0] = isPAC(R_distance);
	prediction[1] = isAtrialTachycardia();
	prediction[2] = isBundleBranchBlock(R_distance);
	prediction[3] = isSuperVentricularTachycardia(bpm);
	prediction[4] = isBradycardia(bpm);
	prediction[5] = isTargetHeartRate(bpm, 22); */
var test = detector.addData(10);

console.log(test);

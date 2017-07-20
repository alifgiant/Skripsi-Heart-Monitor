#include "Reader.h"
#include "Arduino.h"

const int Reader::ANALOG_PIN = 0;

int Reader::read(){
  int raw = analogRead(Reader::ANALOG_PIN);
  int rawProcessed = process(raw);
  return rawProcessed;
}

int Reader::process(int read){
  // do something here later
  return read;
}
//
// double Reader::convertToVolt(int raw){
//   double result = (raw * 3.3)/1024;
// }

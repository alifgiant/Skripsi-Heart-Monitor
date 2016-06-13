#include <Ticker.h>

//  Variables
int pulsePin = 0;                 // Pulse Sensor purple wire connected to analog pin 0
int blinkPin = 13;                // pin to blink led at each beat

volatile int Signal;                // holds the incoming raw data

Ticker flipper;
Ticker sender;

void setup_ticker(){
  pinMode(blinkPin,OUTPUT);         // pin that will blink to your heartbeat!
  flipper.attach_ms(3, readPulse);
  //sender.attach(2, senderfunc);
}

void removeReader(){
  flipper.detach();
}

void readPulse(){  
  Signal = analogRead(pulsePin);              // read the Pulse Sensor  
  Serial.println(Signal);
  send_message(Signal);
}


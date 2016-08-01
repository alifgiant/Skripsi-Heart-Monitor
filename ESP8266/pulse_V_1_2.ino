/*
  FILENAME:   pulse.ino
  AUTHOR:     Orlando S. Hoilett
  DATE:       Sunday, September 28, 2014
  VERSION:    1.2

  
  AFFILIATIONS:
  
  Calvary Engineering Family Group, USA

  Vanderbilt University Department of Biomedical Engineering,
    Nashville, TN


  UPDATES:
  Version 1.0
  2014/10/05/1700>
              Re-formatted the code by creating functions for all
              the major operations. Removed the digital filter
              because all the filtering is done in hardware.
  Version 1.1
  2014/10/07:0435>
              Setting up code for the students to put in their
              data
  Version 1.2
  2014/10/07:0651>
              Added thresholding for pulse rate calculation. The
              calculation is determined by calculating the time
              between two peaks. This results in a good amount of
              variation from peak to peak.
              
              
  DESCRIPTION
  This code was written as an example for introduction to
  engineering students enrolled in the BME module of ES140 at
  Vanderbilt University.
  
  Uses an optical sensor (IR led, and photodiode) to detect pulse.
  The finger cuffs used are the ones available in the Vanderbilt
  University Instrumentation Lab. This version uses a simple
  transimpedance amplifier, high-pass filter, voltage divider to
  set-up a bias voltage and a simple gain stage for amplification.
  
  The codes reads the voltage from the circuit, which corresponds
  to the transmission of light through a person's finger. It then
  calculates the absorbance of the signal and outputs the data
  via serial communication. A corresponding LabVIEW VI reads the
  incoming serial data and plots it with time. The resulting
  waveform is the pulse waveform.
  
  The code also detects the time between pulses and uses the
  time between pulses to calculate pulse rate. This data is
  outputted via serial communication as well and is viewed in the
  corresponding LabVIEW VI.
  
  
  DISCLAIMER
  
  This code is in the public domain. Please feel free to modify,
  use, etc however you see fit. But, please give reference to
  original authors as a courtesy to Open Source developers.

*/


const int sensorPin = A0;
int sensorVal = 0;
unsigned long pulseCounter = 0;
unsigned long firstPulseStartTime = 0;
unsigned long secondPulseStartTime = 0;
unsigned long time_between_pulses = 0;
const unsigned long refractoryPeriod = 300;
const double minutes_in_milliseconds = 60000;


//When you have determined your threshold, declare your variable
//right below this line. This will change from person to person.
const double threshold = 0.33;


void setup()
{
  Serial.begin(115200);
  
  delay(2000); //a slight delay for system stabilization
}


void loop()
{
  //creates a timer variable to keep track of time
  unsigned long timer = millis();
  
  sensorVal = analogRead(sensorPin);
  
  double voltage = convertToVoltage(sensorVal);
  
  double absorbance = calculateAbsorbance(voltage);
  
  long time_between_pulses = detectThreshold(absorbance);
  
  int pulseRate = calculatePulseRate(time_between_pulses);
    
  displayPulseInLabVIEW(absorbance, pulseRate);
  
  //small delay to change our sampling rate
  //and stabilize our signal
  delay(25);  
}


//displayPulseInLabVIEW()
//Outputs the data via serial communication. LabVIEW reads the
//data coming in and plots the pulse waveform as well as the
//pulse rate
void displayPulseInLabVIEW(double absorbance, int pulseRate)
{
  //Serial.print allows us to output the data
  //via serial communication
  Serial.print(absorbance,5);
  Serial.print("\t");
  Serial.print(pulseRate);
  Serial.println();
}


//convertToVoltage()
//Does the calculation to convert the Arduino's analog-to-digital
//converter number to a voltage. This function then returns the
//value to the rest of the program.
double convertToVoltage(double ADC_Val)
{
  double volt = 0;
  
  //please put your calculation in between
  //the "=" and the ";"
  //NOTE: you will use the variable "ADC_Val" for your
  //calculation. It is case senstive and must be written with
  //the underscore as well. It does not include the quotation
  //marks (of course). It is the value currently outputted
  //by your analog to digital converter
  
  volt = 5*(ADC_Val/1023);
  
  return volt;
}


//calculateAbsorbance()
//Does the caluclation to convert the voltage to an absorbance.
//The value of the absorbance is then returned to the rest of
//the program
double calculateAbsorbance(double volt)
{
  double absorbance = 0;
  
  //please put your calculation in between
  //the parentheses of the log function
  //NOTE: you will use the variable "volt" in your calculation.
  //It is case sensitive and does not include the quotation
  //marks (of course). The variable "volt" is the current
  //voltage being read by the Arduino
  
  absorbance = log10(5/volt);
  
  return absorbance;  
}


//calculatePulseRate()
//This method calculates pulse rate by dividing 60 seconds by the
//time between subsequent pulses
double calculatePulseRate(long time_between_pulses)
{
  return minutes_in_milliseconds/time_between_pulses;
}


//detectThreshold()
//This method detects whether the signal has passed our
//threshold and determines the time between subsequent peaks
long detectThreshold(double absorbance)
{
  if (millis() - firstPulseStartTime >= refractoryPeriod
    && absorbance >= threshold)
  {
    if (pulseCounter == 0)
    {
      pulseCounter++;
      firstPulseStartTime = millis();
    }
    else if (pulseCounter == 1)
    {
      secondPulseStartTime = millis();
      time_between_pulses = secondPulseStartTime - firstPulseStartTime;
      firstPulseStartTime = secondPulseStartTime;
    }
  }

  return time_between_pulses;
}

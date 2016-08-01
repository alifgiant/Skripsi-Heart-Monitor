// Graphing sketch
 
 
 // This program takes ASCII-encoded strings
 // from the serial port at 9600 baud and graphs them. It expects values in the
 // range 0 to 1023, followed by a newline, or newline and carriage return
 
 // Created 20 Apr 2005
 // Updated 18 Jan 2008
 // by Tom Igoe
 // This example code is in the public domain.
 
import processing.serial.*;
 
Serial myPort;        // The serial port
int xPos = 1;         // horizontal position of the graph
 
void setup ()
{
  // set the window size:
  size(400, 300);        
  
  // List all the available serial ports
  println(Serial.list());
  // I know that the first port in the serial list on my mac
  // is always my  Arduino, so I open Serial.list()[0].
  // Open whatever port is the one you're using.
  myPort = new Serial(this, Serial.list()[1], 115200);
  // don't generate a serialEvent() unless you get a newline character:
  myPort.bufferUntil('\n');
  // set inital background:
  background(255);
}


void draw ()
{
  // everything happens in the serialEvent()
}

 
void serialEvent (Serial myPort)
{
  // get the ASCII string:
  String inString = myPort.readStringUntil('\n');
 
  if (inString != null)
  {
    // trim off any whitespace:
    inString = trim(inString);
    //println(inString);
    String[] pulseData = split(inString, "\t");
    // convert to an int and map to the screen height:
    float inByte = float(pulseData[0]);
    //println(inByte);
    
    /******/
    //You may need to change 0.25 and 0.45 to fit your
    //pulse waveform better
    inByte = map(inByte, 0.25, 0.45, 0, height);
    
    
    // draw the line:
    stroke(255,0,0);
    line(xPos, height, xPos, height - inByte);
    
    // at the edge of the screen, go back to the beginning:
    if (xPos >= width)
    {
      xPos = 0;
      background(255); 
    }
    else
    {
      // increment the horizontal position:
      xPos+=4;
    }
  }
}

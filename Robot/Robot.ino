#include "MeMegaPi.h"
#include <SoftwareSerial.h>
#include <Wire.h>
#define INCREMENT 25; //Changes the motor speed based on the line follower

//sensors and motors
MeBluetooth bluetooth(PORT_5);
MeUltrasonicSensor detect(PORT_4);
MeUltrasonicSensor altitude(PORT_3);
MeLineFollower lineFinder(PORT_2);
MeGyro gyro(PORT_1);
MeMegaPiDCMotor left(PORT1A);
MeMegaPiDCMotor right(PORT1B);

//motor speeds
uint8_t leftSpeed = 100, rightSpeed = 100;
//bluetooth data
String data = "";
void setup()
{
  Serial.begin(115200);
  gyro.begin();
}

//Data format: gyroX,gyroY,gyroZ,altitude
void loop()
{
  //update sensor values
  gyro.update();
  int sensorState = lineFinder.readSensors();
  switch(sensorState)
  {
    case S1_IN_S2_OUT: 
      rightSpeed += INCREMENT;
      break;
    case S1_OUT_S2_IN: 
      leftSpeed += INCREMENT;
      break;
    case S1_OUT_S2_OUT: 
      leftSpeed = 100;
      rightSpeed = 100;
      break;
    default: break;
  }
  left.run(leftSpeed);
  right.run(rightSpeed);
  data = String(gyro.getAngleX()) + "," + String(gyro.getAngleY()) + "," + String(gyro.getAngleZ()) + "," + String(altitude.distanceCm());
}

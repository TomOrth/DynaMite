//Need to download MakeBlock library into Arduino IDE
#include "MeMegaPi.h"
#include <Wire.h>
#include <SoftwareSerial.h>
 
#define MeMCore_H 
#define Rx 10 // DOUT to pin 10
#define Tx 11 // DIN to pin 11

MeMegaPiDCMotor MotorR(PORT1B); 
MeMegaPiDCMotor MotorL(PORT2B); 
MeLineFollower lineFollower(PORT_7); //line follower
MeUltrasonicSensor ultraSensor_X(PORT_8);
MeGyro gyro;

int moveSpeed = 95;
double lineDirIndex=10;
double dist_X;
double dist_Y;
uint8_t input;
int movement;
String data;
int backward;

void setup() {
  // put your setup code here, to run once:
  Serial3.begin(115200);  
  gyro.begin();
  delay(500);
  movement = 0;
  backward = 0;
}

void loop() {
      input = Serial3.read();
      
      if (input == 115) {
          movement = 1;
      }
      if (input == 101) {
          Stop();
          movement = 0;
      }
      if (movement > 0) {
        lineFollow();
        if (backward == 0) {
          gyro.update();
          dist_X = ultraSensor_X.distanceCm();
          data = "";
          Serial3.println(data + dist_X + " " + gyro.getAngleZ());
        }
      }
      
      delay(150);
}

void lineFollow(){
  int sensorStateCenter = lineFollower.readSensors();
  
  switch(sensorStateCenter)
  {
    case S1_IN_S2_IN: 
      Forward(); // Forward    
      lineDirIndex=10; 
      break;
    case S1_IN_S2_OUT:  
      Forward();
      if(lineDirIndex>1) {
        lineDirIndex--;
      }       
      break;
    case S1_OUT_S2_IN: 
      Forward();
      if(lineDirIndex<20) {
        lineDirIndex++;
      }     
      break;
    case S1_OUT_S2_OUT:  
      if(lineDirIndex==10){
        Backward(); // Backward
      }
      if(lineDirIndex==10.5){
      MotorL.run(-moveSpeed); // Turn right
      MotorR.run(moveSpeed/1.8);
      }
      if(lineDirIndex<10){
        TurnLeft(); // Turn left
      }
      if(lineDirIndex>10.5){
        TurnRight(); // Turn right
      }            
      break;
  }
}

void obstacleAvoidance() {
  MotorL.run(moveSpeed); // Turn left
  MotorR.run(moveSpeed);
  delay(450);
  lineDirIndex=10.5;
}

void Forward()
{
  backward = 0;
  MotorL.run(moveSpeed);
  MotorR.run(-moveSpeed);
}
void Backward()
{
  backward = 1;
  MotorL.run(-moveSpeed);
  MotorR.run(moveSpeed);
}

void TurnRight()
{
  backward = 0;
  MotorL.run(-moveSpeed/5); // Turn right
  MotorR.run(moveSpeed);
}
void TurnLeft()
{
  backward = 0;
  MotorL.run(-moveSpeed); // Turn left
  MotorR.run(moveSpeed/5);
}
void Stop()
{
  MotorL.run(0);
  MotorR.run(0);
}

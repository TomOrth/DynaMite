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

//Prob can get rid of
MeUltrasonicSensor ultraSensor_Y(PORT_5);

//Needs to be added to hardware and port needs to be changed
MeBluetooth bluetooth(PORT_5);

uint8_t moveSpeed = 150;
double lineDirIndex=10;
double dist_X;
double dist_Y;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600); 
  bluetooth.begin(9600);  
  Serial.println("setup");
  delay(500);
}

void loop() {
  // measurements from ultrasonic sensors
  dist_X = ultraSensor_X.distanceCm();
  //bluetooth.print(dist_X);
  Serial.println(ultraSensor_X.distanceCm());

  dist_Y = ultraSensor_Y.distanceCm();
  //bluetooth.print(dist_Y);
  
  /*if(dist<9 && dist>1) 
  {
    obstacleAvoidance();      
  } else {
    lineFollow();  
  } */
  Forward();
}

void lineFollow(){
  int sensorStateCenter = lineFollower.readSensors();

  if(moveSpeed>230) {
    moveSpeed=230;
  }
  switch(sensorStateCenter)
  {
    case S1_IN_S2_IN: 
      //Serial.println("Sensor 1 and 2 are inside of black line"); 
      Forward(); // Forward    
      lineDirIndex=10;
      break;
    case S1_IN_S2_OUT: 
      //Serial.println("Sensor 2 is outside of black line"); 
      Forward(); // Forward    
      if(lineDirIndex>1) {
        lineDirIndex--;
      }       
      break;
    case S1_OUT_S2_IN: 
      //Serial.println("Sensor 1 is outside of black line"); 
      Forward(); // Forward
      if(lineDirIndex<20) {
        lineDirIndex++;
      }     
      break;
    case S1_OUT_S2_OUT: 
      //Serial.println("Sensor 1 and 2 are outside of black line"); 
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
  MotorL.run(moveSpeed);
  MotorR.run(-moveSpeed);
  delay(1000);
}
void Backward()
{
  MotorL.run(-moveSpeed);
  MotorR.run(moveSpeed);
}
void TurnLeft()
{
  MotorL.run(-moveSpeed/10); // Turn left
  MotorR.run(moveSpeed);
}
void TurnRight()
{
  MotorL.run(-moveSpeed); // Turn right
  MotorR.run(moveSpeed/10);
}
void Stop()
{
  MotorL.run(0);
  MotorR.run(0);
}

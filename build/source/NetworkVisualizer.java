import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import net.sourceforge.jpcap.capture.*; 
import net.sourceforge.jpcap.net.*; 
import net.sourceforge.jpcap.simulator.*; 
import net.sourceforge.jpcap.util.*; 
import org.rsg.carnivore.*; 
import org.rsg.carnivore.cache.*; 
import org.rsg.carnivore.net.*; 
import peasy.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class NetworkVisualizer extends PApplet {










/************************************************************
      CARL MOORE - NETWORK VISUALIZER - AUG 8 2019
      --------------------------------------------
        run: sudo chmod 777 /dev/bpf* in terminal
************************************************************/

int posIdx = 0;
float size_x = 50;
float size_y = 50;
float size_z = 50;
float distMult = 10;
float scaleMult = 0.1f;
float sizeDecayRate = 0.993f;
boolean firstNode;

StringList active_ips;
ArrayList<NetworkNode> netNodes;
Table ip_locations;

PeasyCam cam;
CarnivoreP5 c;

public void setup(){
  //basic setup
  ip_locations = loadTable("/Users/carl/Documents/Processing/NetworkSniffer/dbip-city-lite-2019-08.csv");
  
  background(255);
  colorMode(HSB, 255);
  //variable initialization
  cam = new PeasyCam(this, 100);
  c = new CarnivoreP5(this);
  netNodes = new ArrayList<NetworkNode>();
  active_ips = new StringList();
  firstNode = true;
}

public void draw(){
  background(35);
  fill(255);
  rotateY(frameCount * 0.001f);
  rotateY(frameCount * 0.001f);
  lights();
  for(int i = 0; i < netNodes.size(); i++){
    netNodes.get(i).display();
  }
}

public void packetEvent(CarnivorePacket p){ // Called each time a new packet arrives
  String receiver_address = String.valueOf(p.receiverAddress);
  String sender_address = String.valueOf(p.senderAddress);
  NetworkNode nodeSend;
  NetworkNode nodeReceive;

  println("recieved ip address " + receiver_address);
  println("sending to " + sender_address);

  if(!active_ips.hasValue(sender_address)){
    PVector nodePos = new PVector(random(-size_x, size_x), random(-size_y, size_y), random(-size_z, size_z));
    boolean inLocalNet;
    if(receiver_address.contains("192.168")){
      inLocalNet = true;
      if(firstNode){ //first node flag sets node at center
        firstNode = false;
        nodePos = new PVector(0, 0, 0); //sets node to center
      }
    } else {
      inLocalNet = false;
    }
    nodeSend = new NetworkNode(sender_address, nodePos, distMult, sizeDecayRate, inLocalNet);
    active_ips.append(sender_address);
    netNodes.add(nodeSend);
    posIdx++;
  }

  if(!active_ips.hasValue(receiver_address)){
    PVector nodePos = new PVector(random(-size_x, size_x), random(-size_y, size_y), random(-size_z, size_z));
    boolean inLocalNet;
    if(receiver_address.contains("192.168")){
      inLocalNet = true;
      if(firstNode){ //first node flag sets node at center
        firstNode = false;
        nodePos = new PVector(0, 0, 0); //sets node to center
      }
    } else {
      inLocalNet = false;
    }
    nodeReceive = new NetworkNode(receiver_address, nodePos, distMult, sizeDecayRate, inLocalNet);
    active_ips.append(receiver_address);
    netNodes.add(nodeReceive);
    posIdx++;
  }

  String packet = p.toString();
  PVector sending_node = new PVector(); //coords for node sending to receiving

  for(int i = 0; i < netNodes.size(); i++){ //send draw function
    NetworkNode currentNode = netNodes.get(i);
    if(currentNode.ip_address == sender_address){
      netNodes.get(i).Send(pow(packet.length() * scaleMult, 0.5f)); //make sending values based on packet size and information
      sending_node = netNodes.get(i).position;
    }
  }
  for(int i = 0; i < netNodes.size(); i++){
    NetworkNode currentNode = netNodes.get(i);
    if(currentNode.ip_address == receiver_address){ //send receive function
      netNodes.get(i).Receive(pow(packet.length() * scaleMult, 0.5f), sending_node);
    }
  }
  println(netNodes.size() + " num nodes");
}
class NetworkNode{
  PVector position;
  PVector connectedNode;
  String ip_address;
  int strokeCol;
  int scaleInDur = 15;
  int scaleIn;
  int lineInDur = 50;
  int lineIn;
  float radius;
  float distMult = 1;
  float decayRate;
  float strokeDecayRate = 0.999f;
  int colReceive;
  int col;
  boolean inLocalNet;
  boolean scaleRadius = true;
  boolean connected = false;

  public NetworkNode (String tempIp_address, PVector tempPosition, float tempMult, float tempDecayRate, boolean tempInLocal){
    ip_address = tempIp_address;
    position = tempPosition;
    distMult = tempMult;
    decayRate = tempDecayRate;
    inLocalNet = tempInLocal;
    strokeCol = 255;
    colReceive = color(random(100,200), 50, 200);
  }

  public void Send(float rad){
    radius = rad;
    scaleRadius = true;
    col = color(0, 150, 200);
    scaleIn = 0;
    lineIn = 0;
  }

  public void Receive(float rad, PVector cNode){ //receiver sets line
    radius = rad;
    scaleRadius = true;
    col = colReceive;
    connectedNode = cNode;
    connected = true;
    strokeCol = 255;
    scaleIn = 0;
    lineIn = 0;
  }

  public void display(){
    //println("displaying " + ip_address + " at " + position.x + position.y + position.z);
    pushMatrix();
    stroke(255, strokeCol);

    if(connected){ //draw line from sender to receiver
      if(lineIn < lineInDur){
        lineIn++;
      }
      float lineLength = (float) lineIn / lineInDur;
      PVector thisPosition = position;
      PVector thisConnNode = connectedNode;

      float x2 = position.x;
      float y2 = position.y;
      float z2 = position.z;
      float x1 = connectedNode.x;
      float y1 = connectedNode.y;
      float z1 = connectedNode.z;

      line(x1, y1, z1,
       ( (1-lineLength)*x1 + lineLength*x2 ),
       ( (1-lineLength)*y1 + lineLength*y2 ),
       ( (1-lineLength)*z1 + lineLength*z2 ) );
    }
    if(strokeCol > 0){
      strokeCol *= strokeDecayRate;
    } else {
      strokeCol = 0;
    }
    noStroke();
    fill(col);
    translate(position.x, position.y, position.z);

    float thisRadius = radius;
    boolean scaledIn = true;

    if(scaleIn < scaleInDur){
      thisRadius *= (float) scaleIn / scaleInDur; //set size proportional to frames in
      scaledIn = false; //only scale down if fully scaled in
      scaleIn++;
    }

    sphere(thisRadius);
    if(inLocalNet == false && scaleRadius && scaledIn){
      radius *= decayRate;
      if(radius < 0.1f){
        radius = 0;
        scaleRadius = false;
      }
    }
    popMatrix();
  }
}
  public void settings() {  size(1280, 720, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "NetworkVisualizer" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

import net.sourceforge.jpcap.capture.*;
import net.sourceforge.jpcap.net.*;
import net.sourceforge.jpcap.simulator.*;
import net.sourceforge.jpcap.util.*;
import org.rsg.carnivore.*;
import org.rsg.carnivore.cache.*;
import org.rsg.carnivore.net.*;
import peasy.*;

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
float scaleMult = 0.1;
float sizeDecayRate = 0.993;
boolean firstNode;

StringList active_ips;
ArrayList<NetworkNode> netNodes;
Table ip_locations;

PeasyCam cam;
CarnivoreP5 c;

void setup(){
  //basic setup
  // ip_locations = loadTable("/Users/carl/Documents/Processing/NetworkSniffer/dbip-city-lite-2019-08.csv");
  size(1280, 720, P3D);
  background(255);
  colorMode(HSB, 255);
  //variable initialization
  cam = new PeasyCam(this, 100);
  c = new CarnivoreP5(this);
  netNodes = new ArrayList<NetworkNode>();
  active_ips = new StringList();
  firstNode = true;
}

void draw(){
  background(35);
  fill(255);
  rotateY(frameCount * 0.001);
  rotateY(frameCount * 0.001);
  lights();
  for(int i = 0; i < netNodes.size(); i++){
    netNodes.get(i).display();
  }
}

void packetEvent(CarnivorePacket p){ // Called each time a new packet arrives
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
      netNodes.get(i).Send(pow(packet.length() * scaleMult, 0.5)); //make sending values based on packet size and information
      sending_node = netNodes.get(i).position;
    }
  }
  for(int i = 0; i < netNodes.size(); i++){
    NetworkNode currentNode = netNodes.get(i);
    if(currentNode.ip_address == receiver_address){ //send receive function
      netNodes.get(i).Receive(pow(packet.length() * scaleMult, 0.5), sending_node);
    }
  }
  println(netNodes.size() + " num nodes");
}

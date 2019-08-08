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
  float strokeDecayRate = 0.999;
  color colReceive;
  color col;
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

  void Send(float rad){
    radius = rad;
    scaleRadius = true;
    col = color(0, 150, 200);
    scaleIn = 0;
    lineIn = 0;
  }

  void Receive(float rad, PVector cNode){ //receiver sets line
    radius = rad;
    scaleRadius = true;
    col = colReceive;
    connectedNode = cNode;
    connected = true;
    strokeCol = 255;
    scaleIn = 0;
    lineIn = 0;
  }

  void display(){
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
      if(radius < 0.1){
        radius = 0;
        scaleRadius = false;
      }
    }
    popMatrix();
  }
}

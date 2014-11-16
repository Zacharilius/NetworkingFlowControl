package goBackN;


import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.io.*;
import java.text.*;

public class Server {

  private static final int window = 64;
  private static final int timeout = 300;
  private static final int maxCharMessage = 508;
  private static final int modulo = 128;
  private static int errorPercent;
  private int packetsLost;
  private int packetsSent;
  private int base;
  private int nextSeqNum;
  private int realSeqNum;
  private int receiverPort;
  private int senderPort;
  private DatagramChannel dataChannel;	//DatagramChannel
  private DatagramSocket dataSocket;
  private SocketAddress clientAddress;  //DatagramChannel
  private DatagramChannel ackChannel;	//DatagramChannel
  private SocketAddress receiverAddress; //DatagramChannel
  private DatagramSocket ackSocket;
  private InetAddress IPAddress;
  private String fileName;
  private List<String> data;
  private Timer timer;
  
  public static void main(String args[]) throws Exception {
    // Parse command line arguments	
	  
//ADD THROW CLAUSE TO STOP IF USER DOESN'T INPUT INTS FOR PORTS
    String IP = args[0];
	int receiverPort = Integer.parseInt(args[1]);
	int senderPort = Integer.parseInt(args[2]);
    String fileName = args[3];	
	errorPercent = Integer.parseInt(args[4]);
	System.out.println("Start: " );
	new Server(IP, receiverPort, senderPort, fileName);
  }
  
  public Server(String rHost, int rPort, int sPort, String fName) throws Exception {

  	// Set data from command line args
	IPAddress = InetAddress.getByName(rHost);
	receiverPort = rPort;
	senderPort = sPort;
	fileName = fName;
	
	
	//CREATE DATAGRAMCHANNELS
	//Create Data DatagramChannel
	dataChannel = DatagramChannel.open();
	SocketAddress address = new InetSocketAddress(0);
    DatagramSocket dataSocket = dataChannel.socket();
    dataSocket.setSoTimeout(5000);
    dataSocket.bind(address);
    clientAddress = new InetSocketAddress(IPAddress, receiverPort);
	
	//Create Receiving DatagramChannel
    DatagramChannel ackChannel = DatagramChannel.open();
    receiverAddress = new InetSocketAddress(receiverPort);
    DatagramSocket ackSocket = ackChannel.socket();
    ackSocket.bind(receiverAddress);
/*		
	// Create Sockets
	try {
	  dataSocket = new DatagramSocket();
	  dataSocket.setSoTimeout(5000);
	} catch (NumberFormatException e) {
	  e.printStackTrace();
	  System.out.println("Error with dataport and number format");
	}  catch (IOException e) {
	  e.printStackTrace();
	  System.out.println("IO exception in data connection");
	}
	
	try {
	  ackSocket = new DatagramSocket(senderPort);
	  ackSocket.setSoTimeout(5000);
	} catch(NumberFormatException e) {
	  e.printStackTrace();
	  System.out.println("Error with ackport and number format");
	} catch (IOException e) {
	  e.printStackTrace();
	  System.out.println("IO exception in ack connection");
	}
*/
	// Initialize go-back-n variables
	base = 0;
	nextSeqNum = 0;
	timer = new Timer();
	
	// Create array containing data for each packet
	data = readFileData(fileName);
	send();
  }
  
  private void send() throws Exception {
	packetsLost = 0;
	packetsSent = 0;
	int maxPacket = data.size() - 1;
	int lastSeqNum = 0;
	int multiplier = 0;
	
	//Initialize sendChannel;
	
	//Initialize ackChannel;
	
	DatagramPacket dataPacket;
	DatagramPacket ackPacket;
	byte[] ackArray = new byte[512];
//	byte[] currentArray; //remove
	ByteBuffer currentBuffer;
	Packet currentData = null;
	Packet currentAck = null;
	
	Random r = new Random(System.currentTimeMillis());
	System.out.println("tada");
	while(realSeqNum <= maxPacket) {
	  
	  //Random integer to simulate error rate.
	 if(nextSeqNum < base + window && nextSeqNum <= maxPacket) {
		if(r.nextInt(101) <= errorPercent){
			System.out.println("sentWrongPacket: " + (nextSeqNum+1));
			currentData = Packet.createPacket(nextSeqNum+1, data.get(nextSeqNum));
			packetsLost++;
		}
		else{
			currentData = Packet.createPacket(nextSeqNum, data.get(nextSeqNum));
			System.out.println("nextSeqNum: " + nextSeqNum);
			packetsSent++;
		}
		//currentArray = currentData.getBytesFromPacket();
		currentBuffer = currentData.getBufferFromPacket();
		currentBuffer.flip();
//		dataPacket = new DatagramPacket(currentArray, currentArray.length, IPAddress, receiverPort); //Can remove
		dataPacket = new DatagramPacket(currentBuffer.array(), currentBuffer.array().length, IPAddress, receiverPort); //Added to handle array
		
		//Send over channel
		dataSocket.send(dataPacket);
		if(base == nextSeqNum) {
		  timer.schedule(new Timeout(), timeout);
		}
		nextSeqNum++;
	  } 
	 else {
		System.out.println("Checking for ACK: ");
		ackPacket = new DatagramPacket(ackArray, ackArray.length);
		ackSocket.receive(ackPacket);
//		currentAck = Packet.getPacketFromBytes(ackPacket.getData()); //Remove
		ByteBuffer buffer2 = ByteBuffer.wrap(ackPacket.getData());
		currentAck = Packet.getPacketFromBuffer(buffer2.array()); //Remove
		
		// Convert seqNum from modulo 32
		int newSeq = currentAck.getSequenceNumber();
		if(newSeq < lastSeqNum) {
		  multiplier++;
		}
		realSeqNum = newSeq + modulo*multiplier;
		lastSeqNum = newSeq;
		
		// update base and timer
		base = realSeqNum + 1;
		if(base == nextSeqNum) {
		  timer.cancel();
		} else {
		  timer.schedule(new Timeout(), timeout);
		}
		if(realSeqNum == maxPacket) break;
	  }
    }

	// Send last packet
	currentData = Packet.createLastPacket(nextSeqNum);
//	currentArray = currentData.getBytesFromPacket();
	currentBuffer = currentData.getBufferFromPacket();
	
//	dataPacket = new DatagramPacket(currentArray, currentArray.length, IPAddress, receiverPort);
	dataPacket = new DatagramPacket(currentBuffer.array(), currentBuffer.array().length, IPAddress, receiverPort); //Added to handle array
	dataSocket.send(dataPacket);
	
	// Wait to receive lastAck
	int lastAck;
	while(true) {
	    ackPacket = new DatagramPacket(ackArray, ackArray.length);
	    ackSocket.receive(ackPacket);
	    

	    
//		lastAckdEOT = Packet.getPacketFromBytes(ackPacket.getData()).getSequenceNumber();
	    ByteBuffer buffer3 = ByteBuffer.wrap(ackPacket.getData());
	    lastAck = Packet.getPacketFromBuffer(buffer3.array()).getSequenceNumber();
	    
		if(lastAck == ((maxPacket + 1)%modulo)) break;
	}

	// Close resources and exit
	dataSocket.close();
	ackSocket.close();
	timer.cancel();
	printStatistics();
  }
  private void printStatistics(){
	System.out.println("\n\nGoBack-N Statistics: ");
	System.out.println("\tError percent: " + errorPercent + "%");
	System.out.println("\tPackets Sent: " + (data.size() + packetsLost));
	System.out.println("\tPackets lost: " + packetsLost);
	DecimalFormat df = new DecimalFormat("#.#");
	System.out.println("\tActual error percent: " + df.format((100 * ((float)packetsLost)/((float)(data.size() + packetsLost)))) + "%");
	
  }
  private static List<String> readFileData(String fileName) throws Exception {
    FileReader fr = new FileReader(fileName);
    List<String> data = new ArrayList<String>();
    char[] buf = new char[maxCharMessage];
    int pos = 0;

	// Read file and store up to maxCharMessage chars to be sent
	try {
      while(true) {
        int read = fr.read(buf, pos, maxCharMessage - pos);
        if (read == -1) {
          if (pos > 0) {
            data.add(new String(buf, 0, pos));
		  }
          break;
        }
        pos += read;
        if (pos == maxCharMessage) {
          data.add(new String(buf));
          pos = 0;
        }
      }
	} catch (IOException e) {
	  e.printStackTrace();
	  System.out.println("IO exception reading " + fileName);
    } 
    return data;
  }
  
  class Timeout extends TimerTask {
    public void run() {
	  // Restart timer
      timer.schedule(new Timeout(), timeout);
	  
	  // Resend all packets in window
	  for(int i = base; i <= nextSeqNum - 1; i++) {
	    try {
	    	Packet packetObject = Packet.createPacket(i, data.get(i));
	    	ByteBuffer currentBuffer = packetObject.getBufferFromPacket(); //added
	    	DatagramPacket dataPacket = new DatagramPacket(currentBuffer.array(), currentBuffer.array().length, IPAddress, receiverPort); //Added to handle array

//	    	byte[] currentArray = packetObject.getBytesFromPacket();
//	    	DatagramPacket dataPacket = new DatagramPacket(currentArray, currentArray.length, IPAddress, receiverPort);
	    	dataSocket.send(dataPacket);
	    } catch (Exception e) {
		  // TODO
		}
	  }
    }
  }
}

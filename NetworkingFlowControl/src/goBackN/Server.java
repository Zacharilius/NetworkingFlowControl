/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package goBackN;

import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.text.*;

/**
 *
 * @author zacharilius
 */
public class Server {
    //final variables. 
    private static final int DATA_PORT = 1778;
    private static final int ACK_PORT = 1779;
    private static final String FILE_NAME = "COSC635_2148_P2_DataSent.txt";
    private static final int WINDOW_SIZE = 10;
    private static final int TIME_OUT = 300;
    private static final int MAX_MESSAGE_BYTES = 510;
    private static final int MAX_SEQ_MODULO = 128;
    
    //Setup global goBackN variables.
    private boolean notPacketError;
    private static int errorPercent;
    private int packetsSentError;
    private int packetsSentWindow;
    private int base;
    private int nextSeqNum;
    private int realSeqNum;
    private DatagramSocket dataSocket;
    private DatagramSocket ackSocket;
    private InetAddress IPAddress;
    private List<String> data;
    private Timer timer;
  
    public static void main(String args[]) throws Exception {
        System.out.println("Server starting: ");
        String IP = args[0];
        String FILE_NAME = "COSC635_2148_P2_DataSent.txt";//args[3];	
        errorPercent = Integer.parseInt(args[1]);

        new Server(IP);
    }
  
  public Server(String IP) throws Exception {
        // Set data from command line args
        IPAddress = InetAddress.getByName(IP);

        // Create Sockets	
        try {
          dataSocket = new DatagramSocket();
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println("IO exception in data connection");
        }

        try {
          ackSocket = new DatagramSocket(ACK_PORT);
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println("IO exception in ack connection");
        }

        // Initialize go-back-n variables
        base = 0;
        nextSeqNum = 0;
        timer = new Timer();

        // Create array containing data for each packet
        data = getFileInBytes();
        send();
  }
  
    private void send() throws Exception {
        notPacketError = true;
	packetsSentError = 0;
        packetsSentWindow = 0;
	int maxPacket = data.size() - 2;
	int lastSeqNum = 0;
	int multiplier = 0;                
	DatagramPacket dataPacket;
	DatagramPacket ackPacket;
	byte[] ackArray = new byte[512];
	byte[] currentArray;
	Packet currentData = null;
	Packet currentAck = null;
	
        
	Random r = new Random(System.currentTimeMillis());
	while(realSeqNum <= maxPacket) {
	  
	 //Random integer to simulate error rate.
	 if(nextSeqNum < base + WINDOW_SIZE && nextSeqNum <= maxPacket) {
            if((r.nextInt(100) < errorPercent) && notPacketError) {
                currentData = Packet.createPacket(nextSeqNum+1, data.get(nextSeqNum).getBytes());
                packetsSentError++;
                packetsSentWindow += (base + WINDOW_SIZE) - nextSeqNum;
                notPacketError = false;
                System.out.println("Sending packet out of order#: " + nextSeqNum);
            }
            else{
                    currentData = Packet.createPacket(nextSeqNum, data.get(nextSeqNum).getBytes());
                    System.out.println("Sending packet#: " + nextSeqNum);
            }
            currentArray = currentData.getBytesFromPacket();
            dataPacket = new DatagramPacket(currentArray, currentArray.length, IPAddress, DATA_PORT);
            dataSocket.send(dataPacket);
            if(base == nextSeqNum) {
              timer.schedule(new Timeout(), TIME_OUT);
            }
            nextSeqNum++;
	  } 
	 else {
            notPacketError = true;
            ackPacket = new DatagramPacket(ackArray, ackArray.length);
            ackSocket.receive(ackPacket);
            currentAck = Packet.getPacketFromBytes(ackPacket.getData());
            System.out.println("Received ACK: " + currentAck.getSequenceNumber());

            // Convert seqNum from MAX_SEQ_MODULO 
            int newSeq = currentAck.getSequenceNumber();
            if(newSeq < lastSeqNum) {
              multiplier++;
            }
            realSeqNum = newSeq + MAX_SEQ_MODULO*multiplier;
            lastSeqNum = newSeq;

            // update base and timer
            base = realSeqNum + 1;
            if(base == nextSeqNum) {
              timer.cancel();
            } else {
              timer.schedule(new Timeout(), TIME_OUT);
            }
            if(realSeqNum == maxPacket){
                break;
            }
	  }
    }

	// Send last packet
        System.out.println("Sending last packet");
	currentData = Packet.createLastPacket(nextSeqNum, data.get(nextSeqNum).getBytes());
	currentArray = currentData.getBytesFromPacket();
	dataPacket = new DatagramPacket(currentArray, currentArray.length, IPAddress, DATA_PORT);
        //System.out.println("Last: \n" + new String(dataPacket.getData()));
	dataSocket.send(dataPacket);
	
	// Wait to receive EOT Ack
	int lastAckPacket;
	while(true) {
	    ackPacket = new DatagramPacket(ackArray, ackArray.length);
	    ackSocket.receive(ackPacket);
		lastAckPacket = Packet.getPacketFromBytes(ackPacket.getData()).getSequenceNumber();
		if(lastAckPacket == ((maxPacket + 1)%MAX_SEQ_MODULO)) {
                    System.out.println("Received last ACK");
                    break;
                }
	}

	// Close resources and exit
	dataSocket.close();
	ackSocket.close();
	timer.cancel();
	printStatistics();
  }
  private void printStatistics(){
	System.out.println("\n\n\nGoBack-N Statistics: ");
	System.out.println("\tError percent: " + errorPercent + "%");
	System.out.println("\tPackets required: " + data.size());
	System.out.println("\tPackets lost: " + packetsSentError);
        System.out.println("\tPackets discared by receiver: " + packetsSentWindow);
	DecimalFormat df = new DecimalFormat("#.#");
	System.out.println("\tActual error percent: " + df.format((100 * ((double)packetsSentWindow)/((double)data.size()))) + "%");
        System.out.println("\tError percent without thrown out packets: " + df.format((100 * ((double)packetsSentError)/((double)data.size()))) + "%");
  }

  private static List<String> getFileInBytes() throws Exception {
        FileInputStream f = new FileInputStream(FILE_NAME);
        
        List<String> packets = new ArrayList<String>();
        byte[] buffer = new byte[MAX_MESSAGE_BYTES];
        int remaining = buffer.length;                      
        int content;
        
        while((content = f.read(buffer, buffer.length - remaining, remaining)) != -1){
            packets.add(new String(buffer));
            //System.out.print(new String(buffer));
            remaining = buffer.length;
            //System.out.println((remaining - (buffer.length - remaining)));
            //buffer = new byte[remaining - (buffer.length - remaining)];
            Arrays.fill(buffer,(byte)0);
        }
        f.close();
      
      return packets;
  }
  class Timeout extends TimerTask {
    public void run() {
	  // Restart timer
      timer.schedule(new Timeout(), TIME_OUT);
	  Random r = new Random(System.currentTimeMillis());
	  // Resend all packets in WINDOW_SIZE
	  for(int i = base; i <= nextSeqNum - 1; i++) {
	    try {
                Packet packetObject;
                if((r.nextInt(101) <= errorPercent) && notPacketError){
                    packetObject = Packet.createPacket(i+1, data.get(i).getBytes());
                    packetsSentError++;
                    packetsSentWindow += (base + WINDOW_SIZE) - i;
                    notPacketError = false;
           
                }
                else{
                    packetObject = Packet.createPacket(i, data.get(i).getBytes());
                    System.out.println("Sending packet#: " + nextSeqNum);
                }        
                byte[] currentArray = packetObject.getBytesFromPacket();
                DatagramPacket dataPacket = new DatagramPacket(currentArray, currentArray.length, IPAddress, DATA_PORT);
                dataSocket.send(dataPacket);                
                
//                Packet packetObject = Packet.createPacket(i, data.get(i));
//                byte[] currentArray = packetObject.getBytesFromPacket();
//                System.out.println("Timeout resending: " + (int)(currentArray[1]));
//                DatagramPacket dataPacket = new DatagramPacket(currentArray, currentArray.length, IPAddress, DATA_PORT);
//                dataSocket.send(dataPacket);
	    } catch (Exception e) {
		System.out.println("Error: Timer error");
            }
	  }
    }
  }
}

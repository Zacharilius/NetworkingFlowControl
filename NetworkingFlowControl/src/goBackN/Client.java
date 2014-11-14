package goBackN;

import java.net.* ;
import java.lang.* ;
import java.io.*;
import java.nio.* ;

public class Client{

	private static String hostname;
	private static String ackport;
	private static String dataport;
	private static String filename;
	private static String logfile;
	private static int seqnum;
	private static int window;
	private static DatagramSocket acksocket;
	private static DatagramSocket datasocket;
	private static BufferedWriter fileout;
	private static BufferedWriter logout;
	
 	public static void main(String[] args) throws Exception {
		//Assign variable names and set defaults
		hostname = args[0];
		ackport = args[1];
		dataport = args[2];
		filename = args[3];
		logfile = "arrival.log";
		seqnum = 0;	
		window = 64;
		
		//Create file to be written to
		try {
			fileout = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Exception found with creating Filename file \n");
		};
		
		//Create log files 
		try {
			logout = new BufferedWriter(new FileWriter(logfile));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Exception found with creating log file \n");
		};


		//Create Sockets to receive and send packets
		try {
			datasocket = new DatagramSocket(Integer.parseInt(dataport));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Error with dataport and number format");
		}  catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO exception in data aconnection");
		}
		
		try {
			acksocket = new DatagramSocket();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Error with ackport and number format");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO exception in ack aconnection");
		}

		byte[] currentarray;
		DatagramPacket datapacket;
		currentarray = new byte[512];
		Packet currentpacket = null;
		Packet ackpacket = null;
		byte[] ackarray = null;
		byte[] stringarray;
		InetAddress address = InetAddress.getByName(hostname);
			
		//inital loop
		datapacket = new DatagramPacket(currentarray,currentarray.length);
		datasocket.receive(datapacket);
		currentpacket= Packet.getPacketFromBytes(datapacket.getData());
		
		//Loops until receive last packet value where == 2
		while(currentpacket.getPacketType() != 2	) {
			if(seqnum == currentpacket.getSequenceNumber() ){
				stringarray = currentpacket.getMessage();
				fileout.write(new String(stringarray));
				logout.write("Sequence number recieved:" + currentpacket.getSequenceNumber() +"\n");
				System.out.println("Sequence number recieved:" + currentpacket.getSequenceNumber() +"\n");
				ackpacket = Packet.createACK(seqnum);
				seqnum++;
				ackarray = ackpacket.getBytesFromPacket();
				acksocket.send(new DatagramPacket(ackarray,ackarray.length,address,Integer.parseInt(ackport)));				
			}
			
			else{ 
				logout.write("Repeated:" + currentpacket.getSequenceNumber() +"\n");
				System.out.println("Repeated:" + currentpacket.getSequenceNumber() +"\n");
				if (ackpacket !=null){/*resend*/acksocket.send(new DatagramPacket(ackarray,ackarray.length,address,Integer.parseInt(ackport)));}	
			}	
			datapacket = new DatagramPacket(currentarray,currentarray.length);
			datasocket.receive(datapacket);
			currentpacket= Packet.getPacketFromBytes(datapacket.getData());	
		}
		//The last packet has arrived then Save file and send ack
		fileout.close();
		logout.write("Last packet received");
		System.out.println("Last packet received");
		logout.close();
		ackpacket = Packet.createLastPacket(seqnum);
		acksocket.send(new DatagramPacket(ackpacket.getBytesFromPacket(),ackpacket.getBytesFromPacket().length,address,Integer.parseInt(ackport)));
		acksocket.close();
		datasocket.close();
    }
}

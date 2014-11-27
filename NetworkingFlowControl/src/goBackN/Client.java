/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package goBackN;


import java.net.* ;
import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client class that receives packets from 
 * @author zacharilius
 */
public class Client{
    private static final int ACK_PORT = 1779;
    private static final int DATA_PORT = 1778;
    private static final String FILE_NAME = "COSC635_2148_P2_DataRecieved.txt";
    private static final int MAX_SEQ_MODULO = 128;
    
    private static InetAddress serverIP;
    private static String logFile;
    private static int sequenceNum;
    private static DatagramSocket ackSocket;
    private static DatagramSocket dataSocket;
    private static BufferedWriter fileOut;
	
    public static void main(String[] args) {
        System.out.println("Client starting: ");
        //
        String IP = args[0];
        sequenceNum = 0;

        //Get IP Address in InetAddress format.
         try {
            serverIP = InetAddress.getByName(IP);
         } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: Creating InetAddress");
        }

        //Create file to be written to
        try {
            fileOut = new BufferedWriter(new FileWriter(FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Creation when creating ackSocket ");
        };

        //Create Sockets to receive and send packets
        try {
            dataSocket = new DatagramSocket(DATA_PORT);
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Creating dataSocket");
        }

        try {
            ackSocket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Creating ackSocket");
        }

        try {
            receive();
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: When calling receive()");
        }
    }
    public static void receive() throws Exception{
        DatagramPacket dataPacket;
        Packet currPacket = null;
        Packet ackPacket = null;
        byte[] receiverArray = new byte[512];
        byte[] ackArray = null;
        byte[] stringArray = null;
        

        //Get first packet
        dataPacket = new DatagramPacket(receiverArray,receiverArray.length);
        System.out.println("Waiting to receive packets..");
        dataSocket.receive(dataPacket);
        currPacket= Packet.getPacketFromBytes(dataPacket.getData());
        //Loop until receive last packet value where == 2
        while(currPacket.getPacketType() != 2) {
            if((sequenceNum % MAX_SEQ_MODULO) == currPacket.getSequenceNumber()){
                stringArray = currPacket.getMessage();
                System.out.println("stringArray.length: " + stringArray.length);
                System.out.println(currPacket.getSequenceNumber() + ": " + new String(stringArray));
                fileOut.write(new String(stringArray));
                ackPacket = Packet.createACK(sequenceNum++);
                ackArray = ackPacket.getBytesFromPacket();
                System.out.println("Sending ACK:" + ackPacket.getSequenceNumber() +"\n");
                ackSocket.send(new DatagramPacket(ackArray,ackArray.length,serverIP,ACK_PORT));				
            }

            else{ 
                System.out.println("Repeated or waiting:" + currPacket.getSequenceNumber() +"\n");
                if (ackPacket !=null){
                    ackSocket.send(new DatagramPacket(ackArray,ackArray.length,serverIP,ACK_PORT));
                }	
            }
            //Arrays.fill(receiverArray,(byte)0);
            dataPacket = new DatagramPacket(receiverArray,receiverArray.length);
            dataSocket.receive(dataPacket);
            currPacket = Packet.getPacketFromBytes(dataPacket.getData());
            System.out.println(currPacket.getSequenceNumber() + ": " + new String(currPacket.getBytesFromPacket()));
            System.out.println(currPacket.getSequenceNumber() + ": " + currPacket.getBytesFromPacket().length);

        }
        stringArray = currPacket.getMessage();
        System.out.println(new String(stringArray));
        fileOut.write(new String(stringArray));
        //The last packet has arrived then Save file and send ack
        fileOut.close();
        System.out.println("Last packet received");
        ackPacket = Packet.createLastPacket(sequenceNum, new byte[0]);
        ackSocket.send(new DatagramPacket(ackPacket.getBytesFromPacket(),ackPacket.getBytesFromPacket().length,serverIP,ACK_PORT));
        fileOut.close();
        ackSocket.close();
        dataSocket.close();
    }
}

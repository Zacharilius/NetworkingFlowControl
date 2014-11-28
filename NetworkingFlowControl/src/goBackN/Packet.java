/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package goBackN;

import java.nio.ByteBuffer;
/**
 *
 * @author zacharilius
 */
public class Packet {
	
    private final int MAX_MESSAGE_LENGTH = 510;
    private final int MAX_SEQ_MODULO = 128;
    
    private  int packetType;
    private  int sequenceNumber;
    private  byte[] message;
	
	
	
    public static Packet createACK(int sequenceNumber) throws Exception {
        return new Packet(0, sequenceNumber, new byte[0]);
    }

    public static Packet createPacket(int sequenceNumber, byte[] message) throws Exception {
        return new Packet(1, sequenceNumber, message);
    }

    public static Packet createLastPacket(int sequenceNumber, byte[] message) throws Exception {
        return new Packet(2, sequenceNumber, message);
    }	

    private Packet(int packetType, int sequenceNumber, byte[] message) throws Exception {
        // Tests if inputed string is too large"
        if (message.length > MAX_MESSAGE_LENGTH){
                throw new Exception("Message > " + MAX_MESSAGE_LENGTH);
        }
        this.packetType = packetType;
        this.sequenceNumber = sequenceNumber % MAX_SEQ_MODULO;
        this.message = message;
    }
    public byte[] getBytesFromPacket() {
        ByteBuffer buffer = ByteBuffer.allocate(message.length + 2);
        buffer.put((byte)packetType);
        buffer.put((byte)sequenceNumber);
//        System.out.println("len: " + message.getBytes().length);
        buffer.put(message);

        //buffer.put(message.getBytes(),0,message.length());
     /*   
        buffer.flip();
        System.out.println("gBFP: " + message + "ZZZ");
        buffer.get();
        buffer.get();
        byte test[] = new byte[message.length()];
        buffer.get(test, 0, (message.length() - 2));
        System.out.println("Yep: " + new String(test));
        buffer.flip();
        
        buffer = ByteBuffer.allocate(getLength()+ 2);
        buffer.put((byte)packetType);
        buffer.put((byte)sequenceNumber);
        buffer.put(message.getBytes(),0,message.length());
*/
        return buffer.array();
    }

    public static Packet getPacketFromBytes(byte[] UDPmessage) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPmessage);
        int packetType = buffer.get();
        int sequenceNumber = buffer.get();
        byte message[] = new byte[(UDPmessage.length - 2)];
        buffer.get(message, 0, (UDPmessage.length - 2));
        return new Packet(packetType, sequenceNumber, message);
    }

    public int getPacketType() {
        return packetType;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getMessage() {
        return message;
    }
}

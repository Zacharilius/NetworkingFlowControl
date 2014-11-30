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
public class Packet2 {
	
    private final int MAX_MESSAGE_LENGTH = 510;
    private final int MAX_SEQ_MODULO = 128;
    
    private  int packetType;
    private  int sequenceNumber;
    private  byte[] message;
	
	
	
    public static Packet2 createACK(int sequenceNumber) throws Exception {
        return new Packet2(0, sequenceNumber, new byte[0]);
    }

    public static Packet2 createPacket(int sequenceNumber, byte[] message) throws Exception {
        return new Packet2(1, sequenceNumber, message);
    }

    public static Packet2 createLastPacket(int sequenceNumber, byte[] message) throws Exception {
        return new Packet2(2, sequenceNumber, message);
    }	

    private Packet2(int packetType, int sequenceNumber, byte[] message) throws Exception {
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

    public static Packet2 getPacketFromBytes(byte[] UDPmessage) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPmessage);
        int packetType = buffer.get();
        int sequenceNumber = buffer.get();
        byte message[] = new byte[(UDPmessage.length - 2)];
        buffer.get(message, 0, (UDPmessage.length - 2));
        return new Packet2(packetType, sequenceNumber, message);
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

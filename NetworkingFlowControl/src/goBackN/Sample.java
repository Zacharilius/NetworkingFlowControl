package goBackN;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Date;

public class Sample {
	
	public static void main(String[] args) throws IOException {
		ByteBuffer buffer = byteBufferTest();
		buffer.flip();
		byteBufferTest(buffer);
		System.out.println("Finish A");
		
		DatagramChannelTester();
		System.out.println("Finish B");
		
	}
	
	public static void DatagramChannelTester(){
		DatagramChannel channel;
		try {
			channel = DatagramChannel.open();
			DatagramSocket socket = channel.socket();
			SocketAddress address = new InetSocketAddress(9999);
		    socket.bind(address);
		    
		    
		    
		    ByteBuffer buffer = ByteBuffer.allocateDirect(512);
		    SocketAddress client = channel.receive(buffer);
		    buffer.flip();
		    channel.send(buffer, client);
		    buffer.clear();
		    
		}
	    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ByteBuffer byteBufferTest(){
		String message = "Yep yep yep...this is my message";
		System.out.println(message.length());
		int a = 4;
		int b = 3;
		ByteBuffer buffer = ByteBuffer.allocate(message.length() + 4);
		buffer.putShort((short)a);
		buffer.putShort((short)b);
		buffer.put(message.getBytes());
		System.out.println(buffer.toString());

		return buffer;
	}
	public static void byteBufferTest(ByteBuffer buffer){
		System.out.println(buffer.capacity());
		int a = buffer.getShort();
		int b = buffer.getShort();
		System.out.println(buffer.capacity());
		byte[] byteArray = new byte[buffer.capacity() - 4];
		buffer.get(byteArray);
		String s = new String(byteArray);
		System.out.println("a: " + a + " b: " + b + " message: " + s );
	}
}

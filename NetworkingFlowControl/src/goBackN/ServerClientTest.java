package goBackN;

import java.util.concurrent.TimeUnit;

public class ServerClientTest {

	public static void main(String[] args) throws InterruptedException {
		
		try {
			Client.main(new String[] {"localhost", "9888", "9889", "out.txt"});
		} catch (Exception e) {
			e.printStackTrace();
		}
		TimeUnit.MILLISECONDS.sleep(20);
		
		try {
			Server.main(new String[] {"localhost", "9889", "9888", "COSC635_2148_P2_DataSent.txt", "25"});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.print("Finished");

	}

}

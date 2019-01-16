import java.util.*;
import java.net.*;
import java.io.*;

public class UDPServer{
	public static void main(String[] args) {
		System.out.println();
		Scanner sc=new Scanner(System.in);
		String inMsg="",outMsg="";
		byte[] inData,outData;
		InetAddress clientAddress;
		int clientPort;
		try{
			DatagramSocket ds=new DatagramSocket(8581);
			try{
				//ds=new DatagramSocket(8515);
				System.out.println("Server IP : "+InetAddress.getLocalHost().getHostAddress());
				System.out.println("Server Port : "+ds.getLocalPort());
				DatagramPacket dsp,drp;
				do{
					inData=new byte[1024];
					drp=new DatagramPacket(inData,inData.length);
					ds.receive(drp);
					clientAddress=drp.getAddress();
					clientPort=drp.getPort();
					inMsg=new String(drp.getData(),0,drp.getLength());
					System.out.println("Client msg : "+inMsg);
					if (inMsg.equalsIgnoreCase("bye")) {
						break;
					}

					System.out.print("Enter something :");
					outMsg=sc.nextLine();
					outData=new byte[1024];
					outData=outMsg.getBytes();
					dsp=new DatagramPacket(outData,outData.length,clientAddress,clientPort);
					ds.send(dsp);
				}while (!outMsg.equalsIgnoreCase("bye"));
				ds.close();
			}
			catch (Exception e) {
				System.out.println(e.toString());
				System.out.println();
			}
			finally{
				ds.close();
			}
		}
		catch(Exception e){}
	}
}

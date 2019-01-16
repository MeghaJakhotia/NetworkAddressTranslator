import java.util.*;
import java.net.*;
import java.io.*;

public class UDPClient{
	
	static String encode(String[] array){
		StringBuilder output=new StringBuilder();
		for (String i:array ) {
			output.append((i.length()+1)+"-"+i);
		}
		return output.toString();
	}

	static String[] decode(String encoded){
		StringBuilder en=new StringBuilder(encoded);
		ArrayList<String> output = new ArrayList<String>();
		int len,i;
		for (i=0,len=0;en.length()!=0;++i,len=0) {
			while(Character.isDigit(en.charAt(0))){
				len=len*10+Integer.parseInt(en.charAt(0)+"");
				en.deleteCharAt(0);
			}
			output.add(en.substring(1,len));//consider region after -
			en.delete(0,len);
		}
		return output.toArray(new String[0]);
	}

	public static void main(String[] args) {
		System.out.println();
		String inMsg = "", outMsg = "";
		int format = 3, serverPort, vpnPublicPort = 3333,vpnPrivatePort;
		boolean vpnConnect=false;
		String vpnPublicIP="",serverIP,vpnPrivateIP;
		String secretAuthenticationKey = "AbraKaDabra",validAuthenticationRespnse="readyToUse";
		String[] authentication = new String[format], vpnDetails = new String[format], vpnResponse, server;
		Scanner sc = new Scanner(System.in);
		byte[] inData, outData;
		try{
			vpnPublicIP=InetAddress.getLocalHost().getHostAddress();//you can also make it as user input
			DatagramSocket ds = new DatagramSocket();
			DatagramPacket dsp, drp;
			authentication[0] = vpnPublicIP;//public ip address of vpn -- dummy
			authentication[1] = ""+vpnPublicPort;//public port of vpn
			authentication[2] = secretAuthenticationKey;//key is required to establish a connection
			outMsg = encode(authentication);
			outData = outMsg.getBytes();
			dsp = new DatagramPacket(outData, outData.length, InetAddress.getByName(vpnPublicIP), vpnPublicPort);
			ds.send(dsp);
			System.out.println("asking for authentication from vpn "+secretAuthenticationKey);

			inData = new byte[1024];
			drp = new DatagramPacket(inData, inData.length);
			ds.receive(drp);
			inMsg = new String(drp.getData(), 0, drp.getLength());
			vpnDetails = decode(inMsg);
			if(vpnDetails[2].equals(validAuthenticationRespnse)){
				vpnConnect = true;
				vpnPrivateIP=InetAddress.getLocalHost().getHostAddress();//this can be different
				vpnPrivatePort=Integer.parseInt(vpnDetails[1]);
				System.out.println("Connection is established with vpn");
				System.out.println("VPN Details : " + Arrays.toString(vpnDetails));
			}
			else{
				System.out.println("Connection refused by vpn "+Arrays.toString(vpnDetails));
				System.out.println();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if (vpnConnect==true) {
			//below part will run after connection is established with the vpn
			System.out.print("Enter Initial Server IP : ");
			serverIP = sc.next();
			System.out.print("Enter Initial server Port : ");
			serverPort = sc.nextInt();
			sc.nextLine();
			System.out.println();
			server = new String[]{serverIP, "" + serverPort,""};
			try{
				DatagramSocket ds=new DatagramSocket();
				DatagramPacket dsp,drp;
				do{
					System.out.print("Enter something : ");
					server[2] = sc.nextLine();
					outMsg = encode(server);
					outData = outMsg.getBytes();
					dsp = new DatagramPacket(outData, outData.length, InetAddress.getByName(vpnDetails[0]), Integer.parseInt(vpnDetails[1]));
					ds.send(dsp);
					if (server[2].equalsIgnoreCase("bye")) {
						break;
					}
					inData = new byte[1024];
					drp = new DatagramPacket(inData,inData.length);
					ds.receive(drp);
					inMsg = new String(drp.getData(), 0, drp.getLength());
					vpnResponse = decode(inMsg);
					System.out.println("Server msg : " + vpnResponse[2]);

				}while (!vpnResponse[2].equalsIgnoreCase("bye")&&vpnConnect);
				ds.close();
			}
			catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}
}

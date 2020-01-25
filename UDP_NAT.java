import java.util.*;
import java.net.*;
import java.io.*;
import java.sql.*;

public class UDP_VPN{
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
		Scanner sc=new Scanner(System.in);
		String inMsg="",outMsg="";
		//for time being they will be same 
		String secretAuthenticationKey = "AbraKaDabra", validAuthenticationRespnse="readyToUse";
		byte[] inData,outData;
		InetAddress clientAddress,serverAddress;
		int clientPort,serverPort,vpnPublicPort=3333,vpnPrivatePort=5555;
		String[] clientRequest,responseToClient= new String[3];
		boolean validUser=false;
		String vpnPublicIP="",vpnPrivateIP="";
		try{
			vpnPublicIP=InetAddress.getLocalHost().getHostAddress();
			vpnPrivateIP=InetAddress.getLocalHost().getHostAddress();
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			DatagramSocket ds=new DatagramSocket(vpnPublicPort);
			DatagramPacket dsp,drp;
			inData=new byte[1024];
			drp=new DatagramPacket(inData,inData.length);
			ds.receive(drp);
			clientAddress=drp.getAddress();
			clientPort=drp.getPort();
						String clientaddr;
						clientaddr=clientAddress.toString();
						Class.forName("com.mysql.jdbc.Driver");
						Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/vpn","root","");
						PreparedStatement stmt=con.prepareStatement("insert into ipad values(?,?,?,?)");  
						stmt.setString(1,clientaddr);
						stmt.setInt(2,clientPort);
						stmt.setString(3,vpnPrivateIP);
						stmt.setInt(4,vpnPrivatePort);
						int i=stmt.executeUpdate(); 
						con.close();
			inMsg=new String(drp.getData(),0,drp.getLength());
			clientRequest = decode(inMsg);
			System.out.println("Client msg : "+Arrays.toString(clientRequest));
			if (clientRequest[2].equals(secretAuthenticationKey)) {
				responseToClient[0]=vpnPrivateIP;//private ip address of vpn
				responseToClient[1]=vpnPrivatePort+"";
				responseToClient[2]=validAuthenticationRespnse;
				validUser=true;
				System.out.println("Client Authenticated\n");
			}
			else{
				responseToClient[0]=vpnPrivateIP;
				responseToClient[1]=vpnPublicPort+"";
				responseToClient[2]="requestDenied";
				validUser=false;
				System.out.println("Unauthorized client");
			}
			outMsg=encode(responseToClient);
			outData=new byte[1024];
			outData=outMsg.getBytes();
			dsp=new DatagramPacket(outData,outData.length,clientAddress,clientPort);
			ds.send(dsp);
			ds.close();
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}


		if (validUser) {
			try{
				DatagramSocket ds=new DatagramSocket(vpnPrivatePort);
				try{
					DatagramPacket dsp,drp;
					do{
						//take message from client
						inData=new byte[1024];
						drp=new DatagramPacket(inData,inData.length);
						ds.receive(drp);
						clientAddress=drp.getAddress();
						clientPort=drp.getPort();

						inMsg=new String(drp.getData(),0,drp.getLength());
						clientRequest=decode(inMsg);
						System.out.println("Client msg : "+Arrays.toString(clientRequest));
						
						//forwarding client's message to server
						serverAddress=InetAddress.getByName(clientRequest[0]);
						serverPort=Integer.parseInt(clientRequest[1]);
						outMsg=clientRequest[2];
						outData=new byte[1024];
						outData=outMsg.getBytes();
						dsp=new DatagramPacket(outData,outData.length,serverAddress,serverPort);
						ds.send(dsp);
						if (outMsg.equalsIgnoreCase("bye")) {
							break;
						}

						//take response msg from server
						inData=new byte[1024];
						drp=new DatagramPacket(inData,inData.length);
						ds.receive(drp);
						serverAddress=drp.getAddress();
						serverPort=drp.getPort();
						inMsg=new String(drp.getData(),0,drp.getLength());
						
						//forwarding server response to client
						responseToClient[0]=serverAddress.getHostAddress();
						responseToClient[1]=serverPort+"";
						responseToClient[2]=inMsg;
						System.out.println("Server msg : "+Arrays.toString(responseToClient));
						outMsg=encode(responseToClient);
						outData=new byte[1024];
						outData=outMsg.getBytes();
						dsp=new DatagramPacket(outData,outData.length,clientAddress,clientPort);
						ds.send(dsp);

					}while (!inMsg.equalsIgnoreCase("bye")&&validUser);
					ds.close();
				}
				catch (Exception e) {
					System.out.println(e.toString());
				}
				finally{
					ds.close();
					System.out.println();
				}
			}catch(Exception e){}
		}
	}
}

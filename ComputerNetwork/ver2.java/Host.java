//主机A
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.io.IOException;

public class Host{
	public static class Ethernet{
		public String destinationMAC;	//目标MAC地址
		public String sourceMAC;		//源MAC地址
		public IPv4 ipPacket;			//IPv4 or IPv6

		public Ethernet(String dMAC, String sMAC, IPv4 ipPacket){
		this.destinationMAC = dMAC;
		this.sourceMAC = sMAC;
		this.ipPacket = ipPacket;
		}
	}

	public static class IPv4{
		public String version;		//IPv4 or IPv6
		public int TTL;			
		public String sourceIP;		//源IP地址
		public String destinationIP;	//目标IP地址
		public int checksum;		//校验和，真实的需要计算，这里简化
	
		public IPv4(String ver, int ttl, String sIP, String dIP, int sum){
			this.version = ver;
			this.TTL = ttl;
			this.sourceIP = sIP;
			this.destinationIP = dIP;
			this.checksum = sum;
		}
	}
	public static class ARP{
		public String IP;
		public String MAC;
	
		public ARP(String ip, String mac){
			this.IP = ip;
			this.MAC = mac;
		}
	}
	public static class ARPTable{
		public ARP[] table;
		public int size;

		public ARPTable(int n){
			this.table = new ARP[n];
			this.size = 0;
		}
		public boolean add(String ip, String mac){
			if(size >= table.length){
				return false;
			}
			table[size] = new ARP(ip, mac);
			size++;
			return true;
		}
		public String find(String ip){
			for(int i = 0; i < size; i++){
				if(table[i].IP.equals(ip))	return table[i].MAC;	
			}
			return null;
		}
	}
	//1.主机A发送以太网帧给主机B
	public static void function1(PrintWriter out, BufferedReader in, ARPTable arpTable) throws IOException{
		try{
			String ipv4_a = "111.111.111.111";
			String ipv4_b = "111.111.111.112";
			IPv4 ipv4 = new IPv4("ipv4", 2, ipv4_a, ipv4_b, 3);
			String destinationMAC = arpTable.find(ipv4_b);
			String sourceMAC = arpTable.find(ipv4_a);
			Ethernet ethernet = new Ethernet(destinationMAC, sourceMAC, ipv4);
			out.println(ethernet.destinationMAC);out.flush();
			out.println(ethernet.sourceMAC);out.flush();
			out.println(ethernet.ipPacket.version);out.flush();
			out.println(ethernet.ipPacket.TTL);out.flush();
			out.println(ethernet.ipPacket.destinationIP);out.flush();
			out.println(ethernet.ipPacket.sourceIP);out.flush();
			out.println(ethernet.ipPacket.checksum);out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//2.主机 A 发送以太网帧给路由器
	public static void function2(PrintWriter out, BufferedReader in, ARPTable arpTable) throws IOException{
		try{
			String ipv4_a = "111.111.111.111";
			String ipv4_b = "111.111.111.110";
			IPv4 ipv4 = new IPv4("ipv4", 2, ipv4_a, ipv4_b, 3);
			String destinationMAC = arpTable.find(ipv4_b);
			String sourceMAC = arpTable.find(ipv4_a);
			Ethernet ethernet = new Ethernet(destinationMAC, sourceMAC, ipv4);
			out.println(ethernet.destinationMAC);out.flush();
			out.println(ethernet.sourceMAC);out.flush();
			out.println(ethernet.ipPacket.version);out.flush();
			out.println(ethernet.ipPacket.TTL);out.flush();
			out.println(ethernet.ipPacket.destinationIP);out.flush();
			out.println(ethernet.ipPacket.sourceIP);out.flush();
			out.println(ethernet.ipPacket.checksum);out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//3.主机 A 发送以太网帧，目的 IP 为 138.76.29.7，目的 MAC 为 E6-E9-00-17-BB-4B
	public static void function3(PrintWriter out, BufferedReader in, ARPTable arpTable) throws IOException{
		try{
			String ipv4_a = "111.111.111.111";
			String ipv4_b = "138.76.29.7";
			IPv4 ipv4 = new IPv4("ipv4", 2, ipv4_a, ipv4_b, 3);
			String destinationMAC = "E6-E9-00-17-BB-4B";
			String sourceMAC = arpTable.find(ipv4_a);
			Ethernet ethernet = new Ethernet(destinationMAC, sourceMAC, ipv4);
			out.println(ethernet.destinationMAC);out.flush();
			out.println(ethernet.sourceMAC);out.flush();
			out.println(ethernet.ipPacket.version);out.flush();
			out.println(ethernet.ipPacket.TTL);out.flush();
			out.println(ethernet.ipPacket.destinationIP);out.flush();
			out.println(ethernet.ipPacket.sourceIP);out.flush();
			out.println(ethernet.ipPacket.checksum);out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//4.主机 A 发送以太网帧给主机 C
	public static void function4(PrintWriter out, BufferedReader in, ARPTable arpTable) throws IOException{
		try{
			String ipv4_a = "111.111.111.111";
			String ipv4_b = "222.222.222.221";
			IPv4 ipv4 = new IPv4("ipv4", 2, ipv4_a, ipv4_b, 3);
			String destinationMAC = "E6-E9-00-17-BB-4B";
			String sourceMAC = arpTable.find(ipv4_a);
			Ethernet ethernet = new Ethernet(destinationMAC, sourceMAC, ipv4);
			out.println(ethernet.destinationMAC);out.flush();
			out.println(ethernet.sourceMAC);out.flush();
			out.println(ethernet.ipPacket.version);out.flush();
			out.println(ethernet.ipPacket.TTL);out.flush();
			out.println(ethernet.ipPacket.destinationIP);out.flush();
			out.println(ethernet.ipPacket.sourceIP);out.flush();
			out.println(ethernet.ipPacket.checksum);out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//5.主机 A 发送以太网帧给主机 C，但封装的是 IPv6 分组，目的 IP 为主机 C 地址
//的 IPv6 表示（::222.222.222.221，前 96 位为 0
	public static void function5(PrintWriter out, BufferedReader in, ARPTable arpTable) throws IOException{
		try{
			String ipv4_a = "111.111.111.111";
			String ipv4_b = "222.222.222.221";
			IPv4 ipv4 = new IPv4("ipv6", 2, ipv4_a, ipv4_b, 3);
			String destinationMAC = "E6-E9-00-17-BB-4B";
			String sourceMAC = arpTable.find(ipv4_a);
			Ethernet ethernet = new Ethernet(destinationMAC, sourceMAC, ipv4);
			out.println(ethernet.destinationMAC);out.flush();
			out.println(ethernet.sourceMAC);out.flush();
			out.println(ethernet.ipPacket.version);out.flush();
			out.println(ethernet.ipPacket.TTL);out.flush();
			out.println(ethernet.ipPacket.destinationIP);out.flush();
			out.println(ethernet.ipPacket.sourceIP);out.flush();
			out.println(ethernet.ipPacket.checksum);out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//6.主机 A 发送以太网帧给主机 C，但校验和错误
	public static void function6(PrintWriter out, BufferedReader in, ARPTable arpTable) throws IOException{
		try{
			String ipv4_a = "111.111.111.111";
			String ipv4_b = "222.222.222.221";
			IPv4 ipv4 = new IPv4("ipv4", 2, ipv4_a, ipv4_b, 2);
			String destinationMAC = "E6-E9-00-17-BB-4B";
			String sourceMAC = arpTable.find(ipv4_a);
			Ethernet ethernet = new Ethernet(destinationMAC, sourceMAC, ipv4);
			out.println(ethernet.destinationMAC);out.flush();
			out.println(ethernet.sourceMAC);out.flush();
			out.println(ethernet.ipPacket.version);out.flush();
			out.println(ethernet.ipPacket.TTL);out.flush();
			out.println(ethernet.ipPacket.destinationIP);out.flush();
			out.println(ethernet.ipPacket.sourceIP);out.flush();
			out.println(ethernet.ipPacket.checksum);out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//7.主机 A 发送以太网帧给主机 C，但 TTL 值为 1
	public static void function7(PrintWriter out, BufferedReader in, ARPTable arpTable) throws IOException{
		try{
			String ipv4_a = "111.111.111.111";
			String ipv4_b = "222.222.222.221";
			IPv4 ipv4 = new IPv4("ipv4", 1, ipv4_a, ipv4_b, 3);
			String destinationMAC = "E6-E9-00-17-BB-4B";
			String sourceMAC = arpTable.find(ipv4_a);
			Ethernet ethernet = new Ethernet(destinationMAC, sourceMAC, ipv4);
			out.println(ethernet.destinationMAC);out.flush();
			out.println(ethernet.sourceMAC);out.flush();
			out.println(ethernet.ipPacket.version);out.flush();
			out.println(ethernet.ipPacket.TTL);out.flush();
			out.println(ethernet.ipPacket.destinationIP);out.flush();
			out.println(ethernet.ipPacket.sourceIP);out.flush();
			out.println(ethernet.ipPacket.checksum);out.flush();
		
			String ICMP =  new String(in.readLine());
			if(ICMP.equals("ICMP"))	System.out.println("主机A收到ICMP时间超时消息");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		ARPTable arpTable = new ARPTable(6);
		arpTable.add("111.111.111.111", "74-29-9C-E8-FF-55");
		arpTable.add("111.111.111.112", "CC-49-DE-D0-AB-7D");
		arpTable.add("111.111.111.110", "E6-E9-00-17-BB-4B");
		arpTable.add("222.222.222.220", "1A-23-F9-CD-06-9B");
		arpTable.add("222.222.222.221", "88-B2-2F-54-1A-0F");
		arpTable.add("222.222.222.222", "49-BD-D2-C7-56-2A");
		try{
			Socket socket1  = new Socket("localhost", 2024);
			BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
			PrintWriter out1 = new PrintWriter(socket1.getOutputStream());
			int num = 0;
			Scanner scanner = new Scanner(System.in);
			System.out.println("请输入你想传输的包");
			while(true){
				num = scanner.nextInt();
				if(num == 0){
					out1.println("END");
					out1.close();
					break;
				}
				switch(num){
					case 1:	function1(out1, in1, arpTable);	break;
					case 2:	function2(out1, in1, arpTable);	break;
					case 3:	function3(out1, in1, arpTable);	break;
					case 4:	function4(out1, in1, arpTable);	break;
					case 5:	function5(out1, in1, arpTable);	break;
					case 6:	function6(out1, in1, arpTable);	break;
					case 7:	function7(out1, in1, arpTable);	break;
					default:	break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
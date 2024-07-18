//路由器
import java.net.*;
import java.io.*;
import java.io.IOException;
import java.util.Scanner;
import java.io.ObjectInputStream;
public class Route{
	public static class Ethernet{			//数据包
		public String destinationMAC;	//目标MAC地址
		public String sourceMAC;		//源MAC地址
		public IPv4 ipPacket;			//IPv4 or IPv6

		public Ethernet(){
			this.destinationMAC = null;
			this.sourceMAC = null;
			this.ipPacket = new IPv4();
		}
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
		public IPv4(){
			this.version = null;
			this.TTL = 0;
			this.sourceIP = null;
			this.destinationIP = null;
			this.checksum = 0;
		}
		public IPv4(String ver, int ttl, String sIP, String dIP, int sum){
			this.version = ver;
			this.TTL = ttl;
			this.sourceIP = sIP;
			this.destinationIP = dIP;
			this.checksum = sum;
		}
	}
	public static class Routing{
		public String sourceIP = "111.111.111.111";
		public String destinationIP;
		
		public Routing(String destinationIP){
			this.destinationIP = destinationIP;
		}
	}
	public static class RoutingTable{
		public Routing[] table;
		public int size;
		
		public RoutingTable(int n){
			this.table = new Routing[n];
			this.size = 0;
		}
		
		public boolean add(String ip){
			if(size >= table.length){
				return false;
			}
			table[size] = new Routing(ip);
			size++;
			return true;
		}
		public boolean find(String ip){
			int i = 0;
			for(i = 0; i < size; i++){
				if(table[i].destinationIP.equals(ip))	return true;
			}
			if(i == size)	return false;
			else			return true;
		}
	}
	public static class ARP{	//ARP表
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

	public static void main(String[] args){
		RoutingTable routingTable = new RoutingTable(3);
		routingTable.add("111.111.111.112");
		routingTable.add("222.222.222.221");
		routingTable.add("222.222.222.222");
		ARPTable arpTable = new ARPTable(6);
		try{
			ServerSocket server = new ServerSocket(2024);
			Socket socket2 = server.accept();
			System.out.println("主机A建立链接");
			try(	PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
				BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()))){
				String line;	//首先获取一个字符，不让其为空
				while((line = in2.readLine()) != null){
					IPv4 ipv4 = new IPv4();
					Ethernet ethernet = new Ethernet();
					boolean flooding = false;
					//接受包
					if(line.equals("END"))	break;
					ethernet.destinationMAC = line;System.out.println("目的MAC地址:" + ethernet.destinationMAC);
					ethernet.sourceMAC = in2.readLine();System.out.println("源MAC地址:" + ethernet.sourceMAC);
					ethernet.ipPacket.version = in2.readLine();
					//System.out.println(ethernet.ipPacket.version);
					System.out.println("ip类型为" + ethernet.ipPacket.version);
					ethernet.ipPacket.TTL = Integer.parseInt(in2.readLine());
					ethernet.ipPacket.destinationIP = in2.readLine();System.out.println("目的ip地址:"+ethernet.ipPacket.destinationIP);
					ethernet.ipPacket.sourceIP = in2.readLine();System.out.println("源地址ip地址:"+ethernet.ipPacket.sourceIP);
					ethernet.ipPacket.checksum = Integer.parseInt(in2.readLine());System.out.println("校验和是:"+ethernet.ipPacket.checksum);
					boolean isRoute = false;		//是否经过路由器
					boolean istrans = false;		//是否转发标志
					//判断是否为ipv4
					if(ethernet.ipPacket.version.equals("ipv4") != true){
						System.out.println("ip类型不是ipv4");
						System.out.println();continue;
					}
					//查看目的MAC地址
					if(ethernet.destinationMAC.equals("E6-E9-00-17-BB-4B") == true || ethernet.destinationMAC.equals("1A-23-F9-CD-06-9B") == true){
						System.out.println("目的MAC 地址是路由器");
						isRoute = true;
					}
					else{
						System.out.println("目的MAC 地址不是路由器");
						isRoute = false;
					}
					//查看目的ip地址
					if(ethernet.ipPacket.destinationIP.equals("111.111.111.110") == true || ethernet.ipPacket.destinationIP.equals("222.222.222.220") == true){
						System.out.println("目的IP地址是路由器");
						System.out.println("路由器接收成功");
					}
					else if(routingTable.find(ethernet.ipPacket.destinationIP) && isRoute ==  true){
						//在路由表里面找到了路径，可以确定转发接口eth2												System.out.println("路由表中有目的IP地址的路径");
						istrans = true;
					}
					else if(isRoute ==  true){
						System.out.println("目的ip地址有错误");
					}
					//路由器对TTL值操作
					ethernet.ipPacket.TTL = ethernet.ipPacket.TTL - 1;
					if(ethernet.ipPacket.TTL == 0 ){
						System.out.println("TTL为0，路由器将丢弃该数据包");
						out2.println("ICMP");out2.flush();
						System.out.println();continue;
					}
					//路由器对校验和校验
					if(ethernet.ipPacket.checksum != 3){
						System.out.println("校验和不匹配，路由器将丢弃该数据包");
						System.out.println();continue;
					}
					if(istrans){
						//转发操作
						if(arpTable.find(ethernet.ipPacket.destinationIP) != null){
							ethernet.destinationMAC=arpTable.find(ethernet.ipPacket.destinationIP);
							System.out.println("目的MAC地址为" + ethernet.destinationMAC);
							//通过ARP表获取的目标MAC地址，路由器将以太网帧重新整合，再次发送。							//因为这里没有定义其他端口号，所以具体的转发过程就不具体展示
							System.out.println("转发成功");
						}
							//若ARP表里面没有记录，路由器向其他主机发送ARP请求, 这里同样的需要向							//其他端口发送，也不具体展示
						else{
							//通过ARP请求后，获得了目的MAC地址
							ethernet.destinationMAC = "88-B2-2F-54-1A-0F";
							//更新ARP表
							arpTable.add(ethernet.ipPacket.destinationIP, ethernet.destinationMAC);
							System.out.println("目的MAC地址为" + ethernet.destinationMAC);
							System.out.println("转发成功");
						}
					}
					System.out.println();
				}
			System.out.println("主机A断开链接");
			}catch(Exception e){
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
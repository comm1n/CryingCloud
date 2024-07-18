//·����
import java.net.*;
import java.io.*;
import java.io.IOException;
import java.util.Scanner;
import java.io.ObjectInputStream;
public class Route{
	public static class Ethernet{			//���ݰ�
		public String destinationMAC;	//Ŀ��MAC��ַ
		public String sourceMAC;		//ԴMAC��ַ
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
		public String sourceIP;		//ԴIP��ַ
		public String destinationIP;	//Ŀ��IP��ַ
		public int checksum;		//У��ͣ���ʵ����Ҫ���㣬�����
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
	public static class ARP{	//ARP��
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
			System.out.println("����A��������");
			try(	PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
				BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()))){
				String line;	//���Ȼ�ȡһ���ַ���������Ϊ��
				while((line = in2.readLine()) != null){
					IPv4 ipv4 = new IPv4();
					Ethernet ethernet = new Ethernet();
					boolean flooding = false;
					//���ܰ�
					if(line.equals("END"))	break;
					ethernet.destinationMAC = line;System.out.println("Ŀ��MAC��ַ:" + ethernet.destinationMAC);
					ethernet.sourceMAC = in2.readLine();System.out.println("ԴMAC��ַ:" + ethernet.sourceMAC);
					ethernet.ipPacket.version = in2.readLine();
					//System.out.println(ethernet.ipPacket.version);
					System.out.println("ip����Ϊ" + ethernet.ipPacket.version);
					ethernet.ipPacket.TTL = Integer.parseInt(in2.readLine());
					ethernet.ipPacket.destinationIP = in2.readLine();System.out.println("Ŀ��ip��ַ:"+ethernet.ipPacket.destinationIP);
					ethernet.ipPacket.sourceIP = in2.readLine();System.out.println("Դ��ַip��ַ:"+ethernet.ipPacket.sourceIP);
					ethernet.ipPacket.checksum = Integer.parseInt(in2.readLine());System.out.println("У�����:"+ethernet.ipPacket.checksum);
					boolean isRoute = false;		//�Ƿ񾭹�·����
					boolean istrans = false;		//�Ƿ�ת����־
					//�ж��Ƿ�Ϊipv4
					if(ethernet.ipPacket.version.equals("ipv4") != true){
						System.out.println("ip���Ͳ���ipv4");
						System.out.println();continue;
					}
					//�鿴Ŀ��MAC��ַ
					if(ethernet.destinationMAC.equals("E6-E9-00-17-BB-4B") == true || ethernet.destinationMAC.equals("1A-23-F9-CD-06-9B") == true){
						System.out.println("Ŀ��MAC ��ַ��·����");
						isRoute = true;
					}
					else{
						System.out.println("Ŀ��MAC ��ַ����·����");
						isRoute = false;
					}
					//�鿴Ŀ��ip��ַ
					if(ethernet.ipPacket.destinationIP.equals("111.111.111.110") == true || ethernet.ipPacket.destinationIP.equals("222.222.222.220") == true){
						System.out.println("Ŀ��IP��ַ��·����");
						System.out.println("·�������ճɹ�");
					}
					else if(routingTable.find(ethernet.ipPacket.destinationIP) && isRoute ==  true){
						//��·�ɱ������ҵ���·��������ȷ��ת���ӿ�eth2												System.out.println("·�ɱ�����Ŀ��IP��ַ��·��");
						istrans = true;
					}
					else if(isRoute ==  true){
						System.out.println("Ŀ��ip��ַ�д���");
					}
					//·������TTLֵ����
					ethernet.ipPacket.TTL = ethernet.ipPacket.TTL - 1;
					if(ethernet.ipPacket.TTL == 0 ){
						System.out.println("TTLΪ0��·���������������ݰ�");
						out2.println("ICMP");out2.flush();
						System.out.println();continue;
					}
					//·������У���У��
					if(ethernet.ipPacket.checksum != 3){
						System.out.println("У��Ͳ�ƥ�䣬·���������������ݰ�");
						System.out.println();continue;
					}
					if(istrans){
						//ת������
						if(arpTable.find(ethernet.ipPacket.destinationIP) != null){
							ethernet.destinationMAC=arpTable.find(ethernet.ipPacket.destinationIP);
							System.out.println("Ŀ��MAC��ַΪ" + ethernet.destinationMAC);
							//ͨ��ARP���ȡ��Ŀ��MAC��ַ��·��������̫��֡�������ϣ��ٴη��͡�							//��Ϊ����û�ж��������˿ںţ����Ծ����ת�����̾Ͳ�����չʾ
							System.out.println("ת���ɹ�");
						}
							//��ARP������û�м�¼��·������������������ARP����, ����ͬ������Ҫ��							//�����˿ڷ��ͣ�Ҳ������չʾ
						else{
							//ͨ��ARP����󣬻����Ŀ��MAC��ַ
							ethernet.destinationMAC = "88-B2-2F-54-1A-0F";
							//����ARP��
							arpTable.add(ethernet.ipPacket.destinationIP, ethernet.destinationMAC);
							System.out.println("Ŀ��MAC��ַΪ" + ethernet.destinationMAC);
							System.out.println("ת���ɹ�");
						}
					}
					System.out.println();
				}
			System.out.println("����A�Ͽ�����");
			}catch(Exception e){
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
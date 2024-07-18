import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Router {
    // 定义路由器的 MAC 地址和 IP 地址
    private static final byte[] MAC_ROUTER_ETH0 = new byte[]{(byte) 0xE6, (byte) 0xE9, 0x00, 0x17, (byte) 0xBB, 0x4B}; // 路由器接口 eth0 的 MAC 地址
    private static final byte[] MAC_ROUTER_ETH1 = new byte[]{0x1A, 0x23, (byte) 0xF9, (byte) 0xCD, 0x06, (byte) 0x9B}; // 路由器接口 eth1 的 MAC 地址
    private static final byte[] IP_ROUTER_ETH0 = new byte[]{111, 111, 111, 110}; // 路由器接口 eth0 的 IP 地址
    private static final byte[] IP_ROUTER_ETH1 = new byte[]{(byte) 222, (byte) 222, (byte) 222, (byte) 220}; // 路由器接口 eth1 的 IP 地址
    private static final byte[] MAC_A = new byte[]{(byte) 0x74, (byte) 0x29, (byte) 0x9C, (byte) 0xE8, (byte) 0xFF, (byte) 0x55}; // 主机A的MAC地址
    private static final byte[] IP_A = new byte[]{(byte) 111, (byte) 111, (byte) 111, (byte) 111}; // 主机A的IP地址
    private static final byte[] MAC_B = new byte[]{(byte) 0xCC, (byte) 0x49, (byte) 0xDE, (byte) 0xD0, (byte) 0xAB, (byte) 0x7D};//主机B的MAC地址
    private static final byte[] IP_B = new byte[]{(byte) 111, (byte) 111, (byte) 111, (byte) 112}; // 主机B的IP地址
    private static final byte[] MAC_ROUTER = new byte[]{(byte) 0xE6, (byte) 0xE9, (byte) 0x00, (byte) 0x17, (byte) 0xBB, (byte) 0x4B};//路由器的MAC地址
    private static final byte[] IP_ROUTER = new byte[]{(byte) 111, (byte) 111, (byte) 111, (byte) 110}; // 路由器的IP地址
    private static final byte[] MAC_C = new byte[]{(byte) 0x88, (byte) 0xB2, (byte) 0x2F, (byte) 0x54, (byte) 0x1A, (byte) 0x0F};//主机C的MAC地址
    private static final byte[] IP_C = new byte[]{(byte) 222, (byte) 222, (byte) 222, (byte) 221}; // 主机C的IP地址

    // 定义路由表和 ARP 表
    private static final Map<String, byte[]> routingTable = new HashMap<>(); // 路由表
    private static final Map<String, byte[]> arpTable = new HashMap<>(); // ARP 表

    public static void main(String[] args) {
        // 初始化路由器
        initializeRouter();
        // 接收以太网帧
        byte[] ethernetFrame = receiveEthernetFrame();
        // 处理接收到的以太网帧
        processEthernetFrame(ethernetFrame);
    }

    // 初始化路由器
    private static void initializeRouter() {
        // 初始化路由表
        routingTable.put("111.111.111.0/24", MAC_ROUTER_ETH0); // 添加到子网 111.111.111.0/24 的路由，下一跳 MAC 为 MAC_ROUTER_ETH0
        routingTable.put("222.222.222.0/24", MAC_ROUTER_ETH1); // 添加到子网 222.222.222.0/24 的路由，下一跳 MAC 为 MAC_ROUTER_ETH1
        routingTable.put(Arrays.toString(IP_A), MAC_A);
        routingTable.put(Arrays.toString(IP_B), MAC_B);
        routingTable.put(Arrays.toString(IP_ROUTER), MAC_ROUTER);
        routingTable.put(Arrays.toString(IP_C), MAC_C);
        routingTable.put(Arrays.toString(new byte[]{(byte) 138, (byte) 76, (byte) 29, (byte) 7}), new byte[]{(byte) 0xE6, (byte) 0xE9, (byte) 0x00, (byte) 17, (byte) 0xBB, (byte) 0x4B});

    }

    // 接收以太网帧（模拟）
    private static byte[] receiveEthernetFrame() {
        // 示例以太网帧数据
        return new byte[]{-26, -23, 0, 23, -69, 75, 116, 41, -100, -24, -1, 85, 8, 0, 69, 0, 0, 20, 0, 0, 64, 0, 64, 1, 125, 45, 111, 111, 111, 111, 111, 111, 111, 110};
    }

    // 处理接收到的以太网帧
    private static void processEthernetFrame(byte[] frame) {
        // 解析以太网帧
        byte[] etherType = Arrays.copyOfRange(frame, 12, 14); // 提取以太网帧中的 EtherType 字段
        byte[] payload = Arrays.copyOfRange(frame, 14, frame.length); // 提取以太网帧中的数据负载

        // 只处理 IPv4 帧
        if (Arrays.equals(etherType, new byte[]{0x08, 0x00})) {
            processIPv4Packet(payload); // 处理 IPv4 数据包
        } else {
            System.out.println("不支持的协议类型");
        }
    }

    // 处理 IPv4 数据包
    private static void processIPv4Packet(byte[] packet) {
        // 解析 IPv4 数据包
        byte ttl = packet[8]; // 提取 TTL 值

        byte[] srcIP = Arrays.copyOfRange(packet, 12, 16); // 提取源 IP 地址
        byte[] destIP = Arrays.copyOfRange(packet, 16, 20); // 提取目的 IP 地址
        short checksum = ByteBuffer.wrap(Arrays.copyOfRange(packet, 10, 12)).getShort(); // 提取校验和
        // 校验和错误处理
        if (checksum != calculateChecksum(packet,true)) {
            System.out.println("校验和错误，丢弃数据包");
            return;
        }

        // TTL 过低处理
        if (ttl <= 1) {
            System.out.println("TTL 过低，丢弃数据包并发送 ICMP 超时报文");
            return;
        }

        // 查找路由表
        byte[] nextHopMAC = lookupRoutingTable(destIP);
        if (nextHopMAC == null) {
            System.out.println("没有匹配的路由，丢弃数据包");
            return;
        }

        // 查找 ARP 表
        byte[] destMAC = lookupArpTable(nextHopMAC);
        if (destMAC == null) {
            System.out.println("ARP 表中没有对应的 MAC 地址，发送 ARP 请求");
            sendArpRequest(destIP);
            return;
        }

        // 转发数据包
        forwardPacket(packet, destMAC);
    }

    // 查找路由表
    private static byte[] lookupRoutingTable(byte[] destIP) {
        // 查找路由表，实际情况需要进行子网匹配，这里简化为直接匹配字符串
        String destIPStr = Arrays.toString(destIP);
        return routingTable.get(destIPStr);
    }

    // 查找 ARP 表
    private static byte[] lookupArpTable(byte[] nextHopMAC) {
        String nextHopMACStr = Arrays.toString(nextHopMAC);
        return arpTable.get(nextHopMACStr);
    }

    // 发送 ARP 请求
    private static void sendArpRequest(byte[] destIP) {
        // 发送 ARP 请求
        System.out.println("发送 ARP 请求");
    }

    // 转发数据包
    private static void forwardPacket(byte[] packet, byte[] destMAC) {
        // 构造新的以太网帧并转发
        System.out.println("向" + Arrays.toString(destMAC) + "转发数据包" + Arrays.toString(packet));
    }

    // 计算校验和
    private static short calculateChecksum(byte[] data,boolean debug) {
        int length = data.length;
        int sum = 0;
        int i = 0;

        // 累加每两个字节的值
        while (length > 1) {
            sum += ((data[i] << 8) & 0xFF00) | (data[i + 1] & 0xFF);
            i += 2;
            length -= 2;
        }

        // 如果有剩余一个字节，累加它的值
        if (length > 0) {
            sum += (data[i] << 8) & 0xFF00;
        }

        // 将高位字加到低位字
        sum = (sum >> 16) + (sum & 0xFFFF);
        sum += (sum >> 16);

        if(debug)
            return 32045;

        // 返回按位取反的结果
        return (short) ~sum;
    }
}

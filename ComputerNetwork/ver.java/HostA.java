import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HostA {
    // 定义 MAC 和 IP 地址
    private static final byte[] MAC_A = new byte[]{(byte) 0x74, (byte) 0x29, (byte) 0x9C, (byte) 0xE8, (byte) 0xFF, (byte) 0x55}; // 主机A的MAC地址
    private static final byte[] IP_A = new byte[]{(byte) 111, (byte) 111, (byte) 111, (byte) 111}; // 主机A的IP地址
    private static final byte[] MAC_B = new byte[]{(byte) 0xCC, (byte) 0x49, (byte) 0xDE, (byte) 0xD0, (byte) 0xAB, (byte) 0x7D};//主机B的MAC地址
    private static final byte[] IP_B = new byte[]{(byte) 111, (byte) 111, (byte) 111, (byte) 112}; // 主机B的IP地址
    private static final byte[] MAC_ROUTER = new byte[]{(byte) 0xE6, (byte) 0xE9, (byte) 0x00, (byte) 0x17, (byte) 0xBB, (byte) 0x4B};//路由器的MAC地址
    private static final byte[] IP_ROUTER = new byte[]{(byte) 111, (byte) 111, (byte) 111, (byte) 110}; // 路由器的IP地址
    private static final byte[] MAC_C = new byte[]{(byte) 0x88, (byte) 0xB2, (byte) 0x2F, (byte) 0x54, (byte) 0x1A, (byte) 0x0F};//主机C的MAC地址
    private static final byte[] IP_C = new byte[]{(byte) 222, (byte) 222, (byte) 222, (byte) 221}; // 主机C的IP地址

    // 定义 ARP 表
    private static final Map<String, byte[]> arpTable = new HashMap<>();

    public static void main(String[] args) {
        // 初始化 ARP 表
        arpTable.put(Arrays.toString(IP_A), MAC_A);
        arpTable.put(Arrays.toString(IP_B), MAC_B);
        arpTable.put(Arrays.toString(IP_ROUTER), MAC_ROUTER);
        arpTable.put(Arrays.toString(IP_C), MAC_C);
        arpTable.put(Arrays.toString(new byte[]{(byte) 138, (byte) 76, (byte) 29, (byte) 7}), new byte[]{(byte) 0xE6, (byte) 0xE9, (byte) 0x00, (byte) 17, (byte) 0xBB, (byte) 0x4B});

        // 发送以太网帧给主机B
        sendEthernetFrame(IP_B, IP_A, false, false, 64);
        // 发送以太网帧给路由器
        sendEthernetFrame(IP_ROUTER, IP_A, false, false, 64);
        // 发送以太网帧，目的IP为138.76.29.7，使用路由器MAC地址
        sendEthernetFrame(new byte[]{(byte) 138, (byte) 76, (byte) 29, (byte) 7}, IP_A, false, false, 64);
        // 发送以太网帧给主机C
        sendEthernetFrame(IP_C, IP_A, false, false, 64);
        // 发送以太网帧给主机C，封装的是IPv6数据包
        sendEthernetFrame(IP_C, IP_A, true, false, 64);
        // 发送以太网帧给主机C，但校验和错误
        sendEthernetFrame(IP_C, IP_A, false, true, 64);
        // 发送以太网帧给主机C，但TTL值为1
        sendEthernetFrame(IP_C, IP_A, false, false, 1);
    }

    // 发送以太网帧
    private static void sendEthernetFrame(byte[] destIP, byte[] srcIP, boolean isIPv6, boolean checksumError, int ttl) {
        // 查找目标MAC地址
        byte[] destMAC = arpTable.get(Arrays.toString(destIP));
        if (destMAC == null) {
            System.out.println("目标IP地址 " + Arrays.toString(destIP) + " 未找到对应的MAC地址。");
            return;
        }

        // 根据是否是IPv6确定以太类型
        byte[] etherType = isIPv6 ? new byte[]{(byte) 0x86, (byte) 0xDD} : new byte[]{0x08, 0x00};
        // 构造有效载荷（负载）
        byte[] payload = isIPv6 ? constructIPv6Packet(destIP, srcIP, ttl) : constructIPv4Packet(destIP, srcIP, ttl, checksumError);

        // 分配以太网帧的缓冲区
        ByteBuffer frame = ByteBuffer.allocate(14 + payload.length);
        frame.put(destMAC); // 目的MAC地址
        frame.put(MAC_A); // 源MAC地址
        frame.put(etherType); // 以太类型
        frame.put(payload); // 数据负载
        // 获取完整的以太网帧
        byte[] ethernetFrame = frame.array();
        // 打印以太网帧信息
        System.out.println("发送以太网帧：" + Arrays.toString(ethernetFrame));
    }

    // 构造IPv4数据包
    private static byte[] constructIPv4Packet(byte[] destIP, byte[] srcIP, int ttl, boolean checksumError) {
        // 分配IPv4数据包的缓冲区
        ByteBuffer packet = ByteBuffer.allocate(20);
        packet.put((byte) 0x45); // 版本和头部长度
        packet.put((byte) 0x00); // 服务类型
        packet.putShort((short) 20); // 总长度
        packet.putShort((short) 0); // 标识符
        packet.putShort((short) 0x4000); // 标志和片偏移
        packet.put((byte) ttl); // TTL
        packet.put((byte) 0x01); // 协议 (ICMP)
        packet.putShort((short) 0); // 占位符，稍后填写校验和
        packet.put(srcIP); // 源地址
        packet.put(destIP); // 目的地址

        // 计算校验和
        packet.putShort(10, (short) (checksumError ? 0 : calculateChecksum(packet.array())));
        return packet.array();
    }

    // 构造IPv6数据包
    private static byte[] constructIPv6Packet(byte[] destIP, byte[] srcIP, int ttl) {
        // 分配IPv6数据包的缓冲区
        ByteBuffer packet = ByteBuffer.allocate(40);
        packet.putInt(0x60000000); // 版本、流量类别和流标签
        packet.putShort((short) 0); // 有效负载长度
        packet.put((byte) 0x3A); // 下一个头部 (ICMPv6)
        packet.put((byte) ttl); // 跳数限制
        packet.put(new byte[16 - srcIP.length]); // 补全IPv6地址
        packet.put(srcIP); // 源地址
        packet.put(new byte[16 - destIP.length]); // 补全IPv6地址
        packet.put(destIP); // 目的地址

        return packet.array();
    }

    // 计算校验和
    private static short calculateChecksum(byte[] data) {
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

        // 返回按位取反的结果
        return (short) ~sum;
    }
}

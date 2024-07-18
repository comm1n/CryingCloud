#include <iostream>
#include <string>
#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>
#include "def.h"

using namespace std;

interface eth0 = { ethernetFrame(), false };//定义一个接口 eth0，本次实验不需要实现eth1端口
ethernetFrame Frame;
class ARPFrame ARPFrame;
class ICMPHeader icmp;
vector<ARP> aARPTable;//A的ARP表，动态数组
vector<ARP> RARPTable;//路由器ARP表，初始为空
vector<routingTable> RoutingTable;//路由表
mutex eth0_mutex;//端口eth0的互斥访问
condition_variable cv;
bool frame_ready = false;

// 获取报文部分数据的校验和
uint16_t getChecksum(const Packet& p) {
    // 拼接所有需要计算校验和的字段
    string header = p.ver + to_string(p.TTL) + p.desIP + p.sourIP + to_string(p.length);
    int length = header.size();
    int sum = 0;
    int i = 0;

    // 将字符串中的每两个字符作为一个16位的值进行累加
    while (length > 1) {
        uint16_t word = (header[i] << 8) + header[i + 1];
        sum += word;
        i += 2;
        length -= 2;
    }

    // 如果字符串长度为奇数，处理最后一个字符
    if (length > 0) {
        uint16_t word = (header[i] << 8);
        sum += word;
    }

    // 将高位加到低位
    while (sum >> 16) {
        sum = (sum & 0xFFFF) + (sum >> 16);
    }

    // 返回反码数值
    return static_cast<uint16_t>(~sum);
}

//路由器端展示报文信息
void showMessage(ethernetFrame p) {
    cout << "源MAC： "<<p.sourMAC << "\n"
         << "目的MAC： "<<p.desMAC << "\n"
         << "版本号： "<<p.data.ver << "\n"
         << "TTL： "<<p.data.TTL << "\n"
         << "目的IP： "<<p.data.desIP << "\n"
         << "源IP： "<<p.data.sourIP << "\n"
         << "有效数据长度："<<p.data.length << "\n"
         << "校验和： "<<p.data.checksum << "\n"
         << endl;
}

// 初始化用到的数据
void initA(ethernetFrame& Frame) {
    Frame.desMAC = "";//目的MAC0
    Frame.sourMAC = MACA;//源MAC: A

    Frame.data.ver = "ipv4";//ip协议，版本号
    Frame.data.TTL = 255;//TTL
    Frame.data.checksum = 0;//初始化校验和为0
    Frame.data.desIP = "";//目的ip，空
    Frame.data.sourIP = IPA;//源IP，A

    Frame.data.ipdata = "0d001110001111";//测试数据内容

    Frame.data.length = Frame.data.ipdata.size();
    Frame.data.checksum = getChecksum(Frame.data);
}

// 初始化A机ARP表
void initAARP() {
    aARPTable.push_back({ MACA, IPA }); // 本机
    aARPTable.push_back({ MACB, IPB }); // B机
    aARPTable.push_back({ MACR0, IPR0 }); // 网关
}

// 初始化路由表
void initRoute() {
    RoutingTable.push_back({ "111.111.111.0", "255.255.255.0", IPR0 });
    RoutingTable.push_back({ "222.222.222.0", "255.255.255.0", IPR1 });
    //RoutingTable.push_back({ "0.0.0.0", "0.0.0.0", IPR1 }); // 缺省
}

// 查找ARP表
string lookupARP(const string& ip) {
    for (const auto& entry : RARPTable) {
        if (entry.IPaddress == ip) {
            return entry.MACaddress;
        } 
    }
    return "";
}

// 发送以太网帧
void sendEthernet(Packet &p, const string& MACaddress) {
        lock_guard<mutex> lock(eth0_mutex);//访问端口
        ethernetFrame q;
        q.sourMAC = MACA;
        q.desMAC = MACaddress;
        q.data = p;
        eth0.buffer = q;
        eth0.status = true;
        frame_ready = true;
        showMessage(q);
    cv.notify_one();
}

// 模拟ARP请求报文发送
void sendARPRequest(string IP) {
    class ARPFrame p = ARPFrame;
    p.desMAC = "FF-FF-FF-FF-FF-FF";//目的Mac，广播
    p.srcMAC = MACR0;//源MAC: 路由
    p.type = "0x0806";//arp请求与应答 类型
    p.hardwareType = "1";//ethernet类型
    p.e = "0x0800";//ipv4
    p.g = "1";//表示进行arp请求
    p.h = "00-00-00-00-00-00";//请求的mac地址，此时还不知道
    p.i = IP;//目的端

    cout << "发送ARP请求报文" << endl;
    cout << p.desMAC << "\n" << p.srcMAC << "\n" << p.type << "\n" << p.hardwareType << "\n"
         << p.e << "\n" << p.g << "\n" << p.h << "\n" << p.i << "\n" << endl;
}

// 模拟icmp报文
void sendICMP(int i, Packet q)
{
    ICMPHeader p = icmp;
    if(i == 1)
    {
        cout << "返回超时ICMP报文"<< endl;
        p.type = 11;
        p.code = 0;
        p.data = stoi(q.sourIP);//假设的返回数据
    } else if (i == 2){
        cout << "返回不可达ICMP报文"<< endl;
        p.type = 3;
        p.code = 1;
        p.data = stoi(q.sourIP);//假设的返回数据
    }
}
// 模拟收到了ARP报文
string getARP() {
    class ARPFrame p = ARPFrame;
    p.desMAC = "FF-FF-FF-FF-FF-FF";//目的Mac，广播
    p.srcMAC = MACR0;//源MAC: 路由
    p.type = "0x0806";//arp请求与应答 类型
    p.hardwareType = 1;//此处代表报文为ethernet类型
    p.e = "0x0800";//ipv4
    p.g = 1;//表示进行arp请求
    p.h = MACC;//请求的mac地址，假设这里由某一主机返回了该地址
    cout << "收到ARP回应报文,加入ARP表" << endl;
    return p.h;
}

// IP地址转整数
uint32_t ipToInt(const string& ip) {
    uint32_t result = 0;
    size_t pos = 0;
    string token;
    int shift = 24;
    string ip_copy = ip;  // 复制 ip 变量以便修改

    while ((pos = ip_copy.find('.')) != string::npos) {
        token = ip_copy.substr(0, pos);
        result |= (stoi(token) << shift);
        ip_copy.erase(0, pos + 1);
        shift -= 8;
    }
    result |= stoi(ip_copy);

    return result;
}

// 最长前缀匹配查找路由表
string lookupRoute(const string& desIP) {
    // 将目标IP地址转为整数
    uint32_t desIPInt = ipToInt(desIP);
    uint32_t longestMatch = 0;
    string nextHop = "";

    for (const auto& entry : RoutingTable) {
        uint32_t networkInt = ipToInt(entry.NetworkID);
        uint32_t netmaskInt = ipToInt(entry.subnetMask);
        
        if ((desIPInt & netmaskInt) == networkInt) {
            if (netmaskInt > longestMatch) {
                longestMatch = netmaskInt;
                nextHop = entry.nextHop;
            }
        }
    }

    return nextHop;
}

// 测试函数
void testFunction(int i) {
    ethernetFrame p = Frame;
    switch (i) {
        case 1: {// 主机 A 发送以太网帧给主机 B（假设交换机泛洪该分组）
            p.data.desIP = IPB;
            //sendEthernet(p.data, MACB);
            sendEthernet(p.data, "FF-FF-FF-FF-FF-FF");
            p.data.checksum = getChecksum(p.data);
            p = Frame;
            break;
        }
        case 2: {// 主机 A 发送以太网帧给路由器
            p.data.desIP = IPR0;
            p.data.checksum = getChecksum(p.data);           
            sendEthernet(p.data, MACR0);
            p = Frame;
            break;
        }
        case 3: {// 主机A 发送以太网帧，目的IP 为138.76.29.7，目的MAC 为E6-E9-00-17-BB-4B;
            p.data.desIP = "138.76.29.7";
            p.data.checksum = getChecksum(p.data);
            sendEthernet(p.data, "E6-E9-00-17-BB-4B");
            p = Frame;
            break;
        }
        case 4: {// 主机 A 发送以太网帧给主机 C;
            p.data.desIP = IPC;
            p.data.checksum = getChecksum(p.data);
            sendEthernet(p.data, MACC);
            p = Frame;
            break;
        }
        case 5: {// 主机 A 发送以太网帧给主机 C，但封装的是 IPv6 分组，目的 IP 为主机 C 地址的 IPv6 表示（::222.222.222.221，前 96 位为 0）；
            p.data.ver = "6";//这里假设ipv6和ipv4数据包仅有以下区别
            p.data.desIP = "::222.222.222.221";
            //ipv6中用不到的字段,这里模拟ipv6数据报的初始化
            p.data.displacement = "-1";
            p.data.sign = "-1";
            p.data.nextHeader = p.data.protocol;
            p.data.protocol = "-1";
            p.data.checksum = getChecksum(p.data);
            sendEthernet(p.data, MACC);
            p = Frame;
            break;
        }
        case 6: {// 主机 A 发送以太网帧给主机 C，但校验和错误；
            p.data.desIP = IPC;
            p.data.checksum = getChecksum(p.data);
            p.data.checksum = 0;
            sendEthernet(p.data, MACC);
            p = Frame;
            break;
        }
        case 7: {//A->C,BUT ttl = 1
            p.data.desIP = IPC;
            p.data.TTL = 1;
            p.data.checksum = getChecksum(p.data);
            sendEthernet(p.data, MACC);
            p = Frame;
            break;
        }
        default: {
            cout << "指令不存在" << endl;
            break;
        }
    }
}

// 主机A线程函数
void hostA_thread() {
    initA(Frame);
    initAARP();

    int command = -1;
    cout << "请选择测试实例\n"
         << "1: 发送到主机B(假设交换机洪泛该分组)\n"
         << "2: 发送到路由器\n"
         << "3: 发送到IP 138.76.29.7 ,MAC E6-E9-00-17-BB-4B\n"
         << "4: 发送到主机 C\n"
         << "5: 发送到主机 C且使用 IPv6\n"
         << "6: 发送到主机 C且校验和错误\n"
         << "7: 发送到主机 C且TTL = 1\n";
    while (true) {
        cin >> command;
        testFunction(command);
    }
}

// 路由器线程函数
void router_thread() {
    initRoute();
    Packet q;
    ethernetFrame r;
    while (true) {
        unique_lock<mutex> lock(eth0_mutex);
        cv.wait(lock, [] { return frame_ready; });

        if (eth0.status) {
            cout << "Router: 收到以太帧 from " << eth0.buffer.sourMAC << " to " << eth0.buffer.desMAC << endl;
            q = eth0.buffer.data;
            r = eth0.buffer;
                if (eth0.buffer.desMAC == "FF-FF-FF-FF-FF-FF") {
                    cout << "Router: 交换机泛洪发送的帧，丢弃." << endl;
                } else if (q.checksum != getChecksum(q)) {
                    cout << "Router: error : 校验和错误，数据可能发生了损坏." << endl;
                    //showMessage(r);
                } else if (q.ver == "6") {
                    cout << "Router: error : 不支持的协议类型." << endl;
                    //showMessage(r);
                } else if (q.TTL <= 1) {
                    cout << "Router: error : TTL 超时" << endl;
                    sendICMP(1,q);
                    //showMessage(r);
                }  else {
                    q.TTL--;
                    r.data = q;
                    string nextHopIP = lookupRoute(q.desIP);
                    if (nextHopIP.empty()) {
                        cout << "Router: 无法找到 " << q.desIP << " 对应的路由" << endl;
                        sendICMP(2,q);
                    } else {
                        string nextHopMAC = lookupARP(nextHopIP);
                        RARPTable.push_back({ r.sourMAC, q.sourIP });
                        if (r.desMAC == MACR0 && q.desIP == IPR0){// 检查数据包的目的MAC地址和目的IP地址是否为路由器接口
                            cout << "Router: 目的地址是路由器接口，处理数据包" << endl;
                            showMessage(r);
                        } else if (!nextHopMAC.empty()) {
                            sendEthernet(q, nextHopMAC);
                            cout << "已转发到 " << q.desIP << " 对应的主机" << endl;
                            //showMessage(r);
                        } else {
                            cout << "Router: 未找到 " << nextHopIP << " 对应的MAC地址" << endl;
                            sendARPRequest(nextHopIP);
                            string arp = getARP();
                            RARPTable.push_back({ arp, nextHopIP });
                            sendEthernet(q,arp);
                            showMessage(r);
                        }
                    }
                }
            eth0.status = false;
            frame_ready = false;
        }
    }

}

int main() {
    thread hostA(hostA_thread);
    thread router(router_thread);

    hostA.join();
    router.join();

    return 0;
}

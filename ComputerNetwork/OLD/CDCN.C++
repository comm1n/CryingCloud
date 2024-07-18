#include <iostream>
#include <string>
#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>
#include "def.h"

using namespace std;

interface eth0 = { ethernetFrame(), false };//定义一个接口 eth0，本次实验实际上不需要实现eth1端口
ethernetFrame Frame;
class ARPFrame ARPFrame;
vector<ARP> aARPTable;//A的ARP表，动态数组
vector<ARP> RARPTable;//路由器ARP表，初始为空
vector<routingTable> RoutingTable;//路由表
mutex eth0_mutex;//端口eth0的互斥访问
condition_variable cv;
bool frame_ready = false;


// 获取报文部分数据的校验和
uint16_t getChecksum(const Packet& p) {
    int length = p.length;
    int sum = 0;
    int i = 0;

    // 累加每两个字节的值
    while (length > 1) {
        sum += ((p.ipdata[i] << 8) & 0xFF00) | (p.ipdata[i + 1] & 0xFF);
        i += 2;
        length -= 2;
    }

    // 如果有剩余一个字节，累加它的值
    if (length > 0) {
        sum += (p.ipdata[i] << 8) & 0xFF00;
    }

    // 将高位字加到低位字
    sum = (sum >> 16) + (sum & 0xFFFF);
    sum += (sum >> 16);

    // 返回按位取反的结果
    return static_cast<uint16_t>(~sum);
}

// 初始化用到的数据
void initA(ethernetFrame& Frame) {
    //ethernet头部
    Frame.desMAC = "";//目的MAC0
    Frame.sourMAC = MACA;//源MAC: A

    //ip数据报头部
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
    RoutingTable.push_back({ "0.0.0.0", "0.0.0.0", IPR1 }); // 缺省
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
void sendEthernet(ethernetFrame p, const string& MACaddress) {
    {
        lock_guard<mutex> lock(eth0_mutex);//访问端口
        p.desMAC = MACaddress;
        eth0.buffer = p;
        eth0.status = true;
        frame_ready = true;
    }
    cv.notify_one();

    /*
    cout << "Sending Ethernet Frame:" << endl;
    cout << "Source MAC: " << p.sourMAC << endl;
    cout << "Destination MAC: " << p.desMAC << endl;
    cout << "Checksum: " << p.checksum << endl;
    cout << "IP Version: " << p.data.ver << endl;
    cout << "TTL: " << p.data.TTL << endl;
    cout << "Source IP: " << p.data.sourIP << endl;
    cout << "Destination IP: " << p.data.desIP << endl;
    cout << "Data: " << p.data.ipdata << endl;
    */
}

//模拟泛洪
void sendToEth1(ethernetFrame p,string add)
{
    cout << "已将目的地为" << p.desMAC << "的数据包泛洪"<<endl;
}
//模拟ARP请求报文发送
void sendARPRequest(string IP)
{
    class ARPFrame p = ARPFrame;
    //初始化ARP请求报文
    p.desMAC = "FF-FF-FF-FF-FF-FF";//目的Mac，广播
    p.srcMAC = MACR0;//源MAC: 路由
    p.type ="0x0806";//arp请求与应答 类型
    p.hardwareType = 1;//ethernet类型
    p.e = "0x0800";//ipv4
    //ARPFrame.f 代表长度
    p.g = 1;//表示进行arp请求
    p.h = "00-00-00-00-00-00";//请求的mac地址，此时还不知道
    p.i = IP;//目的端

    cout << "发送ARP请求报文" << endl;
    cout << p.desMAC <<"\n"
         << p.srcMAC<<"\n"
         << p.type<<"\n"
         << p.hardwareType<<"\n"
         << p.e<<"\n"
         << p.g<<"\n"
         << p.h<<"\n"
         << p.i<<"\n"
         << endl;
}

//模拟收到了ARP报文
string getARP ()
{
    class ARPFrame p = ARPFrame;
    p.desMAC = "FF-FF-FF-FF-FF-FF";//目的Mac，广播
    p.srcMAC = MACR0;//源MAC: 路由
    p.type = "0x0806";//arp请求与应答 类型
    p.hardwareType = 1;//ethernet类型
    p.e = "0x0800";//ipv4
    //ARPFrame.f 代表长度
    p.g = 1;//表示进行arp请求
    p.h = "00-00-00-00-00-00";//请求的mac地址，假设这里由某一主机返回了该地址
    cout<< "收到ARP回应报文,加入ARP表"<<endl;
    return p.h;
}

// 测试函数
void testFunction(int i) 
{
    ethernetFrame p = Frame;
    switch (i) {
        case 1: {// 主机 A 发送以太网帧给主机 B（假设交换机泛洪该分组）
            p.data.desIP = IPB;
            sendEthernet(p, MACB);
            p = Frame;
            break;}
        case 2: {// 主机 A 发送以太网帧给路由器
            p.data.desIP = IPR0;
            sendEthernet(p, MACR0);
            p = Frame;
            break;}
        case 3: {// 主机A 发送以太网帧，目的IP 为138.76.29.7，目的MAC 为E6-E9-00-17-BB-4B;
            p.data.desIP = "138.76.29.7";
            sendEthernet(p, "E6-E9-00-17-BB-4B");
            p = Frame;
            break;}
        case 4: {// 主机 A 发送以太网帧给主机 C;
            p.data.desIP = IPC;
            sendEthernet(p, MACC);
            p = Frame;
            break;}
        case 5: {// 主机 A 发送以太网帧给主机 C，但封装的是 IPv6 分组，目的 IP 为主机 C 地址的 IPv6 表示（::222.222.222.221，前 96 位为 0）；
            p.data.ver = "ipv6";
            p.data.desIP = "::222.222.222.221";
            sendEthernet(p, MACC);
            p = Frame;
            break;}
        case 6: {// 主机 A 发送以太网帧给主机 C，但校验和错误；
            p.data.desIP = IPC;
            p.data.checksum = 0;
            sendEthernet(p, MACC);
            p = Frame;
            break;}
        case 7: {//A->C,BUT ttl = 1
            p.data.desIP = IPC;
            p.data.TTL = 1;
            sendEthernet(p, MACC);
            p = Frame;
            break;}
        default:{
            cout << "指令不存在" << endl;
            break;}
    }
}

void showMessage(ethernetFrame p)
{
    cout << p.sourMAC <<"\n"
         << p.desMAC <<"\n"
         << p.data.ver <<"\n"
         << p.data.TTL <<"\n"
         << p.data.desIP <<"\n"
         << p.data.sourIP <<"\n"
         << p.data.length <<"\n"
         << p.data.checksum <<"\n"
         << endl;
}
// 主机A线程函数
void hostA_thread() {
    initA(Frame);
    initAARP();
    initRoute();

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
    Packet q;
    ethernetFrame r;
    while (true) {
        unique_lock<mutex> lock(eth0_mutex);
        cv.wait(lock, [] { return frame_ready; });

        if (eth0.status) {//接口内收到了信息
            cout << "Router: 收到以太帧 from " << eth0.buffer.sourMAC << " to " << eth0.buffer.desMAC << endl;
            q = eth0.buffer.data;//提取ip数据报
            r = eth0.buffer;
            if (q.TTL <= 1) {//7:ttl = 1
                cout << "Router: error : TTL 超时, 返回 ICMP 报文" << endl;
                showMessage(r);
                
            } else if(q.ver == "ipv6"){//5:ver = ipv6
                cout << "Router: error : 不支持的协议类型." << endl;
                showMessage(r);
             
            } else if(q.checksum != getChecksum(q)){//6: checksunerror
                //cout << getChecksum(q) <<endl;
                cout << "Router: error : 校验和错误，数据可能发生了损坏." << endl;
                showMessage(r);
 
            } else{//信息没有错误，查询ARP表获取mac地址并转发
                q.TTL--;
                string nextHopMAC = lookupARP(q.desIP);//从ARP中获取对应的MAC
                RARPTable.push_back({r.sourMAC , q.sourIP});//自学习
                RARPTable.push_back({r.desMAC , q.desIP});
                if(q.desIP == IPB){
                    sendToEth1(eth0.buffer, MACR1);
                } else if (!nextHopMAC.empty()) {
                    sendEthernet(eth0.buffer, nextHopMAC);//发送到对应的主机
                    cout<< "已转发到" << q.desIP << "对应的主机" <<endl;
                    showMessage(r);
                } else {
                    cout << "Router: 未找到 " << q.desIP << " 对应的MAC地址"
                    << endl;
                    string desipadd = q.desIP;
                    sendARPRequest(desipadd);//模拟ARP请求
                    RARPTable.push_back({getARP() ,desipadd });//将收到的MAC地址与对应的ip地址压入ARP表
                    showMessage(r);
                }
            }
            eth0.status = false;
            frame_ready = false;
        }
    }
}

int main() {
    // 创建并启动主机A线程和路由器线程
    thread hostA(hostA_thread);
    thread router(router_thread);

    // 等待两个线程结束
    hostA.join();
    router.join();

    return 0;
}

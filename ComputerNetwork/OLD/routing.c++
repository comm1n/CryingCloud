#include <bits/stdc++.h>
#include "def.h"

using namespace std;

// 定义路由表
vector<ARP> ARPTable;

// 定义接口

// 初始化路由表
void init()
{   
    // 添加一些静态的 ARP 表条目
    ARPTable.push_back({ MACA, IPA });
    ARPTable.push_back({ MACB, IPB });
    ARPTable.push_back({ MACR0, IPR0 });
}

// 处理以太网帧，发送给网络层继续处理
Packet dataLink(etherFrame p)
{
    // 输出日志
    cout << "Received Ethernet Frame:" << endl;
    cout << "Source MAC: " << p.sourMAC << endl;
    cout << "Destination MAC: " << p.desMAC << endl;
    cout << "Checksum: " << p.checksum << endl;
    cout << "IP Version: " << p.data.ver << endl;
    cout << "TTL: " << p.data.TTL << endl;
    cout << "Source IP: " << p.data.sourIP << endl;
    cout << "Destination IP: " << p.data.desIP << endl;
    cout << "Data: " << p.data.ipdata << endl;

    // 返回一个IP数据包
    return p.data;
}

// 判断TTL；
bool isTTLValid(Packet& p)
{
    p.TTL--;
    return p.TTL > 0;
}

// 网络层处理函数
void network(Packet& p)
{
    // 处理IP数据报
    if (p.ver == "ipv6")
    {
        // 1.判断协议类型，不处理IPv6内容
        cout << "错误：无法处理IPv6协议" << endl;
        return;
    }

    if (!isTTLValid(p))
    {
        cout << "警告：接收包的TTL为0" << endl;
        // 发送 ICMP 包
        // sendICMP(..., p.sourIP)
        return;
    }

    // 2.根据数据进行相应操作
    // 这里根据具体数据进行处理，可以是转发、丢弃等

    // 3.处理转发
    // 4.根据下一跳IP查找ARP并操作，包括ARP更新
    // 5.组装新帧
    // 假设我们要将包转发给某个IP
    /*string nextHopIP = "111.111.111.112"; // 这是一个示例，实际应该根据路由表查找
    string nextHopMAC = "";

    for (const auto& entry : ARPTable)
    {
        if (entry.IPaddress == nextHopIP)
        {
            nextHopMAC = entry.MACaddress;
            break;
        }
    }

    if (nextHopMAC.empty())
    {
        cout << "错误：未找到下一跳MAC地址" << endl;
        return;
    }

    // 组装新的帧
    etherFrame newFrame;
    newFrame.sourMAC = MACA; // 设置源MAC地址
    newFrame.desMAC = nextHopMAC; // 设置目的MAC地址
    newFrame.data = p; // 将数据包放入帧中
    newFrame.checksum = "0"; // 计算校验和，示例中直接设置为0

    // 假设发送到eth0
    eth0.buffer = newFrame;
    cout << "发送新帧到eth0" << endl;*/
}

int main()
{
    init(); // 初始化路由表
    Packet p;

    // 模拟数据接收
    while (1)
    {
        if (eth0.status)
        {
            // 处理接收到的以太网帧
            p = dataLink(eth0.buffer);
            // 处理网络层数据
            network(p);
        }
          cout << "Received Ethernet Frame:" << endl;
    cout << "Source MAC: " << eth0.buffer.sourMAC << endl;
    cout << "Destination MAC: " << eth0.buffer.desMAC << endl;
    cout << "Checksum: " << eth0.buffer.checksum << endl;
    cout << "IP Version: " << eth0.buffer.data.ver << endl;
    cout << "TTL: " << eth0.buffer.data.TTL << endl;
    cout << "Source IP: " << eth0.buffer.data.sourIP << endl;
    cout << "Destination IP: " << eth0.buffer.data.desIP << endl;
    cout << "Data: " << eth0.buffer.data.ipdata << endl;
        // 等待50毫秒
        this_thread::sleep_for(std::chrono::milliseconds(50));
        system("pause");
    }

    return 0;
}

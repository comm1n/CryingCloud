#include <iostream>
#include <string>
#include <vector>
#include "def.h"

using namespace std;

interface eth0 = { etherFrame(), false } ;//模拟主机与eth0端口的以太网连接
etherFrame Frame;
vector<ARP> ARPTable;

// 初始化测试帧
void init(etherFrame& Frame) {
    Frame.desMAC = " ";
    Frame.sourMAC = MACA;
    Frame.checksum = "0";
    Frame.data.ver = "ipv4";
    Frame.data.TTL = 255;
    Frame.data.desIP = " ";
    Frame.data.sourIP = IPA;
    Frame.data.ipdata = "Thequickbrownfoxjumpsoveralazydog";
}


// 初始化ARP
void initARP() {
    ARPTable.push_back({ MACA, IPA }); // 本机
    ARPTable.push_back({ MACB, IPB }); // B机
    ARPTable.push_back({ MACR0, IPR0 }); // 网关
}

// 发送以太网帧
void sendEthernet(etherFrame p, string MACaddress) {
    /*bool found = false;
    for (int i = 0; i < ARPTable.size(); i++) {
        // 如果在ARP中找到了该机MAC地址
        if (ARPTable[i].MACaddress == MACaddress) {
            p.desMAC = MACaddress;
            found = true;
            break;
        }
    }
    // 没找到，发送到路由
    if (!found) {
        p.desMAC = MACR0;
        
    }*/
    eth0.buffer = p; // 发送到路由的eth0端口
    // 输出发送的以太网帧信息
    cout << "Sending Ethernet Frame:" << endl;
    cout << "Source MAC: " << p.sourMAC << endl;
    cout << "Destination MAC: " << p.desMAC << endl;
    cout << "Checksum: " << p.checksum << endl;
    cout << "IP Version: " << p.data.ver << endl;
    cout << "TTL: " << p.data.TTL << endl;
    cout << "Source IP: " << p.data.sourIP << endl;
    cout << "Destination IP: " << p.data.desIP << endl;
    cout << "Data: " << p.data.ipdata << endl;

 
}

void testFunction(int i) {
    switch (i) {
    case 1: // 主机 A 发送以太网帧给主机 B（假设交换机泛洪该分组）；
        sendEthernet(Frame, MACB);
        eth0.status = 1;
        break;
    case 2: // 主机 A 发送以太网帧给路由器；
        sendEthernet(Frame, MACR0);
        break;
    case 3: // 主机A 发送以太网帧，目的IP 为138.76.29.7，目的MAC 为E6-E9-00-17-BB-4B;
        Frame.data.desIP = "138.76.29.7";
        sendEthernet(Frame, "E6-E9-00-17-BB-4B");
        break;
    case 4: // 主机 A 发送以太网帧给主机 C；
        sendEthernet(Frame, MACC);
        break;
    case 5: // 主机 A 发送以太网帧给主机 C，但封装的是 IPv6 分组，目的 IP 为主机 C 地址的 IPv6 表示（::222.222.222.221，前 96 位为 0）；
        Frame.data.ver = "ipv6";
        Frame.data.desIP = "::222.222.222.221";
        sendEthernet(Frame, MACC);
        break;
    case 6: // 主机 A 发送以太网帧给主机 C，但校验和错误；
        Frame.checksum = "error";
        sendEthernet(Frame, MACC);
        break;
    case 7: // 主机 A 发送以太网帧给主机 C，但 TTL 值为 1。
        Frame.data.TTL = 1;
        sendEthernet(Frame, MACC);
        break;
    default:
        cout << "Invalid command!" << endl;
        break;
    }
}

int main() {
    init(Frame);
    initARP();
    int command = -1;
    cout << "Please choose a number to send test frame.\n1: Send to host B\n2: Send to router\n3: Send to IP 138.76.29.7 with MAC E6-E9-00-17-BB-4B\n4: Send to host C\n5: Send to host C with IPv6\n6: Send to host C with checksum error\n7: Send to host C with TTL=1\n8: Exit testing system\n";
    while (true) {
        cin >> command;
        if (command < 1 || command > 7) {
            cout << "Invalid command! Exiting..." << endl;
            break;
        }
        testFunction(command);
        cout << "Ethernet Frame:" << endl;
    cout << "Source MAC: " << eth0.buffer.sourMAC << endl;
    cout << "Destination MAC: " << eth0.buffer.desMAC << endl;
    cout << "Checksum: " << eth0.buffer.checksum << endl;
    cout << "IP Version: " << eth0.buffer.data.ver << endl;
    cout << "TTL: " << eth0.buffer.data.TTL << endl;
    cout << "Source IP: " << eth0.buffer.data.sourIP << endl;
    cout << "Destination IP: " << eth0.buffer.data.desIP << endl;
    cout << "Data: " << eth0.buffer.data.ipdata << endl;
    cout << eth0.status << endl;
    }
    return 0;
}

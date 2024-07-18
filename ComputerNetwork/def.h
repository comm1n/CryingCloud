#ifndef MYDEF_H
#define MYDEF_H

#include <string> // 包含 string 头文件

using namespace std;

#define MACA "74-29-9C-E8-FF-55"
#define MACB "CC-49-DE-D0-AB-7D"
#define MACR0 "E6-E9-00-17-BB-4B"
#define MACR1 "1A-23-F9-CD-06-9B"
#define MACC "88-B2-2F-54-1A-0F"
#define MACD "49-BD-D2-C7-56-2A"
#define IPA "111.111.111.111"
#define IPB "111.111.111.112"
#define IPR0 "111.111.111.110"
#define IPR1 "222.222.222.220"
#define IPC "222.222.222.221"
#define IPD "222.222.222.222"

//ipv4数据报
struct Packet
{
    string ver;//模拟四位版本号，区分ipv4和ipv6
    int length;//消息长度，这里只计算了不包含首部的数据长度
    string sign;//标志，3位，标记分片发送时的终点分组
    string displacement;//片位移,确定分片发送时该片在原分组的位置
    string protocol;//所载报文协议类型
    int TTL;//TTL
    uint16_t checksum;//校验
    string desIP;//目的ip
    string sourIP;//源ip
    string ipdata;//其中的数据

    //ipv6报头新增加的字段
    string trafficClass;//通信量类
    string nextHeader;//下一个首部，用于扩展头部，没有扩展时与上面的procotol用法一致
};

//以太网帧
struct ethernetFrame
{
    string desMAC;//目的MAC地址
    string sourMAC;//源MAC地址
    Packet data;//ipv4数据报
};

//ARP报文
struct ARPFrame
{
    string desMAC;//目的MAC地址
    string srcMAC;//源MAC地址
    string type;//帧类型
    string hardwareType;//硬件类型
    string e;//需要映射的协议类型
    string f;//硬件地址长度与协议地址长度
    string g;//操作类型字段
    string h;//发送端ARP请求或应答的硬件地址，这里是以太网地址
    string i;//发送ARP请求或应答的IP地址
    string j;//目的端的硬件地址和IP地址
};

//ICMP报文
struct ICMPHeader {
    uint8_t type;          // 类型
    uint8_t code;          // 代码
    uint16_t checksum;     // 校验和
    uint16_t data;   // 标识符

};

//定义路由接口
struct interface
{
    ethernetFrame buffer;
    bool status;
};

//定义路由表
struct routingTable
{
    string NetworkID;
    string subnetMask;
    string nextHop;
};

// ARP表
typedef struct ARP
{
    string MACaddress;   // MAC地址
    string IPaddress;    // ip地址
} ARP;

#endif
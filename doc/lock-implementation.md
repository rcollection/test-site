# Background
锁在人类的自然语言中的含义，表示一种安保装置，引申为封闭。主要目的是为了保护防止丢失。
和人类语言中的含义不同，在计算机中，锁的含义是为了避免竞争，简单说，在人类语言中，计算中锁的含义就像法官，用来决断一个物品应该归属谁。
显然，如果多方同时宣布拥有一个物品，那么后果无法预测，带来混乱。
本文简单从几个层面上，介绍一下计算机系统中锁的具体实现。
计算机锁的基本含义：需要锁的机制来排他的占用某个物品。

# Lock implementation in Hardware
在物理层面，锁是如何实现的？
电路层面，需要设计电信号协议来保证资源的排他性。这里举2个例子：
1. 一种简单的实现是忙信号，一个元件有一个信号端，表示该元件是否可以用，如果是高电位，就说明元件忙，需要等待；如果是低电位，说明元件空闲，可以使用；
2. 较复杂的多方竞争的场景，例如对数据总线的占用，这个时候需要引入仲裁电路，由仲裁逻辑判定多个竞争者谁享有资源的占用。
电路层面另一种更常见的广泛意义上的锁是同步，有2中常见的形式：
1. 基于节拍的协议：双方拥有同样的时钟频率，在同一个节拍下同步通信；
2. 基于编码的协议：双方约定数字信号协议，信息编码在电信号中，比如：连续4个高电位表示传输的开始。

# Lock implementation in CPU (instruction)
理论上，可以在CPU的硬件里设计支持直观意义上的锁硬件。比如，当多个CPU核同时发起锁的需求是，由硬件仲裁元件决定哪一个核获得锁。
实际上，没有必要做到如此强的锁，浪费资源，因为软件结构层面的一些约束会让软件层面的锁更简单，当然这个软件层面的锁还是需要硬件支持的。
那么这个"软件结构的约束"是什么呢？就是冯诺伊曼计算机结构：程序的代码和数据都存放在内存中。不管现代计算有多少处理器，有多少内存，在程序逻辑层面，内存空间只有一个，所有的东西（运行的程序）都放在内存。
这是一个很强的设定，既然有了唯一的地方，就可以用来是实现锁：如果你对一个内存地址独占里，那么就是一把锁。
CPU里面一般又这样的指令来支持这个弱化的锁操作：test and set（https://en.wikipedia.org/wiki/Test-and-set）
这个指令的含义是：不可中断的、原子的设定一个内存的值，并返回这个内存地址设定前的旧值。
如何用这个指令实现锁？
```
function Lock(addr){
  while(test_and_set(addr, 1) == 1);
}
function Unlock(addr){
  if(test_and_set(addr, 0) == 0){
    error("unlock more than once")
  }
}
```
因为`test_and_set`是原子的，所以所有`test_and_set`都是串行执行的。假设addr初始化为0。
1. 多方竞争锁，都调用Lock，除了第一次会返回0外，其他全部调用都返回1。返回0表示成功获得锁，否则表示失败，所以需要一直获取锁，这就是"自旋锁。也可以实现为如果失败，就告诉操作系统阻塞线程，就是我们编程意义层面的最基本的锁了。虽然后续所有的test_and_set都执行了，并且把内存地址设置为1，但是因为第一条指令已经把内存设置为1了，所以可以理解为内存变成1后，状态不变了。
2. 在获得锁的线程调用Unlock之前，所有其他线程都没法获得锁。当unlock的时候，把addr设置为0。如果之前已经上锁了，那么这次test_and_set返回就是1，表示锁释放成功，addr的值又恢复到了初始化的空闲状态，值为0。如果返回值是0，说明这个addr地址已经被释放过了，显然多了多次unlock，这应该是一个错误。

其实更加通用、更加好理解的指令是：compare and swap（https://en.wikipedia.org/wiki/Compare-and-swap）

有了这个排他的锁之后，很多其他的锁变种都可以实现。

# Lock implementation in Operating-System
为什么操作系统层面需要设计锁接口呢？
其实，有了机器码级别的锁指令支持，就可以在用户空间里实现各种锁，而不需要使用任何操作系统的资源，也就不需要内核调用，性能上可以更好。
确实，如果在用户态使用锁，性能显然比内核调用要高。所以，确实，有一些锁是是实现在用户态的，不走操作系统内核调用，比如自旋锁。但是这样有个问题，如果线程获得不到锁，就会一直死循环等待，如果时间特别长，会导致极大的浪费CPU资源。
所以一个常见的想法是，如果是没有得到锁，就挂起线程。显然线程的挂起，是需要操作系统支持的，所以也就需要内核调用。所以我们编程常见的重锁，阻塞锁，都涉及到操作系统调用。
所以操作系统会在锁指令的基础之上，封装一些编程规范的锁。当然本身锁指令也是开放使用的。

# Lock implementation in SDK(api)
理论上，锁API是可以作为SDK发布的，因为锁指令本身并不需要操作系统的支持。所以，有机会，可以把一些同步互斥的操作抽象成一些锁的API，以SDK的方法发布。
那么在编程层面，有哪些抽象的锁的形式呢？
一下是我本人的理解：
从同步互斥的概念出发，同步主要依赖：信号量（semaphore）；互斥主要依赖：锁（lock）
在实际的应用编程层面，互斥是主要的场景。而信号量和锁本质上可以统一成一个概念，信号量可以是锁多次的锁，锁可以是大小为1的信号量。

我们从实际的编程SDK中去找找锁的定义：
1. 在C++11中引入了\<mutex>头文件，只提供了mutex锁作为互斥手段，别无其他。
2. 在java jdk concurrent中，提供了基础的信号量Semaphore，锁ReentrantLock（CountDownLatch、CyclicBarrier这2个更高级）；在java语言层面提供了synchronized、wait、notify、notifyall的抽象。
从源码分析，发现也是在锁指令上和操作系统线程管理上封装的一些同步互斥量。最基础的还是锁指令。

从上面可以看出，一旦一个系统提供了lock的基本定义，上面的全部同步互斥量都可以实现。

# Lock implementation in distributed-system
在一个计算机单机系统内部，有一个统一的唯一的内存定义，从而保证可以构建排他的锁。那么在多机之间，如何获取锁呢？
显然，这样的场景下，依靠计算机硬件是不可能的，那么就需要纯的软件锁，怎么实现呢？
说回来，如果可以实现分布式场景下的纯软件锁，理论上在一个单机内部也可以实现这么一个纯软件锁。
参考zookeeper
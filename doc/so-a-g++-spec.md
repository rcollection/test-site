# g++的一些特性说明
g++的分为编译和链接共2个阶段。编译阶段一般比较好理解，也很少有问题。针对链接阶段，这里简单说明一下链接时的常用概念和一些问题。
g++编译完毕后，生成一些.o文件。o文件中包括这cpp文件（一个cpp对应一个o文件）的代码、数据、符号，符号包括导入和导出的符号。
比如一个cpp文件使用了xml的一个函数调用，因为cpp文件本身没有这个函数的代码，所以在生成的o文件中会说明，调用了一个外部的函数。等到链接阶段的时候，再解决这个外部调用的函数地址问题。
链接阶段可以简单理解为把一些o文件合并在一起，生成可执行文件或者库文件。这个阶段包括地址和空间的分配、符号的解析和重定位2个部分。前者完成程序段地址的分配和符号的索引；后者完成符号的解析和替换，最终代码中就是对地址的引用。
链接又分为动态链接和静态链接，会生成三类文件：so文件，a文件和可执行文件（linux就是elf可执行文件）
动态链接和静态链接决定了**链接过程**需要执行的动作和程序**启动过程**中需要执行的动作，而不仅仅是链接过程。下面简单解释下。
## 静态链接
静态链接是比较简单的，就是在链接过程完成全部符号的解析和重定位，最后生成一个可执行的文件。这个文件生成后，任何地方都可以运行（当然需要在同样的操作系统下），不依赖于任何其他包。
典型的g++静态链接例子：
```
g++ -o test main.cpp tools.cpp
g++ -o hello hello.o tools.o
```
第一条命令，会编译main.cpp和tools.cpp为o文件，再把o文件静态链接成test可执行文件。-o选项含义：指定生成的文件名
第二条命令，把已经编译好的o文件静态链接成hello可执行文件。

静态链接的好处是，一处链接，处处执行；坏处是文件比较大，每次上游依赖的library的升级都要重新链接后，才能完成程序的升级。因此，就有了动态链接。

## 动态链接
动态链接的意思是，在链接的过程中，并不真正解析和重定位外部符号，而是在程序加载的时候（程序启动的第一步是加载，然后才是执行）才去解析和重定位符号。
典型的g++动态链接的例子：
```
g++ -o test main.cpp -L/usr/lib/ -ljson
g++ -o hello hello.o -L/usr/lib/ -ljson
```
第一条命令，会编译main.cpp为o文件，再把o文件静态链接、json库文件动态链接，生成test可执行文件。-L含义：指定链接的library目录；-l含义：一定链接的库名字
第二条命令，把已经编译好的o文件静态链接成hello可执行文件。

这个时候生成的test和hello并不能独立执行，要不然会在执行的时候报错：`error while loading shared libraries:xxx.so`
因为选择了动态链接json库，所以test文件中并没有解析json的符号，也就代表了选择了在文件执行的时候，再去动态的解析json库文件。如果你不告诉操作系统json库在什么地方，操作系统的加载起会报错，说找不到动态库文件。
怎么办呢？很简单，把json库的路径加入到`LD_LIBRARY_PATH`环境变量中，就可以了。

## 站在库的角度，理解静态链接和动态链接
上面是站在可执行文件的角度，理解动态链接和静态链接。这里介绍下，如何生成静态库和动态库。
### 生成静态库
如果你有全部的源码文件，那么你不需要静态库，直接用g++链接就可以了。
但是很多时候，你没法拥有全部的源码文件，比如隔壁team不愿意把他们的源码提供给你们用，怎么办？
没有源码可以静态链接。因为其实链接不需要源码，只需要o文件就好了。Done，隔壁team只需要把o文件给我，我就可以静态链接了。但是这里有个问题，隔壁team有1000个cpp文件，于是就有了1000个o文件，虽然有了o文件，但是文件太多，操作起来很繁杂。
于是静态库文件应运而生：把全部的o文件打包成一个文件，就是库文件，就是.a文件。
其实a文件的本质就是打包，所以甚至你可以把各种乱七八糟的文件都打到a文件中，也没关系，比如你可以把txt文件打包到a文件中。
所以，自然的，如下a文件的生成也就很自然了：
```
ar cr libtools.a tools.o readme.txt conf.json
ar cr libtools.a tools.o myjson.o
```
ar就是用来打包的程序。c含义：创建a文件；r含义：replace a文件中的同名冲突文件
第一个命令，把toos.o readme.txt conf.json三个文件打包到libtools.a文件。
第二个命令，把toos.o myjson.o二个文件打包到libtools.a文件。
虽然ar允许，但是一般而言，我们只会把o文件打包到a文件中。

那么a文件如何使用呢？如下：
`g++ -o hello hello.o -L./lib -ltools` 怎么静态链接静态库和动态链接动态库一样呢？
其实是不一样的，g++看到-ltools后，会优先使用动态链接方法去链接动态库，也就是说，会去找libtools.so做动态链接。
如果这个文件不存在，就会转而使用静态链接方法去静态链接静态库，也就是说，会去找libtools.a文件。如果还找不到，就报错。
如果刚好目录下这两个文件都存在，那么先找到的先使用，也就是会用动态库做动态链接。
可以打开-v开关后，看到库的加载过程：`g++ -o hello hello.o -Wl,--verbose -L./lib -ltools`，-Wl的意思是这个是链接的参数，--verbos含义：打印详细的信息。
如果想要强制指定静态链接，可以这么做：
`g++ -o hello hello.o -L./lib -Wl,-Bstatic -ltools -Wl,-Bdynamic`，-Bstatic指定，这个参数后面的-l指定的库强制采用静态链接，如果找不到a文件，就报错。最后的一个-Bdynamic指定这个后面的库都用动态链接（很蠢是吧，后面什么都没有了）。不加这条语句会报错，虽然这是最后一个命令，但是g++会把一些标准库加载命令的最后面，而这些库只提供动态链接，所以如果不加这句话，就会报错。

### 生成动态库
动态库的生成比较简单：
```
g++ -share -fPIC -o libtools.so tools.c myjson.c -lxml
```
-fPIC的含义是：使用PIC技术生成的代码才能生成动态库。
命令的含义：从tools.c和myjson.c源文件，链接xml库，生成libtools.so动态库。
当然这里是动态链接xml还是静态链接xml，就看g++能找到什么。这里不再赘述。

### 特别说明
1. g++在链接的时候，命令行的库顺序是有关系的。因为g++进行了链接优化，如果发现一个依赖库没有被前面的库（就是命令行左边的库、或者文件）用到，就会丢弃，这会导致后面的（命令行右边的）库找不到定义。在生成可执行文件的时候，如果有这样的错误，总是会报出来；但是生成动态库的时候，这个错误只有到运行的时候才会暴露出来，比较隐晦。
1. 链接时，静态链接只能链接静态库，如果想要下游静态链接你的代码，你必须发布静态库
1. 链接时，动态链接可以依赖于静态库，比如没有so文件，但是有a文件，是可以链接通过的（但是静态库的代码并没有整合到输出文件），但是执行的时候仍然需要动态库，否则会出现undefined symbol错误。
1. 链接选项的含义，不区分生成的文件是库文件还是可执行文件，都是一致的。例如：可以静态链接静态库，生成可执行文件或者动态库；也可以使用动态链接动态库，生成可执行文件或者动态库。
1. 在生成动态库的过程中，虽然可以不指定这个动态库的动态依赖A（动态链接的动态库就是动态依赖）而可以链接通过，但是这会导致这个动态库的库信息缺失对A的依赖，当下游使用这个动态库的时候（g++ -l选项中中不加入依赖库A）会导致undefined symbol错误；但是如果动态库中有了依赖库A的依赖信息，那么下游使用的时候报错的就是依赖库A的so找不到的错误。
1. 注意，虽然动态链接的动态库的代码没有进入到最后的目标文件中，但是仍然是必须要写的，用来在生成可执行文件的时候解析符号。
1. ar命令只负责打包文件，不做其他的，与上游依赖无关。
1. 静态库文件不会递归依赖，a文件中的a文件是不会解析的，作为无关文件忽略掉。
1. 默认，g++优先动态链接-l库，找不到再静态链接，还找不到，就报错。
1. so文件可以打包到a文件中，当作动态链接，类似于动态链接的-l选项

## 二进制分析工具
常用的有四个工具：

1. nm:分析文件中的符号和地址
1. readelf：分析elf可执行文件中的信息
1. objdump：分析文件中的符号信息
1. ldd：查看文件所需要的动态链接库

### 一个实验：展示动态库依赖动态库
生成f1动态库和静态库：(这两个文件在f1目录下)
```
// f1.h
int add(int x, int y);

// f1.c
#include "f1.h"
#include <stdio.h>
int add(int x, int y){
	return x+y;
}

// build libf1.so
$ g++ -shared -fPIC f1.c  -o libf1.so 

// build libf1.a
$ g++ -fPIC  -c f1.c 
$ ar cqs libf1.a f1.o
```
生成f2的动态库：
```
// f2.h 
float score(int, int);

// f2.c
#include "f2.h"
#include "f1/f1.h"
float score(int x, int y){
	int z = add(x,y);
	return z/100.0;
}

// main.c
#include "f2.h"
#include <stdio.h>
int main(){
	printf("score:%f", score(11,22));
}
```
1，动态链接f1
```
// build libf2.so 
$ g++ -shared -fPIC f2.c -o libf2.dynamic.so -Lf1 -lf1

// link to executable file without libf1.so, got error
$ g++ -o test main.c -L. -lf2.dynamic
/usr/bin/ld: warning: libf1.so, needed by ./libf2.dynamic.so, not found (try using -rpath or -rpath-link)
./libf2.dynamic.so: undefined reference to `add(int, int)'
collect2: ld returned 1 exit status

// link to executable file with libf1.so
$ g++ -o test main.c -L. -lf2.dynamic -Lf1 -lf1
$./test
./test: error while loading shared libraries: libf2.dynamic.so: cannot open shared object file: No such file or directory
$ export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.:f1
$./test
score:0.330000
```

2，静态链接f1
```
// build libf2.so 
$ g++ -shared -fPIC f2.c -o libf2.static.so -Lf1 -Wl,-Bstatic -lf1 -Wl,-Bdynamic

// link to executable file without libf1.a
$ g++ -o test main.c -L. -lf2.static
$./test
./test: error while loading shared libraries: libf2.static.so: cannot open shared object file: No such file or directory
$ export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.
$./test
score:0.330000
```

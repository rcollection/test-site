#include <stdio.h>
#include <unistd.h>
#include "org_rx_test_tools_CPUCounter.h"

inline unsigned long long rdtsc(void)
{
    unsigned int hi, lo;
	__asm__ __volatile__ ("rdtsc" : "=a"(lo), "=d"(hi));
    return ( (unsigned long long)lo)|( ((unsigned long long)hi)<<32 );
}

JNIEXPORT jlong JNICALL Java_org_rx_test_tools_CPUCounter_count
  (JNIEnv *, jobject)
{
	return rdtsc();
}

int main(){
	while(true){
		unsigned long long t = rdtsc();
		printf("t:%llu\n", t);
		sleep(1);
	}
	return 0;
}

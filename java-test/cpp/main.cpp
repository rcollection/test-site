#include <stdio.h>
#include <unistd.h>
typedef unsigned long long cycles_t; 

inline unsigned long long currentcycles()
{
    unsigned long long result;
	__asm__ __volatile__ ("rdtsc" : "=A" (result));
			        
	return result; 
}

inline unsigned long long rdtsc(void)
{
    unsigned int hi, lo;
	__asm__ __volatile__ ("rdtsc" : "=a"(lo), "=d"(hi));
    return ( (unsigned long long)lo)|( ((unsigned long long)hi)<<32 );
}

int main(){
	while(true){
		cycles_t t = rdtsc();
		printf("t:%llu\n", t);
		sleep(1);
	}
	return 0;
}

### clock cost of lock
env: intel i7 2.2GMZ

we test 2 lock: synchronized and ReentrantLock, we got 4 results:
1. synchronized.lock
2. synchronized.unlock
3. ReentrantLock.lock
4. ReentrantLock.unlock

for 500k times, total 10 rounds, as follows:

| items  | average   | max    | min   |
| ------ | --------- | ------ | ----- |
| 1  |73|     24730|        40|
| 2  |75|     68118|        40|
| 3  |96|    101706|        38|
| 4  |107|    114790|        40|
|1   |86|     35404|        42|
|2   |85|    112458|        40|
|3   |100|    272826|        38|
|4   |94|     49070|        40|
|1   |73|     36230|        40|
|2   |73|     62530|        40|
|3   |67|     33562|        38|
|4   |73|     32794|        42|
|1   |72|     77508|        40|
|2   |72|    123528|        38|
|3   |66|     41004|        38|
|4   |72|     71586|        42|
|1   |78|     38050|        40|
|2   |79|     84625|        40|
|3   |72|     40078|        38|
|4   |78|     37540|        42|
|1   |76|     37202|        40|
|2   |77|     55616|        38|
|3   |70|    122208|        38|
|4   |76|     35759|        42|
|1   |72|     26188|        40|
|2   |74|     44402|        40|
|3   |67|     97772|        38|
|4   |73|     75052|        42|
|1   |71|     72062|        40|
|2   |71|     23346|        40|
|3   |65|     88518|        38|
|4   |71|     62684|        42|
|1   |78|     25517|        40|
|2   |79|    100544|        40|
|3   |72|     56396|        38|
|4   |79|    106300|        42|
|1   |76|     86290|        40|
|2   |77|     41536|        38|
|3   |70|     51458|        38|
|4   |76|     46724|        42|

* we can see that the lock and unlock operation are almost the same expensively.
* synchronized and ReentrantLock has the same operation cost.
* they almost cost 80 cpu-clock-cycles.

### Questions: why lock operation costs 80 cycles? And how is the lock implemented in CPU instructs
Yes, CPU needs to support lock, usually using test-and-set instruct etc.
You can refer to [atomic-instructions](https://en.wikipedia.org/wiki/Linearizability#Primitive_atomic_instructions) or [test-and-set](https://en.wikipedia.org/wiki/Test-and-set)

A test-and-set instruction needs 80 cpu-cycles?!!! Is that reasonable? 
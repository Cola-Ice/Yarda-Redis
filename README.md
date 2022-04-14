# Redis

## 一、Redis简介

### 1.NoSQL

泛指非关系型的数据库，NoSQL即Not-Only SQL。NoSQL数据库产生就是为了解决大规模数据集合多重数据种类带来的挑战，尤其是大数据应用难题

> 1.对数据库高并发读写的需求
>
> 2.对海量数据的高效率存储和访问的需求
>
> 3.对数据库的高可扩展和高可用性的需求

#### NoSQL的类别

1.键值(key-value)存储数据库

Redis、Voldemort等。这类数据库主要使用到哈希表，Key-Value模型对于IT系统来说优势在于简单、易部署，但如果DBA只对部分值进行查询或更新时，效率就显得比较低下了。典型应用：缓存，处理大量数据的高访问负载

2.列存储数据库

HBase、Riak等。通常用来应对分布式存储的海量数据。键仍然存在，但他们的特点是指向了多个列，这些列是由列家族来安排的。典型应用：分布式文件系统

3.文档型数据库

MongoDB等。该类型的数据模型是版本化的文档，半结构化的文档以特定的格式存储，比如JSON。典型应用：Web应用

4.图形化数据库

### 2.什么是Redis

Redis是一个简单的、高效的、分布式的，基于内存亦可持久化的Key-Value型数据库

### 3.Redis的优缺点

#### 1.Redis的优点

- **性能极高：**数据存储在内存中(QPS：11万，TPS：8万)；采用单线程架构，避免了上下文切换，不存在加锁释放锁操作；采用非阻塞IO多路复用
- **数据类型丰富：**String、Hash、List、Set以及Ordered Set等
- **原子性：**Redis中所有操作都是原子性的，要么执行成功要么失败，单个操作是原子性的，多个操作也支持事务
- **可持久化：**Redis直接将数据存储到内存中，但也提供了两种持久化机制：RDB和AOF
- **高可用：**Redis支持主从复制，并提供哨兵机制，保证服务器的高可用

#### 2.Redis的缺点

- **耗内存：**数据存储在内存
- **难以在线扩容：**Redis难以支持在线扩容，尤其在集群场景里，当存储容量达到上限，在线扩容非常困难

## 二、Redis安装

```console
docker run -p 6379:6379 --name redis \
-v /usr/local/docker/redis/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf \
--appendonly yes
```

### 1.Redis的配置

下载redis配置文件

```console
wget http://download.redis.io/redis-stable/redis.conf
```

修改配置

```console
# 默认只允许本机连接，注释掉，解除本地连接限制
# bind 127.0.0.1 -::1
# 解除保护模式
protected-mode no
# 守护线程方式启动,docker容器部署时设置为no
daemonize yes
# 设置密码
requirepass 密码
# 配置持久化
appendonly yes
```

### 2.Redis的内存维护方案

redis的内存维护方案主要包含过期策略和内存淘汰策略

#### 1.过期策略

redis中同时使用了定期删除，惰性删除两种过期策略

**定期删除**

redis会将设置了过期时间的key放入一个独立的字典中，默认每隔100ms就进行一次过期扫描：

1.随机抽取20个key

2.删除这20个key中过期的key

3.如果过期的key比例超过1/4，就重复执行1，继续删除

4.redis为每次定期删除设置了上限时间，默认最多执行25ms

> 为什么不全部扫描？
>
> 全部扫描会占用大量的CPU资源处理过期数据，影响redis的响应时间和吞吐量，为了防止每次扫描过期key比例都超过1/4，导致不停循环卡死线程，redis为每次扫描设置了上限时间，默认25ms

**惰性删除**

当访问某个key时，redis会判断该key是否已过期，过期则删除

#### 2.内存淘汰策略

redis的内存淘汰策略是指，当redis用于缓存的内存不足时，怎么处理新写入的数据

（1） no-enviction（驱逐）：默认策略，当内存不足时，写入就会报错

（2）volatile-lru：从已设置过期时间的数据集中挑选最近最少使用的数据淘汰

（3）volatile-ttl：从已设置过期时间的数据集中挑选将要过期的数据淘汰

（4）volatile-random：从已设置过期时间的数据集中任意选择数据淘汰

（5）volatile-lfu：从已设置过期时间的数据集中挑选使用频率最低的数据淘汰

（6）allkeys-lru：从数据集中挑选最近最少使用的数据淘汰

（7）allkeys-lfu：从数据集中挑选使用频率最低的数据淘汰

（8）allkeys-random：从数据集中任意选择数据淘汰

这八种大体上可以分为4类，lru(最近最少使用)、lfu(最少使用)、random(随机)、ttl(将要过期)

**LRU算法**

```
标准LRU实现原理
LRU会使用一个链表维护缓存中每个数据的访问情况，并根据数据的访问，实时调整数据在链表中的位置，链头(MRU)数据表示最近刚被访问过，链尾(LRU)数据表示最近最少被访问。如果严格按照标准LRU算法，Redis需要额外内存空间来保存链表，且当有数据插入或数据被访问时维护链表，不仅占用内存，而且还影响性能。

Redis中的LRU实现
Redis中提供了一个近似LRU算法实现。首先是设置了全局LRU时钟，并在键值对(KV)创建时获取全局LRU时钟值作为访问时间戳，之后每次访问时获取全局LRU时钟值，更新访问时间戳；然后，当Redis每处理一个命令，都调performEvictions判断是否需释放内存。若已使用内存超出最大值，则通过随机采样并结合键值对的空闲时间组成淘汰池，再从淘汰池中选出空闲时间(idle)最大的数据删除

具体步骤：
1.全局LRU时钟值计算：当redis服务启动会初始化全局LRU时钟值lruclock，默认每隔100ms会更新全局LRU时间值，精度为1s
2.键值对LRU时钟值的初始化与更新：当键值对(KV)被创建时，就会获取全局LRU时钟值初始化redisObject对象中lru字段；之后每次访问时，都会获取全局LRU时钟值，更新访问时间
3.近似LRU算法的实际执行：
（1）判断是否需要进行内存淘汰：redis在处理每个命令时，都会评估内存使用量，若发现已超过最大值就会计算需要释放的内存量，然后while(终止条件为到达计算的内存释放量)循环淘汰数据释放内存；
（2）初始化并填充淘汰池：每次循环redis初始化一个长度16的数组淘汰池，用来存放待淘汰的键值对(KV)，循环所有数据库，每个数据库随机采样samples个键值对，通过与池中原有数据对比，将空闲时间(idle)更长的键值对放入，池中数据是按照空闲时间有序排列的；
	淘汰池进入策略为：
		-淘汰池未满，直接放入；
		-淘汰池已满，判断淘汰池中是否有键值对的空闲时间小于当前键值对(KV)，如果有则替换掉
（3）确定删除的键值对：当本轮淘汰池确定后，从池中最后一个键值对(KV)开始选择，如果选到的K非空，就把他作为最终淘汰的K。删除结束后会判断释放的内存是否达标，不达标则继续执行循环逻辑
4.可以通过更改取样数来调整近似LRU算法的精度maxmemory-samples 5
```

**LFU算法**

```
Redis中提供了一个近似LFU算法实现，根据访问频率来淘汰数据的。LFU算法会将访问频率信息记录在redisObject对象中，并在键值对访问时更新访问频率信息（衰减访问次数+访问递增），最后执行淘汰算法时，访问次数较小的优先放到淘汰池

具体步骤：
1.键值对访问频率记录：LFU算法复用了redisObject的lru字段记录访问频率信息，并将其分成两部分，高16位保存最后访问时间和低8位保存访问次数
2.键值对访问频率初始化和更新：当键值对(KV)被创建时，就会初始化redisObject对象中lru字段保存的访问频率信息，16位的当前时间戳，精度1min，8位的访问频率，初始值5；之后每次访问时，都会更新访问频率信息：
	（1）根据距离上次访问的时长，衰减访问次数(最小0)。衰减大小取决于上次访问距离当前的分钟数，默认每分钟衰减1(lfu-decay-time=1)
	（2）根据当前访问更新访问次数(最大255)。根据配置进行递增，核心是访问次数越大，访问次数被递增的可能性越小
	（3）更新访问频率信息lru值
3.执行LFU算法淘汰数据：
LFU算法的基本执行流程和LRU算法相同，但是在填充淘汰池的函数(evictionPoolPopulate)中，使用了不同与LRU的逻辑来计算每个待淘汰键值对的空闲时间(idle)
不同点在于：
	LRU算法的idle用来记录空闲时间，LFU算法使用访问次数计算idle值：255-访问次数(近似访问频率)；为了保证计算结果合理性，在计算idle前还会执行一次访问次数衰减
	
优点：
LFU算法解决了偶尔被访问一次之后，数据就不会被淘汰的问题，相比于LRU算法也更合理
缺点：
复杂度较高，淘汰池计算过程复杂；性能开销，每次访问需要额外维护访问频率信息（衰减访问次数+访问递增）

访问次数初始化默认5，目的是避免新添加的键值对(KV)访问次数短时间内衰减为0，面临淘汰风险
```

## 三、Redis命令

### 1.常用key管理命令

```console
keys *：返回所有key值,keys name* 代表以name开头的key
type key：返回key的类型
exisets key：是否存在执行的key,存在返回1，不存在0
expire key 时间s：设置key的过期时间
ttl key：返回指定key的剩余时间，单位s,-1表示永久存在，-2表示已过期
del key：删除指定key
persist key：取消过期时间
select dbindex：切换到库index
move key dbindex：将key转移到其他数据库
info：查看数据库信息
flushdb：清空当前库中所有数据
flushall：清空所有数据库中数据
```

### 2.Key的命名建议

1.key不要太长，尽量不要超过1024字节，这不仅消耗内存空间，还影响查询效率

2.key也不要太短，太短可读性降低

3.在同一项目中，key最好使用统一的命名模式，例如system:user:password

4.key的名称区分大小写

## 四、Redis的数据类型

官网数据类型介绍：https://redis.io/docs/manual/data-types/

官网redis相关命令介绍：https://redis.io/commands/?group=list

**Redis的数据结构**

![img](https://github.com/Cola-Ice/Yarda-Redis/raw/master/doc/image/image-20220125164236168-206314492.png)

**RedisObject结构体**

Redis中并没有直接使用其底层定义的数据结构，而是在这些数据结构之上又封装了一层RedisObject，RedisObject有五种对象：字符串对象、列表对象、哈希对象、集合对象和有序集合对象

RedisObject中定义的字段包含：数据类型、编码方式、lru、引用计数、指向底层数据结构的指针

```console
typedef struct redisObject {
    // 类型
    unsigned type:4;
    // 编码
    unsigned encoding:4;
    // LRU策略：对象最后一次被访问的时间
    // LFU策略：最低8位的访问频率，和最高16位的访问时间
    unsigned lru:REDIS_LRU_BITS; /* LRU time (relative to global lru_clock) or
                            * LFU data (least significant 8 bits frequency
                            * and most significant 16 bits access time). */
    // 引用计数（C语言需要自己管理内存，该字段与内存回收相关）
    int refcount;
    // 指向实际值的指针
    void *ptr;
} robj;
```

### 1.String类型

string是redis中最基本的数据类型，value不仅仅是string，也可以是数字。string类型是二进制安全的，redis的string可以包含任何数据，比如序列化的对象，图片二进制等，字符串最大长度512M

> 底层实现：C语言没有字符串类型，只能使用指针或字符数组的形式表示一个字符串，所以redis设计了一种简单动态字符串作为string类型的底层实现

#### 1.String类型常用命令

```console
# 赋值语法
set key value：设置key-value，已存在就覆盖
setnx key value：(not exist)设置key-value,并返回1，如果key存在，不设置并返回0，适用分布式锁
# 取值语法
get key：获取key的值，不存在返回nil
getrange key start end：获取key的值，并截取
getset key value：获取并设置key的值，返回旧值，不存在返回nil
# 删除
del key：删除指定的key，key存在返回1，不存在返回0
# 自增/自减
incr key：自增，每次增1
incrby key increment：按照指定增量自增
decr key：自减，每次减1
decrby key decrement：按照指定减量自减
```

#### 2.String类型应用场景

- string通常用于保存单个字符串或JSON字符串数据
- 因为string是二进制安全的，完全可以保存图片二进制
- string类型可以用作计数器，利用incr，decr来实现原子计数（微博数、粉丝数）

### 2.Hash类型

Hash类型是string类型的field和value的映射表，可以看作具有key-value的Map容器，特别适合存储对象，并且可以对Hash中存储的数字值执行自增或自减操作，相比而言，将一个对象类型存储在Hash比存储在string类型中占用更少的内存空间，最多存储 2^32 - 1个键值对

> 底层实现：哈希表，时间复杂度O(1)

#### 1.Hash类型常用命令

```console
# 赋值语法
hset key field value：为指定的key，设置field-value
hmset key field1 value1 field2 value2...：为指定的key，设置多组field-value
# 取值语法
hget key field：获取指定Hash中，field对应的value
hmget key field1 field2...：获取Hash中，多个field对应的value
hgetall key：获取Hash中所有的field-value
hkeys key：获取Hash中所有的键值
hlen key：获取Hash的长度
# 删除语法
del key：直接删除key对应的整个Hash类型
hdel key field：删除Hash中某个field
# 其他语法
hsetnx key field value：向Hash中添加键值对，如果不存在则添加并返回1，已存在直接返回0
hincrby key field increment：Hash表中字段field对应的整数值自增，增量increment
hincrbyfloat key field increment：Hash表中字段field对应的浮点值自增，增量decrement
hexists key field：查看Hash表中是否存在指定的字段field
```

#### 2.Hash类型应用场景

常用于存储对象（例如存储一个用户信息）

> 为什么不用string存储对象？
>
> Redis的Hash实际上内部类似一个HashMap，它能方便的存储一组键值数据，如果采用string存储对象主要有两种方式：
>
> 1.将用户id作为key，value为用户信息JSON，这种方式的缺点是：存取信息需要序列化/反序列化，并且不能只取单个字段、或修改单个字段（修改还可能存在线程安全问题）
>
> 2.将用户信息存储为多个string类型，使用用户id+字段名作为唯一标识获取对应属性值，虽然解决了序列化/反序列化以及并发安全问题，但是用户id重复存储，创建多个redisObject，浪费内存空间

### 3.List类型

redis中的List是简单的字符串列表，元素按插入顺序排序，在插入元素时可以指定将元素插入到链表的头部或尾部，列表最大长度2^32-1（超40亿元素）

> 底层实现：quicklist，它结合了ziplist和linkedlist的优点。整体上quicklist就是一个双向链表结构，插入删除效率很高，查询时间复杂度O(n)，不过访问两端元素的时间复杂度是O(1)，所以list的操作多数是push和pop；每个quicklist节点又是一个ziplist，具备压缩列表特性

#### 1.List类型常用命令

```console
# 赋值命令
lpush key elements：从左侧向列表中添加数据，链表不存在时自动创建
rpush key elements：从右侧向列表中添加数据，链表不存在时自动创建
lpushx key elements：从左侧向列表中添加数据，链表不存在不执行任何操作
rpushx key elements：从右侧向列表中添加数据，链表不存在不执行任何操作
# 取值命令
llen key：获取列表长度
lindex key index：获取列表指定下标位置的元素
lrange key start stop：获取指定index范围的所有元素，与java中不同，这返回的元素包含start和stop，范围不存时将返回空（start和stop可以为负数，表示距离列表末尾的偏移量，如-1表示最后一个元素）
lpop key count：弹出列表左侧的count个元素（返回并删除）
rpop key count：弹出列表有侧的count个元素（返回并删除）
blpop keys timeout：依次检查keys中列表，从第一个非空列表弹出左侧第一个元素，如果所有列表为空时，阻塞等待timeout秒，0表示一直阻塞直到获取到元素（可实现消息队列）
brpop keys timeout：依次检查keys中列表，从第一个非空列表弹出右侧第一个元素，如果所有列表为空时，阻塞等待timeout秒，0表示一直阻塞直到获取到元素
# 修改命令
lset key index element：将列表下标为index的元素设置为element，index超出len范围将报错
linsert key before|after pivot element：在列表中元素pivot之前或之后插入元素，当pivot有多个时匹配第一个，key存在不包含pivot时，返回-1，key不存在不进行任何操作
# 删除命令
lrem key count element：删除列表中count个element元素（count>0从左向右，count小于0，从右向左，count等于0删除所有）
ltrim key start stop：将列表修剪到指定范围，包含start和stop（start和stop可以为负数，表示距离列表末尾的偏移量）
del key：直接删除key对应的整个列表
# 其他命令
lmove source destination left|right left|right：将列表1中左侧/右侧第一个元素，移动到列表2中最左侧/最右侧；如果列表1为空，不执行任何操作，如果列表1与列表2相同，则视为列表轮换
blmove source destination left|right left|right timeout：lmove的block版，当列表1中没有元素时会阻塞等待timeout秒，0表示一直阻塞直到获取到元素
```

#### 2.List类型应用场景

1.存储key-list型一对多的数据，例如关注列表、粉丝列表、人气榜单、热点新闻，使用命令lpush+ltrim+lrange命令，ltrim固定列表长度，lrange分页

2.消息队列，使用命令rpush+lpop，rpush生产数据，lpop消费数据，还可使用blpop阻塞读取数据

3.延迟队列，使用rpush+lpop，rpush生产数据时，消息内容增加一个delay时间，每次pop出来的数据检查是否到期

### 4.Set类型

redis中Set是无序不可重复的字符串集合

> 底层实现：redis的集合和列表都可以存储多个字符串，但是列表可以存储多个相同的字符串，而集合则通过使用哈希表(hashtable)来保证自己存储的每个字符串各不相同，redis中的集合是无序的

#### 1.Set类型常用命令

```console
# 赋值命令
sadd members：向集合中添加成员，key不存在时自动创建集合，返回值为添加的成员数，已存在的成员将被忽略
# 查询命令
scard key：查询集合中成员个数
smembers key：返回集合中所有成员
sismember key member：判断指定成员在集合中是否存在，包含返回1，不包含返回0
smismember key members：判断每个成员在集合中的存在情况，包含返回1，不包含返回0
srandmember key count：从集合中随机返回count个元素，默认1个，当count>0返回的数不允许重复，当count小于0，返回的数可能重复
spop key count：类似与srandmember，从集合中返回随机返回count个成员，并删除，这里count>0
# 集合命令
sdiff key1 keys：返回第一个集合与所有后续集合的差集（存在于第一个集合，不存在于后续集合）
sdiffstore destination ke1 keys：类似于sdiff，计算第一个集合和所有后续集合的差集，并将差集作为一个新集合
sinter keys：返回所有集合的交集
sinterstore keys：类似于sinter，计算所有集合的交集，并将交集作为一个新的集合
sunion keys：返回所有集合的并集
sunionstore keys：类似于sunion，计算所有集合的并集，并将并集作为一个新的集合
smove source destination member：将成员member从一个集合移动到另一个集合
# 删除命令
srem key members：删除集合中指定成员，可以为多个
del key：删除整个集合
```

#### 2.Set类型应用场景

1.利用多个集合交集、并集、差集操作，实现共同好友、感兴趣的人(拥有多个相同标签)

2.利用set集合的唯一性，存储不可重复值（例如统计访问网站的独立IP）

3.利用srandmember随机返回(spop随机弹出)，实现抽奖

### 5.ZSet类型

redis中的ZSet是有序不可重复集合，相比Set类型多了分数值（score），用来代表排序的权重，默认按照分数从小到大排序(分数相同先插入的在前边)

> ZSet类型有序集合底层采用ziplist或跳跃表实现。当集合中元素数量超过128(默认值)，或新添加元素长度超过64(默认值)时，会使用跳跃表skiplist实现，否则使用压缩表ziplist作为有序集合的底层实现

> 跳表O(logN)是最经典的空间换时间解决方案，只有在数据量较大的情况下才能体现出优势，适用于读多写少的场景，维护成本高，新增和删除都需要更新所有索引

![img](https://github.com/Cola-Ice/Yarda-Redis/raw/master/doc/image/image-20220126165447834-243377527.png)

#### 1.ZSet类型常用命令

```console
# 赋值命令
zadd key score member：添加成员，链表不存在时自动创建
# 取值命令
zcard key：返回有序集合成员个数
zrank key member：返回集合指定成员的索引index
zcount key min max：返回集合指定分数区间内的成员个数
zrange key start stop：获取集合内指定index范围内的成员，分数从低到高
zrangebyscore key min max：获取集合指定分数区间内的成员，分数从低到高
zrevrange key start stop：获取集合内指定index范围内的成员，分数从高到低
zrevrangebyscore key max min：获取集合指定分数区间内的成员，分数从高到底
# 删除命令
zrem key members：删除集合中的某个成员，可以删除多个
zremrangebyrank key start stop：删除集合中指定index范围内的所有成员
zremrangebyscore key min max：删除集合中指定分区区间内的所有成员
del key：删除整个有序集合
# 其他命令
zincrby key increment member：增加指定成员的分数，返回值为更改后的分数
```

#### 2.ZSet类型应用场景

利用ZSet集合的积分排序，实现排行榜功能，例如销量排名、积分排名

### 6.HperLogLogs类型

Redis中的hperloglogs是用来做基数统计的算法，优点是计算基数所需的空间小、且固定，不随数据量上升而增加；缺点只能统计基数量

> 实现原理：本质上基于string类型

#### 1.HperLogLogs类型常用命令

```console
pfadd key elements：添加元素到hperloglogs
pfcount key：返回给定hperloglogs基数估算值
pfmerge destination sources：合并多个hperloglogs作为一个新的hperloglogs
```

#### 2.HperLogLogs类型应用场景

HperLogLogs主要用作基数统计，例如，统计网站访问独立IP数，统计文章真实阅读数

### 7.Bitmaps类型

Redis中的Bitmaps提供了对位的操作：

（1）Bitmaps本身不是一种数据类型，实际上就是字符串，但是它可以对字符串的位进行操作

（2）Bitmaps单独提供了命令，可以把Bitmaps想象成一个以位为单位的数据，数组的每个单元只能存储0和1，数组的下标在Bitmaps中叫做偏移量

#### 1.Bitmaps类型常用命令

```console
# 赋值命令
setbit key offset 0|1：设置指定偏移量对应的值
# 取值命令
getbit key offset：获取指定偏移量的值
```

#### 2.Bitmaps类型应用场景

当我们需要存储大量的Boolean类型0|1数据时，可以考虑用Bitmaps。例如存储用户每天是否打卡、存储网站一天活跃用户

### 8.Geospatial类型

Geospatial类型用于存放地理位置信息，将指定的地理位置信息（经度、维度、名称）添加到指定key中，redis底层会将这些数据将被保存到zset中，目的是对数据进行半径查询等操作。通过该类型提供的命令，可以方便实现周围的人、两地之间距离等功能

## 五、Redis的高级功能

### 1.Redis的发布订阅

Redis发布订阅(pub/sub)是一种消息通信模式：发送者发送消息，订阅者接收消息。Redis客户端可以订阅任意数量的频道

#### 1.Redis发布订阅命令

```console
# 订阅频道
subscribe channels：订阅一个或多个频道
# 发布频道
publish channel message：将消息发送到指定的频道
# 退订频道
unsubscribe channels：退订指定的频道
```

### 2.Redis的事务

Redis事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序的执行，事务在执行过程中，不会被其他命令打断。Redis事务的主要作用将多个命令打包顺序执行

#### 1.事务执行过程

Redis中一个事务从开始到执行会经历 **开始事务** 、 **命令入队** 和 **执行事务** 三个阶段

**开始事务：**使用 **multi** 命令开启事务，客户端从非事务状态切换到事务状态

**命令入队：**当客户端处于非事务状态下， 所有发送给服务器端的命令都会立即执行，但当客户端进入事务状态后，服务器会将命令先放进一个事务队列，然后返回`QUEUED`，表示命令已入队

**执行事务：**当服务器收到 **exec** 命令就会开始执行命令，或 **discard** 取消执行

redis中事务相关的命令

```console
multi：开启事务
exec：执行事务
discard：取消事务
watch key：监听一个或多个key，如果在事务执行之前这些key被其他命令改动，那么事务将被取消（监控一直延续到exec执行之后）
unwath：取消watch命令对所有key的监听
```

#### 2.事务的错误处理

入队过程某个命令报告错误，执行时队列中所有命令都会被取消

执行过程某个命令执行失败，其他命令不会被取消（执行失败不回滚）

#### 3.事务冲突处理

事务冲突的处理可以选择悲观锁或乐观锁

**悲观锁**

悲观认为每次拿数据后都会出现数据修改，所以在拿数据时就会加锁，这样别的操作就只能block直到它释放锁，传统的关系型数据库里的行锁、表锁都是这种锁机制。适用于写多读少的场景

**乐观锁**

乐观认为每次拿数据后不会出现数据修改，所以不会上锁，等到更新的时候才回去判断数据是否被修改过（版本号）。适用于读多写少的场景。redis中的事务冲突就是通过乐观锁机制解决的

**Redis的watch机制**

redis采用watch机制实现的乐观锁来解决事务冲突问题

**watch key：**监听一个或多个key，如果在事务执行之前这个key被其他命令改动，那么事务将被取消

> watch的实现：
>
> watch底层实现基于redis中的字典结构，key为被监视的键，value是监视这个键的客户端。当key发生变动时，会找到对应的客户端，将他们事务被破坏的标志打开，即redis_dirty_cas标志，在执行事务操作时会检查该标志，判断是否取消事务。redis的字典基于hashtable实现，类似于java中的HashMap

#### 4.Redis事务三特性

单独的隔离操作、没有隔离级别、不保证原子性

```
# 单独的隔离操作
事务中所有命令都会序列化、按顺序执行，事务执行过程不会被其他命令打断
# 没有隔离级别
因为事务提交前任何指令都不会被实际执行
# 不保证原子性
事务中如果有一条命令执行失败，其他命令依然会执行，不会回滚
```

### 3.Redis的持久化

Redis直接将数据存储到内存中，但也提供了两种持久化机制：RDB和AOF，默认开启RDB

#### 1.RDB

Redis DataBase，在指定的触发条件下，将该时刻内存中数据集快照写入磁盘

**执行过程**

> 1.在满足条件时触发 bgsave 执行，Redis父进程判断是否存在正在执行的RDB/AOF子进程，如果存在直接返回
>
> 2.不存在时，父进程执行fork操作创建子进程（fork的作用是复制进程）
>
> 3.子进程根据父进程内存生成临时RDB文件（dump.rdb）
>
> 4.使用临时RDB文件原子替换原RDB文件，替换完成后退出子进程（直接写入redis down掉可能造成数据不完整）

![image_16496620736272](https://github.com/Cola-Ice/Yarda-Redis/raw/master/doc/image/image_16496620736272.png)

**触发条件**

分为手动触发和自动触发

手动触发：客户端命令，bgsave

自动触发：配置文件指定触发条件

```
# 默认配置
# save 3600 1 1h有至少1key修改，就生成RDB  
# save 300 100 5min有至少100key修改，就生成RDB
# save 60 10000 1min有至少10000key修改，就生成RDB
```

**RDB特点**

优点：数据文件小，恢复速度快，适合大规模的数据恢复

缺点：执行过程占用大量内存，两次持久化之间服务挂掉可能造成部分数据丢失

#### 2.AOF

Append Only File，将服务器执行的每个写操作以追加的方式记录在日志文件，在服务器启动时重新执行这些命令，重建原始数据集。AOF默认关闭

**AOF日志写入磁盘策略**

- Always：所有的写命令执行之后，都会立即写入磁盘，写入磁盘之后才会返回结果。保证完全持久化，不会丢数据，性能也是最差的
- EverySecond：默认配置，每秒写入磁盘一次。所有的写命令都会先写入AOF缓冲区，然后redis后台线程每隔1s从缓冲区中拉取日志，写入磁盘。性能和持久化均衡
- No：和EverySecond一样，都会将命令日志先写入AOF缓冲区，但redis不会再执行写入磁盘操作，完全依赖操作系统刷写，一般30秒一次。性能最好，但是持久化没有保障

**AOF重写**

> 当AOF文件过大时，会fork出一条新进程进行文件重写。重写文件并没有读取旧AOF文件，而是将内存中的数据以set命令的方式重写一个新的AOF文件。重写过程中新产生的数据会先写入到重写缓冲区，当重写完成后会将重写缓冲区的内容写入到新文件，这个过程是阻塞执行。(整个重写过程也是先写临时文件，最后原子替换rename)

**AOF特点**

优点：文件完整性更高，可以根据业务场景选择日志写入磁盘的策略

缺点：AOF文件远大于RDB文件，数据恢复速度慢，每次读写都同步会影响性能

#### 3.总结

**官方持久化策略推荐**

1.如果能够忍受一段时间的数据丢失，可以单独使用RDB

2.不推荐单独使用AOF，因为不时创建 RDB 快照对于进行数据库备份、更快地重新启动以及 AOF 引擎中出现错误时都有积极作用

3.同一个实例可以同时使用 AOF 和 RDB，RDB文件用于定期数据备份。在这种情况下，当 Redis 重新启动时，AOF 文件将用于重建原始数据集，因为它保证是最完整的

**数据恢复**

将备份文件(RDB/AOF文件)移动到指定目录并启动服务，redis就会自动加载文件数据至内存

> RDB本身为二进制文件，恢复很快：1GB加载到内存大约20~30s

### 4.Redis的主从复制

redis提供了主从复制功能，主节点以写为主，从节点默认只读

#### 1.Redis主从复制流程

**主从节点第一次数据同步**

1.建立连接：从节点主动向主节点发送命令建立连接

2.初始数据同步：当从节点第一次连接到主节点时，主节点会生成并发送RDB文件到从节点执行全量同步

3.同步复制缓冲区命令：针对新写入的命令，主节点会先写入复制缓冲区，然后再发送复制缓冲区中数据给从节点

**从节点异常重启数据同步**

1.当主节点和从节点连接中断时，主库将断开期间收到的命令写入复制缓冲的同时写入环形缓冲区（offset）

2.从节点重新连接后会尝试进行增量同步，同步断开期间的命令（从节点保存了当前同步的偏移量）

3.当无法进行增量同步时，从节点将请求全量同步（断开时间太久，环形缓冲区中命令已被覆盖，就只能全量同步）

```
复制缓冲区：主库会给每个建立连接的从库分配一个复制缓冲区，操作命令会先写入复制缓冲区再发送给从库。当从库复制过慢导致主库的复制缓冲区达到限制后，主库就会强制断开从库的连接
环形缓冲区：为了解决从库重新连接后找不到主从差异数据而设立的环形缓冲区，避免全量同步带来的性能开销，如果从库断开太久，环形缓冲区的命令会就覆盖
```

#### 2.Redis主从复制基本特性

1.redis主从复制过程是异步处理，在主节点是非阻塞的。客户端也可使用 **`wait`** 命令请求某些数据同步复制（非强一致性）

2.复制时从节点也有很程度是非阻塞的

> 当从节点执行初始同步时，默认仍可以使用旧版本数据集处理查询，仅当初始同步完成，删除旧数据集并加载新数据集时，短暂阻塞客户端请求

3.主节点可以有多个副本，副本也能够接受其他副本的连接

#### 3.Redis主从复制作用

1.读写分离，提高并发性能

2.提升安全性，实现数据热备份

3.提高可用型，避免单机故障导致服务不可用

> 可以使用主从复制来避免让主节点将完整数据集写入磁盘，而在副本中开启备份(RDB)或启用AOF。需要注意，当这种情况下主节点没有开启持久化，不要开启自启动，可能造成数据丢失

#### 4.搭建redis主从环境

```console
1.docker启动三个redis实例
# 实例1
docker run -p 6379:6379 --name redis1 --network redis-ms \
-v /usr/local/docker/redis-ms/redis6379/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-ms/redis6379/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
# 实例2
docker run -p 6380:6379 --name redis2 --network redis-ms \
-v /usr/local/docker/redis-ms/redis6380/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-ms/redis6380/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
# 实例3
docker run -p 6381:6379 --name redis3 --network redis-ms \
-v /usr/local/docker/redis-ms/redis6381/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-ms/redis6381/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
2.从节点连接主节点
slaveof redis1 6379 # 可配置在从节点的配置文件(永久)，从节点执行该命令(临时)
```

主从复制模型中，在主节点挂掉后，需要我们手动将从节点晋升为主节点

### 5.哨兵机制

Redis sentinel 哨兵机制，哨兵是Redis集群架构中一个重要组件，哨兵的出现解决了主从复制出现故障需要人为干预的问题，保证系统的高可用

#### 1.哨兵的主要功能

1.集群监控：负责监控Redis主从节点是否正常工作

2.消息通知：当某个redis实例故障，哨兵负责发送报警通知给管理员

3.故障转移：当master节点挂掉，哨兵负责从slave中选举出新的master

4.配置中心：如果发生故障转移，通知client客户端新的master地址

#### 2.集群监控

1.哨兵机制建立了多个哨兵节点，共同监控Redis集群节点的运行状态（通常每个实例上都会再起一个哨兵进程）

2.同时哨兵之间也会互相通信，交换低主从节点的监控状况

3.每隔1s哨兵都会向整个集群中所有节点发送心跳（ping）（包含redis master/slave + 其他哨兵）

#### 3.故障转移

当超过半数的哨兵都认定主节点发生故障，就会发起投票，先选举出领头的哨兵（半数以上支持），由领头哨兵选举新的master节点，选举规则：

1.首选排除已下线、或与master断开连接时间太长的slave

2.优先级较高的slave优先作为master

3.复制偏移量offset较大的优先作为master

4.运行ID（run id）较小的优先作为master

## 六、Redis集群

Redis 3.0中提供了无中心化集群配置，Redis cluster实现了 对redis服务的**水平扩容，故障切换**，**主从复制**，通过分片（插槽段）将整个数据库分布式存储在多个节点，解决了单个redis内存不足问题

> 集群最少包含3个主节点

### 1.搭建redis集群

集群规模：3主 + 3从（集群规模最小为3）

1.docker启动6个redis实例

```
# 实例1
docker run -p 6379:6379 -p 16379:16379 --name redis1 --network redis-cluster \
-v /usr/local/docker/redis-cluster/redis6379/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-cluster/redis6379/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
# 实例2
docker run -p 6380:6380 -p 16380:16380 --name redis2 --network redis-cluster \
-v /usr/local/docker/redis-cluster/redis6380/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-cluster/redis6380/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
# 实例3
docker run -p 6381:6381 -p 16381:16381 --name redis3 --network redis-cluster \
-v /usr/local/docker/redis-cluster/redis6381/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-cluster/redis6381/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
# 实例4
docker run -p 6389:6389 -p 16389:16389 --name redis4 --network redis-cluster \
-v /usr/local/docker/redis-cluster/redis6389/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-cluster/redis6389/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
# 实例5
docker run -p 6390:6390 -p 16390:16390 --name redis5 --network redis-cluster \
-v /usr/local/docker/redis-cluster/redis6390/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-cluster/redis6390/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
# 实例6
docker run -p 6391:6391 -p 16391:16391 --name redis6 --network redis-cluster \
-v /usr/local/docker/redis-cluster/redis6391/redis.conf:/usr/local/etc/redis/redis.conf \
-v /usr/local/docker/redis-cluster/redis6391/data:/data \
-d redis:6.2.5 redis-server /usr/local/etc/redis/redis.conf 
```

2.初始化集群

```
./redis-cli -a password --cluster create --cluster-replicas 1 ip:6379 ip:6380 \
ip:6381 ip:6389 ip:6390 ip:6391
```

3.查看集群节点信息

```
./redis-cli -c # 以集群方式连接
cluster nodes # 查看集群节点信息
```

### 2.集群操作

#### 1.散列槽(hash slot)

Redis 集群包含16384（2^14）个散列槽，这些散列槽平均分配在主节点上，每个键都属于一个散列槽，集群通过公式计算键对应的槽

> slot = CRC16(key) & 16383

#### 2.MOVED重定向

当Redis集群中的节点收到请求时，会先根据key计算对应的哈希槽，再根据哈希槽与节点的映射关系找到对应的节点，如果属于当前节点则执行指令，如果不属于当前节点则返回MOVED重定向错误给客户端，通知客户端重新请求正确的节点

### 3.故障恢复

1.当集群中某个主节点挂掉时，从节点会自动升为主节点，挂掉的主机重新启动后会作为从机运行

2.当某一段槽的主从节点都挂掉时，可以通过配置文件指定此时集群是否可用

> cluster-require-full-coverage no # 当某个槽挂掉时，挂掉的槽不可用，但集群可用

### 4.重新分片

Redis集群的重新分片是指将任意数量已经指派给某个节点的槽，改为指派给零一节点，且相关槽所属的键值对也会从原节点迁移到目标节点。重新分片可以在线进行，集群不需要下线

> Redis集群的重新分片操作是由Redis的集群管理软件 `redis-trib`负责执行的

### 5.Redis cluster 优缺点

**优点：**

1.水平扩容，解决单个redis实例内存不足问题

2.提高并发量，读写分离

3.无中心化配置，集群支持故障恢复、主从复制

**缺点：**

1.不支持多键操作（keys 也只能查看当前节点的数据）

2.不支持多键的Redis事务（事务执行过程不能有服务器切换，多个key可能分布在不同的散列槽）

3.Redis cluster方案出现晚，之前采用其他集群方案的需要整体迁移，复杂度大

## 七、Spring集成redis客户端

### 1.常用redis客户端介绍及对比

Java中常用redis客户端有Jedis、Redission、Lettuce，在Spring boot2.x之后，默认采用lettuce作为Redis客户端连接工具

```
Jedis：老牌Java Redis客户端，提供了比较全面的redis命令支持
Redission：提供了很多分布式相关操作，例如分布式锁、分布式集合，通过redis支持延迟队列
Lettuce：高级Redis客户端，用户线程安全同步、异步和响应适用，支持集群、Sentinel、管道和编码器
```

### 2.Spring Boot集成Jedis

maven依赖

```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
```

详见代码

### 3.RedisTemplate使用

spring boot 2.x版本后，redisTemplate默认的客户端类型是lettuce

maven依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### 1.序列化方式

需要注意的是，当我们把Java中对象通过redisTemplate写入redis时，需要先进行序列化，redisTemplate中默认采用JDK的序列化方式（JdkSerializationRedisSerializer），该序列化方式存入redis中的数据不够直观，需要修改序列化方式

> 为什么需要序列化？
>
> 数据存入redis需要按照redis的类型要求进行存储，redis中string类型key为string，value为string或数字，不能直接存储对象，所以当我们将对象交给redisTemplate时，它会帮我们序列化后再存入redis

```java
@Bean
public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<Object, Object> template = new RedisTemplate();
    template.setConnectionFactory(redisConnectionFactory);
    // 默认序列化是JDK，导致redis中数据不够直观
    Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    jackson2JsonRedisSerializer.setObjectMapper(mapper);
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    // key 序列化方式 stringRedisSerializer
    template.setKeySerializer(stringRedisSerializer);
    // value 序列化方式 jackson
    template.setValueSerializer(jackson2JsonRedisSerializer);
    // hash key 序列化方式 stringRedisSerializer
    template.setHashKeySerializer(stringRedisSerializer);
    // hash value 序列化方式 jackson
    template.setHashValueSerializer(jackson2JsonRedisSerializer);
    template.afterPropertiesSet();
    return template;
}
```

redisTemplate<Object,Object>避免强制类型转换：

```java
public void testHashOps(){
    HashOperations<String,String,String> hashOperations = redisTemplate.opsForHash();
    // 新增、修改单个key
    hashOperations.put("user:lisi", "username", "王五");
    // 获取单个key对应的value
    String username = hashOperations.get("user:lisi", "username");
    System.out.println("username>>>>>>>>>>>>" + username);
}
```

#### 2.连接 Redis cluster

```yaml
spring:
  redis:
    cluster:
      nodes: xxx.xxx.x.xx:6379,xxx.xxx.x.xx:6380,xxx.xxx.x.xx:6381,xxx.xxx.x.xx:6389,xxx.xxx.x.xx:6390,xxx.xxx.x.xx:6391
      max-redirects: 3 # 最大转发次数
    password: xxxxxx # redis密码
    database: 0 # 库名
    client-type: lettuce # 2.x版本后，默认就是lettuce
    lettuce: # lettuce相关配置
      pool:
        enabled: true
        max-active: 8
        max-idle: 8
        max-wait: 1ms
        min-idle: 2
```

## 八、Redis实战

### 1.缓存穿透

**问题描述：**访问一个缓存和数据库都不存在的key，此时会直接打到数据库，并且差不到数据，没法写缓存，所以下次同样会打到数据库。此时，缓存起不到作用，请求每次都会走到数据库，流量大时数据库可能会被打挂

**解决方案：**

1.缓存空值。当访问缓存和DB都没有值时，可以将空值写进缓存，但设置较短的过期时间

2.接口校验。增加用户鉴权、数据合法性校验。缓存穿透场景最大的可能性是遭到非法攻击

3.布隆过滤器。使用布隆过滤器存储所有可能访问的key，不存在key直接被过滤，存在的key则再进一步查询缓存和数据库

> 布隆过滤器：由一个很长的二进制向量和一系列哈希函数组成。主要用于判断一个元素是否存在于某个集合中。通过元素哈希函数转换再经过一系列计算，最终计算出多个下标，添加元素时，将所有下标的值修改为1，查询元素时，判断元素对应所有下标值是否为1
>
> 优点：占用空间小，性能高
>
> 缺点：存在一定的误判；不能删除布隆过滤器里的元素

![image_16499283893703](https://github.com/Cola-Ice/Yarda-Redis/raw/master/doc/image/image_16499283893703.png)

### 2.缓存击穿

**问题描述：**当某个热点key，在缓存过期的一瞬间，同时有大量的请求打进来，由于此时缓存过期，所有请求最终都会打到数据库，造成数据库瞬间访问压力大增，甚至可能打垮数据库

**解决方案：**

1.热点数据不过期

2.加互斥锁。在多个并发请求中，只有第一个能拿到锁并执行数据库查询，其他线程阻塞等待，等第一个线程执行完，直接走缓存即可

### 3.缓存雪崩

**问题描述：**大量的热点key在同一时刻全部失效，造成数据库瞬间访问压力大增，引起雪崩，甚至导致数据库被打挂

**解决方案：**

1.过期时间打散。过期时间加上一个随机值，避免集中失效

2.热点数据不过期

3.加互斥锁。对于同一key，在多个并发请求中，只有第一个能拿到锁并执行数据库查询，其他线程阻塞等待，等第一个线程执行完，直接走缓存即可

### 4.分布式锁

分布式锁是解决分布式系统在并发场景下，多个线程访问同一资源时出现的并发数据安全问题

**分布式锁应具备的条件：**

1.互斥性，在分布式系统环境下，同一方法在同一时间只能被一个机器的一个线程执行；

2.高可用的获取锁和释放锁

3.高性能的获取锁和释放锁

4.具备可重入特性

5.具备锁失效机制，防止死锁

**Redis实现分布式锁的优缺点：**

优点：性能高，可解决失效死锁问题

缺点：实现较复杂，可靠性不如zookeeper（主从异步复制，可能造成上锁数据丢失）

> Redlock红锁：antirez提出的新的分布式锁的算法Redlock，基于N个完全独立的Redis节点（通常N可设置5）

**Redis实现分布式锁：**

1.使用setnx命令上锁，del释放锁

> SET lock 1 EX 10 NX

2.锁失效机制：可以设置预估的过期时间，更可靠的方式是锁续期（redisson使用watchDog实现锁续期）

3.可重入实现、防止释放别人的锁：加入唯一标识，判断是否当前客户端持有锁

5.

## 附录 Redis使用案例

### 1.短信验证码

```
1.验证码5分钟内有效
2.同一手机号每小时只能发送3个验证码
```

### 2.登录限制

```
1.五分钟内，密码输入错误5次，账户锁定20分钟
```

### 3.热点新闻

```
1.利用lpush+lrange+ltrim实现热点新闻
2.lrange分页
3.ltrim固定列表长度
```

### 4.消息队列

```
1.利用rpush+blpop实现消息队列
2.blpop实现消息阻塞消费(无消息时阻塞等待)
```

### 5.共同好友

```
1.利用sadd+sinter实现共同好友
2.利用sinter的取交集获取共同好友
```

### 6.随机抽奖

```
1.利用set集合的srandmember和spop随机抽奖
```

### 7.秒杀活动

```
1.利用redis中watch机制实现的乐观锁解决事务冲突
```




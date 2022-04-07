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

### 3.Redis的应用场景



### 4.Redis的优缺点

#### 1.Redis的优点

- **性能极高：**数据存储在内存中（QPS：11万，TPS：8万）
- **丰富的数据类型：**String、Hash、List、Set以及Ordered Set等
- **原子性：**Redis中所有操作都是原子性的，要么执行成功要么失败，单个操作是原子性的，多个操作也支持事务
- **丰富的特性：**支持publish/subscribe，通知，key过期等
- **高速读写：**Redis使用自己实现的分离器，代码量很短，没有锁，因此效率非常高
- **可持久化：**Redis直接将数据存储到内存中，但也提供了两种持久化机制，数据快照、日志文件

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

```
标准LRU实现原理
LRU会使用一个链表维护缓存中每个数据的访问情况，并根据数据的访问，实时调整数据在链表中的位置，链头(MRU)数据表示最近刚被访问过，链尾(LRU)数据表示最近最少被访问。如果严格按照标准LRU算法，Redis需要额外内存空间来保存链表，且当有数据插入或数据被访问时维护链表，不仅占用内存，而且还影响性能。

Redis中的LRU实现
Redis中提供了一个近似LRU算法实现。首先是设置了全局LRU时钟，并在KV对创建时获取全局LRU时钟值作为访问时间戳，及在每次访问时获取全局LRU时钟值，更新访问时间戳；然后，当Redis每处理一个命令，都调performEvictions判断是否需释放内存。若已使用内存超出maxmemory，则随机选择一些KV对，组成待淘汰候选集合，并根据它们的访问时间戳，选出最旧数据淘汰

具体步骤：
1.全局LRU时钟值计算：当redis服务启动会初始化全局LRU时钟值lruclock，每个redisObjec中的lru变量会保存最后一次被访问的时间戳，lruclock精度为1s（redisObject结构体会保存指向value的指针）
2.键值对LRU时钟值的初始化与更新：
3.近似LRU算法的实际执行：
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
hincrbyfloat key field increment：Hash表中字段field对应的浮点值自减，减量decrement
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

redis中Set是字符串的无序不可重复集合

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



## 五、Spring集成redis客户端

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

#### 序列化方式

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



























## Redis使用案例

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








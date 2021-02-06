基于spark实现的推荐系统，包含推荐引擎（RecommenderModule）、web后端（BusinessServerModule）、召回排序（RetrieveRankModule）、秒杀服务（SecKillModule）四个模块，每个模块都会与其他模块进行交互，相互配合，一起完成认证、推荐、秒杀、管理等功能。使用到的技术主要有springboot、spark、各种推荐算法、redis、kafka、mysql、mongoDB、deepFM、flask、redisson等等。。  
  
1.RecommenderModule子项目是推荐引擎核心，由scala语言编写，使用spark来实现离线和实时计算。  
1.1.基于spark-core和spark-sql实现了基于数据统计、基于内容、基于协同过滤、基于矩阵分解等离线计算及召回模块，这些模块用的是全量数据，反映用户的长期的兴趣偏好，计算结果会保存在mongoDB中。  
1.2.基于spark-streaming实现反映近期兴趣的实时推荐，用户近期访问会保存在redis中，用户点击物品时，日志被采集后送入kafka解耦，召回物品后由spark-streaming计算候选物品和保存在redis中的用户近期访问过的物品的相似度，将相似度高的物品推荐给用户，反映了用户近期的兴趣偏好；  
  
2.RetrieveRankModule是多路召回排序子项目，由python语言实现，采用deepFM算法，基于flask搭建后端接口，提供给RecommenderModule需要调用的接口。  
2.1.由于没有实际数据，因此在测试时用movielens数据集训练deepFM模型，得到模型权重参数文件，然后用flask搭建后端，通过多路召回策略来召回候选物品，召回的方法有：近期最高评分召回、近期最多评分召回、标签召回、协同过滤召回、矩阵分解召回等。  
2.2.召回多个物品(需要排除用户已经评分或者浏览的物品)，然后用训练好的deepFM进行综合排序，取回一定数目的得分比较高的候选推荐物品，在RecommenderModule项目中进行包装之后推荐给用户；  
  
3.BusinessServerModule是推荐系统的web后端，由java语言编写，使用springboot框架。  
3.1.实现了基于token的用户注册登录认证，并通过redis缓存账户信息和token的方式来保证用户登录的唯一性；  
3.2.用户点击物品评分时，会将评分日志写入到日志文件中，通过flume采集日志后，经kafka处理后写入到mongo数据库，同时将用户近期评分保存在redis，RecommenderModule中的online模块会通过spark-streaming计算用户近期偏好；  
3.3.提供多个api可以直接将RecommenderModule中各个召回模块计算出来的，已经保存在mongoDB中的离线推荐物品推荐给用户，以及可以调用RetrieveRankModule中多路召回综合排序后的物品推荐给用户；
3.4.热榜的实现用到了redis进行缓存和定时更新，减少数据库压力，由于多路召回综合排序延时较大，因此通过redis缓存和异步更新策略来优化体验；  
3.5.还实现了kafka消息消费者，负责消费秒杀模块中产生的订单消息，生成订单时会扣数据库库存以及将订单写入数据库，使用了spring事务来保证原子性，用户取消订单时同样需要使用事务并恢复redisson的信号量值。  
  
4.秒杀的具体实现：  
4.1.限流：基于Alibaba Sentinel来实现秒杀接口限流，此外还实现了基于漏桶的限流，基于redis计数的限流。  
4.2.缓存预热：通过定时任务定时上架近期秒杀商品到redis进行预热库存，访问秒杀场次信息和商品信息时，只需要查询redis缓存。  
4.3.防止链接暴露：上架商品时，会生成商品的随机码防止链接暴露恶意被刷。  
4.4.防止重复下单：以用户id和商品id作为key，通过redis的setIfAbsent命令占位来实现，如果用户未下过单，则设置key并返回true，否则返回false组织用户重复下单。  
4.5.分布式锁：通过分布式锁来预扣库存，信号量的值初始设置位商品库存，每次用户要秒杀时则信号量减去秒杀的商品数量，如果信号值为0则说明没有库存，返回失败。  
4.6.异步生成订单：如果用户抢到信号量，则生成一个订单信息，并将这个订单信息发生给kafka消息队列，由BusinessServerModule中的kafka的消费者获取消息并异步创建订单，进行流量削峰。  
4.7.事务管理：在kafka消费者生成订单时，扣数据库库存和生成订单到数据库应该为一个事务操作，在事务失败时需要回滚数据库的值，并需要恢复分布式锁的信号值，以及删除用户已下单的占位。  
4.8.结合nginx还可以做负载均衡，由保存在redis中token来进行用户token共享，由redis中的用户占位来避免重复下单问题。
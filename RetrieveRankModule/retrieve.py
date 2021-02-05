# -*- coding: utf-8 -*-

'''
热榜召回
@param
    mongo:mongoDB连接对象
    cfg:配置参数对象
    exclude_con:查询时的排除条件
@return
    res_item_list:召回的物品id列表
'''
def retrieve_hotList(mongo,cfg,exclude_con):
    itemList=mongo.db.RateMoreRecentlyProducts.find({'$or':exclude_con}).limit(cfg.n_hotList)
    res_item_list=[]
    for item in itemList:
        res_item_list.append(item['productId'])
    return res_item_list
'''
高评分召回
@param
    mongo:mongoDB连接对象
    cfg:配置参数对象
    exclude_con:查询时的排除条件
@return
    res_item_list:召回的物品id列表
'''
def retrieve_highScoreList(mongo,cfg,exclude_con):
    itemList=mongo.db.AverageScoreProducts.find({'$or':exclude_con}).limit(cfg.n_highScoreList)
    res_item_list=[]
    for item in itemList:
        res_item_list.append(item['productId'])
    return res_item_list
'''
召回相似用户历史评分物品
@param
    mongo:mongoDB连接对象
    cfg:配置参数对象
    exclude_con:查询时的排除条件
    userIds:相似用户列表
@return
    res_item_list:召回的物品id列表
'''
def retrieve_rating_item(mongo,cfg,userIds,exclude_con):
    cond_list=[]
    for userId in userIds:
        cond_list.append({'userId':userId})
    #cond_list.extend(exclude_con)
    #print(cond_list)
    condition={'$and':[{'$or':cond_list},{'$or':exclude_con},{'timestamp':{'$gt':875071515-10000}}]}
    itemList=mongo.db.Rating.find(condition).limit(cfg.n_userCF)
    res_item_list=[]
    for item in itemList:
        res_item_list.append(item['productId'])
    return res_item_list
'''
召回历史评分物品
@param
    mongo:mongoDB连接对象
    userId:用户id
@return
    his_item_ids:召回的历史评分物品id列表
'''
def retrieve_history_item(mongo,userId):
    itemList=mongo.db.Rating.find({'userId':userId})
    his_item_ids=[]
    for item in itemList:
        his_item_ids.append(item['productId'])
    return his_item_ids
'''
生成查询排除条件，查询时排除his_item_ids中的物品id
@param
    his_item_ids:历史评分物品id列表
@return
    exclude_con:查询时排除条件
'''
def gen_exclude_condition(his_item_ids):
    exclude_con=[]
    for item_id in his_item_ids:
        exclude_con.append({'productId':{'$ne':item_id}})
    return exclude_con
'''
基于用户协同过滤召回
@param
    mongo:mongoDB连接对象
    cfg:配置参数对象
    userId:用户id
    exclude_con:查询时的排除条件
@return
    res_item_list:召回的物品id列表
'''
def retrieve_userCF(mongo,cfg,userId,exclude_con):
    userRec=mongo.db.UserCFProductRecs.find_one({"userId":userId})
    user_recs=userRec['recs']
    if(len(user_recs)>cfg.n_simUser):
        user_recs=user_recs[0:cfg.n_simUser]
    userIds=[]
    for user in user_recs:
        userIds.append(user['userId'])
    return retrieve_rating_item(mongo,cfg,userIds,exclude_con)
'''
基于矩阵分解召回
@param
    mongo:mongoDB连接对象
    cfg:配置参数对象
    userId:用户id
    his_item_ids:历史评分物品id列表
@return
    item_ids:召回的物品id列表
'''
def retrieve_MFactorization(mongo,cfg,userId,his_item_ids):
    userRec=mongo.db.UserRecs.find_one({"userId":userId})
    item_recs=userRec['recs']
    item_ids=[]
    n=0
    for item in item_recs:
        item_id=item['productId']
        if item_id in his_item_ids:
            continue
        item_ids.append(item_id)
        n+=1
        if n>cfg.n_MFactorization:
            break
    return item_ids

'''
多路召回策略
@param
    mongo:mongoDB连接对象
    cfg:配置参数对象
    userId:用户id
@return
    item_ids_set:召回的物品id集合，去除了重复id
'''
def retrieve_mutiPathItems(mongo,cfg,userId):
    his_item_ids=retrieve_history_item(mongo,userId)
    exclude_con=gen_exclude_condition(his_item_ids)
    #多路召回
    hotList_ids=retrieve_hotList(mongo,cfg,exclude_con)
    highScoreList_ids=retrieve_highScoreList(mongo,cfg,exclude_con)
    userCF_ids=retrieve_userCF(mongo,cfg,userId,exclude_con)
    mFactorization_ids=retrieve_MFactorization(mongo,cfg,userId,his_item_ids)
    item_ids_set=set()
    item_ids_set=item_ids_set.union(set(hotList_ids)).union(set(highScoreList_ids)).union(set(userCF_ids)).union(set(mFactorization_ids))
    return item_ids_set


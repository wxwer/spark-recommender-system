# -*- coding: utf-8 -*-


from deepctr.models import DeepFM
import pickle
import numpy as np
import logging

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
handler = logging.FileHandler('./logs/stdout.log')
formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(module)s - %(funcName)s - %(lineno)d - %(message)s")
handler.setFormatter(formatter)
logger.addHandler(handler)

'''
加载模型及模型参数
@param
    feature_columns_file:特征列文件
    checkpoint_file:模型权重参数
@return
    model:加载后的模型
'''
def load_model(feature_columns_file,checkpoint_file):
    with open(feature_columns_file,'rb') as f:
        fixed_feature_columns=pickle.load(f)
    model = DeepFM(fixed_feature_columns, fixed_feature_columns, task='binary',l2_reg_embedding=1e-5)
    model.load_weights(checkpoint_file)
    return model
'''
使用训练后的模型进行综合排序
@param
    model:加载后的模型
    pred_input:指定格式的模型输入数据
    cfg:配置参数对象
@return
    rank_item_ids:综合排序后的物品id
'''
def predict_rank(model,pred_input,count):
    pred_res=model.predict(pred_input,batch_size=256)
    pred_res=np.squeeze(pred_res,axis=1)
    index=np.flipud(np.argsort(pred_res))
    sorted_pred_res=np.flipud(np.sort(pred_res))
    rank_item_ids=pred_input['mid'][index]
    rank_items=[]
    for i in range(len(rank_item_ids)):
        if i<10:
            continue
        rank_items.append({'productId':str(rank_item_ids[i]),'score':str(sorted_pred_res[i])})
        if i-10>count:
            break
    return rank_items
'''
生成指定格式的模型输入数据
@param
    mongo:mongo连接对象
    userId:用户id
    item_ids:召回的物品id
@return
    pred_input:指定格式的模型输入数据
'''
def create_pred_input(mongo,userId,item_ids):
    user=mongo.db.User.find_one({"userId":userId})
    uid_list=[]
    mid_list=[]
    gender_list=[]
    occp_list=[]
    age_list=[]
    genre_list=[]
    sparse_features = ["uid", "mid","gender", "occp",'genre']
    for item_id in item_ids:
        item=mongo.db.Product.find_one({"productId":item_id})
        uid_list.append(user['userId'])
        mid_list.append(item_id)
        gender_list.append(user['gender'])
        occp_list.append(user['occupation'])
        age_list.append(user['age'])
        try:
            genre=int(item['tags'].split('|')[0])
        except Exception as e:
            logger.error(e)
            genre=0
        genre_list.append(genre)
    pred_input={'uid':np.array(uid_list),'mid': np.array(mid_list),'gender': np.array(gender_list),
                  'occp': np.array(occp_list),'genre': np.array(genre_list),'age': np.array(age_list)}
    for feat in sparse_features:
        with open('./model/'+feat+'_label_encoder.pkl','rb') as f:
            lbe=pickle.load(f)
            pred_input[feat] = lbe.fit_transform(pred_input[feat])
    return pred_input

'''
model=load_model(cfg.feature_columns_file,cfg.checkpoint_file)
pred_model_input={'uid':np.array([545]),'mid': np.array([221]),'gender': np.array([1]),
                      'occp': np.array([6]),'genre': np.array([1]),'age': np.array([20])}
pred_res=model.predict(pred_model_input)
print(pred_res)
'''
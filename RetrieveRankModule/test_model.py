# -*- coding: utf-8 -*-
import numpy as np
import tensorflow as tf
from deepctr.models import DeepFM,DCN,DIN,xDeepFM,WDL
import pickle


with open('./model/feature_columns.pkl','rb') as f:
    fixed_feature_columns=pickle.load(f)
print(fixed_feature_columns)
model = DeepFM(fixed_feature_columns, fixed_feature_columns, task='binary',l2_reg_embedding=1e-5)
model.load_weights('./model/ml100k_checkpoint.h5')

pred_model_input={'uid':np.array([545,454]),'mid': np.array([221,221]),'gender': np.array(['F','M']),
                  'occp': np.array(['technician','technician']),'genre': np.array([1,2]),'age': np.array([20,15])}

data=pred_model_input
sparse_features = ["uid", "mid","gender", "occp",'genre']
for feat in sparse_features:
    with open('./model/'+feat+'_label_encoder.pkl','rb') as f:
        lbe=pickle.load(f)
    data[feat] = lbe.fit_transform(data[feat])
print(data)
pred_res=model.predict(data)
print(pred_res)
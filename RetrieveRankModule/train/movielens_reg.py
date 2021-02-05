import pandas as pd
from sklearn.metrics import mean_squared_error,log_loss, roc_auc_score,accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder,MinMaxScaler
import numpy as np
import tensorflow as tf
from deepctr.models import DeepFM,DCN,DIN,xDeepFM,WDL
from deepctr.feature_column import SparseFeat,get_feature_names,DenseFeat
import pickle

def func(x):
    if x>0.5:
        return 1
    else:
        return 0
if __name__ == "__main__":
    file=r'D:\files\code\RecommendSystem\data\ml-latest-small\ml_100k_feat.csv'
    data = pd.read_csv(file)
    sparse_features = ["uid", "mid","gender", "occp",'genre']
    dense_features = ["age"]
    data[dense_features] = data[dense_features].fillna(0, )
    target = ['rating']

    # 1.Label Encoding for sparse features,and do simple Transformation for dense features
    for feat in sparse_features:
        lbe = LabelEncoder()
        data[feat] = lbe.fit_transform(data[feat])
        with open(feat+'_label_encoder.pkl','wb') as f:
            pickle.dump(lbe,f)
    #mms = MinMaxScaler(feature_range=(0, 1))
    #data[dense_features] = mms.fit_transform(data[dense_features])
    # 2.count #unique features for each sparse field
    '''
    with open('labelEncoder.pkl','wb') as f:
        pickle.dump(lbe,f)
    with open('labelEncoder.pkl','rb') as f:
        lbe2=pickle.load(f)
    print(lbe2.classes_)
    '''
    

    fixlen_feature_columns = [SparseFeat(feat, data[feat].nunique(),embedding_dim=5)
                              for feat in sparse_features]+[DenseFeat(feat, 1, )
                                                              for feat in dense_features]
    linear_feature_columns = fixlen_feature_columns
    dnn_feature_columns = fixlen_feature_columns
    feature_names = get_feature_names(linear_feature_columns + dnn_feature_columns)
    print(dnn_feature_columns)
    
    with open('feature_columns.pkl','wb') as f:
        pickle.dump(fixlen_feature_columns,f)
    
    # 3.generate input data for model
    train, test = train_test_split(data, test_size=0.3, random_state=2021)
    train_model_input = {name:train[name].values for name in feature_names}
    test_model_input = {name:test[name].values for name in feature_names}
    #print(test_model_input)
    # 4.Define Model,train,predict and evaluate
    model = DeepFM(linear_feature_columns, dnn_feature_columns, task='binary',l2_reg_embedding=1e-5)
    '''
    model.load_weights('./ml100k_checkpoint.h5')
    pred_model_input={'uid':np.array([545]),'mid': np.array([221]),'gender': np.array([1]),
                      'occp': np.array([6]),'genre': np.array([1]),'age': np.array([20])}
    print(model.predict(pred_model_input),)
    '''
    
    
    model.compile("adam", "binary_crossentropy", metrics=["binary_crossentropy"], )

    history = model.fit(train_model_input, train[target].values,
                        batch_size=256, epochs=5, verbose=2, validation_split=0.2, )
    pred_ans = model.predict(test_model_input, batch_size=256)
    print("")
    print("test LogLoss", round(log_loss(test[target].values, pred_ans), 4))
    print("test AUC", round(roc_auc_score(test[target].values, pred_ans), 4))
    pred_ans=[[func(pred[0])] for pred in pred_ans]
    print("test_accuracy",round(accuracy_score(test[target].values, pred_ans), 4))
    
    #print("test[target].values",test[target].values)
    pred_model_input={'uid':np.array([545]),'mid': np.array([221]),'gender': np.array([1]),
                      'occp': np.array([6]),'genre': np.array([1]),'age': np.array([20])}
    print(model.predict(pred_model_input),)
    model.save_weights('./ml100k_checkpoint.h5')
    print(type(model))
    #model.save('./ml100k_checkpoint.h5')
    #print(model.predict(test_model_input, batch_size=256))
    
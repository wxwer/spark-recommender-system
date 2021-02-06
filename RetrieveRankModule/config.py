# -*- coding: utf-8 -*-

class Config:
    def __init__(self):
        self.mongo_url='mongodb://localhost:27017/test'
        self.mongo_port=27017
        
        self.feat_sizes_file='./weights/feat_sizes.pkl'
        self.checkpoint_file='./weights/ml100k_checkpoint.h5'
        
        self.n_hotList=20
        self.n_highScoreList=20
        self.n_simUser=5
        self.n_userCF=100
        self.n_MFactorization=100
        
        self.n_rank=100
        
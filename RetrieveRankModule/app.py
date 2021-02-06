# -*- coding: utf-8 -*-

from flask import Flask,make_response
from flask_pymongo import PyMongo
import json
from flask import request
from config import Config
from utils import load_model,predict_rank,create_pred_input
from retrieve import retrieve_mutiPathItems
import logging
#------------log------------#
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
handler = logging.FileHandler('./logs/stdout.log')
formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(module)s - %(funcName)s - %(lineno)d - %(message)s")
handler.setFormatter(formatter)
logger.addHandler(handler)
#-----------init------------#
app = Flask(__name__)
cfg=Config()
app.config["MONGO_URI"] = cfg.mongo_url
mongo=PyMongo(app)

@app.route('/retrieve',methods = ['GET'])
def retrieve_rank():
    try:
        userId=request.args.get("userId",type=int)
        count=request.args.get("count",type=int)
        logger.info("userId=%s retrieves items"%userId)
        model=load_model(cfg.feat_sizes_file,cfg.checkpoint_file)
        item_ids=retrieve_mutiPathItems(mongo,cfg,userId)
        pred_input=create_pred_input(mongo,userId,item_ids)
        rank_items=predict_rank(model,pred_input,count)
        #print(rank_items)
        resp={'errorCode':200,
         'msg':'success',
         'data':json.dumps(rank_items)
            }
    except Exception as e:
        logger.error(e)
        resp={'errorCode':500,
             'msg':str(e),
             'data':''
             }
    response=make_response(json.dumps(resp,ensure_ascii=False))
    response.headers['Content-Type']='application/json'
    return response

if __name__ == "__main__":
    app.run(host='localhost', port=8585,debug=True)
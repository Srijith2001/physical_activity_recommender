import pandas as pd
import numpy as np
import io
import matplotlib.pyplot as plt
from scipy.stats import mode
from statistics import mean
from os.path import dirname, join

activity_plan = []

def get_activity_plan(cluster,day,target):
    # df = pd.read_csv("dataset/SecondLevel/Cluster "+str(cluster+1)+'/'+day+'.csv')
    filename = join(dirname(__file__), "libs/secondLevel/cluster_"+str(cluster+1)+'/'+day+'.csv')
    df = pd.read_csv(filename)
    largest = int(mode(df['Cluster'])[0])
    temp = []
    count = 0
    row,column = 0,0
    for index in df.index:
        if df.at[index,'Cluster']==largest:
            temp.append(df.at[index,'ID'])
            count+=1
    hour = [[0 for i in range(24)] for j in range(count)]
    ttl_hr_avg = [0] * 24
    for id in temp:
        f = join(dirname(__file__),"libs/steps/" + str(id) + ".csv")
        user = pd.read_csv(f,index_col = 0)
        column = 0
        for col in user.columns:
            if col=='Total Steps':
                continue
            hour[row][column] = user.at[day.split('.')[0].capitalize(),col].mean()
            column+=1
        row+=1
    ttl_hr_avg = ([mean(x) for x in zip(*hour)])
    ttl_steps = sum(ttl_hr_avg)
    
    
    for i in range(24):
        activity_plan.append((ttl_hr_avg[i]/ttl_steps)*target)
    
    fig = plt.figure()
    ax = fig.add_axes([0,0,1,1])
    x = [i for i in range(1,25)]
    ax.bar(x,activity_plan)
    f = io.BytesIO()
    plt.savefig(f, format="png")
    return f.getvalue()

def ret_act_plan():
    return  activity_plan
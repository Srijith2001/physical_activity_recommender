import pandas as pd
import pickle
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from os.path import dirname, join

def dataset_split(activity,cluster,day):
    filename = join(dirname(__file__), "libs/predictive_model_dataset.csv")
    df = pd.read_csv(filename)
    data = pd.DataFrame(columns = ['ID','Day','h_num','cSteps','h_tar','h_t_ac','daily_tar','prediction','Contextual_group','Activity Pattern'])
    for index in df.index:
        if(int(df.at[index,"Activity Pattern"])==activity and int(df.at[index,"Contextual_group"])==cluster and df.at[index,"Day"]==day):
            dict1 = dict([('ID',df.at[index,"ID"]),('Day',df.at[index,"Day"]),('h_num',df.at[index,"h_num"]),('cSteps',df.at[index,'cSteps']),('h_tar',df.at[index,"h_tar"]),('h_t_ac',df.at[index,"h_t_ac"]),('daily_tar',df.at[index,"daily_tar"]),('prediction',df.at[index,"prediction"]),('Contextual_group',df.at[index,"Contextual_group"]),('Activity Pattern',df.at[index,"Activity Pattern"])])
            data = data.append(dict1,ignore_index = True)
    return data

def trainModel(ap,cgroup,currDay):
    df = dataset_split(ap,cgroup,currDay)
    y = df.prediction
    features = ['h_num','cSteps','h_tar','h_t_ac','daily_tar','Contextual_group','Activity Pattern']
    X = df[features]
    y = y.astype('int')
    clf = RandomForestClassifier()  
    clf.fit(X, y)
    filename = join(dirname(__file__), "libs/predictive_model.sav")
    pickle.dump(clf, open(filename, 'wb'))
    return True

def willTargetAchieve(h_num,cSteps,h_tar,h_t_ac,daily_tar,cgroup,ap):
    # performing predictions on the test dataset
    filename = join(dirname(__file__), "libs/predictive_model.sav")
    loaded_model = pickle.load(open(filename, 'rb'))
    test = [[h_num,cSteps,h_tar,h_t_ac,daily_tar,cgroup,ap]]
    y_pred = loaded_model.predict(test)
    return y_pred[0]
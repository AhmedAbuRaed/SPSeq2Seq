# Automatic Related Work Section Generation: Experiments in Scientific Document Abstracting
This repo contains the tools and data to reproduce the work that has been done on the scientific paper published at <XXX>.
## OpenNMT-py   
  
First of all you will need OpenNMT-py, we recoomend following the offical doc page: 

https://opennmt.net/OpenNMT-py/main.html   

We recommend getting the sources: 

git clone https://github.com/OpenNMT/OpenNMT-py.git   

cd OpenNMT-py    

python setup.py install    


After that you will need to download the packages from the requerments file    

pip install -r requirements.opt.txt 


## data &nbsp;
You can get the data at ScientificPapers.zip    

you can also find a trainining, validation and testing datasets at the DataSet folder 

## resources &nbsp;
Used to filter the data:   

presentation_noun.lst and first_pron.lst

Used by the JAVA classes   

Utilities.java

## code
Extract Title Abstracts (Source) and Citation (Target) pairs from MAG :  ExtractMAGAbstractsCitationsCorpus.java   

Index with elastic search engine: IndexMAGAbstractsCitations.java   

Filtered Pairs : MAGElasticFilteredClosestPairs.java   

Microsoft Academic Graph : MAGElasticPairs.java   

Encapsulators for MAG formated Json array: MAGMetaData.java and MAGExtendedMetaData.java    

Testing Dataset:   TACTestingPairs.java   

Filer data with Python : filter.py    

Replace citation markers with <CITE>: preprocesstarget.py    
  

PS: you have to replace XXX with your own MAG code or with your Elastic Server IP to index them.    





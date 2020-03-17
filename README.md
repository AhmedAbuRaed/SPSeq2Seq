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

## implemnettaion
### internal representation
``` 
python3.6 preprocess.py -train_src data/TACClosest4/train.txt.src -train_tgt data/TACClosest4/train.txt.tgt.tagged -valid_src data/TACClosest4/val.txt.src -valid_tgt data/TACClosest4/val.txt.tgt.tagged -save_data data/TACClosest4/TACClosest4 -src_seq_length 10000 -tgt_seq_length 10000 -src_seq_length_trunc 600 -tgt_seq_length_trunc 200 -dynamic_dict -share_vocab -shard_size 100000

python3.6 train.py -data data/TACClosest4/TACClosest4 -save_model models/TACClosest4 -layers 4 -rnn_size 512 -word_vec_size 512 -max_grad_norm 0 -optim adam -encoder_type transformer -decoder_type transformer -position_encoding -dropout 0.2 -param_init 0. -warmup_steps 8000 -learning_rate 2 -decay_method noam -label_smoothing 0.1 -adam_beta2 0.998 -batch_size 4096 -batch_type tokens -normalization tokens -max_generator_batches 2 -accum_count 4 -share_embeddings -copy_attn -param_init_glorot -report_every 5000 -save_checkpoint_steps 5000 -valid_steps 5000 -train_steps 200000 -world_size 2 -gpu_ranks 0 1

for i in {5000..200000..5000}
do
    python3.6 translate.py -gpu 0 -batch_size 20 -beam_size 10 -model models/TACClosest4_step_$i.pt -src data/TACClosest4/test.txt.src -output TACClosest4_predicted_step_$i.txt -min_length 35 -verbose -stepwise_penalty -coverage_penalty summary -beta 5 -length_penalty wu -alpha 0.9 -block_ngram_repeat 3 -ignore_when_blocking "." "</t>" "<t>" -replace_unk
done;
```

### word2vec from pre-trained model represenattion
```
python3.6 tools/embeddings_to_torch.py -emb_file_enc "embeddings/cc.en.300.vec" -emb_file_dec "embeddings/cc.en.300.vec" -dict_file data/TACClosest4/TACClosest4.vocab.pt -output_file "./embeddings_fasttext4"

python3.6 train.py -data data/TACClosest4/TACClosest4 -save_model models/TACClosest4_using_embeddings_fasttext -pre_word_vecs_enc embeddings_fasttext4.enc.pt -pre_word_vecs_dec embeddings_fasttext4.dec.pt -layers 4 -rnn_size 512 -word_vec_size 512 -max_grad_norm 0 -optim adam -encoder_type transformer -decoder_type transformer -position_encoding -dropout 0.2 -param_init 0. -warmup_steps 8000 -learning_rate 2 -decay_method noam -label_smoothing 0.1 -adam_beta2 0.998 -batch_size 4096 -batch_type tokens -normalization tokens -max_generator_batches 2 -accum_count 4 -share_embeddings -copy_attn -param_init_glorot -report_every 5000 -save_checkpoint_steps 5000 -valid_steps 5000 -train_steps 200000 -world_size 1 -gpu_ranks 0

for i in {5000..200000..5000}
do
    python3.6 translate.py -gpu 0 -batch_size 20 -beam_size 10 -model models/TACClosest4_using_embeddings_fasttext_step_$i.pt -src data/TACClosest4/test.txt.src -output TACClosest4CBW2V_predicted_step_$i.txt -min_length 35 -verbose -stepwise_penalty -coverage_penalty summary -beta 5 -length_penalty wu -alpha 0.9 -block_ngram_repeat 3 -ignore_when_blocking "." "</t>" "<t>" -replace_unk
done;




```





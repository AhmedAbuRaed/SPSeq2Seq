import nltk.data
import re
sent_detector = nltk.data.load('tokenizers/punkt/english.pickle')
presentation_noun = set(open("presentation_noun.lst", "rb").read().decode("utf8").splitlines())
presentation_noun = set(["this " + i for i in presentation_noun])
first_pron = set(open("first_pron.lst", "rb").read().decode("utf8").splitlines())
keywords = presentation_noun | first_pron

src = open("TACtrain.txt.src", "rb").read().decode("utf8").splitlines()
tgt = open("TACtrain.txt.tgt.tagged", "rb").read().decode("utf8").splitlines()

filtered_src = open("filteredTACtrain.txt.src", "w", encoding='utf-8')
filtered_tgt = open("filteredTACtrain.txt.tgt.tagged", "w", encoding='utf-8')
final_src = list()
final_tgt = list()
count =0
for sline, tline in zip(src, tgt):
    count += 1
    print(count)
    sentences = sent_detector.tokenize(sline)
    title = sentences[0].lower()
    temp = title
    abstract = sentences[1:]
    abstract = [s.lower() for s in abstract]
    for s in abstract:
        for word in keywords:
            match = re.search(".*(\\b" + word + "\\b)+.*", s)
            if match:
                temp = temp + " " + s
                break
    final_src.extend([temp])
    final_tgt.extend([tline])

for fsline, ftline in zip(final_src, final_tgt):
    filtered_src.write(fsline + "\n")
    filtered_tgt.write(ftline + "\n")

filtered_src.close()
filtered_tgt.close()

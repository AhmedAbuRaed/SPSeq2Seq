import re

src = open("train.txt.src", "rb").read().decode("utf8").splitlines()
tgt = open("train.txt.tgt.tagged", "rb").read().decode("utf8").splitlines()

new_src = open("trainCITE.txt.src", "w", encoding='utf-8')
new_tgt = open("trainCITE.txt.tgt.tagged", "w", encoding='utf-8')

for sline, tline in zip(src,tgt):
    tline = re.sub("(\([^()]*\d{2,4}[^()]*\))|([A-Z]\w+\s*.{1,8}\([^()]*\d{4}[^()]*\))|([A-Z]\w+\s*\([^()]*\d{4}[^()]*\))|([A-Z]\w+\s*and\s*[A-Z]\w+\s*\([^()]*\d{4}\))|(\[[^\[\]]*\d{1,4}[^\[\]]*\])", "<CITE>", tline)
    tline = re.sub("\d{1}", "#", tline)
    sline = re.sub("\d{1}", "#", sline)
    new_src.write(sline.lower() + "\n")
    new_tgt.write(tline.lower() + "\n")

new_src.close()
new_tgt.close()
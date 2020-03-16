package edu.upf.taln.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.util.*;

public class MAGElasticFilteredClosestPairs {
    public static void main(String args[]) {
        Set<String> keywords = new HashSet<String>();
        SentenceDetectorME sentenceDetector = null;
        try {
            System.out.println("Creating REST Clients ...");
            RestClient elasticRestClient = Utilities.buildRestClientConnection("XXX", 80, "http", Optional.empty(), Optional.of(new Header[]{}));
            System.out.println("REST Clients Connected.");

            Set<String> firstPronoun = Sets.newHashSet(Files.readLines(new File("first_pron.lst"), Charsets.UTF_8));
            Set<String> presentationNoun = Sets.newHashSet(Files.readLines(new File("presentation_noun.lst"), Charsets.UTF_8));
            for (String item : presentationNoun) {
                keywords.add("this " + item);
            }
            keywords.addAll(firstPronoun);

            InputStream modelIn = new FileInputStream("en-sent.bin");
            SentenceModel model = new SentenceModel(modelIn);

            sentenceDetector = new SentenceDetectorME(model);

            HashMap<String, Pair<String,List<String>>> TAC = new HashMap<String, Pair<String,List<String>>>();

            FileWriter sourceFW = null;
            FileWriter targetFW = null;
            try {
                sourceFW = new FileWriter(new File("ElasticnoSourceRepeatedClosest.src"));
                targetFW = new FileWriter(new File("ElasticnoSourceRepeatedClosest.tgt"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            int count = 0;
            int size = 5000;
            int from = 0;
            for (int i = 0; i < 6; i++) {
                JsonNode respond = Utilities.getElasticSearchFrom(elasticRestClient, "mag", "paper", size, from);

                System.out.println(respond.path("hits").path("hits").size());

                for (JsonNode node : respond.path("hits").path("hits")) {
                    JsonNode source = node.path("_source");
                    MAGMetaData paper = MAGMetaData.getMAGMetaDataFromJsonString(source.toString());
                    if (paper != null && paper.getExtendedMetaData() != null) {
                        MAGExtendedMetaData extendedMetaData = MAGExtendedMetaData.getMAGMetaDataFromJsonString(paper.getExtendedMetaData());
                        if (extendedMetaData != null && extendedMetaData.getCitationContexts() != null) {
                            Set<String> keys = extendedMetaData.getCitationContexts().keySet();
                            for (String referencePaperID : keys) {
                                try {
                                    MAGMetaData referencePaper = MAGMetaData.getMAGElasticMetaDataFromID(Long.valueOf(referencePaperID.replaceAll("\"", "").trim()), elasticRestClient);
                                    if (referencePaper != null && referencePaper.getExtendedMetaData() != null) {
                                        MAGExtendedMetaData referencePaperExtendedMetaData = MAGExtendedMetaData.getMAGMetaDataFromJsonString(referencePaper.getExtendedMetaData());
                                        if (referencePaperExtendedMetaData != null && referencePaperExtendedMetaData.getInvertedAbstract() != null) {
                                            String citingPaperString = cleanString(referencePaper.getTitle()).toLowerCase() + "." ;

                                            String abs = cleanString(ExtractMAGAbstractsCitationsCorpus.getAbstract(referencePaperExtendedMetaData.getInvertedAbstract().getInvertedIndex())).toLowerCase();

                                            String citingAbstractSentences[] = sentenceDetector.sentDetect(abs);


                                            for (int k = 0; k < citingAbstractSentences.length; k++) {
                                                String sentence = citingAbstractSentences[k].toLowerCase();
                                                for (String word : keywords) {
                                                    if (sentence.matches(".*(\\b" + word + "\\b)+.*")) {
                                                        citingPaperString += " " + sentence;
                                                        break;
                                                    }
                                                }
                                            }

                                            ArrayList<String> citationsList = (ArrayList<String>) extendedMetaData.getCitationContexts().get(referencePaperID);
                                            for (String cit : citationsList) {
                                                if (cit.length() > 35) {
                                                    if(TAC.containsKey(referencePaperID))
                                                    {
                                                        List<String> temp = new ArrayList<String>(TAC.get(referencePaperID).getRight());
                                                        temp.add(cleanString(cit));
                                                        TAC.put(referencePaperID, Pair.of(cleanString(citingPaperString), temp));
                                                    }
                                                    else {
                                                        TAC.put(referencePaperID, Pair.of(cleanString(citingPaperString), Arrays.asList(cleanString(cit))));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    count++;
                }
                from += size;
            }

            for(String key: TAC.keySet())
            {
                String TA = TAC.get(key).getLeft();
                List<String> C = TAC.get(key).getRight();

                System.out.println(TA + " -> " + Joiner.on(" | ").join(C));

                String citance = C.get(0);
                double max = 0d;
                for(String c: C) {
                    double smoothBLUEScore = edu.stanford.nlp.mt.metrics.BLEUMetric.computeLocalSmoothScore(c,
                            Arrays.asList(TA), 2);
                    if (smoothBLUEScore > max) {
                        max = smoothBLUEScore;
                        citance = c;
                    }
                }
                sourceFW.write(TA + "\r\n");
                targetFW.write("<t>" + citance + "</t>" + "\r\n");
            }


            sourceFW.close();
            targetFW.close();
            System.out.println("Closing Clients");
            System.out.println(count);
            try {
                elasticRestClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Closing Clients");
        }


    }

    private static String cleanString(String s) {
        return s.replaceAll("\\R+", " ")
                .replaceAll("[^a-zA-Z0-9,.!?\\[\\]\\(\\)\\s+]", " ");
    }
}

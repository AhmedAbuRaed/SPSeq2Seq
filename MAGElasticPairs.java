package edu.upf.taln.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.jena.base.Sys;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.search.SearchHit;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MAGElasticPairs {
    public static void main(String args[]) {

        try {
            System.out.println("Creating REST Clients ...");
            RestClient elasticRestClient = Utilities.buildRestClientConnection("XXX", 80, "http", Optional.empty(), Optional.of(new Header[]{}));
            System.out.println("REST Clients Connected.");
            Set<String> covered = new HashSet<>();

            FileWriter sourceFW = null;
            FileWriter targetFW = null;
            try {
                sourceFW = new FileWriter(new File("ElasticnoSourceRepeated.src"));
                targetFW = new FileWriter(new File("ElasticnoSourceRepeated.tgt"));
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
                            //Utilities.putElasticSearch(MAGMetaData.getJsonStringFromMAGMetaData(paper), "mag", "paper", paperID, elasticRestClient);
                            Set<String> keys = extendedMetaData.getCitationContexts().keySet();
                            for (String referencePaperID : keys) {
                                try {
                                    MAGMetaData referencePaper = MAGMetaData.getMAGElasticMetaDataFromID(Long.valueOf(referencePaperID.replaceAll("\"", "").trim()), elasticRestClient);
                                    if (referencePaper != null && referencePaper.getExtendedMetaData() != null) {
                                        MAGExtendedMetaData referencePaperExtendedMetaData = MAGExtendedMetaData.getMAGMetaDataFromJsonString(referencePaper.getExtendedMetaData());
                                        if (referencePaperExtendedMetaData != null && referencePaperExtendedMetaData.getInvertedAbstract() != null) {
                                            //if (!covered.contains(paper.getId()) && !covered.contains(referencePaper.getId())) {
                                            //Utilities.putElasticSearch(MAGMetaData.getJsonStringFromMAGMetaData(referencePaper), "mag", "paper", referencePaperID, elasticRestClient);
                                            //System.out.println(paper.getId() + " -> " + referencePaper.getId());
                                            ArrayList<String> citationsList = (ArrayList<String>) extendedMetaData.getCitationContexts().get(referencePaperID);
                                            for (String cit : citationsList) {
                                                if (cit.length() > 35 && !covered.contains(cleanString(referencePaper.getTitle() + ". " + ExtractMAGAbstractsCitationsCorpus.getAbstract(referencePaperExtendedMetaData.getInvertedAbstract().getInvertedIndex())))) {
                                                    sourceFW.write(cleanString(referencePaper.getTitle() + ". " + ExtractMAGAbstractsCitationsCorpus.getAbstract(referencePaperExtendedMetaData.getInvertedAbstract().getInvertedIndex())) + "\r\n");
                                                    targetFW.write("<t>" + cleanString(cit.replaceAll("(?!\\r|\\n|\\t)[\\x00-\\x1f\\x80-\\x9f]", " ")) + "</t>" + "\r\n");
                                                    covered.add(cleanString(referencePaper.getTitle() + ". " + ExtractMAGAbstractsCitationsCorpus.getAbstract(referencePaperExtendedMetaData.getInvertedAbstract().getInvertedIndex())));
                                                }
                                            }
                                            //}
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

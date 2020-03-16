package edu.upf.taln.corpus;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.util.*;

public class ExtractMAGAbstractsCitationsCorpus {
    public static void main(String args[]) {
        if (args.length > 0) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(new File("mag_papers_1.txt")));
                for(int i=0; i<250000; i++)
                {
                    br.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String line;
            System.out.println("Creating REST Clients ...");
            Header header = new BasicHeader("Ocp-Apim-Subscription-Key", "XXX");
            RestClient madRestClient = Utilities.buildRestClientConnection("api.labs.cognitive.microsoft.com", 443, "https", Optional.empty(), Optional.of(new Header[]{header}));
            RestClient elasticRestClient = Utilities.buildRestClientConnection("XXX", 80, "http", Optional.empty(), Optional.of(new Header[]{header}));
            System.out.println("REST Clients Connected.");
            int counter = 0;
            int it = 0;
            try {
                while ((line = br.readLine()) != null && counter < 20000) {
                    System.out.println("It " + it);
                    String paperID = line.split(",")[0].substring(7).replaceAll("\"", "").trim();
                    MAGMetaData paper = MAGMetaData.getMAGMetaDataFromID(Long.parseLong(paperID), madRestClient);
                    if (paper != null && paper.getExtendedMetaData() != null) {
                        MAGExtendedMetaData extendedMetaData = MAGExtendedMetaData.getMAGMetaDataFromJsonString(paper.getExtendedMetaData());
                        if (extendedMetaData != null && extendedMetaData.getCitationContexts() != null) {
                            Utilities.putElasticSearch(MAGMetaData.getJsonStringFromMAGMetaData(paper), "mag", "paper", paperID, elasticRestClient);
                            Set<String> citationsKeys = extendedMetaData.getCitationContexts().keySet();
                            for (String referencePaperID : citationsKeys) {
                                try {
                                    MAGMetaData referencePaper = MAGMetaData.getMAGMetaDataFromID(Long.valueOf(referencePaperID.replaceAll("\"", "").trim()), madRestClient);
                                    if (referencePaper != null && referencePaper.getExtendedMetaData() != null) {
                                        MAGExtendedMetaData referencePaperExtendedMetaData = MAGExtendedMetaData.getMAGMetaDataFromJsonString(referencePaper.getExtendedMetaData());
                                        if (referencePaperExtendedMetaData != null && referencePaperExtendedMetaData.getInvertedAbstract() != null && referencePaperExtendedMetaData.getInvertedAbstract().getInvertedIndex() != null) {
                                            //LinkedHashMap mapping = referencePaperExtendedMetaData.getInvertedAbstract().getInvertedIndex();
                                            Utilities.putElasticSearch(MAGMetaData.getJsonStringFromMAGMetaData(referencePaper), "mag", "paper", referencePaperID, elasticRestClient);

                                            System.out.println(counter);
                                            counter++;
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    it++;
                }
                System.out.println("Done");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Closing Clients");
            }

            System.out.println("Closing Clients");
            try {
                madRestClient.close();
                elasticRestClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("No Arguments ...");
        }
    }

    public static String getAbstract(LinkedHashMap mapping)
    {
        String abs = "";
        HashMap<Integer, String> map = new HashMap<Integer, String>();

        Set<String> keys = mapping.keySet();
        for (String k : keys) {
            List<Integer> list = (List<Integer>) mapping.get(k);
            for(Integer value: list)
            {
                map.put(value, k);
            }
        }

        for(int i=0; i< map.size(); i++)
        {
            abs += " " + map.getOrDefault(i, "");
        }

        return abs;
    }
}

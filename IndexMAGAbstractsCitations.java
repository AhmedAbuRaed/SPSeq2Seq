package edu.upf.taln.corpus;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

public class IndexMAGAbstractsCitations {
    public static void main(String args[]) {
        if (args.length > 0) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(new File("mag_papers_1.txt")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line;
            System.out.println("Creating REST Clients ...");
            Header header = new BasicHeader("Ocp-Apim-Subscription-Key", "XXX");
            RestClient madRestClient = Utilities.buildRestClientConnection("api.labs.cognitive.microsoft.com", 443, "https", Optional.empty(), Optional.of(new Header[]{header}));
            RestClient elasticRestClient = Utilities.buildRestClientConnection("XXX", 80, "http", Optional.empty(), Optional.of(new Header[]{}));
            System.out.println("REST Clients Connected.");
            int counter = 0;
            try {
                while ((line = br.readLine()) != null && counter < 50000) {
                    String paperID = line.split(",")[0].substring(7).replaceAll("\"", "").trim();
                    MAGMetaData paper = MAGMetaData.getMAGMetaDataFromID(Long.parseLong(paperID), madRestClient);
                    if (paper != null && paper.getExtendedMetaData() != null) {
                        MAGExtendedMetaData extendedMetaData = MAGExtendedMetaData.getMAGMetaDataFromJsonString(paper.getExtendedMetaData());
                        if (extendedMetaData != null && extendedMetaData.getCitationContexts() != null) {
                            Utilities.putElasticSearch(MAGMetaData.getJsonStringFromMAGMetaData(paper), "mag", "paper", paperID, elasticRestClient);
                            Set<String> keys = extendedMetaData.getCitationContexts().keySet();
                            for (String referencePaperID : keys) {
                                try {
                                    MAGMetaData referencePaper = MAGMetaData.getMAGMetaDataFromID(Long.valueOf(referencePaperID.replaceAll("\"", "").trim()), madRestClient);
                                    if (referencePaper != null && referencePaper.getExtendedMetaData() != null) {
                                        MAGExtendedMetaData referencePaperExtendedMetaData = MAGExtendedMetaData.getMAGMetaDataFromJsonString(referencePaper.getExtendedMetaData());
                                        if(referencePaperExtendedMetaData != null && referencePaperExtendedMetaData.getInvertedAbstract() != null) {
                                            Utilities.putElasticSearch(MAGMetaData.getJsonStringFromMAGMetaData(referencePaper), "mag", "paper", referencePaperID, elasticRestClient);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    counter++;
                    System.out.println(counter);
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
}

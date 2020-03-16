package edu.upf.taln.corpus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;
import gate.Annotation;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class Utilities {
    public static String getPDFFileTitle(File PDF) {
        try {
            Document diDocument = Factory.getPDFloader().parsePDF(PDF);
            if (diDocument == null) {
                return null;
            }
            diDocument.preprocess();
            gate.Document gateDocument = gate.Factory.newDocument(diDocument.getXMLString());
            List<Annotation> paperTitles = gateDocument.getAnnotations("Analysis").get("Title").inDocumentOrder();
            if (paperTitles.size() > 0) {
                return gateDocument.getContent().getContent(paperTitles.get(0).getStartNode().getOffset(), paperTitles.get(0).getEndNode().getOffset()).toString();
            }
        } catch (DRIexception drIexception) {
            drIexception.printStackTrace();
        } catch (ResourceInstantiationException e) {
            e.printStackTrace();
        } catch (InvalidOffsetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonNode searchElasticSearch(RestClient restClient, String index, String type, String field, String term){
        try {
            Map<String, String> params = Collections.<String, String>emptyMap();
            String request = "";
            request += "/" + index;
            request += "/" + type;
            request += "/_search";
            HttpEntity entity = new NStringEntity("{\"query\":{\"query_string\":{\"default_field\":\"" + field + "\",\"query\":\"" + term + "\"}}}", ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params,
                    entity);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(EntityUtils.toString(response.getEntity()));
        } catch (JsonProcessingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static JsonNode getElasticSearchFrom(RestClient restClient, String index, String type, Integer size, Integer from){
        try {
            Map<String, String> params = new HashMap<>()/*Collections.<String, String>emptyMap()*/;
            String request = "/elastic";
            request += "/" + index;
            request += "/" + type;
            request += "/_search?size=" + size + "&from=" + from;
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(EntityUtils.toString(response.getEntity()));
        } catch (JsonProcessingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static JsonNode searchMAG(RestClient restClient, String index, String type, String feature, String expr) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("expr", expr);
            params.put("attributes", "Id,Ti,L,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,AA.S,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E");
            params.put("count", "1000");
            String request = "";
            request += "/" + index;
            request += "/" + type;
            request += "/" + feature;

            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(EntityUtils.toString(response.getEntity()));
        } catch (JsonProcessingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static void putElasticSearch(String json, String index, String type, String id, RestClient restClient) throws IOException {
            // Index new element
            Map<String, String> params = Collections.<String, String>emptyMap();
            String request = "/elastic";
            request += "/" + index + "/" + type + "/" + id;
            HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest(
                    "PUT",
                    request,
                    params,
                    entity);
    }

    public static boolean idExistsElasticSearch(String id, String index, String type, RestClient restClient) {
        try {
            Map<String, String> params = Collections.<String, String>emptyMap();
            String request = "/elastic";
            request += "/" + index;
            request += "/" + type;
            request += "/" + id;
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (ResponseException response) {
            if (response.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            } else {
                response.getStackTrace();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
        return false;
    }

    public static RestClient buildRestClientConnection(String host, int port, String protocol, Optional<String> prefix, Optional<Header[]> headers) {
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, port, protocol)).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(5000)
                        .setSocketTimeout(100000000);
            }
        }).setMaxRetryTimeoutMillis(60000000);

        if (prefix.isPresent()) {
            restClientBuilder = restClientBuilder.setPathPrefix(prefix.get());
        }
        if (headers.isPresent()) {
            restClientBuilder = restClientBuilder.setDefaultHeaders(headers.get());
        }
        return restClientBuilder.build();

    }

    public static void orderByBooleanThenInteger(List<Paper> citingPapers) {
        Collections.sort(citingPapers, new Comparator() {

            public int compare(Object o1, Object o2) {

                boolean x1 = ((Paper) o1).isInfluencedByReference();
                boolean x2 = ((Paper) o2).isInfluencedByReference();
                int sComp = Boolean.compare(x2, x1);

                if (sComp != 0) {
                    return sComp;
                } else {
                    Integer y1 = ((Paper) o1).getCitationsCount();
                    Integer y2 = ((Paper) o2).getCitationsCount();
                    return y2.compareTo(y1);
                }
            }
        });
    }

    /**
     * Downloads from a (http/https) URL and saves to a file.
     * Does not consider a connection error an Exception. Instead it returns:
     * <p>
     * 0=ok
     * 1=connection interrupted, timeout (but something was read)
     * 2=not found (FileNotFoundException) (404)
     * 3=server error (500...)
     * 4=could not connect: connection timeout (no internet?) java.net.SocketTimeoutException
     * 5=could not connect: (server down?) java.net.ConnectException
     * 6=could not resolve host (bad host, or no internet - no dns)
     * 7=small content probably html page instead of PDF
     * 8=content length less than threshold
     *
     * @param file               File to write. Parent directory will be created if necessary
     * @param url                http/https url to connect
     * @param secsConnectTimeout Seconds to wait for connection establishment
     * @param secsReadTimeout    Read timeout in seconds - trasmission will abort if it freezes more than this
     * @return See above
     * @throws IOException Only if URL is malformed or if could not create the file
     */
    public static int saveUrl(final Path file, final URL url,
                              int secsConnectTimeout, int secsReadTimeout) throws IOException {
        Files.createDirectories(file.getParent()); // make sure parent dir exists , this can throw exception
        URLConnection conn = url.openConnection(); // can throw exception if bad url
        if (secsConnectTimeout > 0) conn.setConnectTimeout(secsConnectTimeout * 1000);
        if (secsReadTimeout > 0) conn.setReadTimeout(secsReadTimeout * 1000);
        int ret = 0;
        boolean somethingRead = false;

        try (InputStream is = conn.getInputStream()) {
            if (conn.getContentLengthLong() < 8192) {
                return 8;
            }
            try (BufferedInputStream in = new BufferedInputStream(is); OutputStream fout = Files
                    .newOutputStream(file)) {
                final byte data[] = new byte[8192];// this was 8192
                int count;
                while ((count = in.read(data)) > 0) {
                    somethingRead = true;
                    fout.write(data, 0, count);
                }
            }
        } catch (SSLHandshakeException ssl) {
            return -1;
        } catch (java.io.IOException e) {
            int httpcode = 999;
            try {
                httpcode = ((HttpURLConnection) conn).getResponseCode();
            } catch (Exception ee) {
                return -1;
            }
            if (somethingRead && e instanceof java.net.SocketTimeoutException) ret = 1;
            else if (e instanceof FileNotFoundException && httpcode >= 400 && httpcode < 500) ret = 2;
            else if (httpcode >= 400 && httpcode < 600) ret = 3;
            else if (e instanceof java.net.SocketTimeoutException) ret = 4;
            else if (e instanceof java.net.ConnectException) ret = 5;
            else if (e instanceof java.net.UnknownHostException) ret = 6;
            else /*throw e*/ return -1;
        } catch (Exception ex) {
            return -1;
        }
        return ret;
    }

    public static float simpleSimilarity(String u, String v) {
        String[] a = u.split(" ");
        String[] b = v.split(" ");

        long correct = 0;
        int minLen = Math.min(a.length, b.length);

        for (int i = 0; i < minLen; i++) {
            String aa = a[i];
            String bb = b[i];
            int minWordLength = Math.min(aa.length(), bb.length());

            for (int j = 0; j < minWordLength; j++) {
                if (aa.charAt(j) == bb.charAt(j)) {
                    correct++;
                }
            }
        }

        return (float) (((double) correct) / Math.max(u.length(), v.length()));
    }

    public static void exportGATEDocument(gate.Document document, File outputFile)
    {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.println(document.toXml());
        pw.flush();
        pw.close();
        gate.Factory.deleteResource(document);
    }
}

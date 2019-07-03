package edu.upf.taln.corpus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;

public class MAGExtendedMetaData {
    @JsonProperty("DN")
    private String paperDisplayName;
    @JsonProperty("S")
    private S[] sources;
    @JsonProperty("VFN")
    private String venueFullName;
    @JsonProperty("VSN")
    private String venueShortName;
    @JsonProperty("V")
    private String journalVolume;
    @JsonProperty("I")
    private String journalIssue;
    @JsonProperty("FP")
    private String firstPage;
    @JsonProperty("LP")
    private String lastPage;
    @JsonProperty("DOI")
    private String doi;
    @JsonProperty("PR")
    private String[] referencePapers;
    @JsonProperty("CC")
    private LinkedHashMap citationContexts;
    @JsonProperty("IA")
    private IA invertedAbstract;
    @JsonProperty("ANF")
    private ANF[] authors;
    @JsonProperty("BV")
    private String BV;
    @JsonProperty("BT")
    private String BT;
    @JsonProperty("PB")
    private String PB;

    public String getPaperDisplayName() {
        return paperDisplayName;
    }

    public void setPaperDisplayName(String paperDisplayName) {
        this.paperDisplayName = paperDisplayName;
    }

    public S[] getSources() {
        return sources;
    }

    public void setSources(S[] sources) {
        this.sources = sources;
    }

    public String getVenueFullName() {
        return venueFullName;
    }

    public void setVenueFullName(String venueFullName) {
        this.venueFullName = venueFullName;
    }

    public String getVenueShortName() {
        return venueShortName;
    }

    public void setVenueShortName(String venueShortName) {
        this.venueShortName = venueShortName;
    }

    public String getJournalVolume() {
        return journalVolume;
    }

    public void setJournalVolume(String journalVolume) {
        this.journalVolume = journalVolume;
    }

    public String getJournalIssue() {
        return journalIssue;
    }

    public void setJournalIssue(String journalIssue) {
        this.journalIssue = journalIssue;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }

    public String getLastPage() {
        return lastPage;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String[] getReferencePapers() {
        return referencePapers;
    }

    public void setReferencePapers(String[] referencePapers) {
        this.referencePapers = referencePapers;
    }

    public LinkedHashMap getCitationContexts() {
        return citationContexts;
    }

    public void setCitationContexts(LinkedHashMap citationContexts) {
        this.citationContexts = citationContexts;
    }

    public IA getInvertedAbstract() {
        return invertedAbstract;
    }

    public void setInvertedAbstract(IA invertedAbstract) {
        this.invertedAbstract = invertedAbstract;
    }

    public ANF[] getAuthors() {
        return authors;
    }

    public void setAuthors(ANF[] authors) {
        this.authors = authors;
    }

    public String getBV() {
        return BV;
    }

    public void setBV(String BV) {
        this.BV = BV;
    }

    public String getBT() {
        return BT;
    }

    public void setBT(String BT) {
        this.BT = BT;
    }

    public String getPB() {
        return PB;
    }

    public void setPB(String PB) {
        this.PB = PB;
    }

    public static class S {
        @JsonProperty("Ty")
        private Integer sourceType;
        @JsonProperty("U")
        private String sourceURL;

        public Integer getSourceType() {
            return sourceType;
        }

        public void setSourceType(Integer sourceType) {
            this.sourceType = sourceType;
        }

        public String getSourceURL() {
            return sourceURL;
        }

        public void setSourceURL(String sourceURL) {
            this.sourceURL = sourceURL;
        }
    }

    public static class IA {
        @JsonProperty("IndexLength")
        private Integer indexLength;
        @JsonProperty("InvertedIndex")
        private LinkedHashMap invertedIndex;

        public Integer getIndexLength() {
            return indexLength;
        }

        public void setIndexLength(Integer indexLength) {
            this.indexLength = indexLength;
        }

        public LinkedHashMap getInvertedIndex() {
            return invertedIndex;
        }

        public void setInvertedIndex(LinkedHashMap invertedIndex) {
            this.invertedIndex = invertedIndex;
        }
    }

    public static class ANF {
        @JsonProperty("FN")
        private String firstName;
        @JsonProperty("LN")
        private String lastName;
        @JsonProperty("S")
        private Integer authorOrder;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Integer getAuthorOrder() {
            return authorOrder;
        }

        public void setAuthorOrder(Integer authorOrder) {
            this.authorOrder = authorOrder;
        }
    }

    public static MAGExtendedMetaData getMAGMetaDataFromJsonString(String jsonElementString) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            MAGExtendedMetaData magExtendedMetaData = mapper.readValue(jsonElementString, MAGExtendedMetaData.class);
            return mapper.readValue(jsonElementString, MAGExtendedMetaData.class);
        } catch (Exception e) {
            return null;
        }
    }
}

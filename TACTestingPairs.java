package edu.upf.taln.corpus;

import gate.*;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TACTestingPairs {
    public static String testingClusters = "C08-1013_C08-1031_C08-1064_C08-1066_E09-1018_N09-1008_N09-1019_N09-1027_N09-1034_N09-1042_P07-1034_P08-1001_P08-1006_P08-1027_P08-1032_P08-1052_p27-kalashnikov_p79-raghavan_p203-wu_p343-ko";

    public static void main(String args[]) {
        if (args.length > 0) {
            try {
                Gate.init();
            } catch (GateException e) {
                e.printStackTrace();
            }

            String workingDirectory = args[1];

            File testSource = new File("D:/Research/UPF/Projects/ExtractCorpus/TACtest.txt.src");
            File testTarget = new File("D:/Research/UPF/Projects/ExtractCorpus/TACtest.txt.tgt.tagged");
            File testIDs = new File("D:/Research/UPF/Projects/ExtractCorpus/TACtestIDs.txt");

            FileWriter testSourceWriter = null;
            FileWriter testTargetWriter = null;
            FileWriter testIDsWriter = null;

            try {
                testSourceWriter = new FileWriter(testSource);
                testTargetWriter = new FileWriter(testTarget);
                testIDsWriter = new FileWriter(testIDs);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String targetPaper : testingClusters.split("_")) {
                System.out.println(targetPaper);

                File fileT = new File(workingDirectory + "/dataset/" + targetPaper + File.separator + targetPaper + "_PreProcessed_gate.xml");
                try {
                    Document targetDocument = Factory.newDocument(new URL("file:///" + fileT.getPath()), "UTF-8");

                    AnnotationSet citances = targetDocument.getAnnotations("Citances");
                    Set<String> coveredRPs = new HashSet<>();
                    for (Annotation citance : citances) {
                        boolean citanceFlag = false;
                        boolean referenceFlag = false;
                        int addedCount = 0;
                        StringBuilder citanceText = new StringBuilder();
                        StringBuilder referenceText = new StringBuilder();
                        String id = citance.getFeatures().get("citing_paper").toString() + "_" + citance.getFeatures().get("reference_paper").toString() + "_" + citance.getFeatures().get("sids").toString();
                        String referencePaperName = citance.getFeatures().get("reference_paper").toString();
                        if (!coveredRPs.contains(referencePaperName)) {
                            coveredRPs.add(referencePaperName);
                            FeatureMap fm = Factory.newFeatureMap();
                            fm.put("reference_paper", referencePaperName);

                            List<Annotation> referenceExplicitCitances = citances.get("Explicit", fm).inDocumentOrder();
                            for (int i = 0; i < referenceExplicitCitances.size(); i++) {
                                List<Annotation> sents = targetDocument.getAnnotations("Analysis").get("Sentence").get(referenceExplicitCitances.get(i).getStartNode().getOffset(), referenceExplicitCitances.get(i).getEndNode().getOffset()).inDocumentOrder();
                                for (int s = 0; s < sents.size(); s++) {
                                    citanceText.append("<t> " + cleanString(targetDocument.getContent().getContent(sents.get(s).getStartNode().getOffset(), sents.get(s).getEndNode().getOffset()).toString().trim()) + " </t> ");
                                    citanceFlag = true;
                                }
                            }
                            if(!citanceFlag) {
                                List<Annotation> referenceImplicitCitances = citances.get("Implicit", fm).inDocumentOrder();
                                for (int i = 0; i < referenceImplicitCitances.size(); i++) {
                                    List<Annotation> sents = targetDocument.getAnnotations("Analysis").get("Sentence").get(referenceImplicitCitances.get(i).getStartNode().getOffset(), referenceImplicitCitances.get(i).getEndNode().getOffset()).inDocumentOrder();
                                    for (int s = 0; s < sents.size(); s++) {
                                        citanceText.append("<t> " + cleanString(targetDocument.getContent().getContent(sents.get(s).getStartNode().getOffset(), sents.get(s).getEndNode().getOffset()).toString().trim()) + " </t> ");
                                        citanceFlag = true;
                                        break;
                                    }
                                }
                            }

                            File file = new File(workingDirectory + "/dataset/" + targetPaper + "/ref/preprocessed/" + citance.getFeatures().get("reference_paper") + "/" + citance.getFeatures().get("reference_paper") + "_PreProcessed_gate.xml");

                            Document referenceDocument = Factory.newDocument(new URL("file:///" + file.getPath()), "UTF-8");

                            AnnotationSet titles = referenceDocument.getAnnotations("Original markups").get("title");
                            AnnotationSet titleGroups = referenceDocument.getAnnotations("Original markups").get("title-group");
                            AnnotationSet refAbstractText = referenceDocument.getAnnotations("Original markups").get("abstract_text");
                            AnnotationSet refSectionTitle = referenceDocument.getAnnotations("Original markups").get("section_title");
                            AnnotationSet refSectionText = referenceDocument.getAnnotations("Original markups").get("section_text");
                            AnnotationSet refAbstract = referenceDocument.getAnnotations("Original markups").get("abstract");
                            AnnotationSet refBody = referenceDocument.getAnnotations("Original markups").get("body");
                            AnnotationSet refSection = referenceDocument.getAnnotations("Original markups").get("section");

                            if (titles.size() > 0) {
                                Annotation title = titles.iterator().next();
                                referenceText.append(cleanString(referenceDocument.getContent().getContent(title.getStartNode().getOffset(), title.getEndNode().getOffset()).toString().trim() + ". "));
                            } else if (titleGroups.size() > 0) {
                                Annotation titleGroup = titleGroups.iterator().next();
                                referenceText.append(cleanString(referenceDocument.getContent().getContent(titleGroup.getStartNode().getOffset(), titleGroup.getEndNode().getOffset()).toString().trim() + ". "));
                            }

                            List<Annotation> sentences = referenceDocument.getAnnotations("Analysis").get("Sentence_LOA").inDocumentOrder();
                            for (int i = 0; i < sentences.size(); i++) {

                                Annotation sentence = sentences.get(i);
                                if (refSectionText.size() > 0) {
                                    AnnotationSet sentenceBelongsToRefAbstractText = refAbstractText.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
                                    String refSenetenceSectionTitleString = "";
                                    AnnotationSet refSentenceSectionText = refSectionText.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
                                    if (refSentenceSectionText.size() > 0) {
                                        AnnotationSet refSenetenceSectionTitle = refSectionTitle.get(refSentenceSectionText.iterator().next().getStartNode().getOffset() - 2L, sentence.getStartNode().getOffset());
                                        if (refSenetenceSectionTitle.size() > 0) {
                                            Annotation refSenetenceSectionTitleAnnotation = refSenetenceSectionTitle.iterator().next();
                                            refSenetenceSectionTitleString = referenceDocument.getContent().getContent(refSenetenceSectionTitleAnnotation.getStartNode().getOffset(),
                                                    refSenetenceSectionTitleAnnotation.getEndNode().getOffset()).toString().toLowerCase();
                                        }
                                    }
                                    if (sentenceBelongsToRefAbstractText.size() > 0 || refSenetenceSectionTitleString.contains("introduction") || refSenetenceSectionTitleString.contains("conclusion")) {
                                        referenceText.append(cleanString(referenceDocument.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()).toString().trim() + " "));
                                        referenceFlag = true;
                                    }
                                } else {
                                    AnnotationSet sentenceBelongsToRefAbstractText = refAbstract.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
                                    if (sentenceBelongsToRefAbstractText.size() > 0) {
                                        referenceText.append(cleanString(referenceDocument.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()).toString().trim() + " "));
                                        referenceFlag = true;
                                    }

                                    AnnotationSet refBodyText = refBody.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
                                    if (refBodyText.size() > 0) {
                                        if (refSection.size() > 0) {
                                            AnnotationSet sentenceBelongsToRefSectionText = refSection.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
                                            if (sentenceBelongsToRefSectionText.size() > 0) {
                                                Annotation sentenceBelongsToRefSectionTextAnnotation = sentenceBelongsToRefSectionText.iterator().next();
                                                if (sentenceBelongsToRefSectionTextAnnotation.getFeatures().get("class").toString().toLowerCase().endsWith("introduction") ||
                                                        sentenceBelongsToRefSectionTextAnnotation.getFeatures().get("class").toString().toLowerCase().endsWith("conclusion")) {
                                                    referenceText.append(cleanString(referenceDocument.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()).toString().trim() + " "));
                                                    referenceFlag = true;
                                                }
                                            }
                                        } else {
                                            if (!sentence.getFeatures().get("rhetorical_class").toString().equals("DRI_Background") &&
                                                    !sentence.getFeatures().get("rhetorical_class").toString().equals("DRI_FutureWork") &&
                                                    !sentence.getFeatures().get("rhetorical_class").toString().equals("DRI_Unspecified") &&
                                                    sentence.getFeatures().get("keep") != null && !sentence.getFeatures().get("keep").toString().equals("0")) {
                                                if (addedCount >= 10) {
                                                    break;
                                                }
                                                referenceText.append(cleanString(referenceDocument.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()).toString().trim() + " "));
                                                referenceFlag = true;
                                                addedCount++;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!referenceFlag) {
                                for (int i = 0; i < 15; i++) {
                                    Annotation sentence = sentences.get(i);
                                    referenceText.append(cleanString(referenceDocument.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()).toString().trim() + " "));
                                    referenceFlag = true;
                                }
                                if (!referenceFlag) {
                                    System.out.println("SOMETHING IS WRONGGGGGG");
                                }
                            }
                            if (!citanceFlag || !referenceFlag) {
                                System.out.println("SOMETHING IS WROOOOOOONG");
                            } else {
                                testSourceWriter.write(referenceText.toString() + "\r\n");
                                testTargetWriter.write(citanceText.toString() + "\r\n");
                                testIDsWriter.write(id + "\r\n");
                            }
                            Factory.deleteResource(referenceDocument);
                        }
                    }
                    Factory.deleteResource(targetDocument);
                } catch (ResourceInstantiationException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidOffsetException e) {
                    e.printStackTrace();
                }
            }
            try {
                testSourceWriter.close();
                testTargetWriter.close();
                testIDsWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No Arguments ...");
        }
    }

    private static String cleanString(String s) {
        return s.replaceAll("\\R+", " ")
                .replaceAll("[^a-zA-Z0-9,.!?\\[\\]\\(\\)\\s+]", " ");
    }
}

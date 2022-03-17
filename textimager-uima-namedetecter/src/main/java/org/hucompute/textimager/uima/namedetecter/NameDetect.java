package org.hucompute.textimager.uima.namedetecter;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.hucompute.textimager.uima.base.DockerRestAnnotator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.texttechnologylab.annotation.AnnotationComment;

import java.lang.annotation.Annotation;
import java.util.Iterator;

public class NameDetect extends DockerRestAnnotator {

    @Override
    protected String getDefaultDockerImage() {
        return "textimager-uima-service-name_detect";
    }

    @Override
    protected String getDefaultDockerImageTag() {
        return "0.1";
    }

    @Override
    protected int getDefaultDockerPort() {
        return 8000;
    }

    @Override
    protected String getRestRoute() {
        return "/tagnames";
    }

    @Override
    protected String getAnnotatorVersion() {
        return "0.0.1";
    }

    @Override
    protected JSONObject buildJSON(JCas aJCas) throws AnalysisEngineProcessException {
        JSONObject payload = new JSONObject();
        JSONArray tokens = new JSONArray();
        for (Token token : JCasUtil.select(aJCas, Token.class)) {
                JSONObject tokenObj = new JSONObject();
                tokenObj.put("begin", token.getBegin());
                tokenObj.put("end", token.getEnd());
                tokenObj.put("text", token.getCoveredText());
                tokens.put(tokenObj);
        }
        payload.put("tokens", tokens);

        return payload;
    }

    @Override
    protected void updateCAS(JCas aJCas, JSONObject jsonResult) throws AnalysisEngineProcessException {
        if (jsonResult.has("tokens")) {
            JSONObject tokens = jsonResult.getJSONObject("tokens");
            for (Iterator<String> it = tokens.keys(); it.hasNext(); ) {
                String word = it.next();
                JSONObject token = (JSONObject) tokens.getJSONObject(word);
                int begin = token.getInt("begin");
                int end = token.getInt("end");
                boolean typo = token.getBoolean("typonym");
                boolean proper = token.getBoolean("proper");
                if (typo){
                    Location loc = new Location(aJCas, begin, end);
                    loc.setValue("LOC");
                    loc.addToIndexes();
                    addAnnotatorComment(aJCas, loc);
                    if (proper){
                        AnnotationComment modelAnno = new AnnotationComment(aJCas);
                        modelAnno.setReference(loc);
                        modelAnno.setKey("propername");
                        modelAnno.setValue("1");
                        addAnnotatorComment(aJCas, modelAnno);
                    }
                }
                else if(proper){
                    NamedEntity newAnno = new NamedEntity(aJCas, begin, end);
                    newAnno.addToIndexes();
                    addAnnotatorComment(aJCas, newAnno);
                    AnnotationComment modelAnno = new AnnotationComment(aJCas);
                    modelAnno.setReference(newAnno);
                    modelAnno.setKey("propername");
                    modelAnno.setValue("1");
                    addAnnotatorComment(aJCas, modelAnno);
                }
            }
        }
    }

}
package org.hucompute.textimager.uima.julie;

import de.julielab.jcore.types.POSTag;
import de.julielab.jcore.types.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertArrayEquals;
/**
 * BieLemmatizer
 *
 * @date 13.08.2021
 *
 * @author Grzegorz Siwiecki, Chieh Kang
 * @version 1.1
 *
 * This class provide BioLemmatizer test case */
public class BioLemmatizerTest {
    public void init_jcas(JCas jcas, String[] POSTAG) {
        //initialize index
        int index_start = 0;
        int index_end = 0;

        //loop for all words
        for (int i=0; i< POSTAG.length; i++) {
            index_end = index_start + POSTAG[i].length();
            POSTag pos = new POSTag(jcas);

            pos.setBegin(index_start);
            pos.setEnd(index_end);
            pos.setValue(POSTAG[i]);
            pos.addToIndexes();

            index_start = index_end + 1;
        }
    }
    /**
     * Test for simple english text.
     * @throws UIMAException
     */
    @Test
    public void testProcess() throws IOException, UIMAException {
        String Text = "Three horses were going contemplatively around bushy bushes .";
        JCas jCas = JCasFactory.createText(Text);
        // get postag
        //AnalysisEngineDescription engine_postag = createEngineDescription(OpennlpPostag.class);
        AnalysisEngineDescription engine_postag = createEngineDescription(OpennlpPostag.class);
        SimplePipeline.runPipeline(jCas, engine_postag);

        String[] casPostag = (String[]) JCasUtil.select(jCas, Token.class).stream().map(a -> a.getPosTag(0).getValue()).toArray(String[]::new);
        jCas.reset();
        jCas.setDocumentText(Text);

        init_jcas(jCas, casPostag);
        //AnalysisEngineDescription engine = createEngineDescription(BioLemmatizer.class);
        AnalysisEngineDescription engine = createEngineDescription(BioLemmatizer.class);

        SimplePipeline.runPipeline(jCas, engine);

        String[] casLemma = (String[]) JCasUtil.select(jCas, Token.class).stream().map(b -> b.getLemma().getValue()).toArray(String[]::new);
        String[] testLemma = new String[] {"three", "horse", "be", "go", "contemplative",
                                            "around", "Bushy", "bush", "."};

        assertArrayEquals(testLemma, casLemma);
    }

}
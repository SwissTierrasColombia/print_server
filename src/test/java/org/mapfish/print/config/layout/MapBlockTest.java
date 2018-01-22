package org.mapfish.print.config.layout;

import static org.mockito.Matchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.PDFCustomBlocks;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.config.Config;
import org.mapfish.print.utils.PJsonObject;
import org.pvalsecc.misc.FileUtilities;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Test for Map Block class
 */
public class MapBlockTest {
    private static String tempDir = System.getProperty("java.io.tmpdir");
    private static String fileSeparator = System.getProperty("file.separator");

    @Test
    public void testMultipleMaps() throws IOException, DocumentException, JSONException {
        PJsonObject globalParams = new PJsonObject(new JSONObject("{\n" +
                "    \"layout\": \"A4 portrait\",\n" +
                "    \"title\": \"A simple example\",\n" +
                "    \"srs\": \"EPSG:4326\",\n" +
                "    \"dpi\": 254,\n" +
                "    \"units\": \"degrees\",\n" +
                "    \"scale\": 20000,\n" +
                "    \"outputFormat\": \"png\"\n" +
                "}"), "global");
        
		PJsonObject params = MapPrinter
				.parseSpec(FileUtilities.readWholeTextFile(new File(MapBlockTest.class.getClassLoader()
                .getResource("config/multiple-maps.json").getFile())));
        Block.PdfElement target = new Block.PdfElement() {
            @Override
            public void add(Element element) throws DocumentException {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        Config config = new Config();
        
        // set some paraameters needed to render the map block
        TreeSet<Integer> tree = new TreeSet<Integer>();
        tree.add(254);
        config.setDpis(tree);
        tree = new TreeSet<Integer>();
        tree.add(20000);
        config.setScales(tree);

        RenderingContext context = mock(RenderingContext.class);

        PdfContentByte dc = mock(PdfContentByte.class);
        PdfTemplate template = mock(PdfTemplate.class);
        when(dc.createTemplate(anyFloat(), anyFloat())).thenReturn(template);

        when(context.getGlobalParams()).thenReturn(globalParams);
        when(context.getConfig()).thenReturn(config);
        when(context.getPdfLock()).thenReturn(new Object());
        when(context.getDirectContent()).thenReturn(dc);
        
        /*
         * Generate a temporal document to print.
         * This code is copied from LegendsBlock internal render 
         */
    	String tempFilename = null;
        Document tempDocument = new Document();
        try {
            tempFilename = tempDir.indexOf('/') != -1 ? "" : "\\";
            long time = (new Date()).getTime();
            tempFilename = tempDir + fileSeparator +
                    "test-mapblock"+ time +".pdf";
            // Unfortunately have to open an actual file on disk
            // for the calculations to work properly
            PdfWriter writer = PdfWriter.getInstance(tempDocument,
                    new FileOutputStream(tempFilename));
            tempDocument.open();
            when(context.getCustomBlocks()).thenReturn(new PDFCustomBlocks(writer, context));
        } catch (FileNotFoundException e) {
            throw new DocumentException(e);
        } catch (DocumentException e) {
            // don't forget to delete the useless file
            new File(tempFilename).delete();
            throw new DocumentException(e);
        }
        
        // it render the mapblock with the name 'main'
        MapBlock mapBlock = new MapBlock();
        mapBlock.setWidth("100");
        mapBlock.setHeight("100");
        mapBlock.setName("main");
        mapBlock.render(params, target, context);
        
        // and the another one
        MapBlock otherBlock = new MapBlock();
        otherBlock.setWidth("100");
        otherBlock.setHeight("100");
        otherBlock.setName("other");
        otherBlock.render(params, target, context);
    }     
}

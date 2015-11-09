import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Tim on 11/8/15.
 */
public class SketchFinderTest extends TestCase {

    public void testSketchesToJSON() throws Exception {

        String expectedOutput0 = "No sketches detected.";

        SketchFinder sf_test0 = new SketchFinder();

        ArrayList<File> files0 = new ArrayList<File>();
        ArrayList<String> sketches0 = new ArrayList<String>();
        ArrayList<Integer> levels0 = new ArrayList<Integer>();


        String output0 = sf_test0.sketchesToJSON(files0, sketches0, levels0);

        assertEquals(expectedOutput0, output0);

        //////////////////

        String expectedOutput1 = "[\n" +
                "    {\"Test1\" : [\n" +
                "        {\"Test2\" : [\n" +
                "            \"Test2.ino\"\n" +
                "        ]}\n" +
                "    ]}\n" +
                "]";

        SketchFinder sf_test1 = new SketchFinder();

        ArrayList<File> files1 = new ArrayList<File>();
        ArrayList<String> sketches1 = new ArrayList<String>();
        ArrayList<Integer> levels1 = new ArrayList<Integer>();

        sketches1.add("Test1");
        sketches1.add("Test2");
        levels1.add(0);
        levels1.add(1);

        String output1 = sf_test1.sketchesToJSON(files1, sketches1, levels1);

        assertEquals(expectedOutput1, output1);

        //////////////////

        String expectedOutput2 = "[\n" +
                "    {\"Test\" : [\n" +
                "        {\"Alex_Mouse\" : [\n" +
                "            \"Alex_Mouse.ino\"\n" +
                "        ]},\n" +
                "        {\"Test2\" : [\n" +
                "            \"Test2.ino\"\n" +
                "        ]},\n" +
                "        {\"Test3\" : [\n" +
                "            {\"yes\" : [\n" +
                "                \"yes.ino\"\n" +
                "            ]}\n" +
                "        ]}\n" +
                "    ]}\n" +
                "]";

        SketchFinder sf_test2 = new SketchFinder();

        ArrayList<File> files2 = new ArrayList<File>();
        ArrayList<String> sketches2 = new ArrayList<String>();
        ArrayList<Integer> levels2 = new ArrayList<Integer>();

        sketches2.add("Test");
        sketches2.add("Alex_Mouse");
        sketches2.add("Test2");
        sketches2.add("Test3");
        sketches2.add("yes");
        levels2.add(0);
        levels2.add(1);
        levels2.add(1);
        levels2.add(1);
        levels2.add(2);

        String output2 = sf_test2.sketchesToJSON(files2, sketches2, levels2);

        assertEquals(expectedOutput2, output2);


    }

    public void testSketchesToJSON1() throws Exception {

    }

    public void testSketchesToJSONAll() throws Exception {

    }

    public void testSketchesToJSONAll1() throws Exception {

    }
}
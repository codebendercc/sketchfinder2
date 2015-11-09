import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by Tim on 11/8/15.
 */
public class SketchFinderTest extends TestCase {

    private final ByteArrayOutputStream redirectedOutputStream = new ByteArrayOutputStream();

    private final PrintStream originalOutputStream = System.out;

    public void redirectOutput() {
        System.setOut(new PrintStream(redirectedOutputStream));
    }

    public void releaseOutput() {
        System.setOut(originalOutputStream);
    }

    public void testSketchesToJSON() throws Exception {

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

        files1.add(new File("Test2.ino"));
        sketches1.add("Test1");
        sketches1.add("Test2");
        levels1.add(0);
        levels1.add(1);

        String output = sf_test1.sketchesToJSON(files1, sketches1, levels1);

        assertEquals(expectedOutput1, output);


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

//        files2.add(new File("Alex_Mouse.ino"));
//        files2.add(new File("Test2.ino"));
//        files2.add(new File("nope.ino"));
//        files2.add(new File("yes.ino"));
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
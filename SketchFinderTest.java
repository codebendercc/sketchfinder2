import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Tim on 11/8/15.
 */
public class SketchFinderTest extends TestCase {

    public void testSketchesToJSON() throws Exception {

        ///////////////////
        /// Test Case 1 ///
        ///////////////////

        String expectedOutput0 = "No sketches detected.";

        SketchFinder sf_test0 = new SketchFinder();

        ArrayList<File> files0 = new ArrayList<File>();
        ArrayList<String> sketches0 = new ArrayList<String>();
        ArrayList<Integer> levels0 = new ArrayList<Integer>();


        String output0 = sf_test0.sketchesToJSON(files0, sketches0, levels0);

        assertEquals(expectedOutput0, output0);

        ///////////////////
        /// Test Case 2 ///
        ///////////////////

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

        ///////////////////
        /// Test Case 3 ///
        ///////////////////

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

        ///////////////////
        /// Test Case 4 ///
        ///////////////////

        String expectedOutput3 = "[\n" +
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
                "    ]},\n" +
                "    {\"Test3\" : [\n" +
                "        {\"yes\" : [\n" +
                "            \"yes.ino\"\n" +
                "        ]}\n" +
                "    ]},\n" +
                "    {\"Test4\" : [\n" +
                "        {\"yes\" : [\n" +
                "            \"yes.ino\"\n" +
                "        ]},\n" +
                "        {\"yes2\" : [\n" +
                "            {\"yes\" : [\n" +
                "                \"yes.ino\"\n" +
                "            ]},\n" +
                "            {\"yes3\" : [\n" +
                "                {\"yes4\" : [\n" +
                "                    \"yes4.ino\"\n" +
                "                ]}\n" +
                "            ]}\n" +
                "        ]}\n" +
                "    ]},\n" +
                "    {\"yes\" : [\n" +
                "        \"yes.ino\"\n" +
                "    ]}\n" +
                "]";

        SketchFinder sf_test3 = new SketchFinder();

        ArrayList<File> files3 = new ArrayList<File>();
        ArrayList<String> sketches3 = new ArrayList<String>();
        ArrayList<Integer> levels3 = new ArrayList<Integer>();

        sketches3.add("Test");
        sketches3.add("Alex_Mouse");
        sketches3.add("Test2");
        sketches3.add("Test3");
        sketches3.add("yes");
        sketches3.add("Test3");
        sketches3.add("yes");
        sketches3.add("Test4");
        sketches3.add("yes");
        sketches3.add("yes2");
        sketches3.add("yes");
        sketches3.add("yes3");
        sketches3.add("yes4");
        sketches3.add("yes");
        levels3.add(0);
        levels3.add(1);
        levels3.add(1);
        levels3.add(1);
        levels3.add(2);
        levels3.add(0);
        levels3.add(1);
        levels3.add(0);
        levels3.add(1);
        levels3.add(1);
        levels3.add(2);
        levels3.add(2);
        levels3.add(3);
        levels3.add(0);

        String output3 = sf_test3.sketchesToJSON(files3, sketches3, levels3);

        assertEquals(expectedOutput3, output3);

        ///////////////////
        /// Test Case 5 ///
        ///////////////////

        String expectedOutput4 = "[\n" +
                "    {\"Test\" : [\n" +
                "        {\"Test2\" : [\n" +
                "            {\"Test3\" : [\n" +
                "                {\"Test4\" : [\n" +
                "                    {\"Test5\" : [\n" +
                "                        {\"Test6\" : [\n" +
                "                            \"Test6.ino\"\n" +
                "                        ]}\n" +
                "                    ]}\n" +
                "                ]}\n" +
                "            ]}\n" +
                "        ]}\n" +
                "    ]}\n" +
                "]";

        SketchFinder sf_test4 = new SketchFinder();

        ArrayList<File> files4 = new ArrayList<File>();
        ArrayList<String> sketches4 = new ArrayList<String>();
        ArrayList<Integer> levels4 = new ArrayList<Integer>();

        sketches4.add("Test");
        sketches4.add("Test2");
        sketches4.add("Test3");
        sketches4.add("Test4");
        sketches4.add("Test5");
        sketches4.add("Test6");
        levels4.add(0);
        levels4.add(1);
        levels4.add(2);
        levels4.add(3);
        levels4.add(4);
        levels4.add(5);

        String output4 = sf_test3.sketchesToJSON(files4, sketches4, levels4);

        assertEquals(expectedOutput4, output4);

    }
}


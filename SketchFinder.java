import java.io.File;
import java.util.Comparator;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Created by Law on 10/10/15.
 */
public class SketchFinder {

    private static boolean help = false;
    private static boolean showIno = true;

    public static void main(String[] args) {

        parseArgs(args);

        if (help) {
            System.out
                    .print("usage: sketchFinder [-h --h --help -help] filepath [options] \n"
                            + "options include: \n"
                            + "   -a:    to show all files \n"); // TODO: Get help
            // message.
            return;
        }

        File f = new File(args[0]);
        ArrayList<String> sketches = new ArrayList<String>();
        ArrayList<Integer> levels = new ArrayList<Integer>();

        if (showIno) {
            SketchFinder.getSketches(f, sketches, levels);
        } else {
            SketchFinder.getAllFiles(f, sketches, levels);
        }

        SketchFinder.sketchesToJSON(sketches, levels, showIno);
    }

    public static void parseArgs(String[] args) {
        if (args == null || args.length == 0 || args[0].equals("--help")
                || args[0].equals("-help") || args[0].equals("-h")
                || args[0].equals("--h")) {
            // If a help argument or no argument is specified, calls for the
            // help menu.
            help = true;
        } else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-a")) {
                    showIno = false;
                }
            }
        }
    }

    public static void getSketches(File folder, ArrayList<String> sketches,
                                      ArrayList<Integer> levels) {
        getSketches(folder, sketches, levels, 0);
    }

    public static boolean getSketches(File folder, ArrayList<String> sketches,
                                      ArrayList<Integer> levels, int level) {
        if (folder == null)
            return false;

        if (!folder.isDirectory()) return false;

        File[] files = folder.listFiles();
        // If a bad folder or unreadable or whatever, this will come back null
        if (files == null) return false;

        // Alphabetize files, since it's not always alpha order
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                return file.getName().compareToIgnoreCase(file2.getName());
            }
        });

        boolean ifound = false;

        for (File subfolder : files) {
            // Checks if hidden file
            if (subfolder.isHidden() || subfolder.getName().charAt(0) == '.') {
                continue;
            }

            if (!subfolder.isDirectory()) continue;

            if (getNestedSketches(subfolder.getName(), subfolder, sketches, levels, level)) {
                ifound = true;
            }
        }
        return ifound;
    }

    public static boolean getNestedSketches(String name, File folder, ArrayList<String> sketches,
                                             ArrayList<Integer> levels, int level) {

        File entry = new File(folder, name + ".ino");
        if (entry.exists()) {
            sketches.add(name);
            levels.add(level);
            return true;
        }

        // not a sketch folder, but maybe a subfolder containing sketches
        sketches.add(name);
        levels.add(level);

        boolean found = getSketches(folder, sketches, levels, level + 1);

        if (!found) {
            sketches.remove(sketches.size() - 1);
            levels.remove(levels.size() - 1);
        }
        return found;
    }

    public static void getAllFiles(File directory,
                                    ArrayList<String> files, ArrayList<Integer> levels) {
        getAllFiles(directory, files, levels, 0);
    }

    public static void getAllFiles(File directory,
                                    ArrayList<String> files, ArrayList<Integer> levels, int level) {
        // get all the files from a directory
        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (!file.isHidden()) {
                    files.add(file.getName());
                    levels.add(level);
                    if (file.isDirectory()) {
                        getAllFiles(file, files, levels, level + 1);
                    }
                }
            }
        }
    }

    public static void sketchesToJSON(ArrayList<String> sketches, ArrayList<Integer> levels, boolean displayIno) {
        if (sketches.size() == 0) {
            return;
        }
        StringBuilder output = new StringBuilder("{\n");

        // first sketch
        output.append(tabToSpaces(1) + "{\"" + sketches.get(0) + "\" : [\n");

        // middle sketches
        sketchesToJSON(output, sketches, levels, 1, displayIno);

        // last sketch
        int lastIndex = levels.size() - 1;

        if (displayIno) {
            output.append(tabToSpaces(levels.get(lastIndex) + 2) + sketches.get(lastIndex) + ".ino\n");
        }

        for (int i = levels.get(lastIndex); i > 0; i--) {
            output.append(tabToSpaces(levels.get(i) + 1) + "]}\n");
        }
        // print last closing bracket for previous level 0 sketch
        output.append(tabToSpaces(1) + "]}\n");

        output.append("}");

        System.out.println(output.toString());
    }

    public static void sketchesToJSON(StringBuilder output, ArrayList<String> sketches, ArrayList<Integer> levels,
                                      int currentIndex, boolean displayIno) {
        if (currentIndex == sketches.size()) {
            return;
        }

        // checks if done recursing into a sketch path by checking the depth
        if (levels.get(currentIndex) <= levels.get(currentIndex - 1)) {
            int index = currentIndex - 1;

            // previous sketch has the same depth
            if (levels.get(currentIndex).intValue() == levels.get(index).intValue()) {
                output.append(tabToSpaces(levels.get(index) + 1) + "]},\n");

                // previous sketch does not have the same depth
            } else {

                // base level
                if (levels.get(currentIndex) == 0) {
                    // print closing brackets for previous level
                    for (int i = levels.get(index); i > 0; i--) {
                        output.append(tabToSpaces(levels.get(i) + 1) + "]}\n");
                    }

                    // print last closing bracket for previous level 0 sketch
                    output.append(tabToSpaces(levels.get(currentIndex) + 1) + "]},\n");
                } else {
                    // print closing brackets for sketches not at base level
                    while((index > 0) &&
                            (levels.get(currentIndex) < levels.get(index))) {
                        output.append(tabToSpaces(levels.get(index) + 1) + "]}\n");
                        index--;
                    }
                    // hits same level case and needs to print comma
                    output.append(tabToSpaces(levels.get(index) + 1) + "]},\n");
                }
            }
        }

        // prints out current sketch folder name
        output.append(tabToSpaces(levels.get(currentIndex) + 1) + "{\"" + sketches.get(currentIndex) + "\" : [\n");

        // prints out .ino file
        if (displayIno) {
            if ((currentIndex != sketches.size() - 1) &&
                    (levels.get(currentIndex) >= levels.get(currentIndex + 1))) {
                output.append(tabToSpaces(levels.get(currentIndex) + 2) + sketches.get(currentIndex) + ".ino\n");
            }
        }

        // recurse for next sketch
        sketchesToJSON(output, sketches, levels, currentIndex + 1, displayIno);
    }

    private static String tabToSpaces(int n) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < n * 4; i++) {
            spaces.append(" ");
        }
        return spaces.toString();
    }
}

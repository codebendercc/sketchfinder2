import java.io.File;
import java.util.Comparator;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Law on 10/10/15.
 */
public class SketchFinder {

    private static boolean help = false;
    private static boolean showIno = true;
    private static boolean isZip = false;

    public static void main(String[] args) {

        parseArgs(args);

        if (help) {
            System.out
                    .print("usage: sketchFinder [-h --h --help -help] filepath [options] \n"
                            + "options include: \n"
                            + "   -a:    to show all files \n"); 
            return;
        }

        String dirName;
        File f;

        // if zip file
        if ( (args[0].length() > 4) && args[0].substring(args[0].length()-4).equals(".zip")){
            isZip = true;
            dirName = createZipDirectory(args[0]);
            f = new File(dirName);
        } else {
            dirName = args[0];
            f = new File(dirName);
        }

        ArrayList<File> files = new ArrayList<File>();
        ArrayList<String> sketches = new ArrayList<String>();
        ArrayList<Integer> levels = new ArrayList<Integer>();
        String output;

        if (showIno) {
            // gets list of sketches
            SketchFinder.getSketches(f, files, sketches, levels);

            // displays sketches in JSON format
            output = SketchFinder.sketchesToJSON(files, sketches, levels);
            System.out.println(output);
        } else {
            // gets list of all files
            SketchFinder.getAllFiles(f, files, sketches, levels);

            // displays sketches in JSON format
            output = SketchFinder.sketchesToJSONAll(files, sketches, levels);
            System.out.println(output);
        }

        // delete directories from zip file
        if (isZip) {
            deleteDirectory(f);
        }
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

    /**
     * Populates sketches and levels array with list of valid sketches
     * and their depths in the directory
     */
    public static void getSketches(File folder, ArrayList<File> filesList, 
                    ArrayList<String> sketches, ArrayList<Integer> levels) {
        getSketches(folder, filesList, sketches, levels, 0);
    }

    public static boolean getSketches(File folder, ArrayList<File> filesList, 
            ArrayList<String> sketches, ArrayList<Integer> levels, int level) {
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

            // found valid sketch
            if (getNestedSketches(subfolder.getName(), subfolder, filesList, sketches, levels, level)) {
                ifound = true;
            }
        }
        return ifound;
    }

    public static boolean getNestedSketches(String name, File folder, ArrayList<File> filesList,
            ArrayList<String> sketches, ArrayList<Integer> levels, int level) {

        File entry = new File(folder, name + ".ino");
        if (entry.exists()) {
            filesList.add(entry);
            sketches.add(name);
            levels.add(level);
            return true;
        }

        // add sketch to list
        filesList.add(entry);
        sketches.add(name);
        levels.add(level);

        boolean found = getSketches(folder, filesList, sketches, levels, level + 1);

        // sketch was invalid
        if (!found) {
            filesList.remove(filesList.size() - 1);
            sketches.remove(sketches.size() - 1);
            levels.remove(levels.size() - 1);
        }
        return found;
    }

    /**
     * Finds all files from the directory and populates it in files list.
     * Also populates levels list with corresponding depth in directory
     */
    public static void getAllFiles(File directory, ArrayList<File> filesList,
                                    ArrayList<String> fileNames, ArrayList<Integer> levels) {
        getAllFiles(directory, filesList, fileNames, levels, 0);
    }

    public static void getAllFiles(File directory, ArrayList<File> filesList, 
                                    ArrayList<String> fileNames, ArrayList<Integer> levels, int level) {
        // get all the files from a directory
        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (!file.isHidden()) {
                    filesList.add(file);
                    fileNames.add(file.getName());
                    levels.add(level);
                    if (file.isDirectory()) {
                        getAllFiles(file, filesList, fileNames, levels, level + 1);
                    }
                }
            }
        }
    }

    /**
     * Displays sketches in JSON format.
     * Wrapper function that displays first and last sketches
     * Prints middle sketches by delegating to overloaded method
     */
    public static String sketchesToJSON(ArrayList<File> filesList, 
                    ArrayList<String> sketches, ArrayList<Integer> levels) {
        if (sketches.size() == 0) {
            return "No sketches detected.";
        }
        StringBuilder output = new StringBuilder("[\n");

        // first sketch
        output.append(tabToSpaces(1) + "{\"" + sketches.get(0) + "\" : [\n");

        // middle sketches
        sketchesToJSON(output, filesList, sketches, levels, 1);

        // last sketch
        int lastIndex = levels.size() - 1;

        output.append(tabToSpaces(levels.get(lastIndex) + 2) + "\"" +sketches.get(lastIndex) + ".ino\"\n");

        for (int i = levels.get(lastIndex); i > 0; i--) {
            output.append(tabToSpaces(levels.get(i) + 1) + "]}\n");
        }
        // print last closing bracket for previous level 0 sketch
        output.append(tabToSpaces(1) + "]}\n");

        output.append("]");

        return output.toString();
    }

    /**
     * Displays sketches in JSON format.
     */
    public static void sketchesToJSON(StringBuilder output, ArrayList<File> filesList,
                ArrayList<String> sketches, ArrayList<Integer> levels, int currentIndex) {
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
        if ((currentIndex != sketches.size() - 1) &&
                (levels.get(currentIndex) >= levels.get(currentIndex + 1))) {
            output.append(tabToSpaces(levels.get(currentIndex) + 2) + "\"" + sketches.get(currentIndex) + ".ino\"\n");
        }

        // recurse for next sketch
        sketchesToJSON(output, filesList, sketches, levels, currentIndex + 1);
    }

    /**
     * Used to print all files
     */
    public static String sketchesToJSONAll(ArrayList<File> filesList, 
                    ArrayList<String> sketches, ArrayList<Integer> levels) {
        if (sketches.size() == 0) {
            return "No files detected.";
        }
        StringBuilder output = new StringBuilder("[\n");

        // first sketch
        if (filesList.get(0).isDirectory()) {
            output.append(tabToSpaces(1) + "{\"" + sketches.get(0) + "\" : [\n");
        } else {
            output.append(tabToSpaces(1) + "\"" + sketches.get(0) + "\"");
        }

        // middle sketches
        sketchesToJSONAll(output, filesList, sketches, levels, 1);

        // last sketch
        int lastIndex = levels.size() - 1;
        int index = levels.get(lastIndex);

        // check for last level isDirectory / needs bracket or not
        while((index > 0) &&
                (levels.get(lastIndex) < levels.get(index)) ) {
            if (filesList.get(index).isDirectory()) {
                output.append(tabToSpaces(levels.get(index) + 1) + "]}\n");
            }
            index--;
        }

        if (levels.get(lastIndex) == index) {
            output.append("\n");
        }

        // print last closing bracket for previous level 0 sketch
        output.append(tabToSpaces(1) + "]}\n");

        output.append("]");

        return output.toString();
    }

    /**
     * Used to print all files
     */
    public static void sketchesToJSONAll(StringBuilder output, ArrayList<File> filesList,
                ArrayList<String> sketches, ArrayList<Integer> levels, int currentIndex) {
        if (currentIndex == sketches.size()) {
            return;
        }

        // checks if done recursing into a sketch path by checking the depth
        if (levels.get(currentIndex) <= levels.get(currentIndex - 1)) {
            int index = currentIndex - 1;

            // previous sketch has the same depth
            if (levels.get(currentIndex).intValue() == levels.get(index).intValue()) {
                // is directory
                if (filesList.get(currentIndex).isDirectory()) {
                    if (filesList.get(currentIndex - 1).isDirectory()) {
                        output.append(tabToSpaces(levels.get(index) + 1) + "]},\n");
                    } else {
                        output.append(tabToSpaces(levels.get(index) + 1) + "\n");
                    }
                } else {
                    output.append(",\n");
                }

            // previous sketch does not have the same depth
            } else {

                // base level
                if (levels.get(currentIndex) == 0) {
                    // print closing brackets for previous level
                    for (int i = levels.get(index); i > 0; i--) {
                        output.append(tabToSpaces(levels.get(i) + 1) + "\n");
                    }

                    // previous file was directory
                    if (filesList.get(currentIndex - 1).isDirectory()) {
                        int spaceOffset = (levels.get(currentIndex - 1) + 1) * 4;
                        output.delete(output.length() - spaceOffset, output.length() - 1);
                        output.append(tabToSpaces(levels.get(currentIndex) + 2) + "]}\n");
                    }

                    // print last closing bracket for previous level 0 sketch
                    output.append(tabToSpaces(levels.get(currentIndex) + 1) + "]},\n");
                } else {
                    if (!filesList.get(currentIndex - 1).isDirectory()) {
                        index--;
                        output.append("\n");
                        // print closing brackets for sketches not at base level
                        while((index > 0) &&
                                (levels.get(currentIndex) < levels.get(index)) ) {
                            if (filesList.get(index).isDirectory()) {
                                output.append(tabToSpaces(levels.get(index) + 1) + "]}\n");
                            }
                            index--;
                        }
                        // hits same level case and needs to print comma
                        output.append(tabToSpaces(levels.get(index) + 1) + "]},\n");
                    }
                }
            }
        }

        if (filesList.get(currentIndex).isDirectory()) {
            // prints out current folder name
            output.append(tabToSpaces(levels.get(currentIndex) + 1) + "{\"" + sketches.get(currentIndex) + "\" : [\n");
        } else {
            // prints out current file name
            output.append(tabToSpaces(levels.get(currentIndex) + 1) + "\"" + sketches.get(currentIndex) + "\"");
        }

        // recurse for next sketch
        sketchesToJSONAll(output, filesList, sketches, levels, currentIndex + 1);
    }

    /**
     * Converts tabs to spaces and returns as a String
     */
    private static String tabToSpaces(int n) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < n * 4; i++) {
            spaces.append(" ");
        }
        return spaces.toString();
    }

    /**
     * Creates the file structure of a zip file
     * Returns name of parent directory in zip file
     */
    public static String createZipDirectory(String zipName) {
        try {
            // Open the zip file
            String parentDirName = "";
            boolean isFirstFile = true;

            ZipFile zipFile = new ZipFile(zipName);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                String name = zipEntry.getName();
                long size = zipEntry.getSize();
                long compressedSize = zipEntry.getCompressedSize();

                if (isFirstFile) {
                    parentDirName = name;
                    isFirstFile = false;
                }

                // Do we need to create a directory ?
                File file = new File(name);
                if (name.endsWith("/")) {
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                // Extract the file
                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();

            }
            zipFile.close();
            return parentDirName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // recursively deletes the directory, used to delete directory created by zipfile
    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for(int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return directory.delete();
    }
}

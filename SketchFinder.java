import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    // program flags for output
    private static boolean help = false;
    private static boolean showIno = true;
    private static boolean isZip = false;
    private static boolean showLibs = false;
    private static boolean showCodeFiles = false;

    // used to detect code files that are opened up by the Arduino IDE
    public static final List<String> SKETCH_EXTENSIONS = Arrays.asList("ino", "pde");
    public static final List<String> OTHER_ALLOWED_EXTENSIONS = Arrays.asList("c", "cpp", "h", "hh", "hpp", "s");
    public static final List<String> EXTENSIONS = Stream.concat(SKETCH_EXTENSIONS.stream(), OTHER_ALLOWED_EXTENSIONS.stream()).collect(Collectors.toList());

    public static void main(String[] args) {

        parseArgs(args);

        if (help) {
            System.out
                    .print("usage: sketchFinder [-h --h --help -help] filepath [option] \n"
                            + "[Note: Only one option may be used] \n"
                            + "options include: \n"
                            + "   -a:    to show all files \n"
                            + "   -l:    to show libraries \n"
                            + "   -c:    to show code files opened up by IDE \n"); 
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

        // used to contain names of relevant files to display
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
        } else if (showLibs) {
            // looks for libraries folder
            f = new File(dirName + "/libraries");

            // gets list of libraries
            SketchFinder.getSketches(f, files, sketches, levels);

            // displays sketches in JSON format
            output = SketchFinder.sketchesToJSON(files, sketches, levels);
            System.out.println(output);
        } else if (showCodeFiles) {
            // gets code files displayed in IDE tabs
            ArrayList<File> codeList = null;
            try {
                codeList = getCodeFiles(f);
            } catch (IOException e) {
                System.out.println(e);
            }

            // displays code files 
            if (codeList != null) {
                for (File codeFile : codeList) {
                    System.out.println(codeFile);
                }
            }
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

    /**
     * Processes command line arguments and sets flags used to display 
     * information to the user properly
     *
     * @param args command line arguments 
     */
    public static void parseArgs(String[] args) {
        if (args == null || args.length == 0 || args[0].equals("--help")
                || args[0].equals("-help") || args[0].equals("-h")
                || args[0].equals("--h")) {
            // If a help argument or no argument is specified, calls for the
            // help menu.
            help = true;
        } else {
            boolean showOne = false;
            for (int i = 0; i < args.length; i++) {
                if (showOne) {
                    help = true;
                    break;
                } else if (args[i].equalsIgnoreCase("-a")) {
                    showIno = false;
                    showOne = true;
                } else if (args[i].equalsIgnoreCase("-l")) {
                    showLibs = true;
                    showIno = false;
                    showOne = true;
                } else if (args[i].equalsIgnoreCase("-c")) {
                    showCodeFiles = true;
                    showIno = false;
                    showOne = true;
                } else if (i != 0) {
                    help = true;
                    break;
                }
            }
        }
    }

    /**
     * Populates sketches and levels array with list of valid sketches
     * and their depths in the directory.
     * 
     * @param folder folder to check for sketches within
     * @param filesList empty list that will contain list of files of valid sketches
     * @param sketches empty list that will contain list of file names of valid sketches
     * @param levels empty list that will contain list of corresponding depths of files
     */
    public static void getSketches(File folder, ArrayList<File> filesList, 
                    ArrayList<String> sketches, ArrayList<Integer> levels) {
        getSketches(folder, filesList, sketches, levels, 0);
    }

    /**
     * Populates sketches and levels array with list of valid sketches
     * and their depths in the directory.
     * Checks subdirectories.
     * 
     * @param folder folder to check for sketches within
     * @param filesList empty list that will contain list of files of valid sketches
     * @param sketches empty list that will contain list of file names of valid sketches
     * @param levels empty list that will contain list of corresponding depths of files
     * @param level current depth of searching filepaths
     */
    public static boolean getSketches(File folder, ArrayList<File> filesList, 
            ArrayList<String> sketches, ArrayList<Integer> levels, int level) {
        if (folder == null) {
            return false;
        }

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

    /**
     * Populates sketches and levels array with list of valid sketches
     * and their depths in the directory.
     * Checks if valid sketch was found.
     * 
     * @param folder folder to check for sketches within
     * @param filesList empty list that will contain list of files of valid sketches
     * @param sketches empty list that will contain list of file names of valid sketches
     * @param levels empty list that will contain list of corresponding depths of files
     * @param level current depth of searching filepaths
     */
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
     * 
     * @param folder folder to check for sketches within
     * @param filesList empty list that will contain list of files of valid sketches
     * @param sketches empty list that will contain list of file names of valid sketches
     * @param levels empty list that will contain list of corresponding depths of files
     */
    public static void getAllFiles(File directory, ArrayList<File> filesList,
                                    ArrayList<String> fileNames, ArrayList<Integer> levels) {
        getAllFiles(directory, filesList, fileNames, levels, 0);
    }

    /**
     * Finds all files from the directory and populates it in files list.
     * Also populates levels list with corresponding depth in directory
     * 
     * @param folder folder to check for sketches within
     * @param filesList empty list that will contain list of files of valid sketches
     * @param sketches empty list that will contain list of file names of valid sketches
     * @param levels empty list that will contain list of corresponding depths of files
     * @param level current depth of searching filepaths
     */
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
     *
     * @param filesList list of files of valid sketches
     * @param sketches list of file names of valid sketches
     * @param levels list of corresponding depths of files
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

        // displays last ino file
        output.append(tabToSpaces(levels.get(lastIndex) + 2) + "\"" +sketches.get(lastIndex) + ".ino\"\n");

        // displays closing brackets
        for (int i = levels.get(lastIndex); i > 0; i--) {
            output.append(tabToSpaces(i + 1) + "]}\n");
        }
        // print last closing bracket for previous level 0 sketch
        output.append(tabToSpaces(1) + "]}\n");

        output.append("]");

        return output.toString();
    }

    /**
     * Displays sketches in JSON format.
     * Wrapper function that displays first and last sketches
     * Prints middle sketches by delegating to overloaded method
     *
     * @param filesList list of files of valid sketches
     * @param sketches list of file names of valid sketches
     * @param levels list of corresponding depths of files
     * @param currentIndex current index within sketches list
     */
     */
    public static void sketchesToJSON(StringBuilder output, ArrayList<File> filesList,
                ArrayList<String> sketches, ArrayList<Integer> levels, int currentIndex) {
        if (currentIndex == sketches.size()) {
            return;
        }

        int index = currentIndex - 1;

        // checks if done recursing into a sketch path by checking the depth and 
        // previous sketch has the same depth
        if ((levels.get(currentIndex) <= levels.get(currentIndex - 1)) &&
            (levels.get(currentIndex).intValue() == levels.get(index).intValue())
            ) {

            output.append(tabToSpaces(levels.get(index) + 1) + "]},\n");

        // previous sketch does not have the same depth
        } else if ((levels.get(currentIndex) <= levels.get(currentIndex - 1)) &&
            (levels.get(currentIndex).intValue() != levels.get(index).intValue())
            ) {

            int brackets = levels.get(index);
            int spaces = brackets + 1;

            // prints closing brackets
            while (brackets > levels.get(currentIndex)) {
                output.append(tabToSpaces(spaces--) + "]}\n");
                brackets--;
            }
            output.append(tabToSpaces(spaces) + "]},\n");
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
     * Used to print all files in JSON format.
     *
     * @param filesList list of files of valid sketches
     * @param sketches list of file names of valid sketches
     * @param levels list of corresponding depths of files
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
        int index = levels.get(lastIndex) - 1;

        // brackets for last file
        int i = filesList.get(index).isDirectory() ? levels.get(index) : levels.get(index) - 1;
        int spaces = i + 1;
        // print closing brackets for previous level
        for (; i > 0; i--) {
            output.append(tabToSpaces(spaces--) + "]}\n");
        }

        // print last closing bracket for previous level 0 sketch
        if (filesList.get(lastIndex).isDirectory()) {
            i = levels.get(levels.size() - 1);
            spaces = i + 1;
            // print closing brackets for previous level
            for (; i > 0; i--) {
                output.append(tabToSpaces(spaces--) + "]}\n");
            }
        }

        if (!filesList.get(lastIndex).isDirectory()) {
            output.append("\n");
        }

        // print last closing bracket for previous level 0 sketch
        output.append(tabToSpaces(levels.get(lastIndex)) + "]}\n");

        output.append("]");

        return output.toString();
    }

    /**
     * Used to print all files
     *
     * @param filesList list of files of valid sketches
     * @param sketches list of file names of valid sketches
     * @param levels list of corresponding depths of files
     * @param currentIndex current index within sketches list
     */
    public static void sketchesToJSONAll(StringBuilder output, ArrayList<File> filesList,
                ArrayList<String> sketches, ArrayList<Integer> levels, int currentIndex) {
        if (currentIndex == sketches.size()) {
            return;
        }

        int index = currentIndex - 1;

        // checks if done recursing into a sketch path by checking the depth and 
        // previous sketch has the same depth
        if ((levels.get(currentIndex) <= levels.get(currentIndex - 1)) &&
            (levels.get(currentIndex).intValue() == levels.get(index).intValue())
            ) {

            // is directory
            if (filesList.get(currentIndex).isDirectory()) {
                StringBuilder sb = new StringBuilder(tabToSpaces(levels.get(index) + 1));

                // last file was also directory
                if (filesList.get(currentIndex - 1).isDirectory()) {
                    sb.append("]},");
                }
                output.append(sb + "\n");
            } else {
                output.append(",\n");
            }
        } else if ((levels.get(currentIndex) <= levels.get(currentIndex - 1)) &&
            (levels.get(currentIndex).intValue() != levels.get(index).intValue())
            ) {

            output.append("\n");
            int brackets = levels.get(index) - 1;
            int spaces = brackets + 1;

            // prints closing brackets
            while (brackets > levels.get(currentIndex)) {
                output.append(tabToSpaces(spaces--) + "]}\n");
                brackets--;
            }
            output.append(tabToSpaces(spaces) + "]},\n");
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
     *
     * @param n number of tabs to print
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
     *
     * @param zipName name of zip file
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

    /**
     * Deletes the directory.
     * Used to delete directory created by zipfile.
     *
     * @param directory directory to delete
     * @return true if directory was successfully deleted
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for(int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    /**
     * Builds list of valid code files opened by Arduio IDE
     *
     * @param f directory to grab code files from
     * @return list of valid code files that are opened up by Arduino IDE
     */
    public static ArrayList<File> getCodeFiles(File f) throws IOException {
        ArrayList<File> result = new ArrayList<File>();
        String[] list = f.list();
        if (list == null) {
            throw new IOException("Unable to list files from " + f);
        }

        for (String filename : list) {
            // Ignoring the dot prefix files is especially important to avoid files
            // with the ._ prefix on Mac OS X. (You'll see this with Mac files on
            // non-HFS drives, i.e. a thumb drive formatted FAT32.)
            if (filename.startsWith(".")) continue;

            // Don't let some wacko name a directory blah.pde or bling.java.
            if (new File(f, filename).isDirectory()) continue;

            // figure out the name without any extension
            String base = filename;
            // now strip off the .pde and .java extensions
            for (String extension : EXTENSIONS) {
                if (base.toLowerCase().endsWith("." + extension)) {
                    base = base.substring(0, base.length() - (extension.length() + 1));

                    // Don't allow people to use files with invalid names, since on load,
                    // it would be otherwise possible to sneak in nasty filenames. [0116]
                    if (isSanitaryName(base)) {
                    result.add(new File(filename));
                    } else {
                    System.err.println("File name " + filename + " is invalid: ignored");
                    }
                }
            }
        }

        if (result.size() == 0)
            throw new IOException("No valid code files found");

        return result;
    }

    /**
    * Determines if the name is valid for a Processing sketch.
    *
    * @param name name of the file to check
    * @return true if valid name
    */
    public static boolean isSanitaryName(String name) {
        return sanitizeName(name).equals(name);
    }

    /**
     * Produce a sanitized name that fits our standards for likely to work.
     * <p/>
     * Java classes have a wider range of names that are technically allowed
     * (supposedly any Unicode name) than what we support. The reason for
     * going more narrow is to avoid situations with text encodings and
     * converting during the process of moving files between operating
     * systems, i.e. uploading from a Windows machine to a Linux server,
     * or reading a FAT32 partition in OS X and using a thumb drive.
     * <p/>
     * This helper function replaces everything but A-Z, a-z, and 0-9 with
     * underscores. Also disallows starting the sketch name with a digit.
     *
     * @param origName original name of file to check
     * @return String sanitized name of the file
     */
    public static String sanitizeName(String origName) {
        char c[] = origName.toCharArray();
        StringBuffer buffer = new StringBuffer();

        // can't lead with a digit, so start with an underscore
        if ((c[0] >= '0') && (c[0] <= '9')) {
            buffer.append('_');
        }
        for (int i = 0; i < c.length; i++) {
            if (((c[i] >= '0') && (c[i] <= '9')) ||
                ((c[i] >= 'a') && (c[i] <= 'z')) ||
                ((c[i] >= 'A') && (c[i] <= 'Z')) ||
                ((i > 0) && (c[i] == '-')) ||
                ((i > 0) && (c[i] == '.'))) {
                buffer.append(c[i]);
            } else {
                buffer.append('_');
            }
        }
        // let's not be ridiculous about the length of filenames.
        // in fact, Mac OS 9 can handle 255 chars, though it can't really
        // deal with filenames longer than 31 chars in the Finder.
        // but limiting to that for sketches would mean setting the
        // upper-bound on the character limit here to 25 characters
        // (to handle the base name + ".class")
        if (buffer.length() > 63) {
            buffer.setLength(63);
        }
        return buffer.toString();
    }

    /**
     * Checks, whether the child directory is a subdirectory of the base directory.
     *
     * @param base  the base directory.
     * @param child the suspected child directory.
     * @return true, if the child is a subdirectory of the base directory.
     */
    public static boolean isSubDirectory(File base, File child) {
        try {
            base = base.getCanonicalFile();
            child = child.getCanonicalFile();
        } catch (IOException e) {
            return false;
        }

        File parentFile = child;
        while (parentFile != null) {
        if (base.equals(parentFile)) {
            return true;
        }
            parentFile = parentFile.getParentFile();
        }
        return false;
    }
}
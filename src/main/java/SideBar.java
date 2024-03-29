import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 生成_sidebar.md文件
 */
public class SideBar {

    private final static String IGNORE_FILE_PREFIX = "_";

    public static void main(String[] args) {
        String path = SideBar.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf(getDicSeparator(path)));
        StringBuilder text = getSlideBarText(path);
        try {
            overwriteSlidebar(path, text);
            System.out.println("write the file successfully: _sidebar.md");
        } catch (IOException e) {
            System.out.println("failed to write the file: " + e.getMessage());
        }
    }

    private static void overwriteSlidebar(String path, StringBuilder text) throws IOException {
        String fileName = "_sidebar.md";
        File f = new File(path + getDicSeparator(path) + fileName);
        if (!f.exists()) {
            f.createNewFile();
        }
        Writer writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        writer.write(text.toString());
        writer.close();
    }

    private static String getDicSeparator(String path) {
        return path.indexOf("/") > -1 ? "/" : "\\";
    }

    private static StringBuilder getSlideBarText(String path) {
        StringBuilder result = new StringBuilder();

        File rootDic = new File(path);

        String rootPath = rootDic.getPath() + getDicSeparator(rootDic.getPath());

        if (!rootDic.isDirectory()) {
            System.out.println("不是文件夹");
            return null;
        }
        // 获取根目录下所有文件
        File[] subFiles = rootDic.listFiles();

        int rootLevel = 0;
        for (File f : subFiles) {
            String ignorePatternStr = "node_modules";
            // 跳过当前文件
            boolean ignorePattern = Pattern.matches(ignorePatternStr, f.getName());
            boolean isCurrentFile = f.getName().endsWith("docsify-slidebar") && f.getName().endsWith(".jar");
            boolean isDotPrefix = f.getName().startsWith(".");
            boolean isUnderlinePrefix = f.getName().startsWith(IGNORE_FILE_PREFIX);
            if (isCurrentFile || isDotPrefix || isUnderlinePrefix || ignorePattern) {
                continue;
            }
            if (f.isDirectory()) {
                boolean isEmpty = dirIsEmpty(f);
                if (!isEmpty) {
                    result.append(dicSlideBar(f, rootLevel, rootPath));
                }
            }
        }
        return result;
    }

    private static String fileSlideBar(File file, int level, String rootPath) {
        if (file.getName().startsWith(IGNORE_FILE_PREFIX)) {
            return "";
        }
        return getIndent(level) + "- [" + getSlideBarTitle(file) + "](" + fileNameSpecialHandle(file, rootPath) + ")\n";
    }

    private static String getSlideBarTitle(File file) {
        String title = file.getName().lastIndexOf(".") > 0 ? file.getName().substring(0, file.getName().lastIndexOf(".")) : file.getName();
        if (title.indexOf(" ") > -1) {
            title.replace(" ", "");
        }
        return title;
    }

    private static String fileNameSpecialHandle(File file, String rootPath) {
        String title = file.getPath().replace(rootPath, "");

        if (file.getName().startsWith("@")) {
            title = title.replace(file.getName(), "%5C%40" + file.getName().substring(1));
        }

        if (title.indexOf(" ") > -1) {
            title = title.replaceAll(" ", "%20");
        }
        return title.replaceAll("\\\\", "/");
    }

    private static Boolean dirIsEmpty(File targetFile) {
        boolean isEmpty = true;
        for (File fileItem : targetFile.listFiles()) {
            if (fileItem.getName().endsWith(".md")) {
                isEmpty = false;
                break;
            }
            if (fileItem.isDirectory()) {
                boolean innerIsEmpty = dirIsEmpty(fileItem);
                if (!innerIsEmpty) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    private static String dicSlideBar(File file, int level, String rootPath) {
        if (file.getName().startsWith(IGNORE_FILE_PREFIX)) {
            return "";
        }
        StringBuilder result = new StringBuilder(getIndent(level) + "- " + file.getName() + "\n");

        File[] subFiles = file.listFiles();
        if (subFiles.length == 0) {
            return "";
        }
        for (File f : subFiles) {
            if (f.isDirectory()) {
                boolean isEmpty = dirIsEmpty(f);
                if (!isEmpty) {
                    result.append(dicSlideBar(f, level + 1, rootPath));
                }
            } else if (f.getName().endsWith(".md")) {
                result.append(fileSlideBar(f, level + 1, rootPath));
            }
        }

        return result.toString();
    }

    private static String getIndent(int level) {
        if (level == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        while (level-- > 0) {
            result.append("  ");
        }
        return result.toString();
    }
}

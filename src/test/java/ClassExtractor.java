import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

/**
 * How to get the modern Intellij Look and Feels:
 * <br/><br/>
 * <p>
 * 1. Add the look and field class name to your application. For example,
 * `UIManager.setLookAndFeel("com.intellij.ide.ui.laf.darcula.DarculaLightLaf");`, or
 * `UIManager.setLookAndFeel("com.intellij.ide.ui.laf.IdeaLaf");`.
 * <br/><br/>
 * <p>
 * 2. Run your application with Intellij's `lib` directory on the classpath, and export
 * the loaded class list to a file. Make sure to play around with your app so that every
 * UI component that can be loaded makes it into the classlist<br/><br/>
 * <p>
 * java -verbose:class -cp "/Applications/IntelliJ IDEA.app/Contents/lib/*" MyClass > classlist.txt
 * <br/><br/>
 * <p>
 * 3. Run this program and pass the classlist as an argument.
 * <br/><br/>
 * <p>
 * 4. A directory named `extracted` is created, with the minimal classes & resources
 * extracted. You can now add this folder to your classpath to get the desired theme.
 * NOTE: The Intellij icons jar must be manually added to your classpath. It is in the same
 * Intellij instalation lib folder. And the applications must be run on Intellij Runtime
 * (this is a best practice anyway, since IJR has many patches for swing performance improvements)
 * <br/><br/>
 * <p>
 * Unfortunately the Darcula (and Idea) themes have become very
 * coupled to the Intellij codebase. Even a running ClassExtractor on a simple program will pull
 * in 5+ MB of dependencies, including the Kotlin standard library. Hopefully Jetbrains
 * takes some time to update up their code, and let developers use their great themes!
 */
public class ClassExtractor {

    private static final Pattern reg = Pattern.compile(".*load] (?<cls>\\S+) source: (?<jar>.+/lib/.+)");

    public static void main(String[] args) throws IOException {
        Path classListPath = Paths.get(args[0]);

        Matcher m = reg.matcher("");

        // Keys are the Jars in Intellij's lib directory, and
        // Values are the set of classnames that your program depends on
        // for the look and feel.
        Map<Path, Set<String>> loadedclasses = Files.lines(classListPath)
                .map(m::reset)
                .filter(Matcher::matches)
                .map(ClassExtractor::buildLoadedClass)
                // ignore the jre stdlib jar
                .filter(lc -> !lc.getJarPath().toString().contains("rt.jar"))
                .collect(groupingBy(LoadedClass::getJarPath, mapping(LoadedClass::getCls, toSet())));


        Path base = Paths.get("extracted");
        Files.createDirectories(base);

        // for each Jar that we depend on, open a JarFile
        // and extract the contents that we need

        for (Map.Entry<Path, Set<String>> entry : loadedclasses.entrySet()) {
            Path jarPath = entry.getKey();
            Set<String> classNames = entry.getValue();

            try (JarFile jf = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jf.entries();

                while (entries.hasMoreElements()) {
                    JarEntry ze = entries.nextElement();
                    String zeName = ze.getName();
                    Path zePath = base.resolve(zeName);

                    if (ze.isDirectory()
                            || (zeName.endsWith(".class") && !classNames.contains(zeName))
                            || !(zeName.contains("resources") || !zeName.contains("WEB_INF") || !zeName.contains("properties")))
                        continue;

                    Files.createDirectories(zePath.getParent());
                    try (BufferedInputStream is = new BufferedInputStream(jf.getInputStream(ze))) {

                        Files.copy(is, zePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

    }

    private static LoadedClass buildLoadedClass(Matcher m) {
        String jar = m.group("jar");
        jar = jar.substring(jar.indexOf('/') + 1);
        jar = jar.replaceAll("%20", " ");
        Path jarPath = Paths.get(jar);

        String cls = m.group("cls").replace('.', '/') + ".class";

        return new LoadedClass(jarPath, cls);
    }

    private static final class LoadedClass {
        final Path jarPath;
        final String cls;

        LoadedClass(Path jarPath, String cls) {
            this.jarPath = jarPath;
            this.cls = cls;
        }

        Path getJarPath() {
            return jarPath;
        }

        String getCls() {
            return cls;
        }
    }
}
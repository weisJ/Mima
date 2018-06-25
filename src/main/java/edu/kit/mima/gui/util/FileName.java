package edu.kit.mima.gui.util;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class FileName {

    private FileName() {
        assert false : "utility class constructor";
    }

    public static String shorten(String fileName, int maxLength) {
        String name = fileName;
        String[] split = name.split("\\\\");
        int indexLow = split.length / 2;
        int indexHigh = indexLow + 1;
        while (name.length() > maxLength && indexHigh < split.length && indexLow > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indexLow; i++) {
                sb.append(split[i]).append('\\');
            }
            sb.append("...\\");
            for (int i = indexHigh; i < split.length; i++) {
                sb.append(split[i]);
                if (i != split.length - 1) {
                    sb.append('\\');
                }
            }
            name = sb.toString();
            indexHigh++;
            indexLow--;
        }
        return name;
    }
}

package edu.kit.mima.gui.components.fontchooser.util;

import java.util.ResourceBundle;

public class ResourceBundleUtil {

    private final ResourceBundle resourceBundle;

    public ResourceBundleUtil(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public char getFirstChar(String key) {
        String bundleString = resourceBundle.getString(key);
        return bundleString.charAt(0);
    }

}

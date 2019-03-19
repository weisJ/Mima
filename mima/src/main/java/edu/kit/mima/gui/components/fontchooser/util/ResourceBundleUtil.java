package edu.kit.mima.gui.components.fontchooser.util;

import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class ResourceBundleUtil {

    private final ResourceBundle resourceBundle;

    public ResourceBundleUtil(final ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public char getFirstChar(@NotNull final String key) {
        final String bundleString = resourceBundle.getString(key);
        return bundleString.charAt(0);
    }

}

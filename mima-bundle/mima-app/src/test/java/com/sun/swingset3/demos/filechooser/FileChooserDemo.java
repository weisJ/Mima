/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.swingset3.demos.filechooser;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.swingset3.demos.JGridPanel;
import com.sun.swingset3.demos.ResourceManager;
import com.sun.swingset3.DemoProperties;

/**
 * JFileChooserDemo
 *
 * @author Pavel Porvatov
 */
@DemoProperties(
        value = "JFileChooser Demo",
        category = "Choosers",
        description = "Demonstrates JFileChooser, a component which allows the user to open and save files.",
        sourceFiles = {
                "src/test/com.sun.swingset3/demos/filechooser/FileChooserDemo.java",
                "src/test/com.sun.swingset3/demos/DemoUtilities.java",
                "src/test/com.sun.swingset3/demos/filechooser/resources/FileChooserDemo.properties",
                "src/test/com.sun.swingset3/demos/filechooser/resources/images/apply.png",
                "src/test/com.sun.swingset3/demos/filechooser/resources/images/FileChooserDemo.gif",
                "src/test/com.sun.swingset3/demos/filechooser/resources/images/fliphor.png",
                "src/test/com.sun.swingset3/demos/filechooser/resources/images/flipvert.png",
                "src/test/com.sun.swingset3/demos/filechooser/resources/images/rotateleft.png",
                "src/test/com.sun.swingset3/demos/filechooser/resources/images/rotateright.png"
                }
)
public class FileChooserDemo extends JPanel {
    private enum State {
        EMPTY,
        IMAGE_LOADED,
        IMAGE_CHANGED
    }

    private static final int MIN_FILTER_ID = 0;

    private static final int MAX_FILTER_ID = 7;

    private static final String[] FILTER_NAMES = {
            "FileChooserDemo.filter.blur",
            "FileChooserDemo.filter.edge",
            "FileChooserDemo.filter.sharpen",
            "FileChooserDemo.filter.darken",
            "FileChooserDemo.filter.brighten",
            "FileChooserDemo.filter.lesscontrast",
            "FileChooserDemo.filter.morecontrast",
            "FileChooserDemo.filter.gray"
    };

    private static final BufferedImageOp[] FILTER_OPERATIONS = {
            new ConvolveOp(new Kernel(3, 3,
                    new float[]{.1111f, .1111f, .1111f, .1111f, .1111f, .1111f, .1111f, .1111f, .1111f}),
                    ConvolveOp.EDGE_NO_OP, null),
            new ConvolveOp(new Kernel(3, 3,
                    new float[]{0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f, 0.0f}),
                    ConvolveOp.EDGE_NO_OP, null),
            new ConvolveOp(new Kernel(3, 3,
                    new float[]{0.0f, -1.0f, 0.0f, -1.0f, 5.f, -1.0f, 0.0f, -1.0f, 0.0f}),
                    ConvolveOp.EDGE_NO_OP, null),
            new RescaleOp(1, -5.0f, null),
            new RescaleOp(1, 5.0f, null),
            new RescaleOp(0.9f, 0, null),
            new RescaleOp(1.1f, 0, null),
            new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
    };

    private final ResourceManager resourceManager = new ResourceManager(this.getClass());

    private final JLabel lbImage = new JLabel(resourceManager.getString("FileChooserDemo.image.text"), JLabel.CENTER);

    private final JScrollPane pnImage = new JScrollPane(lbImage);

    private final JButton btnSelect = new JButton(resourceManager.getString("FileChooserDemo.select.text"));

    private final JButton btnSelectWithPreview = new JButton(resourceManager.getString("FileChooserDemo.selectwithpreview.text"));

    private final JComboBox cbFilters = new JComboBox();

    private final JButton btnApplyFilter = createButton("apply.png", "FileChooserDemo.applyfilter.tooltip");

    private final JButton btnRotateLeft = createButton("rotateleft.png", "FileChooserDemo.rotateleft.tooltip");

    private final JButton btnRotateRight = createButton("rotateright.png", "FileChooserDemo.rotateright.tooltip");

    private final JButton btnFlipHorizontal = createButton("fliphor.png", "FileChooserDemo.fliphorizontal.tooltip");

    private final JButton btnFlipVertical = createButton("flipvert.png", "FileChooserDemo.flipvertical.tooltip");

    private final JButton btnSave = new JButton(resourceManager.getString("FileChooserDemo.save.text"));

    private final JButton btnCancel = new JButton(resourceManager.getString("FileChooserDemo.cancel.text"));

    private final JFileChooser externalChooser = new JFileChooser();

    private final JFileChooser embeddedChooser = new JFileChooser();

    private final JGridPanel pnContent = new JGridPanel(1, 0, 0);

    private State state;

    private boolean fileChoosing;

    private File file;

    private BufferedImage image;

    /**
     * main method allows us to run as a standalone demo.
     */
    public static void main(final String[] args) {
        JFrame frame = new JFrame(FileChooserDemo.class.getAnnotation(DemoProperties.class).value());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new FileChooserDemo());
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * FileChooserDemo Constructor
     */
    public FileChooserDemo() {
        setLayout(new BorderLayout());

        initUI();

        embeddedChooser.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
                    loadFile(embeddedChooser.getSelectedFile());
                }

                if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
                    setState(state, false);
                }
            }
        });

        btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (fileChoosing) {
                    loadFile(embeddedChooser.getSelectedFile());
                } else {
                    setState(state, true);
                }
            }
        });

        btnSelectWithPreview.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (externalChooser.showOpenDialog(FileChooserDemo.this) == JFileChooser.APPROVE_OPTION) {
                    loadFile(externalChooser.getSelectedFile());
                }
            }
        });

        btnApplyFilter.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                doFilter(FILTER_OPERATIONS[((FilterItem) cbFilters.getSelectedItem()).getId()]);
            }
        });

        btnRotateLeft.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                doAffineTransform(image.getHeight(), image.getWidth(),
                        new AffineTransform(0, -1, 1, 0, 0, image.getWidth()));
            }
        });

        btnRotateRight.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                doAffineTransform(image.getHeight(), image.getWidth(),
                        new AffineTransform(0, 1, -1, 0, image.getHeight(), 0));
            }
        });

        btnFlipHorizontal.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                doAffineTransform(image.getWidth(), image.getHeight(),
                        new AffineTransform(-1, 0, 0, 1, image.getWidth(), 0));
            }
        });

        btnFlipVertical.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                doAffineTransform(image.getWidth(), image.getHeight(),
                        new AffineTransform(1, 0, 0, -1, 0, image.getHeight()));
            }
        });

        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (JOptionPane.showConfirmDialog(FileChooserDemo.this,
                        resourceManager.getString("FileChooserDemo.savequiestion.message"),
                        resourceManager.getString("FileChooserDemo.savequiestion.title"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }

                String fileName = file.getName();

                int i = fileName.lastIndexOf('.');

                try {
                    ImageIO.write(image, fileName.substring(i + 1), file);

                    setState(State.IMAGE_LOADED, false);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(FileChooserDemo.this,
                            MessageFormat.format(resourceManager.getString("FileChooserDemo.errorsavefile.message"), e1),
                            resourceManager.getString("FileChooserDemo.errorsavefile.title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                loadFile(file);
            }
        });
    }

    private void initUI() {
        externalChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG images", "jpg"));
        externalChooser.addChoosableFileFilter(new FileNameExtensionFilter("All supported images",
                ImageIO.getWriterFormatNames()));

        final FilePreview filePreview = new FilePreview();

        externalChooser.setAccessory(filePreview);
        externalChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent evt) {
                        filePreview.loadFileInfo(externalChooser.getSelectedFile());
                    }
                });

        embeddedChooser.setControlButtonsAreShown(false);

        embeddedChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG images", "jpg"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("All supported images",
                ImageIO.getWriterFormatNames());

        embeddedChooser.addChoosableFileFilter(filter);
        embeddedChooser.setFileFilter(filter);

        for (int i = MIN_FILTER_ID; i <= MAX_FILTER_ID; i++) {
            cbFilters.addItem(new FilterItem(i, resourceManager.getString(FILTER_NAMES[i])));
        }

        JGridPanel pnFilter = new JGridPanel(2, 0);

        pnFilter.cell(cbFilters).
                cell(btnApplyFilter);

        JGridPanel pnRotateButtons = new JGridPanel(4, 3);

        pnRotateButtons.cell(btnRotateLeft).
                cell(btnRotateRight).
                cell(btnFlipHorizontal).
                cell(btnFlipVertical);

        JGridPanel pnBottom = new JGridPanel(4, 1);

        pnBottom.setHGap(JGridPanel.DEFAULT_GAP * 4);

        pnBottom.cell(btnSelect, JGridPanel.Layout.FILL).
                cell().
                cell(pnFilter).
                cell(btnSave, JGridPanel.Layout.FILL).
                cell(btnSelectWithPreview, JGridPanel.Layout.FILL).
                cell().
                cell(pnRotateButtons).
                cell(btnCancel, JGridPanel.Layout.FILL);

        pnContent.cell(pnImage);
        pnContent.cell(pnBottom, new Insets(10, 10, 10, 10));

        add(pnContent);

        setState(State.EMPTY, false);
    }

    private JButton createButton(final String image, final String toolTip) {
        JButton res = new JButton(resourceManager.createImageIcon(image, null));

        res.setPreferredSize(new Dimension(26, 26));
        res.setMinimumSize(new Dimension(26, 26));
        res.setToolTipText(resourceManager.getString(toolTip));

        return res;
    }

    private void doAffineTransform(final int width, final int height, final AffineTransform transform) {
        BufferedImage newImage = new BufferedImage(image.getColorModel(),
                image.getRaster().createCompatibleWritableRaster(width, height),
                image.isAlphaPremultiplied(), new Hashtable<Object, Object>());

        ((Graphics2D) newImage.getGraphics()).drawRenderedImage(image, transform);

        image = newImage;

        lbImage.setIcon(new ImageIcon(image));

        setState(State.IMAGE_CHANGED, false);
    }

    private void doFilter(final BufferedImageOp imageOp) {
        BufferedImage newImage = new BufferedImage(image.getColorModel(),
                image.getRaster().createCompatibleWritableRaster(image.getWidth(), image.getHeight()),
                image.isAlphaPremultiplied(), new Hashtable<Object, Object>());

        imageOp.filter(image, newImage);

        image = newImage;

        lbImage.setIcon(new ImageIcon(image));

        setState(State.IMAGE_CHANGED, false);
    }

    private void loadFile(final File file) {
        if (file == null) {
            JOptionPane.showMessageDialog(this,
                    resourceManager.getString("FileChooserDemo.selectfile.message"),
                    resourceManager.getString("FileChooserDemo.selectfile.title"),
                    JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        try {
            image = ImageIO.read(file);

            if (image != null) {
                lbImage.setText(null);
                lbImage.setIcon(new ImageIcon(image));

                this.file = file;

                setState(State.IMAGE_LOADED, false);

                return;
            }
        } catch (IOException e1) {
            // Do nothing
        }

        JOptionPane.showMessageDialog(this,
                resourceManager.getString("FileChooserDemo.errorloadfile.message"),
                resourceManager.getString("FileChooserDemo.errorloadfile.title"),
                JOptionPane.ERROR_MESSAGE);
    }

    private void setState(final State state, final boolean fileChoosing) {
        if (this.fileChoosing != fileChoosing) {
            pnContent.setComponent(fileChoosing ? embeddedChooser : pnImage, 0, 0);
        }

        this.state = state;
        this.fileChoosing = fileChoosing;

        btnSelectWithPreview.setEnabled(!fileChoosing);

        boolean isImageLoaded = !fileChoosing && state != State.EMPTY;

        cbFilters.setEnabled(isImageLoaded);
        btnApplyFilter.setEnabled(isImageLoaded);
        btnRotateRight.setEnabled(isImageLoaded);
        btnRotateLeft.setEnabled(isImageLoaded);
        btnFlipHorizontal.setEnabled(isImageLoaded);
        btnFlipVertical.setEnabled(isImageLoaded);

        boolean isImageChanged = !fileChoosing && state == State.IMAGE_CHANGED;

        btnSave.setEnabled(isImageChanged);
        btnCancel.setEnabled(isImageChanged);
    }

    private static class FilterItem {
        /**
         * 0 - blur
         * 1 - edge
         * 2 - sharpen
         * 3 - darken
         * 4 - brighten
         * 5 - less contrast
         * 6 - more contrast
         * 7 - gray
         */
        private final int id;

        private final String name;

        private FilterItem(final int id, final String name) {
            assert id >= MIN_FILTER_ID && id <= MAX_FILTER_ID;

            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return name;
        }
    }

    private enum FileType {
        IMAGE
    }

    private class FilePreview extends JGridPanel {
        private static final int SIZE = 200;

        private final JLabel lbType = new JLabel();

        private final JLabel lbSize = new JLabel();

        private final JLabel lbPreview = new JLabel(resourceManager.getString("FileChooserDemo.preview.emptytext"), JLabel.CENTER);

        private final Map<String, FileType> knownTypes = new HashMap<String, FileType>();

        public FilePreview() {
            super(1, 0, 1);

            for (String s : ImageIO.getWriterFormatNames()) {
                knownTypes.put(s.toLowerCase(), FileType.IMAGE);
            }

            initUI();
        }

        public void loadFileInfo(final File file) {
            boolean emptyPreview = true;

            if (file == null) {
                lbType.setText(null);
                lbSize.setText(null);
            } else {
                lbType.setText(externalChooser.getFileSystemView().getSystemTypeDescription(file));
                lbSize.setText(Long.toString(file.length()));

                String fileName = file.getName();

                int i = fileName.lastIndexOf(".");

                String ext = i < 0 ? null : fileName.substring(i + 1);

                FileType fileType = knownTypes.get(ext.toLowerCase());

                if (fileType != null) {
                    switch (fileType) {
                        case IMAGE:
                            try {
                                BufferedImage image = ImageIO.read(file);

                                double coeff = Math.min(((double) SIZE) / image.getWidth(),
                                        ((double) SIZE) / image.getHeight());

                                BufferedImage scaledImage = new BufferedImage(
                                        (int) Math.round(image.getWidth() * coeff),
                                        (int) Math.round(image.getHeight() * coeff),
                                        BufferedImage.TYPE_INT_RGB);

                                Graphics2D g = (Graphics2D) scaledImage.getGraphics();

                                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                g.drawImage(image, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);

                                lbPreview.setText(null);
                                lbPreview.setIcon(new ImageIcon(scaledImage));

                                setComponent(lbPreview, 0, 1);

                                emptyPreview = false;
                            } catch (IOException e) {
                                // Empty preview
                            }

                            break;
                    }
                }
            }

            if (emptyPreview) {
                lbPreview.setIcon(null);
                lbPreview.setText(resourceManager.getString("FileChooserDemo.preview.emptytext"));

                setComponent(lbPreview, 0, 1);
            }
        }

        private void initUI() {
            setPreferredSize(new Dimension(SIZE, -1));

            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

            JGridPanel pnInfo = new JGridPanel(2, 1);

            pnInfo.cell(new JLabel(resourceManager.getString("FileChooserDemo.preview.type"))).
                    cell(lbType).
                    cell(new JLabel(resourceManager.getString("FileChooserDemo.preview.size"))).
                    cell(lbSize);

            cell(pnInfo);
            cell(lbPreview, Layout.FILL, Layout.FILL);
        }
    }
}

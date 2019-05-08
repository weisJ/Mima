/*
 Copyright (c) 2008-2009, Piet Blok All rights reserved.
 <p>
 Redistribution and use in source and binary forms, with or without modification, are permitted
 provided that the following conditions are met:
 <p>
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 and the following disclaimer. * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the documentation and/or other
 materials provided with the distribution. * Neither the name of the copyright holder nor the
 names of the contributors may be used to endorse or promote products derived from this software
 without specific prior written permission.
 <p>
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package demo;

import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.NotNull;
import org.pbjar.jxlayer.plaf.ext.TransformUI;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;

/**
 * Demonstrates the use of transformations in a tool tip.
 *
 * <p>Run a web start demo: <a
 * href="http://www.pbjar.org/blogs/jxlayer/jxlayer40/ZoomedTooltipDemo.jnlp"><IMG style="CLEAR:
 * right" alt="Web Start zoomed tooltip demo" src="http://javadesktop
 * .org/javanet_images/webstart.small2.gif" align="middle" border="1" /> </a>
 */
public class ToolTipDemo extends JTextPane {

    private static final long serialVersionUID = 1L;

    {
        // Set the tool tip text to a non empty string.
        this.setToolTipText("Trigger");
    }

    public static void main(final String[] args) {
        TransformUI.prepareForJTextComponent();
        SwingUtilities.invokeLater(
                () -> {
                    JFrame frame = new JFrame("TabFrameDemo tool tip");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    JPanel content = new JPanel(new GridLayout(0, 3));
                    for (int index = 0; index < 9; index++) {
                        JTextPane textPane = new ToolTipDemo();
                        textPane.setBorder(
                                new CompoundBorder(
                                        new BevelBorder(BevelBorder.RAISED), new BevelBorder(BevelBorder.LOWERED)));
                        textPane.setContentType("text/html");
                        textPane.setText(
                                "<html><h1><font size='2'>TEST "
                                        + (index + 1)
                                        + "</font></h1><font size='1'>A tool tip text</font></html>");

                        content.add(textPane);
                    }
                    frame.add(content);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                });
    }

    @NotNull
    @Override
    public JToolTip createToolTip() {
        return new JToolTip() {

            private static final long serialVersionUID = 1L;

            {
                this.removeAll();
                this.setLayout(new BorderLayout());
                JTextPane tipPane = new JTextPane();
                tipPane.setContentType(ToolTipDemo.this.getContentType());
                tipPane.setDocument(ToolTipDemo.this.getDocument());
                JXLayer<?> layer = TransformUtils.createTransformJXLayer(tipPane, 4.0);
                this.add(layer, BorderLayout.CENTER);
                this.setPreferredSize(layer.getPreferredSize());

                this.setComponent(ToolTipDemo.this);
            }

            @Override
            protected void paintComponent(final Graphics g) {
                // do nothing;
            }
        };
    }
}

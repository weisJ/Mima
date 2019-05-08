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
import org.pbjar.jxlayer.plaf.misc.GeneralLayerUI;
import org.pbjar.jxlayer.plaf.misc.HideCursorUI;
import org.pbjar.jxlayer.plaf.misc.MagnifierUI;
import org.pbjar.jxlayer.plaf.misc.MouseDrawingUI;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

/**
 * TabFrameDemo the wrapping of JXLayer's into one another.
 *
 * <p>Run a web start demo: <a
 * href="http://www.pbjar.org/blogs/jxlayer/jxlayer40/WrappingDemo.jnlp"><IMG style="CLEAR: right"
 * alt="Web Start Wrapped JXLayer" src="http://javadesktop.org/javanet_images/webstart.small2.gif"
 * align="middle" border="1" /> </a>
 *
 * @author Piet Blok
 */
public class TestWrapped {

    /**
     * Run the progtam.
     *
     * @param args not used.
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    try {
                        new TestWrapped().test();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void addActions(@NotNull final JMenu menu, final String name, @NotNull final List<Action> actionList) {
        for (Action action : actionList) {
            if (action.getValue(Action.SELECTED_KEY) != null) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
                menu.add(item);
            } else {
                menu.add(action);
            }
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private JComponent createTarget(
            final JFrame frame,
            final String id,
            @NotNull final JMenu menubar,
            @NotNull final GeneralLayerUI<JComponent, ?>[] layerUIs) {
        JTextPane originalComponent =
                new JTextPane() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean getScrollableTracksViewportWidth() {
                        return true;
                    }
                };
        try {
            originalComponent.setPage(this.getClass().getResource("WrapTest.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        originalComponent.setEditable(false);

        JComponent wrappingTarget = originalComponent;

        for (GeneralLayerUI<JComponent, ?> layerUI : layerUIs) {
            wrappingTarget = new JXLayer<>(wrappingTarget, layerUI);
            JMenu menu = new JMenu(layerUI.getName());
            menubar.add(menu);
            addActions(menu, layerUI.getName(), layerUI.getActions());
            addActions(menu, layerUI.getName(), layerUI.getActions((JXLayer<JComponent>) wrappingTarget));
        }

        return new JScrollPane(wrappingTarget);
    }

    @SuppressWarnings("unchecked")
    private void test() throws IOException {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);

        GeneralLayerUI<JComponent, ?>[] layerUIs =
                new GeneralLayerUI[]{
                        new MouseDrawingUI(), new HideCursorUI(500), new MagnifierUI(),
                };

        JMenu menu = new JMenu("Options");
        menubar.add(menu);

        frame.add(createTarget(frame, "Target", menu, layerUIs));
        frame.setVisible(true);
    }
}

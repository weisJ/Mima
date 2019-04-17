import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.NotNull;
import org.pbjar.jxlayer.plaf.ext.transform.DefaultTransformModel;
import org.pbjar.jxlayer.plaf.ext.transform.TransformUtils;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;

public class JLayerTransform {

    public JLayerTransform() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (@NotNull ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignore) {
            }

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(new ExamplePane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new JLayerTransform();
    }

    public class ExamplePane extends JPanel {

        private JSlider slider;
        private DefaultTransformModel transformModel;

        public ExamplePane() {

            setLayout(new BorderLayout());

            slider = new JSlider(0, 360);
            slider.setValue(0);
            slider.addChangeListener(
                    e -> transformModel.setRotation(Math.toRadians(slider.getValue())));

            var fieldPane = new JTextField();

            transformModel = new DefaultTransformModel();
            transformModel.setRotationCenter(new Point2D.Double(0, 0));
            transformModel.setRotationCenterSupplier(
                    d -> new Point2D.Double(fieldPane.getX(), fieldPane.getY()));
            transformModel.setRotation(Math.toRadians(0));
            transformModel.setScaleToPreferredSize(true);
            JXLayer<JComponent> rotatePane =
                    TransformUtils.createTransformJXLayer(fieldPane, transformModel);

            add(slider, BorderLayout.NORTH);
            add(rotatePane);

        }
    }

    public class FieldPane extends JPanel {

        public FieldPane() {
            setLayout(new GridBagLayout());

            JTextField field = new JTextField(10);
            field.setText("Hello world");

            add(field);

        }
    }
}
/* ====================================================================
 * /dialog/AboutDialog.java
 * 
 * (c) by Dirk Lehmann
 * ====================================================================
 */
package de.lehmannet.om.ui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import de.lehmannet.om.ui.navigation.ObservationManager;
import de.lehmannet.om.ui.util.ConstraintsBuilder;
import de.lehmannet.om.ui.util.LocaleToolsFactory;

public class AboutDialog extends OMDialog implements ActionListener {

    private static final long serialVersionUID = 4875893088001020590L;

    private final ResourceBundle bundle = LocaleToolsFactory.appInstance().getBundle("ObservationManager",
            Locale.getDefault());

    private final JButton close = new JButton(this.bundle.getString("about.button.close"));

    public AboutDialog(ObservationManager om) {

        super(om);

        this.setTitle(this.bundle.getString("about.button.title"));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(om);

        this.initDialog();
        this.setSize(AboutDialog.serialVersionUID, 400, 260);

        this.setVisible(true);

    }

    // --------------
    // ActionListener ---------------------------------------------------------
    // --------------

    @Override
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();
        if (source instanceof JButton) {
            if (source.equals(this.close)) {
                this.dispose();
            }
        }

    }

    private void initDialog() {

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        this.getContentPane().setLayout(gridbag);

        ConstraintsBuilder.buildConstraints(constraints, 0, 0, 1, 5, 1, 99);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        JTextArea text = new JTextArea();
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setText(this.bundle.getString("about.dialog.text"));
        gridbag.setConstraints(text, constraints);
        this.getContentPane().add(text);

        ConstraintsBuilder.buildConstraints(constraints, 0, 5, 1, 1, 1, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.close.addActionListener(this);
        gridbag.setConstraints(this.close, constraints);
        this.getContentPane().add(this.close);

    }

}

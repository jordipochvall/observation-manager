/* ====================================================================
 * /extension/deepSky/panel/DeepSkyTargetCGPanel.java
 * 
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.extension.deepSky.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.lehmannet.om.IObserver;
import de.lehmannet.om.ISchemaElement;
import de.lehmannet.om.ITarget;
import de.lehmannet.om.extension.deepSky.DeepSkyTargetCG;
import de.lehmannet.om.model.ObservationManagerModel;
import de.lehmannet.om.ui.navigation.ObservationManager;
import de.lehmannet.om.ui.panel.AbstractPanel;
import de.lehmannet.om.ui.util.ConstraintsBuilder;
import de.lehmannet.om.ui.util.OMLabel;
import de.lehmannet.om.util.FloatUtil;

public class DeepSkyTargetCGPanel extends AbstractPanel {

    private static final long serialVersionUID = -1417896902967737514L;

    private final PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle
            .getBundle("de.lehmannet.om.ui.extension.deepSky.DeepSky", Locale.getDefault());

    private ObservationManager observationManager = null;
    private DeepSkyTargetCG target = null;

    private DeepSkyTargetContainer deepSkyTargetContainer = null;
    private JTextField magnitude10th = null;
    private final ObservationManagerModel model;

    public DeepSkyTargetCGPanel(ObservationManager om, ObservationManagerModel model,
    ITarget target, Boolean editable)
            throws IllegalArgumentException {

        super(editable);

        if ((target != null) && !(target instanceof DeepSkyTargetCG)) {
            throw new IllegalArgumentException(
                    "Passed ITarget must derive from de.lehmannet.om.extension.deepSky.DeepSkyTargetCG\n");
        }

        this.target = (DeepSkyTargetCG) target;
        this.observationManager = om;
        this.model = model;

        this.createPanel();

        if (this.target != null) {
            this.loadSchemaElement();
        }

    }

    @Override
    public ISchemaElement getSchemaElement() {

        return this.target;

    }

    @Override
    public ISchemaElement updateSchemaElement() {

        if (this.target == null) {
            return null;
        }

        ITarget t = this.deepSkyTargetContainer.updateTarget(this.target);
        if (t == null) {
            return null;
        } else {
            this.target = (DeepSkyTargetCG) t;
        }

        // Optional parameters
        String magnitude = this.magnitude10th.getText().trim();
        if (!"".equals(magnitude)) {
            float mag = FloatUtil.parseFloat(magnitude);
            this.target.setMagnitudeOf10thBrightestMember(mag);
        }

        return this.target;

    }

    @Override
    public ISchemaElement createSchemaElement() {

        String name = this.deepSkyTargetContainer.getName();
        String datasource = this.deepSkyTargetContainer.getDatasource();
        IObserver observer = this.deepSkyTargetContainer.getObserver();

        // Make sure only datasource or observer is set
        if (this.deepSkyTargetContainer.checkOrigin(datasource, observer)) {
            return null;
        }

        if (observer != null) {
            this.target = new DeepSkyTargetCG(name, observer);
        } else {
            this.target = new DeepSkyTargetCG(name, datasource);
        }

        // Set all other fields
        ITarget t = (ITarget) this.updateSchemaElement();
        if (t == null) {
            return null;
        } else {
            this.target = (DeepSkyTargetCG) t;
        }

        return this.target;

    }

    private void loadSchemaElement() {

        if (!Float.isNaN(this.target.getMagnitudeOf10thBrightestMember())) {
            this.magnitude10th.setText("" + this.target.getMagnitudeOf10thBrightestMember());
        }
        this.magnitude10th.setEditable(this.isEditable());

    }

    private void createPanel() {

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.setLayout(gridbag);

        ConstraintsBuilder.buildConstraints(constraints, 0, 0, 4, 1, 45, 1);
        this.deepSkyTargetContainer = new DeepSkyTargetContainer(this.observationManager, this.model, this.target,
                this.isEditable());
        gridbag.setConstraints(this.deepSkyTargetContainer, constraints);
        this.add(this.deepSkyTargetContainer);

        ConstraintsBuilder.buildConstraints(constraints, 0, 1, 4, 1, 100, 1);
        JSeparator seperator1 = new JSeparator(SwingConstants.HORIZONTAL);
        gridbag.setConstraints(seperator1, constraints);
        this.add(seperator1);

        ConstraintsBuilder.buildConstraints(constraints, 0, 2, 2, 1, 5, 1);
        OMLabel Lmag = new OMLabel(this.bundle.getString("panel.cg.label.magnitude10th"), false);
        Lmag.setToolTipText(this.bundle.getString("panel.cg.tooltip.magnitude10th"));
        gridbag.setConstraints(Lmag, constraints);
        this.add(Lmag);
        ConstraintsBuilder.buildConstraints(constraints, 2, 2, 2, 1, 45, 1);
        this.magnitude10th = new JTextField();
        this.magnitude10th.setToolTipText(this.bundle.getString("panel.cg.tooltip.magnitude10th"));
        this.magnitude10th.setEditable(this.isEditable());
        gridbag.setConstraints(this.magnitude10th, constraints);
        this.add(this.magnitude10th);

        ConstraintsBuilder.buildConstraints(constraints, 0, 3, 4, 1, 45, 95);
        constraints.fill = GridBagConstraints.BOTH;
        JLabel Lfill = new JLabel("");
        gridbag.setConstraints(Lfill, constraints);
        this.add(Lfill);

    }

}

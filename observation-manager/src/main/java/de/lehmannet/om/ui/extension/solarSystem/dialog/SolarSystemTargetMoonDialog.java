/* ====================================================================
 * /extension/solarSystem/dialog/SolarSystemTargetMoonDialog.java
 * 
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.extension.solarSystem.dialog;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import de.lehmannet.om.IObservation;
import de.lehmannet.om.ITarget;
import de.lehmannet.om.ui.dialog.AbstractDialog;
import de.lehmannet.om.ui.dialog.ITargetDialog;
import de.lehmannet.om.ui.extension.solarSystem.panel.SolarSystemTargetMoonPanel;
import de.lehmannet.om.ui.navigation.ObservationManager;

public class SolarSystemTargetMoonDialog extends AbstractDialog implements ITargetDialog {

    private static final long serialVersionUID = 11451630089774356L;

    public SolarSystemTargetMoonDialog(ObservationManager om, IObservation o, ITarget editableTarget) {

        super(om, new SolarSystemTargetMoonPanel(om, editableTarget, o, Boolean.TRUE));

        PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle
                .getBundle("de.lehmannet.om.ui.extension.solarSystem.SolarSystem", Locale.getDefault());
        if (editableTarget == null) {
            super.setTitle(bundle.getString("dialog.moon.title"));
        } else {
            super.setTitle(bundle.getString("dialog.moon.titleEdit") + " " + editableTarget.getDisplayName());
        }

        super.setSize(SolarSystemTargetMoonDialog.serialVersionUID, 550, 260);
        super.setVisible(true);

    }

    @Override
    public ITarget getTarget() {

        if (super.schemaElement != null) {
            return (ITarget) super.schemaElement;
        }

        return null;

    }

}

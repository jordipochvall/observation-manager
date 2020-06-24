/* ====================================================================
 * /extension/deepSky/dialog/DeepSkyTargetGCDialog.java
 * 
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.extension.deepSky.dialog;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JFrame;

import de.lehmannet.om.ITarget;
import de.lehmannet.om.model.ObservationManagerModel;
import de.lehmannet.om.ui.dialog.AbstractDialog;
import de.lehmannet.om.ui.dialog.ITargetDialog;
import de.lehmannet.om.ui.extension.deepSky.panel.DeepSkyTargetGCPanel;
import de.lehmannet.om.ui.util.UserInterfaceHelper;

public class DeepSkyTargetGCDialog extends AbstractDialog implements ITargetDialog {

    private static final long serialVersionUID = -7217690597304275686L;

    public DeepSkyTargetGCDialog(JFrame om, UserInterfaceHelper uiHelper, ObservationManagerModel model,
            ITarget editableTarget) {

        super(om, model, uiHelper, new DeepSkyTargetGCPanel(uiHelper, model, editableTarget, Boolean.TRUE));

        ResourceBundle bundle = ResourceBundle.getBundle("de.lehmannet.om.ui.extension.deepSky.DeepSky",
                Locale.getDefault());
        if (editableTarget == null) {
            this.setTitle(bundle.getString("dialog.gc.title"));
        } else {
            this.setTitle(bundle.getString("dialog.gc.titleEdit") + " " + editableTarget.getDisplayName());
        }

        this.setSize(DeepSkyTargetGCDialog.serialVersionUID, 575, 360);

        this.setVisible(true);

    }

    @Override
    public ITarget getTarget() {

        if (this.schemaElement != null) {
            return (ITarget) this.schemaElement;
        }

        return null;

    }

}

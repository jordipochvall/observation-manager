/* ====================================================================
 * /dialog/GenericTargetDialog.java
 * 
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.dialog;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import de.lehmannet.om.ITarget;
import de.lehmannet.om.ui.navigation.ObservationManager;
import de.lehmannet.om.ui.panel.GenericTargetPanel;

public class GenericTargetDialog extends AbstractDialog implements ITargetDialog {

    private static final long serialVersionUID = -8858493947135823299L;

    public GenericTargetDialog(ObservationManager om, ITarget editableTarget) {

        super(om, new GenericTargetPanel(om, editableTarget, Boolean.TRUE));

        PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle.getBundle("ObservationManager",
                Locale.getDefault());
        if (editableTarget == null) {
            this.setTitle(bundle.getString("dialog.genericTarget.title"));
        } else {
            this.setTitle(bundle.getString("dialog.genericTarget.titleEdit") + " " + editableTarget.getDisplayName());
        }

        this.setSize(GenericTargetDialog.serialVersionUID, 590, 260);

        this.pack();
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

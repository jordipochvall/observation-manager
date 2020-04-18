/* ====================================================================
 * /dialog/LensDialog.java
 * 
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.dialog;

import de.lehmannet.om.ILens;
import de.lehmannet.om.ui.navigation.ObservationManager;
import de.lehmannet.om.ui.panel.LensPanel;

public class LensDialog extends AbstractDialog {

    private static final long serialVersionUID = -8876607801825367237L;

    public LensDialog(ObservationManager om, ILens editableLens) {

        super(om, new LensPanel(editableLens, true));

        if (editableLens == null) {
            setTitle(AbstractDialog.bundle.getString("dialog.lens.title"));
        } else {
            if (editableLens.getFactor() >= 1) {
                setTitle(AbstractDialog.bundle.getString("dialog.lens.barlow.titleEdit") + " "
                        + editableLens.getDisplayName());
            } else {
                setTitle(AbstractDialog.bundle.getString("dialog.lens.shapley.titleEdit") + " "
                        + editableLens.getDisplayName());
            }
        }

       setSize(LensDialog.serialVersionUID, 710, 130);
        pack();
        setVisible(true);
        

    }

    public ILens getLens() {

        if (this.schemaElement != null) {
            return (ILens) this.schemaElement;
        }

        return null;

    }

}

package de.lehmannet.om.ui.navigation;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lehmannet.om.IEyepiece;
import de.lehmannet.om.IFilter;
import de.lehmannet.om.IImager;
import de.lehmannet.om.ILens;
import de.lehmannet.om.IObservation;
import de.lehmannet.om.IObserver;
import de.lehmannet.om.ISchemaElement;
import de.lehmannet.om.IScope;
import de.lehmannet.om.ISession;
import de.lehmannet.om.ISite;
import de.lehmannet.om.ITarget;
import de.lehmannet.om.OALException;
import de.lehmannet.om.ui.dialog.NewDocumentDialog;
import de.lehmannet.om.ui.dialog.ProgressDialog;
import de.lehmannet.om.ui.image.ImageResolver;
import de.lehmannet.om.ui.navigation.observation.utils.SystemInfo;
import de.lehmannet.om.ui.util.IConfiguration;
import de.lehmannet.om.ui.util.Worker;
import de.lehmannet.om.ui.util.XMLFileLoader;
import de.lehmannet.om.util.SchemaElementConstants;
import de.lehmannet.om.util.SchemaLoader;

public final class ObservationManagerMenuFile {

    private final Logger LOGGER = LoggerFactory.getLogger(ObservationManagerMenuFile.class);

    private final XMLFileLoader xmlCache;
    private final IConfiguration configuration;
    private final ObservationManager observationManager;
    private final ObservationManagerHtmlHelper htmlHelper;
    private final ImageResolver imageResolver;
    private final JMenu menu;

    public ObservationManagerMenuFile(IConfiguration configuration, XMLFileLoader xmlCache, ObservationManager om,
            ObservationManagerHtmlHelper htmlHelper, ImageResolver imageResolver) {

        // Load configuration
        this.configuration = configuration;
        this.xmlCache = xmlCache;
        this.observationManager = om;
        this.htmlHelper = htmlHelper;
        this.imageResolver = imageResolver;

        this.menu = this.createMenuFileItems();
    }

    public JMenu getMenu() {
        return this.menu;
    }

    private boolean hasModelChanged() {
        return this.observationManager.isChanged();
    }

    private JMenu createMenuFileItems() {

        final int menuKeyModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        // ----- File Menu
        final JMenu fileMenu = new JMenu(ObservationManager.bundle.getString("menu.file"));
        fileMenu.setMnemonic('f');

        JMenuItem newFile = new JMenuItem(ObservationManager.bundle.getString("menu.newFile"),
                new ImageIcon(this.imageResolver.getImageURL("newDocument.png").orElse(null), ""));
        newFile.setMnemonic('n');
        newFile.addActionListener(new NewFileListener());
        fileMenu.add(newFile);

        JMenuItem openFile = new JMenuItem(ObservationManager.bundle.getString("menu.openFile"),
                new ImageIcon(this.imageResolver.getImageURL("open.png").orElse(null), ""));
        openFile.setMnemonic('o');
        openFile.addActionListener(new OpenFileListener());
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuKeyModifier));
        fileMenu.add(openFile);

        // this.openDir = new JMenuItem("Open dir");
        // this.openDir.setMnemonic('d');
        // this.openDir.addActionListener(this);
        // this.fileMenu.add(openDir); // @todo: Uncomment this as soon as we
        // know what this means

        JMenuItem saveFile = new JMenuItem(ObservationManager.bundle.getString("menu.save"),
                new ImageIcon(this.imageResolver.getImageURL("save.png").orElse(null), ""));
        saveFile.setMnemonic('s');
        saveFile.addActionListener(new SaveFileListener());
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuKeyModifier));
        fileMenu.add(saveFile);

        JMenuItem saveFileAs = new JMenuItem(ObservationManager.bundle.getString("menu.saveAs"),
                new ImageIcon(this.imageResolver.getImageURL("save.png").orElse(null), ""));
        saveFileAs.setMnemonic('a');
        saveFileAs.addActionListener(new SaveAsListener());
        fileMenu.add(saveFileAs);

        fileMenu.addSeparator();

        JMenuItem importXML = new JMenuItem(ObservationManager.bundle.getString("menu.xmlImport"),
                new ImageIcon(this.imageResolver.getImageURL("importXML.png").orElse(null), ""));
        importXML.setMnemonic('i');
        importXML.addActionListener(new ImportXmlListener());
        fileMenu.add(importXML);

        fileMenu.addSeparator();

        JMenuItem exportHTML = new JMenuItem(ObservationManager.bundle.getString("menu.htmlExport"),
                new ImageIcon(this.imageResolver.getImageURL("export.png").orElse(null), ""));
        exportHTML.setMnemonic('e');
        exportHTML.addActionListener(new ExportHtmlListener());
        exportHTML.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuKeyModifier));
        fileMenu.add(exportHTML);

        fileMenu.addSeparator();

        JMenuItem exit = new JMenuItem(ObservationManager.bundle.getString("menu.exit"),
                new ImageIcon(this.imageResolver.getImageURL("exit.png").orElse(null), ""));
        exit.setMnemonic('x');
        exit.addActionListener(new ExitListener());
        fileMenu.add(exit);
        return fileMenu;
    }

    class NewFileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            ObservationManagerMenuFile.this.newFile(ObservationManagerMenuFile.this.hasModelChanged());

        }

    }

    class OpenFileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {            
            ObservationManagerMenuFile.this.openFile(ObservationManagerMenuFile.this.hasModelChanged());
        }

    }

    class SaveFileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ObservationManagerMenuFile.this.saveFile();

        }

    }

    class SaveAsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
             ObservationManagerMenuFile.this.saveFileAs(ObservationManagerMenuFile.this.hasModelChanged());

        }

    }

    class ImportXmlListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {            
            ObservationManagerMenuFile.this.importXML(ObservationManagerMenuFile.this.hasModelChanged());

        }

    }

    class ExportHtmlListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ObservationManagerMenuFile.this.createHTML();

        }

    }

    class ExitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {            
            ObservationManagerMenuFile.this.exit(ObservationManagerMenuFile.this.hasModelChanged());

        }

    }




    public int saveBeforeExit( boolean changed) {

        // Returns:
        // -1 = save failed
        // 0 = save ok
        // 1 = no save wanted
        // 2 = cancel pressed
        // 3 = no save required at all

        // Show question dialog, whether we should save
        if (changed) {
            JOptionPane pane = new JOptionPane(ObservationManager.bundle.getString("info.saveBeforeExit.question"),
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
            JDialog dialog = pane.createDialog(observationManager,
                    ObservationManager.bundle.getString("info.saveBeforeExit.title"));
            dialog.setVisible(true);
            Object selectedValue = pane.getValue();
            if ((selectedValue instanceof Integer)) {
                if ((Integer) selectedValue == JOptionPane.YES_OPTION) {
                    boolean result = this.saveFile(); // Try to save
                    if (!result) {
                        return -1; // save failed
                    }
                    return 0;
                } else if ((Integer) selectedValue == JOptionPane.CANCEL_OPTION) {
                    return 2;
                }
            }
            return 1;
        }

        return 3;

    }

    public boolean exit( boolean changed) {

        // Save before exit...
        switch (this.saveBeforeExit(changed)) {
            case -1:
                // 0 = Save was ok...continue
                // 1 = No save wanted...continue
            case 2: {
                return false; // Save failed (message was provided). Stop here.
            } // Cancel was pressed
              // 3 = No save required...continue
        }

        // Write into log that we start now
        LOGGER.info("--- Observation Manager shutting down...");

        LOGGER.debug("Exit: {}", new Date());
        // LOGGER.debug(this.printMemoryUsage());

        // Save window size and position and maximized state
        if (observationManager.getExtendedState() == Frame.MAXIMIZED_BOTH) {
            this.configuration.setConfig(ObservationManager.CONFIG_MAINWINDOW_MAXIMIZED, Boolean.toString(true));
        } else {
            this.configuration.setConfig(ObservationManager.CONFIG_MAINWINDOW_MAXIMIZED, null); // Remove
                                                                                                // maximzed
        }
        Dimension size = observationManager.getSize();
        String stringSize = size.width + "x" + size.height;
        Point location = observationManager.getLocation();
        // SwingUtilities.convertPointToScreen(location, this);
        String stringLocation = location.x + "," + location.y;
        this.configuration.setConfig(ObservationManager.CONFIG_MAINWINDOW_SIZE, stringSize);
        this.configuration.setConfig(ObservationManager.CONFIG_MAINWINDOW_POS, stringLocation);

        // Save horizontal and vertical dividers position
        float vertical = (float) observationManager.getWidth() / (float) observationManager.getVerticalSplitPane().getDividerLocation();
        float horizontal = (float) observationManager.getHeight() / (float) observationManager.getHorizontalSplitPane().getDividerLocation();
        this.configuration.setConfig(ObservationManager.CONFIG_MAINWINDOW_DIVIDER_HORIZONTAL, "" + horizontal);
        this.configuration.setConfig(ObservationManager.CONFIG_MAINWINDOW_DIVIDER_VERTICAL, "" + vertical);

        // Save column settings to persistance
        observationManager.getTableView().saveSettings();

        // Try to save config...

        boolean result = this.configuration.saveConfiguration();
        if (!result) {
            this.createWarning(ObservationManager.bundle.getString("error.saveconfig"));
        }

        System.exit(0);

        // Will never be reached, but is required, as we need to return
        // something
        return true;

    }

    public void createWarning(String message) {

        JOptionPane.showMessageDialog(observationManager, message, ObservationManager.bundle.getString("title.warning"),
                JOptionPane.WARNING_MESSAGE);

    }

    public void saveFileAs( boolean changed) {

        if (this.xmlCache.isEmpty()) {
            this.createWarning(ObservationManager.bundle.getString("error.saveEmpty"));
            return;
        }

        String oldPath = null;

        String[] files = this.xmlCache.getAllOpenedFiles();
        if ((files != null) && (files.length == 1)) { // @todo This works only
                                                      // with ONE file open
            oldPath = files[0];
        }

        final File f = this.saveDialog();

        
        if (f != null) {
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            observationManager.setCursor(hourglassCursor);

            Worker calculation;

            if (oldPath == null) {

                calculation = new Worker() {

                    private String message = null;
                    private byte returnValue = Worker.RETURN_TYPE_OK;

                    @Override
                    public void run() {

                        boolean result = ObservationManagerMenuFile.this.xmlCache.save(f.getAbsolutePath());
                        if (!result) {
                            message = ObservationManager.bundle.getString("error.save");
                            returnValue = Worker.RETURN_TYPE_ERROR;
                        }

                    }

                    @Override
                    public String getReturnMessage() {

                        return message;

                    }

                    @Override
                    public byte getReturnType() {

                        return returnValue;

                    }

                };

            } else {

                final String op = oldPath;

                calculation = new Worker() {

                    private String message = null;
                    private byte returnValue = Worker.RETURN_TYPE_OK;

                    @Override
                    public void run() {

                        boolean result = ObservationManagerMenuFile.this.xmlCache.saveAs(op, f.getAbsolutePath());
                        if (!result) {
                            message = ObservationManager.bundle.getString("error.save");
                            returnValue = Worker.RETURN_TYPE_ERROR;
                        }

                    }

                    @Override
                    public String getReturnMessage() {

                        return message;

                    }

                    @Override
                    public byte getReturnType() {

                        return returnValue;

                    }

                };

            }

            new ProgressDialog(observationManager, ObservationManager.bundle.getString("progress.wait.title"),
                    ObservationManager.bundle.getString("progress.wait.xml.save.info"), calculation);

            if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
                if (calculation.getReturnMessage() != null) {
                    this.createInfo(calculation.getReturnMessage());
                }
               
            } else {
                this.createWarning(calculation.getReturnMessage());
            
            }

            // Update Tree
            observationManager.getTreeView().updateTree();

            // Unset changed
            this.observationManager.setChanged(false);

            Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            observationManager.setCursor(defaultCursor);
        }

    }

    private File saveDialog() {

        JFileChooser chooser = new JFileChooser();

        String last = this.configuration.getConfig(ObservationManager.CONFIG_LASTDIR);
        if ((last != null) && !("".equals(last.trim()))) {
            File dir = new File(last);
            if (dir.exists()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int returnValue = chooser.showSaveDialog(this.observationManager);
        File file = null;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }
        if ((file != null) && (!file.getName().toLowerCase().endsWith(".xml"))) {
            file = new File(file.getAbsolutePath() + ".xml");
        }

        return file;

    }

    public boolean saveFile() {

        if (this.xmlCache.isEmpty()) {
            this.createWarning(ObservationManager.bundle.getString("error.saveEmpty"));
            return false;
        }

        final String[] files = this.xmlCache.getAllOpenedFiles();
        boolean result = false;
        if ((files == null) // No filename known yet...
                || (files.length == 0)) {
            final File f = this.saveDialog();

            if (f != null) {
                Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                this.observationManager.setCursor(hourglassCursor);

                Worker calculation = new Worker() {

                    private String message = null;
                    private byte returnValue = Worker.RETURN_TYPE_OK;

                    @Override
                    public void run() {

                        boolean result = ObservationManagerMenuFile.this.xmlCache.save(f.getAbsolutePath());
                        if (!result) {
                            message = ObservationManager.bundle.getString("error.save");
                            returnValue = Worker.RETURN_TYPE_ERROR;
                        }

                    }

                    @Override
                    public String getReturnMessage() {

                        return message;

                    }

                    @Override
                    public byte getReturnType() {

                        return returnValue;

                    }

                };

                new ProgressDialog(this.observationManager, ObservationManager.bundle.getString("progress.wait.title"),
                        ObservationManager.bundle.getString("progress.wait.xml.save.info"), calculation);

                if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
                    if (calculation.getReturnMessage() != null) {
                        this.createInfo(calculation.getReturnMessage());
                    }
                    result = true;
                } else {
                    this.createWarning(calculation.getReturnMessage());
                    result = false;
                }

                // Update Tree
                this.observationManager.getTreeView().updateTree();

                // Unset changed
                this.observationManager.setChanged(false);

                Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                this.observationManager.setCursor(defaultCursor);

            }

            return result;
        }

        // Filename already known...just save

        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        this.observationManager.setCursor(hourglassCursor);

        if (LOGGER.isDebugEnabled()) {
            System.out.println("Save file: " + new Date());
            System.out.println(SystemInfo.printMemoryUsage());
        }

        // @todo This works only with ONE file opened
        Worker calculation = new Worker() {

            private String message = null;
            private byte returnValue = Worker.RETURN_TYPE_OK;

            @Override
            public void run() {

                boolean result = ObservationManagerMenuFile.this.xmlCache.save(files[0]);
                if (!result) {
                    message = ObservationManager.bundle.getString("error.save");
                    returnValue = Worker.RETURN_TYPE_ERROR;
                }

            }

            @Override
            public String getReturnMessage() {

                return message;

            }

            @Override
            public byte getReturnType() {

                return returnValue;

            }

        };

        new ProgressDialog(this.observationManager, ObservationManager.bundle.getString("progress.wait.title"),
                ObservationManager.bundle.getString("progress.wait.xml.save.info"), calculation);

        if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
            if (calculation.getReturnMessage() != null) {
                this.createInfo(calculation.getReturnMessage());
            }
            result = true;
        } else {
            this.createWarning(calculation.getReturnMessage());
            result = false;
        }

        if (LOGGER.isDebugEnabled()) {
            System.out.println("Saved: " + new Date());
            System.out.println(SystemInfo.printMemoryUsage());
        }

        // Update Tree
        this.observationManager.getTreeView().updateTree();

        // Unset changed
        this.observationManager.setChanged(false);

        Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        this.observationManager.setCursor(defaultCursor);

        return result;

    }

    public void newFile( boolean changed) {

        // Save before exit...
        switch (this.saveBeforeExit(changed)) {
            case -1:
                // 1 = No save wanted...continue
            case 2: {
                return; // Save failed (message was provided)
            }
            case 0: {
                // 0 = Save was ok...continue, but create message before
                this.createInfo(ObservationManager.bundle.getString("ok.save"));
                break;
            } // Cancel was pressed
              // 3 = No save required...continue
        }

        // Create dialog
        NewDocumentDialog newDialog = new NewDocumentDialog(observationManager, imageResolver);

        // If user selected Cancel
        int result = newDialog.getResult();
        if (result == NewDocumentDialog.CANCEL) {
            return;
        }

        // Get all selected schema elements
        IImager[] imagers = (IImager[]) newDialog.getSchemaElements(SchemaElementConstants.IMAGER);
        IEyepiece[] eyepieces = (IEyepiece[]) newDialog.getSchemaElements(SchemaElementConstants.EYEPIECE);
        IFilter[] filters = (IFilter[]) newDialog.getSchemaElements(SchemaElementConstants.FILTER);
        ILens[] lenses = (ILens[]) newDialog.getSchemaElements(SchemaElementConstants.LENS);
        IObservation[] observations = (IObservation[]) newDialog.getSchemaElements(SchemaElementConstants.OBSERVATION);
        IObserver[] observers = (IObserver[]) newDialog.getSchemaElements(SchemaElementConstants.OBSERVER);
        IScope[] scopes = (IScope[]) newDialog.getSchemaElements(SchemaElementConstants.SCOPE);
        ISession[] sessions = (ISession[]) newDialog.getSchemaElements(SchemaElementConstants.SESSION);
        ISite[] sites = (ISite[]) newDialog.getSchemaElements(SchemaElementConstants.SITE);
        ITarget[] targets = (ITarget[]) newDialog.getSchemaElements(SchemaElementConstants.TARGET);

        // Clear XML cache, uiDataCache, tree
        this.cleanUp();

        // Add schema elements to (empty) cache
        if (imagers != null) {
            for (IImager imager : imagers) {
                this.xmlCache.addSchemaElement(imager);
            }
        }

        if (eyepieces != null) {
            for (IEyepiece eyepiece : eyepieces) {
                this.xmlCache.addSchemaElement(eyepiece);
            }
        }

        if (filters != null) {
            for (IFilter filter : filters) {
                this.xmlCache.addSchemaElement(filter);
            }
        }

        if (lenses != null) {
            for (ILens lens : lenses) {
                this.xmlCache.addSchemaElement(lens);
            }
        }

        if (observers != null) {
            for (IObserver observer : observers) {
                this.xmlCache.addSchemaElement(observer);
            }
        }

        if (scopes != null) {
            for (IScope scope : scopes) {
                this.xmlCache.addSchemaElement(scope);
            }
        }

        if (sites != null) {
            for (ISite site : sites) {
                this.xmlCache.addSchemaElement(site);
            }
        }

        if (sessions != null) {
            for (ISession session : sessions) {
                this.xmlCache.addSchemaElement(session);
            }
        }

        if (targets != null) {
            for (ITarget target : targets) {
                this.xmlCache.addSchemaElement(target);
            }
        }

        // !!! This must be the last entry to add, in order to find the
        // observations
        // under the other schemaElements in the TreeView !!!
        if (observations != null) {
            for (IObservation observation : observations) {
                this.xmlCache.addSchemaElement(observation);
            }
        }

        // Update views
        observationManager.getTableView().showObservations(null, null);
        observationManager.getTreeView().updateTree();

        // Set content changed is elements were copied.
        // (Force save on a blank document doesn't make sense)
        if (result == NewDocumentDialog.OK_COPY) {
            this.observationManager.setChanged(true);
        }

    }

    public void openFile( boolean changed) {

        // Save before exit...
        switch (this.saveBeforeExit(changed)) {
            case -1:
                // 1 = No save wanted...continue
            case 2: {
                return; // Save failed (message was provided)
            }
            case 0: {
                // 0 = Save was ok...continue, but create message before
                this.createInfo(ObservationManager.bundle.getString("ok.save"));
                break;
            } // Cancel was pressed
              // 3 = No save required...continue
        }

        JFileChooser chooser = new JFileChooser();
        FileFilter xmlFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().endsWith(".xml")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return "OAL Files";
            }
        };
        chooser.setFileFilter(xmlFileFilter);
        String last = this.configuration.getConfig(ObservationManager.CONFIG_LASTDIR);
        if ((last != null) && !("".equals(last.trim()))) {
            File dir = new File(last);
            if (dir.exists()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(observationManager);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();

            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            observationManager.setCursor(hourglassCursor);

            this.loadFiles(files);

            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            observationManager.setCursor(normalCursor);
        }

        // Make sure change flag is unset
        this.observationManager.setChanged(false);

    }

    public void openDir() {

        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String last = this.configuration.getConfig(ObservationManager.CONFIG_LASTDIR);
        if ((last != null) && !("".equals(last.trim()))) {
            File dir = new File(last);
            if (dir.exists()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        int returnVal = chooser.showOpenDialog(this.observationManager);
        FilenameFilter xml = (dir, name) -> name.endsWith(".xml");
        File[] files = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] dirs = chooser.getSelectedFiles();

            for (File dir : dirs) {
                files = dir.listFiles(xml);
            }

            this.loadFiles(files);
        }

    }

    public void createInfo(String message) {

        JOptionPane.showMessageDialog(observationManager, message, ObservationManager.bundle.getString("title.info"),
                JOptionPane.INFORMATION_MESSAGE);

    }

    private void cleanUp() {

        this.xmlCache.clear();
        this.observationManager.getTreeView().updateTree();

    }

    private void loadFiles( File[] files) {

        if ((files == null) || (files.length == 0)) {
            return;
        }

        for (File file : files) {
            this.loadFile(file.getAbsolutePath());
        }

        this.configuration.setConfig(ObservationManager.CONFIG_LASTDIR, files[0].getParent());
        this.configuration.setConfig(ObservationManager.CONFIG_LASTXML, files[files.length - 1].getAbsolutePath());

        observationManager.getHorizontalSplitPane().updateUI();
        observationManager.getVerticalSplitPane().updateUI();

    }

    private void loadFile( final String file) {

        if (file == null) {
            return;
        }

        this.cleanUp();

        if (LOGGER.isDebugEnabled()) {
            System.out.println("Load File: " + new Date());
            System.out.println(SystemInfo.printMemoryUsage());
        }

        Worker calculation = new Worker() {

            private String message = null;
            private byte returnValue = Worker.RETURN_TYPE_OK;

            @Override
            public void run() {

                boolean result = ObservationManagerMenuFile.this.xmlCache.loadObservations(file);
                if (!result) {
                    message = ObservationManager.bundle.getString("error.loadXML") + " " + file;
                    returnValue = Worker.RETURN_TYPE_ERROR;
                }

                observationManager.getTableView().showObservations(null, null);
                observationManager.getTreeView().updateTree();

            }

            @Override
            public String getReturnMessage() {

                return message;

            }

            @Override
            public byte getReturnType() {

                return returnValue;

            }

        };

        // This should avoid some nasty ArrayIndexOutOfBoundsExceptions which
        // are
        // thrown time by time at the ProgressDialog.setVisible(true) call.
        // Problems seems that the DefaultTableModelRenderer tries to update a
        // certain
        // part of the screen while the ProgressDialogs calculation thread is
        // currently
        // loading the XML file. This seems to cause the problem. Clearing the
        // table like
        // below, seems to fix this strange problem
        observationManager.getTableView().showObservations(null, null);

        new ProgressDialog(observationManager, ObservationManager.bundle.getString("progress.wait.title"),
                ObservationManager.bundle.getString("progress.wait.xml.load.info"), calculation);

        if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
            if (calculation.getReturnMessage() != null) {
                this.createInfo(calculation.getReturnMessage());
            }
        } else {
            this.createWarning(calculation.getReturnMessage());
        }

        if (LOGGER.isDebugEnabled()) {
            System.out.println("Loaded: " + new Date());
            System.out.println(SystemInfo.printMemoryUsage());
        }

    }
    

    public void importXML(boolean changed) {

        // Let user select the XML file
        JFileChooser chooser = new JFileChooser();
        FileFilter xmlFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().endsWith(".xml")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return "OAL Files";
            }
        };
        chooser.setFileFilter(xmlFileFilter);
        String last = this.configuration.getConfig(ObservationManager.CONFIG_LASTDIR);
        if ((last != null) && !("".equals(last.trim()))) {
            File dir = new File(last);
            if (dir.exists()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(this.observationManager);
        if (returnVal != JFileChooser.APPROVE_OPTION) { // User canceled import
            return;
        }
        File file = chooser.getSelectedFile();

        // Check if a file was selected
        if (file == null) {
            return;
        }

        // Create and start the worker thread to do the actual import
        class ImportWorker implements Worker {

            private File importFile = null;
            private File schemaFile = null;
            private ObservationManager om = null;

            private String message = null;
            private byte returnValue = Worker.RETURN_TYPE_OK;

            ImportWorker(File importFile, ObservationManager om) {

                this.importFile = importFile;
                this.schemaFile = new File(om.getInstallDir().getPathForFile("schema"));
                this.om = om;

            }

            @Override
            public void run() {

                // Load import file
                SchemaLoader importer = new SchemaLoader();
                try {
                    importer.load(importFile, schemaFile);
                } catch (OALException se) {
                    returnValue = Worker.RETURN_TYPE_ERROR;
                    message = ObservationManager.bundle.getString("error.import.xmlFile");
                    return;
                }

                // Get imported elements (without observations)
                List<ISchemaElement> importedElements = new ArrayList<>();
                importedElements.addAll(Arrays.asList(importer.getEyepieces()));
                importedElements.addAll(Arrays.asList(importer.getFilters()));
                importedElements.addAll(Arrays.asList(importer.getImagers()));
                importedElements.addAll(Arrays.asList(importer.getObservers()));
                importedElements.addAll(Arrays.asList(importer.getScopes()));
                importedElements.addAll(Arrays.asList(importer.getSessions()));
                importedElements.addAll(Arrays.asList(importer.getSites()));
                importedElements.addAll(Arrays.asList(importer.getTargets()));
                importedElements.addAll(Arrays.asList(importer.getLenses()));

                // Add imported elements to current file
                for (Object importedElement : importedElements) {
                    this.om.getXmlCache().addSchemaElement((ISchemaElement) importedElement);
                }

                // Finally add observations
                // If we add the observations before all other elements are
                // known to the XMLLoader
                // the references are not set correctly, as only adding
                // observations, checks dependencies
                // and adds references in the XMLLoader.
                IObservation[] obs = importer.getObservations();
                if ((obs != null) && (obs.length > 0)) {
                    for (IObservation ob : obs) {
                        this.om.getXmlCache().addSchemaElement(ob);
                    }
                }

                // Refresh UI
                this.om.updateLeft(); // Refreshes tree (without that, the new
                                      // element won't appear on UI)

                // Set success message
                message = ObservationManager.bundle.getString("ok.import.xmlFile");

            }

            @Override
            public String getReturnMessage() {

                return message;

            }

            @Override
            public byte getReturnType() {

                return returnValue;

            }

        }

    
        ImportWorker calculation = new ImportWorker(file, this.observationManager);

        // Change cursor, as import thread is about to start
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        this.observationManager.setCursor(hourglassCursor);

        new ProgressDialog(this.observationManager, ObservationManager.bundle.getString("progress.wait.title"),
                ObservationManager.bundle.getString("progress.wait.xml.load.info"), calculation);

        // Change cursor back
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        this.observationManager.setCursor(normalCursor);

        if (calculation.getReturnType() == Worker.RETURN_TYPE_OK) {
            if (calculation.getReturnMessage() != null) {
                this.createInfo(calculation.getReturnMessage());
            }

            // Make sure change flag is set
            this.observationManager.setChanged(true);
        } else {
            this.createWarning(calculation.getReturnMessage());
        }

    }


    public void createHTML() {

        this.htmlHelper.createHTML(null, null, null);

    }

    

    
}
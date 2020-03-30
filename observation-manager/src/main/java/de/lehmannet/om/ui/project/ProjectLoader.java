/* ====================================================================
 * /project/CatalogLoader.java
 *
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.project;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JProgressBar;

import de.lehmannet.om.GenericTarget;
import de.lehmannet.om.ISchemaElement;
import de.lehmannet.om.ITarget;
import de.lehmannet.om.ui.catalog.CatalogLoader;
import de.lehmannet.om.ui.dialog.OMDialog;
import de.lehmannet.om.ui.navigation.ObservationManager;
import de.lehmannet.om.ui.panel.AbstractSearchPanel;

public class ProjectLoader {

    final PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle.getBundle("ObservationManager",
            Locale.getDefault());

    private static final String PROJECTS_DIR = "projects";

    private ObservationManager observationManager = null;

    private final List projectList = new ArrayList();

    // Used to load projects in parallel
    private final ThreadGroup loadProjects = new ThreadGroup("Load all projects");

    public ProjectLoader(ObservationManager om) {

        this.observationManager = om;

        this.loadProjects();

    }

    public ProjectCatalog[] getProjects() {

        this.waitForProjectsLoaders();

        return (ProjectCatalog[]) projectList.toArray(new ProjectCatalog[] {});

    }

    private void waitForProjectsLoaders() {

        // Must make sure all project loader threads have finished their work
        if (this.loadProjects.activeCount() > 0) {
            new WaitPopup(this.loadProjects, this.observationManager);
        }

    }

    private void loadProjects() {

        File path = new File(this.observationManager.getInstallDir().getPathForFolder(ProjectLoader.PROJECTS_DIR));
        if (!path.exists()) {
            return;
        }

        // Get all project files
        String[] projects = path.list((dir, name) -> {

            File file = new File(dir.getAbsolutePath() + File.separator + name);
            return file.getName().endsWith(".omp") && !"CVS".equals(file.getName()); // For developers ;-)

        });

        // No project files found
        if ((projects == null) || (projects.length == 0)) {
            return;
        }

        // Load all targets, created by observer
        List userTargets = this.loadUserTargets();

        // Create a thread for all projects, where the projects will be loaded in.
        // As projects are loaded during startup and loading of projects can take some
        // time,
        // this should increase startup times
        // It must be ensured that catalogs are loaded completely before the projects,
        // as projects
        // refer to catalogs
        ArrayList projectThreads = new ArrayList();
        File projectFile = null;
        for (String project : projects) {
            projectFile = new File(path.getAbsolutePath() + File.separator + project);
            ProjectLoaderRunnable runnable = new ProjectLoaderRunnable(this.observationManager, this.projectList,
                    userTargets, projectFile, this.observationManager.isDebug());
            Thread thread = new Thread(this.loadProjects, runnable, "Load project " + project);
            projectThreads.add(thread);
        }

        // Start loading all projects
        for (Object projectThread : projectThreads) {
            ((Thread) projectThread).start();
        }

    }

    private List loadUserTargets() {

        ArrayList userTargets = new ArrayList();

        ITarget[] targets = this.observationManager.getXmlCache().getTargets();
        for (ITarget target : targets) {
            if (target.getObserver() != null) {
                userTargets.add(target);
            }
        }

        return userTargets;

    }

}

class ProjectLoaderRunnable implements Runnable {

    private List projectList = null;
    private File projectFile = null;
    private ObservationManager om = null;
    private List userTargets = null;
    private boolean debug = false;

    public ProjectLoaderRunnable(ObservationManager om, List projectList, List userTargets, File projectFile,
            boolean debug) {

        this.om = om;
        this.projectList = projectList;
        this.projectFile = projectFile;
        this.userTargets = userTargets;
        this.debug = debug;

    }

    @Override
    public void run() {

        if (debug) {
            System.out
                    .println("Project loading start: " + this.projectFile.getName() + " " + System.currentTimeMillis());
        }

        ProjectCatalog pc = this.loadProjectCatalog(this.projectFile);

        // Project is loaded, so add it to map
        if (pc != null) {
            synchronized (this.projectList) { // Make sure access to map is synchronized
                this.projectList.add(pc);
            }
        }

        if (debug) {
            System.out
                    .println("Project loading done: " + this.projectFile.getName() + " " + System.currentTimeMillis());
        }

    }

    private ITarget searchForTarget(String line) {

        final String USER_KEY = "USER";

        // Extract catalogName and targetName
        String catalogName = null;
        if (line.contains(",")) { // Check if catalog name was given at all
            catalogName = line.substring(0, line.indexOf(","));
        }
        String targetName = line.substring(line.indexOf(",") + 1);

        // Check whether line was formated correctly
        if ("".equals(targetName.trim())) {
            return null;
        }

        // Handle user/observer objects (objects not from a catalog)
        if ((catalogName != null) && (USER_KEY.equals(catalogName.toUpperCase()))) {
            String ut_name = null;
            String t_name = this.formatName(targetName);

            if (!this.userTargets.isEmpty()) { // There are userTargets at all
                ListIterator iterator = this.userTargets.listIterator();
                ITarget current = null;
                while (iterator.hasNext()) {
                    current = (ITarget) iterator.next();

                    ut_name = this.formatName(current.getName());
                    if (ut_name.equals(t_name)) { // Target names match
                        return current;
                    } else { // Try whether alias names match
                        if ((current.getAliasNames() != null) && (current.getAliasNames().length > 0)) {
                            String[] aNames = current.getAliasNames();
                            for (String aName : aNames) {
                                ut_name = this.formatName(aName);
                                if (ut_name.equals(t_name)) { // Alias name matches
                                    return current;
                                }
                            }
                            return null; // No need to access catalogs directly or via search as this is a user target
                        } else {
                            return null; // No need to access catalogs directly or via search as this is a user target
                        }
                    }
                }
                return null; // No need to access catalogs directly or via search as this is a user target
            } else {
                return null; // No need to access catalogs directly or via search as this is a user target
            }
        }

        // Handle catalog targets
        CatalogLoader catalogLoader = om.getExtensionLoader().getCatalogLoader();

        // Access target directly (works only if we've a catalog name)
        ISchemaElement target = null;
        if (catalogName != null) {
            target = catalogLoader.getTarget(catalogName, this.formatName(targetName));
        }

        if (target != null) { // Found target via the direct access
            return (ITarget) target;
        }

        // Target cannot be accessed...try to search for it (in all catalogs)

        // Get all catalog names
        String[] catalogNames = catalogLoader.getCatalogNames();
        for (String name : catalogNames) {
            // Search via search panel as searching might be optimized
            // and it'll include alias names
            AbstractSearchPanel searchPanel = catalogLoader.getCatalog(name).getSearchPanel();
            if (searchPanel != null) {
                searchPanel.search(targetName);
                target = searchPanel.getSearchResult();
            }

            if (target != null) {
                return (ITarget) target; // We found something!
            }
        }

        return null; // Given targetName couldn't be found in any catalog

    }

    private String formatName(String name) {

        name = name.trim();
        name = name.replaceAll(" ", "");
        name = name.toUpperCase();

        return name;

    }

    private ProjectCatalog loadProjectCatalog(File projectFile) {

        final String PROJECT_NAME_KEY = "ProjectName";

        if (!projectFile.exists()) {
            return null;
        }

        // Try to read file line by line
        ArrayList targets = new ArrayList();
        ITarget target = null;
        String name = null;
        try {
            FileInputStream fis = new FileInputStream(projectFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) { // Skip comment lines
                    continue;
                }
                if (line.startsWith(PROJECT_NAME_KEY)) { // We've found the project name
                    name = line.substring(line.indexOf("=") + 1);
                    continue;
                }

                // Search for target in all "real" catalogs
                target = this.searchForTarget(line);

                if (target == null) {
                    // Throw exception as target does not exist (neither in catalogs, nor in XML
                    // file)
                    // throw new TargetNotFoundException(projectFilepath, line);

                    // Create dummy target (instead of throwing an exception)
                    String projectName = (name == null) ? projectFile.getName() : name;
                    String targetName = line.substring(line.indexOf(",") + 1);
                    targets.add(new GenericTarget(targetName, projectName));
                } else {
                    targets.add(target);
                }

            }
        } catch (IOException ioe) {
            System.err.println("Cannot load project file: " + projectFile + "\n" + ioe);
        }

        if (targets.isEmpty()) { // Should never happen as exception should be thrown
            return null;
        }

        if (name == null) { // There was no explicit name set in the project file
            name = projectFile.getName(); // Use filename as project name
            name = name.substring(0, name.indexOf(".")); // Crop off file extension
        }

        // Create catalog

        return new ProjectCatalog(name, (ITarget[]) targets.toArray(new ITarget[] {}));

    }

}

class WaitPopup extends OMDialog {

    private static final long serialVersionUID = -3950819080525084021L;

    private ThreadGroup threadGroup = null;

    public WaitPopup(ThreadGroup threadGroup, ObservationManager om) {

        super(om);
        super.setLocationRelativeTo(om);
        PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle.getBundle("ObservationManager",
                Locale.getDefault());
        super.setTitle(bundle.getString("catalogLoader.info.waitOnLoaders"));

        this.threadGroup = threadGroup;

        super.getContentPane().setLayout(new BorderLayout());

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);

        super.getContentPane().add(progressBar, BorderLayout.CENTER);

        this.setSize(WaitPopup.serialVersionUID, 250, 60);
        // this.pack();

        Runnable wait = WaitPopup.this::waitForCatalogLoaders;

        Thread waitThread = new Thread(wait, "ProjectLoader: WaitPopup");
        waitThread.start();

        this.setVisible(true);

    }

    private void waitForCatalogLoaders() {

        while (this.threadGroup.activeCount() > 0) {
            try {
                this.threadGroup.wait(300);
            } catch (InterruptedException ie) {
                System.err.println("Interrupted while waiting for ThreadGroup.\n" + ie);
            } catch (IllegalMonitorStateException imse) {
                // Ignore this
                System.err.println("Ingnoring \n " + imse);
            }
        }
        this.dispose();

    }

}

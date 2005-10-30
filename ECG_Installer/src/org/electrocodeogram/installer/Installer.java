/*
 * Class: Installer
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */
package org.electrocodeogram.installer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.BaseInstallHandler;
import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.INonPluginEntry;

/**
 * This is a special <em>InstallHandler</em> used by the <em>Eclipse</em>
 * plugin update function. This installer puts the downloaded "sensor.properties" file
 * into the user's home dir.
 */
public class Installer extends BaseInstallHandler {

    /**
     * This is the <code>String</code> identifying this file resource inside the <em>Eclipse Feature</em>.
     */
    private static final String SENSOR_PROPERTIES_ID = "../sensor.properties";

    /**
     * The name that should be given to the file after download.
     */
    private static final String SENSOR_PROPERTIES_NAME = "sensor.properties";

    /**
     * If a file with the same name is already present, it is renamed to this.
     */
    private static final String SENSOR_PROPERTIES_BACKUP = "sensor.properties.bak";

    /**
     * The file will go into the user's home directory in a subdirectory with this name.
     */
    private static final String CONFIG_DIR = ".hackystat";

    /**
     * @see org.eclipse.update.core.IInstallHandler#installCompleted(boolean)
     */
    @Override
    public final void installCompleted(@SuppressWarnings("unused")
    final boolean success) throws CoreException {

        String homePath = System.getProperty("user.home");

        if (homePath == null) {
            throw new CoreException(new Status(IStatus.ERROR,
                "Unable to determine the user's home directory.", 0, "",
                new FileNotFoundException()));
        }

        String toPath = homePath + File.separator + CONFIG_DIR;

        for (INonPluginEntry resource : this.nonPluginEntries) {

            ContentReference[] content = this.feature
                .getFeatureContentProvider()
                .getNonPluginEntryArchiveReferences(resource, this.monitor);

            for (int i = 0; i < content.length; i++) {
                try {

                    if (isSensorProperties(content[i])) {
                        InputStream in = content[i].getInputStream();

                        byte[] data = new byte[(int) content[i].getInputSize()];

                        in.read(data);

                        in.close();

                        File configDir = new File(toPath);

                        configDir.mkdir();

                        File configFile = new File(configDir,
                            SENSOR_PROPERTIES_NAME);

                        if (configFile.exists()) {
                            configFile.renameTo(new File(configDir,
                                SENSOR_PROPERTIES_BACKUP));
                        }

                        FileOutputStream fos = new FileOutputStream(configFile,
                            false);

                        fos.write(data);

                        fos.close();

                    }

                } catch (IOException e) {

                    throw new CoreException(new Status(IStatus.ERROR,
                        "Error while reading non-plugin data.", 0, "", e));

                }
            }
        }
    }

    /**
     * Checks if the given resource has the expected name.
     * @param ref Is the downloaded resource.
     * @return <code>true</code> if it is the "sensor.properties" file and <code>false</code> otherwise
     */
    protected final boolean isSensorProperties(final ContentReference ref) {
        String id = ref.getIdentifier();
        return SENSOR_PROPERTIES_ID.equals(id);
    }

}

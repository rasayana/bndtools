package bndtools.launch;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;

import aQute.bnd.build.Project;
import aQute.bnd.build.Workspace;
import bndtools.Central;
import bndtools.Plugin;
import bndtools.builder.BndProjectNature;

final class LaunchUtils {

    private LaunchUtils() {
    }

    static IResource getTargetResource(ILaunchConfiguration configuration) throws CoreException {
        String target = configuration.getAttribute(LaunchConstants.ATTR_LAUNCH_TARGET, (String) null);
        if(target == null || target.length() == 0) {
            throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "Bnd launch target was not specified", null));
        }

        IResource targetResource = ResourcesPlugin.getWorkspace().getRoot().findMember(target);
        if(targetResource == null)
            throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, MessageFormat.format("Bnd launch target \"{0}\" does not exist.", target), null));
        return targetResource;
    }

    static Project getBndProject(ILaunchConfiguration configuration) throws CoreException {
        Project result;

        IResource targetResource = getTargetResource(configuration);

        IProject project = targetResource.getProject();
        File projectDir = project.getLocation().toFile();
        if(targetResource.getType() == IResource.FILE) {
            if(!targetResource.getName().endsWith(LaunchConstants.EXT_BNDRUN))
                throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, MessageFormat.format("Bnd launch target file \"{0}\" is not a .bndrun file.", targetResource.getFullPath().toString()), null));

            // Get the synthetic "run" project (based on a .bndrun file)
            File runFile = targetResource.getLocation().toFile();
            File bndbnd = new File(runFile.getParentFile(), Project.BNDFILE);
            try {
                if (bndbnd.isFile()) {
                    Project parent = new Project(Central.getWorkspace(), projectDir, bndbnd);
                    result = new Project(Central.getWorkspace(), projectDir, runFile);
                    result.setParent(parent);
                } else {
                    result = new Project(Central.getWorkspace(), projectDir, runFile);
                }
            } catch (Exception e) {
                throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, MessageFormat.format("Failed to create synthetic project for run file {0} in project {1}.", targetResource.getProjectRelativePath().toString(), project.getName()), e));
            }
        } else if(targetResource.getType() == IResource.PROJECT) {
            // Use the main project (i.e. bnd.bnd)
            if(!project.hasNature(BndProjectNature.NATURE_ID))
                throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, MessageFormat.format("The configured run project \"{0}\"is not a Bnd project.", project.getName()), null));
            try {
                result = Workspace.getProject(projectDir);
            } catch (Exception e) {
                throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, MessageFormat.format("Failed to retrieve Bnd project model for project \"{0}\".", project.getName()), null));
            }
        } else {
            throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, MessageFormat.format("The specified launch target \"{0}\" is not recognised as a Bnd project or .bndrun file.", targetResource.getFullPath().toString()), null));
        }

        return result;
    }
}

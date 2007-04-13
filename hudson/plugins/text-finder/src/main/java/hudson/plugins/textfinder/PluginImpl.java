package hudson.plugins.textfinder;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Entry point of the Hudson Text Finder plugin.
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @plugin
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.PUBLISHERS.addRecorder(TextFinderPublisher.DescriptorImpl.DESCRIPTOR);
    }
}

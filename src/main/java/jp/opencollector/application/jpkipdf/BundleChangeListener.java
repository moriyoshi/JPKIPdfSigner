package jp.opencollector.application.jpkipdf;

import java.util.EventListener;

public interface BundleChangeListener extends EventListener {
    void bundleChanged(BundleChangeEvent e);
}
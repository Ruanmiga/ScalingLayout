/**
 * @author: RealTechnology
 * Written during the end of 2022
 */

package org.real.technology.scaling.layout;

// Scaling Layout listener
public interface ScalingLayoutObserver {
    
    // Listener that is executed when the ScalingLayout is in State => COLLAPSED
    void onCollapsed()
    // Listener that is executed when the ScalingLayout is in State => EXPANDED
    void onExpanded()
    // Listener that is executed when the ScalingLayout is in State => PROGRESSED
    void onProgress(float progress)
    // Listener that runs when the Scaling Layout changes state
    void onStateChange(State state)
}

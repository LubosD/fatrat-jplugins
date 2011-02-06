/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.listeners.CaptchaListener;

/**
 *
 * @author lubos
 */
public abstract class CachedCaptchaListener implements CaptchaListener {
    String captchaId;

    public CachedCaptchaListener(String captchaId) {
        this.captchaId = captchaId;
    }

    public void onSolved(String text) {
        onSolved(text, captchaId);
    }


    abstract public void onSolved(String text, String captchaId);
}

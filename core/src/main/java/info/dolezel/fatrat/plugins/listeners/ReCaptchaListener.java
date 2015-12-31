/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins.listeners;

import java.util.EventListener;

/**
 *
 * @author lubos
 */
public interface ReCaptchaListener extends EventListener {
    /**
     * Called if FatRat failed to get a captcha solution.
     */
    void onFailed();

    /**
     * Called if a captcha solution was found or entered by the user.
     * @param text The solution
     */
    void onSolved(String text, String code);
}
